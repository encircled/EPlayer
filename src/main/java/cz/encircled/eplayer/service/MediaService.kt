package cz.encircled.eplayer.service

import cz.encircled.eplayer.model.PlayableMedia

/**
 * @author Encircled on 9.6.2014.
 */
interface MediaService {

    var volume: Int
    var subtitles: Int
    var audioTrack: Int

    fun releasePlayer()

    fun play(path: String)

    fun play(media: PlayableMedia)

    fun playNext()

    fun playPrevious()

    fun setTime(value: Long)

    fun start()

    fun toggle()

    fun pause()

    fun stop()

}