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
