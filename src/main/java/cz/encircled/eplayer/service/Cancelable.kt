package cz.encircled.eplayer.service

/**
 * Indicates that an object (like a listener or a subscription) can be cancelled
 */
fun interface Cancelable {

    fun cancel()

}

class CancelableExecution<T>(private var execution: ((T) -> Unit)?) : Cancelable {

    @Volatile
    var isCancelled = false

    override fun cancel() {
        isCancelled = true
        execution = null
    }

    fun invoke(param: T) {
        if (!isCancelled) {
            execution?.invoke(param)
        }
    }

}
