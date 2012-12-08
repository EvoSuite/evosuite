package org.exsyst.genetics;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.SimpleCondition;
import org.uispec4j.Trigger;

import org.exsyst.model.states.UIStateGraph;
import org.exsyst.run.RandomWalkUIController;
import org.exsyst.run.UIRunner;

public class UITestChromosomeFactory implements ChromosomeFactory<UITestChromosome> {
	private static final long serialVersionUID = 1L;
	private UIStateGraph stateGraph;
	private Trigger mainMethodTrigger;
	private int length;

	public UITestChromosomeFactory(UIStateGraph stateGraph, Trigger mainMethodTrigger, int length) {
		this.stateGraph = stateGraph;
		this.mainMethodTrigger = mainMethodTrigger;
		this.length = length;
	}

	public UITestChromosomeFactory(UIStateGraph stateGraph, Trigger mainMethodTrigger) {
		this(stateGraph, mainMethodTrigger, Properties.CHROMOSOME_LENGTH);
	}

	@Override
	public synchronized UITestChromosome getChromosome() {
		final SimpleCondition cond = new SimpleCondition();
		
		try {
			RandomWalkUIController controller = new RandomWalkUIController(Randomness.nextInt(this.length - 1) + 1) {
				@Override
				public void finished(UIRunner uiRunner) {
					super.finished(uiRunner);
					cond.signal();
				}
			};
			
			UIRunner uiRunner = UIRunner.run(this.stateGraph, controller, this.mainMethodTrigger);

			cond.awaitUninterruptibly();
			
			UITestChromosome result = new UITestChromosome(uiRunner.getActionSequence(), this.stateGraph, this.mainMethodTrigger); 
			result.setLastExecutionResult(controller.getExecutionResult());
			result.setChanged(false);
			
			UITestChromosome.addToExecutedChromosomes(result);

			return result;
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(-1);
			return null;
			//return getChromosome();
		}
	}
}
