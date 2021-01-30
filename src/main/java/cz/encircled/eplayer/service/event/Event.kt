package cz.encircled.eplayer.service.event

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.view.UiUtil
import org.apache.logging.log4j.LogManager
import java.util.ArrayList

/**
 * @author Encircled on 13/09/2014.
 */
data class Event<A>(val name: String, val minDelay: Long = 0, val verbose: Boolean = true) {

    private val log = LogManager.getLogger()

    private val listeners: MutableList<(A) -> Unit> = ArrayList()

    private val uiListeners: MutableList<(A) -> Unit> = ArrayList()

    private var lastExecution: Long = 0

    var lastListenersCount = -1

    fun fire(arg: A, bypassThrottling: Boolean = false) {
        if (!bypassThrottling && System.currentTimeMillis() - lastExecution < minDelay) return

        lastExecution = System.currentTimeMillis()
        doFire(arg)
    }

    private fun doFire(arg: A) {
        val totalListenersCount = listeners.size + uiListeners.size
        if (lastListenersCount == -1) {
            lastListenersCount = totalListenersCount
        } else if (lastListenersCount < totalListenersCount) {
            log.warn("Count of listeners has changed for event $this: $lastListenersCount vs $totalListenersCount")
        }
        lastListenersCount = totalListenersCount

        if (verbose) {
            log.debug("Fire event $name, $totalListenersCount listeners")
        }
        Thread {
            val start = System.currentTimeMillis()
            listeners.forEach { it.invoke(arg) }
            if (verbose) {
                log.debug("Event $name: ${System.currentTimeMillis() - start}")
            }
        }.start()

        uiListeners.forEach {
            UiUtil.inUiThread {
                val start = System.currentTimeMillis()
                it.invoke(arg)
                if (verbose) {
                    log.debug("Event $name: ${System.currentTimeMillis() - start}")
                }
            }
        }
    }

    fun listen(listener: (A) -> Unit) = listeners.add(listener)

    fun listenUiThread(listener: (A) -> Unit): Cancelable {
        uiListeners.add(listener)
        return Cancelable {
            uiListeners.remove(listener)
        }
    }

    override fun toString(): String = "Event name [$name]"

    companion object {
        var contextInitialized = Event<ApplicationCore>("contextInitialized")

        var mediaTimeChange = Event<MediaCharacteristic<Long>>("mediaTimeChange", 1000, false)

        var subtitlesUpdated = Event<List<GenericTrackDescription>>("subtitlesUpdated")

        var audioTracksUpdated = Event<List<GenericTrackDescription>>("audioTracksUpdated")

        /**
         * New subtitle selected
         */
        var subtitleChanged = Event<MediaCharacteristic<Int>>("subtitleChanged")

        /**
         * New audio track selected
         */
        var audioTrackChanged = Event<MediaCharacteristic<Int>>("audioChanged")

        // True if playing
        var playingChanged = Event<Boolean>("playingChanged")

        var mediaDurationChange = Event<MediaCharacteristic<Long>>("mediaDurationChange")

        var screenshotAcquired = Event<MediaCharacteristic<String>>("screenshotAcquired", verbose = false)

    }

}