package cz.encircled.eplayer.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.util.MessagesProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;

import static cz.encircled.eplayer.util.LocalizedMessages.ERROR_TITLE;
import static cz.encircled.eplayer.util.LocalizedMessages.MSG_VLC_LIBS_FAIL;

/**
 * Created by Administrator on 9.6.2014.
 */
public class VLCMediaService implements MediaService {

    private static final Logger log = LogManager.getLogger();

    private CacheService cacheService;

    private ViewService viewService;

    private boolean isVlcAvailable = false;

    public static final String VLC_LIB_PATH = "vlc-2.1.3";

    public VLCMediaService(CacheService cacheService, ViewService viewService) {
        this.cacheService = cacheService;
        this.viewService = viewService;
    }

    public boolean isVlcAvailable(){
        return isVlcAvailable;
    }

    @Override
    public void play(@NotNull String path){
        play(cacheService.createIfAbsent(path));
    }

    @Override
    public void play(@NotNull Playable p){
        log.debug("play: {}", p.toString());
        if(p.exists()){
            viewService.play(p.getPath(), p.getTime());
        } else { // TODO
//            JFileChooser fc = new JFileChooser();
//            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
//                cacheService.deleteEntry(p.getPath().hashCode());
//                p.readPath(fc.getSelectedFile().getAbsolutePath());
//                playableCache.put(p.getPath().hashCode(), p);
//                play(p);
//            }
        }
    }

    private void initialize(){
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_LIB_PATH);
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            isVlcAvailable = true;
            log.trace("VLCLib successfully initialized");
        } catch(UnsatisfiedLinkError e){
            isVlcAvailable = false;
            GUIUtil.showMessage(MessagesProvider.get(MSG_VLC_LIBS_FAIL), MessagesProvider.get(ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            log.error("Failed to load vlc libs from specified path {}", VLC_LIB_PATH);
        }
    }

}
