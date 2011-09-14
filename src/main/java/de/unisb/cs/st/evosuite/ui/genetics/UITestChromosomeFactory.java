package de.unisb.cs.st.evosuite.ui.genetics;

import org.uispec4j.Trigger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ui.model.states.UIStateGraph;
import de.unisb.cs.st.evosuite.ui.run.RandomWalkUIController;
import de.unisb.cs.st.evosuite.ui.run.UIRunner;
import de.unisb.cs.st.evosuite.utils.Randomness;
import de.unisb.cs.st.evosuite.utils.SimpleCondition;

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
			UIRunner uiRunner = UIRunner.run(this.stateGraph, new RandomWalkUIController(Randomness.nextInt(this.length - 1) + 1) {
				@Override
				public void finished(UIRunner uiRunner) {
					super.finished(uiRunner);
					cond.signal();
				}
			}, this.mainMethodTrigger);

			cond.awaitUninterruptibly();
			return new UITestChromosome(uiRunner.getActionSequence(), this.stateGraph, this.mainMethodTrigger);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(-1);
			return null;
			//return getChromosome();
		}
	}
}
