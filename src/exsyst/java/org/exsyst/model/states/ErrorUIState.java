package org.exsyst.model.states;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.evosuite.utils.TriBoolean;

import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.UIActionTargetDescriptor;
import org.exsyst.model.WindowDescriptor;
import org.exsyst.util.YWorksEnvironment;

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
	
	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {}
}
