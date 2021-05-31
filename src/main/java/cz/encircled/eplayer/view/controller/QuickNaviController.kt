package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.service.CancelableExecution
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.UiUtil.inUiThread
import org.apache.logging.log4j.LogManager
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * @author encir on 05-Sep-20.
 */
class QuickNaviController(
    private val dataModel: UiDataModel,
    private val core: ApplicationCore
) : RemoteControlHandler {

    private val log = LogManager.getLogger()

    /**
     * For remote control
     */
    private var selectedItemIndex: Int = 0

    private val mediaSource = ArrayList<PlayableMedia>()

    private var currentFolderScanning: CancelableExecution<List<PlayableMedia>>? = null

    // TODO set next episode on back, ios app?
    private val typeToComparator: Map<SortType, Comparator<PlayableMedia>> = mapOf(
        SortType.Duration to Comparator { o1, o2 ->
            o1.duration.value.compareTo(o2.duration.value)
        },
        SortType.Name to Comparator { o1, o2 ->
            o1.name().compareTo(o2.name())
        },
        SortType.Date to Comparator { o1, o2 ->
            o1.watchDate.compareTo(o2.watchDate)
        }
    )

    fun init(appView: AppView) {
        Event.contextInitialized.listenUiThread {
            dataModel.foldersToScan.setAll(listOf(QUICK_NAVI) + core.settings.foldersToScan)

            appView.currentSceneProperty.addNewValueListener {
                if (it == Scenes.QUICK_NAVI) forceRefresh()
            }
        }

        dataModel.filter.addNewValueListener {
            dataModel.media.setAll(getFilteredMedia(mediaSource, it))
        }

        dataModel.sortType.addNewValueListener {
            UiUtil.inNormalThread {
                val filteredMedia = getFilteredMedia(mediaSource)
                inUiThread {
                    dataModel.media.setAll(filteredMedia)
                }
            }
        }
    }

    fun play(media: PlayableMedia) = UiUtil.inNormalThread {
        core.mediaService.play(media)
    }

    fun play(path: String) = UiUtil.inNormalThread {
        core.mediaService.play(path)
    }

    fun deleteEntry(media: PlayableMedia) {
        core.cacheService.deleteEntry(media)
    }

    fun doWebSearch(media: PlayableMedia) {
        val cleanedName = URLEncoder.encode(media.name(), "UTF-8")
        Runtime.getRuntime()
            .exec(arrayOf("PowerShell", "start chrome https://www.kinopoisk.ru/index.php?kp_query=$cleanedName"))
    }

    fun onFolderSelect(path: String) {
        if (dataModel.selectedFolder.get() != path) {
            currentFolderScanning?.cancel()
            currentFolderScanning = null

            if (path == QUICK_NAVI) {
                onQuickNaviSelect()
            } else {
                dataModel.selectedFolder.set(path)
                dataModel.media.clear()
                mediaSource.clear()

                currentFolderScanning = CancelableExecution {
                    inUiThread {
                        mediaSource.addAll(it)
                        dataModel.media.addAll(getFilteredMedia(it))
                    }
                }

                UiUtil.inNormalThread {
                    core.folderScanService.getMediaInFolder(path, currentFolderScanning!!)
                }
            }
        }
    }

    private fun onQuickNaviSelect() {
        dataModel.selectedFolder.set(QUICK_NAVI)

        currentFolderScanning = CancelableExecution {
            mediaSource.clear()
            mediaSource.addAll(it)
            dataModel.media.setAll(getFilteredMedia(mediaSource))
        }
        currentFolderScanning?.invoke(core.cacheService.getPlayedMedia())
    }

    fun forceRefresh() = inUiThread {
        onFolderSelect(dataModel.selectedFolder.get())
    }

    fun addTab(path: String) {
        dataModel.foldersToScan.add(path)
        core.settings.addFolderToScan(path)
    }

    fun removeTab(path: String) {
        if (path != QUICK_NAVI && dataModel.foldersToScan.remove(path)) {
            if (dataModel.selectedFolder.get() == path) {
                onQuickNaviSelect()
            }
            core.settings.removeFolderToScan(path)
        }
    }

    override fun goToNextMedia() = inUiThread {
        selectedItemIndex = nextIndex(selectedItemIndex, dataModel.media)

        if (selectedItemIndex == 0) {
            val currentFolder = dataModel.foldersToScan.indexOf(dataModel.selectedFolder.get())
            onFolderSelect(dataModel.foldersToScan[nextIndex(currentFolder, dataModel.foldersToScan)])
        }

        if (dataModel.media.isNotEmpty()) dataModel.selectedMedia.set(dataModel.media[selectedItemIndex])
    }

    override fun goToPrevMedia() = inUiThread {
        if (selectedItemIndex > 0) {
            selectedItemIndex--
        } else {
            selectedItemIndex = dataModel.media.size - 1
        }
        dataModel.selectedMedia.set(dataModel.media[selectedItemIndex])
    }

    override fun forward() {
        val media = dataModel.selectedMedia.get()
        if (media is MediaSeries) {
            media.toNext()
        }
    }

    override fun backward() {
        val media = dataModel.selectedMedia.get()
        if (media is MediaSeries) {
            media.toPrev()
        }
    }

    override fun playSelected() = inUiThread {
        core.mediaService.play(dataModel.media[selectedItemIndex])
    }

    override fun watchLastMedia() {
        log.debug("Play last media")
        core.cacheService.lastByWatchDate()?.let {
            core.mediaService.play(it)
        }
    }

    override fun playPause() = throw NotImplementedError()

    private fun getFilteredMedia(
        mediaFiles: List<PlayableMedia>,
        filter: String = dataModel.filter.get()
    ): List<PlayableMedia> =
        if (filter.isNotBlank()) {
            val p = Pattern.compile("(?i).*" + filter.replace(" ".toRegex(), ".*") + ".*")
            val m = p.matcher("")
            mediaFiles.filter { m.reset(it.mediaFile().name).matches() }
                .sortedWith(typeToComparator.getValue(dataModel.sortType.get()))
        } else {
            mediaFiles.sortedWith(typeToComparator.getValue(dataModel.sortType.get()))
        }

    private fun nextIndex(current: Int, max: Collection<*>): Int = if (current < max.size - 1) current + 1 else 0

}