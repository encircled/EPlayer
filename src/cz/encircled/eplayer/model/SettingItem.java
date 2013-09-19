package cz.encircled.eplayer.model;

public class SettingItem {
	
	public final static int INPUT_TEXT_ELEMENT = 0;
	
	public final static int COMBOBOX_ELEMENT = 1;
	
	private String viewText;
	
	private String settingName;
	
	private int elementType;
	
	public SettingItem(String viewText, String settingName, int elementType){
		this.viewText = viewText;
		this.settingName = settingName;
		this.elementType = elementType;
	}

	public String getViewText() {
		return viewText;
	}

	public void setViewText(String viewText) {
		this.viewText = viewText;
	}

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	public int getElementType() {
		return elementType;
	}

	public void setElementType(int elementType) {
		this.elementType = elementType;
	}
	
}