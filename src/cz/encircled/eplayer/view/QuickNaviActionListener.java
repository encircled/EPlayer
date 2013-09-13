package cz.encircled.eplayer.view;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;

public class QuickNaviActionListener implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
		Playable p = ((QuickNaviButton)e.getComponent()).getPlayable();
		Application.getInstance().play(p);
		System.out.println("play");
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	

}
