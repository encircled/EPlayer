package cz.encircled.eplayer.app;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.*;

import com.google.gson.JsonSyntaxException;

import cz.encircled.eplayer.view.*;
import cz.encircled.eplayer.view.Frame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.StringUtil;


public class Application {

	private static volatile Application instance;
	
	private final static Logger log = LogManager.getLogger(Application.class);
	
	private Map<Integer, Playable> playableCache;

	private ActionExecutor actionExecutor;
	
	private HoverMouseListener hoverMouseListener;
	
	private BackgroundFocusListener backgroundFocusListener;
	
	private MouseAdapter fileChooserMouseAdapter = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(SwingUtilities.getRoot(e.getComponent())) == JFileChooser.APPROVE_OPTION) 
                ((JTextField)e.getSource()).setText(fc.getSelectedFile().getAbsolutePath());
		}
	};

    private ActionListener defaultActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            getActionExecutor().execute(e.getActionCommand());
        }
    };
	
	private Frame frame;

    private volatile boolean isVlcAvailable = false;
	
	public static Application getInstance(){
		Application local = instance;
		if(local == null){
			synchronized (Application.class) {
				local = instance;
				if(local == null)
					instance = local = new Application();
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

    public boolean isVlcAvailable(){
        return isVlcAvailable;
    }
    
    public MouseAdapter getFileChooserMouseAdapter(){
    	return fileChooserMouseAdapter;
    }
    
	public void initialize(){
		log.trace("Application initialization");
		if(frame != null){
            frame.stopPlayer();
			frame.dispose();
		}
		PropertyProvider.initialize();
		log.trace("Properties initialized");
		MessagesProvider.initialize();
		log.trace("Messages initialized");
		initVLCLib();		
        initializePlayable();
		actionExecutor = new ActionExecutor();
		hoverMouseListener = new HoverMouseListener();
		backgroundFocusListener = new BackgroundFocusListener();
		initializeGui();
	}
	
	private void initializeGui(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
            frame.run();
            frame.showQuickNavi();
            }
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
			JOptionPane.showMessageDialog(frame, MessagesProvider.get(LocalizedMessages.MSG_VLC_LIBS_FAIL), MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
			log.error("Failed to load vlc libs from specified path {}", PropertyProvider.get(PropertyProvider.SETTING_VLC_PATH));
		} 
	}
	
	public Map<Integer, Playable> getPlayableCache(){
        return playableCache;
	}

	
	public void play(Playable p){
		log.debug("play: {}", p.toString());
		if(p.exists()){
			frame.showPlayer();
			frame.play(p.getPath(), p.getTime());
		} else {
			JOptionPane.showMessageDialog(frame, "DOMERGE", "Title", JOptionPane.INFORMATION_MESSAGE);
		}
    }


    public Playable getOrCreatePlayable(String path){
        int hash = path.hashCode();
        Playable p = playableCache.get(hash);
        if(p == null){
            p = new Playable(path);
            playableCache.put(hash, p);
        }
        return p;
    }

    public void play(String path){
        play(getOrCreatePlayable(path));
    }

    public void updatePlayableCache(int hash, long time){
        Playable p = playableCache.get(hash);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        savePlayable();
    }

    public void deletePlayableCache(int hash){
        playableCache.remove(hash);
        frame.repaintQuickNavi();
        savePlayable();
    }
    
    public void mergePlayable(Playable playable, String path){
    	playableCache.remove(playable.hashCode());
    	playable.readPath(path);
        frame.repaintQuickNavi();
        savePlayable();
    }

    public void playLast(){
        Playable p = getLastPlayable();
        if(p != null)
            play(p);
    }

    private Playable getLastPlayable(){
        Playable p = null;
        for(Map.Entry<Integer, Playable> e : playableCache.entrySet()){
            if(p == null || p.getWatchDate() < e.getValue().getWatchDate())
                p = e.getValue();
        }
        return p;
    }

    public void exit() {
        System.exit(0);
    }

    /**
     * Async save playable map to file in JSON format
     */
    public synchronized void savePlayable(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IOUtil.storeJson(playableCache, PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH));
                } catch (IOException e) {
                    log.error("Failed to save playable to {}, msg {}", PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH), e);
                }
            }
        }).start();
    }

    private void initializePlayable(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                playableCache = new HashMap<Integer, Playable>();
                String path = PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH);
                if(StringUtil.notSet(path)){
                	log.warn("Path to qn data file is not set");
                	JOptionPane.showMessageDialog(frame, MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_NOT_SPECIFIED),
                            MessagesProvider.get(LocalizedMessages.WARN_TITLE), JOptionPane.WARNING_MESSAGE);
                	return;
                }
                
                File f = new File(path);
                if(!f.exists()) {
                    try {
                        if(!f.createNewFile())
                            throw new IOException();
                    } catch (IOException e) {
                        log.error("Failed to create QuickNavi data file at {}", path);
                        JOptionPane.showMessageDialog(frame, MessagesProvider.get(LocalizedMessages.MSG_CREATE_QN_FILE_FAIL),
                                                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
                try {
                    playableCache = IOUtil.jsonFromFile(path, IOUtil.DEFAULT_TYPE_TOKEN);
                    if(playableCache == null)
                    	playableCache = new HashMap<Integer, Playable>();
                } catch (IOException e) {
                	playableCache = new HashMap<Integer, Playable>();
                    log.error("Failed to read playableCache data from {} with default type token. Message: {}",
                            PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH), e.getMessage());
                    JOptionPane.showMessageDialog(frame, MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_IO_FAIL),
                                                        MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
                } catch (JsonSyntaxException e){
                	playableCache = new HashMap<Integer, Playable>();
                    log.error("JSON syntax error. Message: {}", e.getMessage());
                    JOptionPane.showMessageDialog(frame, MessagesProvider.get(LocalizedMessages.MSG_QN_FILE_CORRUPTED),
                                                    MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();

    }

    public static void main(final String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        Application.getInstance().initialize();
        Application.getInstance().addCloseHook();
    }

}
