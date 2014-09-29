package cz.encircled.eplayer.view;

/**
 * Created by Encircled on 16/09/2014.
 */
public interface AppView {

    static final String TITLE = "EPlayer";

    void showPlayer();

    void showQuickNavi();

//    void addTabForFolder(@NotNull String tabName, @NotNull String path);

    void setFullScreen(boolean fullScreen);

    void openMedia();

}
