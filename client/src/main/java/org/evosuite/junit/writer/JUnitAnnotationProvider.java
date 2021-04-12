package org.evosuite.junit.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JUnitAnnotationProvider {

    /**
     * Get the annotation for a test case
     *
     * @return the annotation.
     */
    public Class<?> testAnnotation();

    /**
     * Get the annotation for methods that shall be executed before any test is executed.
     *
     * @return the annotation.
     */
    public Class<?> beforeAll();

    /**
     * Get the annotation for methods that shall be executed before every test.
     *
     * @return the annotation
     */
    public Class<?> beforeEach();

    /**
     * Get the annotation for methods that shall be executed after all tests are executed.
     *
     * @return the annotation
     */
    public Class<?> afterAll();

    /**
     * Get the annotation for methods that shall be executed after each test.
     *
     * @return the annotation.
     */
    public Class<?> afterEach();

}
