/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.ga.Randomness;

/**
 * @author Sebastian Steenbuck
 *
 */
public class Scheduler implements Schedule{

	private ControllerRuntime controller=null;
	private final List<Integer> schedule;
	private boolean invalidated;
	private final Set<Scheduler> generatedSchedules;
	private final Set<Integer> seenThreadIDs;
	
	public Scheduler(List<Integer> schedule, Set<Integer> seenThreadID, Set<Scheduler> generatedSchedules){
		this.schedule=schedule;
		this.generatedSchedules=generatedSchedules;
		this.seenThreadIDs=seenThreadID;
		invalidated=false;
	}

	@Override
	public Iterator<Integer> iterator() {

		return new Iterator<Integer>() {
			int current=-1;
			boolean removeCallAllowed=false;
			/**
			 * As we can always generate the numbers, we always have another one
			 */
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Integer next() {
				assert(!invalidated);
				removeCallAllowed=true;
				current++;
				if(schedule.size()<=(current)){
					int nextElement = generateNextElement();
					schedule.add(nextElement);
					seenThreadIDs.add(nextElement);
				}

				assert(schedule.size()>current);
				return schedule.get(current);


			}

			/**
			 * At the current position, we might not be able to run the thread with the last returned id (thread maybe waiting for some other thread 
			 * The thread we returned may also already have ended
			 */
			@Override
			public void remove() {
				assert(!invalidated);
				assert(schedule.size()>current);
				if(removeCallAllowed){
					current--;
					schedule.remove(current);
					removeCallAllowed=false;
				}else{
					throw new IllegalStateException();
				}
			}
		};
	}

	public void invalidate(){
		invalidated=true;
	}

	@Override
	public void setController(ControllerRuntime c) {
		assert(c!=null);
		assert(!invalidated);
		controller=c;
		for(Scheduler s : generatedSchedules){
			s.invalidate();
		}
		generatedSchedules.clear();
		generatedSchedules.add(this);
		invalidated=false;
	}

	public int generateNextElement(){
		assert(controller!=null);
		//assert(controller.liveThreadCount()>0);
		if(controller.liveThreadCount()==0){ //might for example happen with empty test cases
			return 0;
		}

		int nextThreadCount = Randomness.getInstance().nextInt(controller.liveThreadCount());
		int nextThreadID = controller.getIdFromCount(nextThreadCount);
		return nextThreadID;
	}

	@Override
	public int getFirstElement() {
		if(schedule.isEmpty()){
			schedule.add(generateNextElement());
		}

		return schedule.get(0);
	}

	@Override
	public void removeElement(int index) {
		assert(!invalidated);
		schedule.remove(index);
	}

	@Override
	public int size() {
		return schedule.size();
	}

	@Override
	public int getRandomThreadID() {
		if(seenThreadIDs.size()==0)return 0; //if a mutation is done before the first run

		int elementToReturn = Randomness.getInstance().nextInt(seenThreadIDs.size());
		int i=0;
		for(Integer threadID : seenThreadIDs){
			if(i==elementToReturn){
				return threadID;
			}else{
				i++;
			}
		}


		throw new AssertionError("can't reach");
	}

	@Override
	public void add(int index, Integer element) {
		assert(!invalidated);
		schedule.add(index, element);
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
	
	public List<Integer> getSchedule(){
		return schedule;
	}
 
}
