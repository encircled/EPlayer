package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.*
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.TabsPane
import java.awt.Color
import java.awt.GridBagConstraints
import javax.swing.ButtonGroup
import javax.swing.JTabbedPane
import javax.swing.JTextField

class QuickNaviControls(val quickNaviController: QuickNaviController, dataModel: UiDataModel) : BaseJPanel() {

    init {
        anchor = GridBagConstraints.NORTHWEST
        padding(10, 1, 10, 0)

        val group = ButtonGroup()

        nextColumn(folderTabs(dataModel))
        nextColumn(GridData(width = 500, height = 34)) {
            flowPanel(vhap = 0) {
                border(bottom = 1, color = Color(100, 100, 100))

                nextColumn(GridData(30, 30)) {
                    iconButton("sort_duration.png", Localization.size.ln(), group) {
                        println("Sort duration")
                        dataModel.sortType.set(SortType.BY_DURATION)
                    }
                }

                nextColumn(GridData(270, 30)) {
                    JTextField().apply {
                        putClientProperty(Localization.search.ln(), "Placeholder")
                        document.onChange { dataModel.filter.value = it }
                    }
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