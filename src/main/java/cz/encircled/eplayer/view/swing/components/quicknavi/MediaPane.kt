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
        border(DARK_BG)

        val bottomInfo = mediaPaneBottomInfo(media)

        val namePanel = gridPanel {
            background = DARK_BG
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

        fun onHover() {
            bottomInfo.background = MEDIUM_BG
            namePanel.background = MEDIUM_BG
        }

        fun onExist() {
            bottomInfo.background = DARK_BG
            namePanel.background = DARK_BG
        }

        onHover(::onHover, ::onExist)

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

    private fun mediaPaneBottomInfo(media: PlayableMedia): Component {
        val bottomInfo = flowPanel(align = FlowLayout.RIGHT) {
            padding(6)
            background = DARK_BG
        }

        val timeLabel = JLabel(getTimeLabel(media))
        val listener: (Number) -> Unit = {
            timeLabel.text = getTimeLabel(media)
        }
        media.duration.addNewValueListener(listener).cancelOnRemove()
        media.time.addNewValueListener(listener).cancelOnRemove()

        if (media is MediaSeries) {
            media.currentEpisode.addNewValueListener {
                timeLabel.text = media.formattedCurrentTime
            }.cancelOnRemove()
        }

        val onlineSearchIcon = iconButton("search.png") {
            controller.doWebSearch(media)
        }

        val deleteIcon = iconButton("trash.png") {
            controller.deleteEntry(media)
            removeSelf()
        }

        val nextSeries = iconButton("arrow-right.png") {
            // TODO not good
            (media as MediaSeries).toNext()
        }
        val prevSeries = iconButton("arrow-left.png") {
            (media as MediaSeries).toPrev()
        }

        val infoText =
            if (media.time.get() > 0L) "watched ${media.formattedWatchDate}, ${media.path}"
            else media.path

        val icons = flowPanel(6, 0) {
            isOpaque = false

            add(iconButton("info.png", infoText))
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

    private fun getTimeLabel(media: PlayableMedia) =
        media.formattedCurrentTime + " / " + StringUtil.msToTimeLabel(media.duration.get())

}