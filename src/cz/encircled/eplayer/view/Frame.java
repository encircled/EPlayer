package cz.encircled.eplayer.view;

import com.alee.laf.WebLookAndFeel;
import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.componensts.PlayerControls;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import cz.encircled.eplayer.view.componensts.WrapLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static cz.encircled.eplayer.common.Constants.REGEX_ALL;
import static cz.encircled.eplayer.common.Constants.SPACE;

public class Frame extends JFrame {

    private static final String TITLE = "EPlayer";
    private static final int SCROLL_BAR_SPEED = 16;

    private PlayerControls playerControls;

    private JPanel wrapper;

    private JPanel naviPanel;

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
        spuMenu.setEnabled(spuDescriptions.size() > Constants.ZERO);
        spuMenu.revalidate();
    }

    void showFilterInput(){
        filterInput.setVisible(true);

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
        initializeQuickNavi();
        initializeTabs();
    }

    private static void setupBasicUI() {
        Font font = new Font("Dialog", Font.BOLD,  12);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", new Font("Dialog", Font.BOLD,  14));

        UIManager.put("Label.foreground", Components.MAIN_GRAY_COLOR);
        UIManager.put("Button.foreground", Components.MAIN_GRAY_COLOR);
        UIManager.put("TextField.foreground", Components.MAIN_GRAY_COLOR);
    }

    private void initializeWrapper(){
        wrapper = new JPanel(new BorderLayout());
        getContentPane().add(wrapper, BorderLayout.CENTER);
        addMouseWheelListener(e -> {
            if(playerControls != null){
                if(e.getWheelRotation() < Constants.ZERO){
                    playerControls.setVisible(true);
                    if(mediaService.isFullScreen())
                        setCursor(Cursor.getDefaultCursor());
                } else{
                    playerControls.setVisible(false);
                    if(mediaService.isFullScreen())
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

        filterInput = new JTextField();
        filterInput.setPreferredSize(new Dimension(100, 20));
        filterInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                log.debug("Filter key pressed {}", filterInput.getText());
                int selectedIndex = tabs.getModel().getSelectedIndex();
                JPanel selectedPanel = null;
                switch (selectedIndex) {
                    case -1:
                        break;
                    case 0:
                        selectedPanel = naviPanel;
                        break;
                    default:
                        FolderTab folderTab = getFolderTab(selectedIndex - 1);
                        if(folderTab != null)
                            selectedPanel = folderTab.panel;
                }
                if(selectedPanel != null) {
                    Pattern pattern = Pattern.compile(buildFilterPattern(filterInput.getText()));
                    for (Component component : selectedPanel.getComponents()) {
                        QuickNaviButton naviButton = (QuickNaviButton) component;
                        naviButton.setVisible(pattern.matcher(naviButton.getMediaType().getName().toLowerCase()).matches());
                    }
                    selectedPanel.repaint();
                }
            }
        });
        jMenuBar.add(filterInput);
    }

    private String buildFilterPattern(String text) {
        StringBuilder sb = new StringBuilder(REGEX_ALL);
        if(StringUtil.isBlank(text))
            return sb.toString();
        return sb.append(filterInput.getText().toLowerCase().replaceAll(SPACE, REGEX_ALL)).append(REGEX_ALL).toString();
    }

    private void initializeTabs(){
        tabs = new JTabbedPane();
        tabs.add(new JScrollPane(naviPanel), "Navi");
        wrapper.add(tabs);
    }

    private void initializeQuickNavi(){
        naviPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
    }

    @Nullable
    private FolderTab getFolderTab(int index){
        for(FolderTab tab : folderTabs.values()) {
            if (tab.index == index)
                return tab;
        }
        return null;
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
