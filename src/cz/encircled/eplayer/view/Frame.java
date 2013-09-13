package cz.encircled.eplayer.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;

import cz.encircled.eplayer.view.componensts.PlayerControls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.app.LocalizedMessages;
import cz.encircled.eplayer.app.MessagesProvider;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

public class Frame extends JFrame implements Runnable {

    private static final long serialVersionUID = 1L;

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private EmbeddedMediaPlayer player;

    private PlayerControls playerControls;

    private JPanel wrapper;

    private JPanel naviPanel;

    private boolean isFullScreen = false;

    private String current;

    private volatile int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;


    private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    private JMenuBar jMenuBar;

    private final static Logger log = LogManager.getLogger(Frame.class);

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
        initializePlayer();
        showPlayer();

        pack();
    }

    public final void play(String path){
        play(path, 0L);
    }

    public final void play(String path, long time){
        showPlayerInternal();
        if(player.isPlaying())
            stopPlayer();
        current = path;
        player.prepareMedia("file:///" + path, String.format(":start-time=%d", time));
        player.start();
        playerControls.reinitialize();
    }

    public void releasePlayer(){
        if(player != null){
            player.stop();
            player.release();
        }
    }

    public void stopPlayer(){
        if(player != null){
            player.stop();
        }
    }

    public void showQuickNavi(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stopPlayer();
                if(wrapperState != QUICK_NAVI_STATE){
                    wrapperState = QUICK_NAVI_STATE;
                    wrapper.removeAll();
                    wrapper.add(naviPanel, BorderLayout.CENTER);
                    wrapper.repaint();
                }
            }
        });

    }

    public void showPlayer(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showPlayerInternal();
            }
        });
    }

    private void showPlayerInternal(){
        if(wrapperState != PLAYER_STATE){
            wrapperState = PLAYER_STATE;
            wrapper.removeAll();
            wrapper.add(mediaPlayerComponent, BorderLayout.CENTER);
            wrapper.add(playerControls, BorderLayout.SOUTH);
            wrapper.repaint();
        }
    }

    public void fullScreen(){
        final long time = player.getTime()/1000;
        player.stop();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dispose();
                setUndecorated(true);
                setVisible(true);
                toFront();
                setJMenuBar(null);
                play(current, time);
                playerControls.setVisible(false);
                setCursor(blankCursor);
                isFullScreen = true;
            }
        });
    }

    public void exitFullScreen(){
        final long time = player.getTime();
        player.stop();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dispose();
                setUndecorated(false);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setVisible(true);
                setCursor(Cursor.getDefaultCursor());
                play(current, time / 1000);
                setJMenuBar(jMenuBar);
                playerControls.setVisible(true);
                toFront();
                isFullScreen = false;
            }
        });
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
//                play("D:\\video\\boss2\\test.mkv");
            }
        });
    }

    private void initialize(){
        setTitle("EPlayer");
        setPreferredSize(new Dimension(1000, 700));
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
    }

    private void initializeWrapper(){
        wrapper = new JPanel(new BorderLayout());
        getContentPane().add(wrapper, BorderLayout.CENTER);
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if(playerControls != null){
                    if(e.getWheelRotation() < 0){
                        playerControls.setVisible(true);
                        if(isFullScreen)
                            setCursor(Cursor.getDefaultCursor());
                    } else{
                        playerControls.setVisible(false);
                        if(isFullScreen)
                            setCursor(blankCursor);
                    }
                }
            }
        });
    }

    private void initializePlayer(){
        try {
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            player = mediaPlayerComponent.getMediaPlayer();
            player.setEnableMouseInputHandling(false);
            player.setEnableKeyInputHandling(false);

            mediaPlayerComponent.getVideoSurface().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if(e.getClickCount() == 1){
                        if(player.isPlaying())
                            player.pause();
                        else
                            player.start();
                    } else {
                        if(isFullScreen)
                            exitFullScreen();
                        else
                            fullScreen();
                    }
                }
            });

            player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {

                private Long lastTime = -500L;

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    super.timeChanged(mediaPlayer, newTime);
                    if (Math.abs(newTime - lastTime) > 500) {
                        playerControls.fireTimeChanged(newTime);
                        lastTime = newTime;
                    }
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    JOptionPane.showMessageDialog(Frame.this, "Player error", "Error", JOptionPane.ERROR_MESSAGE);
                }

            });

            playerControls = new PlayerControls(player);
        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(Frame.this, "VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void initializeMenu(){
        jMenuBar = new JMenuBar();
        JMenu file = new JMenu(MessagesProvider.get(LocalizedMessages.FILE));
        JMenu tools = new JMenu(MessagesProvider.get(LocalizedMessages.TOOLS));

        tools.add(Components.getMenuItem(LocalizedMessages.OPEN_QUICK_NAVI, ActionCommands.OPEN_QUICK_NAVI));
        tools.add(Components.getMenuItem(LocalizedMessages.SETTINGS, ActionCommands.SETTINGS));

        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.OPEN, ActionCommands.OPEN));
        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.EXIT, ActionCommands.EXIT));
        file.add(new JSeparator());

        setJMenuBar(jMenuBar);
        jMenuBar.add(file);
        jMenuBar.add(tools);
    }



    private final void initializeQuickNavi(){
        naviPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        naviPanel.setBackground(Color.WHITE);

        Map<Integer, Playable> data = Application.getInstance().getPlayable();
        for(Entry<Integer, Playable> e : data.entrySet()){
            Playable p = e.getValue();
            naviPanel.add(new QuickNaviButton(p));
        }
    }

}