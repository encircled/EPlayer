package cz.encircled.eplayer.model;

import cz.encircled.eplayer.util.DateUtil;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MediaType {

    private static final String TO_STRING_FORMAT = "Playable name: %s, path: %s, time: %d, watchDate: %d";

    private boolean isSeries;

    private long time;

    private long watchDate;

    private String timeLabel = "";

    private String path;

    private String name;

    private String extension;

    private long size;

    private String formattedSize;

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

    public String getExtension() {
        return extension;
    }

    public long getSize() {
        return size;
    }

    public String getFormattedSize() {
        return formattedSize;
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
        File file = new File(path);
        this.path = path;

        String fullName = file.getName();
        int lastDot = fullName.lastIndexOf(".");
        if (lastDot > -1) {
            name = fullName.substring(0, lastDot);
            extension = fullName.substring(lastDot + 1, fullName.length());
        } else {
            name = fullName;
            extension = "";
        }

        size = file.length();
        formattedSize = IOUtil.byteCountToDisplaySize(size);
    }
}
