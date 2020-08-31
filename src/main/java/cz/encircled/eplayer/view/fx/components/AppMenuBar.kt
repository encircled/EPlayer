package cz.encircled.eplayer.view.fx.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.fx.FxView
import cz.encircled.eplayer.view.fx.addNewValueListener
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

/**
 * @author Encircled on 27/09/2014.
 */
class AppMenuBar(private val core: ApplicationCore, private val fxView: FxView) {
    lateinit var subtitles: Menu
    lateinit var audioTracks: Menu

    fun getMenuBar(): MenuBar = MenuBar(fileMenu(), viewMenu(), mediaMenu(), toolsMenu())

    private fun fileMenu(): Menu {
        val file = Menu(Localization.file.ln())

        val open = MenuItem(Localization.open.ln())
        open.accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
        open.onAction = EventHandler { fxView.openMediaChooser() }

        val exit = MenuItem(Localization.exit.ln())
        exit.accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)
        exit.onAction = EventHandler { core.exit() }

        file.items.addAll(open, exit)
        return file
    }

    private fun viewMenu(): Menu {
        val view = Menu(Localization.view.ln())

        val fullScreen = CheckMenuItem(Localization.fullScreen.ln())
        fxView.primaryStage.fullScreenProperty().addNewValueListener { newValue: Boolean? -> fullScreen.isSelected = newValue!! }
        fullScreen.onAction = EventHandler { fxView.toggleFullScreen() }
        fullScreen.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
        fullScreen.isSelected = fxView.isFullScreen

        val fitScreen = CheckMenuItem(Localization.fitScreen.ln())
        fxView.playerScreen.fitToScreenProperty().addListener { _: ObservableValue<out Boolean?>?, _: Boolean?, newValue: Boolean? -> fitScreen.isSelected = newValue!! }
        fitScreen.onAction = EventHandler { fxView.playerScreen.toggleFitToScreen() }
        fitScreen.accelerator = KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN)
        fitScreen.isSelected = fxView.playerScreen.fitToScreenProperty().get()

        val back = MenuItem(Localization.back.ln())
        back.onAction = EventHandler { core.back() }
        back.accelerator = KeyCodeCombination(KeyCode.ESCAPE)
        view.items.addAll(fullScreen, fitScreen)

        return view
    }

    private fun mediaMenu(): Menu {
        val media = Menu(Localization.media.ln())

        val play = MenuItem(Localization.play.ln())
        play.onAction = EventHandler { core.mediaService.toggle() }
        play.accelerator = KeyCodeCombination(KeyCode.SPACE)
        core.eventObserver.listenFxThread(Event.playingChanged) { isPlaying: Boolean ->
            play.text = if (isPlaying) Localization.pause.ln() else Localization.play.ln()
        }

        subtitles = Menu(Localization.subtitles.ln())
        audioTracks = Menu(Localization.audioTrack.ln())
        subtitles.isDisable = true
        audioTracks.isDisable = true

        media.items.addAll(play, subtitles, audioTracks)
        return media
    }

    private fun toolsMenu(): Menu {
        val tools = Menu(Localization.tools.ln())

        val openQn = MenuItem(Localization.openQuickNavi.ln())
        openQn.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
        openQn.onAction = EventHandler { core.openQuickNavi() }

        val deleteMissing = MenuItem(Localization.deleteMissing.ln())
        deleteMissing.onAction = EventHandler {
            Thread {
                core.cacheService.cache.removeIf { !it.mediaFile().exists() }
                core.cacheService.save()
                Platform.runLater { fxView.quickNaviScreen.refreshCurrentTab() }
            }.start()
        }

        tools.items.addAll(openQn, deleteMissing)
        return tools
    }

    fun init() {
        core.eventObserver.listenFxThread(Event.subtitlesUpdated) { tracks: List<GenericTrackDescription> ->
            updateTrackMenu(subtitles, tracks, core.mediaService.subtitles) {
                core.mediaService.subtitles = (it.source as RadioMenuItem).userData as Int
            }
        }

        core.eventObserver.listenFxThread(Event.audioTracksUpdated) { tracks: List<GenericTrackDescription> ->
            updateTrackMenu(audioTracks, tracks, core.mediaService.audioTrack) {
                core.mediaService.audioTrack = (it.source as RadioMenuItem).userData as Int
            }
        }

        fxView.screenChangeProperty.addNewValueListener {
            if (FxView.QUICK_NAVI_SCREEN == it) {
                audioTracks.isDisable = true
                subtitles.isDisable = true
            }
        }
    }

    private fun updateTrackMenu(menu: Menu, trackDescriptions: List<GenericTrackDescription>, selected: Int, eventHandler: EventHandler<ActionEvent>) {
        val toggleGroup = ToggleGroup()
        menu.items.setAll(trackDescriptions.map {
            RadioMenuItem(it.description).apply {
                isSelected = it.id == selected
                this.toggleGroup = toggleGroup
                userData = it.id
                onAction = eventHandler
            }
        })
        menu.isDisable = trackDescriptions.isEmpty()
    }

}