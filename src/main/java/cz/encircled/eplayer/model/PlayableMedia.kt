package cz.encircled.eplayer.model

import com.fasterxml.jackson.annotation.JsonIgnore
import cz.encircled.eplayer.util.DateUtil
import cz.encircled.eplayer.util.IOUtil
import cz.encircled.eplayer.util.StringUtil
import javafx.beans.property.IntegerPropertyBase
import javafx.beans.property.SimpleIntegerProperty
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author encir on 29-Aug-20.
 */
interface PlayableMedia {

    fun getId(): String

    val formattedExtension: String
        get() = mediaFile().extension

    val pathToScreenshot: String
        get() = mediaFile().pathToScreenshot

    val formattedWatchDate: String
        get() = DateUtil.daysBetweenLocalized(watchDate)

    val formattedCurrentTime: String
        get() = StringUtil.msToTimeLabel(time)

    val formattedSize: String
        get() = IOUtil.byteCountToDisplaySize(mediaFile().size)

    var time: Long

    var watchDate: Long

    val path: String

    fun mediaFile(): MediaFile

}

data class SingleMedia(
        override val path: String,
        @JsonIgnore
        private val mediaFile: MediaFile = MediaFile(path),
        override var time: Long = 0,
        override var watchDate: Long = 0,
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

}

data class MediaSeries(
        val name: String,
        val series: ArrayList<SingleMedia>
) : PlayableMedia {

    @JsonIgnore
    var currentEpisode: IntegerPropertyBase = SimpleIntegerProperty()

    init {
        series.sortBy { it.path }

        val i = series.indexOfFirst { it.time > 0 }
        currentEpisode.set(if (i == -1) 0 else i)
    }

    override fun getId(): String = name

    override val path: String = ""

    override var watchDate: Long
        get() = series[currentEpisode.get()].watchDate
        set(value) {
            series[currentEpisode.get()].watchDate = value
        }

    override var time: Long = 0
        get() = series[currentEpisode.get()].time

    override fun mediaFile(): MediaFile {
        return series[currentEpisode.get()].mediaFile()
    }

    fun current(): SingleMedia = series[currentEpisode.get()]

    fun toPrev() {
        currentEpisode.set((currentEpisode.get() - 1).coerceAtLeast(0))
    }

    fun toNext() {
        currentEpisode.set((currentEpisode.get() + 1).coerceAtMost(series.size - 1))
    }

}
