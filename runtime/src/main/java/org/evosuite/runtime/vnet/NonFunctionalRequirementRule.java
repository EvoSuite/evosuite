package org.evosuite.runtime.vnet;

import org.evosuite.runtime.TooManyResourcesException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/**
 * Avoid failing tests due to changes in non-functional requirements, like
 * for example execution time. This is necessary due to EvoSuite adding
 * arbitrary timeouts to prevent tests hanging or taking too long
 */
public class NonFunctionalRequirementRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (TooManyResourcesException e) {
                    //prevent TMRE to propagate to the JUnit runner
                } finally {
                }
            }
        };
    }
}
