package org.evosuite.testcase.mutation.insertion;

import org.evosuite.Properties;

public class InsertionStrategyFactory {
    public static AbstractInsertion getStrategy() {
        if (Properties.ALGORITHM == Properties.Algorithm.DYNAMOSA
                && Properties.MUTATION_STRATEGY == Properties.MutationStrategy.GUIDED) {
            return GuidedInsertion.getInstance();
        } else {
            return RandomInsertion.getInstance();
        }
    }
}
