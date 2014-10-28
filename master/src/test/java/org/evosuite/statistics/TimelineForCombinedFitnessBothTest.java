package org.evosuite.statistics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test that runs TimelineForCombinedFitness1Test followed by
 * TimelineForCombinedFitness2Test.
 *
 */
@RunWith(Suite.class)

@Suite.SuiteClasses({ TimelineForCombinedFitness1Test.class, TimelineForCombinedFitness2Test.class })
public class TimelineForCombinedFitnessBothTest {
}
