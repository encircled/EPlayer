package cz.encircled.eplayer.view.fx;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

/**
 * @author encir on 30-Sep-20.
 */
public class VlcjMain {

    public static void main(String[] args) {
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "E:\\vlc-3.0.11");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        long start = System.currentTimeMillis();
//        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory("--plugins-cache", "--no-reset-plugins-cache", "-vvv");
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory("plugins-cache=1", "--no-reset-plugins-cache", "-vvv"); // "--no-reset-plugins-cache=1"
        System.out.println(System.currentTimeMillis() - start);
        EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        System.out.println(System.currentTimeMillis() - start);
        mediaPlayer.release();
    }

}
