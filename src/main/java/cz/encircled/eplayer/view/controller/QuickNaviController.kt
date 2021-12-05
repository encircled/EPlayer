package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.service.CancelableExecution
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.TimeMeasure.measure
import cz.encircled.eplayer.view.*
import cz.encircled.fswing.inNormalThread
import cz.encircled.fswing.inUiThread
import cz.encircled.fswing.onChange
import org.apache.logging.log4j.LogManager
import java.io.File
import java.net.URLEncoder
import kotlin.math.min

interface QuickNaviController : RemoteControlHandler {

    fun play(media: PlayableMedia)
    fun openFolder(folder: UiFolderMedia)
    fun openFolder(path: String)
    fun removeTab(path: String)
    fun addTab(path: String)
    fun doWebSearch(media: PlayableMedia)
    fun deleteEntry(media: PlayableMedia)
    fun openTab(path: String)
    fun play(path: String)
}

/**
 * @author encir on 05-Sep-20.
 */
class QuickNaviControllerImpl(
    private val dataModel: UiDataModel,
    private val core: ApplicationCore
) : QuickNaviController {

    private val log = LogManager.getLogger()

    /**
     * For remote control
     */
    private var selectedItemIndex: Int? = null

    private val mediaSource = ArrayList<PlayableMedia>()

    @Volatile
    private var currentFolderScanning: CancelableExecution<List<PlayableMedia>>? = null

    // TODO set next episode on back?
    // TODO arrow for next/prev video?
    private val mediaProcessors: MutableList<MediaProcessor> = mutableListOf(
        DynamicFilterMediaProcessor(dataModel),
        GroupByFolderMediaProcessor(dataModel),
        NameFilterMediaProcessor(dataModel),
        AggregationMediaProcessor(dataModel),
        SortingMediaProcessor(dataModel),
    )

    fun init(appView: AppView) {
        Event.contextInitialized.listenUiThread {
            dataModel.foldersToScan.setAll(listOf(QUICK_NAVI) + core.settings.foldersToScan)

            appView.currentSceneProperty.onChange {
                if (it == Scenes.QUICK_NAVI) refreshMedia()
            }
        }

        listOf(
            dataModel.dynamicFilterForMedia,
            dataModel.filter,
            dataModel.sortType,
            dataModel.aggregationType,
            dataModel.flatView
        ).forEach {
            it.onChange { refreshMedia() }
        }
    }

    override fun play(media: PlayableMedia) = inNormalThread {
        core.mediaService.play(media)
    }

    override fun play(path: String) = inNormalThread {
        core.mediaService.play(path)
    }

    override fun back() {
        if (dataModel.dynamicFilterForMedia.get() != null) {
            dataModel.dynamicFilterForMedia.set(null)
        } else {
            val fullPath = dataModel.selectedFullPath.get()
            if (fullPath != dataModel.selectedTab.get()) {
                openFolder(fullPath.substring(0, fullPath.lastIndexOf(File.separator)))
            }
        }
    }

    override fun openTab(path: String) {
        if (dataModel.selectedTab.get() != path || dataModel.selectedFullPath.get() != dataModel.selectedTab.get()) {
            log.info("Open tab $path")
            currentFolderScanning?.cancel()
            currentFolderScanning = null

            if (path == QUICK_NAVI) {
                onQuickNaviSelect()
            } else {
                dataModel.selectedFullPath.set(path)
                dataModel.selectedTab.set(path)
                doOpenFolder(path)
            }
        }
    }

    override fun openFolder(folder: UiFolderMedia) {
        if (folder.dynamicFilter == null) {
            openFolder(folder.path)
        } else {
            dataModel.dynamicFilterForMedia.value = folder.dynamicFilter
        }
    }

    override fun openFolder(path: String) {
        if (dataModel.selectedFullPath.get() != path) {
            doOpenFolder(path)
        }
    }

    private fun doOpenFolder(path: String) {
        log.info("Open folder $path")
        dataModel.selectedFullPath.set(path)
        dataModel.dynamicFilterForMedia.value = null
        dataModel.media.clear()
        mediaSource.clear()

        currentFolderScanning = CancelableExecution {
            mediaSource.addAll(it)
            refreshMedia()
        }

        inNormalThread {
            core.folderScanService.getMediaInFolder(path, currentFolderScanning!!)
        }
    }

    override fun deleteEntry(media: PlayableMedia) {
        log.info("Delete $media")
        core.appView.getUserConfirmation(
            "Delete the file as well?",
            onConfirm = {
                core.cacheService.deleteEntry(media)
                File(media.path).delete()
                File(media.filePathToScreenshot).delete()
            },
            onDecline = {
                core.cacheService.deleteEntry(media)
            }
        )
    }

    override fun doWebSearch(media: PlayableMedia) {
        val cleanedName = URLEncoder.encode(media.name(), "UTF-8")
        Runtime.getRuntime()
            .exec(arrayOf("PowerShell", "start chrome https://www.kinopoisk.ru/index.php?kp_query=$cleanedName"))
    }

    override fun addTab(path: String) {
        dataModel.foldersToScan.add(path)
        core.settings.addFolderToScan(path)
    }

    override fun removeTab(path: String) {
        if (path != QUICK_NAVI && dataModel.foldersToScan.remove(path)) {
            if (dataModel.selectedTab.get() == path) {
                onQuickNaviSelect()
            }
            core.settings.removeFolderToScan(path)

            // Delete cache entries, which does not belong to any tab anymore
            core.cacheService.getCachedMedia()
                .filter { media -> dataModel.foldersToScan.all { !media.path.startsWith(it) } }
                .forEach { core.cacheService.deleteEntry(it) }
        }
    }

    override fun goToNextMedia() = goToMedia(1, 0)

    override fun goToPrevMedia() = goToMedia(-1, dataModel.foldersToScan.size - 1)

    private fun goToMedia(direction: Int, nextTab: Int) = inUiThread {
        selectedItemIndex = nextIndex(selectedItemIndex, direction, dataModel.media) {
            val currentFolder = dataModel.foldersToScan.indexOf(dataModel.selectedTab.get())
            openTab(dataModel.foldersToScan[nextIndex(currentFolder, direction, dataModel.foldersToScan) { nextTab }])
            0
        }

        if (dataModel.media.isNotEmpty()) dataModel.selectedMedia.set(dataModel.media[selectedItemIndex!!])
    }

    override fun forward() {
        val media = dataModel.selectedMedia.get()
        // TODO forward for folder?
        if (media is UiPlayableMedia) {
            if (media.media is MediaSeries) {
                media.media.toNext()
            }
        }
    }

    override fun backward() {
        val media = dataModel.selectedMedia.get()
        // TODO backward for folder?
        if (media is UiPlayableMedia) {
            if (media.media is MediaSeries) {
                media.media.toPrev()
            }
        }
    }

    override fun playSelected() = inUiThread {
        val media = dataModel.media[selectedItemIndex ?: 0]
        if (media is UiPlayableMedia) {
            core.mediaService.play(media.media)
        } else if (media is UiFolderMedia) {
            openFolder(media.path)
        }
    }

    override fun watchLastMedia() {
        log.debug("Play last media")
        core.cacheService.lastByWatchDate()?.let {
            core.mediaService.play(it)
        }
    }

    override fun playPause() = throw NotImplementedError()

    private fun refreshMedia() = inNormalThread {
        val filteredMedia = getFilteredMedia(mediaSource)
        inUiThread {
            dataModel.media.setAll(filteredMedia)
            selectedItemIndex?.let {
                val itOrLast = min(it, dataModel.media.size - 1)
                selectedItemIndex = itOrLast
                dataModel.selectedMedia.set(dataModel.media[itOrLast])
            }
        }
    }

    private fun onQuickNaviSelect() {
        if (dataModel.selectedTab.get() != QUICK_NAVI) {
            dataModel.selectedTab.set(QUICK_NAVI)
            dataModel.selectedFullPath.set(QUICK_NAVI)

            currentFolderScanning = CancelableExecution {
                mediaSource.clear()
                mediaSource.addAll(it)
                refreshMedia()
            }
            currentFolderScanning?.invoke(core.cacheService.getPlayedMedia())
        }
    }

    private fun getFilteredMedia(mediaFiles: List<PlayableMedia>): List<UiMedia> {
        var s = mediaFiles.map { UiPlayableMedia(it) as UiMedia }.asSequence()
        mediaProcessors.forEach {
            measure("Media processor ${it.javaClass.simpleName}") {
                s = it.process(s)
            }
        }

        return measure("Filtering media...") { s.toList() }
    }

    private fun nextIndex(current: Int?, increment: Int, max: Collection<*>, doOnOverflow: () -> Int): Int = when {
        current == null -> 0
        (max.indices).contains(current + increment) -> current + increment
        else -> doOnOverflow()
    }

}