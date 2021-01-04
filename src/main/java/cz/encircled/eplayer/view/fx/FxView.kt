package cz.encircled.eplayer.view.fx

import com.sun.jna.NativeLibrary
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.fx.components.AppMenuBar
import cz.encircled.eplayer.view.fx.controller.QuickNaviController
import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.stage.FileChooser
import javafx.stage.Screen
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.binding.RuntimeUtil
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * TODO:
 * - remember subtitles
 * - Set duration after start, auto next series
 * - Toggle in fullscreen
 */
fun main() {
    Application.launch(FxView::class.java)
}

/**
 * @author Encircled on 18/09/2014.
 */
class FxView : Application(), AppView {
    companion object {
        const val VLC_LIB_PATH = "C:\\Program Files\\VideoLAN\\VLC"
        const val MIN_WIDTH = 860
        const val MIN_HEIGHT = 600
        const val QUICK_NAVI_SCREEN = "quickNavi"
        const val PLAYER_SCREEN = "player"
        private val log = LogManager.getLogger()

        init {
            log.trace("Initialize VLC libs")
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_LIB_PATH)
        }
    }

    lateinit var screenBounds: Rectangle2D
        private set

    private lateinit var mediaFileChooser: FileChooser

    private lateinit var quickNaviScreen: QuickNaviScreen

    lateinit var quickNaviController: QuickNaviController
        private set

    private lateinit var playerScreen: PlayerScreen

    lateinit var primaryStage: Stage
        private set

    private lateinit var primaryScene: Scene

    lateinit var sceneChangeProperty: StringProperty

    private lateinit var core: ApplicationCore

    override fun isPlayerScene(): Boolean = PLAYER_SCREEN == sceneChangeProperty.value

    override fun showQuickNavi() = fxThread {
        if (isPlayerScene) {
            primaryScene.root = quickNaviScreen
            sceneChangeProperty.value = QUICK_NAVI_SCREEN
        }
    }

    override fun showPlayer(countDown: CountDownLatch) = fxThread(countDown) {
        if (!isPlayerScene) {
            primaryScene.root = playerScreen
            sceneChangeProperty.value = PLAYER_SCREEN
        }
    }

    override fun toggleFullScreen() = fxThread {
        primaryStage.isFullScreen = !primaryStage.isFullScreen
    }

    override fun isFullScreen(): Boolean = primaryStage.isFullScreen

    override fun start(primaryStage: Stage) {
        val start = System.currentTimeMillis()
        this.primaryStage = primaryStage
        screenBounds = Screen.getPrimary().visualBounds
        sceneChangeProperty = SimpleStringProperty(QUICK_NAVI_SCREEN)
        core = ApplicationCore()

        val dataModel = UiDataModel()
        quickNaviController = QuickNaviController(this, dataModel, core)

        playerScreen = PlayerScreen(dataModel, core, AppMenuBar(core, this, dataModel), this)
        quickNaviScreen = QuickNaviScreen(dataModel, quickNaviController, AppMenuBar(core, this, dataModel), this)
        primaryScene = Scene(quickNaviScreen)

        initializePrimaryStage()
        initializeMediaFileChoose()
        initializeScene()

        log.debug("UI start finished in ${System.currentTimeMillis() - start}")

        Thread {
            core.delayedInit(this, quickNaviController)
        }.start()
    }

    override fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayer) = fxThread {
        playerScreen.setMediaPlayer(mediaPlayer)
    }

    private fun initializePrimaryStage() {
        primaryStage.title = AppView.TITLE
        primaryStage.minHeight = MIN_HEIGHT.toDouble()
        primaryStage.minWidth = MIN_WIDTH.toDouble()
        primaryStage.fullScreenExitHint = ""
        primaryStage.isMaximized = true
    }

    override fun openMediaChooser() {
        val file = mediaFileChooser.showOpenDialog(primaryStage)
        if (file != null) {
            core.mediaService.play(file.absolutePath)
            mediaFileChooser.initialDirectory = file.parentFile
            Thread {
                core.settings.setOpenLocation(file.parentFile.absolutePath)
            }.start()
        }
    }

    private fun initializeScene() {
        primaryScene.stylesheets.add("/stylesheet.css")
        primaryScene.onDragOver = EventHandler { event: DragEvent ->
            if (event.dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY)
            } else {
                event.consume()
            }
        }

        primaryScene.onDragDropped = newTabDropHandler
        primaryStage.onCloseRequest = EventHandler { core.exit() }

        primaryStage.scene = primaryScene
        primaryStage.show()
    }

    val newTabDropHandler: EventHandler<DragEvent>
        get() = EventHandler { event: DragEvent ->
            val db = event.dragboard
            var success = false
            if (db.hasFiles()) {
                success = true
                for (file in db.files) {
                    if (file.isDirectory) {
                        val filePath = file.path
                        log.debug("DnD new tab {}", filePath)
                        quickNaviController.addTab(filePath)
                        success = true
                    }
                }
            }
            event.isDropCompleted = success
            event.consume()
        }

    private fun initializeMediaFileChoose() {
        mediaFileChooser = FileChooser()
        mediaFileChooser.title = Localization.open.ln()
        val initialLocation = core.settings.fcOpenLocation
        if (initialLocation != null) {
            val initialDirectory = File(initialLocation)
            if (initialDirectory.exists()) mediaFileChooser.initialDirectory = initialDirectory
        }
    }

}