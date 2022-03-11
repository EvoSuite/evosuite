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
package com.examples.with.different.packagename.purity;

public class InterfaceInspectorCalls {

    private InterfaceInspector iInspector1;
    private InterfaceInspector iInspector2;

    public InterfaceInspectorCalls(int x) {
        iInspector1 = new AllPureInspectors(x);
        iInspector2 = new PureImpureInspectors(x);
    }

    public boolean pureInspector1() {
        return iInspector1.pureInspector() == 0;
    }

    public boolean impureInspector1() {
        return iInspector1.impureInspector() == 0;
    }

    public boolean pureInspector2() {
        return iInspector2.pureInspector() == 0;
    }

    public boolean impureInspector2() {
        return iInspector2.impureInspector() == 0;
    }

}
