package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.view.Scenes

/**
 * @author encir on 26-Aug-20.
 */
class RemoteControlHandlerImpl(
    private val core: ApplicationCore,
    private val delegate: RemoteControlHandler,
) : RemoteControlHandler by delegate {

    override fun back() = core.back()

    override fun toFullScreen() = core.appView.toggleFullScreen()

    override fun playPause() = core.mediaService.toggle()

    override fun goToNextMedia() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.playNext()
        } else {
            delegate.goToNextMedia()
        }
    }

    override fun goToPrevMedia() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.playPrevious()
        } else {
            delegate.goToPrevMedia()
        }
    }

    override fun forward() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.setTimePlus(3000)
        } else {
            delegate.forward()
        }
    }

    override fun backward() {
        if (core.appView.currentSceneProperty.get() == Scenes.PLAYER) {
            core.mediaService.setTimePlus(-3000)
        } else {
            delegate.backward()
        }
    }

    override fun volumeUp() {
        core.mediaService.volume = core.mediaService.volume + 5
    }

    override fun volumeDown() {
        core.mediaService.volume = core.mediaService.volume - 5
    }

}