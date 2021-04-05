package cz.encircled.eplayer.view

import cz.encircled.eplayer.service.Cancelable
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

/**
 * @author encir on 25-Aug-20.
 */
fun <T> ObservableValue<T>.addNewValueListener(listener: (T) -> Unit): Cancelable {
    // TODO MOVE TO REMOVABLE
    val listenerToAdd: ChangeListener<T> = ChangeListener { _: ObservableValue<out T>, _: T, newValue: T ->
        UiUtil.inUiThread {
            listener.invoke(newValue)
        }
    }
    addListener(listenerToAdd)
    return Cancelable {
        removeListener(listenerToAdd)
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

        UiUtil.inUiThread {
            listener.invoke(added, removed)
        }
    })
}
