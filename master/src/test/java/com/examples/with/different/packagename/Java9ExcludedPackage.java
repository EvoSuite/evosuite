package com.examples.with.different.packagename;

import sun.java2d.SunGraphics2D;

public class Java9ExcludedPackage {
    private SunGraphics2D sunGraphics2D;

    Java9ExcludedPackage() {

    }

    Java9ExcludedPackage(SunGraphics2D sunGraphics2D) {
        this.sunGraphics2D = sunGraphics2D;
    }

    public void drawLine(SunGraphics2D sunGraphics2D) {
        int transx = sunGraphics2D.transX;
        int transy = sunGraphics2D.transY;
        System.out.print("transx : " + transx + " , transy" + transy);
    }

    public int testMe(int x) {
        if (x == 5)
            return 1;
        else
            return 0;
    }
}
