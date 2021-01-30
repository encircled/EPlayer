package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.service.CancelableExecution
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.UiUtil.inUiThread
import java.util.regex.Pattern
import javax.swing.SwingUtilities

/**
 * @author encir on 05-Sep-20.
 */
class QuickNaviController(
    private val dataModel: UiDataModel,
    private val core: ApplicationCore
) : RemoteControlHandler {

    /**
     * For remote control
     */
    private var selectedItemIndex: Int = 0

    private val mediaSource = ArrayList<PlayableMedia>()

    private var currentFolderScanning: CancelableExecution<List<PlayableMedia>>? = null

    // TODO set next episode on back, scroll video via remote, ios app?
    private val typeToComparator: Map<SortType, Comparator<PlayableMedia>> = mapOf(
        SortType.BY_DURATION to Comparator { o1, o2 ->
            o1.duration.value.compareTo(o2.duration.value)
        },
        SortType.BY_NAME to Comparator { o1, o2 ->
            o1.name().compareTo(o2.name())
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
            dataModel.media.setAll(getFilteredMedia(mediaSource))
        }
    }

    fun play(media: PlayableMedia) = UiUtil.inNormalThread {
        core.mediaService.play(media)
    }

    fun deleteEntry(media: PlayableMedia) {
        core.cacheService.deleteEntry(media)
    }

    fun onFolderSelect(path: String, forceRefresh: Boolean = false) {
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

                core.folderScanService.getMediaInFolder(path, currentFolderScanning!!)
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
        onFolderSelect(dataModel.selectedFolder.get(), true)
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

    override fun toFullScreen() = throw NotImplementedError()
    override fun back() = throw NotImplementedError()

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

    override fun playSelected() = inUiThread {
        core.mediaService.play(dataModel.media[selectedItemIndex])
    }

    override fun watchLastMedia() = core.playLast()

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