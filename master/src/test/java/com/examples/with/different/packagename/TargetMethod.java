/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package com.examples.with.different.packagename;

public class TargetMethod {

    private int y = 0;

    public boolean boo(Integer x) {
        return foo(x);
    }

    public boolean foo(Integer x) throws NullPointerException, IllegalArgumentException {
        try {
            if (x == null) {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        if (x > 0) {
            this.y = x;
            return bar(x);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean bar(Integer x) {
        if (x < 500) {
            return false;
        } else {
            return true;
        }
    }

    public int getY() {
        return this.y;
    }
}
