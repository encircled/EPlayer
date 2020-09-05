package cz.encircled.eplayer.view.fx

import cz.encircled.eplayer.model.PlayableMedia
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * @author encir on 05-Sep-20.
 */
data class UiDataModel(

        private val initialFoldersToScan: List<String>,

        val fitToScreen: BooleanProperty = SimpleBooleanProperty(false),

        val filter: StringProperty = SimpleStringProperty(""),

        val media: ObservableList<PlayableMedia> = FXCollections.observableArrayList(),

        val selectedMedia: ObjectProperty<PlayableMedia> = SimpleObjectProperty(),

        val foldersToScan: ObservableList<String> = FXCollections.observableArrayList(initialFoldersToScan),

        ) {


    fun toggleFitToScreen() {
        fitToScreen.set(!fitToScreen.get())
    }

}