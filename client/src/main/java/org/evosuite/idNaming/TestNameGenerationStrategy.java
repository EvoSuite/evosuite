package org.evosuite.idNaming;

import org.evosuite.testcase.TestCase;

/**
 * Created by gordon on 22/12/2015.
 */
public interface TestNameGenerationStrategy {
    public String getName(TestCase test);
}
