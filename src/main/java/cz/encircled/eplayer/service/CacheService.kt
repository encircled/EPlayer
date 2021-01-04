package cz.encircled.eplayer.service

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.model.PlayableMedia

/**
 * @author Encircled on 9.6.2014.
 */
interface CacheService {

    fun getOrNull(path: String): PlayableMedia?

    fun createIfAbsent(path: String): PlayableMedia

    fun createIfAbsent(media: PlayableMedia): PlayableMedia

    fun deleteEntry(media: PlayableMedia): PlayableMedia

    fun lastByWatchDate(): PlayableMedia?

    fun save()

    fun getCached(): List<PlayableMedia>

}