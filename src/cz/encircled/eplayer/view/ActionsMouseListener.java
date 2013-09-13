package cz.encircled.eplayer.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.app.PropertyProvider;

public class ActionsMouseListener implements ActionListener {

	private Frame frame;
	
	private SettingsDialog settingsDialog;
	
	private String fileChooserLastPath = System.getProperty("user.home");
	
	public void setFrame(Frame frame){
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String eName = e.getActionCommand();
		
		System.out.println(eName);
		if(eName.equalsIgnoreCase(ActionCommands.EXIT)){
			frame.stopPlayer();
			frame.dispose();
			System.exit(0);
		} else if(eName.equalsIgnoreCase(ActionCommands.SETTINGS)){
			settingsDialog = new SettingsDialog(frame, true);
			settingsDialog.setVisible(true);
		} else if(eName.equalsIgnoreCase(ActionCommands.CANCEL)){
			int i = 0;
			Object c = e.getSource();
			while(!(c instanceof JDialog) && i++ < 20)
				c = ((JComponent)c).getParent();
			((JDialog)c).dispose();
		} else if(eName.equalsIgnoreCase(ActionCommands.SAVE_SETTINGS)){
			Component[] components = settingsDialog.getValuesPanel().getComponents();
			for(Component c : components){
				if(c instanceof JTextField){
					PropertyProvider.set(c.getName(), ((JTextField)c).getText());
				}
			}
			try {
				PropertyProvider.save();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Application.getInstance().initialize();
		} else if(eName.equalsIgnoreCase(ActionCommands.OPEN)){
			System.out.println("open a");
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
			
		} else if(eName.equalsIgnoreCase(ActionCommands.OPEN_QUICK_NAVI)){
            Application.getInstance().showQuickNavi();
        }
		
	}

}
