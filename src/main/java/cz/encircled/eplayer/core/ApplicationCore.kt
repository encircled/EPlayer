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
import cz.encircled.eplayer.view.Scenes
import cz.encircled.eplayer.view.controller.RemoteControlHandlerImpl
import javafx.application.Platform
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess

/**
 * TODO list
 *
 * Series:
 * - Auto next episode
 *
 * Notify if audio thru pass is needed
 * Switch audio thru HDMI automatically?
 * Check display changes
 * Scan all folders on start
 *
 * CHECK TRUEHD DOLBY
 */
class ApplicationCore {

    val settings: AppSettings

    lateinit var cacheService: CacheService

    lateinit var folderScanService: FolderScanService

    lateinit var mediaService: MediaService

    lateinit var appView: AppView

    lateinit var seriesFinder: SeriesFinder

    lateinit var metaInfoService: MetadataInfoService

    lateinit var mediaSettingsSuggestions: MediaSettingsSuggestions

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
        this.metaInfoService = JavacvMetadataInfoService()
        this.mediaSettingsSuggestions = MediaSettingsSuggestionsImpl()
        this.cacheService = JsonCacheService(this)
        this.folderScanService = OnDemandFolderScanner(this)

        this.seriesFinder = SeriesFinder()
        this.remoteControlServer = RemoteControlHttpServer(RemoteControlHandlerImpl(this, playerRemoteControl))

        this.mediaService = VLCMediaService(this)

        Event.audioPassThroughChange.listen {
            resetPlayer(appView)
        }
        resetPlayer(appView)

        addCloseHook()
        log.debug("Core start finished in ${System.currentTimeMillis() - start}")
        Event.contextInitialized.fire(this)
    }

    fun openQuickNaviScreen() {
        Thread {
            if (mediaService.isPlaying()) {
                mediaService.pause()
            }
            appView.showQuickNaviScreen()
            mediaService.stop()
            cacheService.save()
        }.start()
    }

    fun back() {
        if (appView.currentSceneProperty.get() == Scenes.PLAYER) {
            if (appView.isFullScreen()) {
                appView.toggleFullScreen()
                mediaService.pause()
            } else {
                openQuickNaviScreen()
            }
        }
    }

    fun exit() {
        Platform.exit()
        exitProcess(Constants.ZERO)
    }

    private fun resetPlayer(appView: AppView) {
        val currentMedia = mediaService.currentMedia()
        appView.setMediaPlayer(mediaService.createPlayer())
        if (currentMedia != null) mediaService.play(currentMedia)
    }

    private fun addCloseHook() {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                log.debug("Close hook start")
                mediaService.stop()
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

        const val URL_FILE_PREFIX = "file:\\\\\\"

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