package org.evosuite.runtime.mock.java.text;

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.util.MockCalendar;
import org.evosuite.runtime.mock.java.util.MockLocale;
import sun.util.locale.provider.LocaleServiceProviderPool;

import java.text.DateFormat;
import java.text.spi.DateFormatProvider;
import java.util.Locale;

/**
 * Created by gordon on 31/01/2016.
 */
public abstract class MockDateFormat implements StaticReplacementMock {

    @Override
    public String getMockedClassName() {
        return DateFormat.class.getName();
    }

    public final static DateFormat getTimeInstance() {
        DateFormat format = DateFormat.getTimeInstance();
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getTimeInstance(int style) {
        DateFormat format = DateFormat.getTimeInstance(style);
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getTimeInstance(int style,
                                                   Locale aLocale) {
        DateFormat format = DateFormat.getTimeInstance(style, aLocale);
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getDateInstance() {
        DateFormat format = DateFormat.getDateInstance();
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getDateInstance(int style) {
        DateFormat format = DateFormat.getDateInstance(style);
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getDateInstance(int style,
                                                   Locale aLocale) {
        DateFormat format = DateFormat.getDateInstance(style, aLocale);
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getDateTimeInstance() {
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getDateTimeInstance(int dateStyle,
                                                       int timeStyle) {
        DateFormat format = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale aLocale)
    {
        DateFormat format = DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale);
        format.setCalendar(MockCalendar.getInstance());
        return format;
    }

    public final static DateFormat getInstance() {
        return getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    public static Locale[] getAvailableLocales()
    {
        LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(DateFormatProvider.class);
        return pool.getAvailableLocales();
    }
}
