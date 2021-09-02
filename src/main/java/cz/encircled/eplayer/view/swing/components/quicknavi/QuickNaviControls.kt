package cz.encircled.eplayer.view.swing.components.quicknavi

import com.formdev.flatlaf.FlatClientProperties
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.*
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.ComboBox
import cz.encircled.eplayer.view.swing.components.base.TabsPane
import java.awt.Color
import java.awt.GridBagConstraints
import javax.swing.JCheckBox
import javax.swing.JTabbedPane
import javax.swing.JTextField

class QuickNaviControls(private val quickNaviController: QuickNaviController, dataModel: UiDataModel) : BaseJPanel() {

    init {
        anchor = GridBagConstraints.NORTHWEST

        val flatView = JCheckBox("Flat view", dataModel.flatView.get())
        flatView.addItemListener {
            dataModel.flatView.set(flatView.isSelected)
        }

        val sortSelect = ComboBox(SortType.values(), Localization.sortType)
            .bindWith(dataModel.sortType)

        val aggregationSelect = ComboBox(AggregationType.values(), Localization.aggregationType)
            .bindWith(dataModel.aggregationType)

        val searchField = JTextField().apply {
            putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Localization.search.ln())
            document.onChange { dataModel.filter.value = it }
        }

        // Layout
        padding(10, 1, 10, 0)

        nextColumn(folderTabs(dataModel))

        nextColumn(GridData(width = 840, height = 34)) {
            flowPanel(vhap = 0) {
                border(bottom = 1, color = Color(100, 100, 100))

                nextColumn(GridData(100, 30)) {
                    flatView
                }

                nextColumn(GridData(145, 30)) {
                    aggregationSelect
                }

                nextColumn(GridData(120, 30)) {
                    sortSelect
                }

                nextColumn(GridData(270, 30)) {
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

        dataModel.selectedTab.addNewValueListener {
            tabs.selectedIndex = dataModel.foldersToScan.indexOf(it)
        }

        dataModel.foldersToScan.addChangesListener { added, removed ->
            tabs.removeTabs(removed)
            tabs.addTabs(added)
        }

        return tabs
    }

}