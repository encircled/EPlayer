package cz.encircled.eplayer.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.app.PropertyProvider;

public class ActionsMouseListener implements ActionListener {

	private Frame frame;
	
	private SettingsDialog settingsDialog;
	
	private String fileChooserLastPath = PropertyProvider.get(PropertyProvider.SETTING_DEFAULT_OPEN_LOCATION, System.getProperty("user.home"));
	
	public void setFrame(Frame frame){
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String eName = e.getActionCommand();
		
		if(eName.equalsIgnoreCase(ActionCommands.EXIT)){
            exit();
		} else if(eName.equalsIgnoreCase(ActionCommands.SETTINGS)){
            showSettingsDialog();
		} else if(eName.equalsIgnoreCase(ActionCommands.CANCEL)){
            cancelDialog(e);
		} else if(eName.equalsIgnoreCase(ActionCommands.SAVE_SETTINGS)){
            saveSettings();
		} else if(eName.equalsIgnoreCase(ActionCommands.OPEN)){
            openMedia();
        } else if(eName.equalsIgnoreCase(ActionCommands.OPEN_QUICK_NAVI)){
            Application.getInstance().showQuickNavi();
        }
		
	}

    private void exit() {
        Application.getInstance().exit();
    }

    private void openMedia() {
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

    private void saveSettings() {
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

    private void cancelDialog(ActionEvent e) {
        int i = 0;
        Object c = e.getSource();
        while(!(c instanceof JDialog) && i++ < 20)
            c = ((JComponent)c).getParent();
        ((JDialog)c).dispose();
    }

    private void showSettingsDialog() {
        settingsDialog = new SettingsDialog(frame, true);
        settingsDialog.setVisible(true);
    }

}
