package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class IndicatorComparisonStrategyTest {

    private final IndicatorComparisonStrategy indicatorComparisonStrategy = new IndicatorComparisonStrategy();

    @SuppressWarnings("unused")
    static Stream<Arguments> generateChromosomes() {
        return Stream.of(
                Arguments.of(Arrays.asList(
                        createChromosome(new double[]{7, 50, 1}),
                        createChromosome(new double[]{4, 7, 0}),
                        createChromosome(new double[]{1, 3, 7})),
                        new double[]{0.857, 2.415, 2.0}),
                Arguments.of(Arrays.asList(
                        createChromosome(new double[]{1, 1, 1}),
                        createChromosome(new double[]{1, 1, 1}),
                        createChromosome(new double[]{1, 1, 1})),
                        new double[]{1.5, 1.5, 1.5})
        );
    }

    private static Chromosome createChromosome(double[] indicators) {
        Chromosome chromosome = new TestChromosome();
        for (int i = 0; i < indicators.length; i++)
            chromosome.setIndicatorValues(Double.toString(i), indicators[i]);
        return chromosome;
    }

    @ParameterizedTest
    @MethodSource("generateChromosomes")
    public void testPerformanceScoreAsDistance(List<TestChromosome> chromosomes, double[] oracleScores) {
        indicatorComparisonStrategy.setDistances(chromosomes);
        Assert.assertArrayEquals(chromosomes.stream().mapToDouble(TestChromosome::getDistance).toArray(),
                oracleScores, 0.1);
    }
}