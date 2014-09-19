package cz.encircled.eplayer.view.swing;

// TODO media groups
public class Frame {
}
        /*
        extends JFrame implements AppView {

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

    @Override
    public void showPlayer() {
        tabs.setVisible(false);
        wrapper.add(mediaService.getPlayerComponent(), BorderLayout.CENTER);
        wrapper.add(playerControls, BorderLayout.SOUTH);
    }

    @Override
    public void addTabForFolder(@NotNull String tabName) {
        addTabForFolder(tabName, Collections.emptyList());
    }

    @Override
    public void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType) {
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


    @Override
    public void showQuickNavi(@NotNull Collection<MediaType> mediaType) {
        tabs.setVisible(true);
        setTitle(TITLE);
        repaintQuickNavi(mediaType);
        tabs.repaint();
    }

    public void repaintQuickNavi(@NotNull Collection<MediaType> mediaType) {
        JPanel naviPanel = naviTab.panel;
        naviPanel.removeAll();
        mediaType.forEach((media) -> naviPanel.add(new QuickNaviButton(fromGuiViewService, media, true)));
    }

    public void onMediaTimeChange(long newTime) {
        playerControls.fireTimeChanged(newTime);
    }

    public void reinitializeControls() {
        playerControls.reinitialize();
    }

    @Override
    public void enterFullScreen() {
        setCursor(blankCursor);
        setJMenuBar(null);
        playerControls.setVisible(false);
    }

    @Override
    public void exitFullScreen() {
        setCursor(Cursor.getDefaultCursor());
        setJMenuBar(jMenuBar);
        playerControls.setVisible(true);
    }

    @Override
    public void showShutdownTimeChooser() {
        ShutdownChooserDialog chooserDialog = new ShutdownChooserDialog(this);
        log.debug("result: {}, {}", chooserDialog.getTime(), chooserDialog.getShutdownParam());
//        if(chooserDialog.getCurrentTime() != null)
//            core.shutdown(chooserDialog.getCurrentTime(), chooserDialog.getShutdownParam()); TODO
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {
        spuMenu.setEnabled(isEnabled);
    }

    public void updateSubtitlesMenu(@NotNull List<TrackDescription> spuDescriptions) {
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

    public void updateAudioTracksMenu(@NotNull List<TrackDescription> tracks) {
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

    @Override
    public void showFilterInput() {
        filterInput.setVisible(true);
        filterInput.requestFocus();
        jMenuBar.repaint();
        jMenuBar.revalidate();
    }

    @Override
    public void hideFilterInput() {
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

    @Override
    public void openMedia() {
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
                      */