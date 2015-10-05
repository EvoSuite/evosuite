package org.evosuite.runtime;

/*
    Note: as of JUnit 4.12, "internal" is deprecated. We keep it to avoid
    issues with previous versions.
    It should replace it once a new version of JUnit does not support it anymore
 */
import org.junit.internal.AssumptionViolatedException;

/**
 *  If a test was overfitting (eg, accessing private fields or methods), and
 *  a semantic-preserving refactoring
 *  broke the test, then it should not fail, as otherwise it would be a time consuming
 *  false positive.
 *
 *
 * Created by Andrea Arcuri on 05/10/15.
 */
public class FalsePositiveException extends AssumptionViolatedException{

    public FalsePositiveException(String assumption) {
        super(assumption);
    }
}
