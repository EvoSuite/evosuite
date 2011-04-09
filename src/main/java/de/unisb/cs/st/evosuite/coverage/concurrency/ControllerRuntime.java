/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * 
 * #TODO the different thread contexts should be abstracted away to an access layer 
 * 
 * @author x3k6a2
 *
 */
public class ControllerRuntime implements Callable<Void> {

	private static Logger logger = Logger.getLogger(ControllerRuntime.class);
	
	public static volatile int r = 0;
	
	private int threadID=0; //A counter to generate a unique threadID for each thread

	/**
	 * Holds all the threads which are not running at present
	 */
	public final Set<Thread> locked = Collections.synchronizedSet(new HashSet<Thread>());
	/**
	 * Holds all the threads which have finished running
	 * #TODO a sensible approach would be to use a weakHashMap (right now we're preventing the garbage collector from freeing memory)
	 */
	public final Set<Thread> ended = Collections.synchronizedSet(new HashSet<Thread>());
	/**
	 * The objects the threads are waiting on. 
	 */
	public final Map<Thread, Object> threadWaitingPos = new ConcurrentHashMap<Thread, Object>();
	/**
	 * One integer clock for each thread. So that controller may know, if the thread has already reached a certain position
	 */
	public final Map<Thread, Integer> threadClock = new ConcurrentHashMap<Thread, Integer>();

	/**
	 * Before any access to the class Variables of LockRuntime in the block below the TOCKEN var, a monitor on TOCKEN has to be aquired
	 * Most likely all the class variables should be in the controller runtime
	 */
	public final Object TOCKEN=new Object();

	private volatile Boolean running;
	private Object runningMonitor = new Object();
	private final Map<Integer, Thread> idToThread = new ConcurrentHashMap<Integer, Thread>();
	private final Map<Thread, Integer> threadToId = new ConcurrentHashMap<Thread, Integer>();

	private volatile Boolean awake = false;
	private Object awakeMonitor = new Object();
	private Boolean finish = false;

	private int currentThreadClock=-1;
	private Thread currentThread;

	private final Schedule schedule;

	private final int threadCount;
	/**
	 * Notice that the controller must be made known to the LockRuntime by setting the controller class variable
	 * @param order
	 */
	public ControllerRuntime(Schedule schedule, int threadCount){
		assert(schedule!=null);
		assert(threadCount>0);
		schedule.setController(this);
		this.schedule=schedule;
		//#TODO do we need this
		running=false;
		this.threadCount=threadCount;
		//executionOrder=order;
	}
	
	/**
	 * Used by the schedule
	 * @return # of currently active threads
	 */
	public int liveThreadCount(){
		int count;
		synchronized (TOCKEN) {
			count = threadToId.keySet().size()-ended.size();
		}
		return count;
	}

	/**
	 * used by the schedule
	 * #TODO this only works because of implementation details. The threads should be in a list or something
	 * @param threadCount
	 * @return
	 */
	public int getIdFromCount(int threadCount){
		int count = 0;
		for(Thread t : threadToId.keySet()){
			if(!ended.contains(t)){
				if(count==threadCount){
					assert(threadToId.get(t)!=null);
					return threadToId.get(t);
				}else{
					count++;
				}
			}
		}
		/**
		 * We should never reach this place
		 */
		throw new AssertionError();
	}

	/**
	 * Used to generate the threadIDs.
	 * Each thread should have a unique ID. 
	 * @return int the ID the next thread should have
	 */
	public synchronized int getThreadID(){
		return threadID++;
	}

	public Integer getThreadID(Thread threadToLookup){
		assert(threadToId.get(threadToLookup)!=null);
		return threadToId.get(threadToLookup);
	}

	/**
	 * Called whenever a thread returns from it's run method. Called from the context of the ending thread
	 */
	public void threadEnd(){
		threadEnd(Thread.currentThread());
	}

	private void threadEnd(Thread t){
		assert(threadToId.containsKey(t));

		ended.add(Thread.currentThread());
		if(threadToId.keySet().size()==ended.size()){
			finish=true;
		}
	}

	public boolean isFinished(){
		if(finish)
			return true;

		for(Thread t : threadToId.keySet()){
			if(!ended.contains(t)){
				if(t.getState().equals(Thread.State.TERMINATED)){
					ended.add(t);
				}
			}
		}

		return finish;


	}

	/**
	 * This might be accessed by more than one thread at a time
	 * This is accessed in the context of the different threads
	 * Notice that the threads leave unordered. Therefore the LockRuntime.schedule needs to be called for each thread
	 * id is a unique thread id, we need to assume, that threads are created in a deterministic fashion
	 */
	public void registerThread(Integer id){
		if(threadToId.containsKey(Thread.currentThread()))throw new AssertionError("This shouldn't happen. May happen if (for example) the application under test calls the run() method of a thread manually. ");
		synchronized (TOCKEN) {
			threadWaitingPos.put(Thread.currentThread(), new Object());
			threadClock.put(Thread.currentThread(), 0);
			idToThread.put(id, Thread.currentThread());
			threadToId.put(Thread.currentThread(), id);
			//System.out.println("Register thread " + Thread.currentThread().toString() + " with the id " + id);

		}


		synchronized (runningMonitor) {
			synchronized (startupMonitor) {
				startupMonitor.notify();
			}
			try {
				while(!running){
					runningMonitor.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1); 
				//#TODO handle this case
			}
		}

	}

