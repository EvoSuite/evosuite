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
 * Created by Andrea Arcuri on 02/03/15.
 */
public class OnlyPrivateMethods {

    private void param0(){
        System.out.println("param0");
    }

    private void param1(String s){
        System.out.println("param1: "+s);
    }

    /*
    private void param2(int x, String s){
        System.out.println("param2: "+s+" "+x);
    }

    private void param2(String s, int x){
        System.out.println("param2_b: "+s+" "+x);
    }
*/
}
