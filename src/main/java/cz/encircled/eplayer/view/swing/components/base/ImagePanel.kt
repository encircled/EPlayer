package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.UiUtil
import cz.encircled.eplayer.view.swing.padding
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.JTextArea


class ImagePanel(path: String, private val placeholder: String) : JPanel() {

    private var image: BufferedImage? = null

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (image != null) {
            g.drawImage(image, 0, 0, this)
        }
    }

    init {
        background = BaseJPanel.LIGHTER_BG
        setImage(path)
    }

    fun setImage(path: String) {
        UiUtil.inNormalThread {
            try {
                image = ImageIO.read(File(path))
                UiUtil.inUiThread {
                    removeAll()
                    validate()
                    repaint()
                }
            } catch (ex: IOException) {
                UiUtil.inUiThread {
                    showPlaceholder()
                }
            }
        }
    }

    private fun showPlaceholder() {
        padding(5, 15, 5)
        val label = NoAutoScrollTextArea(placeholder)
        label.lineWrap = true

        label.size = Dimension(AppView.SCREENSHOT_WIDTH, AppView.SCREENSHOT_HEIGHT)
        label.font = Font(label.font.name, Font.BOLD, 17)
        label.background = BaseJPanel.LIGHTER_BG
        label.isEditable = false

        add(label, BorderLayout.CENTER)
    }

    class NoAutoScrollTextArea(value: String) : JTextArea(value) {
        override fun scrollRectToVisible(aRect: Rectangle?) {
        }
    }

}