package cz.encircled.eplayer.view.fx.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.fx.*
import java.util.regex.Pattern

/**
 * @author encir on 05-Sep-20.
 */
class QuickNaviController(
        private val fxView: FxView,
        private val dataModel: UiDataModel,
        private val core: ApplicationCore) : RemoteControlHandler {

    /**
     * For remote control
     */
    private var selectedItemIndex: Int = 0

    private val mediaSource = ArrayList<PlayableMedia>()

    init {
        Event.contextInitialized.listenFxThread {
            dataModel.foldersToScan.setAll(listOf(QUICK_NAVI) + core.settings.foldersToScan)
            onQuickNaviSelect()

            fxView.sceneChangeProperty.addNewValueListener {
                if (FxView.QUICK_NAVI_SCREEN == it) forceRefresh()
            }
        }

        dataModel.filter.addNewValueListener {
            fxThread {
                dataModel.media.setAll(getFilteredMedia(mediaSource, it))
            }
        }
    }

    fun play(media: PlayableMedia) {
        core.mediaService.play(media)
    }

    fun deleteEntry(media: PlayableMedia) {
        core.cacheService.deleteEntry(media)
    }

    fun onFolderSelect(path: String) {
        if (path == QUICK_NAVI) {
            onQuickNaviSelect()
        } else {
            dataModel.selectedFolder.set(path)
            dataModel.media.clear()
            mediaSource.clear()

            core.folderScanService.getMediaInFolder(path) {
                mediaSource.addAll(it)
                dataModel.media.addAll(getFilteredMedia(it))
            }
        }
    }

    fun onQuickNaviSelect() {
        val start = System.currentTimeMillis()
        println("ON QUICK NAVI")
        dataModel.selectedFolder.set(QUICK_NAVI)
        mediaSource.clear()
        mediaSource.addAll(core.cacheService.getCached())
        dataModel.media.setAll(getFilteredMedia(mediaSource))
        println("ON QUICK NAVI: ${System.currentTimeMillis() - start}")
    }

    fun forceRefresh() = fxThread {
        dataModel.media.clear()
        onQuickNaviSelect()
    }

    fun addTab(path: String) {
        dataModel.foldersToScan.add(path)
        core.settings.addFolderToScan(path)
    }

    fun removeTab(path: String) {
        if (dataModel.foldersToScan.remove(path)) {
            if (dataModel.selectedFolder.get() == path) {
                onQuickNaviSelect()
            }
            core.settings.removeFolderToScan(path)
        }
    }

    override fun toFullScreen() = throw NotImplementedError()
    override fun back() = throw NotImplementedError()

    override fun goToNextMedia() = fxThread {
        selectedItemIndex = nextIndex(selectedItemIndex, dataModel.media)

        if (selectedItemIndex == 0) {
            val currentFolder = dataModel.foldersToScan.indexOf(dataModel.selectedFolder.get())
            onFolderSelect(dataModel.foldersToScan[nextIndex(currentFolder, dataModel.foldersToScan)])
        }

        if (dataModel.media.isNotEmpty()) dataModel.selectedMedia.set(dataModel.media[selectedItemIndex])
    }

    override fun goToPrevMedia() = fxThread {
        if (selectedItemIndex > 0) {
            selectedItemIndex--
        } else {
            selectedItemIndex = dataModel.media.size - 1
        }
        dataModel.selectedMedia.set(dataModel.media[selectedItemIndex])
    }

    override fun playSelected() = fxThread {
        core.mediaService.play(dataModel.media[selectedItemIndex])
    }

    override fun watchLastMedia() = core.playLast()

    override fun playPause() = throw NotImplementedError()

    private fun getFilteredMedia(mediaFiles: List<PlayableMedia>, filter: String = dataModel.filter.get()): List<PlayableMedia> =
            if (filter.isNotBlank()) {
                val p = Pattern.compile("(?i).*" + filter.replace(" ".toRegex(), ".*") + ".*")
                val m = p.matcher("")
                mediaFiles.filter { m.reset(it.mediaFile().name).matches() }
            } else {
                mediaFiles
            }

    private fun nextIndex(current: Int, max: Collection<*>): Int = if (current < max.size - 1) current + 1 else 0

}