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