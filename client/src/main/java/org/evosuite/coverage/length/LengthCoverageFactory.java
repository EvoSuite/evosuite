package org.evosuite.coverage.length;

import org.evosuite.Properties;
import org.evosuite.testsuite.AbstractFitnessFactory;

import java.util.ArrayList;
import java.util.List;

public class LengthCoverageFactory  extends AbstractFitnessFactory<LengthTestFitness> {

    @Override
    public List<LengthTestFitness> getCoverageGoals() {
        String className = Properties.TARGET_CLASS;
        List<LengthTestFitness> goals = new ArrayList<>();
        goals.add(new LengthTestFitness(className));
        return goals;
    }
}
