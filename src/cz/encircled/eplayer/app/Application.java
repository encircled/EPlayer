package cz.encircled.eplayer.app;

import com.google.gson.JsonSyntaxException;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.*;
import cz.encircled.eplayer.view.Components;
import cz.encircled.eplayer.view.Frame;
import cz.encircled.eplayer.view.actions.ActionExecutor;
import cz.encircled.eplayer.view.listeners.BackgroundFocusListener;
import cz.encircled.eplayer.view.listeners.HoverMouseListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;


public class Application {

	private static final Logger log = LogManager.getLogger(Application.class);

    private static final String SD_CMD_COMMAND = "shutdown";

    private static final String SD_CMD_TIME = " /t ";

    public static final String SD_CMD_HIBERNATE = " /h ";

    public static final String SD_CMD_SHUTDOWN = " /s";

    private static final String SD_CMD_CANCEL = "shutdown -a";

    private static volatile Application instance;

	private ActionExecutor actionExecutor;
	
	private HoverMouseListener hoverMouseListener;
	
	private BackgroundFocusListener backgroundFocusListener;

    private Map<Integer, Playable> playableCache;

    private Timer hibernateTimer;

    private final MouseAdapter fileChooserMouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fc.showOpenDialog(SwingUtilities.getRoot(e.getComponent())) == JFileChooser.APPROVE_OPTION)
                ((JTextField)e.getSource()).setText(fc.getSelectedFile().getAbsolutePath());
        }
    };

    private final ActionListener defaultActionListener = e -> getActionExecutor().execute(e.getActionCommand());
	
	private Frame frame;

    private volatile boolean isVlcAvailable = false;
	
	public static Application getInstance(){
		Application local = instance;
		if(local == null){
			synchronized (Application.class) {
				local = instance;
				if(local == null)
					instance = new Application();
			}
		}
		return instance;
	}

    public ActionListener getDefaultActionMouseListener(){
    	return defaultActionListener;
    }

    public ActionExecutor getActionExecutor(){
        return actionExecutor;
    }

    public HoverMouseListener getHoverMouseListener(){
    	return hoverMouseListener;
    }
    
    public BackgroundFocusListener getBackgroundFocusListener(){
    	return backgroundFocusListener;
    }

    public MouseAdapter getFileChooserMouseAdapter(){
        return fileChooserMouseAdapter;
    }

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

	public void initialize(){
        log.trace("App init");
		if(frame != null){
            frame.stopPlayer();
			frame.dispose();
            frame = null;
		}
		PropertyProvider.initialize();
        log.trace("Properties init success");
		MessagesProvider.initialize();
        log.trace("Messages init success");
		initVLCLib();
        initializePlayable();
		actionExecutor = new ActionExecutor();
		hoverMouseListener = new HoverMouseListener();
		backgroundFocusListener = new BackgroundFocusListener();
        initializeGui();
        addCloseHook();
        log.trace("Init complete");
	}

    private void initializeGui(){
        SwingUtilities.invokeLater(() -> {
            Font font = new Font("Dialog", Font.BOLD,  12);
            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("TextField.font", new Font("Dialog", Font.BOLD,  14));

            UIManager.put("Label.foreground", Components.MAIN_GRAY_COLOR);
            UIManager.put("Button.foreground", Components.MAIN_GRAY_COLOR);
            UIManager.put("TextField.foreground", Components.MAIN_GRAY_COLOR);

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (Exception e){
                log.warn("l&f failed with msg {}", e.getMessage());
            }

            frame = new Frame();
            actionExecutor.setFrame(frame);
            frame.showQuickNavi();
            frame.run();
            frame.repaintQuickNavi();
        });
    }

    private void addCloseHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                frame.updateCurrentPlayableInCache();
                frame.releasePlayer();
            }
        });
        log.trace("Close hook added");
    }

	private void initVLCLib(){
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PropertyProvider.get(PropertyProvider.SETTING_VLC_PATH));
		try {
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            isVlcAvailable = true;
            log.trace("VLCLib successfully initialized");
		} catch(UnsatisfiedLinkError e){
            isVlcAvailable = false;
            e.printStackTrace();
			waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_VLC_LIBS_FAIL), MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
			log.error("Failed to load vlc libs from specified path {}", PropertyProvider.get(PropertyProvider.SETTING_VLC_PATH));
		} 
	}

	public Map<Integer, Playable> getPlayableCache(){
        if(playableCache == null)
            playableCache = new HashMap<>();
        return playableCache;
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

    public void play(String path){
        play(getOrCreatePlayable(path));
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
            frame.repaintQuickNavi();
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

    /**
     * Call shutdown api
     */
    public void shutdown(final Long minutes, final String param) {
        new Thread(() -> {
            try {
                internalShutdown(minutes, param);
            } catch (IOException e) {
                log.error("Hibernate timer failed", e);
            }
        }).start();
    }

    private void internalShutdown(Long time, String param) throws IOException {
        StringBuilder s = new StringBuilder(SD_CMD_COMMAND);
        if(time != null)
            s.append(SD_CMD_TIME).append(time * 60);
        s.append(param);
        log.debug("Shutdown {} in {} minutes. {}", param, time, s.toString());
        Runtime.getRuntime().exec(s.toString());
    }

    public void hibernate(final int time){
        new Thread(() -> {
            hibernateTimer = new java.util.Timer();
            hibernateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    shutdown(null, SD_CMD_HIBERNATE);
                }
            }, time * 60000);
        }).start();
    }

    public void cancelShutdown(){
        try {
            Runtime.getRuntime().exec(SD_CMD_CANCEL);
            if(hibernateTimer != null)
                hibernateTimer.cancel();
        } catch (IOException e) {
            log.error("Cancel shutdown failed to execute", e);
        }
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
                IOUtil.storeJson(getPlayableCache(), PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH));
                log.debug("Json successfully saved");
            } catch (IOException e) {
                log.error("Failed to save playable to {}, msg {}", PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH), e);
            }
        }).start();
    }

    private void initializePlayable(){
        new Thread(() -> {
            log.trace("Init playable cache");
            String path = PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH);
            if(StringUtil.notSet(path)){
                log.warn("Path to qn data file is not set");
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_NOT_SPECIFIED),
                        MessagesProvider.get(LocalizedMessages.WARN_TITLE), JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                checkOrCreateFile(path);
            } catch (IOException e) {
                log.error("Failed to create QuickNavi data file at {}", path);
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_CREATE_QN_FILE_FAIL),
                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                playableCache = IOUtil.jsonFromFile(path, IOUtil.DEFAULT_TYPE_TOKEN);
            } catch (IOException e) {
                log.error("Failed to read playableCache data from {} with default type token. Message: {}",
                        PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH), e.getMessage());
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_IO_FAIL),
                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            } catch (JsonSyntaxException e){
                log.error("JSON syntax error. Message: {}", e.getMessage());
                waitAndShowMessage(MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_CORRUPTED),
                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            }
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

    private void checkOrCreateFile(String path) throws IOException {
        File f = new File(path);
        if(!f.exists()) {
            if(!f.createNewFile())
                throw new IOException();
            else
                log.debug("File {} has been created", path);
        }
    }

    public static void main(final String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        Application.getInstance().initialize();
    }

}
