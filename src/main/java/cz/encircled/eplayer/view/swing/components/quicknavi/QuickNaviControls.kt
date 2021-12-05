package cz.encircled.eplayer.view.swing.components.quicknavi

import com.formdev.flatlaf.FlatClientProperties
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.AggregationType
import cz.encircled.eplayer.view.QUICK_NAVI
import cz.encircled.eplayer.view.SortType
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.components.base.TabsPane
import cz.encircled.fswing.*
import cz.encircled.fswing.components.FluentComboBox
import cz.encircled.fswing.components.FluentPanel
import java.awt.Color
import java.awt.GridBagConstraints
import javax.swing.JCheckBox
import javax.swing.JTabbedPane
import javax.swing.JTextField

class QuickNaviControls(private val quickNaviController: QuickNaviController, dataModel: UiDataModel) : FluentPanel() {

    init {
        anchor = GridBagConstraints.NORTHWEST

        val flatView = JCheckBox("Flat view", dataModel.flatView.get())
        flatView.addItemListener {
            dataModel.flatView.set(flatView.isSelected)
        }

        val sortSelect = FluentComboBox(SortType.values().toList())
            .bind(dataModel.sortType)

        val aggregationSelect = FluentComboBox(AggregationType.values().toList())
            .bind(dataModel.aggregationType)

        val searchField = JTextField().apply {
            putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Localization.search.ln())
            document.onChange { dataModel.filter.value = it }
        }

        // Layout
        padding(10, 1, 10, 0)

        nextColumn { folderTabs(dataModel) }

        nextColumn(840, 34) {
            flowPanel(vhap = 0) {
                border(bottom = 1, color = Color(100, 100, 100))

                nextColumn(100, 30) {
                    flatView
                }

                nextColumn(145, 30) {
                    aggregationSelect
                }

                nextColumn(120, 30) {
                    sortSelect
                }

                nextColumn(270, 30) {
                    searchField
                }
            }
        }
    }

    private fun folderTabs(dataModel: UiDataModel): JTabbedPane {
        val tabs = TabsPane(quickNaviController)
        tabs.addChangeListener {
            quickNaviController.openTab(dataModel.foldersToScan[tabs.selectedIndex])
        }
        tabs.onClick {
            if (it.button == 2) {
                val tabForCoordinate = tabs.ui.tabForCoordinate(tabs, it.x, it.y)
                quickNaviController.removeTab(dataModel.foldersToScan[tabForCoordinate])
                quickNaviController.openTab(QUICK_NAVI)
            }
        }

        dataModel.selectedTab.onChange {
            tabs.selectedIndex = dataModel.foldersToScan.indexOf(it)
        }

        dataModel.foldersToScan.onChange { added, removed ->
            tabs.removeTabs(removed)
            tabs.addTabs(added)
        }

        return tabs
    }

}