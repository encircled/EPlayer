package cz.encircled.eplayer.service.event

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.service.Cancelable
import cz.encircled.eplayer.view.UiUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
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

    fun fire(arg: A, bypassThrottling: Boolean = false) = Thread {
        if (!bypassThrottling && System.currentTimeMillis() - lastExecution < minDelay) return@Thread
        lastExecution = System.currentTimeMillis()
        try {
            doFire(arg)
        } catch (e: Exception) {
            log.error("Event listener error", e)
        }
    }.start()

    private fun doFire(arg: A) {
        updateListenersCount()
        val countDown = CountDownLatch(lastListenersCount)

        val listenersCopy = ArrayList(listeners)
        val uiListenersCopy = ArrayList(uiListeners)

        log.ifVerbose("Fire event $name [$arg], $lastListenersCount listeners")

        listenersCopy.forEach {
            val start = System.currentTimeMillis()
            try {
                it.invoke(arg)
            } finally {
                val elapsed = System.currentTimeMillis() - start
                if (elapsed > 10) log.ifVerbose("Event $name listener took $elapsed ms")
                countDown.countDown()
            }
        }

        uiListenersCopy.forEach {
            UiUtil.inUiThread {
                try {
                    val start = System.currentTimeMillis()
                    it.invoke(arg)
                    val elapsed = System.currentTimeMillis() - start
                    if (verbose && elapsed > 10) {
                        log.debug("Event $name listener took $elapsed")
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

    fun listen(listener: (A) -> Unit): Cancelable {
        listeners.add(listener)
        return Cancelable {
            listeners.remove(listener)
        }
    }

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
        val subtitleChanged = Event<MediaCharacteristic<GenericTrackDescription>>("subtitleChanged")

        /**
         * New audio track selected
         */
        val audioTrackChanged = Event<MediaCharacteristic<GenericTrackDescription>>("audioChanged")

        // True if playing
        val playingChanged = Event<OptionalMediaCharacteristic<Boolean>>("playingChanged")

        val mediaDurationChange = Event<MediaCharacteristic<Long>>("mediaDurationChange")

        val screenshotAcquired = Event<MediaCharacteristic<String>>("screenshotAcquired")

        val metadataAcquired = Event<MediaCharacteristic<Map<String, String>>>("screenshotAcquired")

        val volumeChanged = Event<Int>("volumeChanged")

        /* SETTINGS */

        val audioPassThroughChange = Event<Boolean>("audioPassThroughChange")

    }

    private fun Logger.ifVerbose(msg: String) {
        if (verbose) debug(msg)
    }

}
