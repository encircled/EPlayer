package cz.encircled.eplayer.view.fx;

import com.sun.jna.Memory;
import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.view.fx.components.PlayerControls;
import cz.encircled.eplayer.view.swing.menu.MenuBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
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
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Encircled on 18/09/2014.
 */
@Resource
@Factory(FxFactory.class)
@Runner(FxRunner.class)
public class PlayerScene extends Scene {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private MediaService mediaService;

    @Resource
    private MenuBuilder menuBuilder;

    @Resource
    private FxTest fxTest;

    private final DirectMediaPlayerComponent mediaPlayerComponent;

    private WritablePixelFormat<ByteBuffer> pixelFormat;

    private WritableImage writableImage;

    private ImageView imageView;

    private Pane playerHolder;

    private Timer resizeTimer;

    private MenuBar menuBar;

    @Resource
    private PlayerControls playerControls;

    private BorderPane mainPane;

    public DirectMediaPlayerComponent getMediaPlayerComponent() {
        return mediaPlayerComponent;
    }

    public PlayerScene() {
        super(new BorderPane());
        mediaPlayerComponent = new CanvasPlayerComponent();
    }

    @PostConstruct
    private void initialize() {
        pixelFormat = PixelFormat.getByteBgraInstance();
        menuBar = menuBuilder.getFxMenu();
        playerHolder = new Pane();
        menuBar.setMinHeight(26);
        menuBar.setPrefHeight(26);
        menuBar.setVisible(true);

        playerHolder.setStyle("-fx-background-color: #000");

        mainPane = (BorderPane) getRoot();

        StackPane stackPane = new StackPane(playerHolder, playerControls);
        StackPane.setAlignment(playerHolder, Pos.CENTER);
        StackPane.setAlignment(playerControls, Pos.BOTTOM_CENTER);

        mainPane.setTop(menuBar);
        mainPane.setCenter(stackPane);

        InvalidationListener sizeListener = observable -> {
            if (resizeTimer != null) {
                resizeTimer.cancel();
            }
            resizeTimer = new Timer();
            resizeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    resizePlayer();
                }
            }, 150);
        };

        mainPane.widthProperty().addListener(sizeListener);
        mainPane.heightProperty().addListener(sizeListener);

        fxTest.getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            playerControls.setVisible(oldValue);
            if (newValue) {
                mainPane.getChildren().remove(menuBar);
            } else {
                mainPane.setTop(menuBar);
            }
        });
    }

    private void resizePlayer() {
        boolean wasPlaying = mediaService.isPlaying();
        long ct = mediaService.getCurrentTime();
        mediaService.stop();
        createWritableImage();
        mediaService.setTime(ct);
        if (wasPlaying) {
            mediaService.start();
        }
    }

    private void createWritableImage() {
        writableImage = new WritableImage(getHolderIntWidth(), getHolderIntHeight());
        imageView = new ImageView(writableImage);
        Platform.runLater(() -> playerHolder.getChildren().setAll(imageView));
    }

    private int getHolderIntWidth() {
        return new Double(playerHolder.getWidth()).intValue();
    }

    private int getHolderIntHeight() {
        return new Double(playerHolder.getHeight()).intValue();
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
            log.debug("New player buffer is {}:{}", playerHolder.getWidth(), playerHolder.getHeight());
            return new RV32BufferFormat(getHolderIntWidth(), getHolderIntHeight());
        }
    }

}
