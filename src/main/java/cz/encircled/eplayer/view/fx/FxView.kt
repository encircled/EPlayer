package cz.encircled.eplayer.view.fx

import com.sun.jna.NativeLibrary
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.UiUtil.inUiThread
import cz.encircled.eplayer.view.fx.components.AppMenuBar
import cz.encircled.eplayer.view.controller.QuickNaviController
import javafx.application.Application
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleObjectProperty
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
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.io.File
import java.util.concurrent.CountDownLatch

fun main() {
    Application.launch(FxView::class.java)
}

/**
 * @author Encircled on 18/09/2014.
 */
class FxView : Application(), AppView {
    companion object {
        const val VLC_LIB_PATH = "C:\\Program Files\\VideoLAN\\VLC"
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

    override lateinit var currentSceneProperty: ObjectProperty<Scenes>

    private lateinit var core: ApplicationCore

    override fun showQuickNaviScreen() = inUiThread {
        if (currentSceneProperty.get() != Scenes.QUICK_NAVI) {
            primaryScene.root = quickNaviScreen
            currentSceneProperty.value = Scenes.QUICK_NAVI
        }
    }

    override fun showPlayer(countDown: CountDownLatch) = inUiThread(countDown) {
        if (currentSceneProperty.get() != Scenes.PLAYER) {
            primaryScene.root = playerScreen
            currentSceneProperty.value = Scenes.PLAYER
        }
    }

    override fun toggleFullScreen() = inUiThread {
        primaryStage.isFullScreen = !primaryStage.isFullScreen
    }

    override fun isFullScreen(): Boolean = primaryStage.isFullScreen

    override fun fullScreenProperty(): ReadOnlyBooleanProperty = primaryStage.fullScreenProperty()

    override fun start(primaryStage: Stage) {
        val start = System.currentTimeMillis()
        UiUtil.uiExecutor = FxUiExecutor()
        this.primaryStage = primaryStage
        screenBounds = Screen.getPrimary().visualBounds
        currentSceneProperty = SimpleObjectProperty(Scenes.QUICK_NAVI)
        core = ApplicationCore()

        val dataModel = UiDataModel()
        quickNaviController = QuickNaviController(dataModel, core)
        quickNaviController.init(this)

        playerScreen = PlayerScreen(dataModel, core, AppMenuBar(core, this, dataModel), this)
        quickNaviScreen = QuickNaviScreen(dataModel, quickNaviController, AppMenuBar(core, this, dataModel), this)
        primaryScene = Scene(quickNaviScreen)

        initializePrimaryStage()
        initializeMediaFileChoose()
        initializeScene()

        Event.contextInitialized.listenUiThread {
            quickNaviController.onFolderSelect(QUICK_NAVI)
        }

        log.debug("UI start finished in ${System.currentTimeMillis() - start}")

        Thread {
            core.delayedInit(this, quickNaviController)
        }.start()
    }

    override fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayerComponent) = inUiThread {
        playerScreen.setMediaPlayer(mediaPlayer)
    }

    private fun initializePrimaryStage() {
        primaryStage.title = AppView.TITLE
        primaryStage.minHeight = AppView.MIN_HEIGHT.toDouble()
        primaryStage.minWidth = AppView.MIN_WIDTH.toDouble()
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

    override fun showUserMessage(msg: String) {
        TODO("Not yet implemented")
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