package org.evosuite.performance.indicator;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.ga.Chromosome;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.graphs.cfg.ControlFlowEdge;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Counts the number of loops!
 * todo: still to test
 */
public class LoopCounter extends AbstractIndicator {

    private static final Logger logger = LoggerFactory.getLogger(LoopCounter.class);
    private static String INDICATOR = LoopCounter.class.getName();

    /**
     * To keep track of the branches, whose basic blocks are the beginning of loops
     */
    private static Set<Branch> loopBranches = null;

    public LoopCounter() {
        super();
        // we retrieve only branches (condition points) within loops
        if (loopBranches == null) {
            // lets initialize the data structure only once
            loopBranches = new HashSet<>();
            for (Branch b : BranchPool
                    .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
                    .getAllBranches()) {
                boolean hasLoop = hasLoop(b);
                if (hasLoop) {
                    loopBranches.add(b);
                }
            }
        }
        // getting only the first branch within the loop; ignoring all the control dependent ones
        Set<Branch> toRemove = new HashSet<>();
        for (Branch b : loopBranches) {
            ActualControlFlowGraph CFG = b.getInstruction().getActualCFG();
            for (Branch b2 : loopBranches) {
                for (BasicBlock parent : CFG.getParents(b.getInstruction().getBasicBlock())) {
                    if (parent.equals(b2.getInstruction().getBasicBlock())) {
                        toRemove.add(b);
                        break;
                    }
                }
            }
        }
        loopBranches.removeAll(toRemove);
    }

    @Override
    public double getIndicatorValue(Chromosome test) {
        if (test instanceof TestSuiteChromosome)
            throw new IllegalArgumentException("This indicator work at test case level");

        // if the test has already its indicator values, we don't need to re-compute them
        if (test.getIndicatorValues().keySet().contains(INDICATOR))
            return test.getIndicatorValue(INDICATOR);

        // retrieve the last execution
        TestChromosome chromosome = (TestChromosome) test;
        ExecutionResult result = chromosome.getLastExecutionResult();

        // let's initialize the counter
        double counter = 0.0;

        Map<Integer, Integer> noExecutionForConditionalNode =
                result.getTrace().getNoExecutionForConditionalNode();

        /**/
        for (Branch branch : loopBranches) {
            Integer freq = noExecutionForConditionalNode.get(branch.getActualBranchId());
            if (freq != null && freq >= 2)
                counter += freq;
        }

        test.setIndicatorValues(this.getIndicatorId(), counter);
        logger.info("No. definitions = " + counter);
        return counter;
    }

    public String getIndicatorId() {
        return INDICATOR;
    }


    /**
     * This method determines whether there is a loop in the CFG with starting point <code>startNode</code>.
     * A loop exists if there exists a path in the CFG that start with <code>startNode</code> and following the
     * child node we meet again  <code>startNode</code>. In the case such loop exists, this method return  the last node
     * in the loop (i.e., the one would lead back to the starting point <code>startNode</code>.
     *
     * @return null if there is no loop startig with startNode;
     * otherwise it return the last block in the loop (the one just before startNode)
     */
    protected boolean hasLoop(Branch branch) {
        Queue<BasicBlock> queue = new LinkedList<>(); //
        Set<BasicBlock> visited = new HashSet<>();

        ActualControlFlowGraph CFG = branch.getInstruction().getActualCFG();

        queue.add(branch.getInstruction().getBasicBlock());

        while (!queue.isEmpty()) {
            BasicBlock node = queue.poll();
            visited.add(node);
            for (BasicBlock child : CFG.getChildren(node)) {
                if (child.equals(branch.getInstruction().getBasicBlock()))
                    return true;
                else if (!visited.contains(child)) {
                    queue.add(child);
                }
            }
        }

        return false;
    }
}
