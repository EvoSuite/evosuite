package org.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.AbstractFitnessFactory;

public class OnlyMutationFactory extends AbstractFitnessFactory<OnlyMutationTestFitness> {

	@Override
	public List<OnlyMutationTestFitness> getCoverageGoals() {
		List<OnlyMutationTestFitness> goals = new ArrayList<OnlyMutationTestFitness>();

		for (Mutation m : MutationPool.getMutants()) {
			if (MutationTimeoutStoppingCondition.isDisabled(m))
				continue;
			goals.add(new OnlyMutationTestFitness(m));
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Mutants, goals.size());

		return goals;
	}

}
