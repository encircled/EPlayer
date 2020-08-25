package cz.encircled.eplayer.view.fx

import javafx.beans.value.ObservableValue

/**
 * @author encir on 25-Aug-20.
 */
fun <T> ObservableValue<T>.addNewValueListener(listener: (T) -> Unit) {
    addListener { _, _, newValue ->
        listener.invoke(newValue)
    }
}
