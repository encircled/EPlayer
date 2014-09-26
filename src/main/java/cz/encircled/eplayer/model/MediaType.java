package cz.encircled.eplayer.model;

import cz.encircled.eplayer.common.Constants;
import org.jetbrains.annotations.NotNull;

public class MediaType {

    private static final String TO_STRING_FORMAT = "Playable %d. name: %s, path: %s, time: %d, watchDate: %d";

    private String name;

    private boolean isSeries;

    private int hash;

    private String path;

    private long time;

    private long watchDate;

    public MediaType(@NotNull String path) {
        updatePath(path);
        time = 0;
        isSeries = false;
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

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, name.hashCode(), name, path, time, watchDate);
    }

    public void updatePath(@NotNull String path) {
        this.path = path;
        this.hash = path.hashCode();
        this.name = path.substring(path.lastIndexOf(Constants.SLASH) + Constants.ONE, path.lastIndexOf(Constants.DOT));
    }
}
