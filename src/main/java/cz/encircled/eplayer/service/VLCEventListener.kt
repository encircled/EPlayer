package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.service.event.MediaCharacteristic
import cz.encircled.eplayer.service.event.OptionalMediaCharacteristic
import cz.encircled.eplayer.util.Localization
import org.apache.logging.log4j.LogManager
import uk.co.caprica.vlcj.media.AudioTrackInfo
import uk.co.caprica.vlcj.media.TrackInfo
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter

class VLCEventListener(val core: ApplicationCore, val mediaService: MediaService) : MediaPlayerEventAdapter() {

    private val log = LogManager.getLogger()

    override fun playing(mediaPlayer: MediaPlayer) =
        Event.playingChanged.fire(OptionalMediaCharacteristic(mediaService.currentMedia(), true))

    override fun paused(mediaPlayer: MediaPlayer) =
        Event.playingChanged.fire(OptionalMediaCharacteristic(mediaService.currentMedia(), false))

    override fun finished(mediaPlayer: MediaPlayer) = withCurrent {
        log.info("Media finished")
        mediaService.setTime(0)
        Event.mediaTimeChange.fire(MediaCharacteristic(it, 0), true)
    }

    override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) = withCurrent {
        Event.mediaTimeChange.fire(MediaCharacteristic(it, newTime))
    }

    override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) = withCurrent {
        Event.mediaDurationChange.fire(MediaCharacteristic(it, newLength))
    }

    override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) = withCurrent {
        Event.screenshotAcquired.fire(MediaCharacteristic(it, it.path))
    }

    override fun stopped(mediaPlayer: MediaPlayer) {
        log.info("Media stopped")
        Event.playingChanged.fire(OptionalMediaCharacteristic(null, false))
    }

    override fun error(mediaPlayer: MediaPlayer) = withCurrent {
        log.error("Failed to open media {} ", it)
        core.appView.showUserMessage(Localization.fileOpenFailed.ln() + " " + it.path)
        core.cacheService.deleteEntry(it)

        core.appActions.openQuickNaviScreen()
    }

    override fun mediaPlayerReady(mediaPlayer: MediaPlayer) = withCurrent { current ->
        log.info("Media parsed - {}", current)

        val textTracks = mediaPlayer.media().info().textTracks().map { it.toGenericTrack() } +
                listOf(GenericTrackDescription(-1, "Disabled"))

        val audioTracks = mediaPlayer.media().info().audioTracks().map { it.toGenericTrack() }

        setAudioAndSubtitleTracks(current, audioTracks, textTracks)
    }

    private fun setAudioAndSubtitleTracks(
        current: PlayableMedia,
        audioTracks: List<GenericTrackDescription>,
        textTracks: List<GenericTrackDescription>
    ) {
        // TODO language to be a setting
        val suggestAudio = core.mediaSettingsSuggestions.suggestAudioTrack("rus", audioTracks)
        val audio = audioTracks.find(current.preferredAudio) ?: suggestAudio.track
        val text = textTracks.find(current.preferredSubtitle) ?: core.mediaSettingsSuggestions.suggestSubtitle(
            "rus",
            current.preferredAudio != null || suggestAudio.isPreferredLanguage,
            textTracks
        ).track

        mediaService.setSubtitle(text, false)
        mediaService.setAudioTrack(audio, false)

        Event.subtitlesUpdated.fire(textTracks)
        Event.audioTracksUpdated.fire(audioTracks)

        mediaService.switchAudioPassThruIfNeeded(mediaService.currentAudioTrack())
    }

    private inline fun withCurrent(crossinline consumer: (PlayableMedia) -> Unit) {
        val c = mediaService.currentMedia()
        if (c != null) {
            consumer.invoke(c)
        }
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