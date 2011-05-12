/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.assertion.Assertion;
import de.unisb.cs.st.evosuite.coverage.concurrency.ScheduleLogWrapper.callReporter;
import de.unisb.cs.st.evosuite.coverage.concurrency.Scheduler.scheduleObserver;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.AbstractStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

//#FIXME irgendwo in dieser software gibt es nicht deterministische prozesse, das sollte nicht sein

/**
 * A concurrentTestCase wraps a BasicTestCase and enriches it with some additional information.
 *  - the schedule (which is a list of thread ids)
 *  - a set of all schedulers, which have been given to other objects
 *  	- used to invalidate said schedulers at appropriate times.
 *   	- a set of seenThreadIDs. Which is used to generate new thread ids.
 *   
 * @author sebastian steenbuck
 */
public class ConcurrentTestCase implements TestCase{

	private static Logger logger = Logger.getLogger(ConcurrentTestCase.class);

	//A list of thread IDs
	private final List<Integer> schedule;

	private final boolean replaceConst;

	/**
	 * Each thread might give out exactly one schedule at any time
	 */
	private Scheduler currentSchedule=null;
	private final BasicTestCase test;
	private final Set<Integer> seenThreadIDs;
	public callReporter reporter;
	private scheduleObserver scheduleObserver;

	/**
	 * 
	 * @param test
	 * @param replaceConst if true all constructors are replaced with calls to a pseudo variable (representing the parameter)
	 */
	public ConcurrentTestCase(BasicTestCase test, boolean replaceConst){
		assert(test!=null);
		this.test=test;
		seenThreadIDs=new HashSet<Integer>();
		schedule=new ArrayList<Integer>();
		this.replaceConst=replaceConst;
	}

	public void setScheduleObserver(scheduleObserver obs){
		this.scheduleObserver=obs;
	}

	public void setCallReporter(callReporter reporter){
		this.reporter=reporter;
		for(StatementInterface st : this){
			if(st instanceof ScheduleLogWrapper){
				((ScheduleLogWrapper)st).setCallReporter(reporter);
			}
		}
	}

	/**
	 * Returns the schedule of this thread.
	 * Note that this schedule is unbounded (new thread ids are generated as needed)
	 * #TODO we should have two getSchedule classes. One for getting a schedule during execution (infinite)
	 * @return
	 */
	public Schedule getSchedule(){
		//assert(scheduleObserver!=null);

		//we need the schedule at two times (1. during execution 2. during output)
		if(currentSchedule!=null){
			currentSchedule.invalidate();
		}
		Scheduler s = new Scheduler(schedule, seenThreadIDs, scheduleObserver);
		return s;
	}


	@Override
	public int hashCode() {
		return test.hashCode()+schedule.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConcurrentTestCase other = (ConcurrentTestCase) obj;

		if(!other.test.equals(this.test))
			return false;

		if(this.schedule.size()!=other.schedule.size())
			return false;

		for(int i=0;i<schedule.size();i++){
			if(!this.schedule.get(i).equals(other.schedule.get(i)))
				return false;
		}

		return true;
	}

	/**
	 * Create a copy of the test case
	 */
	@Override
	public ConcurrentTestCase clone() {
		BasicTestCase newTest = test.clone();
		ConcurrentTestCase newConTest = new ConcurrentTestCase(newTest, replaceConst);
		newConTest.setCallReporter(reporter);
		newConTest.setScheduleObserver(scheduleObserver);
		newConTest.schedule.addAll(schedule);
		return newConTest;
	}

	@Override
	public int size() {
		return test.size();
	}

	@Override
	public boolean isEmpty() {
		return test.isEmpty();
	}

	@Override
	public void chop(int length) {
		test.chop(length);
	}

	@Override
	public String toCode() {
		return test.toCode();
	}

