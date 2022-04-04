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
package com.examples.with.different.packagename.context;

public class SubClass extends ISubClass {

    ISubSubClass subsubclass;

    public SubClass() {
        subsubclass = new SubSubClass();
    }

    public boolean checkFiftneen(int i) {
        boolean bol = bla(i);
        if (bol)
            return true;
        return false;
    }

    private boolean bla(int i) {
        boolean bol = false;
        if (i * 2 == 6) {
            bol = true;
        }
        bol = subsubclass.innermethod(i) || bol;
        return bol;
    }

}
