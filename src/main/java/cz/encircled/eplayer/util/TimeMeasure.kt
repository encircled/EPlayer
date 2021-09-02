package cz.encircled.eplayer.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object TimeMeasure {

    val log: Logger = LogManager.getLogger()

    inline fun <T> measure(name: String, crossinline callback: () -> T): T {
        val start = System.currentTimeMillis()

        try {
            log.info("[$name] execution started")
            return callback()
        } finally {
            val took = System.currentTimeMillis() - start
            log.info("[$name] execution took $took ms")
        }

    }

}