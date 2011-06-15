/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

/**
 * @author x3k6a2
 * 
 */
public interface Schedule extends Iterable<Integer> {

	public void add(int index, Integer element);

	/**
	 * An unmodifiable iterable from the list below. Can be used for outputing
	 * the schedule.
	 * 
	 * @return
	 */
	public Iterable<Integer> getContentIterable();

	/**
	 * Needed for nice (one less boolean condition) startup
	 * 
	 * @return
	 */
	public int getFirstElement();

	/**
	 * Returns a random threadID from the set of threadIDs, which were at some
	 * point added to this schedule
	 * 
	 * @return
	 */
	public int getRandomThreadID();

	/**
	 * called by the schedule if a schedule point is used
	 * 
	 * @param threadID
	 *            only supplied for internal error checking
	 */
	public void notifyOfUsedSchedule(int threadID);

	/**
	 * Like list remove(int)
	 * 
	 * @param index
	 */
	public void removeElement(int index);

	/**
	 * The schedule needs some way to access the currently known threadIDs.
	 * 
	 * We could also just generate new IDs, and remove them (from the
	 * controller) until we get a valid one. -pro: cleaner interface, no
	 * circular dependency -contra: calling the random function multiple times,
	 * while we have all information todo it only once
	 * 
	 * @param count
	 */
	public void setController(ControllerRuntime c);

	/**
	 * Like list size()
	 * 
	 * @return
	 */
	public int size();
}
