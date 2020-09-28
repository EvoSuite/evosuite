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

import org.evosuite.runtime.mock.OverrideMock;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by arcuri on 1/25/15.
 */
public abstract class MockCalendar extends Calendar implements OverrideMock{
    private static final long serialVersionUID = 7787669189246845968L;

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
