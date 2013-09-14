package cz.encircled.eplayer.app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.view.ActionsMouseListener;
import cz.encircled.eplayer.view.BackgroundFocusListener;
import cz.encircled.eplayer.view.Frame;
import cz.encircled.eplayer.view.HoverMouseListener;


public class Application {

	private static volatile Application instance;
	
	private final static Logger log = LogManager.getLogger(Application.class);
	
	private ActionsMouseListener actionsMouseListener;
	
	private HoverMouseListener hoverMouseListener;
	
	private BackgroundFocusListener backgroundFocusListener;

    private Map<Integer, Playable> playableCache;
	
	private Frame frame;

    private volatile boolean isSaving = false;
	
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

    public ActionsMouseListener getActionsMouseListener(){
    	return actionsMouseListener;
    }
    
    public HoverMouseListener getHoverMouseListener(){
    	return hoverMouseListener;
    }
    
    public BackgroundFocusListener getBackgroundFocusListener(){
    	return backgroundFocusListener;
    }
	
	private Application(){

	}
	
	public final void initialize(){
		if(frame != null){
            frame.releasePlayer();
			frame.dispose();
		}
		PropertyProvider.initialize();
		MessagesProvider.initialize();
        initializePlayable();
		initVLCLib();
		actionsMouseListener = new ActionsMouseListener();
		hoverMouseListener = new HoverMouseListener();
		backgroundFocusListener = new BackgroundFocusListener();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame = new Frame();
                actionsMouseListener.setFrame(frame);
                frame.run();
                frame.showQuickNavi();
            }
        });
        addCloseHook();
	}

    private void addCloseHook(){
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                frame.updateCurrentPlayableInCache();
                frame.releasePlayer();
                int c = 0;
                while(isSaving && c++ < 30){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });
    }

	private void initVLCLib(){
		try {
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PropertyProvider.get(PropertyProvider.SETTING_VLC_PATH));
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);     
		} catch(UnsatisfiedLinkError e){
            e.printStackTrace();
			JOptionPane.showMessageDialog(frame, LocalizedMessages.MSG_VLC_LIBS_FAIL, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
			log.error("Failed to load vlc libs from specified path {}", PropertyProvider.get(PropertyProvider.SETTING_VLC_PATH));
		}
	}
	
	public Map<Integer, Playable> getPlayableCache(){
        return playableCache;
	}

	
	public void play(Playable p){
		frame.showPlayer();
		frame.play(p.getPath(), p.getTime());
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

    public void showQuickNavi(){
        frame.showQuickNavi();
    }

    public void updatePlayableCache(int hash, int time){
        playableCache.get(hash).setTime(time);
        savePlayable();
    }

    public void deletePlayableCache(int hash){
        playableCache.remove(hash);
        frame.repaintQuickNavi();
        savePlayable();
    }

    public void exit() {
        System.exit(0);
    }

    public synchronized void savePlayable(){
        isSaving = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IOUtil.storeJson(playableCache, PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH));
                } catch (IOException e) {
                    log.error("Failed to save playable to {}", PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH));
                }
                isSaving = false;
            }
        }).start();
    }

    private void initializePlayable(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                playableCache = new HashMap<Integer, Playable>();
                String path = PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH);
                File f = new File(path);
                if(!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        log.error("Failed to create QuickNavi data file at {}", path);
                        JOptionPane.showMessageDialog(frame, LocalizedMessages.MSG_CREATE_QN_FILE_FAIL, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    }
                }
                try {
                    playableCache = IOUtil.jsonFromFile(path, IOUtil.DEFAULT_TYPE_TOKEN);
                } catch (IOException e) {
                    log.error("Failed to read playableCache data from {} with default type token. Message: {}",
                            PropertyProvider.get(PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH), e.getMessage());
                    JOptionPane.showMessageDialog(frame, LocalizedMessages.MSG_QN_FILE_IO_FAIL, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                } catch (JsonSyntaxException e){
                    log.error("JSON syntax error. Message: {}", e.getMessage());
                    JOptionPane.showMessageDialog(frame, LocalizedMessages.MSG_QN_FILE_CORRUPTED, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();

    }

    public static void main(final String[] args) {
        System.setProperty("-Dfile.encoding", "UTF-8");
        Application.getInstance().initialize();
    }



}
