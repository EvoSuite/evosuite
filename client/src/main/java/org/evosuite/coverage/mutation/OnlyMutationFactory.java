package org.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testsuite.AbstractFitnessFactory;

public class OnlyMutationFactory extends AbstractFitnessFactory<OnlyMutationTestFitness> {

	private List<OnlyMutationTestFitness> goals = null;

	@Override
	public List<OnlyMutationTestFitness> getCoverageGoals() {
		if (this.goals != null) {
			return this.goals;
		}

		this.goals = new ArrayList<OnlyMutationTestFitness>();

		for (Mutation m : MutationPool.getMutants()) {
			//if (MutationTimeoutStoppingCondition.isDisabled(m))
			//	continue;
			this.goals.add(new OnlyMutationTestFitness(m));
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Mutants, this.goals.size());

		return this.goals;
	}

}
