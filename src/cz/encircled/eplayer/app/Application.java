package cz.encircled.eplayer.app;

import com.alee.laf.WebLookAndFeel;
import com.google.gson.JsonSyntaxException;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.model.Playable;
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
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class Application {

	private static final Logger log = LogManager.getLogger(Application.class);

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    public static final String QUICK_NAVI_PATH = APP_DOCUMENTS_ROOT + "quicknavi.json";

	private static ActionExecutor actionExecutor;

    private Map<Integer, Playable> playableCache;

    @Nullable
    private Frame frame;

    private volatile boolean isVlcAvailable = false;

    public boolean isVlcAvailable(){
        return isVlcAvailable;
    }

    private void waitAndShowMessage(final String text, final String title, final int messageLevel){
        new Thread(() -> {
            boolean retry = true;
            int i = 0;
            while(retry && i++ < 10){
                if(frame != null){
                    JOptionPane.showMessageDialog(frame, text, title, messageLevel);
                    retry = false;
                } else {
                    try{
                        Thread.sleep(500);
                    } catch (InterruptedException e){
                        retry = false;
                        log.warn("waitAndShowMessage thread", e);
                    }
                }
            }
        }).start();
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

	private void initializeVLCLib(){
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc-2.1.3");
		try {
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            isVlcAvailable = true;
            log.trace("VLCLib successfully initialized");
		} catch(UnsatisfiedLinkError e){
            isVlcAvailable = false;
            e.printStackTrace();
			waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_VLC_LIBS_FAIL), MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
			log.error("Failed to load vlc libs from specified path {}", "vlc-2.1.3");
		} 
	}

	public Map<Integer, Playable> getPlayableCache(){
        if(playableCache == null)
            playableCache = new HashMap<>();
        return playableCache;
	}

    public void play(@NotNull String path){
        play(getOrCreatePlayable(path));
    }

	public void play(@NotNull Playable p){
		log.debug("play: {}", p.toString());
		if(p.exists()){
			frame.play(p.getPath(), p.getTime());
		} else {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
                getPlayableCache().remove(p.getPath().hashCode());
                p.readPath(fc.getSelectedFile().getAbsolutePath());
                getPlayableCache().put(p.getPath().hashCode(), p);
                play(p);
            }
		}
    }

    public Playable getOrCreatePlayable(@NotNull String path){
        int hash = path.hashCode();
        Playable p = getPlayableCache().get(hash);
        if(p == null){
            p = new Playable(path);
            getPlayableCache().put(hash, p);
        }
        return p;
    }

    public void updatePlayableCache(int hash, long time){
        Playable p = getPlayableCache().get(hash);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        savePlayable();
    }

    public void deletePlayableCache(int hash){
        Playable deleted = getPlayableCache().remove(hash);
        if(deleted == null){
            log.warn("Playable with hash {} not exists", hash);
        } else {
            if (deleted.exists() && JOptionPane.showConfirmDialog(frame, "delete file?", "title", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                boolean wasDeleted =     new File(deleted.getPath()).delete();
                log.debug("Playable {} was deleted: {}", deleted.getName() ,wasDeleted);
            }
            actionExecutor.setDefaultFileChooserPath();
            frame.repaintQuickNavi(); // TODO
            savePlayable();
        }
    }

    public void playLast(){
        Playable p = getLastPlayable();
        if(p != null)
            play(p);
    }

    @Nullable
    private Playable getLastPlayable(){
        Playable p = getPlayableCache()
                            .values()
                            .stream()
                            .max((p1, p2) -> Long.compare(p1.getWatchDate(), p2.getWatchDate())).get();
        log.debug("Last played: {}", p);
        return p;
    }

    public void exit() {
        System.exit(0);
    }

    /**
     * Async save playable map to file in JSON format
     */
    public synchronized void savePlayable(){
        new Thread(() -> {
            try {
                IOUtil.storeJson(getPlayableCache(), QUICK_NAVI_PATH);
                log.debug("Json successfully saved");
            } catch (IOException e) {
                log.error("Failed to save playable to {}, msg {}", QUICK_NAVI_PATH, e);
            }
        }).start();
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
        initializePlayable();
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
        initializeVLCLib();
        initializePlayable();
        actionExecutor = new ActionExecutor(this);
        addCloseHook();
        initializeGui(arguments.length > 0 ? arguments[0] : null);
        log.trace("Init complete");
    }

    private void initializeGui(String openWhenReady){
        SwingUtilities.invokeLater(() -> {
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

    private void initializePlayable(){
        new Thread(() -> {
            log.trace("Init playable cache");
            try {
                if(IOUtil.createIfMissing(QUICK_NAVI_PATH)){
                    log.debug("QuickNavi file was created");
                }
            } catch (IOException e) {
                log.error("Failed to create QuickNavi data file at {}", QUICK_NAVI_PATH);
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_CREATE_QN_FILE_FAIL),
                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                playableCache = IOUtil.getPlayableJson(QUICK_NAVI_PATH);
            } catch (IOException e) {
                log.error("Failed to read playableCache data from {} with default type token. Message: {}",
                        PropertyProvider.get(QUICK_NAVI_PATH), e.getMessage());
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_IO_FAIL),
                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            } catch (JsonSyntaxException e){
                log.error("JSON syntax error. Message: {}", e.getMessage());
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_CORRUPTED),
                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            }
            if(playableCache == null)
                playableCache = new HashMap<>();
            checkHashes();
        }).start();
    }

    private void checkHashes() {
        List<Integer> corruptedHashes = new ArrayList<>();
        playableCache.forEach((key, value) -> {
            if (value.getPath().hashCode() != key) {
                corruptedHashes.add(key);
                log.warn("Playable {} has wrong hash code, updating...", value.getName());
            }
        });
        corruptedHashes.forEach((oldHash) -> playableCache.put(playableCache.get(oldHash).getPath().hashCode(), playableCache.remove(oldHash)));
    }

    public static void main(final String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            new Application().initialize(args);
        } catch (Throwable e) {
            log.error(e);
            System.exit(-1);
        }
    }

}
