package cz.encircled.eplayer.view.fx.components;

import javafx.scene.control.Label;

/**
 * Created by Encircled on 23/09/2014.
 */
public class QuickNaviMenuButton extends Label {

    public QuickNaviMenuButton(String text) {
        super(text);
        setBackground(null);
        getStyleClass().add("menu_button");
        setBorder(null);
    }


}
