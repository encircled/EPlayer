package cz.encircled.eplayer.view.fx

import cz.encircled.eplayer.view.fx.FxUtil.withFastScroll
import cz.encircled.eplayer.view.fx.components.AppMenuBar
import cz.encircled.eplayer.view.fx.components.MediaFolder
import cz.encircled.eplayer.view.fx.components.MediaPane
import cz.encircled.eplayer.view.fx.controller.QuickNaviController
import javafx.geometry.Bounds
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane

import org.apache.logging.log4j.LogManager
import kotlin.math.max
import kotlin.math.min

const val SELECTED = "selected"

/**
 * @author Encircled on 18/09/2014.
 */
class QuickNaviScreen(
        private val dataModel: UiDataModel,
        private val controller: QuickNaviController,
        menuBar: AppMenuBar,
        fxView: FxView
) : BorderPane() {
    private val log = LogManager.getLogger()

    init {
        top = menuBar.getMenuBar()
        val mediaContainer = FlowPane()
        mediaContainer.styleClass.add("qn_video_container")

        val mediaContainerScroll = withFastScroll(ScrollPane(mediaContainer))

        dataModel.selectedMediaPane.addNewValueListener {
            // Set focus on selected one
            mediaContainerScroll.vvalue = (it.layoutY + (it.layoutBounds.height / 3)) / mediaContainer.height
        }

        mediaContainerScroll.isFitToWidth = true
        mediaContainerScroll.onDragDropped = fxView.newTabDropHandler
        mediaContainerScroll.style = "-fx-background-color: -fx-body-color;"
        // Scale with parent container
        mediaContainerScroll.prefWidthProperty().bind(widthProperty())

        val content = BorderPane(mediaContainerScroll)
        content.top = initControls()
        center = content

        dataModel.media.addChangesListener { added, removed ->
            fxThread {
                println("dataModel.media")
                val start = System.currentTimeMillis()
                mediaContainer.children.removeIf { removed.contains((it as MediaPane).media) }
                mediaContainer.children.addAll(added.map { MediaPane(it, dataModel, controller) })
                log.debug("dataModel.media: ${System.currentTimeMillis() - start}")
            }
        }
    }

    private fun initControls(): FlowPane {
        val controls = FlowPane()
        controls.styleClass.add("top_menu")

        val searchField = TextField()
        searchField.promptText = "Search..."
        searchField.styleClass.add("search_field")
        searchField.textProperty().bindBidirectional(dataModel.filter)

        controls.children.addAll(searchField, prepareFoldersControl())

        return controls
    }

    private fun prepareFoldersControl(): FlowPane {
        val folders = FlowPane()
        folders.styleClass.add("folder_selector_container")
        widthProperty().addNewValueListener {
            folders.prefWidthProperty().set(it.toDouble() - 400)
        }

        dataModel.foldersToScan.addChangesListener { added, removed ->
            fxThread {
                val start = System.currentTimeMillis()
                folders.children.removeIf { removed.contains((it as MediaFolder).path) }
                folders.children.addAll(added.map { MediaFolder(it, dataModel, controller) })
                log.debug("dataModel.foldersToScan: ${System.currentTimeMillis() - start}")
            }
        }

        return folders
    }

}
