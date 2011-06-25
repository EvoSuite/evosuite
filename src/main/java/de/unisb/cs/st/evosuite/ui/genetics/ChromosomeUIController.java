/**
 * 
 */
package de.unisb.cs.st.evosuite.ui.genetics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.InterfaceTestRunnable;
import de.unisb.cs.st.evosuite.ui.UIController;
import de.unisb.cs.st.evosuite.ui.UIRunner;
import de.unisb.cs.st.evosuite.ui.model.AbstractUIState;
import de.unisb.cs.st.evosuite.ui.model.DescriptorBoundUIAction;
import de.unisb.cs.st.evosuite.ui.model.IllegalUIStateException;
import de.unisb.cs.st.evosuite.utils.SimpleCondition;

public class ChromosomeUIController implements InterfaceTestRunnable, UIController {
	private Map<Integer, Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();
	private UITestChromosome chromosome;

	private int idx;
	private boolean runFinished;
	private ExecutionResult executionResult;
	private SimpleCondition simpleCondition;
	private Exception exception;
	private List<DescriptorBoundUIAction<?>> actions;

	public ChromosomeUIController(UITestChromosome chromosome) {
		this.chromosome = chromosome;
		this.actions = this.chromosome.getActionSequence().getActions();
		this.reset();
	}
	
	private void reset() {
		this.idx = -1;
		this.runFinished = false;
		this.executionResult = new ExecutionResult(new DefaultTestCase(), null); /* TODO... */
		this.simpleCondition = new SimpleCondition();
		this.exception = null;

		ExecutionTracer.getExecutionTracer().clear();
		// TODO: It would be much better if the current thread check in ExecutionTracer
		// correctly included threads spawned by the thread it is currently tracing...   
		ExecutionTracer.setCheckCallerThread(false);

		this.executionResult.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
	}
	
	@Override
	public synchronized ExecutionResult call() throws Exception {
		this.reset();
		
		UIRunner runner = UIRunner.run(this.chromosome.getStateGraph(), this, this.chromosome.getMainMethodTrigger());
		this.simpleCondition.awaitUninterruptibly();
		
		this.executionResult.setTrace(ExecutionTracer.getExecutionTracer().getTrace());

		this.chromosome.setActionSequence(runner.getActionSequence());
		
		if (this.exception != null) {
			throw this.exception;
		}
		
		return this.executionResult;
	}

	@Override
	public boolean isRunFinished() {
		return this.runFinished;
	}

	@Override
	public Map<Integer, Throwable> getExceptionsThrown() {
		return this.exceptionsThrown;
	}

	@Override
	public void processState(UIRunner uiRunner, AbstractUIState state) {
		if (this.exception != null || ++idx >= this.actions.size()) {
			return;
		}
		
		try {
			ExecutionTracer.statementExecuted();
			uiRunner.executeAction(state, this.actions.get(idx));
		} catch (IllegalUIStateException e) {
			// Swallow the exception if we were in an unknown state (doesn't really matter then, we were speculating anyway)
			if (uiRunner.getUnsharpActionSequence().getFinalState().isKnown()) {
				this.exception = e;
				this.idx = this.actions.size();
			}
		} catch (Exception e) {
			this.exception = e;
		} catch (Throwable t) {
			this.exception = new Exception(t);
		}
		
		if (this.exception != null) {
			this.exceptionsThrown.put(this.idx, this.exception);
		}
	}

	@Override
	public void finished(UIRunner uiRunner) {
		this.runFinished = true;
		this.simpleCondition.signal();
	}

	@Override
	public String toString() {
		return "ChromosomeUIController [actions=" + actions + "]";
	}
}