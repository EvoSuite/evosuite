package org.evosuite.idnaming;

import com.examples.with.different.packagename.idnaming.gnu.trove.decorator.TObjectCharMapDecorator;
import com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableShortSet;
import com.examples.with.different.packagename.idnaming.gnu.trove.list.TLongList;
import com.examples.with.different.packagename.idnaming.gnu.trove.procedure.TShortShortProcedure;
import com.examples.with.different.packagename.idnaming.gnu.trove.set.TCharSet;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.TestFitnessFactory;
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
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.idNaming.TestNameGenerator;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;

public class TestIdNamingWithSameMethodGoals {

	@Test
	 public void testIdNamingWithSameMethodGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
        TestFitnessFunction goal1 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap","keys()[I");
        TestFitnessFunction goal2 = new MethodCoverageTestFitness("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "<init>(Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TIntByteMap;)V");
        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);
        TestFitnessFunction goal3 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()","I","empty"));
        TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.gnu.trove.impl.unmodifiable.TUnmodifiableIntByteMap", "keys()","I","nonempty"));

        DefaultTestCase test1 = new DefaultTestCase();
        test1.addStatement(new IntPrimitiveStatement(test1,42)); // any statement to fool hashcode function
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);
        test1.addCoveredGoal(goal3);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addStatement(new StringPrimitiveStatement(test2,"foo")); // any statement to fool hashcode function
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal4);


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

        assertEquals("Generated test name differs from expected", "test_KeysReturningEmpty", nameTest1);
        assertEquals("Generated test name differs from expected", "test_KeysReturningNonempty", nameTest2);
    }

}