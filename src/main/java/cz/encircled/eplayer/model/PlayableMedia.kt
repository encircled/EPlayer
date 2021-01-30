package cz.encircled.eplayer.model

import com.fasterxml.jackson.annotation.JsonIgnore
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.util.DateUtil
import cz.encircled.eplayer.util.IOUtil
import cz.encircled.eplayer.util.StringUtil
import javafx.beans.property.IntegerPropertyBase
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * @author encir on 29-Aug-20.
 */
interface PlayableMedia {

    fun getId(): String

    val formattedExtension: String
        get() = mediaFile().extension

    /**
     * URL format path (with file:/ prefix)
     */
    val pathToScreenshot: String
        get() = mediaFile().pathToScreenshot

    /**
     * File format path (without file:/ prefix)
     */
    val filePathToScreenshot: String
        get() = mediaFile().pathToScreenshot.substring(ApplicationCore.URL_FILE_PREFIX.length)

    val formattedWatchDate: String
        get() = DateUtil.daysBetweenLocalized(watchDate)

    val formattedCurrentTime: String
        get() = StringUtil.msToTimeLabel(time)

    val formattedSize: String
        get() = IOUtil.byteCountToDisplaySize(mediaFile().size)

    var time: Long

    var duration: SimpleLongProperty

    var preferredSubtitle: Int?

    var preferredAudio: Int?

    var watchDate: Long

    val path: String

    fun mediaFile(): MediaFile

    fun hasScreenshot(): Boolean = File(filePathToScreenshot).exists()

    fun name(): String

}

data class SingleMedia(
    override val path: String,
    @JsonIgnore
    private val mediaFile: MediaFile = MediaFile(path),
    override var time: Long = 0,
    override var duration: SimpleLongProperty = SimpleLongProperty(0),
    override var watchDate: Long = 0,
    override var preferredSubtitle: Int? = null,
    override var preferredAudio: Int? = null,
) : PlayableMedia {

    override fun getId(): String = path

    override fun mediaFile(): MediaFile = mediaFile

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SingleMedia) return false
        if (path != other.path) return false
        return true
    }

    override fun hashCode(): Int = path.hashCode()

    override fun name(): String = mediaFile.name
}

data class MediaSeries(
        val name: String,
        val series: ArrayList<SingleMedia>
) : PlayableMedia {

    @JsonIgnore
    var currentEpisode: IntegerPropertyBase = SimpleIntegerProperty()

    private val creditsTime = 1000 * 60;

    init {
        series.sortBy { it.path }

        var i = series.indexOfLast { it.time > 0 }

        // Go to next episode if watch time is lesser than average credits time
        if (i >= 0 && i < series.size - 1 && series[i].duration.get() - series[i].time < creditsTime) i++

        currentEpisode.set(max(i, 0))
    }

    override fun getId(): String = name

    override val path: String = ""

    override var watchDate: Long
        get() = current().watchDate
        set(value) {
            current().watchDate = value
        }

    override var time: Long
        get() = current().time
        set(value) {
            current().time = value
        }

    override var duration: SimpleLongProperty
        get() = current().duration
        set(value) {
            current().duration = value
        }

    override var preferredSubtitle: Int?
        get() = current().preferredSubtitle
        set(value) {
            series.forEach { it.preferredSubtitle = value }
        }

    override var preferredAudio: Int?
        get() = current().preferredAudio
        set(value) {
            series.forEach { it.preferredAudio = value }
        }

    override fun mediaFile(): MediaFile {
        return current().mediaFile()
    }

    override fun name(): String = current().name()

    fun toPrev() {
        currentEpisode.set(max(currentEpisode.get() - 1, 0))
    }

    fun toNext() {
        currentEpisode.set(min(currentEpisode.get() + 1, series.size - 1))
    }

    private fun current(): SingleMedia = series[currentEpisode.get()]

}
