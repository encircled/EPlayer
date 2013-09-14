package cz.encircled.eplayer.model;

import java.util.regex.Pattern;

public class Playable {

	private String name;
	
	private String path;
	
	private long time;

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^.*\\.*\\..*$");
	
	public Playable(String path){
		this.path = path;
        name = FILENAME_PATTERN.matcher(path).matches()
                                             ? path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."))
                                             : path;
        time = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

}
