/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import java.util.Set;

/**
 * @author fraser
 * 
 */
public interface OutputTraceEntry {

	public boolean differs(OutputTraceEntry other);

	public Set<Assertion> getAssertions(OutputTraceEntry other);

	public Set<Assertion> getAssertions();

	public boolean isDetectedBy(Assertion assertion);

	public OutputTraceEntry cloneEntry();

}
