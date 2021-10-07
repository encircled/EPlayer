package cz.encircled.eplayer.view.swing.components.base

import java.awt.Font
import javax.swing.JLabel

class BaseLabel(
    text: String,
    isBold: Boolean = false,
    fontSize: Float = 12f,
) : JLabel(text) {

    init {
        val style = if (isBold) font.style or Font.BOLD else font.style
        font = font.deriveFont(style, fontSize)
    }

}