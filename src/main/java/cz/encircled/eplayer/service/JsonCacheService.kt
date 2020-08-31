package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.*
import cz.encircled.eplayer.util.IOUtil
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author Encircled on 9.6.2014.
 */
class JsonCacheService : CacheService {

    private lateinit var cache: MutableMap<String, PlayableMedia>
    private lateinit var core: ApplicationCore

    override fun delayedInit(core: ApplicationCore) {
        this.core = core
        val start = System.currentTimeMillis()
        log.trace("JsonCacheService init start")
        try {
            cache = IOUtil.getPlayableJson()
                    .associateBy { if (it is MediaSeries) it.name else it.path }
                    .toMutableMap()
        } catch (e: Exception) {
            cache = HashMap()
            log.error("Failed to read cache data. Message: {}", e.message)
//           TODO guiUtil.showMessage(msgQnFileIoFail.ln(), errorTitle.ln());
        }
        log.trace("JsonCacheService init complete in {} ms", System.currentTimeMillis() - start)
    }

    override fun createIfAbsent(path: String): PlayableMedia {
        val mediaFile = MediaFile(path)

        return if (core.seriesFinder.isSeries(mediaFile.name)) {
            getOrCreateSeries(mediaFile, path)
        } else {
            cache.getOrPut(path) {
                SingleMedia(path, mediaFile, 0, 0)
            }
        }
    }

    private fun getOrCreateSeries(mediaFile: MediaFile, path: String): PlayableMedia {
        val seriesName = core.seriesFinder.seriesName(mediaFile.name)
        val single = SingleMedia(path, mediaFile, 0, 0)

        if (cache.containsKey(seriesName)) {
            (cache.getValue(seriesName) as MediaSeries).series.add(single)
        } else {
            val allSeries = core.seriesFinder.findSeriesForName(File(mediaFile.path).parent, seriesName)
                    .map { SingleMedia(it, MediaFile(it), 0, 0) }

            cache[seriesName] = MediaSeries(seriesName, path, 0, ArrayList(allSeries))
        }

        return cache.getValue(seriesName)
    }

    override fun updateEntry(media: PlayableMedia, time: Long): PlayableMedia {
        val path = media.mediaFile().path
        val p = cache[path] ?: (cache[core.seriesFinder.seriesName(media.mediaFile().name)] as MediaSeries).current()
        log.debug("Update cache entry {}, time {}", p, time)

        (p as SingleMedia).apply {
            this.time = time
            this.watchDate = Date().time
        }

        return p
    }

    override fun deleteEntry(id: String): PlayableMedia? = cache.remove(id)

    override fun getCache(): List<PlayableMedia> {
        return ArrayList(cache.values)
    }

    override fun getLastByWatchDate(): PlayableMedia? {
        val p = getCache().maxWithOrNull(Comparator.comparingLong(PlayableMedia::watchDate))
        log.debug("Last played: {}", p)
        return p
    }

    /**
     * Save playable map to file in JSON format
     */
    @Synchronized
    override fun save() {
        log.debug("Save json cache")
        try {
            IOUtil.savePlayable(cache)
            log.debug("Json successfully saved")
        } catch (e: IOException) {
            log.error("Failed to save playable, msg {}", e)
        }
    }

    companion object {
        private val log = LogManager.getLogger()
    }
}