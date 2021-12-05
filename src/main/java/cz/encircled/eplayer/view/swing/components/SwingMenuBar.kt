package cz.encircled.eplayer.view.swing.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.MediaCharacteristic
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.ActionType
import cz.encircled.eplayer.view.swing.SwingActions
import cz.encircled.eplayer.view.swing.components.base.BaseJMenu
import cz.encircled.fswing.addAll
import cz.encircled.fswing.components.Cancelable
import cz.encircled.fswing.components.RemovalAware
import cz.encircled.fswing.onChange
import java.awt.Toolkit
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.*

class SwingMenuBar(
    val appView: AppView,
    val core: ApplicationCore,
    val dataModel: UiDataModel,
    val controller: QuickNaviController,
    val swingActions: SwingActions,
) : JMenuBar() {

    lateinit var subtitles: JMenu
    lateinit var audioTracks: JMenu

    init {
        addAll(
            fileMenu(),
            viewMenu(),
            mediaMenu(),
            toolsMenu()
        )

        Event.subtitlesUpdated.listenUiThread { tracks: List<GenericTrackDescription> ->
            updateTrackMenu(subtitles, tracks, core.mediaService.currentSubtitle(), Event.subtitleChanged) {
                core.mediaService.setSubtitle((it.source as RadioItem).track)
            }
        }

        Event.audioTrackChanged.listenUiThread {
            it.characteristic
        }
        Event.audioTracksUpdated.listenUiThread { tracks: List<GenericTrackDescription> ->
            updateTrackMenu(audioTracks, tracks, core.mediaService.currentAudioTrack(), Event.audioTrackChanged) {
                core.mediaService.setAudioTrack((it.source as RadioItem).track)
            }
        }

        appView.currentSceneProperty.onChange {
            audioTracks.isEnabled = it == Scenes.PLAYER
            subtitles.isEnabled = it == Scenes.PLAYER
        }
    }

    private fun fileMenu(): JMenu {
        val file = JMenu(Localization.file.ln())

        val open = JMenuItem(Localization.open.ln())
        open.accelerator = shortcut(KeyEvent.VK_O)
        open.addActionListener { appView.openMediaChooser() }

        val exit = JMenuItem(Localization.exit.ln())
        exit.onMenuClick(ActionType.EXIT)

        file.addAll(open, exit)
        return file
    }

    private fun viewMenu(): JMenu {
        val view = JMenu(Localization.view.ln())

        val fullScreen = JCheckBoxMenuItem(Localization.fullScreen.ln())
        appView.fullScreenProperty().onChange { fullScreen.isSelected = it }
        fullScreen.onMenuClick(ActionType.FULL_SCREEN)
        fullScreen.isSelected = appView.isFullScreen()

        val fitScreen = JCheckBoxMenuItem(Localization.fitScreen.ln())
        dataModel.fitToScreen.onChange { fitScreen.isSelected = it }
        fitScreen.addActionListener { dataModel.toggleFitToScreen() }
        fitScreen.accelerator = shortcut(KeyEvent.VK_G)
        fitScreen.isSelected = dataModel.fitToScreen.get()

        val back = JMenuItem(Localization.back.ln())
        back.onMenuClick(ActionType.BACK)
        view.addAll(fullScreen, fitScreen, back)

        return view
    }

    private fun mediaMenu(): JMenu {
        val media = JMenu(Localization.media.ln())

        val play = JMenuItem(Localization.play.ln())
        play.onMenuClick(ActionType.TOGGLE_PLAYER)
        Event.playingChanged.listenUiThread {
            play.text = if (it.characteristic) Localization.pause.ln() else Localization.play.ln()
        }

        subtitles = BaseJMenu(Localization.subtitles.ln())
        audioTracks = BaseJMenu(Localization.audioTrack.ln())
        subtitles.isEnabled = false
        audioTracks.isEnabled = false

        media.addAll(play, subtitles, audioTracks)
        return media
    }

    private fun toolsMenu(): JMenu {
        val tools = BaseJMenu(Localization.tools.ln())

        val openQn = JMenuItem(Localization.openQuickNavi.ln())
        openQn.onMenuClick(ActionType.QUICK_NAVI)

        // TODO just hide/remove it
        val deleteMissing = JMenuItem(Localization.deleteMissing.ln())
        deleteMissing.addActionListener {
            Thread {
                core.cacheService.getCachedMedia().filter {
                    !it.mediaFile().exists()
                }.forEach {
                    core.cacheService.deleteEntry(it)
                }
                core.cacheService.save()
            }.start()
        }

        val audioPassThrough = JRadioButtonMenuItem(Localization.audioPassThrough.ln())
        audioPassThrough.isSelected = core.settings.audioPassThrough
        audioPassThrough.onMenuClick(ActionType.TOGGLE_AUDIO_PASS_THROUGH)
        Event.audioPassThroughChange.listenUiThread { audioPassThrough.isSelected = it }

        tools.addAll(openQn, deleteMissing, audioPassThrough)
        return tools
    }

    private fun shortcut(key: Int): KeyStroke =
        KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().menuShortcutKeyMask)

    private fun updateTrackMenu(
        menu: JMenu,
        trackDescriptions: List<GenericTrackDescription>,
        selected: GenericTrackDescription?,
        changeEvent: Event<MediaCharacteristic<GenericTrackDescription>>,
        eventHandler: ActionListener
    ) {
        menu.removeAll()
        val buttonGroup = ButtonGroup()
        trackDescriptions.map {
            RadioItem(it.description, it).apply {
                isSelected = it == selected
                addActionListener(eventHandler)
                this.model.setGroup(buttonGroup)

                changeEvent.listenUiThread { newValue ->
                    isSelected = this.track == newValue.characteristic
                }.cancelOnRemove()
            }
        }.forEach {
            menu.add(it)
        }
        menu.isEnabled = trackDescriptions.isNotEmpty()
    }

    private fun AbstractButton.onMenuClick(type: ActionType) {
        this.addActionListener { swingActions.invoke(type) }
    }

    private class RadioItem(description: String, val track: GenericTrackDescription) :
        JRadioButtonMenuItem(description), RemovalAware {

        override val cancelableListeners: MutableList<Cancelable> = ArrayList()

    }

}