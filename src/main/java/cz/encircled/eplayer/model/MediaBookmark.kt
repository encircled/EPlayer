package cz.encircled.eplayer.model

import cz.encircled.eplayer.util.StringUtil

data class MediaBookmark(val time: Long) {

    override fun toString(): String = StringUtil.msToTimeLabel(time)

}
