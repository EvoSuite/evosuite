package org.evosuite.idnaming;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmr on 31/07/15.
 */
public class TestIDNamingStandalone {

    @Test
    public void testIDNamingStandalone() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

        // Branch goals for branchless methods (root branches)
        TestFitnessFunction goal1 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","keys()[I", -1));
        TestFitnessFunction goal2 = new BranchCoverageTestFitness(new BranchCoverageGoal(null, true, "com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V", -1));
      //  TestFitnessFunction goal3 = new BranchCoverageTestFitness(new BranchCoverageGoal(null, true, "com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "values()[B", -1));

        // Branch instruction in method isPositive(I)Z
        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);

       
         // Output goals for method isPositive(I)Z
        TestFitnessFunction goal8 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()","I","empty"));
        TestFitnessFunction goal9 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()","I","nonempty"));

        // Method goals for all methods in the class
        TestFitnessFunction goal10 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
        TestFitnessFunction goal11 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()");
     //   TestFitnessFunction goal12 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "mist(I)V");
      //  TestFitnessFunction goal13 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "bar(I)V");
     //   TestFitnessFunction goal14 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(I)V");

        // Overloaded methods become different method goals (difference in the number (or type!) of arguments)
       // TestFitnessFunction goal15 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(Z)V");
       // TestFitnessFunction goal16 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(IIII)V");
  //      TestFitnessFunction goal3 = new InputCoverageTestFitness(new InputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()",-1,"I","nonempty"));
        TestFitnessFunction goal4 = new InputCoverageTestFitness(new InputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V",-1,"0","nonull"));

        
        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal10);
        test1.addCoveredGoal(goal11);
        test1.addCoveredGoal(goal4);
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);


        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal10);
        test2.addCoveredGoal(goal11);
        test2.addCoveredGoal(goal8);
        test2.addCoveredGoal(goal4);
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal2);
        
        DefaultTestCase test3 = new DefaultTestCase();
        test3.addStatement(new StringPrimitiveStatement(test3,"foo1")); // any statement to fool hashcode function
        test3.addCoveredGoal(goal10);
        test3.addCoveredGoal(goal11);
        test3.addCoveredGoal(goal9);
        test3.addCoveredGoal(goal4);
        test3.addCoveredGoal(goal1);
        test3.addCoveredGoal(goal2);

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

        String nameTest3 = TestNameGenerator.getNameGeneratedFor(test3);

        assertEquals("Generated test name differs from expected", "test_init_mist_isPositive_foo_isPositive_init_isPositive_mist_foo", nameTest1);
        assertEquals("Generated test name differs from expected", "test_init_mist_bar_isPositive_foo_isPositive_init_isPositive_mist_bar_foo", nameTest2);
    }
}
