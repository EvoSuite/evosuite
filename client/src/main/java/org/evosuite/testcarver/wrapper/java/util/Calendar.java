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
package org.evosuite.testcarver.wrapper.java.util;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcarver.instrument.Instrumenter;


public class Calendar extends java.util.Calendar {

	private static final long serialVersionUID = 8358505095239298199L;
	
	private java.util.Calendar wrappedCalendar;

	public Calendar(java.util.Calendar cal) {
		wrappedCalendar = cal;
	}
	
//	protected Calendar() {
//		super();
//		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "<init>", "()V", new Object[] {});
//		FieldRegistry.register(this);
//		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);		
//	}
	
	public java.util.Date getTime_final() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getTime", "()Ljava/util/Date;", new Object[] {});
		java.util.Date ret = new Date(wrappedCalendar.getTimeInMillis());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
		return ret;
	}
	
    public final void setTime_final(java.util.Date date) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setTime", "(Ljava/util/Date;)V", new Object[] {date});
		wrappedCalendar.setTime(date);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
		
    }

	
	public static java.util.Date getTime(java.util.Calendar cal) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, cal, "getTime", "()Ljava/util/Date;", new Object[] {});
		Date date = new Date(cal.getTimeInMillis());
		FieldRegistry.register(date);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, cal, date);
		return date;
	}
		
	public static java.util.Calendar getInstance() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), "getInstance", "()Ljava/util/Calendar;", new Object[] {});
		Calendar ret = new Calendar(java.util.Calendar.getInstance());
		FieldRegistry.register(ret);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), ret);
		return ret;
	}

	

