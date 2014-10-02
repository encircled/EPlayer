package cz.encircled.eplayer.util;

import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 9/14/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtil {

    private static final String HTML_TAG = "<html>";

    private static final String HTML_COSING_TAG = "</html>";

    private static final String HTML_BR_TAG = "<br/>";

    public static final String HTML_PADDING = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    public static boolean isSet(@Nullable String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean notSet(String s) {
        return !isSet(s);
    }

    public static boolean isNotBlank(String filter) {
        return !isBlank(filter);
    }
}
