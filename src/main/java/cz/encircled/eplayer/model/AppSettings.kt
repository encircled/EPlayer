package cz.encircled.eplayer.model

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event


/**
 * @author encir on 29-Aug-20.
 */
data class AppSettings(
    val language: String = "en",

    var fcOpenLocation: String? = null,

    var audioPassThrough: Boolean = false,

    val maxVolume: Int = 150,

    var lastVolume: Int = 100,

    val foldersToScan: MutableList<String> = mutableListOf()
) {

    @Transient
    lateinit var core: ApplicationCore

    init {
        Event.volumeChanged.listen { lastVolume(it) }
    }

    fun setOpenLocation(path: String) {
        fcOpenLocation = path
        core.ioUtil.saveSettings(this)
    }

    fun lastVolume(volume: Int) {
        lastVolume = volume
        core.ioUtil.saveSettings(this)
    }

    fun addFolderToScan(path: String) {
        foldersToScan.add(path)
        core.ioUtil.saveSettings(this)
    }

    fun audioPassThrough(value: Boolean) {
        audioPassThrough = value
        core.ioUtil.saveSettings(this)
    }

    fun removeFolderToScan(path: String) {
        if (foldersToScan.remove(path)) {
            core.ioUtil.saveSettings(this)
        }
    }

}
