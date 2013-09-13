package cz.encircled.eplayer.view;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class BackgroundFocusListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {
       e.getComponent().setBackground(new Color(252,252,252));
    }

    @Override
    public void focusLost(FocusEvent e) {
    	e.getComponent().setBackground(Color.WHITE);
    }
	
}
