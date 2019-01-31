package org.evosuite.performance.indicator;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.ga.Chromosome;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.BasicBlock;
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

    /** To keep track of the branches, whose basic blocks are the beginning of loops */
    private static Set<Branch> loopBranches = null;

    /** To keep track of the starting and ending basic blocks of loops */
    private static HashMap<Branch,BasicBlock> loops = null;

    public LoopCounter(){
        super();
        // we retrieve only branches (condition points) within loops
        if (loopBranches == null) {
            // lets initialize the data structure only once
            loopBranches = new HashSet<>();
            loops  = new HashMap<>();
            for (Branch b : BranchPool
                    .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
                    .getAllBranches()) {
                BasicBlock endLoop = findLoop(b.getInstruction().getBasicBlock(), b.getInstruction().getActualCFG());
                if (endLoop != null && b != null) {
                    loopBranches.add(b);
                    loops.put(b, endLoop);
                }
            }
        }
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
        double counter = 1.0;

        Map<Integer, Integer> noExecutionForConditionalNode =
                result.getTrace().getNoExecutionForConditionalNode();

        /**/
        for (Branch branch : loopBranches){
            Integer freq = noExecutionForConditionalNode.get(branch.getActualBranchId());
            BasicBlock end = loops.get(branch);
            if (freq!=null && freq > 2)
                counter += freq;
            /*
            BasicBlock start = branch.getInstruction().getBasicBlock(); // start point of the loop
            BasicBlock end = loops.get(branch); // end point of the loop
            if (result.getTrace().getCoveredLines().contains(end.getLastLine())) {
                int id = branch.getActualBranchId();
                if (noExecutionForConditionalNode.containsKey(id)){
                    int val = noExecutionForConditionalNode.get(id);
                    if (val >2)
                        counter += noExecutionForConditionalNode.get(id);
                }
            }*/
            //logger.error("{}, {}, {}", branch, loops.size(), result.getTrace().getCoveredLines().size());
        }
        //logger.error("HERE");
        /**
         double counter2 = noExecutionForConditionalNode.values().stream()
         .filter(no -> no >= 2)
         .mapToInt(Integer::intValue)
         .sum();

         //logger.error("Before {}, after{}",counter,counter2);
         */
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
     * @param startNode BasicBlock which is the starting node of the loop
     * @param CFG the control flow graph containing startNode
     * @return null if there is no loop startig with startNode;
     * otherwise it return the last block in the loop (the one just before startNode)
     */
    protected BasicBlock findLoop(BasicBlock startNode, ActualControlFlowGraph CFG){
        Queue<BasicBlock> queue = new LinkedList<>(); //
        Set<BasicBlock> visited = new HashSet<>();

        Set<ControlFlowEdge> edges = CFG.edgeSet();

        queue.add(startNode);

        while (!queue.isEmpty()){
            BasicBlock node =  queue.poll();
            visited.add(node);
            for (ControlFlowEdge edge : edges){
                if (CFG.getEdgeSource(edge).equals(node)){
                    BasicBlock child = CFG.getEdgeTarget(edge);
                    if (child.equals(startNode))
                        return node;
                    else if (!visited.contains(child)) {
                        queue.add(child);
                    }
                }
            }
        }

        return null;
    }
}
