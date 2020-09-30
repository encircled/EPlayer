package cz.encircled.eplayer.view.fx

import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.view.fx.components.MediaPane
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

        val selectedMediaPane: ObjectProperty<MediaPane> = SimpleObjectProperty(),

        val selectedFolder: StringProperty = SimpleStringProperty(QUICK_NAVI),

        val foldersToScan: ObservableList<String> = FXCollections.observableArrayList(),

        ) {


    fun toggleFitToScreen() {
        fitToScreen.set(!fitToScreen.get())
    }

}