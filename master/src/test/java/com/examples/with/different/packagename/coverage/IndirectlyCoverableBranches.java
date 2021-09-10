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
package com.examples.with.different.packagename.coverage;

public class IndirectlyCoverableBranches {

    private String str1 = "suf";
    private String str2 = "fix";

    public void someTopLevelMethod() {
        testMe(42, str1 + str2);
        testMe(42, "bla");
        testMe(40, "foo");
    }

    public void otherTopLevelMethod(int x) {
        testMe(x, str1 + str2);
    }

    public boolean testMe(int x, String foo) {
        if (x == 42 && foo.endsWith(str1 + str2))
            return true;
        else
            return false;
    }
}
