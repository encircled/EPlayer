package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.fx.FxTest;
import cz.encircled.eplayer.view.fx.FxUtil;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by Encircled on 21/09/2014.
 */
@Resource
@Runner(FxRunner.class)
public class PlayerControls extends GridPane {

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

    @Resource
    private Settings settings = new Settings();

    @Resource
    private FxTest fxTest;

    @Resource
    private EventObserver eventObserver;

    private static final char TIME_SEPARATOR = ':';

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
        setPrefSize(fxTest.screenBounds.getWidth(), HEIGHT);
        setMaxSize(fxTest.screenBounds.getWidth(), HEIGHT);
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

        getColumnConstraints().add(new ColumnConstraints(35));
        getColumnConstraints().add(new ColumnConstraints(30));
        getColumnConstraints().add(new ColumnConstraints(130));
        getColumnConstraints().add(new ColumnConstraints(50));
        ColumnConstraints timeColumn = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        timeColumn.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(timeColumn);
        getColumnConstraints().add(new ColumnConstraints(100));

        getRowConstraints().add(new RowConstraints(12));
    }

    private void initializeButtons() {
        playerToggleButton = new ToggleButton();
        volumeButton = new ToggleButton();

        playerToggleButton.setId("play");
        playerToggleButton.setOnAction(e -> mediaService.pause());
        eventObserver.listen(Event.playingChanged, (event, arg, arg2) -> playerToggleButton.setSelected(!arg));

        volumeButton.setId("mute");
        volumeButton.setFocusTraversable(false);
        volumeButton.setBorder(null);
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
        timeText = new Text(msToTimeLabel(0L));

        volumeText.setFill(textColor);
        timeFlyingText.setFill(textColor);
        timeText.setFill(textColor);
        totalTimeText.setFill(textColor);
        timeTextSeparator.setFill(textColor);

        timeFlyingText.setY(12);
        timeFlyingText.setVisible(false);

    }

    private void initializeVolumeControls() {

        volumeSlider = new Slider(0, settings.getInt(Settings.MAX_VOLUME, 150), settings.getInt(Settings.LAST_VOLUME, 100));
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
        mediaService.setVolume(volume);
        volumeText.setText(volume + " %");
    }

    private void saveLastVolumeToSettings() {
        FxUtil.workInNormalThread(() -> {
            settings.set(Settings.LAST_VOLUME, (int) volumeSlider.getValue());
            settings.save();
        });
    }

    private void initializeTimeControls() {
        timeSlider = new Slider();
        timeSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            mediaService.pause(); // TODO real pause
            timeFlyingText.setVisible(Boolean.TRUE.equals(newValue));
        });

        timeSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mediaService.pause();
                mediaService.setTime((long) timeSlider.getValue());
                mediaService.pause();
            }
        });

        EventHandler<MouseEvent> handler = event -> {
            timeFlyingText.setX(event.getX() - 20);
            String timeTextLabel = msToTimeLabel((long) timeSlider.getValue());
            timeFlyingText.setText(timeTextLabel);
            timeText.setText(timeTextLabel);
        };
        timeSlider.setOnMouseMoved(handler);
        timeSlider.setOnMouseDragged(handler);

//        timeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
//
//            if(timeSlider.isValueChanging())
//                mediaService.setTime(newValue.longValue());
//        });

        timeSlider.maxProperty().addListener((observable, oldValue, newValue) -> totalTimeText.setText(msToTimeLabel(newValue.longValue())));
        timeSlider.setMax(1000000);

        eventObserver.listenFxThread(Event.mediaTimeChange, (event, newTime, arg2) -> {
            timeSlider.setValue(newTime);
        });
        eventObserver.listenFxThread(Event.mediaDurationChange, (event, newTime, arg2) -> timeSlider.setMax(newTime));

    }

    private static String msToTimeLabel(long ms) {
        long h = TimeUnit.MILLISECONDS.toHours(ms);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
        long s = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));

        StringBuilder sb = new StringBuilder();
        appendZeroIfMissing(sb, h);
        sb.append(h).append(TIME_SEPARATOR);
        appendZeroIfMissing(sb, m);
        sb.append(m).append(TIME_SEPARATOR);
        appendZeroIfMissing(sb, s);
        sb.append(s);

        return sb.toString();
    }

    private static void appendZeroIfMissing(@NotNull StringBuilder sb, long digit) {
        if (digit < Constants.TEN)
            sb.append(Constants.ZERO_STRING);
    }

}
