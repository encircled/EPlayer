package cz.encircled.eplayer.remote

/**
 * @author encir on 26-Aug-20.
 */
interface RemoteControlHandler {

    fun toFullScreen()

    fun back()

    fun goToNextMedia()

    fun goToPrevMedia()

    fun playSelected()

    fun watchLastMedia()

    fun playPause()

}

abstract class RemoteControlHandlerWithDefaultDelegate : RemoteControlHandler {

    abstract fun getRemoteControlDelegate(): RemoteControlHandler

    override fun toFullScreen() = getRemoteControlDelegate().toFullScreen()

    override fun back() = getRemoteControlDelegate().back()

    override fun goToNextMedia() = getRemoteControlDelegate().goToNextMedia()

    override fun goToPrevMedia() = getRemoteControlDelegate().goToPrevMedia()

    override fun playSelected() = getRemoteControlDelegate().playSelected()

    override fun watchLastMedia() = getRemoteControlDelegate().watchLastMedia()

    override fun playPause() = getRemoteControlDelegate().playPause()

}