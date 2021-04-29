package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation;

import java.util.HashMap;
import java.util.Map;

public class BranchDistanceCache extends HashMap<Integer, Integer> {
    public BranchDistanceCache(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public BranchDistanceCache(int initialCapacity) {
        super(initialCapacity);
    }

    public BranchDistanceCache() {
    }

    public BranchDistanceCache(Map<? extends Integer, ? extends Integer> m) {
        super(m);
    }
}
