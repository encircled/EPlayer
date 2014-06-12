package cz.encircled.eplayer.service.action;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.util.PropertyProvider;
import cz.encircled.eplayer.view.SettingsDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.TreeMap;

@SuppressWarnings("UnusedDeclaration")
public class ReflectionActionExecutor implements ActionExecutor {

	private SettingsDialog settingsDialog;
	
	private String fileChooserLastPath;

    private final static Logger log = LogManager.getLogger(ReflectionActionExecutor.class);

    private TreeMap<String, Method> commands;

    private MediaService mediaService;

    private CacheService cacheService;

    private ViewService viewService;

    public ReflectionActionExecutor() {
        log.trace("ReflectionActionExecutor init start");
        initializeCommandsTree();
        setDefaultFileChooserPath();
        log.trace("ReflectionActionExecutor init complete");
    }

    public void setViewService(ViewService viewService) {
        this.viewService = viewService;
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    private void initializeCommandsTree(){
        commands = new TreeMap<>();
        Field[] commandFields = ActionCommands.class.getDeclaredFields();
        for(Field f : commandFields){
            try {
                commands.put((String) f.get(null), ReflectionActionExecutor.class.getMethod((String) f.get(null)));
            } catch (Exception e) {
                log.warn("error reading command field {} ", f.getName());
            }
        }
    }

    public void setDefaultFileChooserPath(){
        fileChooserLastPath = PropertyProvider.get(PropertyProvider.SETTING_DEFAULT_OPEN_LOCATION, System.getProperty("user.home"));
    }

    // TODO: create Object for commands with type. When command is GUI only - do not manipulate with threads
    @Override
    public void execute(String command){
        try {
            if(EventQueue.isDispatchThread()){
                log.debug("Execute is called from EDT, creating SwingWorker");
                new SwingWorker<Object, Object>(){
                    @Override
                    protected Object doInBackground() throws Exception {
                        return commands.get(command).invoke(ReflectionActionExecutor.this);
                    }
                }.execute();
            } else {
                commands.get(command).invoke(ReflectionActionExecutor.this);
            }

        } catch (Throwable e) {
            log.error("Failed to execute command {}, msg:", command, e.getMessage());
        }
    }

    public void exit() {
        System.exit(Constants.ZERO);
    }

    public void openMedia() {
        JFileChooser fc = new JFileChooser(fileChooserLastPath);
        int res = fc.showOpenDialog(viewService.getWindow());
        if (res == JFileChooser.APPROVE_OPTION) {
            fileChooserLastPath = fc.getSelectedFile().getPath();
            mediaService.updateCurrentMediaInCache();
            mediaService.play(fileChooserLastPath);
        }
    }

    public void saveSettings() {
        Component[] components = settingsDialog.getValuesPanel().getComponents();
        for(Component c : components){
            if(c instanceof JTextField)
                PropertyProvider.set(c.getName(), ((JTextField) c).getText());
            else if(c instanceof JComboBox)
                PropertyProvider.set(c.getName(), ((JComboBox<?>) c).getSelectedItem().toString());
        }

        new Thread(() -> {
            try {
                PropertyProvider.save();
            } catch (IOException e1) {
                GUIUtil.showMessage("Failed to save settings", "error", JOptionPane.ERROR_MESSAGE); // TODO message l
            }
//            try {
//                app.reinitialize();
//            TODO
//            } catch (IOException io){
//                log.error("Failed to reinitialize application");
//                System.exit(-1);
//            }
        }).start();
    }

    public void cancelDialog() {
        for (Window w : JDialog.getWindows()){
            if (w instanceof JDialog)
                w.dispose();
        }
    }

    public void showShutdownTimeChooser(){
//        frame.showShutdownTimeChooser(); TODO
    }

    public void settings() {
//        settingsDialog = new SettingsDialog(frame);
//        settingsDialog.setVisible(true); TODO
    }

    public void openQuickNavi(){
        mediaService.pause();
        viewService.showQuickNavi();
        if(mediaService.isFullScreen())
            mediaService.exitFullScreen();
        mediaService.updateCurrentMediaInCache();
        mediaService.stop();
        cacheService.save();
    }

    public void togglePlayer(){
        mediaService.togglePlayer();
    }

    void toggleFullScreen(){
        mediaService.toggleFullScreen();
    }

    void back(){
        if(viewService.isPlayerState()){
            if(mediaService.isFullScreen()){
                mediaService.exitFullScreen();
                mediaService.updateCurrentMediaInCache();
            }
            else
                execute(ActionCommands.OPEN_QUICK_NAVI);
        } else if(viewService.isQuickNaviState())
            execute(ActionCommands.EXIT);
    }

    void playLast(){
        MediaType media = cacheService.getLastByWatchDate();
        if(media != null)
            mediaService.play(media);
    }

    public void mediaFiltering(){
        viewService.initMediaFiltering();
    }

}
