package cz.encircled.eplayer.view;

import com.alee.laf.WebLookAndFeel;
import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.util.PropertyProvider;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.componensts.PlayerControls;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import cz.encircled.eplayer.view.componensts.WrapLayout;
import cz.encircled.eplayer.view.listeners.KeyDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static cz.encircled.eplayer.common.Constants.*;
import static cz.encircled.eplayer.service.action.ActionCommands.*;
import static cz.encircled.eplayer.view.listeners.KeyDispatcher.*;
import static java.awt.event.KeyEvent.*;

public class Frame extends JFrame {

    private static final String TITLE = "EPlayer";

    private static final int SCROLL_BAR_SPEED = 16;

    private PlayerControls playerControls;

    private JPanel wrapper;

    private FolderTab naviTab;

    private JMenuBar jMenuBar;

    private JMenu spuMenu;

    private JTextField filterInput;

    private JTabbedPane tabs;

    private final ViewService viewService;

    private final MediaService mediaService;

    private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");

    private Map<String, FolderTab> folderTabs = new HashMap<>();

    private final static Logger log = LogManager.getLogger(Frame.class);

    private JFileChooser mediaFileChooser;

    public Frame(ViewService viewService, MediaService mediaService) {
        this.viewService = viewService;
        this.mediaService = mediaService;
        initialize();
    }

    void showPlayer() {
        if(playerControls == null)
            playerControls = new PlayerControls(mediaService);
        tabs.setVisible(false);
        wrapper.add(mediaService.getPlayerComponent(), BorderLayout.CENTER);
        wrapper.add(playerControls, BorderLayout.SOUTH);
    }

    void addTabForFolder(@NotNull String tabName){
        addTabForFolder(tabName, Collections.emptyList());
    }