	public String getThreadCode(Map<Integer, Throwable> exceptions, int id){
		/*
		 *#TODO steenbuck this is a workaround which should be removd.
		 *The problem is that the code is executed inside evosuite using a testcase with two more statements. (Thread ID and thread registratioon)
		 */
		this.addStatement(getPseudoStatement(this, Properties.getTargetClass(), 0), 0, false);
		this.addStatement(getPseudoStatement(this, Properties.getTargetClass(), 0), 0, false);
		assert(scheduleObserver!=null);
		StringBuilder b = new StringBuilder();
		b.append("Integer[] schedule");
		b.append(id);
		b.append(" = {");
		for(Integer i : getSchedule().getContentIterable()){
			b.append(i);
			b.append(",");
		}
		b.deleteCharAt(b.length()-1);
		b.append("};");
		String testString = "private class TestThread"+id+" implements Callable<Void>{ \n"+
		" private final Triangle param0; \n"+
		" private final int tid; \n"+
		" public TestThread"+id+"(Triangle param0, int tid)  {\n"+
		"	this.param0=param0;\n"+
		"	this.tid=tid;\n"+
		" }\n"+
		"\n"+
		" @Override\n"+
		" public Void call() throws Exception {\n"+
		"	LockRuntime.registerThread(tid);\n"+
		toCode(exceptions) +
		"	LockRuntime.threadEnd();\n"+
		"	return null;\n"+
		" }\n"+
		"}\n"+	
		"\n" +
		b.toString() + "\n" +
		"public void test"+id+"(){\n"+
		"	Triangle var0 = new Triangle();\n"+
		"	FutureTask<Void> c = new FutureTask<Void>(new ControllerRuntime(new SimpleScheduler(schedule"+id+"), " + ConcurrencyCoverageFactory.THREAD_COUNT + "));\n"+
		"Set<FutureTask<Void>> testFutures = new HashSet<FutureTask<Void>>();\n" +
		"for(int i=0 ; i<" + ConcurrencyCoverageFactory.THREAD_COUNT + " ; i++){\n" +
		"	FutureTask<Void> testFuture = new FutureTask<Void>(new TestThread"+id+"(var0,i));\n"+
		"	Thread testThread = new Thread(testFuture);\n" +
		"	testThread.start();\n" +
		"	testFutures.add(testFuture);\n" +
		"}\n\n" +

		"	try{\n\n"+		
		"		for(FutureTask<Void> testFuture : testFutures){\n"+
		"		testFuture.get();\n"+
		"		}\n\n"+
		"    c.get();\n"+
		"	}catch(Exception e){\n"+
		"    e.printStackTrace();\n"+
		"	}";

		return testString;
	}


