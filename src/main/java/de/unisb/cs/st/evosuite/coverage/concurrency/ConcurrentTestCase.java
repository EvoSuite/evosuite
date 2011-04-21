/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.assertion.Assertion;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

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
	
	//A list of thread IDs
	private List<Integer> schedule;
	/**
	 * Holds all schedules, that might in the future become active
	 */
	private final Set<Scheduler> generatedSchedules;
	private final BasicTestCase test;
	private final Set<Integer> seenThreadIDs;

	public ConcurrentTestCase(BasicTestCase test){
		assert(test!=null);
		this.test=test;
		seenThreadIDs=new HashSet<Integer>();
		schedule=new ArrayList<Integer>();
		generatedSchedules = new HashSet<Scheduler>();
	}

	/**
	 * Returns the schedule of this thread.
	 * Note that this schedule is unbounded (new thread ids are generated as needed)
	 * @return
	 */
	public Schedule getSchedule(){
		Scheduler s = new Scheduler(schedule, seenThreadIDs, generatedSchedules);
		generatedSchedules.add(s);
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
		ConcurrentTestCase newConTest = new ConcurrentTestCase(newTest);
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
		return test.toCode(exceptions);
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
	public void renameVariable(int old_position, int new_position) {
		test.renameVariable(old_position, new_position);
	}

	@Override
	public VariableReference setStatement(StatementInterface statement, int position) {
		return test.setStatement(statement, position);
	}

	@Override
	public void addStatement(StatementInterface statement, int position) {
		test.addStatement(statement, position);
	}


	@Override
	public void addStatement(StatementInterface statement) {
		test.addStatement(statement);
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


}