    void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType){
        if(folderTabs.containsKey(tabName)){
            JPanel tabPanel = folderTabs.get(tabName).panel;
            tabPanel.removeAll();
            mediaType.forEach((media) -> tabPanel.add(new QuickNaviButton(viewService, mediaService, media, false)));
            return;
        }
        JPanel tabPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        mediaType.forEach((media) -> tabPanel.add(new QuickNaviButton(viewService, mediaService, media, false)));
        JScrollPane scrollPane = new JScrollPane(tabPanel);
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

    void repaintQuickNavi(@NotNull Collection<MediaType> mediaType){
        JPanel naviPanel = naviTab.panel;
        naviPanel.removeAll();
        mediaType.forEach((media) -> naviPanel.add(new QuickNaviButton(viewService, mediaService, media)));
    }

    void onMediaTimeChange(long newTime){
        playerControls.fireTimeChanged(newTime);
    }

    void reinitializeControls(){
        playerControls.reinitialize();
    }

    void enterFullScreen(){
        setCursor(blankCursor);
        setJMenuBar(null);
        playerControls.setVisible(false);
    }

    void exitFullScreen(){
        setCursor(Cursor.getDefaultCursor());
        setJMenuBar(jMenuBar);
        playerControls.setVisible(true);
    }

    void showShutdownTimeChooser(){
        ShutdownChooserDialog chooserDialog = new ShutdownChooserDialog(this);
        log.debug("result: {}, {}", chooserDialog.getTime(), chooserDialog.getShutdownParam());
//        if(chooserDialog.getCurrentTime() != null)
//            core.shutdown(chooserDialog.getCurrentTime(), chooserDialog.getShutdownParam()); TODO
    }

    void enableSubtitlesMenu(boolean isEnabled){
        spuMenu.setEnabled(isEnabled);
    }

    void updateSubtitlesMenu(@NotNull List<TrackDescription> spuDescriptions){
        log.debug("Set subtitles, count {}", spuDescriptions.size());
        spuMenu.removeAll();
        spuDescriptions.forEach((desc) -> {
            JMenuItem subtitle = new JMenuItem(desc.description());
            subtitle.addActionListener(e -> mediaService.setSubtitlesById(desc.id()));
            spuMenu.add(subtitle);
        });
        spuMenu.setEnabled(spuDescriptions.size() > ZERO);
        spuMenu.revalidate();
    }

    void showFilterInput(){
        filterInput.setVisible(true);
        filterInput.requestFocus();
    }

    void hideFilterInput(){
        filterInput.transferFocus();
        filterInput.setText(EMPTY);
        filterCurrentTab();

        filterInput.setVisible(false);
    }

    private void initialize(){
        if(!WebLookAndFeel.install()){
            log.error("Failed to initialize weblaf");
        }
        setTitle(TITLE);
        setMinimumSize(new Dimension(1130, 700));
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        setupBasicUI();

        initializeWrapper();
        initializeMenu();
        initializeTabs();
        addTabForFolder("Navi");
        naviTab = getFolderTab(ZERO);
        initializeHotKeys();
        tabs.setTransferHandler(handler);
        mediaFileChooser = new JFileChooser();
    }

    private TransferHandler handler = new TransferHandler() {
        public boolean canImport(TransferHandler.TransferSupport support) {
            return true;
        }

        public boolean importData(TransferHandler.TransferSupport support) {
            Transferable t = support.getTransferable();
            try {
                List<File> folders = (java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
                for (File folder : folders) {
                    log.debug("DnD file path {}", folder.getAbsolutePath());
                    if(folder.isDirectory()){
                        viewService.createNewTab(folder.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    };

    private void initializeHotKeys(){
        // TODO frame dependency
        KeyDispatcher dispatcher = new KeyDispatcher(Application.getActionExecutor());

        dispatcher.bind(focusedOnlyKey(VK_ESCAPE, STOP_MEDIA_FILTERING, filterInput));
        dispatcher.bind(globalKey(VK_ENTER, PLAY_LAST));
        dispatcher.bind(globalKey(VK_SPACE, TOGGLE_PLAYER));
        dispatcher.bind(globalKey(VK_ESCAPE, CANCEL));

        dispatcher.bind(globalControlKey(VK_Q, EXIT));
        dispatcher.bind(globalControlKey(VK_O, OPEN));
        dispatcher.bind(globalControlKey(VK_N, OPEN_QUICK_NAVI));
        dispatcher.bind(globalControlKey(VK_S, SETTINGS));
        dispatcher.bind(globalControlKey(VK_F, MEDIA_FILTERING));

        dispatcher.bind(globalKey(VK_F, TOGGLE_FULL_SCREEN));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

    private static void setupBasicUI() {
        Font font = new Font("Dialog", Font.BOLD,  12);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", new Font("Dialog", Font.BOLD, 14));

        UIManager.put("Label.foreground", Components.MAIN_GRAY_COLOR);
        UIManager.put("Button.foreground", Components.MAIN_GRAY_COLOR);
        UIManager.put("TextField.foreground", Components.MAIN_GRAY_COLOR);
    }

    private void initializeWrapper(){
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

    private void initializeMenu(){
        jMenuBar = new JMenuBar();
        JMenu file = new JMenu(MessagesProvider.get(LocalizedMessages.FILE));
        JMenu tools = new JMenu(MessagesProvider.get(LocalizedMessages.TOOLS));
        JMenu autoShutdown = new JMenu(MessagesProvider.get(LocalizedMessages.AUTO_SHUTDOWN));
        JMenu mediaMenu = new JMenu(MessagesProvider.get(LocalizedMessages.MEDIA));

        spuMenu = new JMenu(MessagesProvider.get(LocalizedMessages.SUBTITLES));
        spuMenu.setEnabled(false);

        autoShutdown.add(Components.getMenuItem(LocalizedMessages.AUTO_SHUTDOWN, SHUTDOWN_TIME_CHOOSER));

        tools.add(Components.getMenuItem(LocalizedMessages.OPEN_QUICK_NAVI, OPEN_QUICK_NAVI));
        tools.add(Components.getMenuItem(LocalizedMessages.SETTINGS, SETTINGS));
        tools.add(autoShutdown);

        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.OPEN, OPEN));
        file.add(new JSeparator());
        file.add(Components.getMenuItem(LocalizedMessages.EXIT, EXIT));
        file.add(new JSeparator());

        mediaMenu.add(spuMenu);
        mediaMenu.add(new JSeparator());

        setJMenuBar(jMenuBar);
        jMenuBar.add(file);
        jMenuBar.add(mediaMenu);
        jMenuBar.add(tools);

        filterInput = new JTextField();
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

    private String buildFilterPattern(String text) {
        StringBuilder sb = new StringBuilder(REGEX_ALL);
        if(StringUtil.isBlank(text))
            return sb.toString();
        return sb.append(filterInput.getText().toLowerCase().replaceAll(SPACE, REGEX_ALL)).append(REGEX_ALL).toString();
    }

    private void initializeTabs(){
        tabs = new JTabbedPane();
        wrapper.add(tabs);
    }

    @Nullable
    private FolderTab getFolderTab(int index){
        for(FolderTab tab : folderTabs.values()) {
            if (tab.index == index)
                return tab;
        }
        return null;
    }

    void openMedia() {
        int res = mediaFileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            mediaService.updateCurrentMediaInCache();
            mediaService.play(mediaFileChooser.getSelectedFile().getPath());
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
