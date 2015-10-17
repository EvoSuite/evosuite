package org.evosuite.idNaming.qualifiers;

import org.evosuite.coverage.TestCoverageGoal;

/**
 * Created by jmr on 06/08/15.
 */
public class GoalQualifier implements TestNameQualifier {

    private TestCoverageGoal goal = null;
    private String preposition = "Covers";

    public GoalQualifier(TestCoverageGoal goal) {
        this.goal = goal;
    }

    public GoalQualifier(TestCoverageGoal goal, String preposition) {
        this.goal = goal;
        this.preposition = preposition;
    }

    @Override
    public String toString() {
        return preposition + goal.toString();
    }
}
