package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.MediaFile
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.event.Event
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.media.TrackInfo
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * @author Encircled on 9.6.2014.
 */
class VLCMediaService(private val core: ApplicationCore, val player: MediaPlayer) : MediaService {

    private val log = LogManager.getLogger()
    private var currentTime: Long = 0
    private var current: PlayableMedia? = null
    var timeToStart: Long? = null

    private val playerEventHandler = object : MediaPlayerEventAdapter() {

        override fun playing(mediaPlayer: MediaPlayer) = core.eventObserver.fire(Event.playingChanged, true)

        override fun paused(mediaPlayer: MediaPlayer) = core.eventObserver.fire(Event.playingChanged, false)

        override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
            log.debug("Media parsed - {}", current)

            val descriptionPrefix = { t: TrackInfo ->
                if (t.description() == null) {
                    "${t.language() ?: ""} "
                } else {
                    "${t.description()} [${t.language()}] "
                }
            }

            val textTracks = mediaPlayer.media().info().textTracks().map {
                GenericTrackDescription(it.id(), "${descriptionPrefix(it)} (${it.codecName()})")
            } + listOf(GenericTrackDescription(-1, "Disabled"))

            val audioTracks = mediaPlayer.media().info().audioTracks().map {
                val d = "${descriptionPrefix(it)} (${it.codecName()}, ${it.rate()} Hz, ${it.channels()}c)"
                GenericTrackDescription(it.id(), d)
            }

            core.eventObserver.fire(Event.subtitlesUpdated, textTracks)
            core.eventObserver.fire(Event.audioTracksUpdated, audioTracks)
        }

        override fun finished(mediaPlayer: MediaPlayer) {
            currentTime = 0L
            core.openQuickNavi()
        }

        override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
            currentTime = newTime
            core.eventObserver.fire(Event.mediaTimeChange, newTime)
        }

        override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
            core.eventObserver.fire(Event.mediaDurationChange, newLength)
        }

        override fun error(mediaPlayer: MediaPlayer) {
            log.error("Failed to open media {} ", current)
            // TODO guiUtil.showMessage(Localization.fileOpenFailed.ln(), Localization.errorTitle.ln());
            if (current != null) {
                core.cacheService.deleteEntry(current!!.path)
                current = null
            }
            core.appView.showQuickNavi()
        }
    }

    override fun releasePlayer() {
        stop()
        player.release()
    }

    // TODO change me?
    override fun updateCurrentMediaInCache() {
        current?.let {
            core.cacheService.updateEntry(it, currentTime)
            player.snapshots().save(File(ApplicationCore.getScreenshotLocation(it.mediaFile())), 336, 189)
        }
    }

    private fun play(media: PlayableMedia, time: Long) {
        val path = media.mediaFile().path
        log.debug("Play {}, start time is {}", path, time)

        val countDownLatch = CountDownLatch(1)

        log.debug("Show player requested")
        core.appView.showPlayer(countDownLatch)
        countDownLatch.await()
        log.debug("Show player done")
        current = media
        player.media().start(path) // --start-time=$time
        println("DUR " + player.media().info().duration())
        setTime(time)
        log.debug("Playing started")
    }

    override fun play(path: String) {
        val media = core.cacheService.createIfAbsent(path)
        play(media, media.time)
    }

    override fun play(p: PlayableMedia) = play(p, p.time)

    override fun getSubtitles(): Int = player.subpictures().track()

    override fun setSubtitles(id: Int) {
        player.subpictures().setTrack(id)
    }

    override fun getAudioTrack(): Int = player.audio().track()

    override fun setAudioTrack(trackId: Int) {
        player.audio().setTrack(trackId)
    }

    override fun isPlaying(): Boolean = player.status().isPlaying

    override fun getMediaLength(): Long = player.media().info().duration()

    override fun getCurrentTime(): Long = currentTime

    override fun getVolume(): Int = player.audio().volume()

    override fun setVolume(value: Int) {
        player.audio().setVolume(value)
    }

    override fun setTime(value: Long) = player.controls().setTime(value)

    override fun start() {
        player.controls().start()
    }

    override fun toggle() {
        if (player.status().isPlaying) {
            player.controls().pause()
        } else {
            player.controls().play()
        }
    }

    override fun pause() = player.controls().pause()

    override fun stop() {
        log.debug("Stop player")
        current = null
        currentTime = 0L
        player.controls().stop()
    }

    init {
        val start = System.currentTimeMillis()
        log.debug("VLCMediaService init start")
        try {
            player.events().addMediaPlayerEventListener(playerEventHandler)
        } catch (e: Exception) {
            log.error("Player initialization failed", e)
            // TODO
//            guiUtil.showMessage("VLC library not found", "Error title");
        }
        log.debug("VLCMediaService init complete in {} ms", System.currentTimeMillis() - start)
    }

}
