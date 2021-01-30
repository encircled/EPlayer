package cz.encircled.eplayer.common

import java.lang.Runnable
import java.util.*

/**
 * @author Encircled on 28/09/2014.
 */
class PostponeTimer(delay: Long? = null, private val runnable: Runnable) {

    private var timer: Timer? = null

    init {
        delay?.let { postpone(it) }
    }

    fun postpone(delay: Long) {
        reset(delay)
    }

    fun cancel() {
        if (timer != null) timer!!.cancel()
    }

    private fun reset(delay: Long) {
        cancel()
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                runnable.run()
            }
        }, delay)
    }
}