/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.mock.java.text;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.util.MockCalendar;

import java.text.DateFormat;
import java.text.spi.DateFormatProvider;
import java.util.Locale;

/**
 * Created by gordon on 31/01/2016.
 */
public class MockDateFormat implements StaticReplacementMock {

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

    public static Locale[] getAvailableLocales() {
        return DateFormat.getAvailableLocales();
    }
}
