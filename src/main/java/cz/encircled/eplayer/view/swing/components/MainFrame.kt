package cz.encircled.eplayer.view.swing.components

import com.formdev.flatlaf.ui.FlatRootPaneUI
import com.formdev.flatlaf.ui.JBRCustomDecorations
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.UiUtil.inUiThread
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.SwingActions
import cz.encircled.eplayer.view.swing.components.quicknavi.QuickNaviPanel
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.fullscreen.adaptive.AdaptiveFullScreenStrategy
import uk.co.caprica.vlcj.player.embedded.fullscreen.windows.Win32FullScreenStrategy
import java.awt.*
import java.util.concurrent.CountDownLatch
import javax.swing.*

import java.awt.event.ActionEvent
import javax.swing.JOptionPane
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible


class MainFrame(
    val dataModel: UiDataModel,
    quickNaviController: QuickNaviController,
    val core: ApplicationCore
) : JFrame(), AppView {

    override var currentSceneProperty: ObjectProperty<Scenes> = SimpleObjectProperty()

    private lateinit var playerComponent: PlayerPanel
    private val quickNaviComponent: QuickNaviPanel

    private val isFullScreen = SimpleBooleanProperty(false)

    private val actions = SwingActions(this, core, quickNaviController)

    init {
        initTitle()

        layout = BorderLayout()
        minimumSize = Dimension(AppView.MIN_HEIGHT, AppView.MIN_WIDTH)
        size = Dimension(1920, 1080)
        defaultCloseOperation = EXIT_ON_CLOSE
        extendedState = MAXIMIZED_BOTH

        jMenuBar = SwingMenuBar(this, core, dataModel, quickNaviController, actions)

        quickNaviComponent = QuickNaviPanel(dataModel, quickNaviController, this)

        showQuickNaviScreen()

        registerShortcuts(quickNaviComponent)
    }

    private fun initTitle() {
        title = AppView.TITLE
        Event.playingChanged.listenUiThread {
            title = if (it.characteristic) "${AppView.TITLE} - ${it.playableMedia?.name()}"
            else AppView.TITLE
        }
    }

    override fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayerComponent) {
        playerComponent = PlayerPanel(this, core, mediaPlayer, jMenuBar)
        registerShortcuts(playerComponent)
    }

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
            if (this::playerComponent.isInitialized) remove(playerComponent)
            contentPane = quickNaviComponent
            quickNaviComponent.isVisible = true

            validate()
            repaint()
        }
    }

    override fun fullScreenProperty(): ReadOnlyBooleanProperty = isFullScreen

    override fun isFullScreen(): Boolean = isFullScreen.get()

    val h = Win32FullScreenStrategy(this)

    override fun toggleFullScreen() {
        if (h.isFullScreenMode) {
            invokeFlatUiFun("installClientDecorations")
            h.exitFullScreenMode()
        } else {
            invokeFlatUiFun("uninstallClientDecorations")

            h.enterFullScreenMode()
        }
        isFullScreen.set(h.isFullScreenMode)
//        graphicsConfiguration.device.fullScreenWindow = if (isFullScreen()) this else null
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
            actions.actions.forEach { (type, action) ->
                component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(action.keyStroke, type.name)
                component.actionMap.put(type.name, object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        action.action.invoke()
                    }
                })
            }
        }
    }
}
