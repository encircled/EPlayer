package cz.encircled.eplayer.core

import cz.encircled.eplayer.model.MediaFile
import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * @author Encircled on 22/09/2014.
 */
class SeriesFinder {

    private val log = LogManager.getLogger()
    private val seriesPattern = Regex("s[\\d]{1,2}.?e[\\d]{1,2}")

    fun isSeries(name: String): Boolean = seriesPattern.find(name.toLowerCase()) != null

    fun seriesName(media: MediaFile): String = seriesName(File(media.path))

    /**
     * Find series name in the full name
     */
    fun seriesName(media: File): String {
        val i = seriesPattern.find(media.name.toLowerCase())?.range?.first ?: 0
        return if (i > 0) media.name.substring(0, i) else media.parent
    }

    /**
     * Find all series in a folder, return list of paths
     */
    fun findSeriesForName(folder: String, seriesName: String): List<String> =
        File(folder).walk().maxDepth(1)
            .filter { seriesName(it) == seriesName }
            .map { it.path }
            .toList()

}