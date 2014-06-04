package cz.encircled.eplayer.view.componensts;

import javax.swing.*;
import java.awt.*;

public class JLine extends JPanel {

	private final Color color;

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