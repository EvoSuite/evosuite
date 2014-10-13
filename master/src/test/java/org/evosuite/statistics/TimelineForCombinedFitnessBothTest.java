package org.evosuite.statistics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Failing test that runs TimelineForCombinedFitness1Test followed by
 * TimelineForCombinedFitness2Test.
 *
 * It fails
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({ TimelineForCombinedFitness1Test.class, TimelineForCombinedFitness2Test.class })
public class TimelineForCombinedFitnessBothTest {
}
