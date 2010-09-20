/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A runner thread in which a test case is executed and can be killed
 * 
 * @author Gordon Fraser
 *
 */
public class TestRunner extends Thread {
	
	private static Logger logger = Logger.getLogger(TestRunner.class);
	
	private TestCase test;
	
	private Scope scope = null;
	
	private boolean log = true;
	
	public boolean runFinished;
	
	public Map<Integer,Throwable> exceptionsThrown = new HashMap<Integer, Throwable>();
	
	public static Map<String, Integer> method_count = new HashMap<String, Integer>();
	public static Map<String, Long> method_time  = new HashMap<String, Long>();
	
	public List<ExecutionObserver> observers;
	
	public TestRunner(ThreadGroup threadGroup) {
	    super(threadGroup, "");
	  }

	  public void setup(TestCase tc, Scope scope, List<ExecutionObserver> observers) {
		  test = tc;
		  this.scope = scope;
		  this.observers = observers;
		  runFinished = false;
	  }

	  @Override
	  public final void run() {
		runFinished = false;
	    executeTestCase();
	    runFinished = true;
	  }

	  public void setLogging(boolean value) {
		  log = value;
	  }

	  private static void log(Statement s, long duration) {
		  if(s instanceof MethodStatement) {
			  MethodStatement ms = (MethodStatement)s;
			  String id = ms.getMethod().getDeclaringClass().getSimpleName()+"."+ms.getMethod().getName();
			  if(!method_count.containsKey(id)) {
				  method_count.put(id, 0);
				  method_time.put(id, 0l);
			  }
			  method_count.put(id, method_count.get(id) + 1);
			  method_time.put(id, method_time.get(id) + duration);
			  
		  }
	  }
	  
	  private void executeTestCase() {
		  int num = 0;
		  try {
//			  exceptionsThrown = test.execute(scope, observers, !log);
			for(Statement s : test.statements) {
				if(isInterrupted()) {
					logger.info("Thread interrupted at statement "+num+": "+s.getCode());
					break;
				}
				if(logger.isDebugEnabled())
					logger.debug("Executing statement "+s.getCode());
				ExecutionTracer.statementExecuted();

				long before = System.currentTimeMillis();
				Throwable exceptionThrown = s.execute(scope, System.out);
				long after = System.currentTimeMillis();
				/*
				for(ExecutionObserver observer : observers) {
					observer.statement(num, scope, s.getReturnValue());
				}
				*/
				if(logger.isDebugEnabled()) {
					log(s, after - before);
					if(after - before > 100)
						logger.info("Statement took "+(after-before)+"ms: "+s.getCode());
				}

				if(exceptionThrown != null) {
					exceptionsThrown.put(num, exceptionThrown);
					//exception_statement = num;
					if(log && logger.isDebugEnabled())
						logger.debug("Exception thrown in statement: "+s.getCode()+" - "+exceptionThrown.getClass().getName()+" - "+exceptionThrown.getMessage());
					//break;
				}
				if(logger.isDebugEnabled())
					logger.debug("Done statement "+s.getCode());
				num++;
			}
			
			num--;
			for(ExecutionObserver observer : observers) {
				for(Statement s : test.statements) {
					observer.statement(num, scope, s.getReturnValue());
				}
			}

			
		  } catch (ThreadDeath e) {//can't stop these guys
			  logger.info("Found error:");
			  logger.info(test.toCode());
			  e.printStackTrace();
			  throw e;
		  } catch (Throwable e) {
			  logger.info("Exception at statement "+num+"! "+e);
			  logger.info(test.toCode());
			  if (e instanceof java.lang.reflect.InvocationTargetException) {
				  logger.info("Cause: ");
				  logger.info(e.getCause());
				  e = e.getCause();
			  }
			  //exceptionThrown = e;
			  e.printStackTrace();
			  //System.exit(1);
		  }
	  }

}
