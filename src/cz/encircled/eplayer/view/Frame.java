package cz.encircled.eplayer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.ws.buffermgmt.impl.MessageConstants;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.app.LocalizedMessages;
import cz.encircled.eplayer.app.MessagesProvider;
import cz.encircled.eplayer.app.PropertyProvider;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.view.componensts.PlayerControlsPanel;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.TrackType;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Frame extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	private JPanel wrapper;
	
	private final static Logger log = LogManager.getLogger(PropertyProvider.class);
	
	
	public void stopPlayer(){
		if(mediaPlayerComponent != null){
			mediaPlayerComponent.getMediaPlayer().stop();
			mediaPlayerComponent.getMediaPlayer().release();
		} 
	}
	
    public Frame() {
    	try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
			Font font = new Font("Dialog", Font.BOLD,  12); 

			UIManager.put("Label.font", font);
			UIManager.put("Button.font", font);
			UIManager.put("TextField.font", new Font("Dialog", Font.BOLD,  14));
			
			UIManager.put("Label.foreground", Components.MAIN_GRAY_COLOR);
			UIManager.put("Button.foreground", Components.MAIN_GRAY_COLOR);
			UIManager.put("TextField.foreground", Components.MAIN_GRAY_COLOR);
			
		} 
	    catch (Exception e){
	    	e.printStackTrace();
	    }    	
    	initialize();
    	initializeWrapper();
    	initializeMenu();
    	initializeQuickNavi();
//    	initializePlayer();
        
//    	setUndecorated(true);  
    }
    
    private final void initialize(){
		 setTitle("EPlayer");
//         setSize(1050, 600);
         setExtendedState(Frame.MAXIMIZED_BOTH);  
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);
    }
    
    private final void initializeWrapper(){
    	wrapper = new JPanel();
    	wrapper.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
    	getContentPane().add(wrapper);
//    	wrapper.setPreferredSize(wrapper.getParent());
    }
    
    public final void initializePlayer(){
    	try {
			 mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
			 mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			     @Override
			     public void error(MediaPlayer mediaPlayer) {
			         System.out.println("error");
			     };
			 });  
			 mediaPlayerComponent.setPreferredSize(new Dimension(1000, 200));
			 wrapper.removeAll();
			 wrapper.setLayout(new BorderLayout());
			 wrapper.add(mediaPlayerComponent, BorderLayout.NORTH);
			 
			 JPanel b = new PlayerControlsPanel(mediaPlayerComponent.getMediaPlayer());
			 b.setPreferredSize(new Dimension(1000, 200));
			 wrapper.add(b, BorderLayout.SOUTH);
		 } catch(Exception e){
			 e.printStackTrace();
			JOptionPane.showMessageDialog(Frame.this, "VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
		 }    	
    }
    
    public final void play(String path){
		mediaPlayerComponent.getMediaPlayer().prepareMedia("file:///" + path);
		int i = 0;
		while(!mediaPlayerComponent.getMediaPlayer().start()){
			log.debug("not ready {}", i++);
		};    	
		repaint();
    }
    
    private final void initializeMenu(){
    	JMenuBar bar = new JMenuBar();
    	JMenu file = new JMenu(MessagesProvider.get(LocalizedMessages.FILE));
    	JMenu tools = new JMenu(MessagesProvider.get(LocalizedMessages.TOOLS));

    	JMenuItem exitItem = Components.getMenuItem(LocalizedMessages.EXIT, ActionCommands.EXIT);
    	JMenuItem settingsItem = Components.getMenuItem(LocalizedMessages.SETTINGS, ActionCommands.SETTINGS);
    	JMenuItem openItem = Components.getMenuItem(LocalizedMessages.OPEN, ActionCommands.OPEN);     	
    	
    	tools.add(settingsItem);
    	file.add(new JSeparator());
    	file.add(openItem);
    	file.add(new JSeparator());
    	file.add(exitItem);
    	file.add(new JSeparator());    	
    	setJMenuBar(bar);
    	bar.add(file);
    	bar.add(tools);
    }
    
    private final void initializeQuickNavi(){
    	JPanel naviPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
    	naviPanel.setBackground(Color.WHITE);
    	
    	Map<Integer, Playable> data = Application.getInstance().getPlayable();
    	for(Entry<Integer, Playable> e : data.entrySet()){
    		Playable p = e.getValue();
    		naviPanel.add(new QuickNaviButton(p));
    	}
    	wrapper.removeAll();
    	wrapper.add(naviPanel);
    }

	@Override
	public void run() {
		setVisible(true);
//		":start-time=20"
		/*
		mediaPlayerComponent.getMediaPlayer().prepareMedia("file:///C:/software/mb/Wildlife.wmv");
		while(!mediaPlayerComponent.getMediaPlayer().start());
		
		EmbeddedMediaPlayer p = mediaPlayerComponent.getMediaPlayer();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					log.debug("REAL TIME IS : {}",mediaPlayerComponent.getMediaPlayer().getTime());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start(); */
		/*
		 JSlider positionSlider = new JSlider();
		 positionSlider.setMinimum(0);
         positionSlider.setMaximum((int)p.getLength()/1000);
         positionSlider.setValue(0);
         positionSlider.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				JSlider s = (JSlider)e.getComponent();
				int v = s.getValue();
				mediaPlayerComponent.getMediaPlayer().setTime(v * 1000);
				log.debug("RELEASE TIME : {}", mediaPlayerComponent.getMediaPlayer().getTime());
				mediaPlayerComponent.getMediaPlayer().start();
				mediaPlayerComponent.getMediaPlayer().setTime(v * 1000);
				log.debug("RELEASE TIME2 : {}", mediaPlayerComponent.getMediaPlayer().getTime());
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
                try {
                    // Half a second probably gets an iframe
                    Thread.sleep(500);
                }
                catch(InterruptedException es) {
                    // Don't care if unblocked early
                }
				mediaPlayerComponent.getMediaPlayer().stop();
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		getContentPane().add(positionSlider);*/
		repaint();
		
	}
    
}