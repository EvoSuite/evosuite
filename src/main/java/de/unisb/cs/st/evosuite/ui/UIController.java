package de.unisb.cs.st.evosuite.ui;

import de.unisb.cs.st.evosuite.ui.model.AbstractUIState;

public interface UIController {
	public void processState(UIRunner uiRunner, AbstractUIState state);

	public void finished(UIRunner uiRunner);
}
