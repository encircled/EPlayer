package cz.encircled.eplayer.model

import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.IOUtil


/**
 * @author encir on 29-Aug-20.
 */
data class AppSettings(
    val language: String = "en",

    var fcOpenLocation: String?,

    var audioPassThrough: Boolean = false,

    val maxVolume: Int = 150,

    var lastVolume: Int = 100,

    val foldersToScan: MutableList<String>
) {

    init {
        Event.volumeChanged.listen { lastVolume(it) }
    }

    fun setOpenLocation(path: String) {
        fcOpenLocation = path
        IOUtil.saveSettings(this)
    }

    fun lastVolume(volume: Int) {
        lastVolume = volume
        IOUtil.saveSettings(this)
    }

    fun addFolderToScan(path: String) {
        foldersToScan.add(path)
        IOUtil.saveSettings(this)
    }

    fun audioPassThrough(value: Boolean) {
        audioPassThrough = value
        IOUtil.saveSettings(this)
    }

    fun removeFolderToScan(path: String) {
        if (foldersToScan.remove(path)) {
            IOUtil.saveSettings(this)
        }
    }

}
