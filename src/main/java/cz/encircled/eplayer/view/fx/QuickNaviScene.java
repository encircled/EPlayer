package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.fx.components.FolderMediaTab;
import cz.encircled.eplayer.view.fx.components.MediaTab;
import cz.encircled.eplayer.view.fx.components.QuickNaviMediaTab;
import cz.encircled.eplayer.view.fx.components.QuickNaviMenuButton;
import cz.encircled.eplayer.view.swing.menu.MenuBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Encircled on 18/09/2014.
 */
@Resource
@Factory(FxFactory.class)
@Runner(FxRunner.class)
public class QuickNaviScene extends Scene {

    @Resource
    private MenuBuilder menuBuilder;

    private VBox sideMenu;

    private StackPane topPane;

    private TabPane centerTabPane;

    @Resource
    private Settings settings;

    @Resource
    private Context context;

    @Resource
    private QuickNaviMediaTab quickNaviMediaTab;

    @PostConstruct
    private void initialize() {
        getStylesheets().add("/stylesheet.css");
        BorderPane borderPane = (BorderPane) getRoot();

        sideMenu = new VBox(0);
        sideMenu.setPadding(new Insets(40, 0, 0, 0));
        sideMenu.getStyleClass().add("side_menu");

        sideMenu.setPrefWidth(120);
        sideMenu.setMaxWidth(120);
        sideMenu.getChildren().addAll(new QuickNaviMenuButton("All"), new QuickNaviMenuButton("Series"));

        topPane = new StackPane();
        topPane.getChildren().add(menuBuilder.getFxMenu());

        centerTabPane.getStyleClass().add("tabs");
        quickNaviMediaTab.getStyleClass().add("tabs");
        borderPane.getStyleClass().add("tabs");

        quickNaviMediaTab.onShow();
        centerTabPane.getTabs().add(quickNaviMediaTab);

        centerTabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTab, newTab) -> {
                    System.out.println(newTab);
                    ((MediaTab) newTab).onShow();
                }
        );

        settings.getList(Settings.FOLDERS_TO_SCAN).forEach(path -> {
            FolderMediaTab tab = context.getComponent(FolderMediaTab.class);
            tab.setPath(path);
            tab.setText(path);
            centerTabPane.getTabs().add(tab);
        });

        borderPane.setCenter(centerTabPane);
        borderPane.setTop(topPane);
        borderPane.setLeft(sideMenu);
    }

    public QuickNaviScene() {
        super(new BorderPane());
        centerTabPane = new TabPane();
    }


}
