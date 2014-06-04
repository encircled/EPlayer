package cz.encircled.eplayer.view.componensts;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.view.Components;
import cz.encircled.eplayer.view.listeners.QuickNaviActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;

public class QuickNaviButton extends JButton {

	private final Playable playable;
	
	private final Dimension sizeDimension;
	
	public QuickNaviButton(Playable p){
		playable = p;
		sizeDimension = new Dimension(350, 190);
		initialize();
	}
	
	public QuickNaviButton(Playable p, Dimension size){
		playable = p;
		sizeDimension = size;
		initialize();
	}
	
	private void initialize(){
		setText(buildHTML());
        setUI(new BasicButtonUI());
        setPreferredSize(sizeDimension);
        setBackground(Components.MAIN_LIGHT_GRAY_COLOR);
        setBorder(Components.QUICK_NAVI_BUTTON_BORDER);
        setBackground(new Color(245,245,245));
        addMouseListener(new QuickNaviActionListener());
        addMouseListener(Application.getInstance().getHoverMouseListener());
        setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        setToolTipText("At " + playable.getPath());

        ActionListener l = e -> Application.getInstance().deletePlayableCache(playable.getPath().hashCode());
        JButton deleteButton = Components.getButton("x", "", 25, 25, l, new Color(255, 81, 81));
        deleteButton.setBackground(new Color(253, 253, 253));
        deleteButton.setBorder(new LineBorder(new Color(235, 235, 235)));
        add(deleteButton);
	}
	
	private String buildHTML(){
        long time = playable.getTime();
        long s = (time / 1000) % 60;
        long m = (time / (1000*60)) % 60;
        long h = (time / (1000*60*60)) % 24;

        String name = playable.getName();
        if(name.length() > 20)
            name = name.substring(0, 20) + "<br/>" + name.substring(20, name.length());

		StringBuilder builder = new StringBuilder("<html>");
		builder.append("<font size=\"6\">").append(name).append("</font><br/>").append("<font color=\"rgb(85,85,85)\">Current time: ").append(String.format("%02d:%02d:%02d", h,m,s));
		if(!playable.exists())
			builder.append("</font><br/><br/><font size=\"3\" color=\"rgb(85,85,85)\">no longer exists at ").append(playable.getPath()).append("</font>");
		return builder.toString();
	}
	
	public Playable getPlayable(){
		return playable;
	}
	
}
