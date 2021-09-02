package cz.encircled.eplayer.view.swing.components.quicknavi

import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiFolderMedia
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.ImagePanel
import cz.encircled.eplayer.view.swing.components.base.RemovalAware
import cz.encircled.eplayer.view.swing.flowPanel
import cz.encircled.eplayer.view.swing.gridPanel
import cz.encircled.eplayer.view.swing.onClick
import cz.encircled.eplayer.view.swing.padding
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.JLabel

class FolderMediaPane(
    media: UiFolderMedia,
    dataModel: UiDataModel,
    controller: QuickNaviController,
    private val folderMedia: UiFolderMedia = media,
) : AbstractMediaPane(media, dataModel, controller), RemovalAware {

    init {
        initLayout()

        onClick {
            controller.openFolder(folderMedia)
        }
    }

    override fun header(): BaseJPanel = gridPanel {
        background = DARK_BG
        padding(16, 20)

        nextRow {
            JLabel("<html>${folderMedia.name}</html>")
        }
    }

    override fun body(): Component? {
        val size = folderMedia.nestedMedia.size
        return if (size > 0) {
            gridPanel {
                nextColumn {
                    ImagePanel(folderMedia.nestedMedia[0].filePathToScreenshot, "")
                }
                if (size > 1) {
                    nextColumn {
                        gridPanel {
                            nextRow {
                                ImagePanel(folderMedia.nestedMedia[1].filePathToScreenshot, "")
                            }
                            if (size > 2) {
                                nextRow {
                                    ImagePanel(folderMedia.nestedMedia[2].filePathToScreenshot, "")
                                }
                            }
                        }
                    }
                }
            }
        } else null
    }

    override fun footer(): Component = flowPanel(align = FlowLayout.RIGHT) {
        background = DARK_BG
        padding(6)

        nextColumn {
            JLabel("${folderMedia.nestedMedia.size} files")
        }
    }

}