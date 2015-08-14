package cz.encircled.eplayer.model;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.util.DateUtil;
import cz.encircled.eplayer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

public class MediaType {

    private static final String TO_STRING_FORMAT = "Playable name: %s, path: %s, time: %d, watchDate: %d";

    private String name;

    private boolean isSeries;

    private String path;

    private long time;

    private long watchDate;

    private String timeLabel = "";

    public MediaType(@NotNull String path) {
        updatePath(path);
        time = 0L;
        isSeries = false;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public boolean isSeries() {
        return isSeries;
    }

    public void setSeries(boolean isSeries) {
        this.isSeries = isSeries;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return path;
    }

    public String getPath() {
        return path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        updateTimeLabel();
    }

    public boolean exists() {
        return path != null && new java.io.File(path).exists();
    }

    public long getWatchDate() {
        return watchDate;
    }

    public void setWatchDate(long watchDate) {
        this.watchDate = watchDate;
    }

    public void updateTimeLabel() {
        timeLabel = time > 0L && watchDate > 0L ?
                DateUtil.daysBetweenLocalized(watchDate) + ", " +
                        StringUtil.msToTimeLabel(time) : " ";
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, name, path, time, watchDate);
    }

    public void updatePath(@NotNull String path) {
        this.path = path;
        this.name = path.substring(path.lastIndexOf(Constants.SLASH) + Constants.ONE, path.lastIndexOf(Constants.DOT));
    }
}
