package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.testcase.EvosuiteError;

/**
 * This class has two functions:
 * 1) It holds some static data heavens. Which are used by the concurrency test case generation to map certain values from the instrumentation (premain/javaagent) time to the runtime
 * 2) It is used as at runtime to conveniently access the controllerRuntime. I.e. static methods are easier to call from bytecode. Therefore we use static methods on this class to map to controller methods. Before each run, this class has to be initialized with a new controllerRuntime object
 * @author Sebastian Steenbuck
 *
 */
public class LockRuntime {

	public interface ThreadStartupMonitor{
		public void threadStartup();
		
		public void passedSchedulingPoint(Object requested, int requestID);
	}
	
	private static Logger logger = Logger.getLogger(LockRuntime.class);


	public static final String RUNTIME_CLASS="de/unisb/cs/st/evosuite/coverage/concurrency/LockRuntime";
	public static final String RUNTIME_REGISTER_THREAD_METHOD="registerThread";
	public static final String RUNTIME_SIGNAL_THREAD_EXIT_METHOD="threadEnd";
	public static final String RUNTIME_GET_NEW_THREAD_ID_METHOD="getUniqueThreadID";
	public static final String RUNTIME_SCHEDULER_CALL_METHOD="scheduler";


	public static volatile Thread runningThread = null;
	public static volatile ConcurrencyTracer tracer;

	/**
	 * Maps fieldAccessID to branch IDs in the CFG.
	 */
	public static final Map<Integer, Integer> fieldAccessIDToCFGBranch = new ConcurrentHashMap<Integer, Integer>();
	/**
	 * Maps fieldAccessIDS to a vertex in the CFG
	 */
	public static final Map<Integer, BytecodeInstruction> fieldAccessIDToCFGVertex = new ConcurrentHashMap<Integer, BytecodeInstruction>();
	
	/**
	 * If this switch is set to true. The behavior of the instrumented program should be changed. 
	 * Some methods may still log information 
	 */
	private static boolean threadControllingDisabled=false;
	
	/**
	 * Maps fieldAccessID to the ConcurrencyInstrumentation which inserted the scheduling point.
	 * Is used to transport information from the instrumentation time (like className/MethodName) to the runtime
	 * #TODO maybe this information can be recovered from the CFGVertices in fieldAccessIDToCFGVertex
	 */
	public static final Map<Integer, ConcurrencyInstrumentation> fieldAccToConcInstr = new ConcurrentHashMap<Integer, ConcurrencyInstrumentation>();
	public static final Set<Integer> threadIDs = new HashSet<Integer>(); //#TODO we make the assumption, that each run will have the same number of threads

	private volatile static int fieldAccessID=0; //A counter to generate a unique ID for each occurrence of a field (class or object) access. IDs are unique per VM

	public static ControllerRuntime controller;

	private static Set<ThreadStartupMonitor> startupMonitors = new HashSet<ThreadStartupMonitor>();
	
	
	public static Set<Thread> getThreadLockStruct(){
		return controller.locked;
	}
	
	/**
	 * StartupMonitors are called, iff a thread registers itself;
	 * @param monitor
	 */
	public static void registerStartupMonitor(ThreadStartupMonitor monitor){
		startupMonitors.add(monitor);
	}
	
	public static void removeStartupMonitor(ThreadStartupMonitor monitor){
		startupMonitors.remove(monitor);
	}
	
	public static void disableThreadControlling(){
		threadControllingDisabled=true;
	}
	
	public static void enableThreadControlling(){
		threadControllingDisabled=false;
	}
	
	

	public static void registerThread(int threadID){
		for(ThreadStartupMonitor monitor : startupMonitors){
			monitor.threadStartup();
		}
		
		if(threadControllingDisabled){
			return;
		}
		
		try{
			assert(controller!=null);
			logger.trace("The thread " + Thread.currentThread() + " registered itself with the threadID: " + threadID);
			controller.registerThread(threadID);
		}catch(Throwable e){
			throw new EvosuiteError(e);
		}
	}

	/**
	 * As the method is called at runtime, it is convenient to have access using a static method. Therefore this thin wrapper exists.
	 * @return
	 */
	public static int getUniqueThreadID(){
		try{
			assert(controller!=null);
			logger.trace("A unique thread ID was requested from the lockruntime, by the thread " + Thread.currentThread() + ". Which forwarded the request to the controller " + controller.toString()); 
			int id = controller.getThreadID(); 
			threadIDs.add(id);
			return id;
		}catch(Throwable e){
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Called right before a thread is destroyed.
	 * @param int the id of the thread 
	 */
	public static void threadEnd(){	
		if(threadControllingDisabled){
			return;
		}
		
		try{
			assert(controller!=null);
			logger.trace("The thread " + Thread.currentThread() + " signaled that he is about to finish");
			controller.threadEnd();
			controller.awake();
		}catch(Throwable e){
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Used to generate the field access ids. One ID should be used for each occurrence of a field access statement in the bytecode.
	 * Each occurrence should have a unique ID. 
	 * @return int the ID the next field access should have
	 */
	public static synchronized int getFieldAccessID(){
		logger.trace("A unique field accessID was requested from the lockruntime");
		return fieldAccessID++;
	}


	public static void mapFieldAccessIDtoCFGid(Integer fieldAccessID, Integer cfgID, BytecodeInstruction vertex){
		try{
			assert(!fieldAccessIDToCFGBranch.containsKey(fieldAccessID));
			//System.out.println(fieldAccessID + " - " + cfgID);
			fieldAccessIDToCFGBranch.put(fieldAccessID, cfgID);		
			fieldAccessIDToCFGVertex.put(fieldAccessID, vertex);	
		}catch(Throwable e){
			throw new EvosuiteError(e);
		}
	}

	/**
	 * This method provides the controllerRuntime with an option to stop threads.
	 * Notice that this code is run in the context of the thread. Therefore we do not need to supply the thread.
	 * Currently both params are for debugging only
	 * #TODO this would maybe be better placed in controllerRuntime
	 * @param requested the object which will be requested for the lock
	 * @param instructionId the id of this monitorInstruction
	 */
	public static void scheduler(Object requested, int requestID){
		for(ThreadStartupMonitor monitor : startupMonitors){
			monitor.passedSchedulingPoint(requested, requestID);
		}
		
		if(threadControllingDisabled){
			return;
		}
		
		try{
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
				logger.fatal("interrupted ! ", e);
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
		}catch(Throwable e){
			throw new EvosuiteError(e);
		}
	}
}
