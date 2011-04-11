/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author x3k6a2
 *
 */
public class SimpleScheduler implements Schedule {

	public final List<Integer> schedule; 
	public SimpleScheduler(Integer[] s){
		this.schedule = new ArrayList<Integer>(s.length);
		for(Integer i : s){
			schedule.add(i);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#add(int, java.lang.Integer)
	 */
	@Override
	public void add(int index, Integer element) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#getContentIterable()
	 */
	@Override
	public Iterable<Integer> getContentIterable() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#getFirstElement()
	 */
	@Override
	public int getFirstElement() {
		return schedule.get(0);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#getRandomThreadID()
	 */
	@Override
	public int getRandomThreadID() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#removeElement(int)
	 */
	@Override
	public void removeElement(int index) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#setController(de.unisb.cs.st.evosuite.coverage.concurrency.ControllerRuntime)
	 */
	@Override
	public void setController(ControllerRuntime c) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Schedule#size()
	 */
	@Override
	public int size() {
		return schedule.size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return schedule.iterator();
	}

}
