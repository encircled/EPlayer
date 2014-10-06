package cz.encircled.eplayer.view.fx;

import com.sun.jna.Memory;
import cz.encircled.eplayer.common.PostponeTimer;
import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventListener;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.view.fx.components.AppMenuBar;
import cz.encircled.eplayer.view.fx.components.PlayerControls;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.awt.*;
import java.nio.ByteBuffer;

/**
 * Created by Encircled on 18/09/2014.
 */
@Resource
@Factory(FxFactory.class)
@Runner(FxRunner.class)
public class PlayerScreen extends BorderPane {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private MediaService mediaService;

    private MenuBar menuBar;

    @Resource
    private FxView appView;

    private final DirectMediaPlayerComponent mediaPlayerComponent;

    private WritablePixelFormat<ByteBuffer> pixelFormat;

    private WritableImage writableImage;

    private StackPane playerStackPane;

    private Pane playerHolder;

    private PostponeTimer hideTimer;

    private FloatProperty videoSourceRatioProperty;

    private BooleanProperty fitToScreen;

    private ImageView imageView;

    private Robot robot;

    @Resource
    private PlayerControls playerControls;

    @Resource
    private Context context;

    @Resource
    private EventObserver eventObserver;

    private EventHandler<MouseEvent> fullScreenMouseMoveHandler = event -> {
        setCursor(Cursor.DEFAULT);
        if (event.getY() > getHeight() - playerControls.getHeight()) {
            playerControls.setVisible(true);
            hideTimer.cancel();
        } else if (event.getY() <= menuBar.getHeight()) {
            hideTimer.cancel();
            menuBar.setVisible(true);
        } else {
            if(menuBar.isVisible()) {
                menuBar.setVisible(false);
                menuBar.getMenus().stream().forEach(Menu::hide);
            }
            hideTimer.postpone(600);
        }
    };

    public void toggleFitToScreen() {
        fitToScreen.set(!fitToScreen.get());
    }

    public BooleanProperty fitToScreenProperty() {
        return fitToScreen;
    }

    public PlayerScreen() {
        super(new BorderPane());
        hideTimer = new PostponeTimer(() -> Platform.runLater(() -> setCursor(Cursor.NONE)));
        fitToScreen = new SimpleBooleanProperty(false);
        mediaPlayerComponent = new CanvasPlayerComponent();
        videoSourceRatioProperty = new SimpleFloatProperty(0.4f);
    }

    public DirectMediaPlayerComponent getMediaPlayerComponent() {
        return mediaPlayerComponent;
    }

    @PostConstruct
    private void initialize() {
        pixelFormat = PixelFormat.getByteBgraPreInstance();

        playerHolder = new Pane();
        playerStackPane = new StackPane(playerHolder);
        playerHolder.setStyle("-fx-background-color: #000");

        menuBar = context.getComponent(AppMenuBar.class).getMenuBar();

        initializeImageView();

        setTop(menuBar);
        setCenter(playerStackPane);
        setBottom(playerControls);

        initializeListeners();
    }

    private void initializeListeners() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        fitToScreen.addListener(observable -> fitImageViewSize((float) playerHolder.getWidth(), (float) playerHolder.getHeight()));
        playerHolder.setOnMouseClicked(event -> {
            mediaService.toggle();
            if (event.getClickCount() > 1) {
                appView.toggleFullScreen();
            }
        });

//        Platform.runLater is required, because elements must be removed after full screen repaint
        appView.getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            if (newValue)
                onFullScreen();
            else
                onNotFullScreen();
        }));
        if (appView.isFullScreen()) {
            onFullScreen();
        }

        eventObserver.listen(Event.playingChanged, new EventListener<Boolean, Void>() {
            @Override
            public void handle(Event event, Boolean isPlaying, Void arg2) {
                if (isPlaying) {
                    // TODO
//                    robot.keyPress(KeyEvent.VK_0);
                }
            }
        });

    }

    private void onNotFullScreen() {
        hideTimer.cancel();

        playerStackPane.getChildren().removeAll(playerControls, menuBar);

        setCursor(Cursor.DEFAULT);
        setBottom(playerControls);
        setTop(menuBar);

        menuBar.setVisible(true);
        playerControls.setVisible(true);

        playerHolder.setOnMouseMoved(null);
        playerControls.setOnMouseExited(null);
        menuBar.setOnMouseExited(null);
    }

    private void onFullScreen() {
        setCursor(Cursor.NONE);
        getChildren().removeAll(playerControls, menuBar);
        playerStackPane.getChildren().addAll(playerControls, menuBar);
        StackPane.setAlignment(playerControls, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        playerHolder.setOnMouseMoved(fullScreenMouseMoveHandler);
        menuBar.setVisible(false);
        playerControls.setVisible(false);
        playerControls.setOnMouseExited(event -> playerControls.setVisible(false));
    }

    private void initializeImageView() {
        writableImage = new WritableImage((int) appView.screenBounds.getWidth(), (int) appView.screenBounds.getHeight());

        imageView = new ImageView(writableImage);
        playerHolder.getChildren().add(imageView);

        playerHolder.widthProperty().addListener((observable, oldValue, newValue) -> {
            fitImageViewSize(newValue.floatValue(), (float) playerHolder.getHeight());
        });

        playerHolder.heightProperty().addListener((observable, oldValue, newValue) -> {
            fitImageViewSize((float) playerHolder.getWidth(), newValue.floatValue());
        });

        videoSourceRatioProperty.addListener((observable, oldValue, newValue) -> {
            fitImageViewSize((float) playerHolder.getWidth(), (float) playerHolder.getHeight());
        });
    }

    private void fitImageViewSize(float width, float height) {
        Platform.runLater(() -> {
            if (fitToScreen.get()) {
                imageView.setX(0);
                imageView.setY(0);
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
            } else {
                float fitHeight = videoSourceRatioProperty.get() * width;
                if (fitHeight > height) {
                    imageView.setFitHeight(height);
                    double fitWidth = height / videoSourceRatioProperty.get();
                    imageView.setFitWidth(fitWidth);
                    imageView.setX((width - fitWidth) / 2);
                    imageView.setY(0);
                } else {
                    imageView.setFitWidth(width);
                    imageView.setFitHeight(fitHeight);
                    imageView.setY((height - fitHeight) / 2);
                    imageView.setX(0);
                }
            }
        });
    }

    private class CanvasPlayerComponent extends DirectMediaPlayerComponent {

        public CanvasPlayerComponent() {
            super(new CanvasBufferFormatCallback());
        }

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
            if (writableImage == null)
                return;
            Memory nativeBuffer = nativeBuffers[0];
            ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
            Platform.runLater(() -> {
                writableImage.getPixelWriter().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
            });
        }

    }

    private class CanvasBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
            return new RV32BufferFormat((int) appView.screenBounds.getWidth(), (int) appView.screenBounds.getHeight());
        }
    }

}
