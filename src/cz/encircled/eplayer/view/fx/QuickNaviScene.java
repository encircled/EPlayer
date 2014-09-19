package cz.encircled.eplayer.view.fx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;

/**
 * Created by Encircled on 18/09/2014.
 */
public class QuickNaviScene extends Scene {

    public QuickNaviScene() {
        super(new VBox(), 400, 400);
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        menuBar.getMenus().add(menuFile);

        add(menuBar);
    }

    private void add(Node node) {
        ((VBox) getRoot()).getChildren().add(node);
    }

}
