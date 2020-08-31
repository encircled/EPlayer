package cz.encircled.eplayer.core

import cz.encircled.eplayer.common.Constants
import cz.encircled.eplayer.model.AppSettings
import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.remote.RemoteControlHttpServer
import cz.encircled.eplayer.service.*
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.EventObserver
import cz.encircled.eplayer.service.event.EventObserverImpl
import cz.encircled.eplayer.util.IOUtil.createIfMissing
import cz.encircled.eplayer.util.IOUtil.getSettings
import cz.encircled.eplayer.util.LocalizationProvider
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.fx.FxRemoteControlHandler
import javafx.application.Platform
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.player.base.MediaPlayer
import kotlin.system.exitProcess

/**
 * TODO
 *
 * Series:
 * - Next episode
 * - Continue
 * - Episode selection
 * - Collapse as single one
 *
 * Check display changes
 *
 * TODO CHECK TRUEHD DOLBY
 */
class ApplicationCore {

    val cacheService: CacheService

    val folderScanService: FolderScanService

    val settings: AppSettings

    val eventObserver: EventObserver

    lateinit var mediaService: MediaService

    lateinit var appView: AppView

    lateinit var seriesFinder: SeriesFinder

    private lateinit var remoteControlServer: RemoteControlHttpServer

    init {
        createIfMissing(APP_DOCUMENTS_ROOT, true)
        createIfMissing(SCREENS_FOLDER, true)

        cacheService = JsonCacheService()
        folderScanService = OnDemandFolderScanner(this)
        eventObserver = EventObserverImpl()
        settings = try {
            getSettings()
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException(e)
        }
        LocalizationProvider.init(settings)
    }

    fun delayedInit(appView: AppView, mediaPlayer: MediaPlayer, playerRemoteControl: RemoteControlHandler?) {
        this.appView = appView
        mediaService = VLCMediaService(this, mediaPlayer)
        cacheService.delayedInit(this)
        seriesFinder = SeriesFinder()
        eventObserver.listen(Event.contextInitialized) {
            remoteControlServer = RemoteControlHttpServer(FxRemoteControlHandler(this, playerRemoteControl!!))
        }
        addCloseHook()
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
        cacheService.lastByWatchDate?.let {
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
            //        return new File(fileLocation).exists() ? URL_FILE_PREFIX + fileLocation : "";
            return URL_FILE_PREFIX + fileLocation
        }
    }

}