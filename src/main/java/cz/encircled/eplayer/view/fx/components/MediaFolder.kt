package cz.encircled.eplayer.view.fx.components

import cz.encircled.eplayer.view.fx.QUICK_NAVI
import cz.encircled.eplayer.view.fx.SELECTED
import cz.encircled.eplayer.view.fx.UiDataModel
import cz.encircled.eplayer.view.fx.addNewValueListener
import cz.encircled.eplayer.view.fx.controller.QuickNaviController
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.input.MouseButton

/**
 * @author encir on 06-Sep-20.
 */
class MediaFolder(
        val path: String,
        dataModel: UiDataModel,
        private val controller: QuickNaviController,
) : Label() {

    private val isDeletable = this.path != QUICK_NAVI

    init {
        styleClass.add("folder_selector")

        dataModel.selectedFolder.addNewValueListener {
            applySelectedStyles(it)
        }
        applySelectedStyles(dataModel.selectedFolder.get())

        onMouseClicked = EventHandler {
            when (it.button) {
                MouseButton.SECONDARY -> {
                    if (isDeletable) controller.removeTab(path)
                }
                else -> {
                    if (path == QUICK_NAVI) controller.onQuickNaviSelect()
                    else controller.onFolderSelect(path)
                }
            }
        }

        text = path
    }

    private fun applySelectedStyles(it: String?) {
        if (path == it) styleClass.add(SELECTED)
        else styleClass.remove(SELECTED)
    }

}