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
package org.evosuite.testcarver.wrapper.java.text;

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.capture.FieldRegistry;
import org.evosuite.testcarver.instrument.Instrumenter;
import org.evosuite.testcarver.wrapper.java.util.Calendar;
import org.evosuite.testcarver.wrapper.java.util.Date;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.TimeZone;

public abstract class DateFormat extends java.text.DateFormat {

    private static final long serialVersionUID = -5974612860032396630L;

    public final StringBuffer format_final(Object obj, StringBuffer toAppendTo,
                                           FieldPosition fieldPosition) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "format", "(Ljava/lang/Object;Ljava/lang/StringBuffer;Ljava/text/FieldPosition;)Ljava/lang/StringBuffer;", new Object[]{obj, toAppendTo, fieldPosition});
        StringBuffer ret = super.format(obj, toAppendTo, fieldPosition);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public final String format_final(java.util.Date date) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "format", "(Ljava/util/Date;)Ljava/lang/String;", new Object[]{date});
        String ret = super.format(date);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public java.util.Date parse(String source) throws ParseException {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "parse", "(Ljava/lang/String;)Ljava/lang/Object;", new Object[]{source});
        java.util.Date date = super.parse(source);
        Date ret = new Date(date.getTime());
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public Object parseObject(String source, ParsePosition pos) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "parse", "(Ljava/lang/String;Ljava/text/ParsePosition;)Ljava/lang/Object;", new Object[]{source, pos});
        Object ret = super.parseObject(source, pos);
        if (ret instanceof java.util.Date) {
            long time = ((java.util.Date) ret).getTime();
            ret = new Date(time);
        }
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getTimeInstance_final() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getTimeInstance", "()Ljava/text/DateFormat;", new Object[]{});
        java.text.DateFormat ret = java.text.DateFormat.getTimeInstance();
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getTimeInstance_final(int style) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getTimeInstance", "(I)Ljava/text/DateFormat;", new Object[]{style});
        java.text.DateFormat ret = java.text.DateFormat.getTimeInstance(style);
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getTimeInstance_final(int style,
                                                             Locale aLocale) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getTimeInstance", "(ILjava/util/Locale;)Ljava/text/DateFormat;", new Object[]{style, aLocale});
        java.text.DateFormat ret = java.text.DateFormat.getTimeInstance(style, aLocale);
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getDateInstance_final() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getDateInstance", "()Ljava/text/DateFormat;", new Object[]{});
        java.text.DateFormat ret = java.text.DateFormat.getDateInstance();
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getDateInstance_final(int style) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getDateInstance", "(I)Ljava/text/DateFormat;", new Object[]{style});
        java.text.DateFormat ret = java.text.DateFormat.getDateInstance(style);
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getDateInstance_final(int style,
                                                             Locale aLocale) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getDateInstance", "(ILjava/util/Locale;)Ljava/text/DateFormat;", new Object[]{style, aLocale});
        java.text.DateFormat ret = java.text.DateFormat.getDateInstance(style, aLocale);
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getDateTimeInstance_final() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getDateTimeInstance", "()Ljava/text/DateFormat;", new Object[]{});
        java.text.DateFormat ret = java.text.DateFormat.getDateInstance();
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getDateTimeInstance_final(int dateStyle,
                                                                 int timeStyle) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getDateTimeInstance", "(II)Ljava/text/DateFormat;", new Object[]{dateStyle, timeStyle});
        java.text.DateFormat ret = java.text.DateFormat.getDateTimeInstance(dateStyle, timeStyle);
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat
    getDateTimeInstance_final(int dateStyle, int timeStyle, Locale aLocale) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getDateTimeInstance", "(IILjava/util/Locale;)Ljava/text/DateFormat;", new Object[]{dateStyle, timeStyle, aLocale});
        java.text.DateFormat ret = java.text.DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale);
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    // TODO: This needs to create an instance of the wrapper class, which it currently doesn't do!
    public static java.text.DateFormat getInstance_final() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getInstance", "()Ljava/text/DateFormat;", new Object[]{});
        java.text.DateFormat ret = java.text.DateFormat.getInstance();
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    public static Locale[] getAvailableLocales() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), "getAvailableLocales", "()[Ljava/util/Locale;", new Object[]{});
        Locale[] ret = java.text.DateFormat.getAvailableLocales();
        FieldRegistry.register(ret);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, CaptureUtil.loadClass("java/text/DateFormat"), ret);
        return ret;
    }

    public void setCalendar(java.util.Calendar newCalendar) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "setCalendar", "(Ljava/util/Calendar;)V", new Object[]{newCalendar});
        super.setCalendar(newCalendar);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public java.util.Calendar getCalendar() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "getCalendar", "()Ljava/util/Calendar;", new Object[]{});
        java.util.Calendar ret = new Calendar(super.getCalendar());
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public void setNumberFormat(NumberFormat newNumberFormat) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "setNumberFormat", "(Ljava/text/NumberFormat;)V", new Object[]{newNumberFormat});
        super.setNumberFormat(newNumberFormat);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public NumberFormat getNumberFormat() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "getNumberFormat", "()Ljava/text/NumberFormat;", new Object[]{});
        NumberFormat ret = super.getNumberFormat();
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public void setTimeZone(TimeZone zone) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "setTimeZone", "(Ljava/util/TimeZone;", new Object[]{zone});
        super.setTimeZone(zone);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public TimeZone getTimeZone() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "getTimeZone", "()Ljava/util/TimeZone;", new Object[]{});
        TimeZone ret = super.getTimeZone();
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public void setLenient(boolean lenient) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "setLenient", "(Z)V", new Object[]{lenient});
        super.setLenient(lenient);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, CaptureLog.RETURN_TYPE_VOID);
    }

    public boolean isLenient() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "isLenient", "()Z", new Object[]{});
        boolean ret = super.isLenient();
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public int hashCode() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "hashCode", "()I", new Object[]{});
        int ret = super.hashCode();
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public boolean equals(Object obj) {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "equals", "(Ljava/lang/Object;)Z", new Object[]{obj});
        boolean ret = super.equals(obj);
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

    public Object clone() {
        Capturer.capture(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, "clone", "()Ljava/lang/Object;", new Object[]{});
        Object ret = super.clone(); // TODO: Should be wrapper DateFormat
        Capturer.enable(Instrumenter.CAPTURE_ID_JAVA_TEXT_DATEFORMAT, this, ret);
        return ret;
    }

}
