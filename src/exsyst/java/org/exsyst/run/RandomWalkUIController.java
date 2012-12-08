package org.exsyst.run;

import java.util.List;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTracer;
import org.slf4j.LoggerFactory;
import org.uispec4j.UIComponent;

import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.states.AbstractUIState;

public class RandomWalkUIController implements UIController {
	private int statesSeen = 0;
	private int targetLength;
	private ExecutionResult executionResult; 
	
	public RandomWalkUIController(int wantedLength) {
		this.targetLength = wantedLength;
		this.reset();
		
		ExecutionTracer.enable();
		ExecutionTracer.setThread(Thread.currentThread());
	}
	
	private void reset() {
		this.executionResult = new ExecutionResult(new DefaultTestCase(), null); /* TODO... */

		ExecutionTracer.getExecutionTracer().clear();
		
		// TODO: It would be much better if the current thread check in ExecutionTracer
		// correctly included threads spawned by the thread it is currently tracing...   
		ExecutionTracer.setCheckCallerThread(false);

		this.executionResult.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
	}
	
	@Override
	public void processState(UIRunner uiRunner, AbstractUIState state) {
		if (statesSeen++ > this.targetLength) {
			return;
		}

		/*List<UIActionTargetDescriptor> actionTargets = ListUtil.shuffledList(state.getActionTargetDescriptors());

		for (UIActionTargetDescriptor atd : actionTargets) {
			UIComponent at = uiRunner.resolve(atd);
			
			List<DescriptorBoundUIAction<? extends UIComponent>> actions = ListUtil.shuffledList(atd.getActions());

			if (at != null && actions.size() > 0) {
				try {
					uiRunner.executeAction(state, actions.get(0));
					return;
				} catch (Throwable t) {
					System.err.println("Error in random walk: ");
					t.printStackTrace();
				}
			}
		}*/
		
		List<DescriptorBoundUIAction<? extends UIComponent>> actions =
				state.allActionsShuffledUnexploredFirst();
		
		for (DescriptorBoundUIAction<? extends UIComponent> action : actions) {
			if (action != null) {
				try {
					uiRunner.executeAction(state, action);
					return;
				} catch (Throwable t) {
					System.err.println("Error in random walk: ");
					t.printStackTrace();
				}
			}
		}
		
		LoggerFactory.getLogger(this.getClass()).warn("End of processState() reached without found action");
	}

	@Override
	public void finished(UIRunner uiRunner) {
		ExecutionTracer.disable();
		this.executionResult.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
	}

	public ExecutionResult getExecutionResult() {
		return this.executionResult;
	}
}
