package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.*
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.IOUtil
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author Encircled on 9.6.2014.
 */
class JsonCacheService(val core: ApplicationCore) : CacheService {

    private var cache: MutableMap<String, PlayableMedia>

    init {
        val start = System.currentTimeMillis()
        log.debug("JsonCacheService init start")
        try {
            cache = IOUtil.getPlayableJson()
                .map {
                    core.metaInfoService.fetchMetadataAsync(it)
                }
                .associateBy { it.getId() }
                .toMutableMap()
        } catch (e: Exception) {
            cache = HashMap()
            log.error("Failed to read cache data. Message: {}", e.message)
//           TODO guiUtil.showMessage(msgQnFileIoFail.ln(), errorTitle.ln());
        }

        addEventListeners()

        log.debug("JsonCacheService init complete in {} ms", System.currentTimeMillis() - start)
    }

    private fun addEventListeners() {
        Event.mediaTimeChange.listen {
            cache.getValue(it.playableMedia.getId()).time = it.characteristic
        }
        Event.mediaDurationChange.listen {
            cache.getValue(it.playableMedia.getId()).duration.set(it.characteristic)
        }

        Event.subtitleChanged.listen {
            cache.getValue(it.playableMedia.getId()).preferredSubtitle = it.characteristic
        }

        Event.audioTrackChanged.listen {
            cache.getValue(it.playableMedia.getId()).preferredAudio = it.characteristic
        }
    }

    override fun getOrNull(path: String): PlayableMedia? {
        val mediaFile = MediaFile(path)

        return if (core.seriesFinder.isSeries(mediaFile.name)) {
            cache[core.seriesFinder.seriesName(mediaFile.name)]
        } else {
            cache[path]
        }
    }

    override fun createIfAbsent(path: String): PlayableMedia {
        val mediaFile = MediaFile(path)

        return if (core.seriesFinder.isSeries(mediaFile.name)) {
            getOrCreateSeries(mediaFile, path)
        } else {
            cache.getOrPut(path) {
                createNewMedia(path, mediaFile)
            }
        }
    }

    override fun createIfAbsent(media: PlayableMedia): PlayableMedia = cache.getOrPut(media.getId()) { media }

    override fun deleteEntry(media: PlayableMedia): PlayableMedia = cache.remove(media.getId())!!

    override fun getCachedMedia(): List<PlayableMedia> = ArrayList(cache.values)

    override fun getPlayedMedia(): List<PlayableMedia> = getCachedMedia().filter { it.time > 0 }

    override fun lastByWatchDate(): PlayableMedia? {
        val p = getCachedMedia().maxWithOrNull(Comparator.comparingLong(PlayableMedia::watchDate))
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

    private fun getOrCreateSeries(mediaFile: MediaFile, path: String): PlayableMedia {
        val seriesName = core.seriesFinder.seriesName(mediaFile.name)

        if (cache.containsKey(seriesName)) {
            val mediaSeries = cache.getValue(seriesName) as MediaSeries
            if (!mediaSeries.series.map { it.path }.contains(path)) {
                mediaSeries.series.add(createNewMedia(path, mediaFile))
            }
        } else {
            val allSeries = core.seriesFinder.findSeriesForName(File(mediaFile.path).parent, seriesName)
                .map { createNewMedia(it) }

            cache[seriesName] = MediaSeries(seriesName, ArrayList(allSeries))
        }

        return cache.getValue(seriesName)
    }

    private fun createNewMedia(path: String, mediaFile: MediaFile = MediaFile(path)): SingleMedia {
        return core.metaInfoService.fetchMetadataAsync(SingleMedia(path, mediaFile))
    }

    companion object {
        private val log = LogManager.getLogger()
    }
}