package org.evosuite.performance.indicator;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author G. Grano, A. Panichella, S. Panichella
 *
 * Implements a dynamic performance indicator; it measure the number of statements covered by a test case.
 * Take also in account the collateral coverage.
 */
public class CoveredStatementsCounter extends AbstractIndicator {

    private static final Logger logger = LoggerFactory.getLogger(CoveredMethodCallCounter.class);
    private static final String INDICATOR = CoveredStatementsCounter.class.getName();

    /** To keep track of the size of each basic block, which is identified by its characterizing branch */
    private static HashMap<Integer,Integer> branches;

    /** To keep track of the size of each method */
    private static HashMap<String, Integer> methods;

    public CoveredStatementsCounter(){
        super();
        if (branches == null) {
            branches = new HashMap<>();
            for (Branch b : BranchPool
                    .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
                    .getAllBranches()) {
                BasicBlock block = b.getInstruction().getBasicBlock();
                branches.put(b.getActualBranchId(), (block.getLastLine() - block.getFirstLine()) + 1);
            }
            methods = new HashMap<>();
            List<BytecodeInstruction> list = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllInstructions();
            for (BytecodeInstruction instr : list) {
                String fullyQualifiedName = instr.getClassName() + "." + instr.getMethodName();
                methods.merge(fullyQualifiedName, 1, Integer::sum);
            }
        }
    }

    @Override
    public double getIndicatorValue(TestChromosome test) {
        if (test.getIndicatorValues().containsKey(INDICATOR))
            return test.getIndicatorValue(INDICATOR);

        // retrieve the last execution
        ExecutionResult result = test.getLastExecutionResult();

        Map<Integer, Integer> noExecutionForConditionalNode =
                result.getTrace().getNoExecutionForConditionalNode();

        // 1. We look at the objects instantiated in the covered production code
        //    First, we select the basic blocks associated with covered branches
        //    Then, we count the number of calls to constructors in such blocks
        double counter = 0.0;
        for (Integer branch_id : result.getTrace().getCoveredFalseBranches()){
            double value = noExecutionForConditionalNode.get(branch_id);
            if (value > 2)
               counter += value * branches.get(branch_id);
        }
        for (Integer branch_id : result.getTrace().getCoveredTrueBranches()){
            double value = noExecutionForConditionalNode.get(branch_id);
            if (value > 2)
                counter += value * branches.get(branch_id);
        }

        for (String branchlessMethod : result.getTrace().getCoveredBranchlessMethods()){
            if (methods.containsKey(branchlessMethod)) {
                int size = methods.get(branchlessMethod);
                int nExecutions = result.getTrace().getMethodExecutionCount().get(branchlessMethod);
                if (nExecutions > 2)
                    counter += size * nExecutions;
            }
        }

        test.setIndicatorValues(this.getIndicatorId(), counter);

        logger.debug("No. statements = " + counter);
        return counter;
    }

    public String getIndicatorId() {
        return INDICATOR;
    }
}
