package cz.encircled.eplayer.model

/**
 * @author encir on 29-Aug-20.
 */
data class MediaTab(
        val id: Long,
        var path: String,
        val closeable: Boolean = true
)