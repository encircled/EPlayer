package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiMedia
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.fswing.border
import cz.encircled.fswing.components.Cancelable
import cz.encircled.fswing.components.FluentPanel
import cz.encircled.fswing.components.RemovalAware
import cz.encircled.fswing.model.Colours.BLUE_BG
import cz.encircled.fswing.model.Colours.DARK_BG
import cz.encircled.fswing.model.Colours.LIGHTER_BG
import cz.encircled.fswing.onChange
import cz.encircled.fswing.onHover
import java.awt.Component
import java.awt.Dimension
import javax.swing.JPanel

abstract class AbstractMediaPane(
    val media: UiMedia,
    val dataModel: UiDataModel,
    val controller: QuickNaviController,
) : FluentPanel(), RemovalAware {

    override val cancelableListeners: MutableList<Cancelable> = ArrayList()

    init {
        size = Dimension(AppView.SCREENSHOT_WIDTH, AppView.SCREENSHOT_HEIGHT + 76)
        preferredSize = Dimension(size)
        maximumSize = Dimension(size)
        background = LIGHTER_BG
        border(DARK_BG)
    }

    protected fun initLayout() {
        val footer = footer()
        val header = header()

        body()?.let {
            nextRow(width = AppView.SCREENSHOT_WIDTH, height = AppView.SCREENSHOT_HEIGHT) {
                it
            }
        }

        nextRow {
            header
        }
        footer?.let { nextRow { it } }

        registerListeners(footer, header)
    }

    private fun registerListeners(footer: Component?, header: JPanel) {
        fun onHover() {
            footer?.background = BLUE_BG
            header.background = BLUE_BG
        }

        fun onExist() {
            footer?.background = DARK_BG
            header.background = DARK_BG
        }

        onHover(::onHover, ::onExist)

        dataModel.selectedMedia.onChange {
            if (media == it) onHover() else onExist()
        }.cancelOnRemove()

        if (dataModel.selectedMedia.get() == media) onHover()
    }

    abstract fun header(): JPanel

    abstract fun body(): Component?

    abstract fun footer(): Component?

}