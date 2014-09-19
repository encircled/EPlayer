package cz.encircled.eplayer.view;

import cz.encircled.eplayer.view.swing.JSBridge;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Created by Encircled on 15/09/2014.
 */
public class FX extends Application {

    public JSBridge jsBridge = new JSBridge();

    public FX() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        primaryStage.setTitle("Hello World!");
        StackPane root = new StackPane();
        Scene scene = new Scene(new VBox(), 400, 350);
        primaryStage.setScene(scene);

        MenuBar menuBar = new MenuBar();

        scene.getStylesheets().add("/stylesheet.css");

        // --- Menu File
        Menu menuFile = new Menu("File");

        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");

        // --- Menu View
        Menu menuView = new Menu("View");

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);


        ((VBox) scene.getRoot()).getChildren().addAll(menuBar);

        Button b = new Button("B1");
        b.getStyleClass().add("qn");
        TitledPane t1 = new TitledPane("T1", b);
        TitledPane t2 = new TitledPane("T2", new Button("B2"));
        TitledPane t3 = new TitledPane("T3", new Button("B3"));
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(t1, t2, t3);

        ((VBox) scene.getRoot()).getChildren().add(accordion);
//        root.getChildren().add(accordion);

        primaryStage.show();
//        Scale scale = new Scale(1, 1, 0, 0);
//        scale.setPivotX(0);
//        scale.setPivotY(0);
//        scene.getRoot().getTransforms().setAll(scale);

        /*
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.load(this.getClass().getResource("/view.html").toExternalForm());


        root.getChildren().add(webView);
        primaryStage.show();

        windowObject = (JSObject) webView.getEngine().executeScript("window");
        windowObject.setMember("app", jsBridge);

//        primaryStage.setFullScreen(true); */
    }

    public static JSObject windowObject;

    public static void main(String[] args) {
        launch();
    }

}
