package cz.encircled.eplayer.core

import cz.encircled.eplayer.common.Constants
import cz.encircled.eplayer.model.AppSettings
import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.remote.RemoteControlHttpServer
import cz.encircled.eplayer.service.*
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.IOUtil.createIfMissing
import cz.encircled.eplayer.util.IOUtil.getSettings
import cz.encircled.eplayer.util.LocalizationProvider
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.fx.FxRemoteControlHandler
import cz.encircled.eplayer.view.fx.FxView
import javafx.application.Platform
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import kotlin.system.exitProcess

/**
 * TODO
 *
 * Series:
 * - Auto next episode
 *
 * Check display changes
 *
 * TODO CHECK TRUEHD DOLBY
 */
class ApplicationCore {

    // Enable HDMI audio passthrough for Dolby/DTS audio TODO configurable
    val VLC_ARGS = "--mmdevice-passthrough=2"

    lateinit var cacheService: CacheService

    lateinit var folderScanService: FolderScanService

    val settings: AppSettings

    lateinit var mediaService: MediaService

    lateinit var appView: AppView

    lateinit var seriesFinder: SeriesFinder

    private lateinit var remoteControlServer: RemoteControlHttpServer

    init {
        createIfMissing(APP_DOCUMENTS_ROOT, true)
        createIfMissing(SCREENS_FOLDER, true)

        settings = try {
            getSettings()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
        LocalizationProvider.init(settings)
    }

    fun delayedInit(appView: AppView, playerRemoteControl: RemoteControlHandler) {
        val start = System.currentTimeMillis()
        this.appView = appView
        cacheService = JsonCacheService()
        folderScanService = OnDemandFolderScanner(this)
        val vlcMediaService = VLCMediaService(this)
        mediaService = vlcMediaService
        cacheService.delayedInit(this)
        seriesFinder = SeriesFinder()
        log.debug("SeriesFinder finished in ${System.currentTimeMillis() - start}")
        remoteControlServer = RemoteControlHttpServer(FxRemoteControlHandler(this, playerRemoteControl))
        log.debug("Remote finished in ${System.currentTimeMillis() - start}")

        val playerStart = System.currentTimeMillis()
        val mediaPlayerFactory = MediaPlayerFactory(VLC_ARGS)
        val mediaPlayer: EmbeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer()
        log.debug("MediaPlayer finished in ${System.currentTimeMillis() - playerStart}")
        log.debug("MediaPlayer total finished in ${System.currentTimeMillis() - start}")
        appView.setMediaPlayer(mediaPlayer)
        log.debug("Core step 1 ${System.currentTimeMillis() - start}")
        vlcMediaService.setMediaPlayer(mediaPlayer)
        log.debug("Core step 2 ${System.currentTimeMillis() - start}")
        addCloseHook()
        log.debug("Core start finished in ${System.currentTimeMillis() - start}")
        Event.contextInitialized.fire(this)
    }

    fun openQuickNavi() {
        Thread {
            mediaService.pause()
            appView.showQuickNavi()
            mediaService.updateCurrentMediaInCache()
            mediaService.stop()
            cacheService.save()
        }.start()
    }

    fun back() {
        if (appView.isPlayerScene) {
            if (appView.isFullScreen) {
                appView.toggleFullScreen()
                Thread { mediaService.updateCurrentMediaInCache() }.start()
            } else openQuickNavi()
        }
    }

    fun playLast() {
        log.debug("Play last media")
        cacheService.lastByWatchDate()?.let {
            mediaService.play(it)
        }
    }

    fun exit() {
        Platform.exit()
        exitProcess(Constants.ZERO)
    }

    private fun addCloseHook() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                log.debug("Close hook start")
                mediaService.updateCurrentMediaInCache()
                cacheService.save()
                mediaService.releasePlayer()
                log.debug("Close hook finished")
            }
        })
        log.trace("Close hook added")
    }

    companion object {
        val APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\"

        val SCREENS_FOLDER = "$APP_DOCUMENTS_ROOT\\frames\\"

        private const val URL_FILE_PREFIX = "file:\\\\\\"

        private val log = LogManager.getLogger()

        fun getScreenshotLocation(mediaFile: MediaFile): String {
            return SCREENS_FOLDER + mediaFile.name + mediaFile.size + ".png"
        }

        @JvmStatic
        fun getScreenshotURL(mediaFile: MediaFile): String {
            val fileLocation = getScreenshotLocation(mediaFile)
            return URL_FILE_PREFIX + fileLocation
        }
    }

}