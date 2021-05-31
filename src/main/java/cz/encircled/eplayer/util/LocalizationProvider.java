package cz.encircled.eplayer.util;

import cz.encircled.eplayer.core.UTF8Control;
import cz.encircled.eplayer.model.AppSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationProvider {

    private static final String NOT_FOUND_VALUE = "?";

    private final static String BUNDLE_NAME = "messages";

    private static ResourceBundle bundle;

    private static Locale locale;

    public static void init(AppSettings settings) {
        locale = new Locale(settings.getLanguage());
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, new UTF8Control());
    }

    @NotNull
    public static String get(@NotNull String key) {
        return bundle.containsKey(key) ? bundle.getString(key) : NOT_FOUND_VALUE + key;
    }

}
