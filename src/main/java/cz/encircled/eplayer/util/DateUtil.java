package cz.encircled.eplayer.util;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Encircled on 5/10/2014.
 */
public class DateUtil {

    private static final float MS_IN_DAY = 86400000L;

    public static String getLocaleFormatted(long time) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, LocalizationProvider.getUsedLocale()).format(new Date(time));
    }

    private static void resetTime(@NotNull Calendar c1) {
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
    }

    public static int daysBetween(long time) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c2.setTime(new Date(time));
        resetTime(c1);
        resetTime(c2);

        return (int) Math.floor(((float) Math.abs(c1.getTime().getTime() - c2.getTime().getTime())) / MS_IN_DAY);
    }

    public static String daysBetweenLocalized(long time) {
        int days = daysBetween(time);
        String localized;
        switch (days) {
            case 0:
                localized = Localization.today.ln();
                break;
            case 1:
                localized = Localization.yesterday.ln();
                break;
            default:
                localized = days + " " + Localization.daysAgo.ln();
        }
        return localized;
    }

    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        c.set(2014, Calendar.OCTOBER, 5, 20, 0);
        System.out.println(daysBetween(c.getTime().getTime()));
    }

}
