package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.eplayer.core.SeriesFinder;
import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.model.SeriesVideo;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.fx.FxUtil;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import cz.encircled.eplayer.view.fx.components.qn.QuickNaviButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
@Scope(Scope.PROTOTYPE)
public abstract class MediaTab extends Tab {

    private final static Logger log = LogManager.getLogger();

    protected FlowPane mainPane;

    @Resource
    private Context context;

    @Resource
    private SeriesFinder seriesFinder;

    @Resource
    private QuickNaviScreen quickNaviScreen;

    public MediaTab() {
        mainPane = new FlowPane(10, 10);
        ScrollPane scrollPane = new ScrollPane(mainPane);

        mainPane.getStyleClass().add("tabs");
        scrollPane.getStyleClass().add("tabs");

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(10, 10, 10, 10));

        setContent(scrollPane);
        mainPane.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(20));
    }

    public void showAll() {
        // TODO cache if needed
        FxUtil.workInNormalThread(() -> {
            repaintButtons(getAllMediaTypesFiltered());
        });
    }

    public void showSeries() {
        FxUtil.workInNormalThread(() -> {
            repaintSeriesButtons(getSeriesMediaTypes().values());
        });
    }

    private Collection<MediaType> getAllMediaTypesFiltered() {
        Collection<MediaType> mediaTypes = getAllMediaTypes();
        log.debug(quickNaviScreen.filterProperty().get());
        String filter = quickNaviScreen.filterProperty().get();
        if (StringUtil.isNotBlank(filter)) {
            StringBuilder sb = new StringBuilder("(?i).*");
            Pattern p = Pattern.compile(sb.append(filter.replaceAll(" ", ".*")).append(".*").toString());
            Matcher m = p.matcher("");
            return mediaTypes.stream().filter(media -> m.reset(media.getName()).matches()).collect(Collectors.toList());
        }
        return mediaTypes;
    }

    protected abstract Collection<MediaType> getAllMediaTypes();

    public void showFilms() {
        // TODO optimize?
        Collection<MediaType> allMediaTypes = getAllMediaTypesFiltered();
        Map<String, SeriesVideo> series = seriesFinder.findSeries(allMediaTypes);
        series.forEach((key, s) -> {
            allMediaTypes.removeAll(s.getMediaTypes().keySet());
        });
        FxUtil.workInNormalThread(() -> {
            repaintButtons(allMediaTypes);
        });
    }

    public Map<String, SeriesVideo> getSeriesMediaTypes() {
        return seriesFinder.findSeries(getAllMediaTypes());
    }

    private void repaintButtons(Collection<MediaType> mediaTypes) {
        Platform.runLater(() -> {
            final Collection<QuickNaviButton> buttons = new ArrayList<>(mediaTypes.size());
            mediaTypes.forEach(media -> {
                QuickNaviButton button = context.getComponent(QuickNaviButton.class);
                button.setMediaType(media);
                buttons.add(button);
            });

            log.debug("Media tab on show, add {} buttons", buttons.size());
            buttons.forEach(QuickNaviButton::initialize);
            mainPane.getChildren().setAll(buttons);
        });
    }

    private void repaintSeriesButtons(Collection<SeriesVideo> series) {
        Platform.runLater(() -> {
            final Collection<QuickNaviButton> buttons = new ArrayList<>(series.size());
            series.forEach(media -> {
                QuickNaviButton button = context.getComponent(QuickNaviButton.class);
                button.setMediaType(media.getLast());
                button.setSeriesVideo(media);
                buttons.add(button);
            });

            log.debug("Media tab on show, add {} buttons", buttons.size());
            buttons.forEach(QuickNaviButton::initialize);
            mainPane.getChildren().setAll(buttons);
        });
    }

}
