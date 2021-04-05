package cz.encircled.eplayer.service.event

import cz.encircled.eplayer.model.PlayableMedia

/**
 * @author encir on 02-Oct-20.
 */
data class MediaCharacteristic<T>(val playableMedia: PlayableMedia, val characteristic: T)

data class OptionalMediaCharacteristic<T>(val playableMedia: PlayableMedia?, val characteristic: T)
