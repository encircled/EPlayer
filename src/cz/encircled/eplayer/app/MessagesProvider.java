package cz.encircled.eplayer.app;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComponent;

public class MessagesProvider {

	private static final String DEFAULT_LANGUAGE = "ru";

	private static final String NOT_FOUND_VALUE = "?";

	private final static String BUNDLE_NAME = "messages";
	
	private static ResourceBundle bundle;
	
	public final static void initialize(){
		Locale l = new Locale(PropertyProvider.get(LocalizedMessages.LANGUAGE, DEFAULT_LANGUAGE));
		bundle = ResourceBundle.getBundle(BUNDLE_NAME, l, new UTF8Control());
		JComponent.setDefaultLocale(l);
	}
	
	public static String get(String key){
		return bundle.containsKey(key) ? bundle.getString(key) : NOT_FOUND_VALUE;
	}
	
}
