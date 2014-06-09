package cz.encircled.eplayer.view;

import com.alee.laf.tabbedpane.WebTabbedPane;
import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.GUIUtil;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static cz.encircled.eplayer.util.GUIUtil.*;

// TODO swing worker

public class Frame extends JFrame implements Runnable {

    private static final String TITLE = "EPlayer";

    private PlayerControls playerControls;

    private JPanel wrapper;

    private JPanel naviPanel;

    private JMenuBar jMenuBar;

    private JMenu spuMenu;

    private WebTabbedPane tabs;

    private final SwingViewService swingViewService;

    private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    private final static Logger log = LogManager.getLogger(Frame.class);

    public Frame(SwingViewService swingViewService) {
        this.swingViewService = swingViewService;
        initialize();
        initializeWrapper();
        initializeMenu();
        initializeQuickNavi();
        initializeTabs();
        wrapper.add(tabs);
    }

    @Override
    public void run() {
        setVisible(true);
    }

    public void enableSpuMenu(boolean enable){
        spuMenu.setEnabled(enable);
    }

    void showShutdownTimeChooser(){
        ShutdownChooserDialog chooserDialog = new ShutdownChooserDialog(this);
        log.debug("result: {}, {}", chooserDialog.getTime(), chooserDialog.getShutdownParam());
//        if(chooserDialog.getTime() != null)
//            core.shutdown(chooserDialog.getTime(), chooserDialog.getShutdownParam()); TODO
    }


    void repaintQuickNavi(Collection<Playable> playable){
        naviPanel.removeAll();
        naviPanel.revalidate();
        playable.forEach(() -> naviPanel.add(new ));
        naviPanel.repaint(); // TODO check if we need this
    }

    void onMediaTimeChange(long newTime){
        playerControls.fireTimeChanged(newTime);
    }

    void enterFullScreen(){
        setCursor(blankCursor);
        setJMenuBar(null);
        playerControls.setVisible(false);
    }

    public void exitFullScreen(){
        setCursor(Cursor.getDefaultCursor());
        setJMenuBar(jMenuBar);
        playerControls.setVisible(true);
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
                    if(swingViewService.isFullScreen())
                        setCursor(Cursor.getDefaultCursor());
                } else{
                    playerControls.setVisible(false);
                    if(swingViewService.isFullScreen())
                        setCursor(blankCursor);
                }
            }
        });
    }

    private void initializeMenu(){
        jMenuBar = new JMenuBar();
        JMenu file = new JMenu(MessagesProvider.get(LocalizedMessages.FILE));
        JMenu tools = new JMenu(MessagesProvider.get(LocalizedMessages.TOOLS));
        JMenu autoShutdown = new JMenu(MessagesProvider.get(LocalizedMessages.AUTO_SHUTDOWN));
        JMenu mediaMenu = new JMenu(MessagesProvider.get(LocalizedMessages.MEDIA));

        spuMenu = new JMenu(MessagesProvider.get(LocalizedMessages.SUBTITLES));
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
//        new HashMap<>(app.getTest()).values().forEach(p -> test.add(new QuickNaviButton(app, p, false)));
        tabs.add(new JScrollPane(naviPanel), "Navi");
        tabs.add(new JScrollPane(test), "Video");
    }

    private void setSubtitlesToMenu(List<TrackDescription> spuDescriptions){
        log.debug("Set subtitles, count {}", spuDescriptions.size());
        spuMenu.removeAll();
        spuDescriptions.forEach((desc) -> {
            JMenuItem subtitle = new JMenuItem(desc.description());
            subtitle.addActionListener(e -> swingViewService.setSpu(desc.id()));
            spuMenu.add(subtitle);
        });
        spuMenu.setEnabled(spuDescriptions.size() > Constants.ZERO);
        spuMenu.revalidate();
    }

    private void initializeQuickNavi(){
        naviPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
    }

}