//    protected Calendar(TimeZone zone, Locale aLocale)
//    {
//    	super(zone, aLocale);
//		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "<init>", "(Ljava/util/TimeZone;Ljava/util/Locale;)V", new Object[] {zone, aLocale});
//		FieldRegistry.register(this);
//		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
//    }

    public static java.util.Calendar getInstance(TimeZone zone)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), "getInstance", "(Ljava/util/TimeZone;)Ljava/util/Calendar;", new Object[] {zone});
		Calendar ret = new Calendar(java.util.Calendar.getInstance(zone));
		FieldRegistry.register(ret);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), ret);
		return ret;
    }

    public static java.util.Calendar getInstance(Locale aLocale)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), "getInstance", "(Ljava/util/Locale;)Ljava/util/Calendar;", new Object[] {aLocale});
		Calendar ret = new Calendar(java.util.Calendar.getInstance(aLocale));
		FieldRegistry.register(ret);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), ret);
		return ret;
    }

    public static java.util.Calendar getInstance(TimeZone zone,
                                       Locale aLocale)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), "getInstance", "(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;", new Object[] {zone, aLocale});
		Calendar ret = new Calendar(java.util.Calendar.getInstance(zone, aLocale));
		FieldRegistry.register(ret);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), ret);
		return ret;
    }
    
    public static synchronized Locale[] getAvailableLocales()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), "getAvailableLocales", "()[Ljava/util/Locale;", new Object[] {});
    	Locale[] ret = java.util.Calendar.getAvailableLocales();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, CaptureUtil.loadClass("java/util/Calendar"), ret);
        return ret;
    }


    public long getTimeInMillis() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getTimeInMillis", "()J", new Object[] {});
    	long ret = wrappedCalendar.getTimeInMillis();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public void setTimeInMillis(long millis) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setTimeInMillis", "(J)V", new Object[] {millis});
		wrappedCalendar.setTimeInMillis(millis);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public int get(int field)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "get", "(I)I", new Object[] {field});
    	int ret = wrappedCalendar.get(field);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public void set(int field, int value)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "set", "(II)V", new Object[] {field, value});
		wrappedCalendar.set(field, value);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public final void set_final(int year, int month, int date)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "set", "(III)V", new Object[] {year, month, date});
		wrappedCalendar.set(year, month, date);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public final void set_final(int year, int month, int date, int hourOfDay, int minute)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "set", "(IIIII)V", new Object[] {year, month, date, hourOfDay, minute});
		wrappedCalendar.set(year, month, date, hourOfDay, minute);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public final void set_final(int year, int month, int date, int hourOfDay, int minute,
                          int second)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "set", "(IIIIII)V", new Object[] {year, month, date, hourOfDay, minute, second});
		wrappedCalendar.set(year, month, date, hourOfDay, minute, second);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public final void clear_final()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "clear", "()Z", new Object[] {});
		wrappedCalendar.clear();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public final void clear_final(int field)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "clear", "(I)V", new Object[] {field});
		wrappedCalendar.clear(field);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public final boolean isSet_final(int field)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "isSet", "(I)Z", new Object[] {field});
    	boolean ret = wrappedCalendar.isSet(field);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public String getDisplayName(int field, int style, Locale locale) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getDisplayName", "(IILjava/util/Locale;)Ljava/lang/String;", new Object[] {field, style, locale});
    	String ret = wrappedCalendar.getDisplayName(field, style, locale);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
        return ret;
    }

    public Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getDisplayNames", "(IILjava/util/Locale;)Ljava/util/Map;", new Object[] {field, style, locale});
    	Map<String, Integer> ret = wrappedCalendar.getDisplayNames(field, style, locale);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
        return ret;
    }
 
    public boolean equals(Object obj) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "equals", "(Ljava/lang/Object;)Z", new Object[] {obj});
    	boolean ret = wrappedCalendar.equals(obj);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public int hashCode() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "hashCode", "()I", new Object[] {});
    	int ret = wrappedCalendar.hashCode();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public boolean before(Object when) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "before", "(Ljava/lang/Object;)Z", new Object[] {when});
    	boolean ret = wrappedCalendar.before(when);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public boolean after(Object when) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "after", "(Ljava/lang/Object;)Z", new Object[] {when});
    	boolean ret = wrappedCalendar.after(when);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public int compareTo(java.util.Calendar anotherCalendar) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "compareTo", "(Ljava/util/Calendar;)I", new Object[] {anotherCalendar});
    	int ret = wrappedCalendar.compareTo(anotherCalendar);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
        return ret;
    }
    
    public void roll(int field, int amount)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "roll", "(II)V", new Object[] {field, amount});
		wrappedCalendar.roll(field, amount);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public void setTimeZone(TimeZone value)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setTimeZone", "(Ljava/util/TimeZone;)V", new Object[] {value});
		wrappedCalendar.setTimeZone(value);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public TimeZone getTimeZone()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getTimeZone", "()Ljava/util/TimeZone;", new Object[] {});
    	TimeZone ret = wrappedCalendar.getTimeZone();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public void setLenient(boolean lenient)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setLenient", "(Z)V", new Object[] {lenient});
		wrappedCalendar.setLenient(lenient);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public boolean isLenient()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "isLenient", "()Z", new Object[] {});
    	boolean ret = wrappedCalendar.isLenient();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
        return ret;
    }

    public void setFirstDayOfWeek(int value)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setFirstDayOfWeek", "(I)V", new Object[] {value});
		wrappedCalendar.setFirstDayOfWeek(value);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public int getFirstDayOfWeek()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getFirstDayOfWeek", "()I", new Object[] {});
    	int ret = wrappedCalendar.getFirstDayOfWeek();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public void setMinimalDaysInFirstWeek(int value)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setMinimalDaysInFirstWeek", "(I)V", new Object[] {value});
		wrappedCalendar.setMinimalDaysInFirstWeek(value);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public int getMinimalDaysInFirstWeek()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getMinimalDaysInFirstWeek", "()I", new Object[] {});
    	int ret = wrappedCalendar.getMinimalDaysInFirstWeek();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public boolean isWeekDateSupported() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "isWeekDateSupported", "()Z", new Object[] {});
        boolean ret = wrappedCalendar.isWeekDateSupported();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
        return ret;
    }

    public int getWeekYear() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getWeekYear", "()I", new Object[] {});
    	int ret = wrappedCalendar.getWeekYear();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "setWeekDate", "(III)V", new Object[] {weekYear, weekOfYear, dayOfWeek});
		wrappedCalendar.setWeekDate(weekYear, weekOfYear, dayOfWeek);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public int getWeeksInWeekYear() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getWeeksInWeekYear", "()I", new Object[] {});
    	int ret = wrappedCalendar.getWeeksInWeekYear();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }
    
    public int getActualMinimum(int field) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getActualMinimum", "(I)I", new Object[] {field});
    	int ret = wrappedCalendar.getActualMinimum(field);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public int getActualMaximum(int field) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getActualMaximum", "(I)I", new Object[] {field});
    	int ret = wrappedCalendar.getActualMaximum(field);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public Object clone()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "clone", "()Ljava/lang/Object;", new Object[] {});
    	Object ret = new Calendar((java.util.Calendar)wrappedCalendar.clone());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

    public String toString() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "toString", "()Ljava/lang/String;", new Object[] {});
    	String ret = wrappedCalendar.toString();
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
    }

	@Override
	protected void computeTime() {
		// TODO		
	}

	@Override
	protected void computeFields() {
		// TODO Auto-generated method stub		
	}

	public void add(int field, int amount) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "add", "(II)V", new Object[] {field, amount});
    	wrappedCalendar.add(field, amount);
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
	}

	@Override
	public void roll(int field, boolean up) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "roll", "(II)V", new Object[] {field, up});
    	wrappedCalendar.roll(field, up);
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, CaptureLog.RETURN_TYPE_VOID);
	}

	@Override
	public int getMinimum(int field) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getMinimum", "(I)V", new Object[] {field});
    	int ret = wrappedCalendar.getMinimum(field);
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
	}

	@Override
	public int getMaximum(int field) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getMaximum", "(I)V", new Object[] {field});
    	int ret = wrappedCalendar.getMaximum(field);
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
	}

	@Override
	public int getGreatestMinimum(int field) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getGreatestMinimum", "(I)V", new Object[] {field});
    	int ret = wrappedCalendar.getGreatestMinimum(field);
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
	}

	@Override
	public int getLeastMaximum(int field) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, "getLeastMaximum", "(I)V", new Object[] {field});
    	int ret = wrappedCalendar.getLeastMaximum(field);
    	Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_CALENDAR, this, ret);
    	return ret;
	}
}
