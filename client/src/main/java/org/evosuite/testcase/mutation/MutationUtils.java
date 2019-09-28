package org.evosuite.testcase.mutation;

import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.graphs.ddg.MethodEntry;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.mutation.insertion.GuidedInsertion;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class MutationUtils {
    private static final Logger logger = LoggerFactory.getLogger(MutationUtils.class);

    private static final int parallelComputationThreshold = 50; // just arbitrarily picked
    private static final int binarySearchThreshold = 10;        // just arbitrarily picked

    public static <T> Optional<T> rouletteWheelSelect(final Collection<T> choices,
                                                      ToDoubleFunction<T> mapper) {
        if (choices.isEmpty()) {
            logger.warn("Nothing to choose from");
            return Optional.empty();
        }

        /*
         * The collection of choices could be unordered (e.g., when it's a set). Still, the inner
         * workings of the roulette wheel selection require some arbitrary but fixed order. We
         * impose this order by converting the collection to an array (arrays also offer
         * good performance and random access, which the algorithm also benefits from). The imposed
         * order, even though being arbitrary, has no impact on the outcome of the selection,
         * since it's fixed during the course of the selection.
         */
        @SuppressWarnings("unchecked")
        final T[] cs = choices.toArray((T[]) new Object[0]);
        Randomness.shuffle(cs); // just to be sure

        if (cs.length == 1) {
            return Optional.of(cs[0]);
        }

        if (cs.length == 2) {
            final double cs0 = mapper.applyAsDouble(cs[0]);
            final double cs1 = mapper.applyAsDouble(cs[1]);

            // The mapper must produce non-negative values.
            if (cs0 < 0 || cs1 < 0) {
                logger.error("Mapper produced some negative results: {} {}", cs0, cs1);
                return Optional.empty();
            }

            final double sum = cs0 + cs1;
            if (!(Double.isFinite(sum) && sum > 0)) {
                logger.error("invalid interval length {}", sum);
                return Optional.empty();
            }

            final double pivot = Randomness.nextDouble(sum);
            final T c = cs0 < pivot ? cs[0] : cs[1];
            return Optional.of(c);
        }

        // The prefix sum of the mapped values is used to determine the index of the chosen
        // element later on.
        final double[] prefixSum = prefixSum(cs, mapper);

        if (prefixSum == null) {
            logger.error("Error during computation of prefix sum");
            return Optional.empty();
        }

        final double sum = prefixSum[prefixSum.length - 1];
        if (!(Double.isFinite(sum) && sum > 0)) {
            logger.error("invalid interval length {}", sum);
            return Optional.empty();
        }

        // We spin the roulette wheel and obtain a pivot point. This is the point on the wheel
        // where the roulette ball falls onto after having lost all of its momentum.
        final double pivot = Randomness.nextDouble(sum);

        // Finds the pocket on the wheel where the pivot point is located in and converts it to an
        // array  index. This index corresponds to the selected goal.
        final int index = findIndex(prefixSum, pivot);

        return Optional.of(cs[index]);
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
        return rouletteWheelSelect(goals,
                // The probability of a goal being selected is inversely proportional to its
                // complexity.
                g -> 1d / g.getCyclomaticComplexity());
    }

    private static <T> double[] prefixSum(final T[] elements, ToDoubleFunction<T> mapper) {
        final double[] prefixSum;

        final boolean parallelComputation = elements.length > parallelComputationThreshold;
        if (parallelComputation) {
            prefixSum = Arrays.stream(elements).parallel()
                    .mapToDouble(mapper)
                    .toArray();
            Arrays.parallelPrefix(prefixSum, Double::sum);
        } else {
            prefixSum = new double[elements.length];
            prefixSum[0] = mapper.applyAsDouble(elements[0]);
            for (int i = 1; i < elements.length; i++) {
                prefixSum[i] = prefixSum[i - 1] + mapper.applyAsDouble(elements[i]);
            }
        }

        // The mapper must produce non-negative values only. If this is satisfied, the array must
        // be sorted by construction.
        final boolean strictlySorted;
        if (parallelComputation) {
            strictlySorted = IntStream.range(0, prefixSum.length - 1)
                    .parallel()
                    .allMatch(i -> prefixSum[i] < prefixSum[i + 1]);
        } else {
            strictlySorted = IntStream.range(0, prefixSum.length - 1)
                    .allMatch(i -> prefixSum[i] < prefixSum[i + 1]);
        }

        if (!strictlySorted) {
            logger.error("prefix sums array is not strictly sorted");
            return null;
        }

        return prefixSum;
    }

    /**
     * Searches the given strictly sorted array for the specified key and returns the appropriate
     * index where the key is found. If the array does not contain the key, the insertion point of
     * the key (i.e. the index where it would be inserted) is returned instead.
     *
     * @param sortedArray the array to be searched (must be sorted and not contain duplicates)
     * @param key         the value to search for in the array
     * @return the index of the key or its insertion point if the key is not found
     */
    private static int findIndex(final double[] sortedArray, final double key) {
        final boolean binarySearch = sortedArray.length > binarySearchThreshold;
        if (binarySearch) {
            final int index = Arrays.binarySearch(sortedArray, key);
            return index < 0 ? ~index : index;
        } else { // linear search
            final int lastIndex = sortedArray.length - 1;
            for (int i = 0; i < lastIndex; i++) {
                if (key < sortedArray[i]) { // the array is sorted and free of duplicates
                    return i;
                }
            }
            return lastIndex;
        }
    }

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
}
