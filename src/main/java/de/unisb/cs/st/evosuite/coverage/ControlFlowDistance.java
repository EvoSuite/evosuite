/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage;

/**
 * @author Gordon Fraser
 * 
 */
public class ControlFlowDistance implements Comparable<ControlFlowDistance> {
	public int approach = 0;
	public double branch = 0.0;

	@Override
	public int compareTo(ControlFlowDistance o) {
		ControlFlowDistance d = (ControlFlowDistance) o;
		if (approach < d.approach)
			return -1;
		else if (approach > d.approach)
			return 1;
		else {
			if (branch < d.branch)
				return -1;
			else if (branch > d.branch)
				return 1;
			else
				return 0;
		}
	}

}
