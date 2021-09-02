package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.addAll
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.WrapLayout
import cz.encircled.eplayer.view.swing.removeIf
import java.awt.FlowLayout

class QuickNaviMediaContainer(
    private val dataModel: UiDataModel,
    private val quickNaviController: QuickNaviController
) : BaseJPanel(WrapLayout(FlowLayout.LEFT, 25, 25)) {

    init {
        dataModel.media.addChangesListener { added, removed ->
            val idOfRemoved = removed.map { it.path() }
            removeIf { idOfRemoved.contains((it as AbstractMediaPane).media.path()) }
            addAll(added.map {
                when (it) {
                    is UiPlayableMedia -> PlayableMediaPane(it, dataModel, quickNaviController)
                    is UiFolderMedia -> FolderMediaPane(it, dataModel, quickNaviController)
                    else -> throw UnsupportedOperationException()
                }
            })
        }

        dataModel.selectedMedia.addNewValueListener { selected ->
            dataModel.selectedMediaPane.value =
                components.filterIsInstance<AbstractMediaPane>().firstOrNull { it.media == selected }
        }
    }

}
