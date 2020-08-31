package cz.encircled.eplayer.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.AppSettings
import cz.encircled.eplayer.model.PlayableMedia
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList

private data class MediaWrapper(val media: ArrayList<PlayableMedia> = ArrayList())

object IOUtil {

    private val log = LogManager.getLogger()

    private val pathToSettings = ApplicationCore.APP_DOCUMENTS_ROOT + "eplayer.properties.json"
    private val quickNaviPath = ApplicationCore.APP_DOCUMENTS_ROOT + "quicknavi2.json"

    private var BD_ONE_KO = BigDecimal(1024L)
    var BD_ONE_MO: BigDecimal = BD_ONE_KO.multiply(BD_ONE_KO)
    var BD_ONE_GO: BigDecimal = BD_ONE_MO.multiply(BD_ONE_KO)

    val mapper: ObjectMapper = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(KotlinModule(nullIsSameAsDefault = true))
            .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(PlayableMedia::class.java)
                    .build())

    @Throws(IOException::class)
    fun getPlayableJson(): List<PlayableMedia> {
        createIfMissing(quickNaviPath, false)
        return mapper.readValue(Files.readAllBytes(Paths.get(quickNaviPath)), MediaWrapper::class.java).media
    }


    @JvmStatic
    @Throws(IOException::class)
    fun savePlayable(settings: Map<String, PlayableMedia>) {
        Files.write(Paths.get(quickNaviPath), mapper.writeValueAsBytes(MediaWrapper(ArrayList(settings.values))))
    }


    @JvmStatic
    @Throws(IOException::class)
    fun getSettings(): AppSettings {
        return mapper.readValue(Files.readAllBytes(Paths.get(pathToSettings)), AppSettings::class.java)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveSettings(settings: AppSettings) {
        Files.write(Paths.get(pathToSettings), mapper.writeValueAsBytes(settings))
    }

    /**
     * @return true if file was created
     */
    @JvmStatic
    fun createIfMissing(pathTo: String, isDirectory: Boolean): Boolean {
        val path = Paths.get(pathTo)
        try {
            if (!Files.exists(path)) {
                if (isDirectory) {
                    Files.createDirectories(path)
                    log.debug("Directory {} not exists and was created", pathTo)
                } else {
                    Files.createFile(path)
                    File(path.toUri()).writeText("{}")
                    log.debug("File {} not exists and was created", pathTo)
                }
                return true
            }
        } catch (io: IOException) {
            throw RuntimeException("Failed to create required file $pathTo", io)
        }
        return false
    }

    fun getFilesInFolder(path: String): List<File> {
        val source = File(path)
        require(source.exists() && source.isDirectory)
        return getFilesInFolderInternal(source, ArrayList())
    }

    private fun getFilesInFolderInternal(source: File, files: MutableList<File>): List<File> {
        val filesInFolder = source.listFiles()
        if (filesInFolder != null) {
            for (file in filesInFolder) {
                if (file.isFile) files.add(file) else getFilesInFolderInternal(file, files)
            }
        }
        return files
    }

    fun byteCountToDisplaySize(size: Long): String {
        return when {
            size / FileUtils.ONE_GB > 0 -> {
                BigDecimal(size).divide(BD_ONE_GO, 2, BigDecimal.ROUND_FLOOR).toString() + " Gb"
            }
            size / FileUtils.ONE_MB > 0 -> {
                BigDecimal(size).divide(BD_ONE_MO, BigDecimal.ROUND_FLOOR).toString() + " Mb"
            }
            size / FileUtils.ONE_KB > 0 -> {
                BigDecimal(size).divide(BD_ONE_KO, BigDecimal.ROUND_FLOOR).toString() + " Kb"
            }
            else -> "$size b"
        }
    }

}