package cz.encircled.eplayer

import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.controller.QuickNaviControllerImpl
import cz.encircled.eplayer.view.swing.components.base.TabsPane
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TabsPaneTest : BaseTest() {

    @Test
    fun testAddTabs() {
        val pane = TabsPane(QuickNaviControllerImpl(UiDataModel(), core))

        pane.addTabs(listOf("0", "1", "2", "3", "4"))
        assertEquals(tabs(0, 1, 2, 3, 4), pane.tabs)
    }

    @Test
    fun testRemoveTabs() {
        val pane = TabsPane(QuickNaviControllerImpl(UiDataModel(), core))

        pane.addTabs(listOf("0", "1", "2", "3", "4"))

        pane.removeTabs(listOf("6"))
        assertEquals(tabs(0, 1, 2, 3, 4), pane.tabs)

        pane.removeTabs(listOf("1", "3"))
        assertEquals(tabs(0, 2, 4), pane.tabs)

        pane.removeTabs(listOf("2"))
        assertEquals(tabs(0, 4), pane.tabs)

        pane.removeTabs(listOf("4"))
        assertEquals(tabs(0), pane.tabs)

        pane.addTabs(listOf("4"))
        assertEquals(tabs(0, 4), pane.tabs)

        pane.removeTabs(listOf("0", "4"))
        assertTrue(pane.tabs.isEmpty())
    }

    private fun tabs(vararg tabs: Int): List<TabsPane.TitleAndIndex> =
        tabs.mapIndexed { index, i -> TabsPane.TitleAndIndex(i.toString(), index) }

}