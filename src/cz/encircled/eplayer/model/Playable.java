package cz.encircled.eplayer.model;

public class Playable {

	private String name;
	
	private String path;
	
	private int time;
	
	
	public Playable(String name, String path){
		this.name = name;
		this.path = path;
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

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public boolean exists(){
		return path != null && new java.io.File(path).exists();
	}
	
}
