package cz.encircled.eplayer.view.listeners;

import cz.encircled.eplayer.view.Components;
import cz.encircled.eplayer.view.componensts.EPlayerJButton;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class HoverMouseListener implements MouseListener {

	public HoverMouseListener(){
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Component c = e.getComponent();
        Color foregroundColor = null;
        if(c instanceof EPlayerJButton){
            foregroundColor = ((EPlayerJButton)c).getColorOnHover();
        }
        if(foregroundColor == null)
            foregroundColor = Components.MAIN_BLUE_COLOR;
		c.setForeground(foregroundColor);
		if(c instanceof QuickNaviButton){
			((QuickNaviButton) c).setBorder(Components.QUICK_NAVI_BUTTON_HOVER_BORDER);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Component c = e.getComponent(); 
		c.setForeground(Components.MAIN_GRAY_COLOR);
		if(c instanceof QuickNaviButton){
			((QuickNaviButton) c).setBorder(Components.QUICK_NAVI_BUTTON_BORDER);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

}
