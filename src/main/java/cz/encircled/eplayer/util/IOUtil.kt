package cz.encircled.eplayer.util

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.AppSettings
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.util.TimeTracker.tracking
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths

data class MediaWrapper(val media: List<PlayableMedia>)

class IOUtil {

    private val log = LogManager.getLogger()

    var pathToSettings = ApplicationCore.APP_DOCUMENTS_ROOT + "eplayer.properties.json"
    var quickNaviPath = ApplicationCore.APP_DOCUMENTS_ROOT + "quicknavi2.json"

    private var BD_ONE_KB = BigDecimal(1024L)
    var BD_ONE_MB: BigDecimal = BD_ONE_KB.multiply(BD_ONE_KB)
    var BD_ONE_GB: BigDecimal = BD_ONE_MB.multiply(BD_ONE_KB)

    private val serializer: Serializer = GsonSerializer()

    @Throws(IOException::class)
    fun getPlayableJson(): List<PlayableMedia> {
        val allBytes = tracking("getPlayableJson") {
            createIfMissing(quickNaviPath, false)
            Files.readAllBytes(Paths.get(quickNaviPath))
        }

        return tracking("serializer.toObject") {
            val result = serializer.toObject(allBytes, MediaWrapper::class.java).media

            result.filterIsInstance<MediaSeries>().forEach { it.doInit() }
            result
        }
    }

    @Throws(IOException::class)
    fun savePlayable(playable: Map<String, PlayableMedia>) {
        log.debug("Saving playable to $quickNaviPath")
        try {
            File(quickNaviPath).writeText(serializer.fromObject(MediaWrapper(playable.values.toList())))
        } catch (e: Exception) {
            log.error("Failed to save playable", e)
        }
    }

    @Throws(IOException::class)
    fun getSettings(): AppSettings {
        return serializer.toObject(Files.readAllBytes(Paths.get(pathToSettings)), AppSettings::class.java)
            ?: AppSettings()
    }

    @Throws(IOException::class)
    fun saveSettings(settings: AppSettings) {
        log.debug("Saving settings to $quickNaviPath")
        File(pathToSettings).writeText(serializer.fromObject(settings))
    }

    /**
     * @return true if file was created
     */
    fun createIfMissing(pathTo: String, isDirectory: Boolean, defaultContent: String = ""): Boolean {
        val path = Paths.get(pathTo)
        try {
            if (!Files.exists(path)) {
                if (isDirectory) {
                    Files.createDirectories(path)
                    log.debug("Directory {} not exists and was created", pathTo)
                } else {
                    Files.createFile(path)
                    File(path.toUri()).writeText(defaultContent)
                    log.debug("File {} not exists and was created", pathTo)
                }
                return true
            }
        } catch (io: IOException) {
            throw RuntimeException("Failed to create required file $pathTo", io)
        }
        return false
    }

    fun byteCountToDisplaySize(size: Long): String = when {
        size / FileUtils.ONE_GB > 0 -> {
            BigDecimal(size).divide(BD_ONE_GB, 2, BigDecimal.ROUND_FLOOR).toString() + " Gb"
        }
        size / FileUtils.ONE_MB > 0 -> {
            BigDecimal(size).divide(BD_ONE_MB, BigDecimal.ROUND_FLOOR).toString() + " Mb"
        }
        size / FileUtils.ONE_KB > 0 -> {
            BigDecimal(size).divide(BD_ONE_KB, BigDecimal.ROUND_FLOOR).toString() + " Kb"
        }
        else -> "$size b"
    }

}