package cz.encircled.eplayer.core

import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.CancelableExecution
import cz.encircled.eplayer.service.FolderScanService
import cz.encircled.eplayer.view.UiUtil
import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * @author Encircled on 20/09/2014.
 */
class OnDemandFolderScanner(private val core: ApplicationCore) : FolderScanService {

    private val log = LogManager.getLogger()
    private val supportedFormats = setOf("avi", "mkv", "mp3", "mp4", "flv", "wav", "wmv", "mov")

    override fun getMediaInFolder(path: String, callback: CancelableExecution<List<PlayableMedia>>) {
        UiUtil.inNormalThread {
            log.debug("OnDemandFolderScanner: start scanning {}", path)
            val start = System.currentTimeMillis()
            val series = HashSet<String>()

            File(path).walk().maxDepth(3)
                .filter { it.isFile && supportedFormats.contains(it.extension.toLowerCase()) && it.canRead() }
                .chunked(50)
                .forEach { chunk ->
                    if (callback.isCancelled) {
                        return@inNormalThread
                    }
                    callback.invoke(chunk.mapNotNull {
                        playableForFile(it, series)
                    })
                }

            log.debug("Folder {} scan complete in {} ms", path, System.currentTimeMillis() - start)
        }
    }

    private fun playableForFile(it: File, series: MutableSet<String>): PlayableMedia? {
        val media = core.cacheService.createIfAbsent(it.path)
        return if (media is MediaSeries) {
            if (series.add(core.seriesFinder.seriesName(media.mediaFile().name))) media else null
        } else media
    }

}
