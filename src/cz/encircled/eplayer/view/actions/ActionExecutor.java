package cz.encircled.eplayer.view.actions;

import cz.encircled.eplayer.app.Application;
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

public class ActionExecutor {

	private cz.encircled.eplayer.view.Frame frame;
	
	private SettingsDialog settingsDialog;
	
	private String fileChooserLastPath;

    private final static Logger log = LogManager.getLogger(ActionExecutor.class);

    private TreeMap<String, Method> commands;

    public ActionExecutor(){
        initializeCommandsTree();
        setDefaultFileChooserPath();
    }

	public void setFrame(cz.encircled.eplayer.view.Frame frame){
		this.frame = frame;
	}

    private void initializeCommandsTree(){
        commands = new TreeMap<>();
        Field[] commandFields = ActionCommands.class.getDeclaredFields();
        for(Field f : commandFields){
            try {
                commands.put((String) f.get(null), ActionExecutor.class.getMethod((String) f.get(null)));
            } catch (Exception e) {
                log.warn("error reading command field {} ", f.getName());
            }
        }
    }

    public void setDefaultFileChooserPath(){
        fileChooserLastPath = PropertyProvider.get(PropertyProvider.SETTING_DEFAULT_OPEN_LOCATION, System.getProperty("user.home"));
    }

    public void execute(String command){
        try {
            commands.get(command).invoke(ActionExecutor.this);
        } catch (Throwable e) {
            log.error("Failed to execute command {}, msg:", command, e.getMessage());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void exit() {
        Application.getInstance().exit();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void openMedia() {
        SwingUtilities.invokeLater(() -> {
            JFileChooser fc = new JFileChooser(fileChooserLastPath);
            int res = fc.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                fileChooserLastPath = fc.getSelectedFile().getPath();
                frame.updateCurrentPlayableInCache();
                Application.getInstance().play(fileChooserLastPath);
            }
        });
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
                JOptionPane.showMessageDialog(frame, "Failed to save settings", "error", JOptionPane.ERROR_MESSAGE);
            }
            Application.getInstance().initialize();
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
        frame.showShutdownTimeChooser();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void settings() {
        settingsDialog = new SettingsDialog(frame);
        settingsDialog.setVisible(true);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void openQuickNavi(){
        frame.showQuickNavi();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void togglePlayer(){
        frame.togglePlayer();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void toggleFullScreen(){
        frame.toggleFullScreen();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void back(){
        if(frame.isPlayerState()){
            if(frame.isFullScreen()){
                frame.exitFullScreen(false);
                frame.updateCurrentPlayableInCache();
            }
            else
                execute(ActionCommands.OPEN_QUICK_NAVI);
        } else if(frame.isQuickNaviState())
            execute(ActionCommands.EXIT);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void playLast(){
        new Thread(() -> Application.getInstance().playLast()).start();
    }

}
