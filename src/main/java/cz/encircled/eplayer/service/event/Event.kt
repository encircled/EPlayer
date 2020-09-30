package cz.encircled.eplayer.service.event

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import javafx.application.Platform
import org.apache.logging.log4j.LogManager
import java.util.ArrayList

/**
 * @author Encircled on 13/09/2014.
 */
data class Event<A>(val name: String) {

    val verbose: Boolean = name != "mediaTimeChange"

    private val log = LogManager.getLogger()

    private val listeners: MutableList<(A) -> Unit> = ArrayList()

    private val fxListeners: MutableList<(A) -> Unit> = ArrayList()

    fun fire(arg: A) {
        if (verbose) {
            log.debug("Fire event $name")
        }
        Thread {
            val start = System.currentTimeMillis()
            listeners.forEach { it.invoke(arg) }
            if (verbose) {
                log.debug("Event $name: ${System.currentTimeMillis() - start}")
            }
        }.start()

        fxListeners.forEach {
            Platform.runLater {
                val start = System.currentTimeMillis()
                it.invoke(arg)
                if (verbose) {
                    log.debug("Event $name: ${System.currentTimeMillis() - start}")
                }
            }
        }
    }

    fun listen(listener: (A) -> Unit) = listeners.add(listener)

    fun listenFxThread(listener: (A) -> Unit) = fxListeners.add(listener)

    override fun toString(): String = "Event name [$name]"

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