package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiFolderMedia
import cz.encircled.eplayer.view.UiPlayableMedia
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.fswing.addAll
import cz.encircled.fswing.components.FluentPanel
import cz.encircled.fswing.layout.WrapLayout
import cz.encircled.fswing.onChange
import cz.encircled.fswing.removeIf
import java.awt.FlowLayout

class QuickNaviMediaContainer(
    private val dataModel: UiDataModel,
    private val quickNaviController: QuickNaviController
) : FluentPanel(WrapLayout(FlowLayout.LEFT, 25, 25)) {

    init {
        dataModel.media.onChange { added, removed ->
            val idOfRemoved = removed.map { it.path() }
            removeIf { idOfRemoved.contains((it as AbstractMediaPane).media.path()) }
            val map = added.map {
                when (it) {
                    is UiPlayableMedia -> PlayableMediaPane(it, dataModel, quickNaviController)
                    is UiFolderMedia -> FolderMediaPane(it, dataModel, quickNaviController)
                    else -> throw UnsupportedOperationException()
                }
            }
            addAll(map)
        }

        dataModel.selectedMedia.onChange { selected ->
            dataModel.selectedMediaPane.value =
                components.filterIsInstance<AbstractMediaPane>().firstOrNull { it.media == selected }
        }
    }

}
