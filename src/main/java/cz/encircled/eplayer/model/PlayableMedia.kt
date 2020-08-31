package cz.encircled.eplayer.model

import com.fasterxml.jackson.annotation.JsonIgnore
import cz.encircled.eplayer.util.DateUtil
import cz.encircled.eplayer.util.IOUtil
import cz.encircled.eplayer.util.StringUtil
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author encir on 29-Aug-20.
 */
interface PlayableMedia {

    val formattedExtension: String
        get() = mediaFile().extension

    val pathToScreenshot: String
        get() = mediaFile().pathToScreenshot

    val formattedTitle: String
        get() = mediaFile().name

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
        override val path: String,
        override var watchDate: Long = 0,
        val series: ArrayList<SingleMedia>
) : PlayableMedia {

    init {
        series.sortBy { it.path }
    }

    override var time: Long = 0
        get() = series[indexOfCurrent()].time

    override val formattedTitle: String
        get() = name

    override fun mediaFile(): MediaFile {
        val index = indexOfCurrent()
        return series[index].mediaFile()
    }

    fun current(): SingleMedia = series[indexOfCurrent()]

    private fun indexOfCurrent(): Int {
        val i = series.indexOfFirst { it.time > 0 }
        return if (i == -1) 0 else i
//        if (series[i].mediaFile().series[i].time)
    }

}
