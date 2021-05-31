package cz.encircled.eplayer.core

import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.CancelableExecution
import cz.encircled.eplayer.service.FolderScanService
import cz.encircled.eplayer.util.TimeTracker
import java.io.File

/**
 * @author Encircled on 20/09/2014.
 */
class OnDemandFolderScanner(private val core: ApplicationCore) : FolderScanService {

    private val supportedFormats = setOf("avi", "mkv", "mp3", "mp4", "flv", "wav", "wmv", "mov")

    override fun getMediaInFolder(path: String, callback: CancelableExecution<List<PlayableMedia>>) =
        TimeTracker.tracking("OnDemandFolderScanner - $path") {
            val series = HashSet<String>()

            File(path).walk().maxDepth(3)
                .filter { it.isFile && supportedFormats.contains(it.extension.toLowerCase()) && it.canRead() }
                .chunked(50)
                .forEach { chunk ->
                    if (callback.isCancelled) {
                        return@tracking
                    }
                    callback.invoke(chunk.mapNotNull {
                        playableForFile(it, series)
                    })
                }
        }

    private fun playableForFile(it: File, series: MutableSet<String>): PlayableMedia? {
        val media = core.cacheService.createIfAbsent(it.path)
        return if (media is MediaSeries) {
            if (series.add(media.name)) media else null
        } else media
    }

}
