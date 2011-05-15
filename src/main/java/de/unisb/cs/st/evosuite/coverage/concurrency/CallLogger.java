/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.coverage.concurrency.ScheduleLogWrapper.callReporter;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * @author Sebastian Steenbuck
 *
 */
public class CallLogger implements callReporter, Scheduler.scheduleObserver {
	private static final Logger logger = Logger.getLogger(CallLogger.class);
	private final Object SYNC = new Object(); //guard for access to the statementToSchedule and threadIdToCurrentStatement, as those might be accessed concurrently
	public boolean log = true;
	/*
	 * Maps statements to schedule.
	 * That is if during the execution of a statement 
	 * Each integer represents a position in the schedule list (0 to schedule.size())
	 */
	//#TODO should be private
	public final Map<StatementInterface, Set<Integer>> statementToSchedule = new HashMap<StatementInterface, Set<Integer>>();

	/**
	 * Maps threadIDs to the currently executed statement
	 */
	private final Map<Integer, StatementInterface> threadIdToCurrentStatement = new HashMap<Integer, StatementInterface>();

	public CallLogger(){

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.ScheduleLogWrapper.callReporter#callEnd(de.unisb.cs.st.evosuite.testcase.StatementInterface, java.lang.Integer)
	 */
	@Override
	public void callEnd(StatementInterface caller, Integer threadID) {
		if(!log) return;
		synchronized (SYNC) {
			assert(threadID!=null);
			assert(statementToSchedule.containsKey(caller)) : "caller " + caller + " code: " + caller.getCode();
			assert(threadIdToCurrentStatement.containsKey(threadID));
			assert(threadIdToCurrentStatement.get(threadID)!=null);
			threadIdToCurrentStatement.put(threadID, null);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.ScheduleLogWrapper.callReporter#callStart(de.unisb.cs.st.evosuite.testcase.StatementInterface, java.lang.Integer)
	 */
	@Override
	public void callStart(StatementInterface caller, Integer threadID) {
		if(!log) return;
		synchronized (SYNC) {
			assert(threadID!=null);
			assert(threadIdToCurrentStatement.get(threadID)==null) : "We expected that no statement would be registered for the thread " + threadID + " . But found " + threadIdToCurrentStatement.get(threadID);
			if(!statementToSchedule.containsKey(caller)){
				statementToSchedule.put(caller, new HashSet<Integer>());
			}
			threadIdToCurrentStatement.put(threadID, caller);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.concurrency.Scheduler.scheduleObserver#notify(int, int)
	 */
	@Override
	public void notify(int position, Integer threadid) {	
		if(!log) return;
		synchronized (SYNC) {
			assert(threadid!=null);
			Thread.State s = LockRuntime.controller.idToThread.get(threadid).getState();
			assert(s.equals(Thread.State.WAITING)) : ("state : " + LockRuntime.controller.idToThread.get(threadid).getState());
			assert(threadIdToCurrentStatement.containsKey(threadid));
			assert(threadIdToCurrentStatement.get(threadid)!=null) : LockRuntime.controller.idToThread.get(threadid).getState();

			StatementInterface currentStatement = threadIdToCurrentStatement.get(threadid);
			assert(statementToSchedule.get(currentStatement)!=null) : LockRuntime.controller.idToThread.get(threadid).getState(); 
			statementToSchedule.get(currentStatement).add(position);
		}
	}

	@Override
	public Set<Integer> getScheduleForStatement(StatementInterface st) {
		assert(st!=null);
		Set<Integer> res =  statementToSchedule.get(st);
		if(res==null){
			logger.debug("Look up for the schedule points of " + st + " showed that the statement is unkwown " + st.getCode());
			res = new HashSet<Integer>();
		}
		logger.debug("Statement " + st + " was influenced by " + res.size() + " scheduling decisions ");
		return res;
	}

}
