package org.evosuite.ga.metaheuristics.mapelites;

import org.evosuite.testcase.TestCase;

public interface TestFeatureMap {
  FeatureVector get(TestCase test);
}
