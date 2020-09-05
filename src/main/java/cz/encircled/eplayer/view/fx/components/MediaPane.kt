package cz.encircled.eplayer.view.fx.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.view.fx.UiDataModel
import cz.encircled.eplayer.view.fx.addNewValueListener
import cz.encircled.eplayer.view.fx.controller.QuickNaviController
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import java.net.URLEncoder

/**
 * @author encir on 01-Sep-20.
 */
class MediaPane(
        val media: PlayableMedia,
        val dataModel: UiDataModel,
        val controller: QuickNaviController,
) : BorderPane() {

    init {
        styleClass.add("qn_video")
        dataModel.selectedMedia.addNewValueListener {
            if (media == it) {
                styleClass.add("selected")
            } else {
                styleClass.remove("selected")
            }

        }

        val screenshot = ImageView(media.pathToScreenshot)
        val nameLabel = Label(name())
        nameLabel.styleClass.add("name_label")

        screenshot.onMouseClicked = EventHandler { controller.play(media) }
        nameLabel.onMouseClicked = EventHandler { controller.play(media) }

        top = screenshot
        center = nameLabel
        bottom = mediaPaneBottomInfo(media, name())
    }

    fun name(): String {
        return (
                if (media is MediaSeries) "${media.name.trim()}- Episode ${media.indexOfCurrent() + 1}"
                else media.mediaFile().name
                ).replace(".", " ")
    }

    private fun mediaPaneBottomInfo(media: PlayableMedia, mediaName: String): BorderPane {
        val bottomInfo = BorderPane()
        bottomInfo.styleClass.add("bottom_info")
        if (media.time > 0L) {
            bottomInfo.left = Label("${media.formattedCurrentTime}, watched ${media.formattedWatchDate}, ${media.formattedExtension}")
        } else {
            bottomInfo.left = Label(media.formattedExtension)
        }

        val onlineSearchIcon = iconButton("icons/search.png", true) {
            val cleanedName = URLEncoder.encode(mediaName, "UTF-8")
            Runtime.getRuntime().exec(arrayOf("PowerShell", "start chrome https://www.kinopoisk.ru/index.php?kp_query=$cleanedName"))
        }
        val deleteIcon = iconButton("icons/trash.png") {
            controller.deleteEntry(media)
            (parent as FlowPane).children.remove(this)
        }

        bottomInfo.right = HBox(15.0, onlineSearchIcon, deleteIcon)
        return bottomInfo
    }

    private fun iconButton(icon: String, sameWidth: Boolean = false, onClick: () -> Unit): ImageView =
            ImageView(Image(icon)).apply {
                fitHeight = 20.0
                fitWidth = if (sameWidth) 20.0 else 15.0
                onMouseClicked = EventHandler {
                    onClick()
                }
            }

}