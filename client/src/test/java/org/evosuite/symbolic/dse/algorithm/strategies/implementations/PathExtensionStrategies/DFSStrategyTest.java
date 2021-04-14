package org.evosuite.symbolic.dse.algorithm.strategies.implementations.PathExtensionStrategies;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.dse.algorithm.GenerationalSearchPathCondition;
import org.evosuite.symbolic.dse.algorithm.strategies.PathExtensionStrategy;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.vm.PathConditionCollector;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DFSStrategyTest {

    @Test
    public void generateChildren() {
        GenerationalSearchPathCondition generationalPathCondition = generatePathCondition1();

        PathCondition pathCondition = generationalPathCondition.getPathCondition();
        PathExtensionStrategy dfsStrategy = new DFSStrategy();

        List<GenerationalSearchPathCondition> children = dfsStrategy.generateChildren(generationalPathCondition);

        // One expansion per branch
        assertEquals(pathCondition.getBranchConditions().size(), children.size());

        // Children composition
        int expectedSize = children.size();
        for(GenerationalSearchPathCondition gpc : children) {
            PathCondition childPathCondition = gpc.getPathCondition();

            // from largest to smallest
            assertEquals(expectedSize, childPathCondition.size());

            // Current size determines the last branch index
            int lastBranchIndex = expectedSize  - 1;

            // Last branch must be negated
            isNegatedVersion(pathCondition, childPathCondition, lastBranchIndex);

            expectedSize--;
        }
    }

    private void isNegatedVersion(PathCondition pathCondition, PathCondition childPathCondition, int expectedSize) {
        BranchCondition childBranch = childPathCondition.get(expectedSize);
        BranchCondition originalBranch = pathCondition.get(expectedSize);

        assertEquals(originalBranch.getConstraint(), childBranch.getNegatedVersion().getConstraint());
    }

    private GenerationalSearchPathCondition generatePathCondition1() {
        PathConditionCollector pcc = new PathConditionCollector();

        IntegerConstraint intConst1 = new IntegerConstraint(new IntegerConstant(2), Comparator.EQ, new IntegerConstant(3));
        IntegerConstraint intConst2 = new IntegerConstraint(new IntegerConstant(5), Comparator.NE, new IntegerConstant(1));
        IntegerConstraint intConst3 = new IntegerConstraint(new IntegerConstant(18), Comparator.LT, new IntegerConstant(3));

        pcc.appendIfBranchCondition("test", "test", 1, true, intConst1);
        pcc.appendIfBranchCondition("test", "test", 1, true, intConst2);
        pcc.appendIfBranchCondition("test", "test", 1, true, intConst3);

        PathCondition result = new PathCondition(pcc.getPathCondition());
        return new GenerationalSearchPathCondition(result, 0);
    }
}