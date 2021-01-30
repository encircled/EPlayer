package cz.encircled.eplayer.view.swing.components

import cz.encircled.eplayer.common.PostponeTimer
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiUtil
import cz.encircled.eplayer.view.addNewValueListener
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Menu
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Cursor.getDefaultCursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseMotionAdapter
import javax.swing.JMenuBar
import javax.swing.SwingUtilities
import java.awt.image.BufferedImage


class PlayerPanel(
    appView: AppView,
    private val core: ApplicationCore,
    private val mediaPlayer: EmbeddedMediaPlayerComponent,
    private val menu: JMenuBar,
) : BaseJPanel(BorderLayout()) {

    private var hideTimer: PostponeTimer? = null

    private var toggleTimer: PostponeTimer? = null

    private val blankCursor: Cursor

    private val playerControls: PlayerControls = PlayerControls(appView, core)

    private val listener = object : MouseAdapter() {

        override fun mouseClicked(e: java.awt.event.MouseEvent) {
            if (toggleTimer == null) {
                toggleTimer = PostponeTimer(170) {
                    core.mediaService.toggle()
                    toggleTimer = null
                }
            }
            if (e.clickCount == 2) {
                toggleTimer?.cancel()
                toggleTimer = null
                appView.toggleFullScreen()
            }
        }

        override fun mouseMoved(e: java.awt.event.MouseEvent) {
            cursor = getDefaultCursor()
            when {
                e.y > height - playerControls.height -> {
                    hideTimer?.cancel()
                    playerControls.isVisible = true
                }
                e.y <= menu.height -> {
                    hideTimer?.cancel()
                    menu.isVisible = true
                }
                else -> {
                    if (menu.isVisible) {
                        menu.isVisible = false
                    }
                    hideTimer?.postpone(600L)
                }
            }
        }
    }

    init {
        val cursorImg = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, Point(0, 0), "blank cursor")

        add(mediaPlayer, BorderLayout.CENTER)
        add(playerControls, BorderLayout.SOUTH)

        appView.fullScreenProperty().addNewValueListener {
            if (it) onFullScreen() else onNotFullScreen()
        }

        mediaPlayer.videoSurfaceComponent().addMouseListener(listener)
    }

    private fun onNotFullScreen() {
        mediaPlayer.videoSurfaceComponent().removeMouseMotionListener(listener)
        hideTimer?.cancel()
        cursor = getDefaultCursor()

        menu.isVisible = true
        playerControls.isVisible = true
    }

    private fun onFullScreen() {
        cursor = blankCursor

        menu.isVisible = false
        playerControls.isVisible = false

        hideTimer = PostponeTimer {
            cursor = blankCursor
            playerControls.isVisible = false
        }
        mediaPlayer.videoSurfaceComponent().addMouseMotionListener(listener)
    }

}