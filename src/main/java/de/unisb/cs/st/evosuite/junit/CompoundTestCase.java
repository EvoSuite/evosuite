package de.unisb.cs.st.evosuite.junit;

import java.util.ArrayList;
import java.util.HashMap;
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
		BEFORE_CLASS, STATIC, BEFORE, FIELDS, CONSTRUCTOR, TEST_METHOD, AFTER, AFTER_CLASS, IGNORE;
	}

	private static final long serialVersionUID = 1L;

	// TODO We need to have this as maps, mapping method names to list<statement>
	// TODO Find out in which order @Before and @BeforeClass methods are called
	private final List<StatementInterface> after = new ArrayList<StatementInterface>();
	private final List<StatementInterface> afterClass = new ArrayList<StatementInterface>();
	private final List<StatementInterface> before = new ArrayList<StatementInterface>();
	private final List<StatementInterface> beforeClass = new ArrayList<StatementInterface>();
	private final List<StatementInterface> constructor = new ArrayList<StatementInterface>();
	private final List<StatementInterface> fields = new ArrayList<StatementInterface>();
	private final List<StatementInterface> staticCode = new ArrayList<StatementInterface>();
	private final List<StatementInterface> testMethod = new ArrayList<StatementInterface>();
	private final Map<IVariableBinding, VariableReference> methodVars = new HashMap<IVariableBinding, VariableReference>();
	private final Map<IVariableBinding, VariableReference> fieldVars = new HashMap<IVariableBinding, VariableReference>();
	private CompoundTestCase parent;
	private TestScope currentScope = TestScope.FIELDS;

	private final DelegatingTestCase delegate;

	public CompoundTestCase() {
		delegate = new DelegatingTestCase();
	}

	public CompoundTestCase(CompoundTestCase parent) {
		this.parent = parent;
		delegate = parent.getReference();
	}

	public void addStatement(StatementInterface statement) {
		switch (currentScope) {
		case BEFORE_CLASS:
			beforeClass.add(statement);
			return;
		case STATIC:
			staticCode.add(statement);
			return;
		case BEFORE:
			before.add(statement);
			return;
		case CONSTRUCTOR:
			constructor.add(statement);
			return;
		case FIELDS:
			fields.add(statement);
			return;
		case TEST_METHOD:
			testMethod.add(statement);
			return;
		case AFTER_CLASS:
			afterClass.add(statement);
			return;
		case AFTER:
			after.add(statement);
			return;
		case IGNORE:
			return;
		default:
			throw new RuntimeException("Scope " + currentScope + " not considered!");
		}
	}

	public void addVariable(IVariableBinding varBinding, VariableReference varRef) {
		if (currentScope == TestScope.FIELDS) {
			fieldVars.put(varBinding, varRef);
			return;
		}
		methodVars.put(varBinding, varRef);
	}

	public TestCase finalizeTestCase() {
		delegate.setDelegate(new DefaultTestCase());
		delegate.addStatements(getInitializationCode());
		if (testMethod.isEmpty()) {
			throw new RuntimeException("Test did not contain any statements!");
		}
		delegate.addStatements(testMethod);
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
		VariableReference varRef = methodVars.get(varBinding);
		if (varRef != null) {
			return varRef;
		}
		return fieldVars.get(varBinding);
	}

	public void setCurrentScope(TestScope scope) {
		this.currentScope = scope;
		methodVars.clear();
	}

	public void variableAssignment(VariableReference varRef, VariableReference newAssignment) {
		for (Map.Entry<IVariableBinding, VariableReference> entry : methodVars.entrySet()) {
			if (entry.getValue() == varRef) {
				methodVars.put(entry.getKey(), newAssignment);
			}
		}
		for (Map.Entry<IVariableBinding, VariableReference> entry : fieldVars.entrySet()) {
			if (entry.getValue() == varRef) {
				fieldVars.put(entry.getKey(), newAssignment);
			}
		}
	}

	private List<StatementInterface> getDeinitializationCode() {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		result.addAll(after);
		result.addAll(afterClass);
		if (parent != null) {
			List<StatementInterface> parentStatements = parent.getDeinitializationCode();
			result.addAll(parentStatements);
		}
		return result;
	}

	private List<StatementInterface> getInitializationCode() {
		List<StatementInterface> result = new ArrayList<StatementInterface>();
		if (parent != null) {
			List<StatementInterface> parentStatements = parent.getInitializationCode();
			result.addAll(parentStatements);
		}
		result.addAll(staticCode);
		result.addAll(fields);
		result.addAll(constructor);
		result.addAll(beforeClass);
		result.addAll(before);
		return result;
	}
}
