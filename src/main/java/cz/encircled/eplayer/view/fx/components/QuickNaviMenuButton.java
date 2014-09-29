package cz.encircled.eplayer.view.fx.components;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * Created by Encircled on 23/09/2014.
 */
public class QuickNaviMenuButton extends ToggleButton {

    public QuickNaviMenuButton(String text, ToggleGroup toggleGroup) {
        super(text);
        setBackground(null);
        getStyleClass().add("menu_button");
        setBorder(null);
        setToggleGroup(toggleGroup);
    }

}
