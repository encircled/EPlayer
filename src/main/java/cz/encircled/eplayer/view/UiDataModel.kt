package cz.encircled.eplayer.view

import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.view.swing.components.quicknavi.AbstractMediaPane
import cz.encircled.fswing.observable.collection.ObservableCollection
import cz.encircled.fswing.observable.observableList
import javafx.beans.property.*

const val QUICK_NAVI = "Quick Navi"

/**
 * @author encir on 05-Sep-20.
 */
data class UiDataModel(

    val fitToScreen: BooleanProperty = SimpleBooleanProperty(false),

    val filter: StringProperty = SimpleStringProperty(""),

    val media: ObservableCollection<UiMedia> = observableList(),

    val selectedMedia: ObjectProperty<UiMedia> = SimpleObjectProperty(),

    val selectedMediaPane: ObjectProperty<AbstractMediaPane?> = SimpleObjectProperty(),

    /**
     * Path to selected tab
     */
    val selectedTab: StringProperty = SimpleStringProperty(),

    /**
     * Path to selected folder within path, i.e. the full path to selected folder
     */
    val selectedFullPath: StringProperty = SimpleStringProperty(),

    val foldersToScan: ObservableCollection<String> = observableList(),

    val lastScrollPosition: IntegerProperty = SimpleIntegerProperty(0),

    val sortType: ObjectProperty<SortType> = SimpleObjectProperty(SortType.Name),

    val aggregationType: ObjectProperty<AggregationType> = SimpleObjectProperty(AggregationType.None),

    var dynamicFilterForMedia: ObjectProperty<((UiMedia) -> Boolean)> = SimpleObjectProperty(),

    /**
     * Display media from sub-folders, or preserve folders structure
     */
    val flatView: BooleanProperty = SimpleBooleanProperty(true),

    val currentMedia: ObjectProperty<PlayableMedia> = SimpleObjectProperty(null)

) {

    fun toggleFitToScreen() {
        fitToScreen.set(!fitToScreen.get())
    }

}