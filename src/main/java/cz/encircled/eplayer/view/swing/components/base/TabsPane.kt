package cz.encircled.eplayer.view.swing.components.base

import javax.swing.JTabbedPane

class TabsPane : JTabbedPane() {

    val tabs: MutableList<TitleAndIndex> = ArrayList()

    fun addTabs(names: List<String>) {
        names.forEach {
            tabs.add(TitleAndIndex(it, tabs.size))
            addTab(it, null)
        }
    }

    fun removeTabs(names: List<String>) {
        names.forEach { toRemove ->
            val pos = tabs.firstOrNull { it.title == toRemove }
            if (pos != null) {
                tabs.removeAt(pos.index)
                removeTabAt(pos.index)
                (pos.index until tabs.size).forEach {
                    tabs[it].index--
                }
            }
        }
    }

    data class TitleAndIndex(val title: String, var index: Int)

}