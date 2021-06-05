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
 * @author Giovanni Grano
 * <p>
 * This class implements a dynamic performance indicator. It measures the number of
 * objects instatiated by a test case.
 * Need the Criterion.DEFUSE set in <code>Properties</code>
 * <p>
 * todo: actually it uses the getDefCounter statit method for the DefUsePool class
 */
public class ObjectInstantiations extends AbstractIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ObjectInstantiations.class);
    private static final String INDICATOR = ObjectInstantiations.class.getName();

    /**
     * To keep track of the branches in the CUT (class under test)
     */
    private static HashMap<Integer, Integer> branches;
    private static HashMap<String, Integer> methods;

    public ObjectInstantiations() {
        if (branches == null) {
            branches = new HashMap<>();
            for (Branch b : BranchPool
                    .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
                    .getAllBranches()) {
                int nObjects = 0;
                BasicBlock block = b.getInstruction().getBasicBlock();
                for (BytecodeInstruction instr : block) {
                    if (instr.isConstructorInvocation() ||
                            instr.isLocalArrayDefinition() ||
                            instr.isWithinConstructor()) {
                        nObjects++;
                    }
                }
                if (nObjects > 0)
                    branches.put(b.getActualBranchId(), nObjects);
            }
            methods = new HashMap<>();
            List<BytecodeInstruction> list = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllInstructions();
            for (BytecodeInstruction instr : list) {
                Integer set = methods.get(instr.getMethodName());
                if (instr.isConstructorInvocation() ||
                        instr.isLocalArrayDefinition() ||
                        instr.isWithinConstructor()) {
                    String fullyQualifiedName = instr.getClassName() + "." + instr.getMethodName();
                    if (set == null) {
                        methods.put(fullyQualifiedName, 1);
                    } else {
                        methods.put(fullyQualifiedName, set + 1);
                    }
                }
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
        double counter = 0;
        for (Integer branch_id : result.getTrace().getCoveredFalseBranches()) {
            double value = noExecutionForConditionalNode.get(branch_id);
            Integer number = branches.get(branch_id);
            if (number != null && value >= 2)
                counter += value * branches.get(branch_id);
        }
        for (Integer branch_id : result.getTrace().getCoveredTrueBranches()) {
            double value = noExecutionForConditionalNode.get(branch_id);
            Integer number = branches.get(branch_id);
            if (number != null && value >= 2)
                counter += value * branches.get(branch_id);
        }

        for (String branchlessMethod : result.getTrace().getCoveredBranchlessMethods()) {
            if (methods.containsKey(branchlessMethod)) {
                int nObjs = methods.get(branchlessMethod);
                int nExecutions = result.getTrace().getMethodExecutionCount().get(branchlessMethod);
                if (nExecutions >= 2)
                    counter += nObjs * nExecutions;
            }
        }

        // 2. We count the number of objects instantiated withing the test code
        //    Thus, we count the number of Strings and calls to constructors
        //for (Statement stmt : chromosome.getTestCase()){
        //    if (stmt instanceof ConstructorStatement || stmt instanceof StringPrimitiveStatement || stmt instanceof ArrayStatement)
        //        counter++;
        //}

        test.setIndicatorValues(this.getIndicatorId(), counter);
        logger.info("No. definitions = " + counter);
        return counter;
    }

    @Override
    public String getIndicatorId() {
        return INDICATOR;
    }
}
