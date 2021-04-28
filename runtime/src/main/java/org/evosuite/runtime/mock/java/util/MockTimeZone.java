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

import java.util.TimeZone;

/**
 * TimeZone is used all over the places in the Java API. So not much of the point to try to mock it,
 * as anyway we just need to handle the default time zone that is machine dependent.
 *
 * Created by arcuri on 12/5/14.
 */
public abstract class MockTimeZone extends TimeZone{

    private static final TimeZone cloneGMT = (TimeZone) TimeZone.getTimeZone("GMT").clone();
    private static final long serialVersionUID = 2606461171386129455L;

    public static void reset(){
        TimeZone.setDefault((TimeZone) cloneGMT.clone());
    }
}
