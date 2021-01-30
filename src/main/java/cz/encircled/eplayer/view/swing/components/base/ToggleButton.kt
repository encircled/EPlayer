package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.eplayer.view.swing.toIcon
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.JToggleButton

class ToggleButton(iconName: String, selectedIconName: String = iconName) : JToggleButton() {

    init {
        isBorderPainted = false
        isFocusPainted = false
        isContentAreaFilled = false

        icon = iconName.toIcon()
        rolloverIcon = iconName.replace(".", "_hover.").toIcon()

        selectedIcon = selectedIconName.toIcon()
        rolloverSelectedIcon = selectedIconName.replace(".", "_hover.").toIcon()
    }

}