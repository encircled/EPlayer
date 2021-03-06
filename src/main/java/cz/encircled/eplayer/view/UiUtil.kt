package cz.encircled.eplayer.view

import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities

/**
 * @author Encircled on 20/09/2014.
 */
object UiUtil {

    private val uiExecutor: UiExecutor = SwingUiExecutor()

    fun inNormalThread(runnable: Runnable) = uiExecutor.inNormalThread(runnable)

    fun inUiThread(runnable: Runnable) = uiExecutor.inUiThread(runnable)

    fun inUiThread(countDownLatch: CountDownLatch, runnable: Runnable) = uiExecutor.inUiThread(countDownLatch, runnable)

}

interface UiExecutor {

    fun inNormalThread(runnable: Runnable)
    fun inUiThread(runnable: Runnable)
    fun inUiThread(countDownLatch: CountDownLatch, runnable: Runnable)

}

private class SwingUiExecutor : UiExecutor {

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