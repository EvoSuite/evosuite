package org.exsyst.run;

import org.exsyst.model.states.AbstractUIState;

public interface UIController {
	public void processState(UIRunner uiRunner, AbstractUIState state);

	public void finished(UIRunner uiRunner);
}
