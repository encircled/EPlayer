package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.fswing.components.FluentPanel
import cz.encircled.fswing.model.GridData
import cz.encircled.fswing.onChange
import org.apache.logging.log4j.LogManager
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

/**
 * Parent container panel on quick navi screen
 */
class QuickNaviPanel(
    private val dataModel: UiDataModel,
    val quickNaviController: QuickNaviController,
    val appView: AppView
) : FluentPanel() {

    private val log = LogManager.getLogger()

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
            if (!it.valueIsAdjusting) {
                dataModel.lastScrollPosition.set(it.value)
            }
        }
        appView.currentSceneProperty.onChange {
            if (it == Scenes.PLAYER) {
                mediaContainerScroll.verticalScrollBar.value = dataModel.lastScrollPosition.get()
            }
        }

        mediaContainerScroll.verticalScrollBar.unitIncrement = 25
        dataModel.selectedMediaPane.onChange {
            // Set focus on selected one
            if (it != null) mediaContainerScroll.verticalScrollBar.value = max(it.y - 80, 0)
        }


        // Layout
        nextRow(height = 34) {
            QuickNaviControls(quickNaviController, dataModel)
        }
        nextRow(GridData(fill = GridBagConstraints.BOTH)) {
            mediaContainerScroll
        }
    }

    private fun registerFilesDragAndDrop() {
        dropTarget = DropTarget(this, object : DropTargetAdapter() {
            override fun drop(e: DropTargetDropEvent) {
                log.info("Dropped action")
                e.acceptDrop(DnDConstants.ACTION_COPY)
                (e.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>)
                    .forEach {
                        if (it.isDirectory) {
                            quickNaviController.addTab(it.path)
                        } else {
                            quickNaviController.play(it.path)
                        }
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