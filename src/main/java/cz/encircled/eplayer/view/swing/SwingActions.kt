package cz.encircled.eplayer.view.swing

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.controller.PlayerController
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.ActionType.*
import java.awt.Toolkit
import java.awt.event.KeyEvent
import javax.swing.KeyStroke
import javax.swing.KeyStroke.getKeyStroke

interface AppActions : RemoteControlHandler {

    fun invoke(action: ActionType)

    fun openQuickNaviScreen()
}

class SwingActions(
    val appView: AppView,
    val core: ApplicationCore,
    val controller: QuickNaviController,
    val playerController: PlayerController,
) : AppActions {

    val actions = mapOf(
        FULL_SCREEN to SwingAction(cmdShortcut(KeyEvent.VK_F), this::toFullScreen),

        QUICK_NAVI to SwingAction(cmdShortcut(KeyEvent.VK_N), this::openQuickNaviScreen),

        TOGGLE_AUDIO_PASS_THROUGH to SwingAction(cmdShortcut(KeyEvent.VK_P)) {
            core.settings.audioPassThrough(!core.settings.audioPassThrough)
            Event.audioPassThroughChange.fire(core.settings.audioPassThrough)
        },

        BACK to SwingAction(shortcut("ESCAPE") + shortcut("BACK_SPACE"), this::back),

        TOGGLE_PLAYER to SwingAction(shortcut("SPACE"), this::playPause),

        EXIT to SwingAction(cmdShortcut(KeyEvent.VK_Q)) {
            core.exit()
        },

        MOVE_RIGHT to SwingAction(shortcut("RIGHT")) {
            when (appView.currentSceneProperty.get()!!) {
                Scenes.PLAYER -> core.mediaService.setTimePlus(7000)
                Scenes.QUICK_NAVI -> controller.goToNextMedia()
            }
        },
        MOVE_LEFT to SwingAction(shortcut("LEFT")) {
            when (appView.currentSceneProperty.get()!!) {
                Scenes.PLAYER -> core.mediaService.setTimePlus(-7000)
                Scenes.QUICK_NAVI -> controller.goToPrevMedia()
            }
        },

        TO_NEXT_MEDIA to SwingAction(shortcut("ctrl RIGHT"), this::goToNextMedia),

        TO_PREV_MEDIA to SwingAction(shortcut("ctrl LEFT"), this::goToPrevMedia),

        VOLUME_UP to SwingAction(shortcut("UP"), this::volumeUp),
        VOLUME_DOWN to SwingAction(shortcut("DOWN"), this::volumeDown),

        SCROLL_UP to SwingAction(listOf(getKeyStroke(KeyEvent.VK_PAGE_UP, 0))) {
            appView.scrollUp()
        },
        SCROLL_DOWN to SwingAction(listOf(getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0))) {
            appView.scrollDown()
        }
    )

    override fun invoke(action: ActionType) {
        actions.getValue(action).action.invoke()
    }

    override fun openQuickNaviScreen() {
        if (core.mediaService.isPlaying()) {
            core.mediaService.pause()
        }
        appView.showQuickNaviScreen()
        core.mediaService.stop()
        core.cacheService.save()
    }

    override fun playPause() = playerController.togglePlaying()

    override fun playSelected() = controller.playSelected()

    override fun watchLastMedia() = controller.watchLastMedia()

    override fun back() {
        if (appView.currentSceneProperty.get() == Scenes.PLAYER) {
            if (appView.isFullScreen()) {
                appView.toggleFullScreen()
                core.mediaService.pause()
            } else {
                openQuickNaviScreen()
            }
        } else if (appView.currentSceneProperty.get() == Scenes.QUICK_NAVI) {
            controller.back()
        }
    }

    override fun toFullScreen() = appView.toggleFullScreen()

    override fun goToNextMedia() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.playNext()
        } else {
            controller.goToNextMedia()
        }
    }

    override fun goToPrevMedia() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.playPrevious()
        } else {
            controller.goToPrevMedia()
        }
    }

    /**
     * Scroll play time or forward to next series episode
     */
    override fun forward() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.setTimePlus(7000)
        } else {
            controller.forward()
        }
    }

    /**
     * Scroll play time or forward to prev series episode
     */
    override fun backward() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.setTimePlus(-7000)
        } else {
            controller.backward()
        }
    }

    override fun volumeUp() {
        playerController.volume = playerController.volume + 5
    }

    override fun volumeDown() {
        playerController.volume = playerController.volume - 5
    }

    private fun cmdShortcut(key: Int): List<KeyStroke> =
        listOf(getKeyStroke(key, Toolkit.getDefaultToolkit().menuShortcutKeyMask))

    private fun shortcut(key: String): List<KeyStroke> = listOf(getKeyStroke(key))

}

data class SwingAction(
    val keyStrokes: List<KeyStroke>,
    val action: () -> Unit,
)

enum class ActionType {
    TOGGLE_PLAYER,
    BACK,
    FULL_SCREEN,
    QUICK_NAVI,
    EXIT,

    TOGGLE_AUDIO_PASS_THROUGH,

    MOVE_RIGHT,
    MOVE_LEFT,

    TO_NEXT_MEDIA,
    TO_PREV_MEDIA,

    VOLUME_UP,
    VOLUME_DOWN,

    SCROLL_UP,
    SCROLL_DOWN,
}
