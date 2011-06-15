/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A list of scheduling decisions. Used to make generating points easier. We can
 * just generate all of the lists and let the equals operation of this class
 * sort the mess Scheduling lists are considered equal if one list can be
 * transformed into the other by substitution of the thread IDs
 * 
 * @author Sebastian Steenbuck
 * 
 */
public class SchedulingDecisionList extends ArrayList<SchedulingDecisionTuple> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8039718645507847201L;

	@Override
	public SchedulingDecisionList clone() {
		SchedulingDecisionList clone = new SchedulingDecisionList();
		clone.addAll(this);
		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof SchedulingDecisionList) {
				SchedulingDecisionList other = (SchedulingDecisionList) o;
				if (other.size() == this.size()) {
					Map<Integer, Integer> renaming = new HashMap<Integer, Integer>(other.size());
					for (int i = 0; i < this.size(); i++) {
						final SchedulingDecisionTuple ownTuple = this.get(i);
						final SchedulingDecisionTuple otherTuple = other.get(i);
						if (ownTuple.scheduleID == otherTuple.scheduleID) {
							if (!renaming.containsKey(otherTuple.threadID)) {
								renaming.put(otherTuple.threadID, ownTuple.threadID);
							}

							if (ownTuple.threadID != renaming.get(otherTuple.threadID)) {
								return false;
							}
						} else {
							return false;
						}
					}
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (SchedulingDecisionTuple t : this) {
			hashCode += t.hashCode();
		}
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SchedulingDecisionTuple t : this) {
			builder.append(t);
			builder.append(" XX ");
		}
		return builder.toString();
	}
}