	@Override
	public String toCode(Map<Integer, Throwable> exceptions) {
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			StatementInterface statement = this.getStatement(i);
			Set<Integer> schedule = this.reporter.getScheduleForStatement(statement);
			StringBuilder scheduleString = new StringBuilder();
			for(Integer p : schedule){
				scheduleString.append(p);
				scheduleString.append(",");
			}
			if (exceptions.containsKey(i)) {
				code.append(statement.getCode(exceptions.get(i)) + "// schedule: " + scheduleString.toString() + " \n");
				code.append(statement.getAssertionCode());
			} else {
				code.append(statement.getCode() + "// schedule: " + scheduleString.toString() + " \n");
				code.append(statement.getAssertionCode()); // TODO: Handle semicolons
				// properly
			}
		}
		return code.toString();
	}

	@Override
	public List<VariableReference> getObjects(Type type, int position) {
		return test.getObjects(type,position);
	}

	@Override
	public List<VariableReference> getObjects(int position) {
		return test.getObjects(position);
	}

	@Override
	public VariableReference getRandomObject() {
		return test.getRandomObject();
	}

	@Override
	public VariableReference getRandomObject(int position) {
		return test.getRandomObject(position);
	}

	@Override
	public VariableReference getRandomObject(Type type)
	throws ConstructionFailedException {
		return test.getRandomObject(type);
	}

	@Override
	public VariableReference getRandomObject(Type type, int position)
	throws ConstructionFailedException {
		return test.getRandomObject(type, position);
	}

	@Override
	public Object getObject(VariableReference reference, Scope scope) {
		return test.getObject(reference, scope);
	}

	@Override
	public VariableReference setStatement(StatementInterface statement, int position) {
		statement = wrapStatements(statement);
		//#TODO this should be reduced to remove and addStatement
		return test.setStatement(statement, position);
	}

	@Override
	public void addStatement(StatementInterface statement, int position) {
		this.addStatement(statement, position, true);
	}

	/**
	 * Same as addStatement.
	 * If wrap is set to false, the added statement isn't wrapped
	 * @param statement
	 * @param position
	 * @param wrap
	 */
	public void addStatement(StatementInterface statement, int position, boolean wrap) {
		if(replaceConst)
			statement = replaceConstructorStatement(statement, position);
		if(wrap)
			statement = wrapStatements(statement);
		test.addStatement(statement, position);
	}

	@Override
	public void addStatement(StatementInterface statement) {
		this.addStatement(statement, test.size());
	}

	public void addStatement(StatementInterface statement, boolean wrap) {
		this.addStatement(statement, test.size(), wrap);
	}

	private StatementInterface wrapStatements(StatementInterface st){
		ScheduleLogWrapper wrapper = new ScheduleLogWrapper(st);
		wrapper.setCallReporter(reporter);
		return wrapper;
	}

	@Override
	public VariableReference getReturnValue(int position) {
		return test.getReturnValue(position);
	}

	@Override
	public boolean hasReferences(VariableReference var) {
		return test.hasReferences(var);
	}

	@Override
	public List<VariableReference> getReferences(VariableReference var) {
		return test.getReferences(var);
	}

	@Override
	public void remove(int position) {
		test.remove(position);
	}

	@Override
	public StatementInterface getStatement(int position) {
		return test.getStatement(position);
	}

	@Override
	public boolean hasObject(Type type, int position) {
		return test.hasObject(type, position);
	}

	@Override
	public boolean hasCastableObject(Type type) {
		return test.hasCastableObject(type);
	}

	@Override
	public Set<Class<?>> getAccessedClasses() {
		return test.getAccessedClasses();
	}

	@Override
	public boolean hasAssertions() {
		return test.hasAssertions();
	}

	@Override
	public List<Assertion> getAssertions() {
		return test.getAssertions();
	}

	@Override
	public void removeAssertions() {
		test.removeAssertions();
	}

	@Override
	public boolean isValid() {
		return test.isValid();
	}

	public Set<Class<?>> getDeclaredExceptions() {
		return test.getDeclaredExceptions();
	}

	@Override
	public boolean hasCalls() {
		return test.hasCalls();
	}

	@Override
	public void addCoveredGoal(TestFitnessFunction goal) {
		test.addCoveredGoal(goal);
	}

	@Override
	public Set<TestFitnessFunction> getCoveredGoals() {
		return test.getCoveredGoals();
	}

	@Override
	public Iterator<StatementInterface> iterator() {
		return test.iterator();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCase#addAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void addAssertions(TestCase other) {
		test.addAssertions(other);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCase#getStatements()
	 */
	@Override
	public List<StatementInterface> getStatements() {
		return test.getStatements();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestCase#isPrefix(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public boolean isPrefix(TestCase t) {
		return test.isPrefix(t);
	}

	/**
	 * Checks if a constructor call should reference a param
	 * @param statement
	 * @param position
	 * @return
	 */
	private StatementInterface replaceConstructorStatement(StatementInterface statement, int position){
		if(replaceConst && statement instanceof ConstructorStatement){
			ConstructorStatement c = (ConstructorStatement)statement;
			//#TODO steenbuck we should check if the constructor uses the object we supplied as param (if yes maybe we should let the object be created)
			assert(Properties.getTargetClass()!=null);
			if(Properties.getTargetClass().isAssignableFrom(c.getConstructor().getDeclaringClass())){
				logger.debug("Replaced a constructor call for " + c.getClass().getSimpleName() + " with a pseudo statement. Representing the object shared between the test threads");
				statement = getPseudoStatement(this, Properties.getTargetClass(), position);
			}
		}

		return statement;
	}

	/**
	 * The statements returned by this method can only be executed with a concurrentScope
	 * @param clazz
	 * @param pos
	 * @return
	 */
	private StatementInterface getPseudoStatement(TestCase tc, final Class<?> clazz, int pos){
		StatementInterface st= new AbstractStatement(tc, new VariableReference(clazz, pos)) {

			@Override
			public int hashCode() {
				return 0;
			}

			@Override
			public Set<VariableReference> getVariableReferences() {
				Set<VariableReference> s = new HashSet<VariableReference>();
				s.add(retval);
				return s;
			}

			@Override
			public List<VariableReference> getUniqueVariableReferences() {
				List<VariableReference> s = new ArrayList<VariableReference>();
				s.add(retval);
				return s;
			}

			@Override
			public String getCode(Throwable exception) {
				//#TODO steenbuck param0 should not be hardcoded
				return retval.getSimpleClassName() + " " + retval.getName() + " = param0;";
			}

			@Override
			public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
					Throwable exception) {
			}

			@Override
			public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
				if(scope instanceof ConcurrentScope){
					//Object o = scope.get(new VariableReference(retval.getType(), -1));
					Object o = ((ConcurrentScope)scope).getSharedObject();
					assert(retval.getVariableClass().isAssignableFrom(o.getClass())) : "we want an " + retval.getVariableClass() + " but got an " + o.getClass();
					scope.set(retval, o);
				}else{
					throw new AssertionError("Statements from " + BasicTestCase.class.getName() + " should only be executed with a concurrent scope");
				}
				return null;
			}

			@Override
			public boolean equals(Object s) {
				return s==this;
			}

			@Override
			public void addAssertion(Assertion assertion) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public StatementInterface clone(TestCase newTestCase) {
				return getPseudoStatement(newTestCase, clazz, retval.getStPosition());
			}

			@Override
			public String getAssertionCode() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<Assertion> getAssertions() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getCode() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<Class<?>> getDeclaredExceptions() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getPosition() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Class<?> getReturnClass() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Type getReturnType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public VariableReference getReturnValue() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasAssertions() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean references(VariableReference var) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void removeAssertion(Assertion assertion) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void removeAssertions() {
				// TODO Auto-generated method stub
				
			}

		};

		return st;
	}

}
