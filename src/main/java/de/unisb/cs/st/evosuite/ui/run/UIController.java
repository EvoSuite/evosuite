package de.unisb.cs.st.evosuite.ui.run;

import de.unisb.cs.st.evosuite.ui.model.states.AbstractUIState;

public interface UIController {
	public void processState(UIRunner uiRunner, AbstractUIState state);

	public void finished(UIRunner uiRunner);
}
