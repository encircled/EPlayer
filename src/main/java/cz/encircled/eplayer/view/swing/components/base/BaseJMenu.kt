package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.fswing.components.Cancelable
import cz.encircled.fswing.components.RemovalAware
import javax.swing.JMenu

class BaseJMenu(title: String) : JMenu(title), RemovalAware {

    override val cancelableListeners: MutableList<Cancelable> = ArrayList()

    override fun removeAll() {
        this.menuComponents.forEach { if (it is RemovalAware) it.beforeRemoved() }
        super.removeAll()
    }

}