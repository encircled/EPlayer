package cz.encircled.eplayer.remote

import com.sun.net.httpserver.HttpServer
import org.apache.logging.log4j.LogManager
import java.io.OutputStream
import java.net.InetSocketAddress

/**
 * @author encir on 26-Aug-20.
 */
class RemoteControlHttpServer(private val remoteControlHandler: RemoteControlHandler) {

    private val log = LogManager.getLogger()

    init {
        val server = HttpServer.create(InetSocketAddress(8001), 0)
        server.createContext("/") {
            val request = if (it.requestBody.available() > 0) {
                it.requestBody.bufferedReader().use { r -> r.readText() }
            } else {
                it.requestURI.query.replace("=".toRegex(), ":")
            }

            println("RECEIVED REQUEST $request")

            try {
                when (request) {
                    "cmd:fullScreen" -> remoteControlHandler.toFullScreen()
                    "cmd:nextToWatch" -> remoteControlHandler.goToNextMedia()
                    "cmd:prevToWatch" -> remoteControlHandler.goToPrevMedia()
                    "cmd:firstSuggested" -> remoteControlHandler.watchLastMedia()
                    "cmd:playPause" -> remoteControlHandler.playPause()
                    "cmd:back" -> remoteControlHandler.back()
                    "cmd:forward" -> remoteControlHandler.forward()
                    "cmd:backward" -> remoteControlHandler.backward()
                    "cmd:volumeUp" -> remoteControlHandler.volumeUp()
                    "cmd:volumeDown" -> remoteControlHandler.volumeDown()
                    "cmd:click" -> remoteControlHandler.playSelected()
                }
            } catch (e: Exception) {
                log.error("Command failed", e)
            }

            val response = "OK"
            println("RESPONSE: $response for REQUEST $request")
            it.sendResponseHeaders(200, response.length.toLong())
            val os: OutputStream = it.responseBody
            os.write(response.toByteArray())
            os.close()
        }
        server.executor = null
        server.start()
    }

}