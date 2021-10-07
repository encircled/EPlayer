package cz.encircled.eplayer.view.swing.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaBookmark
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.addChangesListener
import cz.encircled.eplayer.view.controller.PlayerController
import cz.encircled.eplayer.view.swing.addAll
import cz.encircled.eplayer.view.swing.boxPanel
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.BaseLabel
import cz.encircled.eplayer.view.swing.onClick
import cz.encircled.eplayer.view.swing.padding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JList

class PlayerSideControls(private val playerController: PlayerController, val core: ApplicationCore) : BaseJPanel() {

    private lateinit var currentMediaBookmarks: ObservableList<MediaBookmark>
    private var currentListener: Cancelable? = null
    private val bookmarksList = JList<MediaBookmark>()

    init {
        background = MEDIUM_BG
        preferredSize = Dimension(150, AppView.MIN_HEIGHT - AppView.PLAYER_CONTROLS_HEIGHT)

        Event.mediaChange.listenUiThread {
            currentListener?.cancel()
            currentMediaBookmarks = if (it is SingleMedia) it.bookmarks else FXCollections.observableArrayList()
            currentListener = currentMediaBookmarks.addChangesListener { _, _ ->
                handleBookmarksChanged()
            }
            handleBookmarksChanged()
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
                    BaseLabel("Bookmarks", isBold = true),
                    bookmarksList,
                    JButton("Add bookmark").onClick {
                        playerController.addBookmark()
                    }
                )
            }
        }
    }

    private fun handleBookmarksChanged() {
        bookmarksList.setListData(currentMediaBookmarks.toTypedArray())
    }

}