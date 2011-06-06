/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

/**
 * @author x3k6a2
 *
 */
public interface Schedule extends Iterable<Integer>{
	
	/**
	 * The schedule needs some way to access the currently known threadIDs.
	 * 
	 * We could also just generate new IDs, and remove them (from the controller) until we get a valid one. 
	 *  -pro: cleaner interface, no circular dependency
	 *  -contra: calling the random function multiple times, while we have all information todo it only once
	 * @param count
	 */
	public void setController(ControllerRuntime c);

	/**
	 * Needed for nice (one less boolean condition) startup
	 * @return
	 */
	public int getFirstElement();

	/**
	 * Like list remove(int)
	 * @param index
	 */
	public void removeElement(int index);
	
	/**
	 * Like list size()
	 * @return
	 */
	public int size();
	
	/**
	 * Returns a random threadID from the set of threadIDs, which were at some point added to this schedule
	 * @return
	 */
	public int getRandomThreadID();
	
	public void add(int index, Integer element);
	
	/**
	 * An unmodifiable iterable from the list below.
	 * Can be used for outputing the schedule.
	 * @return
	 */
	public Iterable<Integer> getContentIterable();
	
	/**
	 * called by the schedule if a schedule point is used
	 * @param threadID only supplied for internal error checking
	 */
	public void notifyOfUsedSchedule(int threadID);
}
