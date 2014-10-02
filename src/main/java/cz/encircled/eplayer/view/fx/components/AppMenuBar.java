package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.LocalizedMessages;
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
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Encircled on 27/09/2014.
 */
@Resource
@Scope(Scope.PROTOTYPE)
public class AppMenuBar {

    @Resource
    private ActionExecutor actionExecutor;

    @Resource
    private FxView appView;

    @Resource
    private EventObserver eventObserver;

    @Resource
    private PlayerScreen playerScreen;

    @Resource
    private MediaService mediaService;

    @Resource
    private QuickNaviScreen quickNaviScreen;

    @Resource
    private CacheService cacheService;

    private Menu subtitles;

    private Menu audioTracks;

    public MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(getFileMenu(), getViewMenu(), getMediaMenu(), getToolsMenu());
        return menuBar;
    }

    private Menu getFileMenu() {
        Menu file = new Menu(Localizations.get(LocalizedMessages.FILE));

        MenuItem open = new MenuItem(Localizations.get(LocalizedMessages.OPEN));
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        open.setOnAction(event -> appView.openMedia());

        MenuItem exit = new MenuItem(Localizations.get(LocalizedMessages.EXIT));
        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exit.setOnAction(event -> actionExecutor.execute(ActionCommands.EXIT));

        file.getItems().addAll(open, exit);
        return file;
    }

    private Menu getViewMenu() {
        Menu view = new Menu(Localizations.get(LocalizedMessages.VIEW));

        CheckMenuItem fullScreen = new CheckMenuItem(Localizations.get(LocalizedMessages.FULL_SCREEN));
        appView.getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, newValue) -> fullScreen.setSelected(newValue));
        fullScreen.setOnAction(event -> appView.toggleFullScreen());
        fullScreen.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        fullScreen.setSelected(appView.isFullScreen());

        CheckMenuItem fitScreen = new CheckMenuItem(Localizations.get(LocalizedMessages.FIT_SCREEN));
        playerScreen.fitToScreenProperty().addListener((observable, oldValue, newValue) -> fitScreen.setSelected(newValue));
        fitScreen.setOnAction(event -> playerScreen.toggleFitToScreen());
        fitScreen.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
        fitScreen.setSelected(playerScreen.fitToScreenProperty().get());

        view.getItems().addAll(fullScreen, fitScreen);
        return view;
    }

    private Menu getMediaMenu() {
        Menu media = new Menu(Localizations.get(LocalizedMessages.MEDIA));

        MenuItem play = new MenuItem(Localizations.get(LocalizedMessages.PLAY));
        play.setOnAction(event -> mediaService.toggle());
        eventObserver.listenFxThread(Event.playingChanged, (event, isPlaying, arg2) -> {
            play.setText(Localizations.get(isPlaying ? LocalizedMessages.PAUSE : LocalizedMessages.PLAY));
        });

        subtitles = new Menu(Localizations.get(LocalizedMessages.SUBTITLES));
        audioTracks = new Menu(Localizations.get(LocalizedMessages.AUDIO_TRACK));
        subtitles.setDisable(true);
        audioTracks.setDisable(true);

        media.getItems().addAll(play, subtitles, audioTracks);

        return media;
    }

    private Menu getToolsMenu() {
        Menu tools = new Menu(Localizations.get(LocalizedMessages.TOOLS));

        MenuItem openQn = new MenuItem(Localizations.get(LocalizedMessages.OPEN_QUICK_NAVI));
        openQn.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        openQn.setOnAction(event -> actionExecutor.execute(ActionCommands.OPEN_QUICK_NAVI));

        MenuItem deleteMissing = new MenuItem(Localizations.get(LocalizedMessages.DELETE_MISSING));
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
