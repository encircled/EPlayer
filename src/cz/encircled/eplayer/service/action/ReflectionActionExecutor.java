package cz.encircled.eplayer.service.action;

import cz.encircled.eplayer.common.Constants;
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

public class ReflectionActionExecutor implements ActionExecutor {

	private SettingsDialog settingsDialog;
	
	private String fileChooserLastPath;

    private final static Logger log = LogManager.getLogger(ReflectionActionExecutor.class);

    private TreeMap<String, Method> commands;

    private MediaService mediaService;

    private CacheService cacheService;

    private ViewService viewService;

    public ReflectionActionExecutor() {
        initializeCommandsTree();
        setDefaultFileChooserPath();
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

    @Override
    public void execute(String command){
        try {
            commands.get(command).invoke(ReflectionActionExecutor.this);
        } catch (Throwable e) {
            log.error("Failed to execute command {}, msg:", command, e.getMessage());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void exit() {
        System.exit(Constants.ZERO);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void openMedia() {
        JFileChooser fc = new JFileChooser(fileChooserLastPath);
        int res = fc.showOpenDialog(null); // TODO
        if (res == JFileChooser.APPROVE_OPTION) {
            fileChooserLastPath = fc.getSelectedFile().getPath();
            mediaService.updateCurrentMediaInCache();
            mediaService.play(fileChooserLastPath);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
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

    @SuppressWarnings("UnusedDeclaration")
    public void cancelDialog() {
        for (Window w : JDialog.getWindows()){
            if (w instanceof JDialog)
                w.dispose();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void showShutdownTimeChooser(){
//        frame.showShutdownTimeChooser(); TODO
    }

    @SuppressWarnings("UnusedDeclaration")
    public void settings() {
//        settingsDialog = new SettingsDialog(frame);
//        settingsDialog.setVisible(true); TODO
    }

    @SuppressWarnings("UnusedDeclaration")
    public void openQuickNavi(){
        mediaService.pause();
        viewService.showQuickNavi();
        if(mediaService.isFullScreen())
            mediaService.exitFullScreen();
        mediaService.updateCurrentMediaInCache();
        mediaService.stop();
        cacheService.save();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void togglePlayer(){
        mediaService.togglePlayer();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void toggleFullScreen(){
        mediaService.toggleFullScreen();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void back(){
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

    @SuppressWarnings("UnusedDeclaration")
    public void playLast(){
//        new Thread(app::playLast).start();
        // TODO
    }

}
