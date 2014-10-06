package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.fx.FxUtil;
import cz.encircled.eplayer.view.fx.FxView;
import cz.encircled.eplayer.view.fx.PlayerScreen;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Encircled on 21/09/2014.
 */
@Resource
@Runner(FxRunner.class)
public class PlayerControls extends GridPane {

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

    public static final double HEIGHT = 53;

    private Slider timeSlider;

    private Text timeFlyingText;

    private Text timeText;

    private Text totalTimeText;

    private Text volumeText;

    private Text timeTextSeparator;

    private Slider volumeSlider;

    private ToggleButton volumeButton;

    private ToggleButton playerToggleButton;

    private ToggleButton fitScreenToggleButton;

    @Resource
    private FxView appView;

    @Resource
    private PlayerScreen playerScreen;

    @Resource
    private EventObserver eventObserver;

    @Resource
    private MediaService mediaService;

    private static final Color textColor = Color.rgb(155, 155, 155);

    private double lastVolumeSliderValue;

    @PostConstruct
    private void initialize() {
        initializeTexts();
        initializeTimeControls();
        initializeVolumeControls();
        initializeButtons();
        getStyleClass().add("player_controls");
        setPrefSize(appView.screenBounds.getWidth(), HEIGHT);
        setMaxSize(appView.screenBounds.getWidth(), HEIGHT);
        setPadding(new Insets(2, 20, 0, 20));
        setStyle("-fx-background-color: rgb(40,40,40)");
        setHgap(3);

        HBox timeTextPane = new HBox(timeText, timeTextSeparator, totalTimeText);
        timeTextPane.setPadding(new Insets(3, 0, 0, 0));
        add(playerToggleButton, 0, 1);
        add(volumeButton, 1, 1);
        add(volumeSlider, 2, 1);
        add(volumeText, 3, 1);
        add(new Pane(timeFlyingText), 4, 0);
        add(timeSlider, 4, 1);
        add(timeTextPane, 5, 1);

        add(fitScreenToggleButton, 6, 1);

        getColumnConstraints().add(new ColumnConstraints(35));
        getColumnConstraints().add(new ColumnConstraints(30));
        getColumnConstraints().add(new ColumnConstraints(130));
        getColumnConstraints().add(new ColumnConstraints(50));
        ColumnConstraints timeColumn = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        timeColumn.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(timeColumn);
        getColumnConstraints().add(new ColumnConstraints(130));

        getRowConstraints().add(new RowConstraints(12));
    }

    private void initializeButtons() {
        playerToggleButton = new ToggleButton();
        volumeButton = new ToggleButton();
        fitScreenToggleButton = new ToggleButton();

        fitScreenToggleButton.setId("fitScreen");
        fitScreenToggleButton.setOnAction(event -> playerScreen.toggleFitToScreen());
        playerScreen.fitToScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                fitScreenToggleButton.setSelected(newValue);
            }
        });
        fitScreenToggleButton.setSelected(playerScreen.fitToScreenProperty().get());
        Tooltip tooltip = new Tooltip(Localizations.get(LocalizedMessages.FIT_SCREEN));

        fitScreenToggleButton.setTooltip(tooltip);

        playerToggleButton.setId("play");
        playerToggleButton.setOnAction(e -> mediaService.toggle());
        eventObserver.listenFxThread(Event.playingChanged, (event, arg, arg2) -> playerToggleButton.setSelected(!arg));

        volumeButton.setId("mute");
        volumeButton.setOnAction(event -> {
            if (Boolean.TRUE.equals(volumeButton.isSelected())) {
                lastVolumeSliderValue = volumeSlider.getValue();
                volumeSlider.setValue(0);
            } else {
                volumeSlider.setValue(lastVolumeSliderValue);
            }
        });
    }

    private void initializeTexts() {
        volumeText = new Text();
        timeFlyingText = new Text();
        timeTextSeparator = new Text(" / ");
        totalTimeText = new Text();
        timeText = new Text(StringUtil.msToTimeLabel(0L));

        volumeText.setFill(textColor);
        timeFlyingText.setFill(textColor);
        timeText.setFill(textColor);
        totalTimeText.setFill(textColor);
        timeTextSeparator.setFill(textColor);

        timeFlyingText.setY(12);
        timeFlyingText.setVisible(false);

    }

    private void initializeVolumeControls() {

        volumeSlider = new Slider(0, Settings.getInt(Settings.MAX_VOLUME, 150), Settings.getInt(Settings.LAST_VOLUME, 100));
        volumeSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                saveLastVolumeToSettings();
            }
        });
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int volume = newValue.intValue();
            volumeText.setText(volume + " %");
            volumeButton.setSelected(volume == 0);
            if (!volumeSlider.isValueChanging()) {
                saveLastVolumeToSettings();
            }
            mediaService.setVolume(volume);
        });
        int volume = (int) volumeSlider.getValue();
        volumeText.setText(volume + " %");
    }

    private void saveLastVolumeToSettings() {
        FxUtil.workInNormalThread(() -> {
            Settings.set(Settings.LAST_VOLUME, (int) volumeSlider.getValue());
            Settings.save();
        });
    }

    private void initializeTimeControls() {
        timeSlider = new Slider();

        // Flying label
        timeSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            timeFlyingText.setVisible(Boolean.TRUE.equals(newValue));
        });
        EventHandler<MouseEvent> handler = event -> {
            timeFlyingText.setX(event.getX() - 20);
            timeFlyingText.setText(StringUtil.msToTimeLabel((long) timeSlider.getValue()));
        };
        timeSlider.setOnMouseMoved(handler);
        timeSlider.setOnMouseDragged(handler);

        // Max label
        timeSlider.maxProperty().addListener((observable, oldValue, newValue) -> totalTimeText.setText(StringUtil.msToTimeLabel(newValue.longValue())));
        eventObserver.listenFxThread(Event.mediaDurationChange, (event, newTime, arg2) -> timeSlider.setMax(newTime));

        // Current time and scrolling
        timeSlider.setOnMousePressed(event -> {
            double valueUnderCursor = event.getX() / timeSlider.getWidth() * timeSlider.getMax();
            mediaService.setTime((long) valueUnderCursor);
        });

        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (timeSlider.isValueChanging()) {
                mediaService.setTime(newValue.longValue());
            }
        });

        eventObserver.listenFxThread(Event.mediaTimeChange, (event, newTime, arg2) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newTime);
                timeText.setText(StringUtil.msToTimeLabel(newTime));
            }
        });

    }

}
