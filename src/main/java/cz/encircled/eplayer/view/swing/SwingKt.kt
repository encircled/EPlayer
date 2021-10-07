package cz.encircled.eplayer.view.swing

import cz.encircled.eplayer.view.swing.components.MainFrame
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.RemovalAware
import cz.encircled.eplayer.view.swing.components.base.ToggleButton
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

inline fun JComponent.onHover(crossinline onEnter: () -> Unit, crossinline onLeft: () -> Unit) {
    addMouseListener(object : MouseAdapter() {

        var isInside: AtomicBoolean = AtomicBoolean(false)

        override fun mouseEntered(e: MouseEvent) {
            if (isInside.compareAndSet(false, true)) {
                onEnter.invoke()
            }
        }

        override fun mouseExited(e: MouseEvent) {
            if (e.point.x < 0 || e.point.x >= this@onHover.width || e.point.y < 0 || e.point.y >= this@onHover.height) {
                if (isInside.compareAndSet(true, false)) {
                    onLeft.invoke()
                }
            }
        }
    })
}

inline fun JComponent.onClick(crossinline callback: (e: MouseEvent) -> Unit): JComponent {
    addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            callback(e)
        }
    })
    return this
}

inline fun JSlider.onChange(crossinline callback: (Int, Boolean) -> Unit) {
    addChangeListener {
        callback.invoke((it.source as JSlider).value, (it.source as JSlider).valueIsAdjusting)
    }
}

inline fun Document.onChange(crossinline callback: (String) -> Unit) {
    addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) = callback.invoke(e.document.getText(0, e.document.length))

        override fun removeUpdate(e: DocumentEvent) = callback.invoke(e.document.getText(0, e.document.length))

        override fun changedUpdate(e: DocumentEvent) = callback.invoke(e.document.getText(0, e.document.length))
    })
}

fun JComponent.addAll(vararg components: Component): JComponent {
    components.forEach(this::add)
    revalidate()
    repaint()
    return this
}

fun JComponent.addAll(components: List<Component>) {
    components.forEach(this::add)
    revalidate()
    repaint()
}

inline fun JComponent.removeIf(crossinline callback: (c: Component) -> Boolean) {
    for (component in components) {
        if (callback.invoke(component)) {
            if (component is RemovalAware) {
                try {
                    component.beforeRemoved()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            remove(component)
        }
    }
    revalidate()
    repaint()
}

// ******** //
// BUILDERS //
// ******** //

inline fun gridPanel(crossinline init: BaseJPanel.() -> Unit): BaseJPanel = BaseJPanel().apply { this.init() }
inline fun borderPanel(crossinline init: BaseJPanel.() -> Unit): BaseJPanel =
    BaseJPanel(BorderLayout()).apply { this.init() }

inline fun flowPanel(
    hgap: Int = 5,
    vhap: Int = 5,
    align: Int = FlowLayout.LEFT,
    crossinline init: BaseJPanel.() -> Unit = {}
): BaseJPanel =
    BaseJPanel(FlowLayout(align, hgap, vhap)).apply { this.init() }

inline fun boxPanel(
    align: Int = BoxLayout.Y_AXIS,
    crossinline init: BaseJPanel.() -> Unit = {}
): BaseJPanel =
    BaseJPanel().apply {
        this.layout = BoxLayout(this, align)
        this.init()
    }

fun iconButton(clazz: String, tooltip: String = "", group: ButtonGroup? = null, onClick: () -> Unit = {}): Component =
    ToggleButton(clazz).apply {
        onClick { onClick() }
        this.toolTipText = tooltip
        group?.add(this)
    }

fun String.toIcon() = try {
    ImageIcon(MainFrame::class.java.getResource("/icons/$this"))
} catch (e: Exception) {
    println("Icon not found: $this")
    throw e
}


// ******* //
// STYLING //
// ******* //

fun JComponent.padding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    border = EmptyBorder(top, left, bottom, right)
}

fun JComponent.border(color: Color, thickness: Int = 1) {
    border = BorderFactory.createLineBorder(color, thickness)
}

fun JComponent.border(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, color: Color) {
    border = BorderFactory.createMatteBorder(top, left, bottom, right, color)
}

fun JComponent.removeSelf() {
    val p = parent
    if (this is RemovalAware) {
        beforeRemoved()
    }
    p.remove(this)
    p.validate()
    p.repaint()
}