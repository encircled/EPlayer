package cz.encircled.eplayer

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.remote.RemoteControlHttpServer
import cz.encircled.eplayer.service.MetadataInfoService
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.Scenes
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.AfterAll
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.io.File
import java.util.concurrent.CountDownLatch

abstract class BaseTest {

    companion object {

        lateinit var core: ApplicationCore

        @JvmStatic
        @AfterAll
        fun afterAll() {
            if (this::core.isInitialized) {
                core.stopApp()
            }
        }

    }

    val pathToResources = File("").absoluteFile.resolve("src/main/test/resources")

    init {
        pathToResources.resolve("quicknavi2.json").delete()
        core = ApplicationCore { TestServer() }
        core.ioUtil.quickNaviPath = pathToResources.resolve("quicknavi2.json").path
        core.ioUtil.pathToSettings = pathToResources.resolve("eplayer.properties.json").path

        core.delayedInit(TestView(), TestRemoteControl())
        core.metaInfoService = TestMetadataService()
    }

    class TestRemoteControl : RemoteControlHandler {
        override fun toFullScreen() {
        }

        override fun back() {
        }

        override fun goToNextMedia() {
        }

        override fun goToPrevMedia() {
        }

        override fun playSelected() {
        }

        override fun watchLastMedia() {
        }

        override fun playPause() {
        }
    }

    class TestMetadataService : MetadataInfoService {
        val added: MutableList<PlayableMedia> = ArrayList()
        override fun <T : PlayableMedia> fetchMetadataAsync(media: T): T {
            added.add(media)
            return media
        }
    }

    class TestView : AppView {
        override fun setMediaPlayer(mediaPlayer: EmbeddedMediaPlayerComponent) {
        }

        override fun showPlayer(countDownLatch: CountDownLatch) {
            countDownLatch.countDown()
        }

        override fun isFullScreen(): Boolean = false

        override val currentSceneProperty: ObjectProperty<Scenes>
            get() = SimpleObjectProperty(Scenes.QUICK_NAVI)

        override fun fullScreenProperty(): ReadOnlyBooleanProperty = SimpleBooleanProperty(false)

        override fun showQuickNaviScreen() {
        }

        override fun toggleFullScreen() {
        }

        override fun openMediaChooser() {
        }

        override fun showUserMessage(msg: String) {
            println("User message: $msg")
        }
    }

    class TestServer : RemoteControlHttpServer {
        override fun stop() {

        }
    }

}