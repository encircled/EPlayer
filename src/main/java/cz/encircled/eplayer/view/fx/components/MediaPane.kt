package cz.encircled.eplayer.view.fx.components

import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.view.fx.UiDataModel
import cz.encircled.eplayer.view.fx.addNewValueListener
import cz.encircled.eplayer.view.fx.controller.QuickNaviController
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
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
        private val dataModel: UiDataModel,
        private val controller: QuickNaviController,
) : BorderPane() {

    init {
        styleClass.add("qn_video")
        dataModel.selectedMedia.addNewValueListener {
            if (media == it) {
                styleClass.add("selected")
                dataModel.selectedMediaPane.set(this)
            } else styleClass.remove("selected")
        }

        val screenshot = ImageView(media.pathToScreenshot)
        val nameLabel = Label(name())
        nameLabel.styleClass.add("name_label")

        screenshot.onMouseClicked = EventHandler { controller.play(media) }
        nameLabel.onMouseClicked = EventHandler { controller.play(media) }

        top = screenshot
        center = nameLabel
        bottom = mediaPaneBottomInfo(media, name())

        if (media is MediaSeries) {
            media.currentEpisode.addNewValueListener {
                nameLabel.text = name()
            }
        }
    }

    fun name(): String {
        return (
                if (media is MediaSeries) "${media.name.trim()}- Episode ${media.currentEpisode.get() + 1}"
                else media.mediaFile().name
                ).replace(".", " ")
    }

    private fun mediaPaneBottomInfo(media: PlayableMedia, mediaName: String): BorderPane {
        val bottomInfo = BorderPane()
        bottomInfo.styleClass.add("bottom_info")
        val timeLabel = Label(media.formattedCurrentTime)
        bottomInfo.left = timeLabel

        if (media is MediaSeries) {
            media.currentEpisode.addNewValueListener {
                timeLabel.text = media.formattedCurrentTime
            }
        }

        val onlineSearchIcon = iconButton("search") {
            val cleanedName = URLEncoder.encode(mediaName, "UTF-8")
            Runtime.getRuntime().exec(arrayOf("PowerShell", "start chrome https://www.kinopoisk.ru/index.php?kp_query=$cleanedName"))
        }
        val deleteIcon = iconButton("remove") {
            controller.deleteEntry(media)
            (parent as FlowPane).children.remove(this)
        }
        val nextSeries = iconButton("arrow_right") {
            (media as MediaSeries).toNext()
        }
        val prevSeries = iconButton("arrow_left") {
            (media as MediaSeries).toPrev()
        }

        val infoText =
                if (media.time > 0L) "watched ${media.formattedWatchDate}, ${media.path}"
                else media.path
        val info = iconButton("info", infoText) {}

        val icons = HBox(10.0, info)

        if (media is MediaSeries) {
            icons.children.addAll(prevSeries, nextSeries)
        }
        icons.children.addAll(onlineSearchIcon, deleteIcon)

        bottomInfo.right = icons
        return bottomInfo
    }

    private fun iconButton(clazz: String, tooltip: String = "", onClick: () -> Unit): Node =
            Label().apply {
                if (tooltip.isNotEmpty()) this.tooltip = Tooltip(tooltip)
                styleClass.add(clazz)
                contentDisplay = ContentDisplay.GRAPHIC_ONLY
                onMouseClicked = EventHandler { onClick() }
            }

}