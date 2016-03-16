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
package com.examples.with.different.packagename.reflection;

/**
 * Created by Andrea Arcuri on 03/03/15.
 */
public class PrivateFieldInPublicMethod {

    private boolean flag = false;

    private String s = "foo";

    public void flag(){
        if(flag){
            System.out.println("Flag is true");
        } else {
            System.out.println("Flag is false");
        }
    }

    public void checkString(){
        if(s.equals("42")){
            System.out.println("String is 42");
        } else {
            System.out.println("false");
        }
    }
}
