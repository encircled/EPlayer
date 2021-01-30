package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.eplayer.service.Cancelable

/**
 * Indicates that the component must be notified before being removed
 */
interface RemovalAware {

    val cancelableListeners: MutableList<Cancelable>

    fun beforeRemoved() {
        cancelableListeners.forEach { it.cancel() }
        cancelableListeners.clear()
    }

    fun Cancelable.cancelOnRemove() {
        cancelableListeners.add(this)
    }

}