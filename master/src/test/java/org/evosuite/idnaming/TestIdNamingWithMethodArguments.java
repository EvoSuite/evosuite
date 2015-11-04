package org.evosuite.idnaming;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.TestGenerationContext;
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
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;

public class TestIdNamingWithMethodArguments {
	@Test
	 public void testIDNamingWithDifferentArguments() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
      TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values([B)[B");
      TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
      TestFitnessFunction goal3 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","values()[B");
      
      JumpInsnNode inst1 = new JumpInsnNode(155,null);
      BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
      BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
      Branch br1 = new Branch(bcInst1, 1);
      TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values([B)","[B","empty"));
      TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()","[B","nonempty"));
      
   
      DefaultTestCase test1 = new DefaultTestCase();
      test1.addStatement(new IntPrimitiveStatement(test1,11)); // any statement to fool hashcode function
      test1.addCoveredGoal(goal1);
      test1.addCoveredGoal(goal2);
      test1.addCoveredGoal(goal4);
      
      DefaultTestCase test2 = new DefaultTestCase();
      test2.addStatement(new StringPrimitiveStatement(test2,"barfoo")); // any statement to fool hashcode function
      test2.addCoveredGoal(goal3);
      test2.addCoveredGoal(goal2);
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
      assertEquals("Generated test name differs from expected", "test_ValuesWith1Argument", nameTest1);
      assertEquals("Generated test name differs from expected", "test_ValuesWithNoArgument", nameTest2);
  }
}
