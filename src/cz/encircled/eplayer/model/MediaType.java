package cz.encircled.eplayer.model;

import cz.encircled.eplayer.common.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MediaType {

    private static final String TO_STRING_FORMAT = "Playable %d. name: %s, path: %s, time: %d, watchDate: %d";

	private String name;

    private int hash;
	
	private String path;
	
	private long time;

    private long watchDate;

	public MediaType(@NotNull String path){
		readPath(path);
        time = 0;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

    public int getHash() {
        return hash;
    }

    public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public boolean exists(){
		return path != null && new java.io.File(path).exists();
	}

    public long getWatchDate() {
        return watchDate;
    }

    public void setWatchDate(long watchDate) {
        this.watchDate = watchDate;
    }

    @Override
    public String toString(){
    	return String.format(TO_STRING_FORMAT, name.hashCode(), name, path, time, watchDate);
    }


    public void readPath(@NotNull String path) {
        this.path = path;
        hash = path.hashCode();
        name = path.substring(path.lastIndexOf(Constants.SLASH) + Constants.ONE, path.lastIndexOf(Constants.DOT));
    }
}
