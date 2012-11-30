package org.evosuite.testcase;

import org.evosuite.utils.LoggingUtils;

public class StructuredTestCodeVisitor extends TestCodeVisitor {

	private StructuredTestCase structuredTest = null;
	
	private int exercisePosition = 0;
	
	private int checkingPosition = 0;
	
	@Override
	public void visitTestCase(TestCase test) {
		if(!(test instanceof StructuredTestCase))
			throw new IllegalArgumentException("Need StructuredTestCase");
		
		this.structuredTest = (StructuredTestCase)test;
		this.exercisePosition = structuredTest.getFirstExerciseStatement();
		this.checkingPosition = structuredTest.getFirstCheckingStatement();
		super.visitTestCase(test);
		LoggingUtils.getEvoLogger().info("Using new structured code visitor");
	}
	
	@Override
	public void visitStatement(StatementInterface statement) {
		LoggingUtils.getEvoLogger().info("Visiting structured code statement");
		int position = statement.getPosition();
		LoggingUtils.getEvoLogger().info("Exercise position: "+exercisePosition+", current position: "+position+" / "+structuredTest.size());
		if(position == exercisePosition)
			testCode += "\n// Exercise\n";
		else if(position == 0)
			testCode += "// Setup\n";
		else if(position == checkingPosition)
			testCode += "\n// Check\n";
		
		super.visitStatement(statement);
	}

}
