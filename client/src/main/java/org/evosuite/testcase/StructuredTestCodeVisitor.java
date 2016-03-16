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

import org.evosuite.assertion.Assertion;
import org.evosuite.testcase.statements.Statement;

public class StructuredTestCodeVisitor extends TestCodeVisitor {

	private StructuredTestCase structuredTest = null;

	private int exercisePosition = 0;

	private int checkingPosition = 0;

	@Override
	public void visitTestCase(TestCase test) {
		if (!(test instanceof StructuredTestCase))
			throw new IllegalArgumentException("Need StructuredTestCase");

		this.structuredTest = (StructuredTestCase) test;
		this.exercisePosition = structuredTest.getFirstExerciseStatement();
		this.checkingPosition = structuredTest.getFirstCheckingStatement();
		super.visitTestCase(test);
		if (exceptions.isEmpty())
			checkAdded = false;
		else
			checkAdded = true;
	}

	private boolean checkAdded = false;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestCodeVisitor#visitAssertion(org.evosuite.assertion.Assertion)
	 */
	@Override
	protected void visitAssertion(Assertion assertion) {
		if (!checkAdded && assertion.getStatement().getPosition() == checkingPosition) {
			testCode += "\n// Check\n";
			checkAdded = true;
		}

		/*
		Set<Mutation> killedMutants = assertion.getKilledMutations();
		if (!killedMutants.isEmpty()) {
			testCode += "// Kills: ";
			boolean first = true;
			for (Mutation m : killedMutants) {
				if (!first) {
					testCode += ", ";
				} else {
					first = false;
				}
				testCode += m.getMethodName() + "-" + m.getId();
			}
			testCode += "\n";
		}
		*/
		super.visitAssertion(assertion);
	}

	@Override
	public void visitStatement(Statement statement) {
		int position = statement.getPosition();
		if (position == exercisePosition)
			testCode += "\n// Exercise\n";
		else if (position == 0)
			testCode += "// Setup\n";

		super.visitStatement(statement);
		if (position == checkingPosition) {
			if (!checkAdded && !statement.hasAssertions()) {
				testCode += "\n// Check\n";
				checkAdded = true;
			}
		}
	}

}
