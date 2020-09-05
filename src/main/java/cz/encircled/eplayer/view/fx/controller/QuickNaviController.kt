package cz.encircled.eplayer.view.fx.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.view.fx.UiDataModel
import cz.encircled.eplayer.view.fx.addNewValueListener
import cz.encircled.eplayer.view.fx.fxThread
import java.util.regex.Pattern

/**
 * @author encir on 05-Sep-20.
 */
class QuickNaviController(val dataModel: UiDataModel, val core: ApplicationCore) : RemoteControlHandler {

    /**
     * For remote control
     */
    private var selectedItemIndex: Int = 0

    init {
        Event.contextInitialized.listenFxThread {
            dataModel.media.addAll(core.cacheService.getCached())
        }

        dataModel.filter.addNewValueListener {
            fxThread {
                dataModel.media.setAll(getFilteredMedia(dataModel.media, it))
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
        dataModel.media.clear()
        core.folderScanService.getMediaInFolder(path) {
            dataModel.media.addAll(it)
        }
    }

    fun onQuickNaviSelect() {
        dataModel.media.clear()
        dataModel.media.addAll(core.cacheService.getCached())
    }

    fun forceRefresh() = fxThread {
        // TODO check?
        onQuickNaviSelect()
    }

    fun addTab(path: String) {
        dataModel.foldersToScan.add(path)
        core.settings.addFolderToScan(path)
    }

    override fun toFullScreen() = throw NotImplementedError()
    override fun back() = throw NotImplementedError()

    override fun goToNextMedia() = fxThread {
        if (selectedItemIndex < dataModel.media.size - 1) {
            selectedItemIndex++
        } else {
            selectedItemIndex = 0
        }
        dataModel.selectedMedia.set(dataModel.media[selectedItemIndex])
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

    override fun watchLastMedia() = throw NotImplementedError()
    override fun playPause() = throw NotImplementedError()

    private fun getFilteredMedia(mediaFiles: List<PlayableMedia>, filter: String): List<PlayableMedia> =
            if (filter.isNotBlank()) {
                val p = Pattern.compile("(?i).*" + filter.replace(" ".toRegex(), ".*") + ".*")
                val m = p.matcher("")
                mediaFiles.filter { m.reset(it.mediaFile().name).matches() }
            } else {
                mediaFiles
            }

}