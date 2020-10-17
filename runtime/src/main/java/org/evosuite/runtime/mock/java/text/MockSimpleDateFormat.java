/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.util.MockCalendar;
import org.evosuite.runtime.mock.java.util.MockDate;
import org.evosuite.runtime.mock.java.util.MockTimeZone;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by gordon on 22/01/2016.
 */
public class MockSimpleDateFormat extends java.text.SimpleDateFormat implements OverrideMock {

    private static final long serialVersionUID = 8147368433302111653L;

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
