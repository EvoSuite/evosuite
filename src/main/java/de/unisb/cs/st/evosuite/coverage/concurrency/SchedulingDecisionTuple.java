/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

/**
 * @author Sebastian Steenbuck
 * 
 */
public class SchedulingDecisionTuple {
	public final int threadID;
	/**
	 * The ID of the field
	 */
	public final int scheduleID;

	public SchedulingDecisionTuple(int threadID, int scheduleID) {
		this.threadID = threadID;
		this.scheduleID = scheduleID;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o instanceof SchedulingDecisionTuple) {
			SchedulingDecisionTuple t = (SchedulingDecisionTuple) o;
			if ((t.threadID == threadID) && (t.scheduleID == scheduleID)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	@Override
	public int hashCode() {
		Integer i = threadID + scheduleID;
		return i.hashCode();
	}

	@Override
	public String toString() {
		return "Thread: " + threadID + " scheduleID:" + scheduleID;
	}
}
