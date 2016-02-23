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
package com.examples.with.different.packagename;

import org.apache.commons.cli.AlreadySelectedException;

/**
 * Created by Andrea Arcuri on 21/04/15.
 */
public class DependencyLibrary {

    public void foo(){
        //here, at compile time we use the one in EvoSuite dependency, but not at runtime
        AlreadySelectedException e = new AlreadySelectedException(null);
        if(e.toString().equals("foo")){
            System.out.println("Only executed if SUT version is used, and not the one in EvoSuite's dependencies");
        }
    }
}


//.class compiled and added to "external" folder
/*
package org.apache.commons.cli;

public class AlreadySelectedException {

    public AlreadySelectedException(String msg){
    }

    @Override
    public String toString(){
        //this cannot be returned by the original class in commons-cli
        return "foo";
    }
}


 */
