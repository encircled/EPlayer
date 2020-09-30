package cz.encircled.eplayer.view.fx

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.util.concurrent.CountDownLatch

/**
 * @author encir on 25-Aug-20.
 */
fun <T> ObservableValue<T>.addNewValueListener(listener: (T) -> Unit) {
    addListener { _, _, newValue ->
        listener.invoke(newValue)
    }
}

fun <T> ObservableList<T>.addChangesListener(listener: (added: List<T>, removed: List<T>) -> Unit) {
    addListener(ListChangeListener {
        val added = ArrayList<T>()
        val removed = ArrayList<T>()

        while (it.next()) {
            added.addAll(it.addedSubList)
            removed.addAll(it.removed)
        }

        listener.invoke(added, removed)
    })
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
