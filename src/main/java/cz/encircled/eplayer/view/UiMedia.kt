package cz.encircled.eplayer.view

import cz.encircled.eplayer.model.PlayableMedia

interface UiMedia {

    fun name(): String

    fun path(): String

}

class UiPlayableMedia(
    val media: PlayableMedia
) : UiMedia {

    override fun name(): String = media.name()

    override fun path(): String = media.path

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UiPlayableMedia

        if (media != other.media) return false

        return true
    }

    override fun hashCode(): Int {
        return media.hashCode()
    }

    override fun toString(): String {
        return "UiPlayableMedia(media=${media.path})"
    }

}

class UiFolderMedia(
    val name: String,
    val path: String,
    val nestedMedia: List<PlayableMedia>,
    val dynamicFilter: ((UiMedia) -> Boolean)? = null,
) : UiMedia {

    override fun name(): String = path

    override fun path(): String = path

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UiFolderMedia

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return "UiFolderMedia(path='$path')"
    }

}
