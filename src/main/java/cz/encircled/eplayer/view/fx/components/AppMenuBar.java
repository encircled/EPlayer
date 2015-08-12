package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.view.fx.FxView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.TrackDescription;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Encircled on 27/09/2014.
 */
public class AppMenuBar {

    private FxView fxView;

    private ApplicationCore core;

    private Menu subtitles;

    private Menu audioTracks;

    public AppMenuBar(ApplicationCore core, FxView fxView) {
        this.core = core;
        this.fxView = fxView;
        init();
    }

    @NotNull
    public MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(getFileMenu(), getViewMenu(), getMediaMenu(), getToolsMenu());
        return menuBar;
    }

    @NotNull
    private Menu getFileMenu() {
        Menu file = new Menu(Localization.file.ln());

        MenuItem open = new MenuItem(Localization.open.ln());
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        open.setOnAction(event -> fxView.openMedia());

        MenuItem exit = new MenuItem(Localization.exit.ln());
        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exit.setOnAction(event -> core.exit());

        file.getItems().addAll(open, exit);
        return file;
    }

    @NotNull
    private Menu getViewMenu() {
        Menu view = new Menu(Localization.view.ln());

        CheckMenuItem fullScreen = new CheckMenuItem(Localization.fullScreen.ln());
        fxView.getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, newValue) -> fullScreen.setSelected(newValue));
        fullScreen.setOnAction(event -> fxView.toggleFullScreen());
        fullScreen.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        fullScreen.setSelected(fxView.isFullScreen());

        CheckMenuItem fitScreen = new CheckMenuItem(Localization.fitScreen.ln());
        fxView.getPlayerScreen().fitToScreenProperty().addListener((observable, oldValue, newValue) -> fitScreen.setSelected(newValue));
        fitScreen.setOnAction(event -> fxView.getPlayerScreen().toggleFitToScreen());
        fitScreen.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));
        fitScreen.setSelected(fxView.getPlayerScreen().fitToScreenProperty().get());

        view.getItems().addAll(fullScreen, fitScreen);
        return view;
    }

    @NotNull
    private Menu getMediaMenu() {
        Menu media = new Menu(Localization.media.ln());

        MenuItem play = new MenuItem(Localization.play.ln());
        play.setOnAction(event -> core.getMediaService().toggle());
        core.getEventObserver().listenFxThread(Event.playingChanged, (event, isPlaying, arg2) -> {
            play.setText(isPlaying ? Localization.pause.ln() : Localization.play.ln());
        });

        subtitles = new Menu(Localization.subtitles.ln());
        audioTracks = new Menu(Localization.audioTrack.ln());
        subtitles.setDisable(true);
        audioTracks.setDisable(true);

        media.getItems().addAll(play, subtitles, audioTracks);

        return media;
    }

    @NotNull
    private Menu getToolsMenu() {
        Menu tools = new Menu(Localization.tools.ln());

        MenuItem openQn = new MenuItem(Localization.openQuickNavi.ln());
        openQn.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        openQn.setOnAction(event -> core.openQuickNavi());

        MenuItem deleteMissing = new MenuItem(Localization.deleteMissing.ln());
        deleteMissing.setOnAction(event -> new Thread(() -> {
            Iterator<MediaType> iterator = core.getCacheService().getCache().iterator();
            while (iterator.hasNext()) {
                MediaType m = iterator.next();
                if (!m.exists())
                    iterator.remove();
            }
            core.getCacheService().save();
            Platform.runLater(fxView.getQuickNaviScreen()::refreshCurrentTab);
        }).start());

        tools.getItems().addAll(openQn, deleteMissing);

        return tools;
    }

    public void init() {
        core.getEventObserver().listenFxThread(Event.subtitlesUpdated, (event, subTracks, arg2) -> {
            EventHandler<ActionEvent> subtitlesHandler = event1 -> {
                core.getMediaService().setSubtitles((int) ((RadioMenuItem) event1.getSource()).getUserData());
            };
            updateTrackMenu(subtitles, subTracks, core.getMediaService().getSubtitles(), subtitlesHandler);
        });

        core.getEventObserver().listenFxThread(Event.audioTracksUpdated, (event, subTracks, arg2) -> {

            EventHandler<ActionEvent> audioTrackHandler = event1 -> {
                core.getMediaService().setAudioTrack((int) ((RadioMenuItem) event1.getSource()).getUserData());
            };
            updateTrackMenu(audioTracks, subTracks, core.getMediaService().getAudioTrack(), audioTrackHandler);

        });

        fxView.screenChangeProperty().addListener((observable, oldValue, newValue) -> {
            if (FxView.QUICK_NAVI_SCREEN.equals(newValue)) {
                audioTracks.setDisable(true);
                subtitles.setDisable(true);
            }
        });
    }

    private void updateTrackMenu(@NotNull Menu menu, @NotNull List<TrackDescription> trackDescriptions, final int selected, EventHandler<ActionEvent> eventHandler) {
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
