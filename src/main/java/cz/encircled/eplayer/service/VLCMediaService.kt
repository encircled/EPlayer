package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.MediaCharacteristic
import cz.encircled.eplayer.service.event.OptionalMediaCharacteristic
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiUtil
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.media.AudioTrackInfo
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

    private val VLC_ARGS = "--mmdevice-passthrough=2"
    private val log = LogManager.getLogger()
    private var currentTime: Long = 0
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

    private lateinit var subtitle: GenericTrackDescription

    private lateinit var audioTrack: GenericTrackDescription

    init {
        Event.audioTrackChanged.listen { change ->
            switchAudioPassThruIfNeeded(change.characteristic)
        }
    }

    override fun currentSubtitle(): GenericTrackDescription = subtitle

    override fun setSubtitle(track: GenericTrackDescription) = doSetSubtitle(track, true)

    private fun doSetSubtitle(track: GenericTrackDescription, byUser: Boolean) {
        if (!this::subtitle.isInitialized || track != subtitle) {
            log.debug("Set subtitle track {}", track)
            subtitle = track
            player.subpictures().setTrack(track.id)
            Event.subtitleChanged.fire(MediaCharacteristic(current!!, track, byUser))
        }
    }

    override fun currentAudioTrack(): GenericTrackDescription = audioTrack

    override fun setAudioTrack(track: GenericTrackDescription) = doSetAudioTrack(track, true)

    private fun doSetAudioTrack(track: GenericTrackDescription, byUser: Boolean) {
        if (!this::audioTrack.isInitialized || track != audioTrack) {
            log.debug("Set audio track {}", track)
            this.audioTrack = track
            player.audio().setTrack(track.id)
            Event.audioTrackChanged.fire(MediaCharacteristic(current!!, track, byUser))
        }
    }

    override fun currentMedia(): PlayableMedia? = current

    override fun createPlayer(): EmbeddedMediaPlayerComponent {
        releasePlayer()

        val mediaPlayerFactory = MediaPlayerFactory(if (core.settings.audioPassThrough) VLC_ARGS else "")
        val playerComponent = EmbeddedMediaPlayerComponent(mediaPlayerFactory, null, null, null, null)

        this.player = playerComponent.mediaPlayer()
        this.player.events().addMediaPlayerEventListener(playerEventHandler)

        return playerComponent
    }

    // TODO double check - not to block native event thread
    private val playerEventHandler = object : MediaPlayerEventAdapter() {

        override fun playing(mediaPlayer: MediaPlayer) =
            Event.playingChanged.fire(OptionalMediaCharacteristic(current, true))

        override fun paused(mediaPlayer: MediaPlayer) =
            Event.playingChanged.fire(OptionalMediaCharacteristic(current, false))

        override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
            log.info("Media parsed - {}", current)

            val textTracks = mediaPlayer.media().info().textTracks().map { it.toGenericTrack() } +
                    listOf(GenericTrackDescription(-1, "Disabled"))

            val audioTracks = mediaPlayer.media().info().audioTracks().map { it.toGenericTrack() }

            setAudioAndSubtitleTracks(audioTracks, textTracks)
        }

        override fun finished(mediaPlayer: MediaPlayer) {
            currentTime = 0L
            Event.mediaTimeChange.fire(MediaCharacteristic(current!!, 0), true)
//            core.openQuickNaviScreen()
        }

        override fun stopped(mediaPlayer: MediaPlayer) {
            Event.playingChanged.fire(OptionalMediaCharacteristic(null, false))
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
            current?.let { Event.mediaTimeChange.fire(MediaCharacteristic(it, newTime)) }
        }

        override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
            Event.mediaDurationChange.fire(MediaCharacteristic(current!!, newLength))
        }

        override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {
            try {
                Event.screenshotAcquired.fire(MediaCharacteristic(current ?: SingleMedia(""), current?.path ?: ""))
            } catch (e: Exception) {
                log.warn("Event error", e)
            }
        }

    }

    private fun setAudioAndSubtitleTracks(
        audioTracks: List<GenericTrackDescription>,
        textTracks: List<GenericTrackDescription>
    ) {
        // TODO language to be a setting
        val suggestAudio = core.mediaSettingsSuggestions.suggestAudioTrack("rus", audioTracks)
        val audio = audioTracks.find(current?.preferredAudio) ?: suggestAudio.track
        val text = textTracks.find(current?.preferredSubtitle) ?: core.mediaSettingsSuggestions.suggestSubtitle(
            "rus",
            current?.preferredAudio != null || suggestAudio.isPreferredLanguage,
            textTracks
        ).track

        doSetSubtitle(text, false)
        doSetAudioTrack(audio, false)

        Event.subtitlesUpdated.fire(textTracks)
        Event.audioTracksUpdated.fire(audioTracks)

        switchAudioPassThruIfNeeded(audioTrack)
    }

    private fun switchAudioPassThruIfNeeded(track: GenericTrackDescription) {
        val isRequired = core.mediaSettingsSuggestions.suggestAudioPassThroughRequired(track)
        if (isRequired && !core.settings.audioPassThrough) {
            core.settings.audioPassThrough(true)
            Event.audioPassThroughChange.fire(true)
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

    private fun TrackInfo.toGenericTrack(): GenericTrackDescription {
        val prefix = if (description() == null) "${language() ?: ""} "
        else "${description()} [${language()}] "

        val description =
            if (this is AudioTrackInfo) "$prefix (${codecName()}: ${codecDescription()}, ${rate()} Hz, ${channels()}c)"
            else "$prefix (${codecName()})"

        return GenericTrackDescription(id(), description)
    }

    private fun List<GenericTrackDescription>.find(track: GenericTrackDescription?): GenericTrackDescription? =
        if (track != null && contains(track)) track else null

}
