package cz.encircled.eplayer.service

import cz.encircled.eplayer.model.PlayableMedia
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent

/**
 * @author Encircled on 9.6.2014.
 */
interface MediaService {

    var volume: Int
    var subtitles: Int
    var audioTrack: Int

    fun currentMedia(): PlayableMedia?

    fun createPlayer(): EmbeddedMediaPlayerComponent

    fun releasePlayer()

    fun isPlaying(): Boolean

    fun play(path: String)

    fun play(media: PlayableMedia)

    fun playNext()

    fun playPrevious()

    fun setTime(value: Long)

    /**
     * Add $value ms to the current time
     */
    fun setTimePlus(value: Long)

    fun start()

    fun toggle()

    fun pause()

    fun stop()

}