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
package com.examples.with.different.packagename.fm;

/**
 * Created by foo on 19/12/15.
 */
public class SimpleFM_GenericsReturnWithExtend_Double {

    public interface B {
    }

    public interface W extends B {
        boolean isW();
    }

    public interface Z extends B {
        boolean isZ();
    }

    public interface A {
        void setB(B b);

        <C extends B> C getB();
    }


    public static boolean foo(A a) {
        W w = a.getB();
        Z z = a.getB();

        if (w.isW() && z.isZ()) {
            System.out.println("W and Z");
            return true;
        } else {
            return false;
        }
    }
}
