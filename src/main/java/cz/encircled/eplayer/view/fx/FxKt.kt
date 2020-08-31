package cz.encircled.eplayer.view.fx

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import java.util.concurrent.CountDownLatch

/**
 * @author encir on 25-Aug-20.
 */
fun <T> ObservableValue<T>.addNewValueListener(listener: (T) -> Unit) {
    addListener { _, _, newValue ->
        listener.invoke(newValue)
    }
}

fun fxThread(runnable: () -> Unit) {
    if (Platform.isFxApplicationThread()) runnable.invoke()
    else Platform.runLater(runnable)
}

fun fxThread(countDownLatch: CountDownLatch, runnable: () -> Unit) {
    if (Platform.isFxApplicationThread()) {
        runnable.invoke()
        countDownLatch.countDown()
    } else {
        Platform.runLater {
            runnable.invoke()
            countDownLatch.countDown()
        }
    }
}
