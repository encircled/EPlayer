package cz.encircled.eplayer.util;

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

    public static boolean isSet(String s){
        return s!= null && !s.isEmpty();
    }

    public static boolean notSet(String s){
    	return !isSet(s);
    }

    public static String toHtml(String s, int lineBreakAt, String padding){
        StringBuilder sb = new StringBuilder(HTML_TAG);
        if(padding != null)
            sb.append(padding);
        int addedLength = sb.length();
        sb.append(s);
        if(lineBreakAt > 0 && s.length() > lineBreakAt)
            sb.insert(addedLength + lineBreakAt, padding == null ? HTML_BR_TAG : HTML_BR_TAG + padding);
        sb.append(HTML_COSING_TAG);
        return sb.toString();
    }

}
