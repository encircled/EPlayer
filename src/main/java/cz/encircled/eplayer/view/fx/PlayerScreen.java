package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.common.PostponeTimer;
import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.view.fx.components.AppMenuBar;
import cz.encircled.eplayer.view.fx.components.ImageViewVideoSurface;
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
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * @author Encircled on 18/09/2014.
 */
public class PlayerScreen extends BorderPane {

    private static final Logger log = LogManager.getLogger();

    // Enable HDMI audio passthrough for Dolby/DTS audio
    public static final String VLC_ARGS = "--mmdevice-passthrough=2";

    private MenuBar menuBar;

    private MediaPlayer mediaPlayerComponent;

    private WritableImage writableImage;

    private StackPane playerStackPane;

    private Pane playerHolder;

    private PostponeTimer hideTimer;

    private FloatProperty videoSourceRatioProperty;

    private BooleanProperty fitToScreen;

    private ImageView imageView;

    private PlayerControls playerControls;

    private FxView fxView;

    @NotNull
    private EventHandler<MouseEvent> fullScreenMouseMoveHandler = event -> {
        setCursor(Cursor.DEFAULT);
        if (event.getY() > getHeight() - playerControls.getHeight()) {
            playerControls.setVisible(true);
            hideTimer.cancel();
        } else if (event.getY() <= menuBar.getHeight()) {
            hideTimer.cancel();
            menuBar.setVisible(true);
        } else {
            if (menuBar.isVisible()) {
                menuBar.setVisible(false);
                menuBar.getMenus().forEach(Menu::hide);
            }
            hideTimer.postpone(600);
        }
    };

    public PlayerScreen(ApplicationCore core, FxView fxView) {
        super(new BorderPane());
        this.fxView = fxView;
        this.fitToScreen = new SimpleBooleanProperty(false);
    }

    public void toggleFitToScreen() {
        fitToScreen.set(!fitToScreen.get());
    }

    public BooleanProperty fitToScreenProperty() {
        return fitToScreen;
    }

    public MediaPlayer getMediaPlayerComponent() {
        return mediaPlayerComponent;
    }

    public void init(@NotNull ApplicationCore core, @NotNull AppMenuBar appMenuBar) {
        hideTimer = new PostponeTimer(() -> Platform.runLater(() -> setCursor(Cursor.NONE)));

        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(VLC_ARGS);
        EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        // TODO
        videoSourceRatioProperty = new SimpleFloatProperty(0.56f);

        this.menuBar = appMenuBar.getMenuBar();
        this.playerControls = new PlayerControls(core, fxView);

        playerHolder = new Pane();
        playerStackPane = new StackPane(playerHolder);
        playerHolder.setStyle("-fx-background-color: #000");

        initializeImageView();
        mediaPlayer.videoSurface().set(new ImageViewVideoSurface(writableImage, fxView, videoSourceRatioProperty).videoSurface);
        mediaPlayerComponent = mediaPlayer;

        setTop(menuBar);
        setCenter(playerStackPane);
        setBottom(playerControls);

        initializeListeners(core);
    }

    private void initializeListeners(@NotNull ApplicationCore core) {
        fitToScreen.addListener(observable -> fitImageViewSize((float) playerHolder.getWidth(), (float) playerHolder.getHeight()));
        playerHolder.setOnMouseClicked(event -> {
            core.getMediaService().toggle();
            if (event.getClickCount() > 1) {
                fxView.toggleFullScreen();
            }
        });

//        Platform.runLater is required, because elements must be removed after full screen repaint
        fxView.getPrimaryStage().fullScreenProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            if (newValue)
                onFullScreen();
            else
                onNotFullScreen();
        }));
        if (fxView.isFullScreen()) {
            onFullScreen();
        }

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
        writableImage = new WritableImage((int) fxView.screenBounds.getWidth(), (int) fxView.screenBounds.getHeight());

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
/*

    private class CanvasPlayerComponent extends DirectMediaPlayerComponent {

        @Nullable
        PixelWriter pixelWriter = null;

        public CanvasPlayerComponent() {
            super(new CanvasBufferFormatCallback());
        }

        @NotNull
        private PixelWriter getPW() {
            if (pixelWriter == null)
                pixelWriter = writableImage.getPixelWriter();
            return pixelWriter;
        }

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, @NotNull BufferFormat bufferFormat) {
            if (writableImage == null) {
                return;
            }
            Platform.runLater(() -> {
                Memory nativeBuffer = mediaPlayer.lock()[0];
//                Memory nativeBuffer = nativeBuffers[0];
                try {
                    ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
                    getPW().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    mediaPlayer.unlock();
                }
            });
        }

    }

    private class CanvasBufferFormatCallback implements BufferFormatCallback {
        @NotNull
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            log.debug("GetBufferFormat: source dimension is {}x{}", sourceWidth, sourceHeight);
            Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
            return new RV32BufferFormat((int) fxView.screenBounds.getWidth(), (int) fxView.screenBounds.getHeight());
        }
    }
*/

}
