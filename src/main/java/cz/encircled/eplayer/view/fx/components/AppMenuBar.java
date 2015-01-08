package cz.encircled.eplayer.view.fx.components;

import cz.encircled.elight.core.annotation.Component;
import cz.encircled.elight.core.annotation.Scope;
import cz.encircled.elight.core.annotation.Wired;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.view.fx.FxView;
import cz.encircled.eplayer.view.fx.PlayerScreen;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Encircled on 27/09/2014.
 */
@Component
@Scope(Scope.PROTOTYPE)
public class AppMenuBar {

    @Wired
    private ActionExecutor actionExecutor;

    @Wired
    private FxView appView;

    @Wired
    private EventObserver eventObserver;

    @Wired
    private PlayerScreen playerScreen;

    @Wired
    private MediaService mediaService;

    @Wired
    private QuickNaviScreen quickNaviScreen;

    @Wired
    private CacheService cacheService;

    private Menu subtitles;

    private Menu audioTracks;

    public MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(getFileMenu(), getViewMenu(), getMediaMenu(), getToolsMenu());
        return menuBar;
    }

    private Menu getFileMenu() {
        Menu file = new Menu(Localization.file.ln());

        MenuItem open = new MenuItem(Localization.open.ln());
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        open.setOnAction(event -> appView.openMedia());

        MenuItem exit = new MenuItem(Localization.exit.ln());
        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exit.setOnAction(event -> actionExecutor.execute(ActionCommands.EXIT));

        file.getItems().addAll(open, exit);
        return file;
    }

    private Menu getViewMenu() {
        Menu view = new Menu(Localization.view.ln());

        CheckMenuItem fullScreen = new CheckMenuItem(Localization.fullScreen.ln());
        appView.getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, newValue) -> fullScreen.setSelected(newValue));
        fullScreen.setOnAction(event -> appView.toggleFullScreen());
        fullScreen.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        fullScreen.setSelected(appView.isFullScreen());

        CheckMenuItem fitScreen = new CheckMenuItem(Localization.fitScreen.ln());
        playerScreen.fitToScreenProperty().addListener((observable, oldValue, newValue) -> fitScreen.setSelected(newValue));
        fitScreen.setOnAction(event -> playerScreen.toggleFitToScreen());
        fitScreen.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
        fitScreen.setSelected(playerScreen.fitToScreenProperty().get());

        view.getItems().addAll(fullScreen, fitScreen);
        return view;
    }

    private Menu getMediaMenu() {
        Menu media = new Menu(Localization.media.ln());

        MenuItem play = new MenuItem(Localization.play.ln());
        play.setOnAction(event -> mediaService.toggle());
        eventObserver.listenFxThread(Event.playingChanged, (event, isPlaying, arg2) -> {
            play.setText(isPlaying ? Localization.pause.ln() : Localization.play.ln());
        });

        subtitles = new Menu(Localization.subtitles.ln());
        audioTracks = new Menu(Localization.audioTrack.ln());
        subtitles.setDisable(true);
        audioTracks.setDisable(true);

        media.getItems().addAll(play, subtitles, audioTracks);

        return media;
    }

    private Menu getToolsMenu() {
        Menu tools = new Menu(Localization.tools.ln());

        MenuItem openQn = new MenuItem(Localization.openQuickNavi.ln());
        openQn.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        openQn.setOnAction(event -> actionExecutor.execute(ActionCommands.OPEN_QUICK_NAVI));

        MenuItem deleteMissing = new MenuItem(Localization.deleteMissing.ln());
        deleteMissing.setOnAction(event -> new Thread(() -> {
            Iterator<MediaType> iterator = cacheService.getCache().iterator();
            while(iterator.hasNext()) {
                MediaType m = iterator.next();
                if(!m.exists())
                    iterator.remove();
            }
            cacheService.save();
            Platform.runLater(quickNaviScreen::refreshCurrentTab);
        }).start());

        tools.getItems().addAll(openQn, deleteMissing);

        return tools;
    }

    @PostConstruct
    private void initialize() {
        eventObserver.listenFxThread(Event.subtitlesUpdated, (event, subTracks, arg2) -> {
            EventHandler<ActionEvent> subtitlesHandler = event1 -> {
                mediaService.setSubtitles((int) ((RadioMenuItem) event1.getSource()).getUserData());
            };
            updateTrackMenu(subtitles, subTracks, mediaService.getSubtitles(), subtitlesHandler);
        });

        eventObserver.listenFxThread(Event.audioTracksUpdated, (event, subTracks, arg2) -> {

            EventHandler<ActionEvent> audioTrackHandler = event1 -> {
                mediaService.setAudioTrack((int) ((RadioMenuItem) event1.getSource()).getUserData());
            };
            updateTrackMenu(audioTracks, subTracks, mediaService.getAudioTrack(), audioTrackHandler);

        });

        appView.screenChangeProperty().addListener((observable, oldValue, newValue) -> {
            if (FxView.QUICK_NAVI_SCREEN.equals(newValue)) {
                audioTracks.setDisable(true);
                subtitles.setDisable(true);
            }
        });
    }

    private void updateTrackMenu(Menu menu, List<TrackDescription> trackDescriptions, final int selected, EventHandler<ActionEvent> eventHandler) {
        ToggleGroup toggleGroup = new ToggleGroup();
        menu.getItems().setAll(trackDescriptions.stream().map(s -> {
            RadioMenuItem menuItem = new RadioMenuItem(s.description());

            menuItem.setSelected(s.id() == selected);
            menuItem.setToggleGroup(toggleGroup);
            menuItem.setUserData(s.id());
            menuItem.setOnAction(eventHandler);

            return menuItem;
        }).collect(Collectors.toList()));
        menu.setDisable(trackDescriptions.isEmpty());
    }

}
