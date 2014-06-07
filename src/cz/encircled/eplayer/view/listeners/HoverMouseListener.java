package cz.encircled.eplayer.view.listeners;

import cz.encircled.eplayer.view.Components;
import cz.encircled.eplayer.view.componensts.EPlayerJButton;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HoverMouseListener extends MouseAdapter {


	@Override
	public void mouseEntered(@NotNull MouseEvent e) {
		Component c = e.getComponent();
        Color foregroundColor = null;
        if(c instanceof EPlayerJButton){
            foregroundColor = ((EPlayerJButton)c).getColorOnHover();
        }
        if(foregroundColor == null)
            foregroundColor = Components.MAIN_BLUE_COLOR;
		c.setForeground(foregroundColor);
		if(c instanceof QuickNaviButton){
			((QuickNaviButton) c).setBorder(Components.QUICK_NAVI_BUTTON_HOVER_BORDER);            // TODO baaaaad!
		}
	}

	@Override
	public void mouseExited(@NotNull MouseEvent e) {
		Component c = e.getComponent(); 
		c.setForeground(Components.MAIN_GRAY_COLOR);
		if(c instanceof QuickNaviButton){
			((QuickNaviButton) c).setBorder(Components.QUICK_NAVI_BUTTON_BORDER);
		}
	}

}
