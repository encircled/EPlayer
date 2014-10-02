package cz.encircled.eplayer.view.fx.components.qn;

import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;

/**
 * Created by Encircled on 23/09/2014.
 */
@Resource
@Scope(Scope.PROTOTYPE)
public class QuickNaviViewButton extends ToggleButton {

    private static final String CSS_CLASS = "menu_button";

    @Resource
    private QuickNaviScreen quickNaviScreen;

    public void initialize(@NotNull String viewName, String text, ToggleGroup toggleGroup) {
        setText(text);
        getStyleClass().add(CSS_CLASS);
        setToggleGroup(toggleGroup);
        setOnAction(event -> quickNaviScreen.viewProperty().set(viewName));
        quickNaviScreen.viewProperty().addListener((observable, oldValue, newValue) -> setSelected(viewName.equals(newValue)));
    }

}
