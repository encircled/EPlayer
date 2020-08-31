package cz.encircled.eplayer.view.fx

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.remote.RemoteControlHandler
import cz.encircled.eplayer.remote.RemoteControlHandlerWithDefaultDelegate

/**
 * @author encir on 26-Aug-20.
 */
class FxRemoteControlHandler(
        private val core: ApplicationCore,
        private val playerRemoteHandler: RemoteControlHandler,
) : RemoteControlHandlerWithDefaultDelegate() {

    override fun getRemoteControlDelegate(): RemoteControlHandler = playerRemoteHandler

    override fun back() = core.openQuickNavi()

    override fun toFullScreen() = core.appView.toggleFullScreen()

    override fun playPause() = core.mediaService.toggle()

    override fun watchLastMedia() = core.playLast()

}