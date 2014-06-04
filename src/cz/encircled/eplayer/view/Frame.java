package cz.encircled.eplayer.view;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.util.KeyConstants;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.view.actions.ActionCommands;
import cz.encircled.eplayer.view.componensts.PlayerControls;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Frame extends JFrame implements Runnable {

    private static final String TITLE = "EPlayer";

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private EmbeddedMediaPlayer player;

    private PlayerControls playerControls;

    private JPanel wrapper;

    private JPanel naviPanel;

    private JMenuBar jMenuBar;

    private boolean isFullScreen = false;

    private String current;

    private long currentTime;

    private volatile int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    private final static Logger log = LogManager.getLogger(Frame.class);

    public Frame() {
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
        }
        catch (Exception e){
            log.error("l&f failed with msg {}", e.getMessage());
        }
        initialize();
        initializeWrapper();
        initializeMenu();
        initializeQuickNavi();
        if(Application.getInstance().isVlcAvailable())
            initializePlayer();
        initializeHotKeys();
        player.setSubTitleFile(new File(""));// TODO
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    public synchronized void play(@NotNull String path, long time){
        if(!showPlayerInternal()){
            JOptionPane.showMessageDialog(Frame.this, MessagesProvider.get(LocalizedMessages.MSG_VLC_LIBS_FAIL),
                                            MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!path.equals(current)){
        	current = path;
        	player.prepareMedia(new File(path).toURI().toASCIIString().replaceFirst("file:/", "file:///"));
        }
        player.start();
        player.setTime(Math.min(time, player.getLength() - 1000));
        playerControls.reinitialize();
    }

    public void releasePlayer(){
        if(player != null){
            player.stop();
            player.release();
            current = null;
        }
    }

    public void stopPlayer(){
        if(player != null){
            player.stop();
        }
    }

    public void updateCurrentPlayableInCache(){
        if(current != null){
            if(currentTime > 0L)
                Application.getInstance().updatePlayableCache(current.hashCode(), currentTime);
            else
                log.warn("Current time is negative: {}", currentTime);
        }
    }

    public void pausePlayer(){
            player.pause();
    }

    public void togglePlayer() {
        if(player != null && current != null){
            if(player.isPlaying())
                pausePlayer();
            else
                play(current, currentTime);
        }
    }

    public void showQuickNavi(){
        SwingUtilities.invokeLater(() -> {
            if(wrapperState != QUICK_NAVI_STATE){
                updateCurrentPlayableInCache();
                if(isFullScreen)
                    exitFullScreenInternal(false);
                else
                    stopPlayer();
                current = null;
                wrapperState = QUICK_NAVI_STATE;
                wrapper.removeAll();
                wrapper.add(naviPanel, BorderLayout.CENTER);
                wrapper.repaint();
                setTitle(TITLE);
                repaintQuickNavi();
            }
        });

    }

    public void showShutdownTimeChooser(){
        ShutdownChooserDialog chooserDialog = new ShutdownChooserDialog(this);
        log.debug("result: {}, {}", chooserDialog.getTime(), chooserDialog.getShutdownParam());
        if(chooserDialog.getTime() != null)
            Application.getInstance().shutdown(chooserDialog.getTime(), chooserDialog.getShutdownParam());
    }

    private boolean showPlayerInternal(){
        if(!Application.getInstance().isVlcAvailable())
            return false;
        if(wrapperState != PLAYER_STATE){
            log.debug("Add player to frame");
            wrapperState = PLAYER_STATE;
            wrapper.removeAll();
            wrapper.add(mediaPlayerComponent, BorderLayout.CENTER);
            wrapper.add(playerControls, BorderLayout.SOUTH);
            wrapper.repaint();
            jMenuBar.repaint();
        }
        return true;
    }

    public void repaintQuickNavi(){
        final java.util.List<QuickNaviButton> naviButtons = new ArrayList<>();
        Map<Integer, Playable> data = Application.getInstance().getPlayableCache();
        data.values().forEach((value) -> naviButtons.add(new QuickNaviButton(value)));

        SwingUtilities.invokeLater(() -> {
            naviPanel.removeAll();
            naviPanel.revalidate();
            naviButtons.forEach(naviPanel::add);
            naviPanel.repaint();
        });

    }

    public void fullScreen(){
        stopPlayer();

        SwingUtilities.invokeLater(() -> {
            dispose();
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
            setVisible(true);
            toFront();
            setJMenuBar(null);
            playerControls.setVisible(false);
            setCursor(blankCursor);
            wrapper.repaint();
            play(current, currentTime);
            isFullScreen = true;
        });
    }

    public void exitFullScreen(){
        exitFullScreen(player.isPlaying());
    }

    public void exitFullScreen(final boolean continuePlaying){
        SwingUtilities.invokeLater(() -> exitFullScreenInternal(continuePlaying));
    }

    private void exitFullScreenInternal(final boolean continuePlaying){
        stopPlayer();

        SwingUtilities.invokeLater(() -> {
            dispose();
            setUndecorated(false);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            toFront();
            setJMenuBar(jMenuBar);
            playerControls.setVisible(true);
            setCursor(Cursor.getDefaultCursor());
            if(continuePlaying)
                play(current, currentTime);
            isFullScreen = false;
        });
    }

    public void toggleFullScreen() {
        if(player != null && current != null){
            if(isFullScreen)
                exitFullScreen();
            else
                fullScreen();
        }
    }

    public boolean isQuickNaviState(){
        return wrapperState == QUICK_NAVI_STATE;
    }

    public boolean isPlayerState(){
        return wrapperState == PLAYER_STATE;
    }

    public boolean isFullScreen(){
        return isFullScreen;
    }

    private void initialize(){
        setTitle(TITLE);
        setMinimumSize(new Dimension(1000, 700));
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
    }

    private void initializeWrapper(){
        wrapper = new JPanel(new BorderLayout());
        getContentPane().add(wrapper, BorderLayout.CENTER);
        addMouseWheelListener(e -> {
            if(playerControls != null){
                if(e.getWheelRotation() < Constants.ZERO){
                    playerControls.setVisible(true);
                    if(isFullScreen)
                        setCursor(Cursor.getDefaultCursor());
                } else{
                    playerControls.setVisible(false);
                    if(isFullScreen)
                        setCursor(blankCursor);
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
                    if(e.getClickCount() == 1){
                        togglePlayer();
                    } else {
                        togglePlayer();
                        toggleFullScreen(); // TODO
                    }
                }
            });

            player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    super.finished(mediaPlayer);
                    log.debug("FINISHED");
                    Application.getInstance().updatePlayableCache(current.hashCode(), 0L);
                    current = null;
                    showQuickNavi();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    super.timeChanged(mediaPlayer, newTime);
                    currentTime = newTime;
                    playerControls.fireTimeChanged(newTime);
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    JOptionPane.showMessageDialog(Frame.this, "Failed to open " + current, "Error", JOptionPane.ERROR_MESSAGE);
                    Application.getInstance().deletePlayableCache(current.hashCode());
                    current = null;
                    showQuickNavi();
                }

            });
            playerControls = new PlayerControls(player);
        } catch(Exception e){
            log.error("Player initialization failed", e);
            JOptionPane.showMessageDialog(Frame.this, "VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
        } catch (NoClassDefFoundError re){
            log.error("Player initialization failed", re);
        }
    }

    private void initializeMenu(){
        jMenuBar = new JMenuBar();
        JMenu file = new JMenu(MessagesProvider.get(LocalizedMessages.FILE));
        JMenu tools = new JMenu(MessagesProvider.get(LocalizedMessages.TOOLS));
        JMenu autoShutdown = new JMenu(MessagesProvider.get(LocalizedMessages.AUTO_SHUTDOWN));

        autoShutdown.add(Components.getMenuItem(LocalizedMessages.AUTO_SHUTDOWN, ActionCommands.SHUTDOWN_TIME_CHOOSER));

        tools.add(Components.getMenuItem(LocalizedMessages.OPEN_QUICK_NAVI, ActionCommands.OPEN_QUICK_NAVI));
        tools.add(Components.getMenuItem(LocalizedMessages.SETTINGS, ActionCommands.SETTINGS));
        tools.add(autoShutdown);

        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.OPEN, ActionCommands.OPEN));
        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.EXIT, ActionCommands.EXIT));
        file.add(new JSeparator());

        setJMenuBar(jMenuBar);
        jMenuBar.add(file);
        jMenuBar.add(tools);
    }

    private void initializeQuickNavi(){
        naviPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        naviPanel.setBackground(Color.WHITE);
    }

    private void initializeHotKeys() {
        GUIUtil.bindKey(wrapper, null, 'o', ActionCommands.OPEN);
        GUIUtil.bindKey(wrapper, null, 'n', ActionCommands.OPEN_QUICK_NAVI);
        GUIUtil.bindKey(wrapper, null, 'q', ActionCommands.EXIT);
        GUIUtil.bindKey(wrapper, null, 'f', ActionCommands.TOGGLE_FULL_SCREEN);
        GUIUtil.bindKey(wrapper, null, 'c', ActionCommands.PLAY_LAST);
        GUIUtil.bindKey(wrapper, null, 's', ActionCommands.SETTINGS);
        GUIUtil.bindKey(wrapper, KeyConstants.ENTER, null, ActionCommands.PLAY_LAST);
        GUIUtil.bindKey(wrapper, KeyConstants.SPACE, null, ActionCommands.TOGGLE_PLAYER);
        GUIUtil.bindKey(wrapper, KeyConstants.ESCAPE, null, ActionCommands.BACK);
    }

}
