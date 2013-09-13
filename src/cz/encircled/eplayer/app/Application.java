package cz.encircled.eplayer.app;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.google.gson.reflect.TypeToken;
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
	
	public int VLC_PREPEARING = 0;
	
	public int VLC_READY = 1;
	
	public int VLC_ERROR = 2;
	
	private int vlcLibStatus = VLC_PREPEARING;
	
	private ActionsMouseListener actionsMouseListener;
	
	private HoverMouseListener hoverMouseListener;
	
	private BackgroundFocusListener backgroundFocusListener;
	
	private Frame frame;
	
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

    public int getVlcLibStatus(){
    	return vlcLibStatus;
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
			frame.dispose();
		}
		PropertyProvider.initialize();
		MessagesProvider.initialize();
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
            }
        });		
	}
	
	private final void initVLCLib(){
		try {
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PropertyProvider.get(PropertyProvider.SETTING_VLC_PATH));
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);     
			vlcLibStatus = VLC_READY;
		} catch(UnsatisfiedLinkError e){
			vlcLibStatus = VLC_ERROR;
			JOptionPane.showMessageDialog(frame, "VLC FAILED", "Title ", JOptionPane.ERROR_MESSAGE);
			log.error("Failed to load vlc libs from specified path {}", PropertyProvider.get("vlc_path"));
		}
	}
	
	public Map<Integer, Playable> getPlayable(){
		try {
			return IOUtil.jsonFromFile("C:/software/deleteme.json", IOUtil.DEFAULT_TYPE_TOKEN);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}
	
	public void play(Playable p){
		frame.initializePlayer();
		frame.play(p.getPath());
	}
	
    public static void main(final String[] args) {
    	System.setProperty("-Dfile.encoding", "UTF-8");
    	Application.getInstance().initialize();
    }
    
}
