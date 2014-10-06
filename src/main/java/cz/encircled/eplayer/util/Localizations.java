package cz.encircled.eplayer.util;

import cz.encircled.eplayer.core.UTF8Control;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localizations {

    public static final String DEFAULT_LANGUAGE = "en";

    private static final String NOT_FOUND_VALUE = "?";

    private final static String BUNDLE_NAME = "messages";

    private static ResourceBundle bundle;

    private static Locale locale;

    static {
        locale = new Locale(Settings.get(Settings.LANGUAGE, DEFAULT_LANGUAGE));
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, new UTF8Control());
    }

    @NotNull
    public static String get(@NotNull String key) {
        return bundle.containsKey(key) ? bundle.getString(key) : NOT_FOUND_VALUE + key;
    }

    @NotNull
    public static Locale getUsedLocale() {
        return locale;
    }

}
