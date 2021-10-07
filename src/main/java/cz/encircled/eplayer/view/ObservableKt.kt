package cz.encircled.eplayer.view

import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.view.UiUtil.inUiThread
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

/**
 * @author encir on 25-Aug-20.
 */
inline fun <T> ObservableValue<T>.addNewValueListener(crossinline listener: (T) -> Unit): Cancelable {
    // TODO move to Removable?
    val listenerToAdd: ChangeListener<T> = ChangeListener { _: ObservableValue<out T>, _: T, newValue: T ->
        inUiThread {
            listener.invoke(newValue)
        }
    }
    addListener(listenerToAdd)
    return Cancelable {
        removeListener(listenerToAdd)
    }
}

inline fun <T> ObservableList<T>.addChangesListener(crossinline listener: (added: List<T>, removed: List<T>) -> Unit): Cancelable {
    val listenerToAdd = ListChangeListener<T> {
        val added = ArrayList<T>()
        val removed = ArrayList<T>()

        while (it.next()) {
            added.addAll(it.addedSubList)
            removed.addAll(it.removed)
        }

        inUiThread {
            listener.invoke(added, removed)
        }
    }
    addListener(listenerToAdd)
    return Cancelable {
        removeListener(listenerToAdd)
    }
}
