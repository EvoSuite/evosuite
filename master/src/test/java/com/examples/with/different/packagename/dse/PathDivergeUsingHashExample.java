package com.examples.with.different.packagename.dse;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Classic path divergence problem example.
 */
public class PathDivergeUsingHashExample {
    public PathDivergeUsingHashExample() {}

    public static int test (int x, int y) {
		if (x != new HashCodeBuilder().append(y).build()) {
			if (y == 10) {
				return -1;
			} else {
				return 1;
			}
		}

		return 0;
	}
}
