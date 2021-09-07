package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.MediaCharacteristic
import cz.encircled.eplayer.util.TimeMeasure.measure
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiUtil.inNormalThread
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * @author Encircled on 9.6.2014.
 */
class VLCMediaService(private val core: ApplicationCore) : MediaService {

    private val log = LogManager.getLogger()
    private val vlcArgs = "--mmdevice-passthrough=2"

    private var current: PlayableMedia? = null

    lateinit var player: MediaPlayer

    override var volume: Int
        get() = player.audio().volume()
        set(value) {
            if (value != player.audio().volume()) {
                player.audio().setVolume(value)
                Event.volumeChanged.fire(value)
            }
        }

    private var subtitle: GenericTrackDescription? = null

    private var audioTrack: GenericTrackDescription? = null

    init {
        Event.audioTrackChanged.listen { change ->
            switchAudioPassThruIfNeeded(change.characteristic)
        }
    }

    override fun currentSubtitle() = subtitle

    override fun setSubtitle(track: GenericTrackDescription, byUser: Boolean) {
        if (track != subtitle) {
            log.debug("Set subtitle track {}", track)
            subtitle = track
            player.subpictures().setTrack(track.id)
            Event.subtitleChanged.fire(MediaCharacteristic(current!!, track, byUser))
        }
    }

    override fun currentAudioTrack() = audioTrack

    override fun setAudioTrack(track: GenericTrackDescription, byUser: Boolean) {
        if (track != audioTrack) {
            log.debug("Set audio track {}", track)
            this.audioTrack = track
            player.audio().setTrack(track.id)
            Event.audioTrackChanged.fire(MediaCharacteristic(current!!, track, byUser))
        }
    }

    override fun currentMedia(): PlayableMedia? = current

    override fun createPlayer(): EmbeddedMediaPlayerComponent {
        releasePlayer()

        val mediaPlayerFactory = MediaPlayerFactory(if (core.settings.audioPassThrough) vlcArgs else "")
        val playerComponent = EmbeddedMediaPlayerComponent(mediaPlayerFactory, null, null, null, null)

        this.player = playerComponent.mediaPlayer()
        this.player.events().addMediaPlayerEventListener(VLCEventListener(core, this))

        return playerComponent
    }

    override fun isPlaying(): Boolean = player.status().isPlaying

    override fun play(path: String) = play(core.cacheService.createIfAbsent(path))

    override fun play(media: PlayableMedia) {
        if (!this::player.isInitialized) return

        core.cacheService.createIfAbsent(media)

        val path = media.mediaFile().path
        log.info("Play {}, start time is {}", path, media.time)

        measure("Show player on UI") {
            val countDownLatch = CountDownLatch(1)
            core.appView.showPlayer(countDownLatch)
            countDownLatch.await()
        }

        subtitle = null
        audioTrack = null
        current = media

        inNormalThread {
            player.media().start(path)

            setTime(media.time.get())
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
            val newTime = player.status().time() + value
            setTime(newTime)
            Event.mediaTimeChange.fire(MediaCharacteristic(current!!, newTime), true)
        }
    }

    override fun setTime(value: Long) {
        if (player.status().isPlayable) {
            player.controls().setTime(value)
        } else {
            // Restart the media from given time, if it has already finished
            if (value > 0) {
                current?.let {
                    play(it)
                    player.controls().setTime(value)
                }
            }
        }
    }

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
        } else {
            // Restart the media, if it has finished
            current?.let { play(it) }
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
        player.controls().stop()
        current = null
    }

    override fun releasePlayer() {
        if (this::player.isInitialized) {
            stop()
            player.release()
        }
    }

    override fun switchAudioPassThruIfNeeded(track: GenericTrackDescription) {
        val isRequired = core.mediaSettingsSuggestions.suggestAudioPassThroughRequired(track)
        if (isRequired && !core.settings.audioPassThrough) {
            core.settings.audioPassThrough(true)
            Event.audioPassThroughChange.fire(true)
        }
    }

}
