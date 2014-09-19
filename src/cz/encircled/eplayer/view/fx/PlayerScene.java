package cz.encircled.eplayer.view.fx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;

/**
 * Created by Encircled on 18/09/2014.
 */
public class PlayerScene extends Scene {

    public PlayerScene() {
        super(new VBox());

        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("Player");
        menuBar.getMenus().add(menuFile);

        add(menuBar);
    }

    private void add(Node node) {
        ((VBox) getRoot()).getChildren().add(node);
    }


}
