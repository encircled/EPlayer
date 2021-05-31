package cz.encircled.eplayer.service

import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.MediaCharacteristic
import cz.encircled.eplayer.view.AppView
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import kotlin.math.min

interface MetadataInfoService {

    fun <T : PlayableMedia> fetchMetadataAsync(media: T): T

}

class JavacvMetadataInfoService : MetadataInfoService {

    private val queue: MutableMap<PlayableMedia, Any?> = ConcurrentHashMap()

    init {
        val converter = Java2DFrameConverter()
        Thread {
            while (true) {
                if (queue.isNotEmpty()) {
                    val media = queue.keys.first()

                    if (isApplicable(media)) {
                        println("queue size: ${queue.size}")
                        try {
                            doFetchMetadata(media, converter)
                        } catch (e: FrameGrabber.Exception) {
                            // Happens when can't open the file
                        } catch (e: Exception) {
                            println("Failed for ${media.path}")
                            e.printStackTrace()
                        }
                    }

                    queue.remove(media)
                }
                Thread.sleep(100)
            }
        }.start()
    }

    private fun isApplicable(media: PlayableMedia) =
        media is SingleMedia && (!media.hasScreenshot() || media.duration.get() == 0L)

    private fun doFetchMetadata(media: PlayableMedia, converter: Java2DFrameConverter) {
        val g = FFmpegFrameGrabber(media.path)
        g.start()
        g.imageWidth = AppView.SCREENSHOT_WIDTH
        g.imageHeight = AppView.SCREENSHOT_HEIGHT

        media.duration.set(g.lengthInTime / 1000L)

        if (!media.hasScreenshot()) {
            g.frameNumber = min(360, g.lengthInVideoFrames / 2)

            val grabFrame = g.grabImage()
            val bufferedImage = converter.getBufferedImage(grabFrame)
            ImageIO.write(
                bufferedImage,
                "png",
                File(media.filePathToScreenshot)
            )
            Event.screenshotAcquired.fire(MediaCharacteristic(media, media.path))
        }
        g.stop()
    }

    override fun <T : PlayableMedia> fetchMetadataAsync(media: T): T {
        queue[media] = 0
        return media
    }

}
