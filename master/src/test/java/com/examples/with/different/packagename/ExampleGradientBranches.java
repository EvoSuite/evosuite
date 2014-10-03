package com.examples.with.different.packagename;

public class ExampleGradientBranches {

	public boolean getFlag(int x, int y) {
		if(x > 0 && y < 0 )
			return true;
		else
			return false;
	}

    public boolean getBooleanFlag(boolean b) {
        if(b)
            return true;
        else
            return false;
    }
}
