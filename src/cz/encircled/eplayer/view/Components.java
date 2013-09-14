package cz.encircled.eplayer.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.app.LocalizedMessages;
import cz.encircled.eplayer.app.MessagesProvider;
import cz.encircled.eplayer.view.componensts.EPlayerJButton;
import cz.encircled.eplayer.view.componensts.JLine;

public class Components {
	
	private static final String LABEL_PADDING = "      ";

	public final static Color MAIN_BLUE_COLOR = new Color(73, 117, 255);
	
	final static Color MAIN_GRAY_COLOR = new Color(85, 85, 85);
	
	public final static Color MAIN_LIGHT_GRAY_COLOR = new Color(235,235,235);
	
	public final static Border QUICK_NAVI_BUTTON_BORDER = 
				new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_BLUE_COLOR), new LineBorder(new Color(230,230,230)));
	
	public final static Border QUICK_NAVI_BUTTON_HOVER_BORDER = 
				new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_GRAY_COLOR), new LineBorder(new Color(230,230,230)));
	
	public static JLine getJLine(int width, Color color){
		return new JLine(width, color);
	}
	
    public static JLabel getLabel(String text, int w, int h, boolean addPadding){
        JLabel l = addPadding ? new JLabel(LABEL_PADDING + text) : new JLabel(text);
        l.setPreferredSize(new Dimension(w, h));
        return l;
    }
    
    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height){
        return getButton(text, actionCommand, width, height, Application.getInstance().getActionsMouseListener());
    }

    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height, ActionListener actionListener){
        return getButton(text, actionCommand, width, height, actionListener, null);
    }

    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height, ActionListener actionListener, Color colorOnHover){
        EPlayerJButton b = new EPlayerJButton(text, colorOnHover);
        b.setUI(new BasicButtonUI());
        b.setActionCommand(actionCommand);
        b.setPreferredSize(new Dimension(width, height));
        b.setBackground(Components.MAIN_LIGHT_GRAY_COLOR);
        b.setBorderPainted(false);
        b.addActionListener(actionListener);
        b.addMouseListener(Application.getInstance().getHoverMouseListener());
        return b;
    }
    
    public static JTextField getInput(String name, String value, int width, int height){
        JTextField f = new JTextField(value);
        f.setName(name);
        f.setPreferredSize(new Dimension(width, height));
        f.addFocusListener(Application.getInstance().getBackgroundFocusListener());

        Border bottomBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_BLUE_COLOR);
        f.setBorder(new CompoundBorder(bottomBorder, new EmptyBorder(0, 6, 0, 0)));
        return f;
    }
    
    public static JTextField getInput(String name, int width, int height){
        return getInput(name, "", width, height);
    }
    
    public static JMenuItem getMenuItem(String textToLocalize, String actionCommand){
    	JMenuItem i = new JMenuItem(MessagesProvider.get(textToLocalize));
    	i.setActionCommand(actionCommand);
    	i.addActionListener(Application.getInstance().getActionsMouseListener());
    	return i;
    }

}
