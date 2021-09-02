package cz.encircled.eplayer.view.swing.components.base

import com.formdev.flatlaf.FlatClientProperties.*
import cz.encircled.eplayer.view.controller.QuickNaviController
import java.util.function.BiConsumer
import javax.swing.JTabbedPane

class TabsPane(val controller: QuickNaviController) : JTabbedPane() {

    val tabs: MutableList<TitleAndIndex> = ArrayList()

    init {
        putClientProperty(TABBED_PANE_TAB_CLOSABLE, true)
        putClientProperty(TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close")
        putClientProperty(TABBED_PANE_TAB_CLOSE_CALLBACK,
            BiConsumer<JTabbedPane, Int> { _, tabIndex ->
                controller.removeTab(tabs.first { it.index == tabIndex }.title)
            })
    }

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
