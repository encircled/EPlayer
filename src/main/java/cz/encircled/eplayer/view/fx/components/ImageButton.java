package cz.encircled.eplayer.view.fx.components;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;

/**
 * Created by Encircled on 29/09/2014.
 */
public class ImageButton extends Control {

    public ImageButton(String cssClass, EventHandler<? super MouseEvent> clickHandler) {
        getStyleClass().add(cssClass);
        setCursor(Cursor.HAND);
        setOnMouseClicked(clickHandler);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ImageButtonSkin(this);
    }

    class ImageButtonSkin extends SkinBase {

        public ImageButtonSkin(ImageButton b) {
            super(b);
        }
    }

}
