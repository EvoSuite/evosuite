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
package com.examples.with.different.packagename.sette;

public final class SegmentStructure {
    // it cannot be provided that these fields will be never null without using
    // getter/setter methods
    public CoordinateStructure p1 = new CoordinateStructure();
    public CoordinateStructure p2 = new CoordinateStructure();

    public SegmentStructure() {
    }

    public SegmentStructure(SegmentStructure o) {
        if (o == null) {
            return;
        }

        p1 = new CoordinateStructure(o.p1);
        p2 = new CoordinateStructure(o.p2);
    }
}
