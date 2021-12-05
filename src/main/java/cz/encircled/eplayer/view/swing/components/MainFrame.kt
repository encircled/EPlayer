package cz.encircled.eplayer.view.swing.components

import com.formdev.flatlaf.ui.FlatRootPaneUI
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.controller.PlayerController
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.AppActions
import cz.encircled.eplayer.view.swing.SwingActions
import cz.encircled.eplayer.view.swing.components.quicknavi.QuickNaviPanel
import cz.encircled.fswing.inUiThread
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.fullscreen.windows.Win32FullScreenStrategy
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.util.concurrent.CountDownLatch
import javax.swing.*
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible


class MainFrame(
    val dataModel: UiDataModel,
    quickNaviController: QuickNaviController,
    playerController: PlayerController,
    val core: ApplicationCore
) : JFrame(), AppView {

    val log = LogManager.getLogger()

    override var currentSceneProperty: ObjectProperty<Scenes> = SimpleObjectProperty()

    private var playerComponent: PlayerPanel
    private val quickNaviComponent: QuickNaviPanel

    private val fullScreenStrategy = Win32FullScreenStrategy(this)
    private val isFullScreen = SimpleBooleanProperty(false)

    override val actions: AppActions = SwingActions(this, core, quickNaviController, playerController)

    init {
        initTitle()

        layout = BorderLayout()
        minimumSize = Dimension(AppView.MIN_HEIGHT, AppView.MIN_WIDTH)
        size = Dimension(1920, 1080)
        defaultCloseOperation = EXIT_ON_CLOSE
        extendedState = MAXIMIZED_BOTH

        jMenuBar = SwingMenuBar(this, core, dataModel, quickNaviController, actions as SwingActions)

        quickNaviComponent = QuickNaviPanel(dataModel, quickNaviController, this)

        playerComponent = PlayerPanel(this, core, jMenuBar, playerController)
        showQuickNaviScreen()

        // Remove default actions so it can be overridden
        val tabActionMap = UIManager.get("TabbedPane.actionMap") as ActionMap
        tabActionMap.remove("navigateLeft")
        tabActionMap.remove("navigateRight")

        val listActionMap = UIManager.get("List.actionMap") as ActionMap
        // Uses 'space'
        listActionMap.remove("addToSelection")

        registerShortcuts(quickNaviComponent)

        registerShortcuts(playerComponent)
    }

    private fun initTitle() {
        title = AppView.TITLE
        Event.playingChanged.listenUiThread {
            title = if (it.playableMedia != null) "${AppView.TITLE} - ${it.playableMedia.name()}"
            else AppView.TITLE
        }
    }

    override fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayerComponent) = playerComponent.setPlayer(mediaPlayer)

    override fun showPlayer(countDownLatch: CountDownLatch) = inUiThread {
        try {
            if (currentSceneProperty.get() != Scenes.PLAYER) {
                currentSceneProperty.set(Scenes.PLAYER)
                if (isFullScreen()) {
                    toggleFullScreen()
                }
                quickNaviComponent.isVisible = false
                remove(quickNaviComponent)
                contentPane = playerComponent
                validate()
                repaint()
            }
        } finally {
            countDownLatch.countDown()
        }
    }

    override fun showQuickNaviScreen() = inUiThread {
        if (currentSceneProperty.get() == null || currentSceneProperty.get() != Scenes.QUICK_NAVI) {
            currentSceneProperty.set(Scenes.QUICK_NAVI)
            remove(playerComponent)
            contentPane = quickNaviComponent
            quickNaviComponent.isVisible = true

            validate()
            repaint()
        }
    }

    override fun fullScreenProperty(): ReadOnlyBooleanProperty = isFullScreen

    override fun isFullScreen(): Boolean = isFullScreen.get()

    override fun toggleFullScreen() {
        if (fullScreenStrategy.isFullScreenMode) {
            invokeFlatUiFun("installClientDecorations")
            fullScreenStrategy.exitFullScreenMode()
        } else {
            invokeFlatUiFun("uninstallClientDecorations")
            fullScreenStrategy.enterFullScreenMode()
        }
        isFullScreen.set(fullScreenStrategy.isFullScreenMode)
    }

    private fun invokeFlatUiFun(funName: String) {
        val toInvoke = FlatRootPaneUI::class.declaredFunctions.first { it.name == funName }
        if (!toInvoke.isAccessible) toInvoke.isAccessible = true
        toInvoke.call(rootPane.ui)
    }

    override fun openMediaChooser() {
        TODO("Not yet implemented")
    }

    override fun showUserMessage(msg: String) = inUiThread {
        JOptionPane.showMessageDialog(this, msg, Localization.errorTitle.ln(), JOptionPane.ERROR_MESSAGE)
    }

    override fun getUserConfirmation(msg: String, onConfirm: () -> Unit, onDecline: () -> Unit) {
        when (JOptionPane.showConfirmDialog(
            this,
            msg,
            Localization.confirmTitle.ln(),
            JOptionPane.INFORMATION_MESSAGE
        )) {
            0 -> onConfirm.invoke()
            1 -> onDecline.invoke()
        }
    }

    override fun scrollUp() {
        if (currentSceneProperty.get() == Scenes.QUICK_NAVI) {
            quickNaviComponent.scrollUp()
        }
    }

    override fun scrollDown() {
        if (currentSceneProperty.get() == Scenes.QUICK_NAVI) {
            quickNaviComponent.scrollDown()
        }
    }

    private fun registerShortcuts(component: JComponent) {
        if (component.actionMap.size() == 0) {
            (actions as SwingActions).actions.forEach { (type, action) ->

                action.keyStrokes.forEach {
                    component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(it, type.name)
                }

                component.actionMap.put(type.name, object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        log.info("Performing action for shortcut ${type.name}")
                        action.action.invoke()
                    }
                })
            }
        }
    }
}
