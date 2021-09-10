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

package org.evosuite.testcase.execution;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * <p>InterfaceTestRunnable interface.</p>
 *
 * @author Sebastian Steenbuck
 */
public interface InterfaceTestRunnable extends Callable<ExecutionResult> {
    //#TODO steenbuck add javadoc

    /**
     * <p>getExceptionsThrown</p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Throwable> getExceptionsThrown();

    /**
     * <p>isRunFinished</p>
     *
     * @return a boolean.
     */
    boolean isRunFinished();
}
