package org.exsyst.genetics;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.InterfaceTestRunnable;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TimeoutHandler;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.uispec4j.Trigger;

import org.exsyst.model.states.UIStateGraph;

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
					changed |= this.actionSequence.changeUnsafe(i);
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

		addToExecutedChromosomes(this);

		try {
			ExecutionResult result = handler.execute(callable, executor,
			                                         Properties.TIMEOUT,
			                                         Properties.CPU_TIMEOUT);
			return result;
		} catch (Exception e) {
			addToFailingChromosomes(this);
			System.out.println("Exception on executing test chromosome for fitness function:");
			e.printStackTrace();
			return null;
		}
	}

	private static UITestChromosome distill(UITestChromosome chromosome) {
		UITestChromosome result = (UITestChromosome) chromosome.clone();
		
		result.lastExecutionResult = null;
		result.lastMutationResult = null;
		
		return result;
	}

	static void addToFailingChromosomes(UITestChromosome chromosome) {
		UITestChromosome distilled = distill(chromosome);
		
		failingChromosomes.add(distilled);
		failingChromosomeList.add(distilled);
	}

	static void addToExecutedChromosomes(UITestChromosome chromosome) {
		UITestChromosome distilled = distill(chromosome);

		executedChromosomes.add(distilled);
		executedChromosomeList.add(distilled);
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
