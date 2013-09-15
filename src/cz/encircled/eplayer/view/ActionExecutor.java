package cz.encircled.eplayer.view;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.TreeMap;

import javax.swing.*;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.app.PropertyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionExecutor {

	private Frame frame;
	
	private SettingsDialog settingsDialog;
	
	private String fileChooserLastPath = PropertyProvider.get(PropertyProvider.SETTING_DEFAULT_OPEN_LOCATION, System.getProperty("user.home"));

    private final static Logger log = LogManager.getLogger(ActionExecutor.class);

    private TreeMap<String, Method> commands;

    public ActionExecutor(){
        initializeCommandsTree();
    }

	public void setFrame(Frame frame){
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

    public void execute(String command){
        try {
            commands.get(command).invoke(ActionExecutor.this);
        } catch (Exception e) {
            log.error("Failed to execute command {}, msg:", command, e.getMessage());
        }
    }


    public void exit() {
        Application.getInstance().exit();
    }

    public void openMedia() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFileChooser fc = new JFileChooser(fileChooserLastPath);
                int res = fc.showOpenDialog(frame);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    fileChooserLastPath = f.getPath();
                    Application.getInstance().play(fileChooserLastPath);
                }
            }
        });
    }

    public void saveSettings() {
        Component[] components = settingsDialog.getValuesPanel().getComponents();
        for(Component c : components){
            if(c instanceof JTextField)
                PropertyProvider.set(c.getName(), ((JTextField) c).getText());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PropertyProvider.save();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(frame, "Failed to save settings", "error", JOptionPane.ERROR_MESSAGE);
                }
                Application.getInstance().initialize();
            }
        }).start();
    }

    public void cancelDialog() {
        for (Window w : JDialog.getWindows()){
            if (w instanceof JDialog)
                w.dispose();
        }
    }

    public void settings() {
        settingsDialog = new SettingsDialog(frame, true);
        settingsDialog.setVisible(true);
    }

    public void openQuickNavi(){
        frame.showQuickNavi();
    }

    public void togglePlayer(){
        frame.togglePlayer();
    }

    public void toggleFullScreen(){
        frame.toggleFullScreen();
    }

    public void back(){
        if(frame.isPlayerState()){
            if(frame.isFullScreen())
                frame.exitFullScreen(false);
            else {

                execute(ActionCommands.OPEN_QUICK_NAVI);
            }
        } else if(frame.isQuickNaviState())
            execute(ActionCommands.EXIT);
    }

    public void playLast(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Application.getInstance().playLast();
            }
        }).start();
    }

}
