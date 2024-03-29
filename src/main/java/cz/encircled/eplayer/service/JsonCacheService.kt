package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.util.TimeMeasure.measure
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException

/**
 * @author Encircled on 9.6.2014.
 */
class JsonCacheService(val core: ApplicationCore) : CacheService {

    private lateinit var cache: MutableMap<String, PlayableMedia>

    init {
        measure("JsonCacheService init") {
            try {
                cache = core.ioUtil.getPlayableJson()
                    .map {
                        core.metaInfoService.fetchMetadataAsync(it)
                    }
                    .associateBy { it.path }
                    .toMutableMap()
            } catch (e: Exception) {
                cache = HashMap()
                log.error("Failed to read cache data", e)
                core.appView.showUserMessage(Localization.msgQnFileIoFail.ln())
            }
        }

        measure("JsonCacheService init listeners") {
            addEventListeners()
        }
    }

    private fun addEventListeners() {
        Event.mediaTimeChange.listen {
            cache.getValue(it.playableMedia.path).time.set(it.characteristic)
        }
        Event.mediaDurationChange.listen {
            cache.getValue(it.playableMedia.path).duration.set(it.characteristic)
        }

        Event.subtitleChanged.listen {
            if (it.changedByUser) cache.getValue(it.playableMedia.path).preferredSubtitle = it.characteristic
        }

        Event.audioTrackChanged.listen {
            if (it.changedByUser) cache.getValue(it.playableMedia.path).preferredAudio = it.characteristic
        }

        Event.metadataAcquired.listen {
            // TODO for series
            val media = cache[it.playableMedia.path]
            if (media is SingleMedia) {
                media.metaCreationDate = it.characteristic.getOrDefault("creation_time", "")
            }
        }
    }

    override fun getOrNull(path: String): PlayableMedia? {
        val mediaFile = MediaFile(path)

        return if (core.seriesFinder.isSeries(mediaFile.name)) {
            cache[core.seriesFinder.seriesName(mediaFile)]
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
                createNewMedia(path)
            }
        }
    }

    override fun createIfAbsent(media: PlayableMedia): PlayableMedia = cache.getOrPut(media.path) { media }

    override fun deleteEntry(media: PlayableMedia): PlayableMedia = cache.remove(media.path)!!

    override fun getCachedMedia(): List<PlayableMedia> = ArrayList(cache.values)

    override fun getPlayedMedia(): List<PlayableMedia> = getCachedMedia().filter { it.isPlayed() }

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
            core.ioUtil.savePlayable(cache)
            log.debug("Json successfully saved")
        } catch (e: IOException) {
            log.error("Failed to save playable, msg {}", e)
        }
    }

    private fun getOrCreateSeries(mediaFile: MediaFile, path: String): PlayableMedia {
        val seriesName = core.seriesFinder.seriesName(mediaFile)
        val seriesPath = File(mediaFile.path).parent + File.separator + seriesName

        if (cache.containsKey(seriesPath)) {
            val mediaSeries = cache.getValue(seriesPath) as MediaSeries
            if (!mediaSeries.series.map { it.path }.contains(path)) {
                mediaSeries.series.add(createNewMedia(path))
            }
        } else {
            val allSeries = core.seriesFinder.findSeriesForName(File(mediaFile.path).parent, seriesName)
                .map { createNewMedia(it) }


            cache[seriesPath] = MediaSeries(seriesName, seriesPath, ArrayList(allSeries))
        }

        return cache.getValue(seriesPath)
    }

    private fun createNewMedia(path: String): SingleMedia {
        log.debug("createNewMedia $path")
        return core.metaInfoService.fetchMetadataAsync(SingleMedia(path))
    }

    companion object {
        private val log = LogManager.getLogger()
    }
}