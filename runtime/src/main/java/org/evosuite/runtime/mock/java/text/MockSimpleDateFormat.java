package org.evosuite.runtime.mock.java.text;

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.util.MockCalendar;
import org.evosuite.runtime.mock.java.util.MockDate;
import org.evosuite.runtime.mock.java.util.MockTimeZone;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by gordon on 22/01/2016.
 */
public class MockSimpleDateFormat extends java.text.SimpleDateFormat implements OverrideMock {

    public MockSimpleDateFormat() {
        super();
        set2DigitYearStart(new MockDate());
        setNumberFormat(Locale.getDefault(Locale.Category.FORMAT));
        initializeCalendar(Locale.getDefault(Locale.Category.FORMAT));
    }

    public MockSimpleDateFormat(String pattern)
    {
        this(pattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    public MockSimpleDateFormat(String pattern, Locale locale)
    {
        super(pattern, locale);
        set2DigitYearStart(new MockDate());
        setNumberFormat(locale);
        initializeCalendar(locale);
    }

    public MockSimpleDateFormat(String pattern, DateFormatSymbols formatSymbols)
    {
        super(pattern, formatSymbols);
        set2DigitYearStart(new MockDate());
        setNumberFormat(Locale.getDefault(Locale.Category.FORMAT));
        initializeCalendar(Locale.getDefault(Locale.Category.FORMAT));
    }

    private void setNumberFormat(Locale locale) {
        numberFormat = NumberFormat.getIntegerInstance(locale);
        numberFormat.setGroupingUsed(false);
    }

    private void initializeCalendar(Locale loc) {
        calendar = MockCalendar.getInstance(MockTimeZone.getDefault(), loc);
    }

}
