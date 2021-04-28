/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
