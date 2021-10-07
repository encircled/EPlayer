package cz.encircled.eplayer.view

import cz.encircled.eplayer.view.swing.AppActions
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.util.concurrent.CountDownLatch

/**
 * @author Encircled on 16/09/2014.
 */
interface AppView {

    val actions: AppActions

    val currentSceneProperty: ObjectProperty<Scenes>

    fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayerComponent)

    fun showPlayer(countDownLatch: CountDownLatch)

    fun isFullScreen(): Boolean

    fun fullScreenProperty(): ReadOnlyBooleanProperty

    fun showQuickNaviScreen()

    fun toggleFullScreen()

    fun openMediaChooser()

    fun showUserMessage(msg: String)

    fun getUserConfirmation(msg: String, onConfirm: () -> Unit, onDecline: () -> Unit)

    fun scrollUp() {}

    fun scrollDown() {}

    companion object {
        const val TITLE = "EPlayer"
        const val MIN_WIDTH = 1024
        const val MIN_HEIGHT = 800

        const val PLAYER_CONTROLS_HEIGHT = 60

        const val SCREENSHOT_WIDTH = 336
        const val SCREENSHOT_HEIGHT = 189
    }

}