package cz.encircled.eplayer.view.fx

import cz.encircled.eplayer.common.PostponeTimer
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.view.fx.components.AppMenuBar
import cz.encircled.eplayer.view.fx.components.ImageViewVideoSurface
import cz.encircled.eplayer.view.fx.components.PlayerControls
import javafx.application.Platform
import javafx.beans.Observable
import javafx.beans.property.FloatProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Menu
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.util.function.Consumer

private const val MOUSE_HIDE_DELAY = 600L

/**
 * @author Encircled on 18/09/2014.
 */
class PlayerScreen(
        private val dataModel: UiDataModel,
        core: ApplicationCore,
        menuBarProvider: AppMenuBar,
        private val fxView: FxView) : BorderPane(BorderPane()) {

    private val log = LogManager.getLogger()

    private lateinit var writableImage: WritableImage

    private var hideTimer: PostponeTimer = PostponeTimer { Platform.runLater { cursor = Cursor.NONE } }

    private lateinit var imageView: ImageView

    private lateinit var playerControls: PlayerControls

    private val menuBar = menuBarProvider.getMenuBar()

    private var playerHolder = Pane()

    private var playerStackPane = StackPane(playerHolder)

    private var videoSourceRatioProperty: FloatProperty = SimpleFloatProperty()

    private val fullScreenMouseMoveHandler = EventHandler { event: MouseEvent ->
        cursor = Cursor.DEFAULT
        when {
            event.y > height - playerControls.height -> {
                hideTimer.cancel()
                playerControls.isVisible = true
            }
            event.y <= menuBar.height -> {
                hideTimer.cancel()
                menuBar.isVisible = true
            }
            else -> {
                if (menuBar.isVisible) {
                    menuBar.isVisible = false
                    menuBar.menus.forEach(Consumer { obj: Menu -> obj.hide() })
                }
                hideTimer.postpone(MOUSE_HIDE_DELAY)
            }
        }
    }

    init {
        videoSourceRatioProperty = SimpleFloatProperty((fxView.screenBounds.width / fxView.screenBounds.height).toFloat())
        playerControls = PlayerControls(core, fxView, dataModel)
        playerHolder.style = "-fx-background-color: #000"
        initializeImageView()

        top = menuBar
        center = playerStackPane
        bottom = playerControls

        initializeListeners(core)
    }

    fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayer) {
        Platform.runLater {
            val start = System.currentTimeMillis()
            val videoSurface = ImageViewVideoSurface(writableImage, fxView, videoSourceRatioProperty).videoSurface
            println("Vidoe Surf: ${System.currentTimeMillis() - start}")
            mediaPlayer.videoSurface().set(videoSurface)
            println("Set Surf: ${System.currentTimeMillis() - start}")
        }
    }

    private fun initializeListeners(core: ApplicationCore) {
        dataModel.fitToScreen.addListener { _: Observable? -> fitImageViewSize(playerHolder.width, playerHolder.height) }
        playerHolder.onMouseClicked = EventHandler { event: MouseEvent ->
            core.mediaService.toggle()
            if (event.clickCount > 1) {
                fxView.toggleFullScreen()
            }
        }

        // Platform.runLater is required, because elements must be removed after full screen repaint
        fxView.primaryStage.fullScreenProperty().addNewValueListener { Platform.runLater { if (it) onFullScreen() else onNotFullScreen() } }
        if (fxView.isFullScreen) onFullScreen()
        else onNotFullScreen()
    }

    private fun onNotFullScreen() {
        hideTimer.cancel()
        playerStackPane.children.removeAll(playerControls, menuBar)
        cursor = Cursor.DEFAULT

        bottom = playerControls
        top = menuBar

        menuBar.isVisible = true
        playerControls.isVisible = true

        playerHolder.onMouseMoved = null
        playerControls.onMouseExited = null
        menuBar.onMouseExited = null
    }

    private fun onFullScreen() {
        cursor = Cursor.NONE

        children.removeAll(playerControls, menuBar)
        playerStackPane.children.addAll(playerControls, menuBar)

        StackPane.setAlignment(playerControls, Pos.BOTTOM_CENTER)
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER)

        menuBar.isVisible = false
        playerControls.isVisible = false

        playerHolder.onMouseMoved = fullScreenMouseMoveHandler
        playerControls.onMouseExited = EventHandler { playerControls.isVisible = false }
    }

    private fun initializeImageView() {
        writableImage = WritableImage(fxView.screenBounds.width.toInt(), fxView.screenBounds.height.toInt())
        imageView = ImageView(writableImage)
        playerHolder.children.add(imageView)

        playerHolder.widthProperty().addNewValueListener { fitImageViewSize(it.toDouble(), playerHolder.height) }
        playerHolder.heightProperty().addNewValueListener { fitImageViewSize(playerHolder.width, it.toDouble()) }

        videoSourceRatioProperty.addNewValueListener { fitImageViewSize(playerHolder.width, playerHolder.height) }
    }

    private fun fitImageViewSize(width: Double, height: Double) = fxThread {
        if (dataModel.fitToScreen.get()) {
            imageView.x = 0.0
            imageView.y = 0.0
            imageView.fitWidth = width
            imageView.fitHeight = height
        } else {
            val fitHeight = videoSourceRatioProperty.get() * width
            if (fitHeight > height) {
                val fitWidth = height / videoSourceRatioProperty.get()
                imageView.fitHeight = height
                imageView.fitWidth = fitWidth
                imageView.x = (width - fitWidth) / 2
                imageView.y = 0.0
            } else {
                imageView.fitWidth = width
                imageView.fitHeight = fitHeight
                imageView.y = (height - fitHeight) / 2
                imageView.x = 0.0
            }
        }
    }

}