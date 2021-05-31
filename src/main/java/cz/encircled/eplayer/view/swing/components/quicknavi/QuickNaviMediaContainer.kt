package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.addChangesListener
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
            removeIf { removed.contains((it as MediaPane).media) }
            addAll(added.map { MediaPane(it, dataModel, quickNaviController) })
        }
    }

}