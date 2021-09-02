package cz.encircled.eplayer.view

import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities

/**
 * @author Encircled on 20/09/2014.
 */
object UiUtil {

    inline fun inNormalThread(crossinline runnable: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            CompletableFuture.runAsync {
                runnable.invoke()
            }
        } else {
            runnable.invoke()
        }
    }

    inline fun inUiThread(crossinline runnable: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.invoke()
        } else {
            SwingUtilities.invokeLater {
                runnable.invoke()
            }
        }
    }

}
