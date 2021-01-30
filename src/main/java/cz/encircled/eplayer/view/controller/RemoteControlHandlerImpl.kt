package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.remote.RemoteControlHandlerWithDefaultDelegate
import cz.encircled.eplayer.view.Scenes

/**
 * @author encir on 26-Aug-20.
 */
class RemoteControlHandlerImpl(
    private val core: ApplicationCore,
    private val playerRemoteHandler: RemoteControlHandler,
) : RemoteControlHandlerWithDefaultDelegate() {

    override fun getRemoteControlDelegate(): RemoteControlHandler = playerRemoteHandler

    override fun back() = core.back()

    override fun toFullScreen() = core.appView.toggleFullScreen()

    override fun playPause() = core.mediaService.toggle()

    override fun goToNextMedia() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.playNext()
        } else {
            super.goToNextMedia()
        }
    }

    override fun goToPrevMedia() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.playPrevious()
        } else {
            super.goToPrevMedia()
        }
    }

}