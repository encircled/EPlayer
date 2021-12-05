package cz.encircled.eplayer.view.swing.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaBookmark
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.controller.PlayerController
import cz.encircled.fswing.addAll
import cz.encircled.fswing.boxPanel
import cz.encircled.fswing.components.FluentLabel
import cz.encircled.fswing.components.FluentList
import cz.encircled.fswing.components.FluentPanel
import cz.encircled.fswing.model.Colours.MEDIUM_BG
import cz.encircled.fswing.observable.observableList
import cz.encircled.fswing.onClick
import cz.encircled.fswing.padding
import java.awt.Dimension
import javax.swing.JButton

class PlayerSideControls(private val playerController: PlayerController, val core: ApplicationCore) : FluentPanel() {

    private val bookmarksList = FluentList<MediaBookmark>()

    init {
        background = MEDIUM_BG
        preferredSize = Dimension(150, AppView.MIN_HEIGHT - AppView.PLAYER_CONTROLS_HEIGHT)

        Event.mediaChange.listenUiThread {
            bookmarksList.dataSource(if (it is SingleMedia) it.bookmarks else observableList())
        }

        padding(5, 15, 5)

        bookmarksList.background = MEDIUM_BG
        bookmarksList.fixedCellWidth = 100
        bookmarksList.onClick {
            bookmarksList.selectedValue?.let { playerController.time = it.time }
        }

        nextRow {
            boxPanel {
                addAll(
                    FluentLabel("Bookmarks", isBold = true),
                    bookmarksList,
                    JButton("Add bookmark").onClick {
                        playerController.addBookmark()
                    }
                )
            }
        }
    }

}