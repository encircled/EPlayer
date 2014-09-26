package cz.encircled.eplayer.view.swing;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.nio.ByteBuffer;

/**
 * Example showing how to render video to a JavaFX Canvas component.
 * <p>
 * The target is to render full HD video (1920x1080) at a reasonable frame rate (>25fps).
 * <p>
 * This test can render the video at a fixed size, or it can take the size from the
 * video itself.
 * <p>
 * -Dprism.verbose=true
 * <p>
 * You may need to set -Djna.library.path=[path-to-libvlc] on the command-line.
 * <p>
 * Based on an example contributed by John Hendrikx.
 */
public final class JavaFXDirectRenderingTest extends Application {

    /**
     * Filename of the video to play.
     */
    private static final String VIDEO_FILE = "D:\\video\\Konstantin.avi";

    /**
     * Set this to <code>true</code> to resizePlayer the display to the dimensions of the
     * video, otherwise it will use {@link #WIDTH} and {@link #HEIGHT}.
     */
    private static final boolean useSourceSize = true;

    /**
     * Target width, unless {@link #useSourceSize} is set.
     */
    private static final int WIDTH = 1920;

    /**
     * Target height, unless {@link #useSourceSize} is set.
     */
    private static final int HEIGHT = 1080;

    /**
     * Lightweight JavaFX canvas, the video is rendered here.
     */
    private final Canvas canvas;

    /**
     * Pixel writer to update the canvas.
     */
    private final PixelWriter pixelWriter;

    /**
     * Pixel format.
     */
    private final WritablePixelFormat<ByteBuffer> pixelFormat;

    /**
     *
     */
    private final BorderPane borderPane;

    /**
     * The vlcj direct rendering media player component.
     */
    private final DirectMediaPlayerComponent mediaPlayerComponent;

    /**
     *
     */
    private Stage stage;

    /**
     *
     */
    public JavaFXDirectRenderingTest() {
        canvas = new Canvas();

        pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        pixelFormat = PixelFormat.getByteBgraInstance();

        borderPane = new BorderPane();
        borderPane.setCenter(canvas);

        mediaPlayerComponent = new TestMediaPlayerComponent();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        stage.setTitle("vlcj JavaFX Direct Rendering Test");

        Scene scene = new Scene(borderPane);

        primaryStage.setScene(scene);
        primaryStage.show();

        mediaPlayerComponent.getMediaPlayer().prepareMedia(VIDEO_FILE);
        mediaPlayerComponent.getMediaPlayer().start();
    }

    @Override
    public void stop() throws Exception {
        mediaPlayerComponent.getMediaPlayer().stop();
        mediaPlayerComponent.getMediaPlayer().release();
    }

    /**
     * Implementation of a direct rendering media player component that renders
     * the video to a JavaFX canvas.
     */
    private class TestMediaPlayerComponent extends DirectMediaPlayerComponent {

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
            Memory nativeBuffer = nativeBuffers[0];
            ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                }
            });

        }

        public TestMediaPlayerComponent() {
            super(new TestBufferFormatCallback());
        }
    }

    /**
     * Callback to get the buffer format to use for video playback.
     */
    private class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            int width;
            int height;
            if (useSourceSize) {
                width = sourceWidth;
                height = sourceHeight;
            } else {
                width = WIDTH;
                height = HEIGHT;
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    canvas.setWidth(width);
                    canvas.setHeight(height);
                    stage.setWidth(width);
                    stage.setHeight(height);
                }
            });

            return new RV32BufferFormat(width, height);
        }
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "D:\\vlc-2.1.5-win64\\vlc-2.1.5");
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
        Application.launch(args);
    }
}