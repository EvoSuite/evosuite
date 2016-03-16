/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.regression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionExecutionObserver extends ExecutionObserver {
	private static final Logger logger = LoggerFactory
			.getLogger(RegressionExecutionObserver.class);

	boolean isRegression = false;

	public boolean off = true;

	public void regressionFlag(boolean isRegression) {
		this.isRegression = isRegression;
	}

	/*
	 * Explanation of the "magic" code below:
	 * 
	 * These are object values, given a scope.
	 * List - ([optional] the following data can be obtained after executing each statement), of
	 * List - Scope contains a number of statements, of
	 * Map - Integer: variable reference position, of
	 * Map - String: class of the variable reference, of
	 * Map - String: class of the object, of primitive object value.
	 */
	
	public List<List<Map<Integer, Map<String, Map<String, Object>>>>> objectMapPool = new ArrayList<List<Map<Integer, Map<String, Map<String, Object>>>>>();
	public List<List<Map<Integer, Map<String, Map<String, Object>>>>> regressionObjectMapPool = new ArrayList<List<Map<Integer, Map<String, Map<String, Object>>>>>();

	public List<Map<Integer, Map<String, Map<String, Object>>>> currentObjectMapPool = new ArrayList<Map<Integer, Map<String, Map<String, Object>>>>();
	public List<Map<Integer, Map<String, Map<String, Object>>>> currentRegressionObjectMapPool = new ArrayList<Map<Integer, Map<String, Map<String, Object>>>>();

	@Override
	public void afterStatement(Statement statement, Scope scope,
			Throwable exception) {

	}

	public void requestNewPools() {
		currentObjectMapPool = new ArrayList<Map<Integer, Map<String, Map<String, Object>>>>();
		currentRegressionObjectMapPool = new ArrayList<Map<Integer, Map<String, Map<String, Object>>>>();
	}

	public void addToPools() {
		objectMapPool.add(currentObjectMapPool);
		regressionObjectMapPool.add(currentRegressionObjectMapPool);
	}

	public void addToPools(
			List<Map<Integer, Map<String, Map<String, Object>>>> currentObjectMapPool,
			List<Map<Integer, Map<String, Map<String, Object>>>> currentRegressionObjectMapPool) {
		objectMapPool.add(currentObjectMapPool);
		regressionObjectMapPool.add(currentRegressionObjectMapPool);
	}

	public void clearPools() {
		objectMapPool = new ArrayList<List<Map<Integer, Map<String, Map<String, Object>>>>>();
		regressionObjectMapPool = new ArrayList<List<Map<Integer, Map<String, Map<String, Object>>>>>();
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		long startTime = System.nanoTime();

		ObjectFields ovars = new ObjectFields(s);

		if (!off) {
			if (isRegression)
				currentRegressionObjectMapPool.add(ovars.getObjectVariables());
			else
				currentObjectMapPool.add(ovars.getObjectVariables());
		}

		RegressionSearchListener.odCollectionTime += System.nanoTime()
				- startTime;
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {

	}

}
