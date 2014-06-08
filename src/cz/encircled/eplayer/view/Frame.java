package cz.encircled.eplayer.view;

import com.alee.laf.tabbedpane.WebTabbedPane;
import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.view.actions.ActionCommands;
import cz.encircled.eplayer.view.componensts.PlayerControls;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import cz.encircled.eplayer.view.componensts.WrapLayout;
import cz.encircled.eplayer.view.listeners.KeyDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.TrackDescription;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Frame extends JFrame implements Runnable {

    private static final String TITLE = "EPlayer";
    private static final int MEDIA_PARSED_STATUS = 9;

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private EmbeddedMediaPlayer player;

    private PlayerControls playerControls;

    private JPanel wrapper;

    private JPanel naviPanel;

    private JMenuBar jMenuBar;

    private JMenu spuMenu;

    private WebTabbedPane tabs;

    @Nullable
    private String current;

    private long currentTime;

    private volatile int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    private final static Logger log = LogManager.getLogger(Frame.class);

    private final Application app;

    public Frame(Application app) {
        this.app = app;
        initialize();
        initializeWrapper();
        initializeMenu();
        initializeQuickNavi();
        if(app.isVlcAvailable())
            initializePlayer();
        initializeTabs();
        initializeHotKeys();
        wrapper.add(tabs);
    }

    @Override
    public void run() {
        setVisible(true);
    }

    public void play(@NotNull String path, long time){
        if(!showPlayerInternal()){
            app.showMessage(LocalizedMessages.MSG_VLC_LIBS_FAIL, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!path.equals(current)){
            log.debug("Playing new path {}", path);
        	current = path;
        	player.prepareMedia(path);
        }
        player.start();
        setSubtitlesToMenu(player.getSpuDescriptions());
        player.setTime(Math.min(time, player.getLength()));
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
            spuMenu.setEnabled(false);
        }
    }

    public void updateCurrentPlayableInCache(){
        if(current != null)
            app.updatePlayableCache(current.hashCode(), currentTime);
    }

    public void togglePlayer() {
        if(player != null && current != null)
            player.pause();
    }

    public void showQuickNavi(){
        SwingUtilities.invokeLater(() -> {
            if(wrapperState != QUICK_NAVI_STATE){
                updateCurrentPlayableInCache();
                if(player != null && player.isFullScreen())
                    exitFullScreen();
                stopPlayer();
                current = null;
                wrapperState = QUICK_NAVI_STATE;

                tabs.setVisible(true);
                wrapper.repaint();
                setTitle(TITLE);
                repaintQuickNavi(); // TODO swing invoke
            }
        });

    }

    public void showShutdownTimeChooser(){
        ShutdownChooserDialog chooserDialog = new ShutdownChooserDialog(this);
        log.debug("result: {}, {}", chooserDialog.getTime(), chooserDialog.getShutdownParam());
//        if(chooserDialog.getTime() != null)
//            app.shutdown(chooserDialog.getTime(), chooserDialog.getShutdownParam()); TODO
    }

    private boolean showPlayerInternal(){
        if(!app.isVlcAvailable() || player == null)
            return false;
        if(wrapperState != PLAYER_STATE){
            log.debug("Add player to frame");
            wrapperState = PLAYER_STATE;
            tabs.setVisible(false);
            wrapper.add(mediaPlayerComponent, BorderLayout.CENTER); // TODO or not
            wrapper.add(playerControls, BorderLayout.SOUTH);
        }
        return true;
    }

    public void repaintQuickNavi(){
        final List<QuickNaviButton> naviButtons = new ArrayList<>();
        app.getPlayableCache().forEach((value) -> naviButtons.add(new QuickNaviButton(app, value)));

        SwingUtilities.invokeLater(() -> {
            naviPanel.removeAll();
            naviPanel.revalidate();
            naviButtons.forEach(naviPanel::add);
            naviPanel.repaint(); // TODO check if we need this
        });
    }

    public void enterFullScreen(){
        setCursor(blankCursor);
        setJMenuBar(null);
        playerControls.setVisible(false);
        player.setFullScreen(true);
    }

    public void exitFullScreen(){
        setCursor(Cursor.getDefaultCursor());
        setJMenuBar(jMenuBar);
        playerControls.setVisible(true);
        player.setFullScreen(false);
    }

    public void toggleFullScreen() {
        if(player != null && current != null){
            if(player.isFullScreen())
                exitFullScreen();
            else
                enterFullScreen();
        }
    }

    public boolean isQuickNaviState(){
        return wrapperState == QUICK_NAVI_STATE;
    }

    public boolean isPlayerState(){
        return wrapperState == PLAYER_STATE;
    }

    public boolean isFullScreen(){
        return player.isFullScreen();
    }

    private void initialize(){
        setTitle(TITLE);
        setMinimumSize(new Dimension(1130, 700));
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
                    if(player.isFullScreen())
                        setCursor(Cursor.getDefaultCursor());
                } else{
                    playerControls.setVisible(false);
                    if(player.isFullScreen())
                        setCursor(blankCursor);
                }
            }
        });
    }

    private void initializePlayer(){
        try {
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
                @Override
                protected FullScreenStrategy onGetFullScreenStrategy() {
                    return new Win32FullScreenStrategy(Frame.this);
                }
            };
            player = mediaPlayerComponent.getMediaPlayer();
            player.setEnableMouseInputHandling(false);
            player.setEnableKeyInputHandling(false);

            mediaPlayerComponent.getVideoSurface().addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(@NotNull MouseEvent e) {
                    if(e.getClickCount() == Constants.ONE){
                        togglePlayer();
                    } else {
                        togglePlayer();
                        toggleFullScreen();
                    }
                }
            });

            player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    app.updatePlayableCache(current.hashCode(), Constants.ZERO_LONG);
                    current = null;
                    stopPlayer();
                    showQuickNavi();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    currentTime = newTime;
                    playerControls.fireTimeChanged(newTime);
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    app.showMessage(LocalizedMessages.FILE_OPEN_FAILED, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    if(current != null) {
                        app.deletePlayable(current.hashCode());
                        current = null;
                    }
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
        JMenu mediaMenu = new JMenu(MessagesProvider.get(LocalizedMessages.MEDIA));

        spuMenu = new JMenu("SPU"); // TODO
        spuMenu.setEnabled(false);

        autoShutdown.add(Components.getMenuItem(LocalizedMessages.AUTO_SHUTDOWN, ActionCommands.SHUTDOWN_TIME_CHOOSER));

        tools.add(Components.getMenuItem(LocalizedMessages.OPEN_QUICK_NAVI, ActionCommands.OPEN_QUICK_NAVI));
        tools.add(Components.getMenuItem(LocalizedMessages.SETTINGS, ActionCommands.SETTINGS));
        tools.add(autoShutdown);

        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.OPEN, ActionCommands.OPEN));
        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.EXIT, ActionCommands.EXIT));
        file.add(new JSeparator());

        mediaMenu.add(spuMenu);
        mediaMenu.add(new JSeparator());

        setJMenuBar(jMenuBar);
        jMenuBar.add(file);
        jMenuBar.add(mediaMenu);
        jMenuBar.add(tools);
    }

    private void initializeTabs(){
        tabs = new WebTabbedPane();
        JPanel test = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        app.getTest().values().forEach(p -> test.add(new QuickNaviButton(app, p, false)));
        tabs.add(new JScrollPane(naviPanel), "Navi");
        tabs.add(new JScrollPane(test), "Video");
    }

    private void setSubtitlesToMenu(List<TrackDescription> spuDescriptions){
        log.debug("Set subtitles, count {}", spuDescriptions.size());
        spuMenu.removeAll();
        spuDescriptions.forEach((desc) -> {
            JMenuItem subtitle = new JMenuItem(desc.description());
            subtitle.addActionListener(e -> player.setSpu(desc.id()));
            spuMenu.add(subtitle);
        });
        spuMenu.setEnabled(spuDescriptions.size() > Constants.ZERO);
        spuMenu.revalidate();
    }

    private void initializeQuickNavi(){
        naviPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
    }

    private void initializeHotKeys(){
        // TODO frame dependency
        KeyDispatcher dispatcher = new KeyDispatcher(app);
        dispatcher.bind(KeyEvent.VK_ENTER, ActionCommands.PLAY_LAST);
        dispatcher.bind(KeyEvent.VK_SPACE, ActionCommands.TOGGLE_PLAYER);
        dispatcher.bind(KeyEvent.VK_ESCAPE, ActionCommands.CANCEL);
        dispatcher.bind(KeyEvent.VK_C, ActionCommands.PLAY_LAST);
        dispatcher.bind(KeyEvent.VK_Q, ActionCommands.EXIT);
        dispatcher.bind(KeyEvent.VK_O, ActionCommands.OPEN);
        dispatcher.bind(KeyEvent.VK_N, ActionCommands.OPEN_QUICK_NAVI);
        dispatcher.bind(KeyEvent.VK_F, ActionCommands.TOGGLE_FULL_SCREEN);
        dispatcher.bind(KeyEvent.VK_S, ActionCommands.SETTINGS);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

}
