package cz.encircled.eplayer.view.fx.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.Localization
import cz.encircled.eplayer.util.StringUtil
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiUtil.inNormalThread
import cz.encircled.eplayer.view.fx.FxView
import cz.encircled.eplayer.view.addNewValueListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.Slider
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.text.Text
import org.apache.logging.log4j.LogManager

private const val HEIGHT = 53.0

/**
 * @author Encircled on 21/09/2014.
 */
class PlayerControls(private val core: ApplicationCore, private val fxView: FxView, private val dataModel: UiDataModel) : GridPane() {

    private val log = LogManager.getLogger()

    private var timeSlider: Slider = Slider()

    private var timeFlyingText: Text = Text()

    private var timeText: Text = Text(StringUtil.msToTimeLabel(0L))

    private var totalTimeText: Text = Text()

    private var volumeText: Text = Text()

    private var timeTextSeparator: Text = Text(" / ")

    private lateinit var volumeSlider: Slider

    private var volumeButton: ToggleButton = ToggleButton()

    private var playerToggleButton: ToggleButton = ToggleButton()

    private var fitScreenToggleButton: ToggleButton = ToggleButton()

    private var lastVolumeSliderValue = 0.0

    init {
        initializeTimeControls()
        initializeVolumeControls()
        initializeButtons()

        styleClass.add("player_controls")
        setPrefSize(fxView.screenBounds.width, HEIGHT)
        setMaxSize(fxView.screenBounds.width, HEIGHT)

        val timeTextPane = HBox(timeText, timeTextSeparator, totalTimeText)
        timeTextPane.padding = Insets(3.0, 0.0, 0.0, 0.0)

        val timeColumn = ColumnConstraints(100.0, 100.0, Double.MAX_VALUE)
        timeColumn.hgrow = Priority.ALWAYS

        timeFlyingText.y = 12.0
        timeFlyingText.isVisible = false

        add(playerToggleButton, 0, 1)
        add(volumeButton, 1, 1)
        add(volumeSlider, 2, 1)
        add(volumeText, 3, 1)
        add(Pane(timeFlyingText), 4, 0)
        add(timeSlider, 4, 1)
        add(timeTextPane, 5, 1)
        add(fitScreenToggleButton, 6, 1)

        columnConstraints.add(ColumnConstraints(35.0))
        columnConstraints.add(ColumnConstraints(30.0))
        columnConstraints.add(ColumnConstraints(130.0))
        columnConstraints.add(ColumnConstraints(50.0))
        columnConstraints.add(timeColumn)
        columnConstraints.add(ColumnConstraints(130.0))

        rowConstraints.add(RowConstraints(12.0))
    }

    private fun initializeButtons() {
        dataModel.fitToScreen.addNewValueListener { fitScreenToggleButton.isSelected = it }

        val tooltip = Tooltip(Localization.fitScreen.ln())

        fitScreenToggleButton.tooltip = tooltip
        fitScreenToggleButton.id = "fitScreen"
        fitScreenToggleButton.onAction = EventHandler { dataModel.toggleFitToScreen() }
        fitScreenToggleButton.isSelected = dataModel.fitToScreen.get()

        playerToggleButton.id = "play"
        playerToggleButton.onAction = EventHandler { e: ActionEvent? -> core.mediaService.toggle() }
        Event.playingChanged.listenUiThread { playerToggleButton.isSelected = !it.characteristic }

        volumeButton.id = "mute"
        volumeButton.onAction = EventHandler {
            if (volumeButton.isSelected) {
                lastVolumeSliderValue = volumeSlider.value
                volumeSlider.value = 0.0
            } else {
                volumeSlider.value = lastVolumeSliderValue
            }
        }
    }

    private fun initializeVolumeControls() {
        Event.mediaDurationChange.listen { core.mediaService.volume = core.settings.lastVolume }

        volumeSlider = Slider(0.0, core.settings.maxVolume.toDouble(), core.settings.lastVolume.toDouble())

        volumeSlider.valueChangingProperty().addNewValueListener {
            if (!it) saveLastVolumeToSettings()
        }
        volumeSlider.valueProperty().addNewValueListener {
            val volume = it.toInt()
            volumeText.text = "$volume %"
            volumeButton.isSelected = volume == 0
            if (!volumeSlider.isValueChanging) {
                saveLastVolumeToSettings()
            }
            core.mediaService.volume = volume
        }

        volumeText.text = "${volumeSlider.value.toInt()} %"
    }

    private fun saveLastVolumeToSettings() = inNormalThread {
        core.settings.lastVolume(volumeSlider.value.toInt())
    }

    private fun initializeTimeControls() {
        // Flying label
        timeSlider.valueChangingProperty().addNewValueListener { timeFlyingText.isVisible = it }
        val handler = EventHandler { event: MouseEvent ->
            timeFlyingText.x = event.x - 20
            timeFlyingText.text = StringUtil.msToTimeLabel(timeSlider.value.toLong())
        }
        timeSlider.onMouseMoved = handler
        timeSlider.onMouseDragged = handler

        // Max label
        timeSlider.maxProperty().addNewValueListener { totalTimeText.text = StringUtil.msToTimeLabel(it.toLong()) }

        // TODO check - was deleted
        Event.mediaDurationChange.listenUiThread { timeSlider.max = it.characteristic.toDouble() }

        // Current time and scrolling
        timeSlider.onMousePressed = EventHandler { event: MouseEvent ->
            val valueUnderCursor = event.x / timeSlider.width * timeSlider.max
            core.mediaService.setTime(valueUnderCursor.toLong())
        }
        timeSlider.valueProperty().addNewValueListener {
            if (timeSlider.isValueChanging) {
                core.mediaService.setTime(it.toLong())
            }
        }
        Event.mediaTimeChange.listenUiThread {
            if (!timeSlider.isValueChanging) {
                timeSlider.value = it.characteristic.toDouble()
                timeText.text = StringUtil.msToTimeLabel(it.characteristic)
            }
        }
    }

}