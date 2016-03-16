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

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcarver.instrument.Instrumenter;

public class Date extends java.util.Date {

	private static final long serialVersionUID = 1L;

	public Date() {
		super();
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "<init>", "()V", new Object[] {});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
	public Date(long date) {
		super(date);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "<init>", "(J)V", new Object[] {date});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
    @SuppressWarnings("deprecation")
	public Date(int year, int month, int date) {
        super(year, month, date);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "<init>", "(III)V", new Object[] {year, month, date});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }
	
	@SuppressWarnings("deprecation")
	public Date(String s) {
		super(s);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "<init>", "(Ljava/lang/String;)V", new Object[] {s});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
    @SuppressWarnings("deprecation")
	public Date(int year, int month, int date, int hrs, int min) {
        super(year, month, date, hrs, min);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "<init>", "(IIIII)V", new Object[] {year, month, date, hrs, min});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    @SuppressWarnings("deprecation")
	public Date(int year, int month, int date, int hrs, int min, int sec) {
    	super(year, month, date, hrs, min, sec);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "<init>", "(IIIIII)V", new Object[] {year, month, date, hrs, min});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

	
	@Override
	public boolean after(java.util.Date when) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "after", "(Ljava/util/Date;)Z", new Object[] {when});
		boolean ret = super.after(when);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
		return ret;
	}
	
	@Override
	public boolean before(java.util.Date when) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "before", "(Ljava/util/Date;)Z", new Object[] {when});
		boolean ret = super.before(when);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
		return ret;
	}
	
	@Override
	public Object clone() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "clone", "()Ljava/lang/Object;", new Object[] {});
		java.util.Date copy = (java.util.Date)super.clone();
		Date ret = new Date(copy.getTime());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
		return ret;
	}
	
	@Override
	public int compareTo(java.util.Date anotherDate) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "compareTo", "(Ljava/util/Date;)I", new Object[] {anotherDate});
		int ret = super.compareTo(anotherDate);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
		return ret;
	}
	
	@Override
	public boolean equals(Object obj) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "equals", "(Ljava/lang/Object;)Z", new Object[] {obj});
		boolean ret = super.equals(obj);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
		return ret;
	}
	
	@Override
	public long getTime() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getTime", "()J", new Object[] {});
		long ret = super.getTime();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);		
		return ret;
	}
	
	@Override
	public String toString() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "toString", "()Ljava/lang/String;", new Object[] {});
		String ret = super.toString();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
		
		return ret;
	}
	

 
    @SuppressWarnings("deprecation")
	public static long UTC(int year, int month, int date,
                           int hrs, int min, int sec) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, CaptureUtil.loadClass("java/util/Date"), "UTC", "(IIIIII)J", new Object[] {year, month, date, hrs, min, sec});
    	long ret = java.util.Date.UTC(year, month, date, hrs, min, sec);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, CaptureUtil.loadClass("java/util/Date"), ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public static long parse(String s) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, CaptureUtil.loadClass("java/util/Date"), "parse", "(Ljava/lang/String;)J", new Object[] {s});
    	long ret = java.util.Date.parse(s);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, CaptureUtil.loadClass("java/util/Date"), ret);
    	return ret;

    }

    @SuppressWarnings("deprecation")
	public int getYear() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getYear", "()I", new Object[] {});
    	int ret = super.getYear();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public void setYear(int year) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setYear", "(I)V", new Object[] {year});
        super.setYear(year);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    @SuppressWarnings("deprecation")
	public int getMonth() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getMonth", "()I", new Object[] {});
    	int ret = super.getMonth();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
        return ret;
    }

    @SuppressWarnings("deprecation")
	public void setMonth(int month) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setMonth", "(I)V", new Object[] {month});
    	super.setMonth(month);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    @SuppressWarnings("deprecation")
	public int getDate() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getDate", "()I", new Object[] {});
    	int ret = super.getDate();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public void setDate(int date) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setTime", "(I)V", new Object[] {date});
        super.setDate(date);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    @SuppressWarnings("deprecation")
	public int getDay() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getDays", "()I", new Object[] {});
    	int ret = super.getDay();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public int getHours() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getHous", "()I", new Object[] {});
    	int ret = super.getHours();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
        return ret;
    }

    @SuppressWarnings("deprecation")
	public void setHours(int hours) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setHours", "(I)V", new Object[] {hours});
    	super.setHours(hours);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    @SuppressWarnings("deprecation")
	public int getMinutes() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getMinuates", "()I", new Object[] {});
    	int ret = super.getMinutes();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
        return ret;
    }

    @SuppressWarnings("deprecation")
	public void setMinutes(int minutes) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setMinutes", "(I)V", new Object[] {minutes});
    	super.setMinutes(minutes);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    @SuppressWarnings("deprecation")
	public int getSeconds() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getSeconds", "()I", new Object[] {});
    	int ret = super.getSeconds();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

 
    @SuppressWarnings("deprecation")
	public void setSeconds(int seconds) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setSeconds", "(I)V", new Object[] {seconds});
    	super.setSeconds(seconds);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public void setTime(long time) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "setTime", "(J)V", new Object[] {time});
    	super.setTime(time);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, CaptureLog.RETURN_TYPE_VOID);
    }

    static final long getMillisOf(java.util.Date date) {
    	
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, CaptureUtil.loadClass("java/util/Date"), "getMillisOf", "(Ljava/util/Date;)J", new Object[] {date});
        long ret = date.getTime();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, CaptureUtil.loadClass("java/util/Date"), ret);
        return ret;
    }

    public int hashCode() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "hashCode", "()I", new Object[] {});
    	int ret = super.hashCode();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public String toLocaleString() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "toLocaleString", "()Ljava/lang.String;", new Object[] {});
    	String ret = super.toLocaleString();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public String toGMTString() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "toGMTString", "()Ljava/lang/String;", new Object[] {});
    	String ret = super.toGMTString();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }

    @SuppressWarnings("deprecation")
	public int getTimezoneOffset() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, "getTimezoneOffset", "()I", new Object[] {});
    	int ret = super.getTimezoneOffset();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_UTIL_DATE, this, ret);
    	return ret;
    }
}
