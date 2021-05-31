package cz.encircled.eplayer.service

import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.OptionalMediaCharacteristic
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.Test

class EventTest {

    private val listeners = mutableListOf<Cancelable>()


    @AfterTest
    fun after() {
        listeners.forEach { it.cancel() }
    }

    @Test
    fun testRegistrationDuringInvocation() {
        val cd = CountDownLatch(4)
        val i = AtomicInteger(0)
        Event.playingChanged.testListen {
            val now = i.addAndGet(1)
            if (now < 3) {
                Event.playingChanged.testListen { }
            }
            cd.countDown()
        }
        Event.playingChanged.testListen {
            cd.countDown()
        }


        Event.playingChanged.testUiListen {
            val now = i.addAndGet(1)
            if (now < 3) {
                Event.playingChanged.testUiListen { }
            }
            cd.countDown()
        }
        Event.playingChanged.testUiListen {
            cd.countDown()
        }

        Event.playingChanged.fire(OptionalMediaCharacteristic(null, true))
        cd.await()
    }

    fun <A> Event<A>.testListen(listener: (A) -> Unit) {
        listeners.add(listen(listener))
    }

    fun <A> Event<A>.testUiListen(listener: (A) -> Unit) {
        listeners.add(listenUiThread(listener))
    }

}