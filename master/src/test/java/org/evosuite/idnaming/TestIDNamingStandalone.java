package org.evosuite.idnaming;

import com.examples.with.different.packagename.idnaming.SimpleIdNaming;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.output.OutputCoverageGoal;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.idNaming.TestNameGenerator;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.utils.generic.GenericClass;
import org.junit.Test;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmr on 31/07/15.
 */
public class TestIDNamingStandalone {
    @Test
    public void testIDNamingStandalone() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

        TestFitnessFunction goal1 = new BranchCoverageTestFitness(new BranchCoverageGoal(null,true,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","<init>()V", -1));
        TestFitnessFunction goal2 = new BranchCoverageTestFitness(new BranchCoverageGoal(null, true, "com.examples.with.different.packagename.idnaming.SimpleIdNaming", "mist(I)V", -1));
        TestFitnessFunction goal3 = new BranchCoverageTestFitness(new BranchCoverageGoal(null, true, "com.examples.with.different.packagename.idnaming.SimpleIdNaming", "bar(I)V", -1));

        JumpInsnNode inst1 = new JumpInsnNode(155,null);
        BytecodeInstruction bcInst1 = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",3,1,inst1,9,null);
        BranchPool.registerAsBranch(bcInst1);
        Branch br1 = new Branch(bcInst1, 1);

        TestFitnessFunction goal4 = new BranchCoverageTestFitness(new BranchCoverageGoal(br1, true, "com.examples.with.different.packagename.idnaming.SimpleIdNaming", "isPositive(I)Z", 9));
        TestFitnessFunction goal5 = new BranchCoverageTestFitness(new BranchCoverageGoal(br1, false, "com.examples.with.different.packagename.idnaming.SimpleIdNaming", "isPositive(I)Z", 9));

        JumpInsnNode inst2 = new JumpInsnNode(153,null);
        BytecodeInstruction bcInst = new BytecodeInstruction(null,"com.examples.with.different.packagename.idnaming.SimpleIdNaming","isPositive(I)Z",5,5,inst2,16,null);
        BranchPool.registerAsBranch(bcInst);
        Branch br2 = new Branch(bcInst, 2);
        TestFitnessFunction goal6 = new BranchCoverageTestFitness(new BranchCoverageGoal(br2, true, "com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(I)V", 16));
        TestFitnessFunction goal7 = new BranchCoverageTestFitness(new BranchCoverageGoal(br2, false, "com.examples.with.different.packagename.idnaming.SimpleIdNaming", "foo(I)V", 16));

        TestFitnessFunction goal8 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "isPositive(I)Z","Z","true"));
        TestFitnessFunction goal9 = new OutputCoverageTestFitness(new OutputCoverageGoal("com.examples.with.different.packagename.idnaming.SimpleIdNaming", "isPositive(I)Z","Z","false"));

        DefaultTestCase test1 = new DefaultTestCase();
        test1.addCoveredGoal(goal1);
        test1.addCoveredGoal(goal2);
        test1.addCoveredGoal(goal4);
        test1.addCoveredGoal(goal6);
        test1.addCoveredGoal(goal8);

        DefaultTestCase test2 = new DefaultTestCase();
        test2.addCoveredGoal(goal1);
        test2.addCoveredGoal(goal2);
        test2.addCoveredGoal(goal3);
        test2.addCoveredGoal(goal5);
        test2.addCoveredGoal(goal7);
        test2.addCoveredGoal(goal9);

        ArrayList<TestCase> testCases = new ArrayList<TestCase>();
        testCases.add(test1);
        testCases.add(test2);


        List results = new ArrayList<ExecutionResult>();

        TestNameGenerator.generateAllTestNames(testCases,results);
    }
}
