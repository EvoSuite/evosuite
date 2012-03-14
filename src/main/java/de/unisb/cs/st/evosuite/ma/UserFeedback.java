package de.unisb.cs.st.evosuite.ma;

import java.io.File;


public interface UserFeedback {
	
	/**
	 * Creates a dialog to choose right class file. 
	 * @param className {@code String} - name of the class to load
	 * @return loaded {@link File}
	 */
	File chooseTargetFile(String className);

	/**
	 * Used by the parser to show errors.
	 * @param message {@code String}
	 */
	void showParseException(String message);
}
