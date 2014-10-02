package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.fx.components.AppMenuBar;
import cz.encircled.eplayer.view.fx.components.ImageButton;
import cz.encircled.eplayer.view.fx.components.qn.QuickNaviViewButton;
import cz.encircled.eplayer.view.fx.components.qn.tab.FolderMediaTab;
import cz.encircled.eplayer.view.fx.components.qn.tab.MediaTab;
import cz.encircled.eplayer.view.fx.components.qn.tab.QuickNaviMediaTab;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

    public static final String VIEW_ALL = "viewAll";

    public static final String VIEW_SERIES = "viewSeries";

    public static final String VIEW_FILMS = "viewFilms";

    private StringProperty viewProperty;

    private StringProperty filterProperty;

    public void refreshCurrentTab() {
        refreshTab((MediaTab) centerTabPane.getSelectionModel().getSelectedItem());
    }

    public StringProperty viewProperty() {
        return viewProperty;
    }

    public StringProperty filterProperty() {
        return filterProperty;
    }

    @PostConstruct
    private void initialize() {
        filterProperty = new SimpleStringProperty();
        viewProperty = new SimpleStringProperty();
        viewProperty.addListener(observable -> refreshCurrentTab());

        centerTabPane = new TabPane();
        centerTabPane.getTabs().add(quickNaviMediaTab);

        initializeSideMenu();
        viewProperty.set(VIEW_ALL);

        centerTabPane.getStyleClass().add("tabs");
        quickNaviMediaTab.getStyleClass().add("tabs");
        getStyleClass().add("tabs");

        initializeListeners();

        Settings.getList(Settings.FOLDERS_TO_SCAN).forEach(path -> {
            FolderMediaTab tab = context.getComponent(FolderMediaTab.class);
            tab.setPath(path);
            centerTabPane.getTabs().add(tab);
        });

        setTop(context.getComponent(AppMenuBar.class).getMenuBar());
        setCenter(centerTabPane);
        setLeft(sideMenu);

        HBox statusBar = new HBox();
        statusBar.setPrefHeight(15);
        statusBar.setPadding(new Insets(5, 5, 5, 5));

        ImageButton refreshButton = new ImageButton("refresh_button", event -> refreshCurrentTab());

        statusBar.getChildren().add(refreshButton);
        setBottom(statusBar);
    }

    private void refreshTab(MediaTab tab) {
        switch (viewProperty.get()) {
            case VIEW_ALL:
                tab.showAll();
                break;
            case VIEW_SERIES:
                tab.showSeries();
                break;
            case VIEW_FILMS:
                tab.showFilms();
                break;
        }
    }

    private void initializeListeners() {
        appView.screenChangeProperty().addListener((observable, oldValue, newValue) -> {
            if (FxView.QUICK_NAVI_SCREEN.equals(newValue)) {
                refreshCurrentTab();
            }
        });

        centerTabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTab, newTab) -> refreshTab((MediaTab) newTab)
        );

        filterProperty.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                refreshCurrentTab();

            });
        });
    }

    private void initializeSideMenu() {
        sideMenu = new VBox(0);
        sideMenu.setPadding(new Insets(40, 0, 0, 0));
        sideMenu.getStyleClass().add("side_menu");

        sideMenu.setPrefWidth(120);
        sideMenu.setMaxWidth(120);

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search_input");

        searchField.addEventHandler(KeyEvent.KEY_TYPED, event -> filterProperty.setValue(searchField.getText()));


        // TODO search
        sideMenu.getChildren().add(searchField);
        VBox.setMargin(searchField, new Insets(0, 2, 10, 2));

        ToggleGroup sideMenuGroup = new ToggleGroup();

        QuickNaviViewButton films = context.getComponent(QuickNaviViewButton.class);
        films.initialize(VIEW_FILMS, "Films", sideMenuGroup);

        QuickNaviViewButton series = context.getComponent(QuickNaviViewButton.class);
        series.initialize(VIEW_SERIES, "Series", sideMenuGroup);

        QuickNaviViewButton all = context.getComponent(QuickNaviViewButton.class);
        all.initialize(VIEW_ALL, "All", sideMenuGroup);

        sideMenu.getChildren().addAll(all, series,
                films);
    }

}
