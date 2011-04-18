package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * 
 * @author Sebastian Steenbuck
 *
 */
public class LockRuntime {
	
	private static Logger logger = Logger.getLogger(LockRuntime.class);

	
	public static final String RUNTIME_CLASS="de/unisb/cs/st/evosuite/coverage/concurrency/LockRuntime";
	public static final String RUNTIME_REGISTER_THREAD_METHOD="registerThread";
	public static final String RUNTIME_SIGNAL_THREAD_EXIT_METHOD="threadEnd";
	public static final String RUNTIME_GET_NEW_THREAD_ID_METHOD="getUniqueThreadID";
	public static final String RUNTIME_SCHEDULER_CALL_METHOD="scheduler";
	
	
	public static volatile Thread runningThread = null;
	public static volatile ConcurrencyTracer tracer;
	
	/**
	 * Maps fieldAccessID to IDs in the CFG.
	 */
	public static final Map<Integer, Integer> fieldAccessIDToCFGBranch = new ConcurrentHashMap<Integer, Integer>();
	public static final Map<Integer, CFGVertex> fieldAccessIDToCFGVertex = new ConcurrentHashMap<Integer, CFGVertex>();
	public static final Map<Integer, ConcurrencyInstrumentation> fieldAccToConcInstr = new ConcurrentHashMap<Integer, ConcurrencyInstrumentation>();
	public static final Set<Integer> threadIDs = new HashSet<Integer>(); //#TODO we make the assumption, that each run will have the same number of threads
	
	private volatile static int fieldAccessID=0; //A counter to generate a unique ID for each occurrence of a field (class or object) access. IDs are unique per VM

	public static ControllerRuntime controller;

	public static Set<Thread> getThreadLockStruct(){
		return controller.locked;
	}

	public static void registerThread(int threadID){
		//if(true)throw new AssertionError("ijdknflkjsdgkjnsdgkjndskjgnksdjng");
		//logger.warn("XXXXXXXXXXXXXXXXXXXXXX REGISTER THREAD");
		//if(true)return;
		controller.registerThread(threadID);
	}
	
	/**
	 * As the method is called at runtime, it is convenient to have access using a static method. Therefore this thin wrapper exists.
	 * @return
	 */
	public static int getUniqueThreadID(){
		//logger.warn("XXXXXXXXXXXXXXXXXXXXXX GET THREAD ID");
		int id = controller.getThreadID();
		threadIDs.add(id);
		return id;
	}
	
	/**
	 * Called right before a thread is destroyed.
	 * @param int the id of the thread 
	 */
	public static void threadEnd(){	
		controller.threadEnd();
		controller.awake();
	}
	
	/**
	 * Used to generate the field access ids. One ID should be used for each occurrence of a field access statement in the bytecode.
	 * Each occurrence should have a unique ID. 
	 * @return int the ID the next field access should have
	 */
	public static synchronized int getFieldAccessID(){
		return fieldAccessID++;
	}
	
	
	public static void mapFieldAccessIDtoCFGid(Integer fieldAccessID, Integer cfgID, CFGVertex vertex){
		assert(!fieldAccessIDToCFGBranch.containsKey(fieldAccessID));
		//System.out.println(fieldAccessID + " - " + cfgID);
		fieldAccessIDToCFGBranch.put(fieldAccessID, cfgID);		
		fieldAccessIDToCFGVertex.put(fieldAccessID, vertex);		
	}

	/**
	 * This method provides the controllerRuntime with an option to stop threads.
	 * Notice that this code is run in the context of the thread. Therefore we do not need to supply the thread.
	 * Currently both params are for debugging only
	 * @param requested the object which will be requested for the lock
	 * @param id the id of this monitorInstruction
	 */
	public static void scheduler(Object requested, int requestID){
		//logger.warn("XXXXXXXXXXXXXXXXXXXXXX SCHEDULE THREAD");
		//#TODO steenbuck tmp work around should be in the end
		//System.out.println("test");

		//we need to do something here
		//if(true)return;
		//System.out.println(requested + " is requested by " + controller.getThreadID(Thread.currentThread()) + " requesterID " + " at " + requestID);
		assert(controller!=null); //needs to be set somewhere
		assert(tracer!=null); //maybe we later need to be able to run without a tracer (a dummy could be provided)
		
		/**
		 * This should read: if we're the standard thread to nothing. The generated tests cases are going to todo all method calling from generated threads and not from the test thread.
		 */
		if(!controller.threadWaitingPos.containsKey(Thread.currentThread())){
			return;
		}
		
		try {

			synchronized (controller.threadWaitingPos.get(Thread.currentThread())) {
				controller.locked.add(Thread.currentThread());
				controller.awake(); //this is going to stale for the current Thread as the controller won't be able to get the threadWaitingPos.get(Thread.currentThread()) monitor 
				while(runningThread!=Thread.currentThread()){					
					controller.threadWaitingPos.get(Thread.currentThread()).wait();
				}
				controller.locked.remove(Thread.currentThread());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
			//#TODO handleCase
		}


		synchronized (controller.TOCKEN) {
			//The currentThread is known to the runtime
			assert(controller.threadWaitingPos.containsKey(Thread.currentThread()));
			assert(controller.locked.contains(Thread.currentThread()) || runningThread==Thread.currentThread());

			runningThread=null; 
			controller.threadClock.put(Thread.currentThread(), controller.threadClock.get(Thread.currentThread())+1);
		}
		tracer.passedScheduleID(controller.getThreadID(Thread.currentThread()), requestID);
		//System.out.println(requested + " was granted to " + controller.getThreadID(Thread.currentThread()) + " requesterID " + " at " + requestID);
	}
}
