/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Sebastian Steenbuck
 * 
 */
public class Scheduler implements Schedule {

	public interface scheduleObserver {
		/**
		 * is called by Schedule each time schedule point (threadid) is handed
		 * out.
		 * 
		 * @param schedulePosition
		 *            the position in the schedule which was handed out
		 * @param the
		 *            threadID which was handed out
		 */
		public void notify(int position, Integer threadid);
	}

	private class It implements Iterator<Integer> {
		/**
		 * Current is the index of the last thread id which was handed out
		 */
		protected int current = -1;
		protected boolean removeCallAllowed = false;

		/**
		 * As we can always generate the numbers, we always have another one
		 */
		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public Integer next() {
			assert (!invalidated);
			removeCallAllowed = true;
			current++;
			if (schedule.size() <= (current)) {
				int nextElement = generateNextElement();
				schedule.add(nextElement);
				seenThreadIDs.add(nextElement);
			}

			assert (schedule.size() > current);
			Integer result = schedule.get(current);
			return result;
		}

		/**
		 * At the current position, we might not be able to run the thread with
		 * the last returned id (thread maybe waiting for some other thread The
		 * thread we returned may also already have ended
		 */
		@Override
		public void remove() {
			// throw new
			// UnsupportedOperationException("Currently not supported, as we would need to notify the observer of a removed scheduling point");
			assert (!invalidated);
			assert (schedule.size() > current);
			if (removeCallAllowed) {
				schedule.remove(current);
				removeCallAllowed = false;
				current--;
			} else {
				throw new IllegalStateException();
			}
		}
	}

	private ControllerRuntime controller = null;
	private final List<Integer> schedule;
	private boolean invalidated;
	private final Set<Integer> seenThreadIDs;
	private final Scheduler.scheduleObserver observer;
	private It iterator;

	public Scheduler(List<Integer> schedule, Set<Integer> seenThreadID, Scheduler.scheduleObserver observer) {
		this.schedule = schedule;
		this.seenThreadIDs = seenThreadID;
		invalidated = false;
		this.observer = observer;
	}

	@Override
	public void add(int index, Integer element) {
		assert (!invalidated);
		schedule.add(index, element);
	}

	public int generateNextElement() {
		assert (controller != null);
		// assert(controller.liveThreadCount()>0);
		if (controller.liveThreadCount() == 0) { // might for example happen
													// with empty test cases
			return 0;
		}

		Randomness.getInstance();
		int nextThreadCount = Randomness.nextInt(controller.liveThreadCount());
		int nextThreadID = controller.getIdFromCount(nextThreadCount);
		return nextThreadID;
	}

	@Override
	public Iterable<Integer> getContentIterable() {
		return new Iterable<Integer>() {

			@Override
			public Iterator<Integer> iterator() {
				final Iterator<Integer> it = schedule.iterator();
				return new Iterator<Integer>() {

					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public Integer next() {
						return it.next();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	@Override
	public int getFirstElement() {
		if (schedule.isEmpty()) {
			schedule.add(generateNextElement());
		}

		return schedule.get(0);
	}

	@Override
	public int getRandomThreadID() {
		if (seenThreadIDs.size() == 0) {
			return 0; // if a mutation is done before the first run
		}

		Randomness.getInstance();
		int elementToReturn = Randomness.nextInt(seenThreadIDs.size());
		int i = 0;
		for (Integer threadID : seenThreadIDs) {
			if (i == elementToReturn) {
				return threadID;
			} else {
				i++;
			}
		}

		throw new AssertionError("can't reach");
	}

	public List<Integer> getSchedule() {
		return schedule;
	}

	public void invalidate() {
		invalidated = true;
	}

	@Override
	public Iterator<Integer> iterator() {
		this.iterator = new It();
		return this.iterator;
	}

	@Override
	public void notifyOfUsedSchedule(int threadID) {
		assert (iterator != null);
		assert (iterator.current >= 0);
		assert (schedule.size() > iterator.current);
		assert (schedule.get(iterator.current) == threadID);
		observer.notify(iterator.current, schedule.get(iterator.current));
	}

	@Override
	public void removeElement(int index) {
		assert (!invalidated);
		schedule.remove(index);
	}

	@Override
	public void setController(ControllerRuntime c) {
		assert (c != null);
		assert (!invalidated);
		controller = c;
		invalidated = false;
	}

	@Override
	public int size() {
		return schedule.size();
	}

}
