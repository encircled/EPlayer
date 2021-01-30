package cz.encircled.eplayer.view.swing

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.controller.QuickNaviController
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.security.Key
import javax.swing.KeyStroke

class SwingActions(appView: AppView, core: ApplicationCore, controller: QuickNaviController) {

    val actions = mapOf(
        ActionType.FULL_SCREEN to SwingAction(shortcut(KeyEvent.VK_F)) {
            appView.toggleFullScreen()
        },
        ActionType.QUICK_NAVI to SwingAction(shortcut(KeyEvent.VK_N)) {
            core.openQuickNaviScreen()
        },
        ActionType.BACK to SwingAction(KeyStroke.getKeyStroke("ESCAPE")) {
            core.back()
        },
        ActionType.TOGGLE_PLAYER to SwingAction(KeyStroke.getKeyStroke("SPACE")) {
            core.mediaService.toggle()
        },
        ActionType.EXIT to SwingAction(shortcut(KeyEvent.VK_Q)) {
            core.exit()
        },

        ActionType.MOVE_RIGHT to SwingAction(KeyStroke.getKeyStroke("RIGHT")) {
            when (appView.currentSceneProperty.get()!!) {
                Scenes.PLAYER -> core.mediaService.setTimePlus(2000)
                Scenes.QUICK_NAVI -> controller.goToNextMedia()
            }
        },
        ActionType.MOVE_LEFT to SwingAction(KeyStroke.getKeyStroke("LEFT")) {
            when (appView.currentSceneProperty.get()!!) {
                Scenes.PLAYER -> core.mediaService.setTimePlus(-2000)
                Scenes.QUICK_NAVI -> controller.goToPrevMedia()
            }
        },

        ActionType.SCROLL_UP to SwingAction(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)) {
            appView.scrollUp()
        },
        ActionType.SCROLL_DOWN to SwingAction(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0)) {
            appView.scrollDown()
        }
    )

    private fun shortcut(key: Int): KeyStroke =
        KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().menuShortcutKeyMask)

}

data class SwingAction(
    val keyStroke: KeyStroke,
    val action: () -> Unit,
)

enum class ActionType {
    TOGGLE_PLAYER,
    BACK,
    FULL_SCREEN,
    QUICK_NAVI,
    EXIT,

    MOVE_RIGHT,
    MOVE_LEFT,

    SCROLL_UP,
    SCROLL_DOWN,
}