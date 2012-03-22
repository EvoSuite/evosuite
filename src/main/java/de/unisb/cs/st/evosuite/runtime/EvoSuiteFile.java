/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

/**
 * @author fraser
 * 
 */
public class EvoSuiteFile {

	private final String path;

	public EvoSuiteFile(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return path;
	}
}
