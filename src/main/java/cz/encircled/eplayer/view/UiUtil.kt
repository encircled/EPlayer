package cz.encircled.eplayer.view

import javafx.application.Platform
import javafx.scene.control.ScrollPane
import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities

/**
 * @author Encircled on 20/09/2014.
 */
object UiUtil {

    lateinit var uiExecutor: UiExecutor

    fun inNormalThread(runnable: Runnable) = uiExecutor.inNormalThread(runnable)

    fun inUiThread(runnable: Runnable) = uiExecutor.inUiThread(runnable)

    fun inUiThread(countDownLatch: CountDownLatch, runnable: Runnable) = uiExecutor.inUiThread(countDownLatch, runnable)

    fun withFastScroll(pane: ScrollPane): ScrollPane =
        pane.apply {
            content.setOnScroll { scrollEvent ->
                val deltaY: Double = scrollEvent.deltaY * 0.01
                pane.vvalue = pane.vvalue - deltaY
            }
        }

}

interface UiExecutor {

    fun inNormalThread(runnable: Runnable)
    fun inUiThread(runnable: Runnable)
    fun inUiThread(countDownLatch: CountDownLatch, runnable: Runnable)

}

class FxUiExecutor : UiExecutor {

    override fun inNormalThread(runnable: Runnable) {
        if (!Platform.isFxApplicationThread()) {
            runnable.run()
        } else {
            Thread(runnable).start()
        }
    }

    override fun inUiThread(countDownLatch: CountDownLatch, runnable: Runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run()
            countDownLatch.countDown()
        } else {
            Platform.runLater {
                runnable.run()
                countDownLatch.countDown()
            }
        }
    }

    override fun inUiThread(runnable: Runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run()
        } else {
            Platform.runLater(runnable)
        }
    }

}

class SwingEUiExecutor : UiExecutor {

    override fun inNormalThread(runnable: Runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            runnable.run()
        } else {
            Thread(runnable).start()
        }
    }

    override fun inUiThread(runnable: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run()
        } else {
            SwingUtilities.invokeLater(runnable)
        }
    }

    override fun inUiThread(countDownLatch: CountDownLatch, runnable: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run()
            countDownLatch.countDown()
        } else {
            SwingUtilities.invokeAndWait(runnable)
            countDownLatch.countDown()
        }
    }
}