/*
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
package org.evosuite.runtime;

/*
    Note: as of JUnit 4.12, "internal" is deprecated. We keep it to avoid
    issues with previous versions.
    It should replace it once a new version of JUnit does not support it anymore
 */

import org.junit.internal.AssumptionViolatedException;

/**
 * If a test was overfitting (eg, accessing private fields or methods), and
 * a semantic-preserving refactoring
 * broke the test, then it should not fail, as otherwise it would be a time consuming
 * false positive.
 * <p>
 * <p>
 * Created by Andrea Arcuri on 05/10/15.
 */
public class FalsePositiveException extends AssumptionViolatedException {

    private static final long serialVersionUID = -7779068356023351829L;

    public FalsePositiveException(String assumption) {
        super(assumption);
    }
}
