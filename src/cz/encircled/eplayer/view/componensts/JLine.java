package cz.encircled.eplayer.view.componensts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class JLine extends JPanel {
	private static final long serialVersionUID = 1L;
	Color color;
    public JLine(int w, Color c){
        color = c;
        setPreferredSize(new Dimension(w, 5));
    }

    @Override
    public void paint(Graphics g){
        g.setColor(color);
        g.drawRect(0, 0, (int) getPreferredSize().getWidth(), 1);
    }
    
}