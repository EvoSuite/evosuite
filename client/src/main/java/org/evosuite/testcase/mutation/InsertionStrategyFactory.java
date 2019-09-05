package org.evosuite.testcase.mutation;

import org.evosuite.Properties;

public class InsertionStrategyFactory {
    public static AbstractInsertionStrategy getStrategy() {
        if (Properties.ALGORITHM == Properties.Algorithm.DYNAMOSA
                && Properties.INSERTION_STRATEGY == Properties.InsertionStrategy.GUIDED_INSERTION) {
            return GuidedInsertion.getInstance();
        } else {
            return RandomInsertion.getInstance();
        }
    }
}
