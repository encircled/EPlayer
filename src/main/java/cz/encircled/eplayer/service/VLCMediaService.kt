package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.MediaCharacteristic
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiUtil
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.media.Meta
import uk.co.caprica.vlcj.media.TrackInfo
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * @author Encircled on 9.6.2014.
 */
class VLCMediaService(private val core: ApplicationCore) : MediaService {

    // Enable HDMI audio passthrough for Dolby/DTS audio TODO configurable
    private val VLC_ARGS = "--mmdevice-passthrough=2"
    private val log = LogManager.getLogger()
    private var currentTime: Long = 0
    private var current: PlayableMedia? = null

    lateinit var player: MediaPlayer

    override var volume: Int
        get() = player.audio().volume()
        set(value) {
            player.audio().setVolume(value)
        }

    override var subtitles: Int
        get() = player.subpictures().track()
        set(value) {
            player.subpictures().setTrack(value)
            Event.subtitleChanged.fire(MediaCharacteristic(current!!, value))
        }

    override var audioTrack: Int
        get() = player.audio().track()
        set(value) {
            log.debug("Set audio track with id {}", value)
            player.audio().setTrack(value)
            Event.audioTrackChanged.fire(MediaCharacteristic(current!!, value))
        }

    override fun createPlayer(): EmbeddedMediaPlayerComponent {
        val mediaPlayerFactory = MediaPlayerFactory(VLC_ARGS)
        val playerComponent = EmbeddedMediaPlayerComponent(mediaPlayerFactory, null, null, null, null)

        this.player = playerComponent.mediaPlayer()
        this.player.events().addMediaPlayerEventListener(playerEventHandler)

        return playerComponent
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
            Event.mediaTimeChange.fire(MediaCharacteristic(current!!, 0), true)
            core.openQuickNaviScreen()
        }

        override fun error(mediaPlayer: MediaPlayer) {
            log.error("Failed to open media {} ", current)
            core.appView.showUserMessage(Localization.fileOpenFailed.ln() + " " + current?.path)
            if (current != null) {
                core.cacheService.deleteEntry(current!!)
                current = null
            }
            core.openQuickNaviScreen()
        }

        override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
            currentTime = newTime
            if (current != null) Event.mediaTimeChange.fire(MediaCharacteristic(current!!, newTime))
        }

        override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
            Event.mediaDurationChange.fire(MediaCharacteristic(current!!, newLength))
        }

        override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {
            try {
                Event.screenshotAcquired.fire(MediaCharacteristic(current ?: SingleMedia(""), current?.path ?: ""))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun releasePlayer() {
        if (this::player.isInitialized) {
            stop()
            player.release()
        }
    }

    override fun isPlaying(): Boolean = player.status().isPlaying

    override fun play(path: String) = play(core.cacheService.createIfAbsent(path))

    override fun play(media: PlayableMedia) {
        if (!this::player.isInitialized) return

        core.cacheService.createIfAbsent(media)

        val path = media.mediaFile().path
        log.debug("Play {}, start time is {}", path, media.time)

        val countDownLatch = CountDownLatch(1)

        log.debug("Show player requested")
        core.appView.showPlayer(countDownLatch)
        countDownLatch.await()
        log.debug("Show player done")
        current = media
        UiUtil.inNormalThread {
            player.media().start(path)

            setTime(media.time)
            media.preferredAudio?.let { audioTrack = it }
            media.preferredSubtitle?.let { subtitles = it }
        }

        log.debug("Playing started")
    }

    override fun playNext() {
        val media = current
        if (media is MediaSeries) {
            stop()
            media.toNext()
            play(media)
        }
    }

    override fun playPrevious() {
        val media = current
        if (media is MediaSeries) {
            stop()
            media.toPrev()
            play(media)
        }
    }

    override fun setTimePlus(value: Long) {
        if (current != null) {
            currentTime += value
            setTime(currentTime)
            Event.mediaTimeChange.fire(MediaCharacteristic(current!!, currentTime), true)
        }
    }

    override fun setTime(value: Long) = player.controls().setTime(value)

    override fun start() {
        player.controls().start()
    }

    override fun toggle() {
        if (player.status().isPlayable) {
            if (player.status().isPlaying) {
                player.controls().pause()
            } else {
                player.controls().play()
            }
        }
    }

    override fun pause() {
        if (player.status().isPlaying) player.controls().pause()
    }

    override fun stop() {
        current?.mediaFile()?.let {
            player.snapshots().save(
                File(ApplicationCore.getScreenshotLocation(it)),
                AppView.SCREENSHOT_WIDTH,
                AppView.SCREENSHOT_HEIGHT
            )
        }

        log.debug("Stop player")
        current = null
        currentTime = 0L
        player.controls().stop()
    }

}
