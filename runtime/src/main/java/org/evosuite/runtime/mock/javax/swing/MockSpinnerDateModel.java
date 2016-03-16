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
package org.evosuite.runtime.mock.javax.swing;

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.util.MockCalendar;
import org.evosuite.runtime.mock.java.util.MockDate;

import javax.swing.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by gordon on 01/02/2016.
 */
public class MockSpinnerDateModel extends SpinnerDateModel implements OverrideMock {

    public MockSpinnerDateModel(Date value, Comparable start, Comparable end, int calendarField) {
        super(value, start, end, calendarField);
        this.setValue(MockCalendar.getInstance());
    }

    public MockSpinnerDateModel() {
        this(new MockDate(), null, null, Calendar.DAY_OF_MONTH);
    }

    public Object getNextValue() {
        Calendar cal = MockCalendar.getInstance();
        cal.setTime((Date) getValue());

        cal.add(getCalendarField(), 1);
        Date next = cal.getTime();

        return ((getEnd() == null) || (getEnd().compareTo(next) >= 0)) ? next : null;
    }

    public Object getPreviousValue() {
        Calendar cal = MockCalendar.getInstance();
        cal.setTime((Date) getValue());
        cal.add(getCalendarField(), -1);
        Date prev = cal.getTime();
        return ((getStart() == null) || (getStart() .compareTo(prev) <= 0)) ? prev : null;
    }



}
