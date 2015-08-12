package cz.encircled.eplayer.view.fx.components.qn;

import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import javafx.application.Platform;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.NotNull;

/**
 * @author Encircled on 23/09/2014.
 */
public class QuickNaviViewButton extends ToggleButton {

    private static final String CSS_CLASS = "menu_button";

    public QuickNaviViewButton(@NotNull QuickNaviScreen quickNaviScreen, @NotNull String viewName, String text, ToggleGroup toggleGroup) {
        Platform.runLater(() -> {
            setText(text);
            getStyleClass().add(CSS_CLASS);
            setToggleGroup(toggleGroup);
            setOnAction(event -> quickNaviScreen.viewProperty().set(viewName));
            quickNaviScreen.viewProperty().addListener((observable, oldValue, newValue) -> setSelected(viewName.equals(newValue)));
        });
    }

}
