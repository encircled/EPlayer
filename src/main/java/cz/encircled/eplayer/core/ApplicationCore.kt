package cz.encircled.eplayer.core

import cz.encircled.eplayer.common.Constants
import cz.encircled.eplayer.model.AppSettings
import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.remote.RemoteControlHttpServer
import cz.encircled.eplayer.remote.RemoteControlHttpServerImpl
import cz.encircled.eplayer.service.*
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.IOUtil
import cz.encircled.eplayer.util.LocalizationProvider
import cz.encircled.eplayer.util.TimeMeasure.measure
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.swing.AppActions
import javafx.application.Platform
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess

/**
 * TODO list
 *
 * Series:
 * - Auto next episode
 * title changes on pause
 *
 * Check display changes
 * Scan all folders on start
 *
 * show info messages: name, subtitle, audio track etc
 *
 * CHECK TRUEHD DOLBY
 */
class ApplicationCore(
    private val remoteControlServerCreator: (RemoteControlHandler) -> RemoteControlHttpServer = {
        RemoteControlHttpServerImpl(it)
    }
) {

    val settings: AppSettings

    val ioUtil: IOUtil = IOUtil()

    lateinit var cacheService: CacheService

    lateinit var folderScanService: FolderScanService

    lateinit var mediaService: MediaService

    lateinit var appView: AppView

    lateinit var appActions: AppActions

    lateinit var seriesFinder: SeriesFinder

    lateinit var metaInfoService: MetadataInfoService

    lateinit var mediaSettingsSuggestions: MediaSettingsSuggestions

    private lateinit var remoteControlServer: RemoteControlHttpServer

    init {
        ioUtil.createIfMissing(APP_DOCUMENTS_ROOT, true)
        ioUtil.createIfMissing(SCREENS_FOLDER, true)

        settings = try {
            ioUtil.getSettings()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
        settings.core = this
        LocalizationProvider.init(settings)
    }

    fun delayedInit(appView: AppView) {
        measure("AppCore init") {
            this.appView = appView
            this.metaInfoService = JavacvMetadataInfoService()
            this.mediaSettingsSuggestions = MediaSettingsSuggestionsImpl()
            this.cacheService = JsonCacheService(this)
            this.folderScanService = OnDemandFolderScanner(this)
            this.seriesFinder = SeriesFinder()
            this.appActions = appView.actions
            this.remoteControlServer = remoteControlServerCreator.invoke(appActions)
            this.mediaService = VLCMediaService(this)

            Event.audioPassThroughChange.listen {
                resetPlayer(appView)
            }
        }

        measure("AppCore init player") {
            resetPlayer(appView)
            addCloseHook()
        }

        Event.contextInitialized.fire(this)
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
                stopApp()
            }
        })
        log.trace("Close hook added")
    }

    fun stopApp() {
        log.debug("Close hook start")
        mediaService.stop()
        cacheService.save()
        mediaService.releasePlayer()
        remoteControlServer.stop()
        log.debug("Close hook finished")
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