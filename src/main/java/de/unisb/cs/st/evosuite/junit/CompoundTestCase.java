package de.unisb.cs.st.evosuite.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		BEFORE_CLASS, STATIC, STATICFIELDS, BEFORE, FIELDS, CONSTRUCTOR, AFTER, AFTER_CLASS, METHOD;
	}

	private static final long serialVersionUID = 1L;

	private final Map<String, List<StatementInterface>> methods = new LinkedHashMap<String, List<StatementInterface>>();
	private final Map<String, VariableReference> currentMethodVars = new HashMap<String, VariableReference>();
	private final Map<String, VariableReference> fieldVars = new HashMap<String, VariableReference>();
	private final List<String> constructors = new ArrayList<String>();
	private final List<String> afterMethods = new ArrayList<String>();
	private final List<String> afterClassMethods = new ArrayList<String>();
	private final List<String> beforeMethods = new ArrayList<String>();
	private final List<String> beforeClassMethods = new ArrayList<String>();
	private final List<StatementInterface> staticFields = new ArrayList<StatementInterface>();
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

	public void addVariable(IVariableBinding varBinding, VariableReference varRef) {
		if ((currentScope == TestScope.FIELDS) || (currentScope == TestScope.STATICFIELDS)) {
			fieldVars.put(varBinding.toString(), varRef);
			return;
		}
		currentMethodVars.put(varBinding.toString(), varRef);
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
		Set<String> overridenMethods = Collections.emptySet();
		delegate.setDelegate(new DefaultTestCase());
		delegate.addStatements(getStaticInitializationBeforeClassMethods(overridenMethods));
		delegate.addStatements(getInitializationCode());
		delegate.addStatements(getBeforeMethods(overridenMethods));
		if (testMethod == null) {
			throw new RuntimeException("Test did not contain any statements!");
		}
		delegate.addStatements(methods.get(testMethod));
		delegate.addStatements(getAfterMethods(overridenMethods));
		delegate.addStatements(getAfterClassMethods(overridenMethods));
		return delegate;
	}

	public TestScope getCurrentScope() {
		return currentScope;
	}

	public CompoundTestCase getParent() {
		return parent;
	}

	public DelegatingTestCase getReference() {
		return delegate;
	}

	public VariableReference getVariableReference(IVariableBinding varBinding) {
		VariableReference varRef = currentMethodVars.get(varBinding.toString());
		if (varRef != null) {
			return varRef;
		}
		varRef = fieldVars.get(varBinding.toString());
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

	private List<StatementInterface> getAfterClassMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		for (String method : afterClassMethods) {
			if (!overridenMethods.contains(method)) {
				result.addAll(methods.get(method));
			}
		}
		if (parent != null) {
			// @AfterClass IF NOT OVERRIDEN
			// According to Kent Beck, there is no defined order
			// in which methods of the same leve within one class are called:
			// http://tech.groups.yahoo.com/group/junit/message/20758
			result.addAll(parent.getAfterClassMethods(methods.keySet()));
		}
		return result;
	}

	private List<StatementInterface> getAfterMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		// @After
		for (String method : afterMethods) {
			if (!overridenMethods.contains(method)) {
				result.addAll(methods.get(method));
			}
		}
		if (parent != null) {
			// parent: @After
			result.addAll(parent.getAfterMethods(methods.keySet()));
		}
		return result;
	}

	private List<StatementInterface> getBeforeMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			result.addAll(parent.getBeforeMethods(methods.keySet()));
		}
		// @Before IF NOT OVERRIDEN
		// According to Kent Beck, there is no defined order
		// in which methods of the same leve within one class are called:
		// http://tech.groups.yahoo.com/group/junit/message/20758
		for (String method : beforeMethods) {
			if (!overridenMethods.contains(method)) {
				result.addAll(methods.get(method));
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
		List<StatementInterface> constructor = Collections.emptyList();
		if (constructors.size() > 1) {
			// TODO Find no-args constructor
			throw new RuntimeException("Found " + constructors.size()
					+ " constructors, but can only use default constructor!");
		} else if (constructors.size() == 1) {
			constructor = methods.get(constructors.get(0));
		}
		result.addAll(constructor);
		return result;
	}

	private List<StatementInterface> getStaticInitializationBeforeClassMethods(Set<String> overridenMethods) {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			// @BeforeClass IF NOT OVERRIDEN
			// According to Kent Beck, there is no defined order
			// in which methods of the same leve within one class are called:
			// http://tech.groups.yahoo.com/group/junit/message/20758
			result.addAll(parent.getStaticInitializationBeforeClassMethods(methods.keySet()));
		}
		// this: static initialization
		result.addAll(staticFields);
		// static blocks
		result.addAll(staticCode);
		// @BeforeClass methods
		for (String method : beforeClassMethods) {
			if (!overridenMethods.contains(method)) {
				result.addAll(methods.get(method));
			}
		}
		return result;
	}
}
