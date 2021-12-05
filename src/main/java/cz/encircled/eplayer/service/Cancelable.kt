package cz.encircled.eplayer.service

import cz.encircled.fswing.components.Cancelable

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
