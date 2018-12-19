package org.evosuite.performance.indicator;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.line.LineCoverageFactory;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.graphs.cfg.BasicBlock;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.performance.AbstractIndicator;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author giograno
 *
 * This class implements a dynamic performance indicator. It measures the number of
 * objects instatiated by a test case.
 * Need the Criterion.DEFUSE set in <code>Properties</code>
 *
 * todo: STILL TO CHECK!
 * todo: actually it uses the getDefCounter statit method for the DefUsePool class
 */
public class ObjectInstantiations extends AbstractIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ObjectInstantiations.class);
    private static String INDICATOR = ObjectInstantiations.class.getName();

    /** To keep track of the branches in the CUT (class under test) */
    private static HashMap<Integer,Branch> branches;

    private static HashMap<String, Set<BytecodeInstruction>> methods;

    public ObjectInstantiations(){
        super();
        if (branches == null) {
            branches = new HashMap();
            for (Branch b : BranchPool
                    .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
                    .getAllBranches()) {
                branches.put(b.getActualBranchId(), b);
            }
            methods = new HashMap();
            for (LineCoverageTestFitness line : new LineCoverageFactory().getCoverageGoals()){
                BytecodeInstruction instr = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getFirstInstructionAtLineNumber(line.getClassName(), line.getMethod(), line.getLine());
                String methodName = instr.getClassName()+"."+instr.getMethodName();
                Set<BytecodeInstruction> set = methods.get(methodName);
                if (set == null){
                    set = new HashSet<>();
                    set.add(instr);
                    methods.put(methodName, set);
                } else {
                    set.add(instr);
                }
            }
        }
    }

    @Override
    public double getIndicatorValue(Chromosome test) {
        if (test instanceof TestSuiteChromosome)
            throw new IllegalArgumentException("This indicator works at test case level");

        if (test.getIndicatorValues().keySet().contains(INDICATOR))
            return test.getIndicatorValue(INDICATOR);

        // retrieve the last execution
        TestChromosome chromosome = (TestChromosome) test;
        ExecutionResult result = chromosome.getLastExecutionResult();

        Map<Integer, Integer> noExecutionForConditionalNode =
                result.getTrace().getNoExecutionForConditionalNode();

        // 1. We look at the objects instantiated in the covered production code
        //    First, we select the basic blocks associated with covered branches
        //    Then, we count the number of calls to constructors in such blocks
        double counter = 0;
        for (Integer branch_id : result.getTrace().getCoveredFalseBranches()){
            Branch branch = this.branches.get(branch_id);
            BasicBlock block = branch.getInstruction().getBasicBlock();
            for (BytecodeInstruction instr : block) {
                if (instr.isConstructorInvocation() || instr.isLocalArrayDefinition()) {
                    double value = noExecutionForConditionalNode.get(branch.getActualBranchId());
                    if (value > 1)
                        counter += value;
                }
            }
        }
        for (Integer branch_id : result.getTrace().getCoveredTrueBranches()){
            Branch branch = this.branches.get(branch_id);
            BasicBlock block = branch.getInstruction().getBasicBlock();
            for (BytecodeInstruction instr : block) {
                if (instr.isConstructorInvocation() || instr.isLocalArrayDefinition()) {
                    double value = noExecutionForConditionalNode.get(branch.getActualBranchId());
                    if (value > 1)
                        counter += value;
                }
            }
        }

        for (String branchlessMethod : result.getTrace().getCoveredBranchlessMethods()){
            if (methods.keySet().contains(branchlessMethod)) {
                for (BytecodeInstruction instr : methods.get(branchlessMethod)) {
                    if (instr.isConstructorInvocation() || instr.isLocalArrayDefinition()) {
                        double value = result.getTrace().getMethodExecutionCount().get(branchlessMethod);
                        if (value > 1)
                            counter += value;
                    }
                }
            }
        }

        // 2. We count the number of objects instantiated withing the test code
        //    Thus, we count the number of Strings and calls to constructors
        //for (Statement stmt : chromosome.getTestCase()){
        //    if (stmt instanceof ConstructorStatement || stmt instanceof StringPrimitiveStatement || stmt instanceof ArrayStatement)
        //        counter++;
        //}

        test.setIndicatorValues(this.getIndicatorId(), normalize(counter));
        logger.info("No. definitions = " + counter);
        return counter;
    }

    @Override
    public String getIndicatorId() {
        return INDICATOR;
    }
}
