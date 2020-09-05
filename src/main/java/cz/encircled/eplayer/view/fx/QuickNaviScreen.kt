package cz.encircled.eplayer.view.fx

import cz.encircled.eplayer.view.fx.FxUtil.withFastScroll
import cz.encircled.eplayer.view.fx.components.AppMenuBar
import cz.encircled.eplayer.view.fx.components.MediaPane
import cz.encircled.eplayer.view.fx.controller.QuickNaviController
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane

import org.apache.logging.log4j.LogManager

const val SELECTED = "selected"

/**
 * @author Encircled on 18/09/2014.
 */
class QuickNaviScreen(
        private val dataModel: UiDataModel,
        private val controller: QuickNaviController,
        private val fxView: FxView
) : BorderPane() {
    private val log = LogManager.getLogger()

    private lateinit var mediaContainer: FlowPane

    // TODO
    private lateinit var selectedTab: Label

    fun init(menuBar: AppMenuBar) {
        top = menuBar.getMenuBar()
        mediaContainer = FlowPane()
        mediaContainer.styleClass.add("qn_video_container")

        val mediaContainerScroll = withFastScroll(ScrollPane(mediaContainer))
        mediaContainerScroll.isFitToWidth = true
        mediaContainerScroll.onDragDropped = fxView.newTabDropHandler
        mediaContainerScroll.style = "-fx-background-color: -fx-body-color;"

        val content = BorderPane(mediaContainerScroll)
        content.top = initControls()
        initializeListeners()
        center = content

        dataModel.media.addListener(ListChangeListener { event ->
            while (event.next()) {
                if (event.removedSize > 0) {
                    fxThread {
                        mediaContainer.children.removeIf { event.removed.contains((it as MediaPane).media) }
                    }
                } else {
                    fxThread {
                        mediaContainer.children.addAll(ArrayList(event.addedSubList).map { MediaPane(it, dataModel, controller) })
                    }
                }
            }
        })

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
        val folders = FlowPane(Label(" | "))

        val quickNaviLabel = Label("Quick Navi")
        selectedTab = quickNaviLabel

        quickNaviLabel.styleClass.addAll("folder_selector", SELECTED)
        quickNaviLabel.onMouseClicked = EventHandler {
            controller.onQuickNaviSelect()
            selectedTab.styleClass.remove(SELECTED)
            selectedTab = quickNaviLabel

            quickNaviLabel.styleClass.add(SELECTED)
        }

        folders.children.addAll(quickNaviLabel, Label(" | "))

        // TODO changes listener
        dataModel.foldersToScan.forEach { path ->
            val folder = Label(path)
            folder.styleClass.add("folder_selector")
            folders.children.addAll(folder, Label(" | "))

            folder.onMouseClicked = EventHandler {
                controller.onFolderSelect(path)
                selectedTab.styleClass.remove(SELECTED)
                selectedTab = folder

                folder.styleClass.add(SELECTED)

            }
        }

        return folders
    }

    private fun initializeListeners() {
        fxView.sceeneChangeProperty.addNewValueListener {
            if (FxView.QUICK_NAVI_SCREEN == it) {
                // TODO
            }
        }
    }

}
