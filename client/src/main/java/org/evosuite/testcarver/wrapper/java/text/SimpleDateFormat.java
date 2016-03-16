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
package org.evosuite.testcarver.wrapper.java.text;

import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcarver.instrument.Instrumenter;
import org.evosuite.testcarver.wrapper.java.util.Calendar;
import org.evosuite.testcarver.wrapper.java.util.Date;

/**
 * TODO: This is not complete. Should it be a subclass of the wrapper DateFormat, or the real SimpleDateFormat?
 * 
 *
 */
public class SimpleDateFormat extends java.text.SimpleDateFormat {

    static final long serialVersionUID = 4774881970558875024L;

	public SimpleDateFormat() {
		super();
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "<init>", "()V", new Object[] {});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
	}

	public SimpleDateFormat(String pattern) {
		super(pattern);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "<init>", "(Ljava/lang/String;)V", new Object[] {pattern});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
	public SimpleDateFormat(String pattern, Locale locale) {
		super(pattern, locale);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "<init>", "(Ljava/lang/String;Ljava/util/Locale;)V", new Object[] {pattern, locale});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
    public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {    	
		super(pattern, formatSymbols);
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "<init>", "(Ljava/lang/String;Ljava/text/DateFormatSymbols;)V", new Object[] {pattern, formatSymbols});
		FieldRegistry.register(this);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
    }
    
    public void set2DigitYearStart(java.util.Date startDate) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "set2DigitYearStart", "(Ljava/util/Date;)V", new Object[] {startDate});
		super.set2DigitYearStart(startDate);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);		
    }

    public java.util.Date get2DigitYearStart() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "get2DigitYearStart", "()Ljava/util/Date;", new Object[] {});
		java.util.Date ret = new Date(super.get2DigitYearStart().getTime());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
        return ret;
    }

    
    
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "formatToCharacterIterator", "(Ljava/lang/Object;)Ljava/text/AttributedCharacterIterator;", new Object[] {obj});
		AttributedCharacterIterator ret = super.formatToCharacterIterator(obj);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
        return ret;    
    }
    
    public String toPattern() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "toPattern", "()Ljava/lang/String;", new Object[] {});
		String ret = super.toLocalizedPattern();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
        return ret;
    }

    public String toLocalizedPattern() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "toLocalizedPattern", "()Ljava/lang/String;", new Object[] {});
		String ret = super.toLocalizedPattern();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
        return ret;
    }

    public void applyPattern(String pattern)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "applyPattern", "(Ljava/lang/String;)V", new Object[] {});
		super.applyPattern(pattern);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);		
    }

    public void applyLocalizedPattern(String pattern) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "applyLocalizedPattern", "(Ljava/lang/String;)V", new Object[] {});
		super.applyLocalizedPattern(pattern);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);		
    }

    public DateFormatSymbols getDateFormatSymbols()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "getDateFormatSymbols", "()Ljava/text/DateFormatSymbols;", new Object[] {});
		DateFormatSymbols ret = super.getDateFormatSymbols();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
		return ret;
    }

    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "setDateFormatSymbols", "(Ljava/text/DateFormatSymbols;)V", new Object[] {newFormatSymbols});
		super.setDateFormatSymbols(newFormatSymbols);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);		
    }

    public Object clone() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "clone", "()Ljava/lang/Object;", new Object[] {});
		Object ret = super.clone(); // TODO: Use wrapper for copy
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
		return ret;
    }

    public int hashCode()
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "hashCode", "()I", new Object[] {});
		int ret = super.hashCode();
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
		return ret;
    }


    public boolean equals(Object obj)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "equals", "(Ljava/lang/Object;)Z", new Object[] {obj});
		boolean ret = super.equals(obj);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
		return ret;
    }

	
	@Override
	public void setLenient(boolean lenient) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "setLenient", "(Z)V", new Object[] {lenient});
		super.setLenient(lenient);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
	@Override
	public void setTimeZone(TimeZone zone) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "setTimeZone", "(Ljava/util/TimeZone;)V", new Object[] {zone});
		super.setTimeZone(zone);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
	}
	
	public java.util.Date parse(String source) throws ParseException {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "parse", "(Ljava/lang/String;)Ljava/util/Date;", new Object[] {source});
		Date ret = new Date(super.parse(source).getTime());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);		
		return ret;
	}
	
	public java.util.Date parse(String text, ParsePosition pos)
    {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "parse", "(Ljava/lang/String;Ljava/text/ParsePosition;)Ljava/util/Date;", new Object[] {text, pos});
		java.util.Date ret = new Date(super.parse(text, pos).getTime());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);
		return ret;
    }
	
	@Override
	public java.util.Calendar getCalendar() {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "getCalendar", "()Ljava/util/Calendar;", new Object[] {});
		java.util.Calendar ret = new Calendar(super.getCalendar());
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);
		return ret;
	}
	
	@Override
	public StringBuffer format(java.util.Date date, StringBuffer toAppendTo,
			FieldPosition pos) {
		Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, "format", "(Ljava/util/Date;Ljava/lang/StringBuffer;Ljava/text/FieldPosition;)Ljava/lang/StringBuffer;", new Object[] {date, toAppendTo, pos});
		StringBuffer ret = super.format(date, toAppendTo, pos);
		Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_SIMPLEDATEFORMAT, this, ret);
		return ret;
	}
	
}
