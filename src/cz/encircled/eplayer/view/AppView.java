package cz.encircled.eplayer.view;

import cz.encircled.eplayer.model.MediaType;
import javafx.scene.image.PixelWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by Encircled on 16/09/2014.
 */
public interface AppView {

    static final String TITLE = "EPlayer";

    void showPlayer();

    void addTabForFolder(@NotNull String tabName);

    void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType);


    void showQuickNavi(@NotNull Collection<MediaType> mediaType);

    void enterFullScreen();

    void exitFullScreen();

    void showShutdownTimeChooser();

    void enableSubtitlesMenu(boolean isEnabled);

    void showFilterInput();

    void hideFilterInput();

    void openMedia();

    PixelWriter getPixelWriter();
}
