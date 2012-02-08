package de.unisb.cs.st.evosuite.ui.genetics;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.uispec4j.Trigger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.InterfaceTestRunnable;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TimeoutHandler;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;
import de.unisb.cs.st.evosuite.ui.model.states.UIStateGraph;
import de.unisb.cs.st.evosuite.utils.Randomness;

public class UITestChromosome extends ExecutableChromosome {
	private static final long serialVersionUID = 1L;

	// If only there was a LinkedIdentityHashSet...
	static final Set<UITestChromosome> executedChromosomes = Collections.newSetFromMap(new IdentityHashMap<UITestChromosome, Boolean>());
	static final List<UITestChromosome> executedChromosomeList = new LinkedList<UITestChromosome>();

	static final Set<UITestChromosome> failingChromosomes = Collections.newSetFromMap(new IdentityHashMap<UITestChromosome, Boolean>());
	static final List<UITestChromosome> failingChromosomeList = new LinkedList<UITestChromosome>();

	public static Collection<UITestChromosome> getExecutedChromosomes() {
		return executedChromosomeList;
	}

	public static Collection<UITestChromosome> getFailingChromosomes() {
		return failingChromosomeList;
	}

	private ActionSequence actionSequence;

	private final UIStateGraph stateGraph;

	private final Trigger mainMethodTrigger;

	public UITestChromosome(ActionSequence actionSequence, UIStateGraph stateGraph,
	        Trigger mainMethodTrigger) {
		assert (actionSequence != null);

		this.actionSequence = actionSequence;
		this.stateGraph = stateGraph;
		this.mainMethodTrigger = mainMethodTrigger;
	}

	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		throw new UnsupportedOperationException(
		        "UITestChromosome doesn't support cross-over at arbitrary positions");
	}

	public void replaceWithCrossOverResult(UITestChromosome partner) {
		ActionSequence picked = Randomness.choice(this.actionSequence.crossOverPoints(partner.actionSequence));
		this.actionSequence = picked;
	}

	@Override
	public void mutate() {
		double p = 1.0 / 3.0;
		boolean changed = false;

		// Delete
		if (Randomness.nextDouble() <= p) {
			changed |= mutationDelete();
		}

		// Change
		if (Randomness.nextDouble() <= p) {
			changed |= mutationChange();
		}

		// Insert
		if (Randomness.nextDouble() <= p) {
			changed |= mutationInsert();
		}

		if (changed) {
			this.actionSequence.repair();
			this.setChanged(true);
			this.clearCachedResults();
		}
	}

	/**
	 * Each action is mutated with probability 1 / size
	 * 
	 * @return true if anything was actually changed
	 */
	private boolean mutationChange() {
		boolean changed = false;

		if (this.size() > 0) {
			double p = 1 / this.size();

			for (int i = 0; i < this.actionSequence.size(); i++) {
				if (Randomness.nextDouble() <= p) {
					changed = this.actionSequence.changeUnsafe(i);
				}
			}
		}

		return changed;
	}

	/**
	 * Each action is deleted with probability 1 / size
	 * 
	 * @return true if anything was actually changed
	 */
	private boolean mutationDelete() {
		boolean changed = false;

		if (this.size() > 0) {
			double p = 1 / this.size();

			for (int i = 0; i < this.actionSequence.size(); i++) {
				if (Randomness.nextDouble() <= p) {
					this.actionSequence.removeUnsafe(i);
					changed = true;
				}
			}
		}

		return changed;
	}

	/**
	 * With exponentially decreasing probability, keep inserting statements at
	 * random positions.
	 * 
	 * @return true if anything was actually changed
	 */
	private boolean mutationInsert() {
		boolean changed = false;
		double p = 1.0;

		while (Randomness.nextDouble() <= p
		        && (!Properties.CHECK_MAX_LENGTH || size() < Properties.CHROMOSOME_LENGTH)) {
			changed |= this.actionSequence.insertRandomActionUnsafe();
			p /= 2;
		}

		return changed;
	}

	@Override
	public Chromosome clone() {
		return new UITestChromosome((ActionSequence) this.actionSequence.clone(),
		        this.stateGraph, this.getMainMethodTrigger());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof UITestChromosome)) {
			return false;
		}

		UITestChromosome other = (UITestChromosome) obj;
		return this.actionSequence.equals(other.actionSequence);
	}

	@Override
	public int hashCode() {
		return this.actionSequence.hashCode();
	}

	@Override
	public int size() {
		return this.actionSequence.size();
	}

	@Override
	public void localSearch(LocalSearchObjective objective) {
		throw new UnsupportedOperationException(
		        "UITestChromosome doesn't support localSearch() (yet?)");
	}

	@Override
	public void applyDSE(GeneticAlgorithm ga) {
		throw new UnsupportedOperationException(
		        "UITestChromosome doesn't support applyDSE() (yet?)");
	}

	private static final ExecutorService executor = Executors.newSingleThreadExecutor(TestCaseExecutor.getInstance());

	@Override
	public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction) {
		TimeoutHandler<ExecutionResult> handler = new TimeoutHandler<ExecutionResult>();
		InterfaceTestRunnable callable = new ChromosomeUIController(this);

		executedChromosomes.add(this);
		executedChromosomeList.add(this);

		try {
			ExecutionResult result = handler.execute(callable, executor,
			                                         Properties.TIMEOUT,
			                                         Properties.CPU_TIMEOUT);
			return result;
		} catch (Exception e) {
			failingChromosomes.add(this);
			failingChromosomeList.add(this);
			System.out.println("Exception on executing test chromosome for fitness function:");
			e.printStackTrace();
			return null;
		}
	}

	public ActionSequence getActionSequence() {
		return this.actionSequence;
	}

	void setActionSequence(ActionSequence actionSequence) {
		this.actionSequence = actionSequence;
	}

	public UIStateGraph getStateGraph() {
		return this.stateGraph;
	}

	public Trigger getMainMethodTrigger() {
		return this.mainMethodTrigger;
	}

	@Override
	public String toString() {
		return "UITestChromosome [actionSequence=" + actionSequence + "]";
	}

	public void repair() {
		this.actionSequence.repair();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutableChromosome#copyCachedResults(de.unisb.cs.st.evosuite.testcase.ExecutableChromosome)
	 */
	@Override
	protected void copyCachedResults(ExecutableChromosome other) {
		this.lastExecutionResult = other.getLastExecutionResult();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#compareSecondaryObjective(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
