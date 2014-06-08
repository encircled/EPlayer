package cz.encircled.eplayer.app;

import com.alee.laf.WebLookAndFeel;
import com.google.gson.JsonSyntaxException;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.util.PropertyProvider;
import cz.encircled.eplayer.util.StringUtil;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static cz.encircled.eplayer.util.LocalizedMessages.*;

// TODO Youtube tab

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

	public Collection<Playable> getPlayableCache(){
        return playableCache.values();
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
                playableCache.put(p.getPath().hashCode(), p);
                play(p);
            }
		}
    }

    public Playable getOrCreatePlayable(@NotNull String path){
        return playableCache.computeIfAbsent(path.hashCode(), hash -> new Playable(path));
    }

    public void updatePlayableCache(int hash, long time){
        Playable p = playableCache.get(hash);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        savePlayable();
    }

    public void deletePlayable(int hash){
        Playable deleted = playableCache.remove(hash);
        if(deleted == null){
            log.warn("Playable with hash {} not exists", hash);
        } else {
            if (deleted.exists() && userConfirm(CONFIRM_DELETE_FILE)){
                boolean wasDeleted =     new File(deleted.getPath()).delete();
                log.debug("Playable {} was deleted: {}", deleted.getName() ,wasDeleted);
            }
            actionExecutor.setDefaultFileChooserPath();
            frame.repaintQuickNavi(); // TODO
            savePlayable();
        }
    }

    private boolean userConfirm(String confirmMessage) {
        return JOptionPane.showConfirmDialog(frame, MessagesProvider.get(confirmMessage), MessagesProvider.get(CONFIRM_TITLE), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void playLast(){
        Playable p = getLastPlayable();
        if(p != null)
            play(p);
    }

    @Nullable
    private Playable getLastPlayable(){
        Playable p = getPlayableCache()
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
                IOUtil.storeJson(playableCache, QUICK_NAVI_PATH);
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

    public void showMessage(String text, String title, int level){
        JOptionPane.showMessageDialog(frame, MessagesProvider.get(text),
                MessagesProvider.get(title), level);
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
                showMessage(MSG_CREATE_QN_FILE_FAIL, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                playableCache = IOUtil.getPlayableJson(QUICK_NAVI_PATH);
            } catch (IOException e) {
                log.error("Failed to read playableCache data from {} with default type token. Message: {}",
                                            PropertyProvider.get(QUICK_NAVI_PATH), e.getMessage());
                showMessage(MSG_QN_FILE_IO_FAIL, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            } catch (JsonSyntaxException e){
                log.error("JSON syntax error. Message: {}", e.getMessage());
                showMessage(MSG_QN_FILE_CORRUPTED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
            if(playableCache == null)
                playableCache = new HashMap<>();
            ;

            checkHashes();
        }).start();
    }
    FileVisitorManager d = new FileVisitorManager();
    public Map<Integer, Playable> getTest(){
        return d.getPaths().get(Paths.get("D:\\video"));
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
            showMessage(MessagesProvider.get(MSG_VLC_LIBS_FAIL), MessagesProvider.get(ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            log.error("Failed to load vlc libs from specified path {}", "vlc-2.1.3");
        }
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
