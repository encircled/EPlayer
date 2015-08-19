package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.view.fx.components.AppMenuBar;
import javafx.scene.layout.BorderPane;
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

    private Logger log = LogManager.getLogger();

    private ApplicationCore core;
    private FxView fxView;
    private JsBridge jsBridge;

    public QuickNaviScreen(ApplicationCore core, FxView fxView) {
        this.core = core;
        this.fxView = fxView;
    }

    public void refreshCurrentTab() {
        jsBridge.refreshCurrentTab();
    }

    public void addTab(String path) {
        jsBridge.pushToUi("addTabCallback", new JsBridge.TabDto(path, true));
    }

    public void init(@NotNull AppMenuBar menuBar) {
        setTop(menuBar.getMenuBar());
        WebView webView = new WebView();
        webView.setOnDragDropped(fxView.getNewTabDropHandler());
        WebEngine engine = webView.getEngine();
        URL html = this.getClass().getClassLoader().getResource("html/quicknavi.html");
        if (html == null) {
            throw new RuntimeException("Html not found");
        }
        engine.load(html.toExternalForm());

        JSObject windowObject = (JSObject) engine.executeScript("window");
        jsBridge = new JsBridge(core, windowObject);
        windowObject.setMember("bridge", jsBridge);
        initializeListeners();

        setCenter(webView);
    }

    private void initializeListeners() {
        fxView.screenChangeProperty().addListener((observable, oldValue, newValue) -> {
            if (FxView.QUICK_NAVI_SCREEN.equals(newValue)) {
                refreshCurrentTab();
            }
        });
    }

}
