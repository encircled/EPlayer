package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.util.StringUtil;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

/**
 * Created by Encircled on 29/09/2014.
 */
public class SimpleButton extends Label {

    public SimpleButton(String cssClass, EventHandler<? super MouseEvent> clickHandler) {
        this(cssClass, clickHandler, null);
    }

    public SimpleButton(String cssClass, EventHandler<? super MouseEvent> clickHandler, String text) {

        getStyleClass().add(cssClass);
        setCursor(Cursor.HAND);
        setOnMouseClicked(clickHandler);
        if (StringUtil.isNotBlank(text)) {
            setText(text);
        }
    }


}
