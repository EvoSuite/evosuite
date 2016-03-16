/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.evosuite.testcase.AbstractStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.PrimitiveExpression;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.GenericAccessibleObject;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * A compound test case is a test case that is read from an existing JUnit test
 * file, and may have a complex structure. It contains static code sections,
 * fields, constructors, @BeforeClass, @Before, @After, @AfterClass annotated
 * methods and possibly a class hierarchy. A CompoundTestCase is used to gather
 * all those statements to eventually combine them into a normal
 * {@link TestCase} when {@link #finalizeTestCase()} is called.
 * 
 * @author roessler
 */
public class CompoundTestCase implements Serializable {
	public static class MethodDef {
		private final String name;
		private final List<VariableReference> params = new ArrayList<VariableReference>();
		final List<StatementInterface> code = new ArrayList<StatementInterface>();

		public MethodDef(String name) {
			super();
			this.name = name;
		}

		public void add(StatementInterface statement) {
			code.add(statement);
		}

		public List<StatementInterface> getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public List<VariableReference> getParams() {
			return params;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class ReturnStatementPlaceholder extends AbstractStatement {

		private static final long serialVersionUID = 1L;

		protected ReturnStatementPlaceholder(TestCase tc, VariableReference returnValue) {
			super(tc, returnValue.getType());
			retval = returnValue;
		}

		@Override
		public StatementInterface copy(TestCase newTestCase, int offset) {
			throw new UnsupportedOperationException("Method copy not implemented!");
		}

		@Override
		public Throwable execute(Scope scope, PrintStream out)
		        throws InvocationTargetException, IllegalArgumentException,
		        IllegalAccessException, InstantiationException {
			throw new UnsupportedOperationException("Method execute not implemented!");
		}

		@Override
		public GenericAccessibleObject getAccessibleObject() {
			throw new UnsupportedOperationException(
			        "Method getAccessibleObject not implemented!");
		}

		@Override
		public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
		        Throwable exception) {
			throw new UnsupportedOperationException("Method getBytecode not implemented!");
		}

		@Override
		public List<VariableReference> getUniqueVariableReferences() {
			throw new UnsupportedOperationException(
			        "Method getUniqueVariableReferences not implemented!");
		}

		@Override
		public Set<VariableReference> getVariableReferences() {
			throw new UnsupportedOperationException(
			        "Method getVariableReferences not implemented!");
		}

		@Override
		public boolean isAssignmentStatement() {
			return false;
		}

		@Override
		public void replace(VariableReference oldVar, VariableReference newVar) {
			if (retval.equals(oldVar)) {
				retval = newVar;
			}
		}

		@Override
		public boolean same(StatementInterface s) {
			throw new UnsupportedOperationException("Method same not implemented!");
		}

	}

	public static enum TestScope {
		BEFORE_CLASS,
		STATIC,
		STATICFIELDS,
		BEFORE,
		FIELDS,
		CONSTRUCTOR,
		AFTER,
		AFTER_CLASS,
		METHOD;
	}

	/** Constant <code>STATIC_BLOCK_METHODNAME="<static block>"</code> */
	public static final String STATIC_BLOCK_METHODNAME = "<static block>";

	private static final long serialVersionUID = 1L;

	private final Map<String, MethodDef> methodDefs = new LinkedHashMap<String, MethodDef>();
	private final Map<String, VariableReference> currentMethodVars = new HashMap<String, VariableReference>();
	private final Map<String, VariableReference> fieldVars = new HashMap<String, VariableReference>();
	private final List<MethodDef> constructors = new ArrayList<MethodDef>();
	private final List<MethodDef> afterMethods = new ArrayList<MethodDef>();
	private final List<MethodDef> afterClassMethods = new ArrayList<MethodDef>();
	private final List<MethodDef> beforeMethods = new ArrayList<MethodDef>();
	private final List<MethodDef> beforeClassMethods = new ArrayList<MethodDef>();
	private final List<StatementInterface> staticFields = new ArrayList<StatementInterface>();
	private final List<StatementInterface> fields = new ArrayList<StatementInterface>();
	private final List<StatementInterface> staticCode = new ArrayList<StatementInterface>();
	private final String className;
	private final String testMethod;
	// find here or up the hierarchy
	private CompoundTestCase parent;
	// Needed for methods and fields:
	// find method in actual class or up the hierarchy
	private final CompoundTestCase originalDescendant;
	private TestScope currentScope = TestScope.FIELDS;
	private MethodDef currentMethod = null;

	private final DelegatingTestCase delegate;

	/**
	 * <p>
	 * Constructor for CompoundTestCase.
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param child
	 *            a {@link org.evosuite.junit.CompoundTestCase} object.
	 */
	public CompoundTestCase(String className, CompoundTestCase child) {
		child.parent = this;
		delegate = child.getReference();
		originalDescendant = child.originalDescendant;
		this.testMethod = null;
		this.className = className;
	}

	/**
	 * <p>
	 * Constructor for CompoundTestCase.
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public CompoundTestCase(String className, String methodName) {
		this.testMethod = methodName;
		this.className = className;
		delegate = new DelegatingTestCase();
		originalDescendant = this;
	}

	/**
	 * <p>
	 * addParameter
	 * </p>
	 * 
	 * @param varRef
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public void addParameter(VariableReference varRef) {
		currentMethod.getParams().add(varRef);
	}

	/**
	 * <p>
	 * addStatement
	 * </p>
	 * 
	 * @param statement
	 *            a {@link org.evosuite.testcase.StatementInterface} object.
	 */
	public void addStatement(StatementInterface statement) {
		if (currentScope == TestScope.FIELDS) {
			fields.add(statement);
			return;
		}
		if (currentScope == TestScope.STATICFIELDS) {
			staticFields.add(statement);
			return;
		}
		if (currentScope == TestScope.STATIC) {
			staticCode.add(statement);
			return;
		}
		currentMethod.add(statement);
	}

	/**
	 * <p>
	 * addVariable
	 * </p>
	 * 
	 * @param varBinding
	 *            a {@link org.eclipse.jdt.core.dom.IVariableBinding} object.
	 * @param varRef
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public void addVariable(IVariableBinding varBinding, VariableReference varRef) {
		if ((currentScope == TestScope.FIELDS)
		        || (currentScope == TestScope.STATICFIELDS)) {
			fieldVars.put(varBinding.toString(), varRef);
			return;
		}
		currentMethodVars.put(varBinding.toString(), varRef);
	}

	/**
	 * <p>
	 * convertMethod
	 * </p>
	 * 
	 * @param methodDef
	 *            a {@link org.evosuite.junit.CompoundTestCase.MethodDef}
	 *            object.
	 * @param params
	 *            a {@link java.util.List} object.
	 * @param retVal
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public void convertMethod(MethodDef methodDef, List<VariableReference> params,
	        VariableReference retVal) {
		assert methodDef.getParams().size() == params.size();
		Map<VariableReference, VariableReference> methodVarsMap = new HashMap<VariableReference, VariableReference>();
		for (StatementInterface statement : methodDef.getCode()) {
			for (int idx = 0; idx < params.size(); idx++) {
				statement.replace(methodDef.getParams().get(idx), params.get(idx));
			}
			if (statement instanceof ReturnStatementPlaceholder) {
				VariableReference resultVal = methodVarsMap.get(statement.getReturnValue());
				if (resultVal == null) {
					throw new IllegalStateException();
				}
				AssignmentStatement assignmentStatement = new AssignmentStatement(
				        delegate, retVal, resultVal);
				addStatement(assignmentStatement);
				return;
			}
			StatementInterface newStmt = statement;
			if (!(statement instanceof PrimitiveExpression)) {
				// Since the delegate code is not yet finished,
				// cloning of PrimitiveExpressions does not work.
				newStmt = statement.clone(delegate);
			}
			addReplacementVariable(statement.getReturnValue(), newStmt.getReturnValue());
			methodVarsMap.put(statement.getReturnValue(), newStmt.getReturnValue());
			addStatement(newStmt);
		}
	}

	/**
	 * <p>
	 * discardMethod
	 * </p>
	 */
	public void discardMethod() {
		currentScope = TestScope.FIELDS;
		currentMethod = null;
		currentMethodVars.clear();
	}

	/**
	 * <p>
	 * finalizeMethod
	 * </p>
	 */
	public void finalizeMethod() {
		String currentMethodName = currentMethod.getName();
		methodDefs.put(currentMethodName, currentMethod);
		switch (currentScope) {
		case AFTER:
			afterMethods.add(currentMethod);
			break;
		case AFTER_CLASS:
			afterClassMethods.add(currentMethod);
			break;
		case BEFORE:
			beforeMethods.add(currentMethod);
			break;
		case BEFORE_CLASS:
			beforeClassMethods.add(currentMethod);
			break;
		case CONSTRUCTOR:
			constructors.add(currentMethod);
			break;
		default:
			break;
		}
		currentScope = TestScope.FIELDS;
		currentMethod = null;
		currentMethodVars.clear();
	}

	/**
	 * <p>
	 * finalizeTestCase
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase finalizeTestCase() {
		Set<String> overridenMethods = Collections.emptySet();
		delegate.setDelegate(new DefaultTestCase());
		delegate.addStatements(getStaticInitializationBeforeClassMethods(overridenMethods));
		delegate.addStatements(getInitializationCode());
		delegate.addStatements(getBeforeMethods(overridenMethods));
		if (testMethod == null) {
			throw new RuntimeException("Test did not contain any statements!");
		}
		if (methodDefs.get(testMethod) == null) {
			throw new RuntimeException("Error reading test method " + testMethod + "!");
		}
		delegate.addStatements(methodDefs.get(testMethod).getCode());
		delegate.addStatements(getAfterMethods(overridenMethods));
		delegate.addStatements(getAfterClassMethods(overridenMethods));
		assert delegate.clone().toCode().equals(delegate.toCode());
		return delegate;
	}

	/**
	 * <p>
	 * Getter for the field <code>className</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <p>
	 * Getter for the field <code>currentMethod</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getCurrentMethod() {
		return currentMethod.getName();
	}

	/**
	 * <p>
	 * Getter for the field <code>currentScope</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.junit.CompoundTestCase.TestScope} object.
	 */
	public TestScope getCurrentScope() {
		return currentScope;
	}

	/**
	 * <p>
	 * getLastStatement
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.StatementInterface} object.
	 */
	public StatementInterface getLastStatement() {
		assert currentScope == TestScope.METHOD;
		return currentMethod.code.get(currentMethod.code.size() - 1);
	}

	/**
	 * <p>
	 * getMethod
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.junit.CompoundTestCase.MethodDef} object.
	 */
	public MethodDef getMethod(String name) {
		if (originalDescendant != this) {
			return originalDescendant.getMethodInternally(name);
		}
		return getMethodInternally(name);
	}

	/**
	 * <p>
	 * Getter for the field <code>parent</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.junit.CompoundTestCase} object.
	 */
	public CompoundTestCase getParent() {
		return parent;
	}

	/**
	 * <p>
	 * getReference
	 * </p>
	 * 
	 * @return a {@link org.evosuite.junit.DelegatingTestCase} object.
	 */
	public DelegatingTestCase getReference() {
		return delegate;
	}

	/**
	 * <p>
	 * getVariableReference
	 * </p>
	 * 
	 * @param varBinding
	 *            a {@link org.eclipse.jdt.core.dom.IVariableBinding} object.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public VariableReference getVariableReference(IVariableBinding varBinding) {
		if (originalDescendant != this) {
			return originalDescendant.getVariableReferenceInternally(varBinding);
		}
		return getVariableReferenceInternally(varBinding);
	}

	/**
	 * <p>
	 * isDescendantOf
	 * </p>
	 * 
	 * @param declaringClass
	 *            a {@link java.lang.Class} object.
	 * @return a boolean.
	 */
	public boolean isDescendantOf(Class<?> declaringClass) {
		if (parent == null) {
			return false;
		}
		if (parent.className.equals(declaringClass.getName())) {
			return true;
		}
		return parent.isDescendantOf(declaringClass);
	}

	/**
	 * <p>
	 * newMethod
	 * </p>
	 * 
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public void newMethod(String methodName) {
		assert (currentMethod == null) || currentMethod.getCode().isEmpty();
		assert currentMethodVars.isEmpty();
		currentScope = TestScope.METHOD;
		currentMethod = new MethodDef(methodName);
	}

	/**
	 * <p>
	 * Setter for the field <code>currentScope</code>.
	 * </p>
	 * 
	 * @param scope
	 *            a {@link org.evosuite.junit.CompoundTestCase.TestScope}
	 *            object.
	 */
	public void setCurrentScope(TestScope scope) {
		this.currentScope = scope;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return className;
	}

	/**
	 * <p>
	 * variableAssignment
	 * </p>
	 * 
	 * @param varRef
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 * @param newAssignment
	 *            a {@link org.evosuite.testcase.VariableReference} object.
	 */
	public void variableAssignment(VariableReference varRef,
	        VariableReference newAssignment) {
		for (Map.Entry<String, VariableReference> entry : currentMethodVars.entrySet()) {
			if (entry.getValue() == varRef) {
				currentMethodVars.put(entry.getKey(), newAssignment);
				return;
			}
		}
		for (Map.Entry<String, VariableReference> entry : fieldVars.entrySet()) {
			if (entry.getValue() == varRef) {
				fieldVars.put(entry.getKey(), newAssignment);
				return;
			}
		}
		if (parent != null) {
			for (Map.Entry<String, VariableReference> entry : parent.fieldVars.entrySet()) {
				if (entry.getValue() == varRef) {
					parent.fieldVars.put(entry.getKey(), newAssignment);
					return;
				}
			}
		}
		throw new RuntimeException("Assignment " + varRef + " not found!");
	}

	private void addReplacementVariable(VariableReference oldValue,
	        VariableReference newValue) {
		String variable = getVariableFromCurrentScope(oldValue);
		fieldVars.put(variable, newValue);
	}

	private List<StatementInterface> getAfterClassMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		for (MethodDef methodDef : afterClassMethods) {
			if (!overridenMethods.contains(methodDef.getName())) {
				result.addAll(methodDef.getCode());
			}
		}
		if (parent != null) {
			// @AfterClass IF NOT OVERRIDEN
			// According to Kent Beck, there is no defined order
			// in which methods of the same leve within one class are called:
			// http://tech.groups.yahoo.com/group/junit/message/20758
			result.addAll(parent.getAfterClassMethods(methodDefs.keySet()));
		}
		return result;
	}

	private List<StatementInterface> getAfterMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		// @After
		for (MethodDef methodDef : afterMethods) {
			if (!overridenMethods.contains(methodDef.getName())) {
				result.addAll(methodDef.getCode());
			}
		}
		if (parent != null) {
			// parent: @After
			result.addAll(parent.getAfterMethods(methodDefs.keySet()));
		}
		return result;
	}

	private List<StatementInterface> getBeforeMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			result.addAll(parent.getBeforeMethods(methodDefs.keySet()));
		}
		// @Before IF NOT OVERRIDEN
		// According to Kent Beck, there is no defined order
		// in which methods of the same leve within one class are called:
		// http://tech.groups.yahoo.com/group/junit/message/20758
		for (MethodDef methodDef : beforeMethods) {
			if (!overridenMethods.contains(methodDef.getName())) {
				result.addAll(methodDef.getCode());
			}
		}
		return result;
	}

	private List<StatementInterface> getInitializationCode() {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			result.addAll(parent.getInitializationCode());
		}
		// initialization
		result.addAll(fields);
		// constructor
		result.addAll(getNoArgsConstructor());
		return result;
	}

	private MethodDef getMethodInternally(String name) {
		MethodDef result = methodDefs.get(name);
		if (result != null) {
			return result;
		}
		if (parent != null) {
			return parent.getMethodInternally(name);
		}
		throw new RuntimeException("Method " + name + " not found!");
	}

	private List<StatementInterface> getNoArgsConstructor() {
		for (MethodDef constructor : constructors) {
			if (constructor.getParams().isEmpty()) {
				return constructor.getCode();
			}
		}
		if (constructors.size() > 1) {
			throw new RuntimeException("Found " + constructors.size()
			        + " constructors, but on no-args constructor!");
		}
		return Collections.emptyList();
	}

	private List<StatementInterface> getStaticInitializationBeforeClassMethods(
	        Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			// @BeforeClass IF NOT OVERRIDEN
			// According to Kent Beck, there is no defined order
			// in which methods of the same leve within one class are called:
			// http://tech.groups.yahoo.com/group/junit/message/20758
			result.addAll(parent.getStaticInitializationBeforeClassMethods(methodDefs.keySet()));
		}
		// this: static initialization
		result.addAll(staticFields);
		// static blocks
		result.addAll(staticCode);
		// @BeforeClass methods
		for (MethodDef methodDef : beforeClassMethods) {
			if (!overridenMethods.contains(methodDef.getName())) {
				result.addAll(methodDef.getCode());
			}
		}
		return result;
	}

	private String getVariableFromCurrentScope(VariableReference varRef) {
		if ((currentScope != TestScope.FIELDS)
		        && (currentScope != TestScope.STATICFIELDS)) {
			for (Map.Entry<String, VariableReference> entry : currentMethodVars.entrySet()) {
				if (entry.getValue().equals(varRef)) {
					return entry.getKey();
				}
			}
		}
		for (Map.Entry<String, VariableReference> entry : fieldVars.entrySet()) {
			if (entry.getValue().equals(varRef)) {
				return entry.getKey();
			}
		}
		if (parent != null) {
			return parent.getVariableFromCurrentScope(varRef);
		}
		return null;
	}

	private VariableReference getVariableReferenceInternally(IVariableBinding varBinding) {
		VariableReference varRef = currentMethodVars.get(varBinding.toString());
		if (varRef != null) {
			return varRef;
		}
		varRef = fieldVars.get(varBinding.toString());
		if (varRef != null) {
			return varRef;
		}
		if (parent != null) {
			return parent.getVariableReferenceInternally(varBinding);
		}
		return null;
	}
}
