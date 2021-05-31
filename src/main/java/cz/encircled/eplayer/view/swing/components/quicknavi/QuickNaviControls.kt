package cz.encircled.eplayer.view.swing.components.quicknavi

import com.formdev.flatlaf.FlatClientProperties
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.*
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.TabsPane
import java.awt.Color
import java.awt.GridBagConstraints
import javax.swing.JComboBox
import javax.swing.JTabbedPane
import javax.swing.JTextField

class QuickNaviControls(private val quickNaviController: QuickNaviController, dataModel: UiDataModel) : BaseJPanel() {

    init {
        anchor = GridBagConstraints.NORTHWEST

        val sortSelect = JComboBox(SortType.values().map { it.name }.toTypedArray())
        sortSelect.addItemListener {
            dataModel.sortType.set(SortType.valueOf(it.item.toString()))
        }

        val searchField = JTextField().apply {
            putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, Localization.search.ln())
            document.onChange { dataModel.filter.value = it }
        }

        // Layout
        padding(10, 1, 10, 0)

        nextColumn(folderTabs(dataModel))
        nextColumn(GridData(width = 500, height = 34)) {
            flowPanel(vhap = 0) {
                border(bottom = 1, color = Color(100, 100, 100))

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
        val tabs = TabsPane()
        tabs.addChangeListener {
            quickNaviController.onFolderSelect(dataModel.foldersToScan[tabs.selectedIndex])
        }
        tabs.onClick {
            if (it.button == 2) {
                val tabForCoordinate = tabs.ui.tabForCoordinate(tabs, it.x, it.y)
                quickNaviController.removeTab(dataModel.foldersToScan[tabForCoordinate])
                quickNaviController.onFolderSelect(QUICK_NAVI)
            }
        }

        dataModel.selectedFolder.addNewValueListener {
            tabs.selectedIndex = dataModel.foldersToScan.indexOf(it)
        }

        dataModel.foldersToScan.addChangesListener { added, removed ->
            tabs.removeTabs(removed)
            tabs.addTabs(added)
        }

        return tabs
    }

}