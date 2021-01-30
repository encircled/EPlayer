package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.StringUtil
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.addNewValueListener
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.*
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.ImagePanel
import cz.encircled.eplayer.view.swing.components.base.RemovalAware
import cz.encircled.eplayer.view.swing.components.base.ToggleButton
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JLabel
import javax.swing.JPanel


class MediaPane(
    val media: PlayableMedia,
    val dataModel: UiDataModel,
    val controller: QuickNaviController,
) : BaseJPanel(), RemovalAware {

    override val cancelableListeners: MutableList<Cancelable> = ArrayList()

    init {
        size = Dimension(AppView.SCREENSHOT_WIDTH, AppView.SCREENSHOT_HEIGHT + 76)
        preferredSize = Dimension(size)
        maximumSize = Dimension(size)
        background = LIGHTER_BG
        border(Color(94, 96, 96))

        val bottomInfo = mediaPaneBottomInfo(media, name())

        val namePanel = gridPanel {
            background = DEFAULT_BG
            padding(8, 5, 5, 5)

            nextRow {
                JLabel(name()).apply {
                    if (media is MediaSeries) {
                        media.currentEpisode.addNewValueListener {
                            text = name()
                        }.cancelOnRemove()
                    }
                }
            }
        }

        val screen = ImagePanel(media.filePathToScreenshot, name())

        registerListeners(bottomInfo, namePanel, screen)

        nextRow(screen) {
            width = AppView.SCREENSHOT_WIDTH
            height = AppView.SCREENSHOT_HEIGHT
        }
        nextRow(namePanel)
        nextRow(bottomInfo)

    }

    private fun registerListeners(bottomInfo: Component, namePanel: BaseJPanel, screen: ImagePanel) {
        onClick {
            controller.play(media)
        }

        // TODO inner elems
        fun onHover() {
            bottomInfo.background = DARK_BG
            namePanel.background = DARK_BG
        }

        fun onExist() {
            bottomInfo.background = DEFAULT_BG
            namePanel.background = DEFAULT_BG
        }

        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) = onHover()
            override fun mouseExited(e: MouseEvent) = onExist()
        })

        dataModel.selectedMedia.addNewValueListener {
            if (media == it) onHover() else onExist()
        }.cancelOnRemove()

        Event.screenshotAcquired.listenUiThread {
            if (media.path == it.characteristic) {
                screen.setImage(media.filePathToScreenshot, name())
            }
        }.cancelOnRemove()
    }

    fun name(): String = media.name()
    /*(
        if (media is MediaSeries) "${media.name.trim()}- Episode ${media.currentEpisode.get() + 1}"
        else media.mediaFile().name
        ).replace(".", " ")*/

    private fun mediaPaneBottomInfo(media: PlayableMedia, mediaName: String): Component {
        val bottomInfo = flowPanel(align = FlowLayout.RIGHT) {
            padding(6)
        }

        val timeLabel = JLabel(media.formattedCurrentTime + " / " + StringUtil.msToTimeLabel(media.duration.get()))
        val listener: (Number) -> Unit = {
            timeLabel.text = media.formattedCurrentTime + " / " + StringUtil.msToTimeLabel(media.duration.get())
        }
        media.duration.addNewValueListener(listener).cancelOnRemove()

        if (media is MediaSeries) {
            media.currentEpisode.addNewValueListener {
                timeLabel.text = media.formattedCurrentTime
            }.cancelOnRemove()
        }

        val onlineSearchIcon = iconButton("search.png") {
            val cleanedName = URLEncoder.encode(mediaName, "UTF-8")
            Runtime.getRuntime()
                .exec(arrayOf("PowerShell", "start chrome https://www.kinopoisk.ru/index.php?kp_query=$cleanedName"))
        }
        val deleteIcon = iconButton("trash.png") {
            controller.deleteEntry(media)
            removeSelf()
        }
        val nextSeries = iconButton("arrow-right.png") {
            (media as MediaSeries).toNext()
        }
        val prevSeries = iconButton("arrow-left.png") {
            (media as MediaSeries).toPrev()
        }

        val infoText =
            if (media.time > 0L) "watched ${media.formattedWatchDate}, ${media.path}"
            else media.path
        val info = iconButton("info.png", infoText) {}

        val icons = flowPanel(6, 0) {
            isOpaque = false
            add(info)
        }

        if (media is MediaSeries) {
            icons.add(flowPanel(0, 0) {
                isOpaque = false
                addAll(prevSeries, nextSeries)
            })
        }
        icons.addAll(onlineSearchIcon, deleteIcon)

        return bottomInfo.addAll(timeLabel, icons)
    }

}