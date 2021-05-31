package cz.encircled.eplayer.model

import com.google.gson.annotations.SerializedName
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.util.DateUtil
import cz.encircled.eplayer.util.StringUtil
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * @author encir on 29-Aug-20.
 */
abstract class PlayableMedia {

    abstract fun getId(): String

    @SerializedName("type")
    private val typeName: String = this.javaClass.name

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
        get() = StringUtil.msToTimeLabel(time.get())

    abstract var time: SimpleLongProperty

    abstract var duration: SimpleLongProperty

    abstract var preferredSubtitle: GenericTrackDescription?

    abstract var preferredAudio: GenericTrackDescription?

    abstract var watchDate: Long

    abstract val path: String

    abstract fun mediaFile(): MediaFile

    fun hasScreenshot(): Boolean = File(filePathToScreenshot).exists()

    abstract fun name(): String

    abstract fun isPlayed(): Boolean

}

private val log = LogManager.getLogger()

data class SingleMedia(
    override val path: String,
    override var time: SimpleLongProperty = SimpleLongProperty(0),
    override var duration: SimpleLongProperty = SimpleLongProperty(0),
    override var watchDate: Long = 0,
    override var preferredSubtitle: GenericTrackDescription? = null,
    override var preferredAudio: GenericTrackDescription? = null,
) : PlayableMedia() {

    private val mediaFile: MediaFile by lazy { // TODO is it needed at all?
        MediaFile(path)
    }

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

    override fun isPlayed(): Boolean = time.get() > 0
}

data class MediaSeries(
    val name: String,
    val series: MutableList<SingleMedia>
) : PlayableMedia() {

    @Transient
    private val creditsTime = 1000 * 60 * 2

    @Transient
    val currentEpisode: SimpleIntegerProperty = SimpleIntegerProperty()

    // For Gson
    constructor() : this("", arrayListOf())

    // Workaround for constructor not called by Gson
    fun doInit() {
        series.sortBy { it.path }

        var i = series.indexOfLast { it.time.get() > 0 }

        // Go to next episode if watch time is lesser than average credits time
        if (i >= 0 && i < series.size - 1 && series[i].duration.get() - series[i].time.get() < creditsTime) i++

        currentEpisode.set(max(i, 0))
    }

    override fun getId(): String = name

    override val path: String = ""

    override var watchDate: Long
        get() = current().watchDate
        set(value) {
            current().watchDate = value
        }

    override var time: SimpleLongProperty
        get() = current().time
        set(value) {
            current().time = value
        }

    override var duration: SimpleLongProperty
        get() = current().duration
        set(value) {
            current().duration = value
        }

    override var preferredSubtitle: GenericTrackDescription?
        get() = current().preferredSubtitle
        set(value) {
            series.forEach { it.preferredSubtitle = value }
        }

    override var preferredAudio: GenericTrackDescription?
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

    override fun isPlayed(): Boolean = series.any { it.isPlayed() }

}
