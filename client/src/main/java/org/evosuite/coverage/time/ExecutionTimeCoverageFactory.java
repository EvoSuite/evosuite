package org.evosuite.coverage.time;

import org.evosuite.Properties;
import org.evosuite.testsuite.AbstractFitnessFactory;

import java.util.ArrayList;
import java.util.List;

public class ExecutionTimeCoverageFactory extends AbstractFitnessFactory<ExecutionTimeTestFitness> {

    @Override
    public List<ExecutionTimeTestFitness> getCoverageGoals() {
        String className = Properties.TARGET_CLASS;
        List<ExecutionTimeTestFitness> goals = new ArrayList<>();
        goals.add(new ExecutionTimeTestFitness(className));
        return goals;
    }
}
