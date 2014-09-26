package cz.encircled.eplayer.view.swing.componensts;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 9/14/13
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class EPlayerJButton extends JButton implements ColorOnHover {

    private Color colorOnHover;

    public EPlayerJButton(String text, Color colorOnHover) {
        super(text);
        this.colorOnHover = colorOnHover;
    }

    public EPlayerJButton(Color colorOnHover) {
        super();
        this.colorOnHover = colorOnHover;
    }

    public EPlayerJButton(String text) {
        super(text);
    }

    @Override
    public Color getColorOnHover() {
        return colorOnHover;
    }

}
