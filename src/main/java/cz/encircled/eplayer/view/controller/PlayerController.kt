package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.MediaBookmark
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.view.UiUtil.inNormalThread

interface PlayerController {

    var volume: Int
    var time: Long

    fun togglePlaying()

    fun addBookmark()
    fun removeBookmark(bookmark: MediaBookmark)

}

class PlayerControllerImpl(val core: ApplicationCore) : PlayerController {

    override var volume: Int
        get() = core.mediaService.volume
        set(value) = inNormalThread {
            core.mediaService.volume = value
        }

    override var time: Long
        get() = throw UnsupportedOperationException()
        set(value) = inNormalThread {
            core.mediaService.setTime(value)
        }

    override fun togglePlaying() {
        core.mediaService.toggle()
    }

    override fun addBookmark() {
        val current = core.mediaService.currentMedia()
        if (current is SingleMedia) {
            current.bookmarks.add(MediaBookmark(current.time.get()))
        }
    }

    override fun removeBookmark(bookmark: MediaBookmark) {
        val current = core.mediaService.currentMedia()
        if (current is SingleMedia) {
            current.bookmarks.remove(bookmark)
        }
    }
}