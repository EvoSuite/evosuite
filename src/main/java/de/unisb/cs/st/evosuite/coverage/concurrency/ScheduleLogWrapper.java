/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.assertion.Assertion;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * Wraps a StatementInterface and calls 
 * @author Sebastian Steenbuck
 *
 */
public class ScheduleLogWrapper implements StatementInterface{

	public interface callReporter{
		public void callStart(StatementInterface caller, Integer threadID);
		public void callEnd(StatementInterface caller, Integer threadID);
		public Set<Integer> getScheduleForStatement(StatementInterface st);
	}

	public boolean immutable = false;
	private void exit(){
		if(immutable){
			logger.fatal("good bye.....", new AssertionError());
			System.exit(1);
		}
	}

	private static Logger logger = Logger.getLogger(ScheduleLogWrapper.class);

	public final StatementInterface wrapped;
	private callReporter callReporter;

	public ScheduleLogWrapper(StatementInterface wrapped){
		assert(wrapped!=null) : "undefined behaviour lurks behind one statement beeing executed by multiple threads";
		this.wrapped=wrapped;
	}

	public void setCallReporter(callReporter callReporter){
		exit();
		this.callReporter=callReporter;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#SetRetval(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void SetRetval(VariableReference newRetVal) {
		exit();
		wrapped.SetRetval(newRetVal);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#addAssertion(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public void addAssertion(Assertion assertion) {
		exit();
		wrapped.addAssertion(assertion);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#adjustAssertions(int, int)
	 */
	@Override
	public void adjustAssertions(int position, int delta) {
		exit();
		wrapped.adjustAssertions(position, delta);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#adjustVariableReferences(int, int)
	 */
	@Override
	public void adjustVariableReferences(int position, int delta) {
		exit();
		wrapped.adjustVariableReferences(position, delta);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#equals(de.unisb.cs.st.evosuite.testcase.StatementInterface)
	 */
	@Override
	public boolean equals(Object s) {
		if(s instanceof ScheduleLogWrapper){
			return wrapped.equals(((ScheduleLogWrapper) s).wrapped);
		}else{
			return wrapped.equals(s);
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#execute(de.unisb.cs.st.evosuite.testcase.Scope, java.io.PrintStream)
	 */
	@Override
	public Throwable execute(Scope scope, PrintStream out)
	throws InvocationTargetException, IllegalArgumentException,
	IllegalAccessException, InstantiationException {
		exit();
		assert(LockRuntime.controller!=null);
		assert(callReporter!=null):"SetCallReporter/2 must be called before a wrapped statement may be executed";
		try{
			callReporter.callStart(this, LockRuntime.controller.getThreadID(Thread.currentThread()));
		}catch(Throwable e){
			logger.fatal("test", e);
		}
		Throwable t = wrapped.execute(scope, out);
		callReporter.callEnd(this, LockRuntime.controller.getThreadID(Thread.currentThread()));
		return t;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getAssertionCode()
	 */
	@Override
	public String getAssertionCode() {
		return wrapped.getAssertionCode();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getAssertions()
	 */
	@Override
	public Set<Assertion> getAssertions() {
		return wrapped.getAssertions();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getBytecode(org.objectweb.asm.commons.GeneratorAdapter, java.util.Map, java.lang.Throwable)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
			Throwable exception) {
		wrapped.getBytecode(mg, locals, exception);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getCode()
	 */
	@Override
	public String getCode() {
		return wrapped.getCode();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getCode(java.lang.Throwable)
	 */
	@Override
	public String getCode(Throwable exception) {
		return wrapped.getCode(exception);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getDeclaredExceptions()
	 */
	@Override
	public Set<Class<?>> getDeclaredExceptions() {
		return wrapped.getDeclaredExceptions();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getPosition()
	 */
	@Override
	public int getPosition() {
		return wrapped.getPosition();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getReturnClass()
	 */
	@Override
	public Class<?> getReturnClass() {
		return wrapped.getReturnClass();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getReturnType()
	 */
	@Override
	public Type getReturnType() {
		return wrapped.getReturnType();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getReturnValue()
	 */
	@Override
	public VariableReference getReturnValue() {
		return wrapped.getReturnValue();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return wrapped.getUniqueVariableReferences();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#getVariableReferences()
	 */
	@Override
	public Set<VariableReference> getVariableReferences() {
		return wrapped.getVariableReferences();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#hasAssertions()
	 */
	@Override
	public boolean hasAssertions() {
		return wrapped.hasAssertions();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#references(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public boolean references(VariableReference var) {
		return wrapped.references(var);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#removeAssertion(de.unisb.cs.st.evosuite.assertion.Assertion)
	 */
	@Override
	public void removeAssertion(Assertion assertion) {
		exit();
		wrapped.removeAssertion(assertion);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#removeAssertions()
	 */
	@Override
	public void removeAssertions() {
		exit();
		wrapped.removeAssertions();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#replace(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		exit();
		wrapped.replace(oldVar, newVar);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.StatementInterface#replaceUnique(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceUnique(VariableReference oldVar, VariableReference newVar) {
		exit();
		wrapped.replaceUnique(oldVar, newVar);
	}

	@Override
	public StatementInterface clone(){
		return new ScheduleLogWrapper(wrapped.clone());
	}
	
	@Override
	public int hashCode(){
		return wrapped.hashCode();
	}
}
