package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiMedia
import cz.encircled.eplayer.view.addNewValueListener
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.border
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.RemovalAware
import cz.encircled.eplayer.view.swing.onHover
import java.awt.Component
import java.awt.Dimension

abstract class AbstractMediaPane(
    val media: UiMedia,
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
    }

    protected fun initLayout() {
        val footer = footer()
        val header = header()

        body()?.let {
            nextRow(it) {
                width = AppView.SCREENSHOT_WIDTH
                height = AppView.SCREENSHOT_HEIGHT
            }
        }

        nextRow(header)
        footer?.let { nextRow(footer) }

        registerListeners(footer, header)
    }

    private fun registerListeners(footer: Component?, header: BaseJPanel) {
        fun onHover() {
            footer?.background = BLUE_BG
            header.background = BLUE_BG
        }

        fun onExist() {
            footer?.background = DARK_BG
            header.background = DARK_BG
        }

        onHover(::onHover, ::onExist)

        dataModel.selectedMedia.addNewValueListener {
            if (media == it) onHover() else onExist()
        }.cancelOnRemove()

        if (dataModel.selectedMedia.get() == media) onHover()
    }

    abstract fun header(): BaseJPanel

    abstract fun body(): Component?

    abstract fun footer(): Component?

}