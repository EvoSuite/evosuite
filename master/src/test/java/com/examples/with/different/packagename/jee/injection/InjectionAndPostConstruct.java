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
package com.examples.with.different.packagename.jee.injection;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Andrea Arcuri on 14/07/15.
 */
public class InjectionAndPostConstruct {

    @Inject
    private Event event;

    @Inject
    private String aString;

    private Object obj;

    @PostConstruct
    private void init(){
        event.toString(); //throw exception if not injected
        aString.toString();
        obj = new Object();
        System.out.println("Initialized");
    }

    public void checkObject(){
        obj.toString(); //throw exception if no PostConstruct
    }
}
