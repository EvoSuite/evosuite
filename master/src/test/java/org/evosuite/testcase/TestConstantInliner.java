package org.evosuite.testcase;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.junit.Test;

import com.examples.with.different.packagename.ObjectParameter;

public class TestConstantInliner {

	@Test
	public void testArrayIndexInlining() throws NoSuchMethodException, SecurityException {
		DefaultTestCase test = new DefaultTestCase();
		ConstructorStatement cs = new ConstructorStatement(test, new GenericConstructor(Object.class.getConstructor(), Object.class), new ArrayList<VariableReference>());
		VariableReference objectVar = test.addStatement(cs);
		
		ArrayStatement as = new ArrayStatement(test, Object[].class, 3);
		test.addStatement(as);
		
		ArrayReference arrayVar = as.getArrayReference();
		
		ArrayIndex ai0 = new ArrayIndex(test, arrayVar, 0);
		ArrayIndex ai1 = new ArrayIndex(test, arrayVar, 1);
		ArrayIndex ai2 = new ArrayIndex(test, arrayVar, 2);
		test.addStatement(new AssignmentStatement(test, ai0, objectVar));
		test.addStatement(new AssignmentStatement(test, ai1, objectVar));
		test.addStatement(new AssignmentStatement(test, ai2, objectVar));
		
		ConstructorStatement sutCS = new ConstructorStatement(test, new GenericConstructor(ObjectParameter.class.getConstructor(), ObjectParameter.class), new ArrayList<VariableReference>());
		VariableReference sut = test.addStatement(sutCS);

		List<VariableReference> parameters = new ArrayList<VariableReference>();
		parameters.add(ai0);
		test.addStatement(new MethodStatement(test, new GenericMethod(ObjectParameter.class.getMethods()[0], ObjectParameter.class), sut, parameters));
		parameters = new ArrayList<VariableReference>();
		parameters.add(ai1);
		test.addStatement(new MethodStatement(test, new GenericMethod(ObjectParameter.class.getMethods()[0], ObjectParameter.class), sut, parameters));
		parameters = new ArrayList<VariableReference>();
		parameters.add(ai2);
		test.addStatement(new MethodStatement(test, new GenericMethod(ObjectParameter.class.getMethods()[0], ObjectParameter.class), sut, parameters));
		System.out.println(test.toCode());
		
		ConstantInliner inliner = new ConstantInliner();
		inliner.inline(test);
		
		String code = test.toCode();
		assertFalse(code.contains("objectParameter0.testMe(objectArray0"));
	}
}
