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
package com.examples.with.different.packagename.sette;

public final class StructureInStructure {
    private StructureInStructure() {
        throw new UnsupportedOperationException("Static class");
    }

    public static int guessParams(int x1, int y1, int x2, int y2) {
        SegmentStructure s = new SegmentStructure();
        s.p1.x = x1;
        s.p1.y = y1;
        s.p2.x = x2;
        s.p2.y = y2;

        if (s.p1.x == 1 && s.p1.y == 2) {
            if (s.p2.x == 3 && s.p2.y == 4) {
                return 3; // 1+2
            } else {
                return -1; // 1-2
            }
        } else {
            if (s.p2.x == 3 && s.p2.y == 4) {
                return 1; // -1+2
            } else {
                return -3; // -1-2
            }
        }
    }

    public static int guessCoordinates(CoordinateStructure p1, CoordinateStructure p2) {
        if (p1 == null || p2 == null) {
            return 0;
        }

        SegmentStructure s = new SegmentStructure();
        s.p1 = p1;
        s.p2 = p2;

        if (s.p1.x == 1 && s.p1.y == 2) {
            if (s.p2.x == 3 && s.p2.y == 4) {
                return 3; // 1+2
            } else {
                return -1; // 1-2
            }
        } else {
            if (s.p2.x == 3 && s.p2.y == 4) {
                return 1; // -1+2
            } else {
                return -3; // -1-2
            }
        }
    }

    public static int guess(SegmentStructure s) {
        if (s == null || s.p1 == null || s.p2 == null) {
            // it cannot be provided that the p1 & p2 fields will be never null
            // without using getter/setter methods
            return 0;
        }

        if (s.p1.x == 1 && s.p1.y == 2) {
            if (s.p2.x == 3 && s.p2.y == 4) {
                return 3; // 1+2
            } else {
                return -1; // 1-2
            }
        } else {
            if (s.p2.x == 3 && s.p2.y == 4) {
                return 1; // -1+2
            } else {
                return -3; // -1-2
            }
        }
    }
}