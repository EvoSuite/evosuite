package de.unisb.cs.st.evosuite.ma;

import java.io.File;


public interface UserFeedback {
	File chooseTargetFile(String className);

	void showParseException(String message);
}
