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
package org.evosuite.testcase;

import org.evosuite.testcase.statements.Statement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StructuredTestCase extends DefaultTestCase {

	private static final long serialVersionUID = -1896651382970358963L;

	private final Set<TestFitnessFunction> primaryTargets = new HashSet<TestFitnessFunction>();

	private final Set<TestFitnessFunction> secondaryTargets = new HashSet<TestFitnessFunction>();

	private final Set<Integer> targetStatements = new HashSet<Integer>();

	public StructuredTestCase(TestCase test) {
		for (Statement statement : test) {
			addStatement(statement.clone(this));
		}
	}

	public void addPrimaryGoal(TestFitnessFunction goal) {
		primaryTargets.add(goal);
	}

	/**
	 * Determine the set of methods this test case is exercising
	 * 
	 * @return
	 */
	public Set<String> getTargetMethods() {
		Set<String> targetMethods = new HashSet<String>();
		for (TestFitnessFunction goal : primaryTargets) {
			targetMethods.add(goal.getTargetMethod());
		}
		return targetMethods;
	}

	/**
	 * Determine the class that is exercised by this test case
	 * 
	 * @return
	 */
	public String getTargetClass() {
		return null;
		//primaryTargets.iterator().next().
	}

	/**
	 * Determine if the given statement is part of the setup code
	 * 
	 * @param position
	 * @return
	 */
	public boolean isSetupStatement(int position) {

		int exerciseStart = Collections.min(targetStatements);
		return position < exerciseStart;
	}

	/**
	 * Determine if the given statement is part of the exercised code
	 * 
	 * @param position
	 * @return
	 */
	public boolean isExerciseStatement(int position) {
		return targetStatements.contains(position);
	}

	/**
	 * Return the first statement that is not setup code
	 * 
	 * @return
	 */
	public int getFirstExerciseStatement() {
		return Collections.min(targetStatements);
	}

	/**
	 * Tag a new statement as exercising statement
	 * 
	 * @param position
	 */
	public void setExerciseStatement(int position) {
		targetStatements.add(position);
	}

	/**
	 * Return the first statement that is not exercise code
	 * 
	 * @return
	 */
	public int getFirstCheckingStatement() {
		return Collections.max(targetStatements);
	}

	@Override
	public String toCode() {
		TestCodeVisitor visitor = new StructuredTestCodeVisitor();
		accept(visitor);
		return visitor.getCode();
	}

	@Override
	public String toCode(Map<Integer, Throwable> exceptions) {
		StructuredTestCodeVisitor visitor = new StructuredTestCodeVisitor();
		visitor.setExceptions(exceptions);
		accept(visitor);
		return visitor.getCode();
	}

	@Override
	public DefaultTestCase clone() {
		StructuredTestCase copy = new StructuredTestCase(this);
		copy.targetStatements.addAll(targetStatements);
		copy.primaryTargets.addAll(primaryTargets);
		copy.secondaryTargets.addAll(secondaryTargets);
		return copy;
	}

}
