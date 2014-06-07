package cz.encircled.eplayer.view;

import cz.encircled.eplayer.util.GUIConstants;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.componensts.EPlayerJButton;
import cz.encircled.eplayer.view.componensts.JLine;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;

public class Components {
	
	public final static Color MAIN_BLUE_COLOR = new Color(73, 117, 255);
	
	public final static Color MAIN_GRAY_COLOR = new Color(85, 85, 85);
	
	public final static Color MAIN_LIGHT_GRAY_COLOR = new Color(235,235,235);
	
	public final static Border QUICK_NAVI_BUTTON_BORDER = 
				new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_BLUE_COLOR), new LineBorder(new Color(230,230,230)));
	
	public final static Border QUICK_NAVI_BUTTON_HOVER_BORDER = 
				new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_GRAY_COLOR), new LineBorder(new Color(230,230,230)));
	
	@NotNull
    public static JLine getJLine(int width, Color color){
		return new JLine(width, color);
	}
	
    @NotNull
    public static JLabel getLabel(String text, int w, int h, boolean addPadding){
        text = StringUtil.toHtml(text, 20, addPadding ? StringUtil.HTML_PADDING : null);
        JLabel l = new JLabel(text);
        l.setPreferredSize(new Dimension(w, h));
        return l;
    }
    
    @NotNull
    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height){
        return getButton(text, actionCommand, width, height, GUIConstants.DEFAULT_ACTION_LISTENER);
    }

    @NotNull
    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height, ActionListener actionListener){
        return getButton(text, actionCommand, width, height, actionListener, null);
    }

    @NotNull
    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height, ActionListener actionListener, Color colorOnHover){
        EPlayerJButton b = new EPlayerJButton(text, colorOnHover);
        b.setUI(new BasicButtonUI());
        b.setActionCommand(actionCommand);
        b.setPreferredSize(new Dimension(width, height));
        b.setBackground(Components.MAIN_LIGHT_GRAY_COLOR);
        b.setBorderPainted(false);
        b.addActionListener(actionListener);
        b.addMouseListener(GUIConstants.HOVER_MOUSE_LISTENER);
        return b;
    }
    
    @NotNull
    public static JTextField getInput(String name, String value, int width, int height){
        JTextField f = new JTextField(value);
        f.setName(name);
        f.setPreferredSize(new Dimension(width, height));
        f.addFocusListener(GUIConstants.BACKGROUND_FOCUS_LISTENER);

        Border bottomBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_BLUE_COLOR);
        f.setBorder(new CompoundBorder(bottomBorder, new EmptyBorder(0, 6, 0, 0)));

        return f;
    }
    
    @NotNull
    public static JMenuItem getMenuItem(@NotNull String textToLocalize, String actionCommand){
    	JMenuItem i = new JMenuItem(MessagesProvider.get(textToLocalize));
    	i.setActionCommand(actionCommand);
    	i.addActionListener(GUIConstants.DEFAULT_ACTION_LISTENER);
    	return i;
    }

}
