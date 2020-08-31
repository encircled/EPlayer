package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.view.fx.FxView;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

/**
 * @author encir on 25-Aug-20.
 */
public final class ImageViewVideoSurface {

    private final WritableImage writableImage;
    private final BufferFormatCallback bufferFormatCallback;
    private final PixelBufferRenderCallback renderCallback;
    public PixelBufferVideoSurface videoSurface;
    private final FxView fxView;
    private final FloatProperty videoSourceRatioProperty;

    public ImageViewVideoSurface(WritableImage writableImage, FxView fxView, FloatProperty videoSourceRatioProperty) {
        this.fxView = fxView;
        this.videoSourceRatioProperty = videoSourceRatioProperty;
        this.writableImage = writableImage;
        this.bufferFormatCallback = new BufferFormatCallback();
        this.renderCallback = new PixelBufferRenderCallback();
        this.videoSurface = new PixelBufferVideoSurface();
    }

    private class BufferFormatCallback extends BufferFormatCallbackAdapter {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
            return new RV32BufferFormat((int) fxView.getScreenBounds().getWidth(), (int) fxView.getScreenBounds().getHeight());
        }

    }

    private class PixelBufferRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            writableImage.getPixelWriter().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(),
                    PixelFormat.getByteBgraPreInstance(), nativeBuffers[0], bufferFormat.getPitches()[0]);
        }
    }

    private class PixelBufferVideoSurface extends CallbackVideoSurface {
        private PixelBufferVideoSurface() {
            super(
                    ImageViewVideoSurface.this.bufferFormatCallback,
                    ImageViewVideoSurface.this.renderCallback,
                    true,
                    VideoSurfaceAdapters.getVideoSurfaceAdapter()
            );
        }
    }
}
