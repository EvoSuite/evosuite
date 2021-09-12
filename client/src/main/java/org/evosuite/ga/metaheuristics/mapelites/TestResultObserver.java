/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics.mapelites;

import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Felix Prasse
 */
public class TestResultObserver extends ExecutionObserver implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Inspector[] inspectors;

    private final Class<?> targetClass;

    public TestResultObserver() {
        this.targetClass = Properties.getInitializedTargetClass();

        this.inspectors =
                InspectorManager.getInstance().getInspectors(this.targetClass).toArray(new Inspector[0]);

        // Sort by method name to ensure a consistent feature vector order.
        Arrays.sort(this.inspectors, (a, b) -> a.getMethodCall().compareTo(b.getMethodCall()));
    }

    public int getPossibilityCount() {
        return FeatureVector.getPossibilityCount(this.inspectors);
    }

    public int getFeatureVectorLength() {
        return this.inspectors.length;
    }

    @Override
    public void output(int position, String output) {
        // Do nothing
    }

    @Override
    public void beforeStatement(Statement statement, Scope scope) {
        // Do nothing

    }

    @Override
    public void afterStatement(Statement statement, Scope scope, Throwable exception) {
        // Do nothing
    }

    @Override
    public void testExecutionFinished(ExecutionResult result, Scope scope) {
        for (Object instance : scope.getObjects(this.targetClass)) {
            FeatureVector vector = new FeatureVector(this.inspectors, instance);
            result.addFeatureVector(vector);
        }
    }

    @Override
    public void clear() {
        //  Do nothing
    }
}