package cz.encircled.eplayer.core;

import com.alee.laf.WebLookAndFeel;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.util.*;
import cz.encircled.eplayer.view.Components;
import cz.encircled.eplayer.view.Frame;
import cz.encircled.eplayer.view.actions.ActionExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static cz.encircled.eplayer.util.LocalizedMessages.*;

// TODO Youtube tab

public class Application {

    private static final Logger log = LogManager.getLogger(Application.class);

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    private FileVisitorManager d;

    private static ActionExecutor actionExecutor;

    private CacheService cacheService;

    private MediaService mediaService;

    @Nullable
    private Frame frame;

    public void exit() {
        System.exit(0);
    }

    public static ActionExecutor getActionExecutor(){
        return actionExecutor;
    }

    public void reinitialize() throws IOException {
        log.trace("App init");
        // TODO what do we need here?
        if(frame != null){
            frame.stopPlayer();
            frame.dispose();
            frame = null;
        }
        initializeGui(null);
        log.trace("Reinitializing completed");
    }

    private void initialize(String[] arguments) throws IOException {
        log.trace("App init");
        IOUtil.createIfMissing(APP_DOCUMENTS_ROOT, true);
        PropertyProvider.initialize();
        log.trace("Properties init success");
        MessagesProvider.initialize();
        log.trace("Messages init success");
        GUIUtil.setFrame(frame);
        actionExecutor = new ActionExecutor(this);
        addCloseHook();
        d = new FileVisitorManager();
        initializeGui(arguments.length > 0 ? arguments[0] : null);
        log.trace("Init complete");
    }

    private void initializeGui(String openWhenReady){
        SwingUtilities.invokeLater(() -> {

            // TODO move it
            Font font = new Font("Dialog", Font.BOLD,  12);
            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("TextField.font", new Font("Dialog", Font.BOLD,  14));

            UIManager.put("Label.foreground", Components.MAIN_GRAY_COLOR);
            UIManager.put("Button.foreground", Components.MAIN_GRAY_COLOR);
            UIManager.put("TextField.foreground", Components.MAIN_GRAY_COLOR);
            if(!WebLookAndFeel.install()){
                log.error("Failed to initialize weblaf");
            }
            frame = new Frame(this);
            actionExecutor.setFrame(frame);

            if(StringUtil.isSet(openWhenReady)){
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowActivated(WindowEvent e) {
                        play(openWhenReady);
                        frame.removeWindowListener(this);
                    }
                });
            } else {
                frame.showQuickNavi();
            }

            frame.run();
        });
    }


    public Map<Integer, Playable> getTest(){
        return d.getPaths().get(Paths.get("C:\\"));
    }

    private void addCloseHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if(frame != null) {
                    frame.updateCurrentPlayableInCache();
                    frame.releasePlayer();
                }
            }
        });
        log.trace("Close hook added");
    }

    public static void main(final String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            new Application().initialize(args);
        } catch (Throwable e) {
            log.error(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
