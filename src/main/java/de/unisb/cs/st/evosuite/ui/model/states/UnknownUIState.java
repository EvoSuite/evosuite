package de.unisb.cs.st.evosuite.ui.model.states;

import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.ui.YWorksEnvironment;
import de.unisb.cs.st.evosuite.ui.model.DescriptorBoundUIAction;
import de.unisb.cs.st.evosuite.ui.model.UIActionTargetDescriptor;
import de.unisb.cs.st.evosuite.ui.model.WindowDescriptor;
import de.unisb.cs.st.evosuite.utils.TriBoolean;

public class UnknownUIState extends AbstractUIState {
	private static final long serialVersionUID = 1L;

	private AbstractUIState lastKnownUIState;

	public UnknownUIState(AbstractUIState uiState) {
		this.lastKnownUIState = uiState;
	}

	@Override
	public void addTransition(DescriptorBoundUIAction<?> action, UIState toState) {
		/* Nothing to do */
	}

	@Override
	public AbstractUIState getTransition(DescriptorBoundUIAction<?> action) {
		if (this.lastKnownUIState.getTransitions().containsKey(action)) {
			return new UnknownUIState(this.lastKnownUIState.getTransition(action));
		}
		
		return this;
	}

	@Override
	public List<WindowDescriptor> getTargetableWindowDescriptors() {
		return this.lastKnownUIState.getTargetableWindowDescriptors();
	}
	
	@Override
	public Map<DescriptorBoundUIAction<?>, ? extends AbstractUIState> getTransitions() {
		return this.lastKnownUIState.getTransitions();
	}

	@Override
	public List<UIActionTargetDescriptor> getActionTargetDescriptors() {
		return this.lastKnownUIState.getActionTargetDescriptors();
	}

	@Override
	public TriBoolean canExecuteActionSequence(List<DescriptorBoundUIAction<?>> actionSequence) {
		return actionSequence.isEmpty() ? TriBoolean.True : TriBoolean.Maybe;
	}
	
	@Override
	public boolean isUnknown() {
		return true;
	}
	
	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {}
}
