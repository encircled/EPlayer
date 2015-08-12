package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.model.SeriesVideo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Encircled on 22/09/2014.
 */
public class SeriesFinder {

    private static final Logger log = LogManager.getLogger();

    private static final Pattern seriesPattern = Pattern.compile("(?i).*s[\\d]{1,2}.?e[\\d]{1,2}.*");

    private static final Pattern replacePattern = Pattern.compile("(?i)s[\\d]{1,2}.?e[\\d]{1,2}");

    @NotNull
    private Matcher seriesMatcher = seriesPattern.matcher("");

    @NotNull
    public Map<String, SeriesVideo> findSeries(@NotNull Collection<MediaType> mediaTypes) {
        Map<String, SeriesVideo> result = new HashMap<>();
        mediaTypes.stream().forEach(mediaType -> {
            if (seriesMatcher.reset(mediaType.getName()).matches()) {
                String replacedName = replacePattern.matcher(mediaType.getName()).replaceAll("");
                SeriesVideo seriesVideo = result.get(replacedName);
                if (seriesVideo == null) {
                    log.debug("New series: {}", replacedName);
                    seriesVideo = new SeriesVideo();
                    seriesVideo.setName(replacedName);
                    result.put(replacedName, seriesVideo);
                }
                mediaType.setSeries(true);
                seriesVideo.addMediaType(mediaType);
            }
        });
        return result;
    }

}
