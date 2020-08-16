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
package org.evosuite.runtime.mock.java.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.evosuite.runtime.mock.OverrideMock;

public class MockGregorianCalendar extends GregorianCalendar  implements OverrideMock{

	private static final long serialVersionUID = 4768096296715665262L;
	
	public MockGregorianCalendar() {
		this.setTimeInMillis(org.evosuite.runtime.System.currentTimeMillis());
	}
	
	public MockGregorianCalendar(int year, int month, int dayOfMonth) {
		super(year, month, dayOfMonth);
	}
	
	public MockGregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
		super(year, month, dayOfMonth, hourOfDay, minute);
	}

	public MockGregorianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
		super(year, month, dayOfMonth, hourOfDay, minute, second);
	}
	
	public MockGregorianCalendar(Locale aLocale) {
		super(aLocale);
		this.setTimeInMillis(org.evosuite.runtime.System.currentTimeMillis());
	}
	
	public MockGregorianCalendar(TimeZone zone) {
		super(zone);
		this.setTimeInMillis(org.evosuite.runtime.System.currentTimeMillis());
	}
	
	public MockGregorianCalendar(TimeZone zone, Locale aLocale) {
		super(zone, aLocale);
		this.setTimeInMillis(org.evosuite.runtime.System.currentTimeMillis());
	}
	
	// TODO: This code in Calendar seems to cause access to time
	//       but I don't understand how.
	//    public long getTimeInMillis() {
	//        if (!isTimeSet) {
	//            updateTime();
	//        }
	//        return time;
	//    }
    public long getTimeInMillis() {
        return time;
    }

	public static GregorianCalendar from(ZonedDateTime zdt) {
		GregorianCalendar cal = new MockGregorianCalendar(MockTimeZone.getTimeZone(zdt.getZone()));
		cal.setGregorianChange(new MockDate(Long.MIN_VALUE));
		cal.setFirstDayOfWeek(MONDAY);
		cal.setMinimalDaysInFirstWeek(4);
		try {
			cal.setTimeInMillis(Math.addExact(Math.multiplyExact(zdt.toEpochSecond(), 1000),
					zdt.get(ChronoField.MILLI_OF_SECOND)));
		} catch (ArithmeticException ex) {
			throw new IllegalArgumentException(ex);
		}
		return cal;
	}
}
