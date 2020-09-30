package cz.encircled.eplayer.core

import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.FolderScanService
import cz.encircled.eplayer.service.JsonCacheService
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Encircled on 20/09/2014.
 */
class OnDemandFolderScanner(private val core: ApplicationCore) : FolderScanService {

    private val log = LogManager.getLogger()
    private val supportedFormats = setOf("avi", "mkv", "mp3", "mp4", "flv", "wav", "wmv", "mov")

    override fun getMediaInFolder(path: String, callback: (List<PlayableMedia>) -> Unit) {
        Thread {
            val start = System.currentTimeMillis()
            log.debug("OnDemandFolderScanner: start scanning {}", path)
            val series = mutableMapOf<String, MediaSeries>()

            File(path).walk().maxDepth(3)
                    .filter { it.isFile && supportedFormats.contains(it.extension) }
                    .chunked(20)
                    .forEach { chunk ->
                        callback(chunk.mapNotNull {
                            val mediaFile = MediaFile(it.path)
                            val singleMedia = SingleMedia(it.path, mediaFile)

                            val fromCache = core.cacheService.getOrNull(it.path)

                            if (fromCache != null) {
                                if (fromCache is MediaSeries) {
                                    if (series.containsKey(fromCache.name)) return@mapNotNull null

                                    series[fromCache.name] = fromCache
                                }
                                return@mapNotNull fromCache
                            }

                            if (core.seriesFinder.isSeries(mediaFile.name)) {
                                val seriesName = core.seriesFinder.seriesName(mediaFile.name)
                                if (series.containsKey(seriesName)) {
                                    series.getValue(seriesName).series.add(singleMedia)
                                    null
                                } else {
                                    val newSeries = MediaSeries(seriesName, ArrayList())
                                    newSeries.series.add(singleMedia)
                                    series[newSeries.getId()] = newSeries
                                    newSeries
                                }
                            } else {
                                singleMedia
                            }
                        })
                    }

            log.debug("Folder {} scan complete in {} ms", path, System.currentTimeMillis() - start)
        }.start()
    }

}
