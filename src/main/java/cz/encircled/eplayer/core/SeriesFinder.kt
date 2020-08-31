package cz.encircled.eplayer.core

import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * @author Encircled on 22/09/2014.
 */
class SeriesFinder {

    fun isSeries(name: String): Boolean = seriesPattern.find(name.toLowerCase()) != null

    /**
     * Find series name in the full name
     */
    fun seriesName(name: String): String {
        val i = seriesPattern.find(name.toLowerCase())?.range?.first ?: 0
        return name.substring(0, i)
    }

    /**
     * Find all series in a folder, return list of paths
     */
    fun findSeriesForName(folder: String, seriesName: String): List<String> =
            File(folder).walk().maxDepth(1)
                    .filter { seriesName(it.name) == seriesName }
                    .map { it.path }
                    .toList()

    companion object {
        private val log = LogManager.getLogger()
        private val seriesPattern = Regex("s[\\d]{1,2}.?e[\\d]{1,2}")
    }

}