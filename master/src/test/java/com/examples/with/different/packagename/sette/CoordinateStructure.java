package com.examples.with.different.packagename.sette;

public final class CoordinateStructure {
    public int x = 0;
    public int y = 0;

    public CoordinateStructure() {
    }

    public CoordinateStructure(CoordinateStructure o) {
        if (o == null) {
            return;
        }

        x = o.x;
        y = o.y;
    }
}
