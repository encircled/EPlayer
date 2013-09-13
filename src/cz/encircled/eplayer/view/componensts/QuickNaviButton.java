package cz.encircled.eplayer.view.componensts;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.view.Components;
import cz.encircled.eplayer.view.QuickNaviActionListener;

public class QuickNaviButton extends JButton {

	private static final long serialVersionUID = 1L;

	private final Playable playable;
	
	private final Dimension sizeDimemsion;
	
	public QuickNaviButton(Playable p){
		playable = p;
		sizeDimemsion = new Dimension(280, 120);
		initialize();
	}
	
	public QuickNaviButton(Playable p, Dimension size){
		playable = p;
		sizeDimemsion = size;
		initialize();
	}
	
	private final void initialize(){
		setText(buildHTML());
        setUI(new BasicButtonUI());
        setPreferredSize(sizeDimemsion);
        setBackground(Components.MAIN_LIGHT_GRAY_COLOR);
        setBorder(Components.QUICK_NAVI_BUTTON_BORDER);
        setBackground(new Color(245,245,245));
        addMouseListener(new QuickNaviActionListener());
        addMouseListener(Application.getInstance().getHoverMouseListener());
	}
	
	private String buildHTML(){
		StringBuilder builder = new StringBuilder("<html>");
		builder.append("<font size=\"6\">").append(playable.getName()).append("</font><br/>").append("<font color=\"rgb(85,85,85)\">Currnet time: " + playable.getTime());
		if(!playable.exists())
			builder.append("</font><br/><br/><font size=\"3\" color=\"rgb(85,85,85)\">no longer exists at " + playable.getPath() + "</font>");
		return builder.toString();
	}
	
	public Playable getPlayable(){
		return playable;
	}
	
}
