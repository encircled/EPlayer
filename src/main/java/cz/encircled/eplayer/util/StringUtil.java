package cz.encircled.eplayer.util;

import cz.encircled.eplayer.common.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 9/14/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringUtil {

    private static final char TIME_SEPARATOR = ':';

    public static String msToTimeLabel(long ms) {
        long h = TimeUnit.MILLISECONDS.toHours(ms);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
        long s = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));

        StringBuilder sb = new StringBuilder();
        appendZeroIfMissing(sb, h);
        sb.append(h).append(TIME_SEPARATOR);
        appendZeroIfMissing(sb, m);
        sb.append(m).append(TIME_SEPARATOR);
        appendZeroIfMissing(sb, s);
        sb.append(s);

        return sb.toString();
    }

    private static void appendZeroIfMissing(@NotNull StringBuilder sb, long digit) {
        if (digit < Constants.TEN)
            sb.append(Constants.ZERO_STRING);
    }

}
