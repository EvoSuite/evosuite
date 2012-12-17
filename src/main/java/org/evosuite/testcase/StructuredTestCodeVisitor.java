package org.evosuite.testcase;

import org.evosuite.assertion.Assertion;

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
	public void visitStatement(StatementInterface statement) {
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
