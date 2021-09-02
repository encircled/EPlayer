package cz.encircled.eplayer.view.swing.components.base

import cz.encircled.eplayer.util.Localization
import javafx.beans.property.ObjectProperty
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBox<E : Enum<*>>(values: Array<E>, val ln: Localization) : JComboBox<ComboBox.EnumWithTranslation<E>>() {

    init {
        model = DefaultComboBoxModel(values.map { EnumWithTranslation(ln, it) }.toTypedArray())
    }

    fun bindWith(prop: ObjectProperty<E>): ComboBox<E> {
        addItemListener {
            prop.value = (it.item as EnumWithTranslation<E>).enum
        }
        return this
    }

    class EnumWithTranslation<E>(val ln: Localization, val enum: E) {
        override fun toString(): String = ln.ln(enum.toString())
    }

}