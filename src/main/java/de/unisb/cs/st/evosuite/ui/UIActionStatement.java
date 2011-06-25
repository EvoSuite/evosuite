package de.unisb.cs.st.evosuite.ui;

import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.objectweb.asm.commons.GeneratorAdapter;
import org.uispec4j.UIComponent;

import de.unisb.cs.st.evosuite.testcase.*;
import de.unisb.cs.st.evosuite.ui.model.DescriptorBoundUIAction;

public class UIActionStatement extends AbstractStatement {
	private static final long serialVersionUID = 1L;

	private DescriptorBoundUIAction<UIComponent> action;

	protected UIActionStatement(TestCase tc, DescriptorBoundUIAction<UIComponent> action) {
		super(tc, new VariableReferenceImpl(tc, Void.TYPE));
		
		this.action = action;
	}

	@Override
	public StatementInterface clone(TestCase testCase) {
		/* Can use action directly instead of creating a clone because it is immutable */
		return new UIActionStatement(testCase, this.action);
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out) throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals, Throwable exception) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCode(Throwable exception) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return Collections.emptyList();
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		return Collections.emptySet();
	}

	@Override
	public boolean same(StatementInterface s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;

		UIActionStatement other = (UIActionStatement) s;
		return (action.equals(other.action));
	}

	@Override
	public void replace(VariableReference var1, VariableReference var2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AccessibleObject getAccessibleObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssignmentStatement() {
		return false;
	}
}
