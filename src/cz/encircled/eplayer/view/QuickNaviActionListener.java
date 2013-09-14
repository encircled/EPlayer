package cz.encircled.eplayer.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;

public class QuickNaviActionListener extends MouseAdapter {

	@Override
	public void mouseClicked(MouseEvent e) {
		Playable p = ((QuickNaviButton)e.getComponent()).getPlayable();
		Application.getInstance().play(p);
	}

}
