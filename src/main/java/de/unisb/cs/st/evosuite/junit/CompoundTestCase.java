package de.unisb.cs.st.evosuite.junit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.IVariableBinding;

import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * A compound test case is a test case that is read from an existing JUnit test
 * file, and may have a complex structure. It contains static code sections,
 * fields, constructors, @BeforeClass, @Before, @After, @AfterClass annotated
 * methods and possibly a class hierarchy. A CompoundTestCase is used to gather
 * all those statements to eventually combine them into a normal
 * {@link TestCase}.
 * 
 * @author roessler
 * 
 */
public class CompoundTestCase {
	public static enum TestScope {
		BEFORE_CLASS, STATIC, BEFORE, FIELDS, CONSTRUCTOR, AFTER, AFTER_CLASS, METHOD;
	}

	private static final long serialVersionUID = 1L;

	private final Map<String, List<StatementInterface>> methods = new LinkedHashMap<String, List<StatementInterface>>();
	private final Map<IVariableBinding, VariableReference> currentMethodVars = new HashMap<IVariableBinding, VariableReference>();
	private final Map<IVariableBinding, VariableReference> fieldVars = new HashMap<IVariableBinding, VariableReference>();
	private final List<String> constructors = new ArrayList<String>();
	private final List<String> afterMethods = new ArrayList<String>();
	private final List<String> afterClassMethods = new ArrayList<String>();
	private final List<String> beforeMethods = new ArrayList<String>();
	private final List<String> beforeClassMethods = new ArrayList<String>();
	private final List<StatementInterface> fields = new ArrayList<StatementInterface>();
	private final List<StatementInterface> staticCode = new ArrayList<StatementInterface>();
	private final String testMethod;
	private CompoundTestCase parent;
	private TestScope currentScope = TestScope.FIELDS;
	private List<StatementInterface> currentMethod = new ArrayList<StatementInterface>();
	private String currentMethodName = null;

	private final DelegatingTestCase delegate;

	public CompoundTestCase(CompoundTestCase child) {
		child.parent = this;
		delegate = child.getReference();
		this.testMethod = null;
	}

	public CompoundTestCase(String methodName) {
		this.testMethod = methodName;
		delegate = new DelegatingTestCase();
	}

	public void addStatement(StatementInterface statement) {
		currentMethod.add(statement);
	}

	public void addVariable(IVariableBinding varBinding, VariableReference varRef) {
		if (currentScope == TestScope.FIELDS) {
			fieldVars.put(varBinding, varRef);
			return;
		}
		currentMethodVars.put(varBinding, varRef);
	}

	public void finalizeMethod() {
		methods.put(currentMethodName, currentMethod);
		currentMethod = new ArrayList<StatementInterface>();
		currentMethodVars.clear();
		switch (currentScope) {
		case AFTER:
			afterMethods.add(currentMethodName);
			break;
		case AFTER_CLASS:
			afterClassMethods.add(currentMethodName);
			break;
		case BEFORE:
			beforeMethods.add(currentMethodName);
			break;
		case BEFORE_CLASS:
			beforeClassMethods.add(currentMethodName);
			break;
		case CONSTRUCTOR:
			constructors.add(currentMethodName);
			break;
		}
		currentMethodName = null;
		currentScope = TestScope.FIELDS;
	}

	public TestCase finalizeTestCase() {
		delegate.setDelegate(new DefaultTestCase());
		delegate.addStatements(getInitializationCode());
		if (testMethod == null) {
			throw new RuntimeException("Test did not contain any statements!");
		}
		delegate.addStatements(methods.get(testMethod));
		delegate.addStatements(getDeinitializationCode());
		return delegate;
	}

	public CompoundTestCase getParent() {
		return parent;
	}

	public DelegatingTestCase getReference() {
		return delegate;
	}

	public VariableReference getVariableReference(IVariableBinding varBinding) {
		VariableReference varRef = currentMethodVars.get(varBinding);
		if (varRef != null) {
			return varRef;
		}
		varRef = fieldVars.get(varBinding);
		if (varRef != null) {
			return varRef;
		}
		if (parent != null) {
			return parent.getVariableReference(varBinding);
		}
		return null;
	}

	public void newMethod(String methodName) {
		assert currentMethod.isEmpty();
		assert currentMethodVars.isEmpty();
		currentMethodName = methodName;
		currentScope = TestScope.METHOD;
	}

	public void setCurrentScope(TestScope scope) {
		this.currentScope = scope;
	}

	public void variableAssignment(VariableReference varRef, VariableReference newAssignment) {
		for (Map.Entry<IVariableBinding, VariableReference> entry : currentMethodVars.entrySet()) {
			if (entry.getValue() == varRef) {
				currentMethodVars.put(entry.getKey(), newAssignment);
			}
		}
		for (Map.Entry<IVariableBinding, VariableReference> entry : fieldVars.entrySet()) {
			if (entry.getValue() == varRef) {
				fieldVars.put(entry.getKey(), newAssignment);
			}
		}
	}

	private List<StatementInterface> allStatementsFrom(List<String> methodsToAdd) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		for (String method : methodsToAdd) {
			result.addAll(methods.get(method));
		}
		return result;
	}

	private List<StatementInterface> getDeinitializationCode() {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		result.addAll(allStatementsFrom(afterMethods));
		result.addAll(allStatementsFrom(afterClassMethods));
		if (parent != null) {
			List<StatementInterface> parentStatements = parent.getDeinitializationCode();
			result.addAll(parentStatements);
		}
		return result;
	}

	private List<StatementInterface> getInitializationCode() {
		// According to Kent Beck, there is no defined order
		// in which @Before and @BeforeClass methods are called:
		// http://tech.groups.yahoo.com/group/junit/message/20758
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			List<StatementInterface> parentStatements = parent.getInitializationCode();
			result.addAll(parentStatements);
		}
		result.addAll(allStatementsFrom(beforeClassMethods));
		result.addAll(staticCode);
		result.addAll(fields);
		if (constructors.size() > 1) {
			throw new RuntimeException("Found " + constructors.size()
					+ " constructors, but can only use default constructor!");
		}
		result.addAll(allStatementsFrom(constructors));
		result.addAll(allStatementsFrom(beforeMethods));
		return result;
	}
}
