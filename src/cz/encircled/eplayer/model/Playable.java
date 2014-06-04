package cz.encircled.eplayer.model;

import java.util.regex.Pattern;

public class Playable {

	private static final String TO_STRING_FORMAT = "Playable %d. name: %s, path: %s, time: %d, watchDate: %d";

	private String name;
	
	private String path;
	
	private long time;

    private long watchDate;

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^.*\\.*\\..*$");
	
	public Playable(String path){
		readPath(path);
        time = 0;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
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


    public void readPath(String path) {
        this.path = path;
        name = FILENAME_PATTERN.matcher(path).matches()
                ? path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."))
                : path;
    }
}