	public Object startupMonitor = new Object();
	@Override
	public Void call() throws Exception
	{
		r++;
		//System.out.println("controller startup number " + r++);
		synchronized (startupMonitor) {
			while(liveThreadCount()!=threadCount){
				startupMonitor.wait();
			}	
		}
		
		//Note that both below synchronized statements are for the benefit of the compiler. Nothing concurrent should be going on at this point in time
		synchronized (runningMonitor) {
			
			running=true;
			runningMonitor.notifyAll();
		}
		synchronized (awakeMonitor) {
			awake=true;
			awakeMonitor.notify(); //only one thread should monitor awake	
		}


		try {
			//#FIXME this shouldn't be a sleep but some wait (notice also, that we make the assumption, that the JVM manages it to manipulate the thread state from when we call notifyall() until here)
			//we wait here for the threads to reach there first position
			//Thread.yield();
			Thread.sleep(2);
			synchronized (awakeMonitor) {
				boolean waiting = true;
				while(waiting){
					waiting=false;
					for(Thread t : threadToId.keySet()){
						if(!waiting && !t.getState().equals(Thread.State.WAITING) && !t.getState().equals(Thread.State.TERMINATED)){
							//System.out.println("XXXXXXXXXXXXXXXXX SHOULD WAIT " + r + t.getState());
							waiting=true;
						}
					}
					if(waiting)awakeMonitor.wait(5);
				}
				next();
			}
			//next(); //we assume that now all threads are in the waiting position
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		////System.out.println("controller shutdown");
		return null;
	}

	/**
	 * Called in the context of the instrumented threads
	 */
	public synchronized void awake(){
		//System.out.println("awake call by " + threadToId.get(Thread.currentThread()));
		synchronized (awakeMonitor) {
			awake=true;
			awakeMonitor.notify();
		}

	}

	/**
	 * Starts the next thread
	 */
	private void next(){
		currentThread=idToThread.get(schedule.getFirstElement());
		Iterator<Integer> scheduler = schedule.iterator();
		while(!isFinished()){
			//System.out.println("xxx 0");
			synchronized (TOCKEN) {
				//System.out.println("xxx 1");
				synchronized (threadWaitingPos.get(currentThread)) { //#FIXME evil shit cause current thread may be changed. A monitor objects is in dire need of creation
					//System.out.println("xxx 2");
					if(LockRuntime.runningThread==null 
							&& (locked.contains(currentThread) || ended.contains(currentThread))
							&& threadClock.get(currentThread)>currentThreadClock){
						//if we're here, we can assume that currentThread finished last run.
						//System.out.println("NEXT CALL 3 " + executionOrder.size() + " - " + executionOrder.get(0));
						assert(scheduler.hasNext()); //the scheduler used for test case creation generates new IDs if no old ones exist
						int nextThreadId = scheduler.next();
						//#TODO !isFinished is needed in case one of the live threads is dying unexpected. Really can a infinite loop happen here? I don't think so.
						while((!idToThread.containsKey(nextThreadId) || ended.contains(idToThread.get(nextThreadId))) && !isFinished()){
							scheduler.remove();
							assert(scheduler.hasNext()); 
							nextThreadId=scheduler.next();
						}
						currentThread=idToThread.get(nextThreadId); //we always have a next element
						currentThreadClock=threadClock.get(currentThread);
						LockRuntime.runningThread=currentThread;
						//System.out.println("xxx 3");
						synchronized (threadWaitingPos.get(currentThread)) {
							//System.out.println("xxx 4");
							//System.out.println("awake thread " + threadToId.get(currentThread));
							threadWaitingPos.get(currentThread).notify();
							synchronized (awakeMonitor) {
								awake=false;
							}
						}
					}else{
						//Do nothing. The thread at the first position of executionOrder still needs to call awake()
					}
				}
			}
			//System.out.println("xxx 5 " + finished); 
			if(!isFinished()){
				synchronized (awakeMonitor) {
					//System.out.println("xxx 6");
					try {
						while(!awake){
							//System.out.println("xxx 7");
							awakeMonitor.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(1);
						//#TODO should be handled
					}
					//next(); //Would be nice but is not transformed to while by the compiler. Therefore the all enclosing while
				}
			}
		}
		
		//System.out.println("schedule " + printList(((Scheduler)schedule).getSchedule()));
	}
	
	public static String printList(List<Integer> l ){
		StringBuilder b = new StringBuilder();
		for(Integer  o : l){
			b.append(o);
			b.append(" - ");
		}
		return b.toString();
	}

	public boolean isRunning(){
		return running;
	}
}
