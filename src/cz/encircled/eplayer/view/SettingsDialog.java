package cz.encircled.eplayer.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import cz.encircled.eplayer.util.GUIUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.encircled.eplayer.app.LocalizedMessages;
import cz.encircled.eplayer.app.MessagesProvider;
import cz.encircled.eplayer.app.PropertyProvider;
import cz.encircled.eplayer.model.SettingItem;

public class SettingsDialog extends JDialog {
	
	private final static Logger log = LogManager.getLogger(SettingsDialog.class);

	private static final int BUTTON_HEIGHT = 50;

	private static final int BUTTON_WIDTH = 100;

	private static final int INPUT_HEIGHT = 40;

	private static final int LABEL_HEIGHT = 44;

	private static final long serialVersionUID = 1L;
	
	private final static int DIALOG_WIDTH = 610;

	private final static int DIALOG_HEIGHT = 630;

	private static final int WIDTH_30 = (int)(DIALOG_WIDTH * 0.3);

	private static final int WIDTH_55 = (int)(DIALOG_WIDTH * 0.55);

	private static final int WIDTH_80 = (int)(DIALOG_HEIGHT * 0.8);
	
	private final static int WRAPPER_PANEL_MARGINS = 14;
	
	private final static int BUTTONs_PANEL_HORIZONTAL_MARGIN = 30;
	
	private static List<SettingItem> settings = new ArrayList<SettingItem>();

	private JPanel keysPanel;
	
	private JPanel valuesPanel;
	
	public SettingsDialog(JFrame parent, boolean isModal){
		super(parent, isModal);
		initialize();
		initializeSettings();
	}
	
	public JPanel getValuesPanel(){
		return valuesPanel;
	}
	
	private void initialize(){
		setTitle(MessagesProvider.get(LocalizedMessages.SETTINGS));
		setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		setResizable(false);
		
		JPanel wrapper = new JPanel();
        wrapper.setBackground(Color.WHITE);
        wrapper.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
        wrapper.setLayout(new FlowLayout(FlowLayout.LEADING, WRAPPER_PANEL_MARGINS, WRAPPER_PANEL_MARGINS));
        
        initKeysPanel();
        initValuesPanel();
        
        getContentPane().add(wrapper);
        wrapper.add(keysPanel);
        wrapper.add(valuesPanel);
        wrapper.add(getButtonsPanel());

        GUIUtil.bindKey(wrapper, KeyConstants.ENTER, null, ActionCommands.SAVE_SETTINGS);
        GUIUtil.bindKey(wrapper, KeyConstants.ESCAPE, null, ActionCommands.CANCEL);
        
        pack();
        setLocationRelativeTo(null);
	}
	
	private JPanel getButtonsPanel(){
		JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, BUTTONs_PANEL_HORIZONTAL_MARGIN, 0));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setPreferredSize(new Dimension((int)(DIALOG_WIDTH * 0.9), (int)(DIALOG_HEIGHT * 0.95)));		
        buttonsPanel.add(Components.getButton(
        				MessagesProvider.get(LocalizedMessages.SAVE), ActionCommands.SAVE_SETTINGS, BUTTON_WIDTH, BUTTON_HEIGHT));
        buttonsPanel.add(Components.getButton(
        				MessagesProvider.get(LocalizedMessages.CANCEL), ActionCommands.CANCEL, BUTTON_WIDTH, BUTTON_HEIGHT));
        return buttonsPanel;
	}
	
	private void initKeysPanel(){
		keysPanel = new JPanel();	
		keysPanel.setPreferredSize(new Dimension(WIDTH_30, WIDTH_80));
        keysPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        keysPanel.add(Components.getJLine(WIDTH_30, Components.MAIN_BLUE_COLOR));
	}
	
	private void initValuesPanel(){
		valuesPanel = new JPanel();
        valuesPanel.setBackground(Color.WHITE);
        valuesPanel.setPreferredSize(new Dimension(WIDTH_55, WIDTH_80));
        valuesPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0,3));		
        valuesPanel.add(Components.getJLine(WIDTH_55, Color.WHITE));
	}
	
	private void initializeSettings(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                settings.clear();
                settings.add(new SettingItem(MessagesProvider.get(LocalizedMessages.VLC_PATH), PropertyProvider.SETTING_VLC_PATH, SettingItem.INPUT_TEXT_ELEMENT));
                settings.add(new SettingItem(MessagesProvider.get(LocalizedMessages.DEFAULT_OPEN_LOCATION), PropertyProvider.SETTING_DEFAULT_OPEN_LOCATION, SettingItem.INPUT_TEXT_ELEMENT));
                settings.add(new SettingItem(MessagesProvider.get(LocalizedMessages.QUICK_NAVI_STORAGE_PATH), PropertyProvider.SETTING_QUICK_NAVI_STORAGE_PATH, SettingItem.INPUT_TEXT_ELEMENT));
                settings.add(new SettingItem(MessagesProvider.get(LocalizedMessages.LANGUAGE), PropertyProvider.SETTING_LANGUAGE, SettingItem.INPUT_TEXT_ELEMENT));
                settings.add(new SettingItem(MessagesProvider.get(LocalizedMessages.MAX_VOLUME_PERCENTS), PropertyProvider.SETTING_MAX_VOLUME, SettingItem.INPUT_TEXT_ELEMENT));

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for(SettingItem item : settings){
                            keysPanel.add(Components.getLabel(item.getViewText(), WIDTH_30, LABEL_HEIGHT, true));
                            switch(item.getElementType()){
                                case 0:
                                    valuesPanel.add(Components.getInput(item.getSettingName(), PropertyProvider.get(item.getSettingName()), WIDTH_55, INPUT_HEIGHT));
                                    break;
                                case 1:
                                    valuesPanel.add(Components.getInput(item.getSettingName(), PropertyProvider.get(item.getSettingName()), WIDTH_55, INPUT_HEIGHT));
                                    break;
                            }
                        }
                    }
                });

            }
        }).start();

	}
    
}
