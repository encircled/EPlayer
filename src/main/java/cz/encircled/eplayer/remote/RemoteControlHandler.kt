package cz.encircled.eplayer.remote

/**
 * @author encir on 26-Aug-20.
 */
interface RemoteControlHandler {

    fun toFullScreen() {}

    fun back() {}

    /**
     * Go to next media or series episode
     */
    fun goToNextMedia() {}

    /**
     * Go to prev media or series episode
     */
    fun goToPrevMedia() {}

    fun forward() {}

    fun backward() {}

    fun volumeUp() {}

    fun volumeDown() {}

    fun playSelected() {}

    fun watchLastMedia() {}

    fun playPause() {}

}
