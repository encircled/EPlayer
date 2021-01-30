package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.addNewValueListener
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import java.awt.GridBagConstraints
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.JScrollPane
import kotlin.math.max
import kotlin.math.min

class QuickNaviPanel(val dataModel: UiDataModel, val quickNaviController: QuickNaviController, val appView: AppView) :
    BaseJPanel() {

    private val mediaContainerScroll: JScrollPane

    private val scrollOffset = 290

    init {
        registerFilesDragAndDrop()

        mediaContainerScroll = JScrollPane(
            QuickNaviMediaContainer(dataModel, quickNaviController),
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
        mediaContainerScroll.border = null

        mediaContainerScroll.verticalScrollBar.addAdjustmentListener {
            if (it.valueIsAdjusting) {
                dataModel.lastScrollPosition.set(it.value)
            }
        }
        appView.currentSceneProperty.addNewValueListener {
            if (it == Scenes.PLAYER) {
                mediaContainerScroll.verticalScrollBar.value = dataModel.lastScrollPosition.get()
            }
        }

        mediaContainerScroll.verticalScrollBar.unitIncrement = 25
        dataModel.selectedMediaPane.addNewValueListener {
            // Set focus on selected one
            val mediaPane = it as MediaPane
            mediaContainerScroll.verticalScrollBar.value = max(mediaPane.y - 80, 0)
        }


        // Layout

        nextRow(QuickNaviControls(quickNaviController, dataModel)) {
            height = 34
        }
        nextRow(mediaContainerScroll) {
            fill = GridBagConstraints.BOTH
        }
    }

    private fun registerFilesDragAndDrop() {
        dropTarget = DropTarget(this, object : DropTargetAdapter() {
            override fun drop(e: DropTargetDropEvent) {
                e.acceptDrop(DnDConstants.ACTION_COPY)
                (e.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>)
                    .filter { it.isDirectory }
                    .forEach {
                        quickNaviController.addTab(it.path)
                    }
            }
        })
    }

    fun scrollUp() {
        mediaContainerScroll.verticalScrollBar.value =
            max(0, mediaContainerScroll.verticalScrollBar.value - scrollOffset)
    }

    fun scrollDown() {
        mediaContainerScroll.verticalScrollBar.value = min(
            mediaContainerScroll.verticalScrollBar.maximum,
            mediaContainerScroll.verticalScrollBar.value + scrollOffset
        )
    }

}