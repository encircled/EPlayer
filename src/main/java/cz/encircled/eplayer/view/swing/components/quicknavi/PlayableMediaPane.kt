package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.StringUtil
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiPlayableMedia
import cz.encircled.eplayer.view.addNewValueListener
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.*
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.ImagePanel
import cz.encircled.eplayer.view.swing.components.base.RemovalAware
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.JLabel


class PlayableMediaPane(
    media: UiPlayableMedia,
    dataModel: UiDataModel,
    controller: QuickNaviController,
    val playableMedia: PlayableMedia = media.media
) : AbstractMediaPane(media, dataModel, controller), RemovalAware {

    private val screen = ImagePanel(playableMedia.filePathToScreenshot, name())

    init {
        registerListeners(screen)

        initLayout()
    }

    private fun registerListeners(screen: ImagePanel) {
        onClick {
            controller.play(playableMedia)
        }

        Event.screenshotAcquired.listenUiThread {
            if (playableMedia.path == it.characteristic) {
                screen.setImage(playableMedia.filePathToScreenshot)
            }
        }.cancelOnRemove()
    }

    fun name(): String = playableMedia.name()

    override fun header(): BaseJPanel = gridPanel {
        background = DARK_BG
        padding(8, 5, 5, 5)

        nextRow {
            JLabel(name()).apply {
                if (playableMedia is MediaSeries) {
                    playableMedia.currentEpisode.addNewValueListener {
                        text = name()
                    }.cancelOnRemove()
                }
            }
        }
    }

    override fun body(): Component = screen

    override fun footer(): Component {
        val bottomInfo = flowPanel(align = FlowLayout.RIGHT) {
            padding(6)
            background = DARK_BG
        }

        val timeLabel = JLabel(getTimeLabel(playableMedia))
        val listener: (Number) -> Unit = {
            timeLabel.text = getTimeLabel(playableMedia)
        }
        playableMedia.duration.addNewValueListener(listener).cancelOnRemove()
        playableMedia.time.addNewValueListener(listener).cancelOnRemove()

        if (playableMedia is MediaSeries) {
            playableMedia.currentEpisode.addNewValueListener {
                timeLabel.text = getTimeLabel(playableMedia)
            }.cancelOnRemove()
        }

        val onlineSearchIcon = iconButton("search.png") {
            controller.doWebSearch(playableMedia)
        }

        val deleteIcon = iconButton("trash.png") {
            controller.deleteEntry(playableMedia)
            removeSelf()
        }

        val nextSeries = iconButton("arrow-right.png") {
            // TODO not good
            (playableMedia as MediaSeries).toNext()
        }
        val prevSeries = iconButton("arrow-left.png") {
            (playableMedia as MediaSeries).toPrev()
        }

        val infoText =
            if (playableMedia.time.get() > 0L) "watched ${playableMedia.formattedWatchDate}, ${playableMedia.path}"
            else playableMedia.path

        val icons = flowPanel(6, 0) {
            isOpaque = false

            add(iconButton("info.png", infoText))
        }

        if (playableMedia is MediaSeries) {
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