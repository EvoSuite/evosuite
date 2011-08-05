package de.unisb.cs.st.evosuite.ui.run;

import java.util.List;

import org.uispec4j.UIComponent;

import de.unisb.cs.st.evosuite.ui.model.DescriptorBoundUIAction;
import de.unisb.cs.st.evosuite.ui.model.UIActionTargetDescriptor;
import de.unisb.cs.st.evosuite.ui.model.states.AbstractUIState;
import de.unisb.cs.st.evosuite.utils.ListUtil;

public class RandomWalkUIController implements UIController {
	private int statesSeen = 0;
	private int targetLength; 
	
	public RandomWalkUIController(int wantedLength) {
		this.targetLength = wantedLength;
	}
	
	@Override
	public void processState(UIRunner uiRunner, AbstractUIState state) {
		if (statesSeen++ > this.targetLength) {
			return;
		}

		List<UIActionTargetDescriptor> actionTargets = ListUtil.shuffledList(state.getActionTargetDescriptors());

		for (UIActionTargetDescriptor atd : actionTargets) {
			UIComponent at = uiRunner.resolve(atd);
			List<DescriptorBoundUIAction<? extends UIComponent>> actions = ListUtil.shuffledList(atd.getActions());

			if (at != null && actions.size() > 0) {
				uiRunner.executeAction(state, actions.get(0));
				return;
			}
		}
	}

	@Override
	public void finished(UIRunner uiRunner) { }
}
