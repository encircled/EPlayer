package cz.encircled.eplayer.view

import cz.encircled.eplayer.model.PlayableMedia
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList

const val QUICK_NAVI = "Quick Navi"

/**
 * @author encir on 05-Sep-20.
 */
data class UiDataModel(

    val fitToScreen: BooleanProperty = SimpleBooleanProperty(false),

    val filter: StringProperty = SimpleStringProperty(""),

    val media: ObservableList<PlayableMedia> = FXCollections.observableArrayList(),

    val selectedMedia: ObjectProperty<PlayableMedia> = SimpleObjectProperty(),

    val selectedMediaPane: ObjectProperty<Any> = SimpleObjectProperty(),

    val selectedFolder: StringProperty = SimpleStringProperty(),

    val foldersToScan: ObservableList<String> = FXCollections.observableArrayList(),

    val lastScrollPosition: IntegerProperty = SimpleIntegerProperty(0),

    val sortType: ObjectProperty<SortType> = SimpleObjectProperty(SortType.Name),

    val currentMedia: ObjectProperty<PlayableMedia> = SimpleObjectProperty(null)

) {

    fun toggleFitToScreen() {
        fitToScreen.set(!fitToScreen.get())
    }

}