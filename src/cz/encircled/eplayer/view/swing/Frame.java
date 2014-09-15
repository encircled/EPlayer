package cz.encircled.eplayer.view.swing;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.gui.FromGuiViewService;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.dnd.DndHandler;
import cz.encircled.eplayer.view.swing.componensts.PlayerControls;
import cz.encircled.eplayer.view.swing.componensts.QuickNaviButton;
import cz.encircled.eplayer.view.swing.componensts.WrapLayout;
import cz.encircled.eplayer.view.swing.listeners.KeyDispatcher;
import cz.encircled.eplayer.view.swing.menu.MenuBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static cz.encircled.eplayer.common.Constants.*;
import static cz.encircled.eplayer.service.action.ActionCommands.*;
import static cz.encircled.eplayer.view.swing.listeners.KeyDispatcher.*;
import static java.awt.event.KeyEvent.*;

// TODO media groups
public class Frame extends JFrame {

    private static final String TITLE = "EPlayer";

    private static final int SCROLL_BAR_SPEED = 16;

    private JPanel wrapper;

    private FolderTab naviTab;

    private JMenuBar jMenuBar;

    private JMenuItem spuMenu;

    private JMenuItem audioTracksMenu;

    private JTextField filterInput;

    private JTabbedPane tabs;

    @Resource
    private MenuBuilder menuBuilder;

    @Resource
    private KeyDispatcher keyEventDispatcher;

    @Resource
    private PlayerControls playerControls;

    @Resource
    private MediaService mediaService;

    @Resource
    private DndHandler dndHandler;

    @Resource
    private FromGuiViewService fromGuiViewService;

    @Resource
    private Settings settings;

    private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    private Map<String, FolderTab> folderTabs = new HashMap<>();

    private final static Logger log = LogManager.getLogger(Frame.class);

    private JFileChooser mediaFileChooser;

    void showPlayer() {
        tabs.setVisible(false);
        wrapper.add(mediaService.getPlayerComponent(), BorderLayout.CENTER);
        wrapper.add(playerControls, BorderLayout.SOUTH);
    }

    void addTabForFolder(@NotNull String tabName) {
        addTabForFolder(tabName, Collections.emptyList());
    }

