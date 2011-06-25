package de.unisb.cs.st.evosuite.ui.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.utils.TriBoolean;

public class ErrorUIState extends AbstractUIState {
	private static final long serialVersionUID = 1L;

	@Override
	public void addTransition(DescriptorBoundUIAction<?> action, UIState toState) {
		/* Nothing to do */
	}

	@Override
	public AbstractUIState getTransition(DescriptorBoundUIAction<?> action) {
		return this;
	}

	@Override
	public List<WindowDescriptor> getTargetableWindowDescriptors() {
		return Collections.emptyList();
	}

	@Override
	public Map<DescriptorBoundUIAction<?>, UIState> getTransitions() {
		return Collections.emptyMap();
	}


	@Override
	public List<UIActionTargetDescriptor> getActionTargetDescriptors() {
		return Collections.emptyList();
	}

	@Override
	public TriBoolean canExecuteActionSequence(List<DescriptorBoundUIAction<?>> actionSequence) {
		return actionSequence.isEmpty() ? TriBoolean.True : TriBoolean.False;
	}
}
