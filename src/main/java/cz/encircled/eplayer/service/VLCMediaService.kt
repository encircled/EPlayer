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
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * @author Encircled on 9.6.2014.
 */
class VLCMediaService(private val core: ApplicationCore) : MediaService {

    private val log = LogManager.getLogger()
    private var currentTime: Long = 0
    private var current: PlayableMedia? = null

    lateinit var player: MediaPlayer

    fun setMediaPlayer(player: MediaPlayer) {
        this.player = player
        this.player.events().addMediaPlayerEventListener(playerEventHandler)
    }

    // TODO double check - not to block native event thread
    private val playerEventHandler = object : MediaPlayerEventAdapter() {

        override fun playing(mediaPlayer: MediaPlayer) = Event.playingChanged.fire(true)

        override fun paused(mediaPlayer: MediaPlayer) = Event.playingChanged.fire(false)

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

            Event.subtitlesUpdated.fire(textTracks)
            Event.audioTracksUpdated.fire(audioTracks)
        }

        override fun finished(mediaPlayer: MediaPlayer) {
            currentTime = 0L
            core.openQuickNavi()
        }

        override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
            currentTime = newTime
            Event.mediaTimeChange.fire(newTime)
        }

        override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
            Event.mediaDurationChange.fire(newLength)
        }

        override fun error(mediaPlayer: MediaPlayer) {
            log.error("Failed to open media {} ", current)
            // TODO guiUtil.showMessage(Localization.fileOpenFailed.ln(), Localization.errorTitle.ln());
            if (current != null) {
                core.cacheService.deleteEntry(current!!)
                current = null
            }
            core.appView.showQuickNavi()
        }
    }

    override fun releasePlayer() {
        if (this::player.isInitialized) {
            stop()
            player.release()
        }
    }

    // TODO change me?
    override fun updateCurrentMediaInCache() {
        current?.let {
            core.cacheService.updateEntry(it, currentTime)
            player.snapshots().save(File(ApplicationCore.getScreenshotLocation(it.mediaFile())), 336, 189)
        }
    }

    private fun play(media: PlayableMedia, time: Long) {
        if (!this::player.isInitialized) return

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

    override fun play(p: PlayableMedia) = play(core.cacheService.createIfAbsent(p), p.time)

    override fun getSubtitles(): Int = player.subpictures().track()

    override fun setSubtitles(id: Int) {
        player.subpictures().setTrack(id)
    }

    override fun getAudioTrack(): Int = player.audio().track()

    override fun setAudioTrack(trackId: Int) {
        log.debug("Set audio track with id {}", trackId)
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
        log.debug("Do toggle, currently is playing: {}", player.status().isPlaying)
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

}
