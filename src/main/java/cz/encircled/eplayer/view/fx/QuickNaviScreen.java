package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.view.fx.components.AppMenuBar;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * @author Encircled on 18/09/2014.
 */
public class QuickNaviScreen extends BorderPane {

    public static final String VIEW_ALL = "viewAll";
    public static final String VIEW_SERIES = "viewSeries";
    public static final String VIEW_FILMS = "viewFilms";
    private Logger log = LogManager.getLogger();
    private VBox sideMenu;
    private TabPane centerTabPane;
    private StringProperty viewProperty;

    private StringProperty filterProperty;

    private ApplicationCore core;
    private FxView fxView;
    private JsBridge jsBridge;

    public QuickNaviScreen(ApplicationCore core, FxView fxView) {
        this.core = core;
        this.fxView = fxView;
    }

    public void refreshCurrentTab() {
        jsBridge.pushRefreshCurrentTab();
//        refreshTab((MediaTab) centerTabPane.getSelectionModel().getSelectedItem());
    }

    public StringProperty viewProperty() {
        return viewProperty;
    }

    public StringProperty filterProperty() {
        return filterProperty;
    }

    // TODO move
    public void addTab(String path) {
        /*FolderMediaTab tab = new FolderMediaTab(core, this);
        tab.setPath(path);
        centerTabPane.getTabs().add(tab);*/
    }

    public void init(@NotNull AppMenuBar menuBar) {
        setTop(menuBar.getMenuBar());
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        URL html = this.getClass().getClassLoader().getResource("html/quicknavi.html");
        if (html == null) {
            throw new RuntimeException("Html not found");
        }
        engine.load(html.toExternalForm());

        JSObject windowObject = (JSObject) engine.executeScript("window");
        jsBridge = new JsBridge(core, windowObject);
        windowObject.setMember("bridge", jsBridge);
        initializeListeners(fxView);

        setCenter(webView);
        /*QuickNaviMediaTab quickNaviMediaTab = new QuickNaviMediaTab(core, this);
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

        initializeListeners(fxView);
        core.getEventObserver().listen(Event.contextInitialized, (event, isPlaying, param) -> {
            refreshCurrentTab();
        });

        Settings.folders_to_scan.getList().forEach(this::addTab);

        setTop(menuBar.getMenuBar());
        setCenter(centerTabPane);
        setLeft(sideMenu);

        HBox statusBar = new HBox();
        statusBar.setPrefHeight(15);
        statusBar.setPadding(new Insets(5, 5, 5, 5));

        SimpleButton refreshButton = new SimpleButton("refresh_button", event -> refreshCurrentTab());

        statusBar.getChildren().add(refreshButton);
        setBottom(statusBar);*/
    }

    private void initializeListeners(@NotNull FxView fxView) {
        fxView.screenChangeProperty().addListener((observable, oldValue, newValue) -> {
            if (FxView.QUICK_NAVI_SCREEN.equals(newValue)) {
                refreshCurrentTab();
            }
        });

        /*centerTabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTab, newTab) -> refreshTab((MediaTab) newTab)
        );*/

        /*filterProperty.addListener((observable, oldValue, newValue) -> {
            refreshCurrentTab();
        });*/
    }

}
