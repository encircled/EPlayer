package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.eplayer.view.AppView
import java.awt.*
import javax.swing.JPanel
import kotlin.math.max

open class BaseJPanel(
    defLayout: LayoutManager = GridBagLayout(),
    var anchor: Int = GridBagConstraints.NORTH,
) : JPanel() {

    var currentRow = -1
    var currentColumn = -1

    init {
        layout = defLayout
    }

    inline fun nextRow(component: Component, crossinline prep: GridData.() -> Unit = {}) {
        addNext(component, max(0, currentColumn), ++currentRow, prep)
    }

    inline fun nextRow(gridData: GridData = GridData(), crossinline component: () -> Component) {
        addNext(component.invoke(), max(0, currentColumn), ++currentRow, gridData)
    }

    inline fun nextColumn(component: Component, crossinline prep: GridData.() -> Unit = {}) {
        addNext(component, ++currentColumn, max(0, currentRow), prep)
    }

    inline fun nextColumn(gridData: GridData = GridData(), crossinline component: () -> Component): Component {
        return addNext(component.invoke(), ++currentColumn, max(0, currentRow), gridData)
    }

    inline fun addNext(component: Component, x: Int, y: Int, crossinline prep: GridData.() -> Unit): Component {
        val data = GridData()
        prep(data)
        return addNext(component, x, y, data)
    }

    fun addNext(component: Component, x: Int, y: Int, data: GridData): Component {
        val constraints = GridBagConstraints()
        constraints.weightx = 1.0
        constraints.weighty = 1.0

        if (data.width != null) {
            // TODO or not TODO?
            constraints.fill = GridBagConstraints.VERTICAL
        } else {
            constraints.fill = GridBagConstraints.HORIZONTAL
        }

        if (data.height != null) {
            constraints.weighty = 0.0
            component.minimumSize = Dimension(AppView.MIN_WIDTH, data.height!!)
            component.preferredSize = Dimension(AppView.MIN_WIDTH, data.height!!)
        }
        if (data.width != null) {
            constraints.weightx = 0.0
            component.minimumSize = Dimension(data.width!!, component.minimumSize.height)
            component.preferredSize = Dimension(data.width!!, component.preferredSize.height)
        }
        if (data.height != null && data.width != null) {
            constraints.weighty = 0.0
            constraints.weightx = 0.0
            component.minimumSize = Dimension(data.width!!, data.height!!)
            component.preferredSize = Dimension(data.width!!, data.height!!)
        }

        if (data.width == null && data.height == null) {
            constraints.fill = GridBagConstraints.BOTH
        }

        if (data.fill != null) {
            constraints.fill = data.fill!!
        }

        constraints.gridx = x
        constraints.gridy = y
        constraints.gridheight = 1
        constraints.gridwidth = 1

        constraints.anchor = anchor

        if (layout is GridBagLayout) {
            add(component, constraints)
        } else {
            add(component)
        }

        return component
    }

    data class GridData(
        var width: Int? = null,
        var height: Int? = null,
        var fill: Int? = null
    )

    companion object {
        val BLUE_BG = Color(9, 118, 171)
        val LIGHTER_BG = Color(69, 73, 74)
        val DEFAULT_BG = Color(60, 63, 65)
        val MEDIUM_BG = Color(45, 48, 51)
        val DARK_BG = Color(33, 36, 38)
    }

}