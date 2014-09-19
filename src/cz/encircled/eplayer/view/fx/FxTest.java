package cz.encircled.eplayer.view.fx;

import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Created by Encircled on 18/09/2014.
 */
public class FxTest extends Application {

    private QuickNaviScene quickNaviScene;

    private PlayerScene playerScene;

    private Stage primaryStage;

    private void prepareScenes() {
        quickNaviScene = new QuickNaviScene();
        playerScene = new PlayerScene();
        quickNaviScene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), new Runnable() {
            @Override
            public void run() {
                showPlayerScene();
            }
        });
        playerScene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), new Runnable() {
            @Override
            public void run() {
                showQuickNaviScene();
            }
        });
    }

    private void showQuickNaviScene() {
        primaryStage.setScene(quickNaviScene);
        primaryStage.show();
    }

    private void showPlayerScene() {
        primaryStage.setScene(playerScene);
        primaryStage.show();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        prepareScenes();

        primaryStage.setScene(quickNaviScene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}
