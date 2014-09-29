package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.fx.components.*;
import javafx.geometry.Insets;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Encircled on 18/09/2014.
 */
@Resource
@Factory(FxFactory.class)
@Runner(FxRunner.class)
public class QuickNaviScreen extends BorderPane {

    private Logger log = LogManager.getLogger();

    @Resource
    private FxView appView;

    private VBox sideMenu;

    private TabPane centerTabPane;

    @Resource
    private Context context;

    @Resource
    private QuickNaviMediaTab quickNaviMediaTab;

    @PostConstruct
    private void initialize() {
        centerTabPane = new TabPane();

        sideMenu = new VBox(0);
        sideMenu.setPadding(new Insets(40, 0, 0, 0));
        sideMenu.getStyleClass().add("side_menu");

        sideMenu.setPrefWidth(120);
        sideMenu.setMaxWidth(120);
        ToggleGroup sideMenuGroup = new ToggleGroup();
        sideMenu.getChildren().addAll(new QuickNaviMenuButton("All", sideMenuGroup), new QuickNaviMenuButton("Series", sideMenuGroup),
                new QuickNaviMenuButton("Films", sideMenuGroup));

        centerTabPane.getStyleClass().add("tabs");
        quickNaviMediaTab.getStyleClass().add("tabs");
        getStyleClass().add("tabs");

        quickNaviMediaTab.onShow();
        centerTabPane.getTabs().add(quickNaviMediaTab);

        centerTabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTab, newTab) -> {
                    System.out.println(newTab);
                    ((MediaTab) newTab).onShow();
                }
        );

        Settings.getList(Settings.FOLDERS_TO_SCAN).forEach(path -> {
            FolderMediaTab tab = context.getComponent(FolderMediaTab.class);
            tab.setPath(path);
            tab.setText(path);
            centerTabPane.getTabs().add(tab);
        });

        setTop(context.getComponent(AppMenuBar.class).getMenuBar());
        setCenter(centerTabPane);
        setLeft(sideMenu);

    }


}
