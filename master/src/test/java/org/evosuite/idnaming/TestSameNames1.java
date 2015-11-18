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

public class TestSameNames1 {
	@Test
	 public void testIDNamingWithSameMethodGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
      TestFitnessFunction goal1 = new MethodCoverageTestFitness("org.jdom2.DocType","<init>()V");
      TestFitnessFunction goal2 = new MethodCoverageTestFitness("org.jdom2.DocType","getPublicID()Ljava/lang/String");
      TestFitnessFunction goal3 = new MethodCoverageTestFitness("org.jdom2.DocType","setPublicID(Ljava/lang/String;)Lorg/jdom2/DocType");

      JumpInsnNode inst1 = new JumpInsnNode(155,null);
      BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
      BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
      Branch br1 = new Branch(bcInst1, 1);
      TestFitnessFunction goal4 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","getPublicID()Ljava/lang/String;", -1));
      TestFitnessFunction goal5 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","<init>()V", -1));
      TestFitnessFunction goal6 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"org.jdom2.DocType","setPublicID(Ljava/lang/String;)Lorg/jdom2/DocType;", -1));
      
      TestFitnessFunction goal7 = new OutputCoverageTestFitness(new OutputCoverageGoal("org.jdom2.DocType", "getPublicID()","Ljava/lang/String;","nonnull"));
      TestFitnessFunction goal8 = new OutputCoverageTestFitness(new OutputCoverageGoal("org.jdom2.DocType", "setPublicID(Ljava/lang/String;)","Lorg/jdom2/DocType;","nonnull"));
      
      TestFitnessFunction goal9 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "setPublicID(Ljava/lang/String;)",-1,"0","nonull"));
      TestFitnessFunction goal10 = new InputCoverageTestFitness(new InputCoverageGoal("org.jdom2.DocType", "setPublicID(Ljava/lang/String;)",-1,"0","null"));
     
      DefaultTestCase test1 = new DefaultTestCase();
      test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
      test1.addCoveredGoal(goal1);
      test1.addCoveredGoal(goal2);
      test1.addCoveredGoal(goal3);
      test1.addCoveredGoal(goal4);
      test1.addCoveredGoal(goal5);
      test1.addCoveredGoal(goal6);
      test1.addCoveredGoal(goal7);
      test1.addCoveredGoal(goal8);
      test1.addCoveredGoal(goal9);

      DefaultTestCase test2 = new DefaultTestCase();
      test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
      test2.addCoveredGoal(goal1);
      test2.addCoveredGoal(goal3);
      test2.addCoveredGoal(goal5);
      test2.addCoveredGoal(goal6);
      test2.addCoveredGoal(goal8);
      test2.addCoveredGoal(goal10);
   

   

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
   //   String nameTest3 = TestNameGenerator.getNameGeneratedFor(test3);

      assertEquals("Generated test name differs from expected", "test_KeysReturningEmpty", nameTest1);
      assertEquals("Generated test name differs from expected", "test_KeysReturningNonempty", nameTest2);
   //   assertEquals("Generated test name differs from expected", "test_KeysReturningNonempty", nameTest3);

	}
}
