package org.evosuite.idnaming;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageGoal;
import org.evosuite.coverage.input.InputCoverageTestFitness;
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
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;

public class TestSameNames2 {
	@Test
	 public void testIDNamingWithSameMethodGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
     TestFitnessFunction goal1 = new MethodCoverageTestFitness("org.jdom2.DocType","<init>(Lorg/apache/commons/math/Field;II)V");
     TestFitnessFunction goal2 = new MethodCoverageTestFitness("org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)");

     JumpInsnNode inst1 = new JumpInsnNode(155,null);
     BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
     BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
     Branch br1 = new Branch(bcInst1, 1);
     TestFitnessFunction goal4 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/Field;II)V", -1)); 
       
     TestFitnessFunction goal7 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V",-1,"0","nonull"));
     TestFitnessFunction goal8 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V",-1,"1","zero"));
     TestFitnessFunction goal9 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)V",-1,"2","zero"));
     TestFitnessFunction goal3 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/linear/FieldMatrix;)",-1,"3","null"));
     TestFitnessFunction goal10 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)",-1,"4","positive"));
     TestFitnessFunction goal11 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/Field;II)",-1,"5","positive"));
     TestFitnessFunction goal12 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "<init>(Lorg/apache/commons/math/linear/FieldMatrix;)",-1,"6","nonull"));
     
     TestFitnessFunction goal5 = new ExceptionCoverageTestFitness("<init>(Lorg/apache/commons/math/Field;II)V"
    		  ,ArrayIndexOutOfBoundsException.class ,ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);
     
     TestFitnessFunction goal31 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getColumnDimension()I", -1));
     TestFitnessFunction goal32= new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getRowDimension()I", -1));
     TestFitnessFunction goal33 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/Field;II)V", -1));
     TestFitnessFunction goal34 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getEntry(II)Lorg/apache/commons/math/FieldElement;", -1));
     TestFitnessFunction goal35 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","computeKey(II)I", -1));
     TestFitnessFunction goal36 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)V", -1));
     TestFitnessFunction goal37 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)V", -1));
     TestFitnessFunction goal38 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>(Lorg/apache/commons/math/linear/FieldMatrix;)V", -1));
     TestFitnessFunction goal39 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","setEntry(IILorg/apache/commons/math/FieldElement;)V", -1));
    
     
     DefaultTestCase test1 = new DefaultTestCase();
     test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
     test1.addCoveredGoal(goal1);
     test1.addCoveredGoal(goal4);
     test1.addCoveredGoal(goal7);
     test1.addCoveredGoal(goal8);
     test1.addCoveredGoal(goal9);
     test1.addCoveredGoal(goal5);

     DefaultTestCase test2 = new DefaultTestCase();
     test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
     test2.addCoveredGoal(goal2);
     test2.addCoveredGoal(goal3);
     test2.addCoveredGoal(goal5);
  
     DefaultTestCase test3 = new DefaultTestCase();
     test3.addStatement(new StringPrimitiveStatement(test3,"bar")); // any statement to fool hashcode function
     test3.addCoveredGoal(goal1);
     test3.addCoveredGoal(goal2);
     test3.addCoveredGoal(goal31);
     test3.addCoveredGoal(goal32);
     test3.addCoveredGoal(goal33);
     test3.addCoveredGoal(goal34);
     test3.addCoveredGoal(goal35);
     test3.addCoveredGoal(goal36);
     test3.addCoveredGoal(goal37);
     test3.addCoveredGoal(goal38);
     test3.addCoveredGoal(goal39);
     test3.addCoveredGoal(goal7);
     test3.addCoveredGoal(goal10);
     test3.addCoveredGoal(goal11);
     test3.addCoveredGoal(goal12);
     test3.addCoveredGoal(goal5);

     ArrayList<TestCase> testCases = new ArrayList<TestCase>();
     testCases.add(test1);
     testCases.add(test2);
     testCases.add(test3);

     List results = new ArrayList<ExecutionResult>();
     results.add(null);
     results.add(null);
     results.add(null);
     
     TestNameGenerator.getInstance().NAMING_TYPE="method_output_branch_input";
     TestNameGenerator.getInstance().execute(testCases, results);
     String nameTest1 = TestNameGenerator.getNameGeneratedFor(test1);
     String nameTest2 = TestNameGenerator.getNameGeneratedFor(test2);
  //   String nameTest3 = TestNameGenerator.getNameGeneratedFor(test3);

     assertEquals("Generated test name differs from expected", "test_KeysReturningEmpty", nameTest1);
     assertEquals("Generated test name differs from expected", "test_KeysReturningNonempty", nameTest2);
  //   assertEquals("Generated test name differs from expected", "test_KeysReturningNonempty", nameTest3);

	}
}
