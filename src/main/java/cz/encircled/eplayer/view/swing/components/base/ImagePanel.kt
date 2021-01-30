package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.swing.padding
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.text.StyleConstants

import javax.swing.text.SimpleAttributeSet

import javax.swing.text.StyledDocument


class ImagePanel(path: String, placeholder: String) : JPanel() {

    private var image: BufferedImage? = null

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (image != null) {
            g.drawImage(image, 0, 0, this)
        }
    }

    init {
        background = BaseJPanel.LIGHTER_BG
        setImage(path, placeholder)
    }

    fun setImage(path: String, placeholder: String) {
        try {
            removeAll()
            image = ImageIO.read(File(path))
            validate()
            repaint()
        } catch (ex: IOException) {
        }

        if (image == null) {
            padding(5, 15, 5)
            val label = JTextArea(placeholder)
            label.lineWrap = true

            label.size = Dimension(AppView.SCREENSHOT_WIDTH, AppView.SCREENSHOT_HEIGHT)
            label.font = Font(label.font.name, Font.BOLD, 17)
            label.background = BaseJPanel.LIGHTER_BG
            label.isEditable = false

            add(label, BorderLayout.CENTER)
        }
    }
}