package org.evosuite.runtime.mock.java.util;

import org.evosuite.runtime.mock.OverrideMock;
import sun.util.BuddhistCalendar;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by arcuri on 1/25/15.
 */
public abstract class MockCalendar extends Calendar implements OverrideMock{

    /*
        Note: there are many methods in Calendar, but here we just need to mock the ones that
        create new instances
     */

    //----- constructors  ---------

    protected MockCalendar(){
        super();
    }

    protected MockCalendar(TimeZone zone, Locale aLocale){
        super(zone,aLocale);
    }

    // ------ static methods ----------

    public static Calendar getInstance() {
        return __createCalendar(TimeZone.getDefault(), Locale.getDefault(Locale.Category.FORMAT));
    }

    public static Calendar getInstance(TimeZone zone){
        return __createCalendar(zone, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static Calendar getInstance(Locale aLocale){
        return __createCalendar(TimeZone.getDefault(), aLocale);
    }

    public static Calendar getInstance(TimeZone zone,Locale aLocale){
        return __createCalendar(zone, aLocale);
    }


    private static Calendar __createCalendar(TimeZone zone,Locale aLocale){
        return new MockGregorianCalendar(zone, aLocale);
    }


    public static synchronized Locale[] getAvailableLocales(){
        //TODO do we need to mock it?
        return DateFormat.getAvailableLocales();
    }


}
