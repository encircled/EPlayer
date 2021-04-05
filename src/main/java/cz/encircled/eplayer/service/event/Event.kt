package cz.encircled.eplayer.service.event

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.view.UiUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.ArrayList
import java.util.concurrent.CountDownLatch

/**
 * @author Encircled on 13/09/2014.
 */
data class Event<A>(val name: String, val minDelay: Long = 0, val verbose: Boolean = true) {

    private val log = LogManager.getLogger()

    private val listeners: MutableList<(A) -> Unit> = ArrayList()

    private val uiListeners: MutableList<(A) -> Unit> = ArrayList()

    private var lastExecution: Long = 0

    var lastListenersCount = -1

    fun fire(arg: A, bypassThrottling: Boolean = false) = UiUtil.inNormalThread {
        if (!bypassThrottling && System.currentTimeMillis() - lastExecution < minDelay) return@inNormalThread
        lastExecution = System.currentTimeMillis()
        doFire(arg)
    }

    private fun doFire(arg: A) {
        updateListenersCount()
        val countDown = CountDownLatch(lastListenersCount)

        log.ifVerbose("Fire event $name [$arg], $lastListenersCount listeners")

        listeners.forEach {
            val start = System.currentTimeMillis()
            try {
                it.invoke(arg)
            } finally {
                log.ifVerbose("Event $name listener took ${System.currentTimeMillis() - start} ms")
                countDown.countDown()
            }
        }

        uiListeners.forEach {
            UiUtil.inUiThread {
                try {
                    val start = System.currentTimeMillis()
                    it.invoke(arg)
                    if (verbose) {
                        log.debug("Event $name listener took ${System.currentTimeMillis() - start}")
                    }
                } finally {
                    countDown.countDown()
                }
            }
        }

        countDown.await()
        log.ifVerbose("Finished firing event $name")
    }

    private fun updateListenersCount() {
        val totalListenersCount = listeners.size + uiListeners.size
        if (lastListenersCount == -1) {
            lastListenersCount = totalListenersCount
        } else if (lastListenersCount < totalListenersCount) {
            log.warn("Count of listeners has changed for event $this: $lastListenersCount vs $totalListenersCount")
        }
        lastListenersCount = totalListenersCount
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
        val contextInitialized = Event<ApplicationCore>("contextInitialized")

        val mediaTimeChange = Event<MediaCharacteristic<Long>>("mediaTimeChange", 1000, false)

        val subtitlesUpdated = Event<List<GenericTrackDescription>>("subtitlesUpdated")

        val audioTracksUpdated = Event<List<GenericTrackDescription>>("audioTracksUpdated")

        /**
         * New subtitle selected
         */
        val subtitleChanged = Event<MediaCharacteristic<Int>>("subtitleChanged")

        /**
         * New audio track selected
         */
        val audioTrackChanged = Event<MediaCharacteristic<Int>>("audioChanged")

        // True if playing
        val playingChanged = Event<OptionalMediaCharacteristic<Boolean>>("playingChanged")

        val mediaDurationChange = Event<MediaCharacteristic<Long>>("mediaDurationChange")

        val screenshotAcquired = Event<MediaCharacteristic<String>>("screenshotAcquired")

        val volumeChanged = Event<Int>("volumeChanged")

        /* SETTINGS */

        val audioPassThroughChange = Event<Boolean>("audioPassThroughChange")

    }

    private fun Logger.ifVerbose(msg: String) {
        if (verbose) debug(msg)
    }

}
