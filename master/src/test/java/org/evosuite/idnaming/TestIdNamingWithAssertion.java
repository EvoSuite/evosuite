package org.evosuite.idnaming;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageGoal;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.idNaming.TestNameGenerator;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;

import com.examples.with.different.packagename.StringConstantInliningExample;
import com.googlecode.gentyref.TypeToken;

public class TestIdNamingWithAssertion {
	@Test
	 public void testIDNamingWithDifferentArguments() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
     TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values([B)[B");
     
     JumpInsnNode inst1 = new JumpInsnNode(155,null);
     BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
     BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
     Branch br1 = new Branch(bcInst1, 1);
     TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)","[B","empty"));
     TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()","[B","nonempty"));
     
  
     DefaultTestCase test1 = new DefaultTestCase();
     test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
     test1.addCoveredGoal(goal1);
     test1.addCoveredGoal(goal4);
     test1.addCoveredGoal(goal5);
     
 	Class<?> targetClass = com.examples.with.different.packagename.generic.GenericClassTwoParameters.class;
	Method creatorMethod = targetClass.getMethod("create", new Class<?>[] {});
	Method targetMethod = targetClass.getMethod("get",
	                                            new Class<?>[] { Object.class });
	Method inspectorMethod = targetClass.getMethod("testMe", new Class<?>[] {});
	Constructor<?> intConst = Integer.class.getConstructor(new Class<?>[] { int.class });

	GenericClass listOfInteger = new GenericClass(
	        new TypeToken<com.examples.with.different.packagename.generic.GenericClassTwoParameters<Integer, Integer>>() {
	        }.getType());
	GenericMethod genericCreatorMethod = new GenericMethod(creatorMethod, targetClass).getGenericInstantiationFromReturnValue(listOfInteger);
	System.out.println(genericCreatorMethod.getGeneratedClass().toString());
	GenericMethod genericMethod = new GenericMethod(targetMethod, targetClass).copyWithNewOwner(genericCreatorMethod.getGeneratedClass());
	System.out.println(genericMethod.getGeneratedClass().toString());

     DefaultTestCase test2 = new DefaultTestCase();
     MethodStatement ms1 = new MethodStatement(test2, genericCreatorMethod,
		        (VariableReference) null, new ArrayList<VariableReference>());
		test2.addStatement(ms1);

		IntPrimitiveStatement ps1 = (IntPrimitiveStatement) PrimitiveStatement.getPrimitiveStatement(test2,
		                                                                                             int.class);
		test2.addStatement(ps1);

		GenericConstructor intConstructor = new GenericConstructor(intConst,
		        Integer.class);
		List<VariableReference> constParam = new ArrayList<VariableReference>();
		constParam.add(ps1.getReturnValue());
		ConstructorStatement cs1 = new ConstructorStatement(test2, intConstructor,
		        constParam);
		//test.addStatement(cs1);

		List<VariableReference> callParam = new ArrayList<VariableReference>();
		callParam.add(ps1.getReturnValue());

		MethodStatement ms2 = new MethodStatement(test2, genericMethod,
		        ms1.getReturnValue(), callParam);
		test2.addStatement(ms2);

		Inspector inspector = new Inspector(targetClass, inspectorMethod);
		Assertion assertion = new InspectorAssertion(inspector, ms2,
		        ms1.getReturnValue(), 0);
		ms2.addAssertion(assertion);

	
    
     test2.addCoveredGoal(goal1);
     test2.addCoveredGoal(goal4);
     test2.addCoveredGoal(goal5);
  

     ArrayList<TestCase> testCases = new ArrayList<TestCase>();
     testCases.add(test1);
     testCases.add(test2);

     List results = new ArrayList<ExecutionResult>();
     results.add(null);
     results.add(null);

     TestNameGenerator.getInstance().NAMING_TYPE="method_output_branch_input";
     TestNameGenerator.getInstance().execute(testCases, results);
     String nameTest1 = TestNameGenerator.getNameGeneratedFor(test1);
     String nameTest2 = TestNameGenerator.getNameGeneratedFor(test2);
     System.out.println(nameTest1+nameTest2);
     assertEquals("Generated test name differs from expected", "test_Values", nameTest1);
     assertEquals("Generated test name differs from expected", "test_checksTestMe", nameTest2);
 }
}
