package cz.encircled.eplayer.service.event

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import javafx.application.Platform
import java.util.ArrayList

/**
 * @author Encircled on 13/09/2014.
 */
data class Event<A>(val name: String) {

    val verbose: Boolean = name != "mediaTimeChange"

    private val listeners: MutableList<(A) -> Unit> = ArrayList()

    private val fxListeners: MutableList<(A) -> Unit> = ArrayList()

    fun fire(arg: A) {
        Thread {
            listeners.forEach { it.invoke(arg) }
        }.start()

        fxListeners.forEach {
            Platform.runLater { it.invoke(arg) }
        }
    }

    fun listen(listener: (A) -> Unit) = listeners.add(listener)

    fun listenFxThread(listener: (A) -> Unit) = fxListeners.add(listener)

    override fun toString(): String {
        return "Event name [$name]"
    }

    companion object {
        var contextInitialized = Event<ApplicationCore>("contextInitialized")

        @JvmField
        var mediaTimeChange = Event<Long>("mediaTimeChange")
        var subtitlesUpdated = Event<List<GenericTrackDescription>>("subtitlesUpdated")
        var audioTracksUpdated = Event<List<GenericTrackDescription>>("audioTracksUpdated")

        // True if playing
        @JvmField
        var playingChanged = Event<Boolean>("playingChanged")

        @JvmField
        var mediaDurationChange = Event<Long>("mediaDurationChange")
    }

}