    void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType) {
        if (folderTabs.containsKey(tabName)) {
            JPanel tabPanel = folderTabs.get(tabName).panel;
            tabPanel.removeAll();
            mediaType.forEach((media) -> tabPanel.add(new QuickNaviButton(fromGuiViewService, media, false)));
            return;
        }
        JPanel tabPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        tabPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JPanel panel = (JPanel) e.getSource();
                    tabs.remove(tabs.getModel().getSelectedIndex());
                    folderTabs.remove(panel.getName());
                    fromGuiViewService.removeTabForFolder(panel.getName());
                }
            }
        });
        mediaType.forEach((media) -> tabPanel.add(new QuickNaviButton(fromGuiViewService, media, false)));
        JScrollPane scrollPane = new JScrollPane(tabPanel);
        tabPanel.setName(tabName);
        scrollPane.setName(tabName);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_BAR_SPEED);
        folderTabs.put(tabName, new FolderTab(tabName, folderTabs.size(), tabPanel));
        tabs.add(scrollPane);
    }


    void showQuickNavi(@NotNull Collection<MediaType> mediaType) {
        tabs.setVisible(true);
        setTitle(TITLE);
        repaintQuickNavi(mediaType);
        tabs.repaint();
    }

    void repaintQuickNavi(@NotNull Collection<MediaType> mediaType) {
        JPanel naviPanel = naviTab.panel;
        naviPanel.removeAll();
        mediaType.forEach((media) -> naviPanel.add(new QuickNaviButton(fromGuiViewService, media, true)));
    }

    void onMediaTimeChange(long newTime) {
        playerControls.fireTimeChanged(newTime);
    }

    void reinitializeControls() {
        playerControls.reinitialize();
    }

    void enterFullScreen() {
        setCursor(blankCursor);
        setJMenuBar(null);
        playerControls.setVisible(false);
    }

    void exitFullScreen() {
        setCursor(Cursor.getDefaultCursor());
        setJMenuBar(jMenuBar);
        playerControls.setVisible(true);
    }

    void showShutdownTimeChooser() {
        ShutdownChooserDialog chooserDialog = new ShutdownChooserDialog(this);
        log.debug("result: {}, {}", chooserDialog.getTime(), chooserDialog.getShutdownParam());
//        if(chooserDialog.getCurrentTime() != null)
//            core.shutdown(chooserDialog.getCurrentTime(), chooserDialog.getShutdownParam()); TODO
    }

    void enableSubtitlesMenu(boolean isEnabled) {
        spuMenu.setEnabled(isEnabled);
    }

    void updateSubtitlesMenu(@NotNull List<TrackDescription> spuDescriptions) {
        log.debug("Set subtitles, count {}", spuDescriptions.size());
        spuMenu.removeAll();
        spuDescriptions.forEach((desc) -> {
            JMenuItem subtitle = new JMenuItem(desc.description());
            subtitle.addActionListener(e -> mediaService.setSubtitles(desc.id()));
            spuMenu.add(subtitle);
        });
        spuMenu.setEnabled(spuDescriptions.size() > ZERO);
        spuMenu.revalidate();
    }

    void updateAudioTracksMenu(@NotNull List<TrackDescription> tracks) {
        log.debug("Set audio tracks, count {}", tracks.size());
        audioTracksMenu.removeAll();
        tracks.forEach((desc) -> {
            JMenuItem subtitle = new JMenuItem(desc.description());
            subtitle.addActionListener(e -> mediaService.setAudioTrack(desc.id()));
            audioTracksMenu.add(subtitle);
        });
        audioTracksMenu.setEnabled(tracks.size() > ZERO);
        audioTracksMenu.revalidate();
    }

    void showFilterInput() {
        filterInput.setVisible(true);
        filterInput.requestFocus();
        jMenuBar.repaint();
        jMenuBar.revalidate();
    }

    void hideFilterInput() {
        filterInput.transferFocus();
        filterInput.setText(EMPTY);
        filterCurrentTab();

        filterInput.setVisible(false);
    }

    public Frame() {
        log.debug("Frame initialize");
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.error("Failed to initialize LnF", e);
        }
        setTitle(TITLE);
        setMinimumSize(new Dimension(1200, 700));
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        setupBasicUI();

        initializeWrapper();
        initializeTabs();
        addTabForFolder("Navi");
        naviTab = getFolderTab(ZERO);
        TransferHandler handler = new TransferHandler() {
            public boolean canImport(TransferSupport support) {
                return true;
            }

            public boolean importData(TransferSupport support) {
                Transferable t = support.getTransferable();
                try {
                    dndHandler.receive((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        };
        tabs.setTransferHandler(handler);
        filterInput = new JTextField();

    }

    @PostConstruct
    private void init() {
        mediaFileChooser = new JFileChooser(settings.get(Settings.FC_OPEN_LOCATION, ""));
        initializeHotKeys();
        initializeMenu();
        revalidate();
    }

    private void initializeHotKeys() {
        // TODO frame dependency
        keyEventDispatcher.bind(focusedOnlyKey(VK_ESCAPE, STOP_MEDIA_FILTERING, filterInput));
        keyEventDispatcher.bind(globalKey(VK_ENTER, PLAY_LAST));
        keyEventDispatcher.bind(globalKey(VK_SPACE, TOGGLE_PLAYER));
        keyEventDispatcher.bind(globalKey(VK_ESCAPE, BACK));

        keyEventDispatcher.bind(globalControlKey(VK_Q, EXIT));
        keyEventDispatcher.bind(globalControlKey(VK_O, OPEN));
        keyEventDispatcher.bind(globalControlKey(VK_N, OPEN_QUICK_NAVI));
        keyEventDispatcher.bind(globalControlKey(VK_F, MEDIA_FILTERING));

        keyEventDispatcher.bind(globalKey(VK_F, TOGGLE_FULL_SCREEN));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    private static void setupBasicUI() {
        Font font = new Font("Dialog", Font.BOLD, 12);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", new Font("Dialog", Font.BOLD, 14));

        UIManager.put("Label.foreground", Components.MAIN_GRAY_COLOR);
        UIManager.put("Button.foreground", Components.MAIN_GRAY_COLOR);
        UIManager.put("TextField.foreground", Components.MAIN_GRAY_COLOR);
    }

    private void initializeWrapper() {
        wrapper = new JPanel(new BorderLayout());
        getContentPane().add(wrapper, BorderLayout.CENTER);
        addMouseWheelListener(e -> {
            if (playerControls != null) {
                if (e.getWheelRotation() < ZERO) {
                    playerControls.setVisible(true);
                    if (mediaService.isFullScreen())
                        setCursor(Cursor.getDefaultCursor());
                } else {
                    playerControls.setVisible(false);
                    if (mediaService.isFullScreen())
                        setCursor(blankCursor);
                }
            }
        });
    }

    private void initializeMenu() {
        jMenuBar = menuBuilder.getMenu();
        spuMenu = (JMenuItem) menuBuilder.getByName(LocalizedMessages.SUBTITLES);
        audioTracksMenu = (JMenuItem) menuBuilder.getByName(LocalizedMessages.AUDIO_TRACK);
        setJMenuBar(jMenuBar);

        filterInput.setVisible(false);
        filterInput.setPreferredSize(new Dimension(100, 20));
        filterInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterCurrentTab();
            }
        });
        jMenuBar.add(filterInput);
    }

    private void filterCurrentTab() {
        log.debug("Filter key pressed {}", filterInput.getText());
        JPanel selectedPanel = null;

        int selectedIndex = tabs.getModel().getSelectedIndex();
        if (selectedIndex >= ZERO) {
            FolderTab folderTab = getFolderTab(selectedIndex);
            if (folderTab != null)
                selectedPanel = folderTab.panel;
        }

        if (selectedPanel != null) {
            Pattern pattern = Pattern.compile(buildFilterPattern(filterInput.getText()));
            for (Component component : selectedPanel.getComponents()) {
                QuickNaviButton naviButton = (QuickNaviButton) component;
                naviButton.setVisible(pattern.matcher(naviButton.getMediaType().getName().toLowerCase()).matches());
            }
            selectedPanel.repaint();
        }
    }

    @NotNull
    private String buildFilterPattern(String text) {
        StringBuilder sb = new StringBuilder(REGEX_ALL);
        if (StringUtil.isBlank(text))
            return sb.toString();
        return sb.append(filterInput.getText().toLowerCase().replaceAll(SPACE, REGEX_ALL)).append(REGEX_ALL).toString();
    }

    private void initializeTabs() {
        tabs = new JTabbedPane();
        wrapper.add(tabs);
    }

    @Nullable
    private FolderTab getFolderTab(int index) {
        for (FolderTab tab : folderTabs.values()) {
            if (tab.index == index)
                return tab;
        }
        return null;
    }

    void openMedia() {
        int res = mediaFileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                settings.set(Settings.FC_OPEN_LOCATION, mediaFileChooser.getSelectedFile().getParent());
                settings.save();
            }).start();
            new Thread(() -> {
                mediaService.updateCurrentMediaInCache();
                mediaService.play(mediaFileChooser.getSelectedFile().getPath());
            }).start();
        }
    }

    class FolderTab {

        String name;

        int index;

        JPanel panel;

        FolderTab(String name, int index, JPanel panel) {
            this.name = name;
            this.index = index;
            this.panel = panel;
        }
    }

}
