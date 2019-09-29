package org.evosuite.testcase.mutation;

import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.graphs.ddg.MethodEntry;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.mutation.insertion.GuidedInsertion;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.utils.Randomness;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class MutationUtils {

    public static MethodEntry toMethodEntry(final TestFitnessFunction f) {
        final String clazz = f.getTargetClassName();
        final String nameDesc = f.getTargetMethodName();
        return new MethodEntry(clazz, nameDesc);
    }

    public static MethodEntry toMethodEntry(final EntityWithParametersStatement stmt) {
        final String className = stmt.getDeclaringClassName();
        final String methodName = stmt.getMethodName();
        final String descriptor = stmt.getDescriptor();
        return new MethodEntry(className, methodName, descriptor);
    }

    public static void registerGoalsManager(MultiCriteriaManager mgr) {
        Objects.requireNonNull(mgr);

        if (Properties.ALGORITHM == Properties.Algorithm.DYNAMOSA
                && Properties.MUTATION_STRATEGY == Properties.MutationStrategy.GUIDED) {
            GuidedInsertion insertion = GuidedInsertion.getInstance();
            insertion.setGoalsManager(mgr);
        }
    }

    /**
     * Performs a roulette wheel selection on the given collection of gaols. The probability of a
     * goal being selected is inversely proportional to the cyclomatic complexity of the target
     * executable (i.e., method or constructor) of that goal. This means that two executables with
     * the same cyclomatic complexity have the same probability of being selected, while an
     * executable that is twice as complex as another executable only has half the probability of
     * being selected.
     *
     * @param goals the goals on which to perform the selection
     * @return a goal chosen via biased-random selection
     */
    public static Optional<TestFitnessFunction> rouletteWheelSelect(final Collection<TestFitnessFunction> goals) {
        return Randomness.rouletteWheelSelect(goals,
                // The probability of a goal being selected is inversely proportional to its
                // complexity.
                g -> 1d / g.getCyclomaticComplexity());
    }

    public static Optional<TestFitnessFunction> chooseGoal(final Collection<TestFitnessFunction> goals) {
        if (Properties.FAVOR_SIMPLE_METHODS) {
            return rouletteWheelSelect(goals);
        } else {
            return Optional.ofNullable(Randomness.choice(goals));
        }
    }
}
