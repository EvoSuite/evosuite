/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.evosuite.TestGenerationContext;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.junit.CompoundTestCase.MethodDef;
import org.evosuite.junit.CompoundTestCase.ReturnStatementPlaceholder;
import org.evosuite.junit.CompoundTestCase.TestScope;
import org.evosuite.junit.TestRuntimeValuesDeterminer.CursorableTrace;
import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.BooleanPrimitiveStatement;
import org.evosuite.testcase.BytePrimitiveStatement;
import org.evosuite.testcase.CharPrimitiveStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DoublePrimitiveStatement;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.FloatPrimitiveStatement;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.LongPrimitiveStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.PrimitiveExpression;
import org.evosuite.testcase.PrimitiveExpression.Operator;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.ShortPrimitiveStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.StringPrimitiveStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testcase.VariableReferenceImpl;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;

/**
 * This class implements the Eclipse JDT Visitor to turn an existing test case
 * (in source code form) into an EvoSuite {@link TestCase}.
 * 
 * @author roessler
 */
public class TestExtractingVisitor extends ASTVisitor {

	public static interface TestReader {
		int getLineNumber(int sourcePos);

		CompoundTestCase readTestCase(String clazz, CompoundTestCase reference);
	}

	private static class BoundVariableReferenceImpl extends VariableReferenceImpl {
		private static final long serialVersionUID = 1L;

		protected String name;

		public BoundVariableReferenceImpl(CompoundTestCase testCase,
		        java.lang.reflect.Type type, String name) {
			super(testCase.getReference(), type);
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getStPosition() {
			if (((DelegatingTestCase) testCase).isFinished()) {
				return super.getStPosition();
			}
			return -1;
		}
	}

	private static class ValidArrayReference extends ArrayReference {
		private static final long serialVersionUID = 1L;

		public ValidArrayReference(DelegatingTestCase testCase, GenericClass clazz,
		        int[] lengths) {
			super(testCase, clazz, lengths);
		}

		public ValidArrayReference(TestCase tc, Class<?> clazz) {
			super(tc, clazz);
		}

		@Override
		public int getStPosition() {
			if (((DelegatingTestCase) testCase).isFinished()) {
				return super.getStPosition();
			}
			return -1;
		}
	}

	private static class ValidConstructorStatement extends ConstructorStatement {
		private static final long serialVersionUID = 1L;

		public ValidConstructorStatement(TestCase tc, Constructor<?> constructor,
		        java.lang.reflect.Type type, List<VariableReference> parameters) {
			super(tc, new GenericConstructor(constructor, type), parameters);
		}

		public ValidConstructorStatement(TestCase tc, Constructor<?> constructor,
		        VariableReference retVal, List<VariableReference> parameters) {
			super(tc, new GenericConstructor(constructor, retVal.getType()), retVal,
			        parameters, false);
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}

	private static class ValidMethodStatement extends MethodStatement {
		private static final long serialVersionUID = 1L;

		public ValidMethodStatement(TestCase tc, Method method, VariableReference callee,
		        java.lang.reflect.Type type, List<VariableReference> parameters) {
			super(tc, new GenericMethod(method, callee.getType()), callee, parameters);
		}

		public ValidMethodStatement(TestCase tc, Method method, VariableReference callee,
		        VariableReference retVal, List<VariableReference> parameters) {
			super(tc, new GenericMethod(method, callee.getType()), callee, retVal,
			        parameters);
		}

		@Override
		public StatementInterface copy(TestCase newTestCase, int offset) {
			// Code was partly copied from MethodStatement
			ArrayList<VariableReference> new_params = new ArrayList<VariableReference>();
			for (VariableReference r : parameters) {
				new_params.add(r.copy(newTestCase, offset));
			}
			if (method.isStatic()) {
				// TODO: If callee is an array index, this will return an
				// invalid copy of the cloned variable!
				return new ValidMethodStatement(newTestCase, method.getMethod(), null,
				        retval.getType(), new_params);
			}
			VariableReference newCallee = callee.copy(newTestCase, offset);
			return new MethodStatement(newTestCase, method, newCallee, new_params);
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}

	private static class ValidVariableReference extends VariableReferenceImpl {

		private static final long serialVersionUID = -59873293582106016L;

		// private static int counter = 0;

		public ValidVariableReference(DelegatingTestCase testCase,
		        java.lang.reflect.Type type) {
			super(testCase, type);
			// System.out.println("Created ValidVariableReference #" + counter++
			// + " of type " + type);
		}

		@Override
		public int getStPosition() {
			if (((DelegatingTestCase) testCase).isFinished()) {
				return super.getStPosition();
			}
			return -1;
		}
	}

	private static class WrongMethodBindingException extends Exception {
		private static final long serialVersionUID = 1L;

		public WrongMethodBindingException() {
			// TODO Auto-generated constructor stub
		}
	}

	private static Class<?> toClass(java.lang.reflect.Type type) {
		if (type instanceof java.lang.reflect.ParameterizedType) {
			return (Class<?>) ((java.lang.reflect.ParameterizedType) type).getRawType();
		}
		return (Class<?>) type;
	}

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestExtractingVisitor.class);
	private final CompoundTestCase testCase;
	private final TestReader testReader;
	private final String unqualifiedTest;
	private final String unqualifiedTestMethod;
	private final Stack<VariableReference> nestedCallResults = new Stack<VariableReference>();
	private final Map<String, String> imports = new HashMap<String, String>();
	private final TestRuntimeValuesDeterminer testValuesDeterminer;
	private final Map<String, VariableReference> calleeResultMap = new HashMap<String, VariableReference>();

	private boolean exceptionReadingMethod = false;
	private CursorableTrace cursorableTrace;

	// TODO Remove the following two:
	private final Stack<Integer> iterations = new Stack<Integer>();
	private final Stack<Integer> loopExecCnts = new Stack<Integer>();

	// TODO This is bad practice: here we rely on a global variable
	// for something that should be parameters to the methods!
	private Integer lineNumber = null;

	private static final HashSet<Class<?>> PRIMITIVE_CLASSES = new HashSet<Class<?>>();
	static {
		PRIMITIVE_CLASSES.add(Long.class);
		PRIMITIVE_CLASSES.add(Integer.class);
		PRIMITIVE_CLASSES.add(Short.class);
		PRIMITIVE_CLASSES.add(Byte.class);
		PRIMITIVE_CLASSES.add(Boolean.class);
		PRIMITIVE_CLASSES.add(Character.class);
		PRIMITIVE_CLASSES.add(Double.class);
		PRIMITIVE_CLASSES.add(Float.class);
		PRIMITIVE_CLASSES.add(String.class);
	}

	private final static HashMap<Character, Class<?>> PRIMITIVE_SIGNATURE_MAPPING = new HashMap<Character, Class<?>>();
	static {
		PRIMITIVE_SIGNATURE_MAPPING.put('B', Byte.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('C', Character.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('D', Double.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('F', Float.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('I', Integer.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('J', Long.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('S', Short.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('V', Void.TYPE);
		PRIMITIVE_SIGNATURE_MAPPING.put('Z', Boolean.TYPE);
	}

	private final static HashMap<String, Class<?>> PRIMITIVE_TYPECODE_MAPPING = new HashMap<String, Class<?>>();
	static {
		PRIMITIVE_TYPECODE_MAPPING.put("byte", Byte.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("char", Character.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("double", Double.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("float", Float.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("int", Integer.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("long", Long.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("short", Short.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("void", Void.TYPE);
		PRIMITIVE_TYPECODE_MAPPING.put("boolean", Boolean.TYPE);
	}

	/**
	 * <p>
	 * Constructor for TestExtractingVisitor.
	 * </p>
	 * 
	 * @param testCase
	 *            a {@link org.evosuite.junit.CompoundTestCase} object.
	 * @param testClass
	 *            a {@link java.lang.String} object.
	 * @param testMethod
	 *            a {@link java.lang.String} object.
	 * @param testReader
	 *            a {@link org.evosuite.junit.TestExtractingVisitor.TestReader}
	 *            object.
	 */
	public TestExtractingVisitor(CompoundTestCase testCase, String testClass,
	        String testMethod, TestReader testReader) {
		super();
		this.testCase = testCase;
		this.testReader = testReader;
		this.unqualifiedTest = testClass.substring(testClass.lastIndexOf(".") + 1,
		                                           testClass.length());
		this.unqualifiedTestMethod = testMethod;
		this.testValuesDeterminer = TestRuntimeValuesDeterminer.getInstance(testClass);
		testValuesDeterminer.determineRuntimeValues();
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Assignment assignment) {
		if ((assignment.getRightHandSide() instanceof MethodInvocation)
		        || (assignment.getRightHandSide() instanceof ClassInstanceCreation)) {
			// treated in respective endVisit methods
			return;
		}
		VariableReference varRef = retrieveVariableReference(assignment.getLeftHandSide(),
		                                                     null);
		varRef.setOriginalCode(assignment.getLeftHandSide().toString());
		VariableReference newAssignment = retrieveVariableReference(assignment.getRightHandSide(),
		                                                            null);
		newAssignment.setOriginalCode(assignment.getRightHandSide().toString());
		if (varRef instanceof ArrayIndex) {
			AssignmentStatement assignmentStatement = new AssignmentStatement(
			        testCase.getReference(), varRef, newAssignment);
			testCase.addStatement(assignmentStatement);
			return;
		}
		testCase.variableAssignment(varRef, newAssignment);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Block node) {
		if (testCase.getCurrentScope() == TestScope.STATIC) {
			testCase.setCurrentScope(TestScope.FIELDS);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ClassInstanceCreation instanceCreation) {
		if (instanceCreation.getParent() instanceof ThrowStatement) {
			logger.warn("Ignoring throw statements!");
			return;
		}
		List<?> paramTypes = Arrays.asList(instanceCreation.resolveConstructorBinding().getParameterTypes());
		List<?> paramValues = instanceCreation.arguments();
		Constructor<?> constructor = retrieveConstructor(instanceCreation.getType(),
		                                                 paramTypes, paramValues);
		List<VariableReference> params = convertParams(instanceCreation.arguments(),
		                                               paramTypes);
		VariableReference retVal = retrieveVariableReference(instanceCreation, null);
		retVal.setOriginalCode(instanceCreation.toString());
		ConstructorStatement statement = new ValidConstructorStatement(
		        testCase.getReference(), constructor, retVal, params);
		testCase.addStatement(statement);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ConditionalExpression node) {
		// TODO-JRO Implement method endVisit
		logger.warn("Method endVisit not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(EnhancedForStatement node) {
		// TODO-JRO Implement method endVisit
		logger.warn("Method endVisit not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(IfStatement node) {
		// TODO-JRO Implement method endVisit
		logger.warn("Method endVisit not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MethodDeclaration node) {
		logger.debug("Finished extracting method {}. It caused {} exception.",
		             testCase.getCurrentMethod(), exceptionReadingMethod ? "an" : "no");
		if (exceptionReadingMethod) {
			testCase.discardMethod();
			return;
		}
		testCase.finalizeMethod();
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void endVisit(MethodInvocation methodInvocation) {
		// TODO If in constructor, treat calls to this() and super().
		String methodName = methodInvocation.getName().toString();
		if (methodName.equals("fail") || methodName.startsWith("assert")) {
			logger.warn("We are ignoring fail and assert statements for now.");
			for (Expression expression : (List<Expression>) methodInvocation.arguments()) {
				if ((expression instanceof MethodInvocation)
				        || (expression instanceof ClassInstanceCreation)) {
					assert !nestedCallResults.isEmpty();
					nestedCallResults.pop();
				}
			}
			return;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			throw new RuntimeException("JDT was unable to resolve method for "
			        + methodInvocation);
		}
		List<?> paramTypes = Arrays.asList(methodBinding.getParameterTypes());
		List<VariableReference> params = convertParams(methodInvocation.arguments(),
		                                               paramTypes);
		Method method = retrieveMethod(methodInvocation, methodBinding, params);
		/*
		Class<?> declaringClass = method.getDeclaringClass();
		if (false) {
			// TODO Methods can be declared in an order such that the called
			// method is not yet read
			if (testCase.getClassName().equals(declaringClass.getName())
			        || testCase.isDescendantOf(declaringClass)) {
				MethodDef methodDef = testCase.getMethod(method.getName());
				VariableReference retVal = retrieveResultReference(methodInvocation);
				retVal.setOriginalCode(methodInvocation.toString());
				testCase.convertMethod(methodDef, params, retVal);
				return;
			}
		}
		*/
		VariableReference callee = null;
		if (!Modifier.isStatic(method.getModifiers())) {
			callee = retrieveCalleeReference(methodInvocation);
		}
		MethodStatement methodStatement = null;
		ASTNode parent = methodInvocation.getParent();
		if (parent instanceof ExpressionStatement) {
			VariableReference retVal = new ValidVariableReference(
			        testCase.getReference(), method.getReturnType());
			retVal.setOriginalCode(methodInvocation.toString());
			methodStatement = new ValidMethodStatement(testCase.getReference(), method,
			        callee, retVal, params);
			testCase.addStatement(methodStatement);
			return;
		}
		VariableReference retVal = retrieveResultReference(methodInvocation);
		retVal.setOriginalCode(methodInvocation.toString());
		methodStatement = new ValidMethodStatement(testCase.getReference(), method,
		        callee, retVal, params);
		if (!(parent instanceof Block)) {
			nestedCallResults.push(retVal);
		}
		testCase.addStatement(methodStatement);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ReturnStatement returnStatement) {
		VariableReference returnValue = null;
		if (returnStatement.getExpression() == null) {
			logger.warn("Since we do not represent control structures, we ignore explicit empty return statements ('return;').");
			return;
		}
		if (returnStatement.getExpression() instanceof MethodInvocation) {
			returnValue = testCase.getLastStatement().getReturnValue();
		} else {
			returnValue = retrieveVariableReference(returnStatement.getExpression(), null);
		}
		returnValue.setOriginalCode(returnStatement.toString());
		ReturnStatementPlaceholder returnStmt = new ReturnStatementPlaceholder(
		        testCase.getReference(), returnValue);
		testCase.addStatement(returnStmt);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SuperMethodInvocation superMethodInvocation) {
		List<?> paramTypes = Arrays.asList(superMethodInvocation.resolveMethodBinding().getParameterTypes());
		List<VariableReference> params = convertParams(superMethodInvocation.arguments(),
		                                               paramTypes);
		String name = superMethodInvocation.getName().getIdentifier();
		MethodDef methodDef = testCase.getParent().getMethod(name);
		VariableReference retVal = retrieveResultReference(superMethodInvocation);
		retVal.setOriginalCode(superMethodInvocation.toString());
		testCase.convertMethod(methodDef, params, retVal);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SwitchCase node) {
		// TODO-JRO Implement method endVisit
		logger.warn("Method endVisit not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SwitchStatement node) {
		// TODO-JRO Implement method endVisit
		logger.warn("Method endVisit not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(WhileStatement node) {
		// TODO-JRO Implement method endVisit
		logger.warn("Method endVisit not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ArrayCreation arrayCreation) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(Block node) {
		if (testCase.getCurrentScope() == TestScope.FIELDS) {
			testCase.setCurrentScope(TestScope.STATIC);
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(CatchClause node) {
		logger.warn("We are ignoring catch-clauses for now!");
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		if (Modifier.isStatic(fieldDeclaration.getModifiers())) {
			testCase.setCurrentScope(TestScope.STATICFIELDS);
		}
		VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		Expression expression = varDeclFrgmnt.getInitializer();
		VariableReference varRef;
		if (expression == null) {
			varRef = retrieveDefaultValueAssignment(retrieveTypeClass(varDeclFrgmnt));
		} else {
			varRef = retrieveVariableReference(expression, null);
		}
		varRef.setOriginalCode(fieldDeclaration.toString());
		// TODO Use the name here as well?
		// String name = varDeclFrgmt.getName().getIdentifier();
		// new BoundVariableReferenceImpl(testCase, varType, name);
		testCase.addVariable(varDeclFrgmnt.resolveBinding(), varRef);
		testCase.setCurrentScope(TestScope.FIELDS);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ForStatement forStatement) {
		int lineNumber = testReader.getLineNumber(forStatement.getBody().getStartPosition());
		// TODO This works here, but not if inner loop is a while statement
		// that iterates a different number of times...!
		// Solution: follow the trace

		// loop head is executed 1 times more than the body...
		int loopExecCnt = testValuesDeterminer.getExecutionCount(lineNumber) - 1;
		if (loopExecCnts.size() > 0) {
			for (Integer outerLoopExecCnt : loopExecCnts) {
				// loop head is executed 1 times more than the body...
				loopExecCnt = (loopExecCnt - 1) / outerLoopExecCnt;
			}
		}
		loopExecCnts.push(loopExecCnt);
		int idx = 0;
		for (; idx < loopExecCnt; idx++) {
			iterations.push(idx);
			if ((idx > 0) && (cursorableTrace != null)) {
				cursorableTrace.advanceLoop();
			}
			acceptChildren(forStatement, forStatement.initializers());
			acceptChild(forStatement, forStatement.getExpression());
			acceptChildren(forStatement, forStatement.updaters());
			acceptChild(forStatement, forStatement.getBody());
			iterations.pop();
		}
		iterations.push(idx);
		acceptChildren(forStatement, forStatement.initializers());
		acceptChild(forStatement, forStatement.getExpression());
		acceptChildren(forStatement, forStatement.updaters());
		iterations.pop();
		loopExecCnts.pop();
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ImportDeclaration importDeclaration) {
		String[] importParts = importDeclaration.toString().split(" ");
		String fullImport = importParts[importParts.length - 1].replace(";", "").trim();
		if (fullImport.contains("$")) {
			imports.put(fullImport.split("$")[1], fullImport);
		} else {
			String[] identifiers = fullImport.split("\\.");
			imports.put(identifiers[identifiers.length - 1], fullImport);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MarkerAnnotation markerAnnotation) {
		String annotation = markerAnnotation.toString();
		if (annotation.equals("@BeforeClass")) {
			testCase.setCurrentScope(TestScope.BEFORE_CLASS);
		}
		if (annotation.equals("@Before")) {
			testCase.setCurrentScope(TestScope.BEFORE);
		}
		if (annotation.equals("@AfterClass")) {
			testCase.setCurrentScope(TestScope.AFTER_CLASS);
		}
		if (annotation.equals("@After")) {
			testCase.setCurrentScope(TestScope.AFTER);
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		String currentMethodName = methodDeclaration.getName().getIdentifier();
		logger.debug("Extracting method {}.", currentMethodName);
		testCase.newMethod(currentMethodName);
		if ((unqualifiedTestMethod == null)
		        || !unqualifiedTestMethod.equals(currentMethodName)) {
			if (methodDeclaration.getName().getIdentifier().equals(unqualifiedTest)) {
				testCase.setCurrentScope(TestScope.CONSTRUCTOR);
			}
			if (methodDeclaration.getName().getIdentifier().equals("setUp")) {
				testCase.setCurrentScope(TestScope.BEFORE);
			}
			if (methodDeclaration.getName().getIdentifier().equals("tearDown")) {
				testCase.setCurrentScope(TestScope.AFTER);
			}
		}
		cursorableTrace = testValuesDeterminer.getMethodTrace(currentMethodName);
		calleeResultMap.clear();
		return saveMethodCodeExtraction(methodDeclaration);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SingleVariableDeclaration variableDeclaration) {
		VariableReference varRef = retrieveVariableReference(variableDeclaration);
		varRef.setOriginalCode(variableDeclaration.toString());
		testCase.addParameter(varRef);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (!unqualifiedTest.equals(typeDeclaration.getName().getIdentifier())) {
			logger.warn("Type declarations are ignored for other types than the actual test!");
			return false;
		}
		Type supertype = typeDeclaration.getSuperclassType();
		if (supertype == null) {
			return true;
		}
		Class<?> supertypeClass = retrieveTypeClass(supertype);
		if (supertypeClass == null) {
			return true;
		}
		String superclass = retrieveTypeClass(supertype).getName();
		if (superclass == null) {
			return true;
		}
		if (superclass.startsWith("org.junit.") || //
		        superclass.startsWith("junit.") || //
		        superclass.startsWith("java.")) {
			return true;
		}
		testReader.readTestCase(superclass, testCase);
		return true;
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean visit(VariableDeclarationStatement variableDeclStmt) {
		Class<?> varType = retrieveTypeClass(variableDeclStmt.getType());
		if (varType.isPrimitive() || PRIMITIVE_CLASSES.contains(varType)) {
			// Can only happen to primitive types and String,
			// otherwise it is a constructor call which is handled elsewhere
			logger.debug("Variable has not been treated elsewhere...");
			VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) variableDeclStmt.fragments().get(0);
			Expression expression = varDeclFrgmnt.getInitializer();
			VariableReference varRef = retrieveVariableReference(expression, null);
			varRef.setOriginalCode(variableDeclStmt.toString());
			// TODO Use the name here as well?
			// String name = varDeclFrgmt.getName().getIdentifier();
			// new BoundVariableReferenceImpl(testCase, varType, name);
			testCase.addVariable(varDeclFrgmnt.resolveBinding(), varRef);
			return true;
		}
		if (varType.isArray()) {
			// if (varType.getComponentType().isPrimitive() ||
			// varType.getComponentType().equals(String.class)) {
			// ... or to primitive and string arrays
			VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) variableDeclStmt.fragments().get(0);
			Expression expression = varDeclFrgmnt.getInitializer();
			if (expression instanceof ArrayInitializer) {
				ArrayReference arrayReference = new ValidArrayReference(
				        testCase.getReference(), varType);
				ArrayStatement arrayStatement = new ArrayStatement(
				        testCase.getReference(), arrayReference);
				arrayReference.setOriginalCode(variableDeclStmt.toString());
				testCase.addStatement(arrayStatement);
				testCase.addVariable(varDeclFrgmnt.resolveBinding(), arrayReference);
				ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
				for (int idx = 0; idx < arrayInitializer.expressions().size(); idx++) {
					Expression expr = (Expression) arrayInitializer.expressions().get(idx);
					VariableReference valueRef;
					if (expr instanceof NumberLiteral) {
						valueRef = retrieveVariableReference((NumberLiteral) expr,
						                                     varType.getComponentType());
					} else if (expr instanceof PrefixExpression) {
						valueRef = retrieveVariableReference((PrefixExpression) expr,
						                                     varType.getComponentType());
					} else {
						valueRef = retrieveVariableReference(expr, null);
					}
					valueRef.setOriginalCode(expr.toString());
					VariableReference arrayElementRef = new ArrayIndex(
					        testCase.getReference(), arrayReference, idx);
					arrayElementRef.setOriginalCode(expr.toString());
					arrayStatement.getVariableReferences().add(arrayElementRef);
					AssignmentStatement arrayAssignment = new AssignmentStatement(
					        testCase.getReference(), arrayElementRef, valueRef);
					testCase.addStatement(arrayAssignment);
				}
				// }
				return true;
			}
			if (expression instanceof ArrayCreation) {
				ArrayCreation arrayCreation = ((ArrayCreation) expression);
				List paramTypes = new ArrayList();
				for (int idx = 0; idx < arrayCreation.dimensions().size(); idx++) {
					paramTypes.add(int.class);
				}
				List<VariableReference> lengthsVarRefs = convertParams(arrayCreation.dimensions(),
				                                                       paramTypes);
				ArrayReference arrayReference = new ValidArrayReference(
				        testCase.getReference(), varType);
				arrayReference.setOriginalCode(variableDeclStmt.toString());
				ArrayStatement arrayStatement = new ArrayStatement(
				        testCase.getReference(), arrayReference);
				arrayStatement.setLengths(getLengths(variableDeclStmt, lengthsVarRefs));
				testCase.addVariable(varDeclFrgmnt.resolveBinding(),
				                     arrayStatement.getReturnValue());
				testCase.addStatement(arrayStatement);
			}
		}
		return true;
	}

	/**
	 * <p>
	 * extractArgumentClasses
	 * </p>
	 * 
	 * @param arguments
	 *            a {@link java.util.List} object.
	 * @return an array of {@link java.lang.Class} objects.
	 */
	protected Class<?>[] extractArgumentClasses(List<?> arguments) {
		Class<?>[] argClasses = new Class<?>[arguments.size()];
		for (int idx = 0; idx < arguments.size(); idx++) {
			Object arg = arguments.get(idx);
			argClasses[idx] = retrieveTypeClass(arg);
		}
		return argClasses;
	}

	/**
	 * <p>
	 * retrieveConstructor
	 * </p>
	 * 
	 * @param type
	 *            a {@link org.eclipse.jdt.core.dom.Type} object.
	 * @param argumentTypes
	 *            a {@link java.util.List} object.
	 * @param arguments
	 *            a {@link java.util.List} object.
	 * @return a {@link java.lang.reflect.Constructor} object.
	 */
	protected Constructor<?> retrieveConstructor(Type type, List<?> argumentTypes,
	        List<?> arguments) {
		Class<?>[] argClasses = extractArgumentClasses(argumentTypes);
		Constructor<?> constructor = null;
		Class<?> clazz = retrieveTypeClass(type);
		try {
			constructor = clazz.getConstructor(argClasses);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		return constructor;
	}

	/**
	 * <p>
	 * retrieveTypeClass
	 * </p>
	 * 
	 * @param argument
	 *            a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Class} object.
	 */
	protected Class<?> retrieveTypeClass(Object argument) {
		assert argument != null;
		if (argument instanceof SimpleType) {
			SimpleType simpleType = (SimpleType) argument;
			return retrieveTypeClass(simpleType);
		}
		if (argument instanceof ITypeBinding) {
			ITypeBinding binding = (ITypeBinding) argument;
			return retrieveTypeClass(binding);
		}
		if (argument instanceof IVariableBinding) {
			IVariableBinding variableBinding = (IVariableBinding) argument;
			return retrieveTypeClass(variableBinding.getType());
		}
		if (argument instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) argument;
			return retrieveTypeClass(simpleName.resolveBinding());
		}
		if (argument instanceof StringLiteral) {
			return String.class;
		}
		if (argument instanceof NumberLiteral) {
			return retrieveTypeClass((NumberLiteral) argument);
		}
		if (argument instanceof PrimitiveType) {
			PrimitiveType primitiveType = (PrimitiveType) argument;
			String typeCode = primitiveType.getPrimitiveTypeCode().toString();
			Class<?> result = PRIMITIVE_TYPECODE_MAPPING.get(typeCode);
			assert result != null : "Could not resolve typecode " + typeCode + ".";
			return result;
		}
		if (argument instanceof ArrayType) {
			ArrayType arrayType = (ArrayType) argument;
			return retrieveTypeClass(arrayType);
		}
		if (argument instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) argument;
			return retrieveTypeClass(parameterizedType.getType());
		}
		if (argument instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) argument;
			return retrieveTypeClass(varDeclFrgmnt.resolveBinding());
		}
		if (argument instanceof InfixExpression) {
			InfixExpression infixExpr = (InfixExpression) argument;
			ITypeBinding refTypeBinding = infixExpr.resolveTypeBinding();
			if (refTypeBinding != null) {
				return retrieveTypeClass(refTypeBinding);
			} else {
				throw new RuntimeException(
				        "Could not determine type class of infix expression '"
				                + infixExpr + "'.");
			}
		}
		if (argument instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) argument;
			ITypeBinding typeBinding = methodInvocation.resolveTypeBinding();
			if (typeBinding != null) {
				return retrieveTypeClass(typeBinding);
			}
			Expression typeExpression = methodInvocation.getExpression();
			if (typeExpression instanceof MethodInvocation) {
				MethodInvocation parentMethodInvocation = (MethodInvocation) typeExpression;
				IMethodBinding parentMethodBinding = parentMethodInvocation.resolveMethodBinding();
				return retrieveTypeClass(parentMethodBinding.getDeclaringClass());
			} else {
				return retrieveTypeClass(typeExpression);
			}
		}
		if (argument instanceof ArrayAccess) {
			ArrayAccess arrayAccess = (ArrayAccess) argument;
			return retrieveTypeClass(arrayAccess.getArray());
		}
		if (argument instanceof Class<?>) {
			return (Class<?>) argument;
		}
		if (argument instanceof ClassInstanceCreation) {
			return retrieveTypeClass(((ClassInstanceCreation) argument).resolveTypeBinding());
		}
		if (argument instanceof BooleanLiteral) {
			return Boolean.TYPE;
		}
		throw new UnsupportedOperationException("Retrieval of type "
		        + argument.getClass() + " not implemented yet!");
	}

	/**
	 * <p>
	 * retrieveVariableReference
	 * </p>
	 * 
	 * @param argument
	 *            a {@link java.lang.Object} object.
	 * @param varType
	 *            a {@link java.lang.Class} object.
	 * @return a {@link org.evosuite.testcase.VariableReference} object.
	 */
	protected VariableReference retrieveVariableReference(Object argument,
	        Class<?> varType) {
		if (argument instanceof ClassInstanceCreation) {
			return retrieveVariableReference((ClassInstanceCreation) argument, varType);
		}
		if (argument instanceof VariableDeclarationFragment) {
			return retrieveVariableReference((VariableDeclarationFragment) argument);
		}
		if (argument instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) argument;
			lineNumber = testReader.getLineNumber(simpleName.getStartPosition());
			return retrieveVariableReference(simpleName.resolveBinding(), varType);
		}
		if (argument instanceof IVariableBinding) {
			return retrieveVariableReference((IVariableBinding) argument, varType);
		}
		if (argument instanceof PrefixExpression) {
			return retrieveVariableReference((PrefixExpression) argument);
		}
		if (argument instanceof InfixExpression) {
			return retrieveVariableReference((InfixExpression) argument, varType);
		}
		if (argument instanceof ExpressionStatement) {
			ExpressionStatement exprStmt = (ExpressionStatement) argument;
			Expression expression = exprStmt.getExpression();
			return retrieveVariableReference(expression, varType);
		}
		if (argument instanceof NullLiteral) {
			return retrieveVariableReference((NullLiteral) argument, varType);
		}
		if (argument instanceof StringLiteral) {
			return retrieveVariableReference((StringLiteral) argument);
		}
		if (argument instanceof NumberLiteral) {
			return retrieveVariableReference((NumberLiteral) argument);
		}
		if (argument instanceof CharacterLiteral) {
			return retrieveVariableReference((CharacterLiteral) argument);
		}
		if (argument instanceof BooleanLiteral) {
			return retrieveVariableReference((BooleanLiteral) argument);
		}
		if (argument instanceof ITypeBinding) {
			if (varType != null) {
				return new ValidVariableReference(testCase.getReference(), varType);
			}
			return new ValidVariableReference(testCase.getReference(),
			        retrieveTypeClass(argument));
		}
		if (argument instanceof QualifiedName) {
			return retrieveVariableReference((QualifiedName) argument);
		}
		if (argument instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) argument;
			VariableReference result = retrieveResultReference(methodInvocation);
			nestedCallResults.push(result);
			return result;
		}
		if (argument instanceof ArrayCreation) {
			return retrieveVariableReference((ArrayCreation) argument);
		}
		if (argument instanceof VariableDeclaration) {
			return retrieveVariableReference((VariableDeclaration) argument);
		}
		if (argument instanceof ArrayAccess) {
			// return retrieveVariableReference(((ArrayAccess)
			// argument).getArray(), null);
			return retrieveVariableReference((ArrayAccess) argument);
		}
		if (argument instanceof Assignment) {
			return retrieveVariableReference(((Assignment) argument).getLeftHandSide(),
			                                 null);
		}
		if (argument instanceof CastExpression) {
			CastExpression castExpression = (CastExpression) argument;
			VariableReference result = retrieveVariableReference(castExpression.getExpression(),
			                                                     null);
			Class<?> castClass = retrieveTypeClass(castExpression.resolveTypeBinding());
			assert castClass.isAssignableFrom(toClass(result.getType()));
			result.setType(castClass);
			return result;
		}
		throw new UnsupportedOperationException("Argument type " + argument.getClass()
		        + " not implemented!");
	}

	private void acceptChild(ASTNode parent, ASTNode child) {
		try {
			Method acceptChild = ASTNode.class.getDeclaredMethod("acceptChild",
			                                                     ASTVisitor.class,
			                                                     ASTNode.class);
			acceptChild.setAccessible(true);
			acceptChild.invoke(parent, this, child);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private void acceptChildren(ASTNode parent, List<?> childNodes) {
		try {
			Method[] allMethods = ASTNode.class.getDeclaredMethods();
			Method acceptChildren = null;
			for (Method method : allMethods) {
				if (method.getName().equals("acceptChildren")) {
					if (acceptChildren != null) {
						throw new RuntimeException(
						        "Found method 'acceptChildren' more than once!");
					}
					acceptChildren = method;
				}
			}
			if (acceptChildren == null) {
				throw new RuntimeException(
				        "Method acceptChildren not found for type ASTNode!");
			}
			acceptChildren.setAccessible(true);
			acceptChildren.invoke(parent, this, childNodes);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private List<VariableReference> convertParams(List<?> params, List<?> argumentTypes) {
		List<VariableReference> result = new ArrayList<VariableReference>();
		if ((params.size() == 0) && (argumentTypes.size() == 0)) {
			return result;
		}
		if ((argumentTypes.size() > params.size())
		        && (argumentTypes.size() - 1 != params.size())) {
			throw new RuntimeException(
			        "Number of declared and actual params do not match!");
		}
		int limit = argumentTypes.size();
		Class<?> lastDeclaredParamType = retrieveTypeClass(argumentTypes.get(argumentTypes.size() - 1));
		if (lastDeclaredParamType.isArray()) {
			limit = argumentTypes.size() - 1;
		}
		for (int idx = 0; idx < limit; idx++) {
			if (idx >= params.size()) {
				break;
			}
			Object argument = params.get(idx);
			if ((argument instanceof MethodInvocation)
			        || (argument instanceof ClassInstanceCreation)) {
				assert !nestedCallResults.isEmpty();
				result.add(nestedCallResults.pop());
				continue;
			}
			Class<?> argClass = retrieveTypeClass(argumentTypes.get(idx));
			VariableReference argRef = retrieveVariableReference(argument, argClass);
			argRef.setOriginalCode(argument.toString());
			result.add(argRef);
		}
		if (limit == argumentTypes.size()) {
			return result;
		}
		assert lastDeclaredParamType.isArray();
		if (argumentTypes.size() == params.size()) {
			Object lastParam = params.get(params.size() - 1);
			Class<?> lastActualParamType = retrieveTypeClass(lastParam);
			if (lastParam instanceof MethodInvocation) {
				assert !nestedCallResults.isEmpty();
				lastActualParamType = nestedCallResults.peek().getVariableClass();
			}
			if (lastActualParamType.isArray()) {
				if ((lastParam instanceof MethodInvocation)
				        || (lastParam instanceof ClassInstanceCreation)) {
					assert !nestedCallResults.isEmpty();
					result.add(nestedCallResults.pop());
				} else {
					result.add(retrieveVariableReference(lastParam, null));
				}
				return result;
			}
		}
		ArrayReference arrayReference = new ValidArrayReference(testCase.getReference(),
		        lastDeclaredParamType);
		arrayReference.setOriginalCode(params.toString());
		ArrayStatement arrayStatement = new ArrayStatement(testCase.getReference(),
		        arrayReference);
		testCase.addStatement(arrayStatement);
		result.add(arrayStatement.getReturnValue());
		arrayStatement.setSize(params.size() - (argumentTypes.size() - 1));
		int arrayIdx = 0;
		for (int idx = argumentTypes.size() - 1; idx < params.size(); idx++) {
			ArrayIndex arrayIndex = new ArrayIndex(testCase.getReference(),
			        arrayReference, arrayIdx);
			Object param = params.get(idx);
			VariableReference paramRef = null;
			if ((param instanceof MethodInvocation)
			        || (param instanceof ClassInstanceCreation)) {
				assert !nestedCallResults.isEmpty();
				paramRef = nestedCallResults.pop();
			} else {
				paramRef = retrieveVariableReference(param,
				                                     lastDeclaredParamType.getComponentType());
			}
			paramRef.setOriginalCode(param.toString());
			AssignmentStatement assignment = new AssignmentStatement(
			        testCase.getReference(), arrayIndex, paramRef);
			testCase.addStatement(assignment);
			arrayIdx++;
		}
		return result;
	}

	private PrimitiveStatement<?> createPrimitiveStatement(Class<?> primitiveClass,
	        Object value) {
		if (String.class.equals(primitiveClass)) {
			return new StringPrimitiveStatement(testCase.getReference(), (String) value);
		}
		if (primitiveClass.equals(Boolean.class) || primitiveClass.equals(boolean.class)) {
			return new BooleanPrimitiveStatement(testCase.getReference(), (Boolean) value);
		}
		if (primitiveClass.equals(Integer.class) || primitiveClass.equals(int.class)) {
			return new IntPrimitiveStatement(testCase.getReference(),
			        ((Number) value).intValue());
		}
		if (primitiveClass.equals(Long.class) || primitiveClass.equals(long.class)) {
			return new LongPrimitiveStatement(testCase.getReference(),
			        ((Number) value).longValue());
		}
		if (primitiveClass.equals(Byte.class) || primitiveClass.equals(byte.class)) {
			return new BytePrimitiveStatement(testCase.getReference(),
			        ((Number) value).byteValue());
		}
		if (primitiveClass.equals(Float.class) || primitiveClass.equals(float.class)) {
			return new FloatPrimitiveStatement(testCase.getReference(),
			        ((Number) value).floatValue());
		}
		if (primitiveClass.equals(Short.class) || primitiveClass.equals(short.class)) {
			return new ShortPrimitiveStatement(testCase.getReference(),
			        ((Number) value).shortValue());
		}
		if (primitiveClass.equals(Double.class) || primitiveClass.equals(double.class)) {
			return new DoublePrimitiveStatement(testCase.getReference(),
			        ((Number) value).doubleValue());
		}
		throw new UnsupportedOperationException(
		        "Not all primitives have been implemented!");
	}

	private Class<?> doBoxing(Class<?> primitiveClass) {
		if (primitiveClass.equals(long.class)) {
			return Long.class;
		}
		if (primitiveClass.equals(int.class)) {
			return Integer.class;
		}
		if (primitiveClass.equals(short.class)) {
			return Short.class;
		}
		if (primitiveClass.equals(byte.class)) {
			return Byte.class;
		}
		if (primitiveClass.equals(boolean.class)) {
			return Boolean.class;
		}
		if (primitiveClass.equals(char.class)) {
			return Character.class;
		}
		if (primitiveClass.equals(double.class)) {
			return Double.class;
		}
		if (primitiveClass.equals(float.class)) {
			return Float.class;
		}
		throw new UnsupportedOperationException("Cannot doBoxing for class "
		        + primitiveClass);
	}

	private Integer getArrayIndex(Expression index) {
		if (index instanceof SimpleName) {
			String variable = retrieveVariableName(index);
			Integer lineNumber = testReader.getLineNumber(index.getStartPosition());
			if (cursorableTrace != null) {
				Object value = cursorableTrace.getVariableValueAfter(lineNumber, variable);
				return (Integer) value;
			}
			throw new RuntimeException("Cannot determine array index without trace for "
			        + index + "!");
		}
		if (index instanceof NumberLiteral) {
			Object value = ((NumberLiteral) index).resolveConstantExpressionValue();
			return (Integer) value;
		}
		throw new RuntimeException(
		        "Method getArrayIndex not implemented for index expression type " + index
		                + "!");
	}

	private int[] getLengths(VariableDeclarationStatement variableDeclStmt,
	        List<VariableReference> lengthsVarRefs) {
		int[] lengths = new int[lengthsVarRefs.size()];
		String variable = retrieveVariableName(variableDeclStmt);
		int lineNumber = testReader.getLineNumber(variableDeclStmt.getStartPosition());
		Object value = null;
		if (cursorableTrace != null) {
			value = cursorableTrace.getVariableValueAfter(lineNumber, variable);
		}
		int idx = 0;
		while ((value != null) && value.getClass().isArray()) {
			lengths[idx] = Array.getLength(value);
			if (lengths[idx] > 0) {
				value = Array.get(value, 0);
			} else {
				value = null;
			}
			idx++;
		}
		if (idx == lengths.length) {
			return lengths;
		}

		idx = 0;
		for (VariableReference lengthVarRef : lengthsVarRefs) {
			// TODO This is a hack. We should use the variablereferences
			// instead!
			lengths[idx] = Integer.valueOf(lengthVarRef.toString());
			idx++;
		}
		return lengths;
	}

	private VariableReference getLoopVariable(IVariableBinding varBinding,
	        Class<?> varClass) {
		if (lineNumber == null) {
			throw new RuntimeException("Don't know in which line we are...");
		}
		Object value = cursorableTrace.getVariableValueAfter(lineNumber,
		                                                     varBinding.getName());
		if (varClass.isPrimitive()) {
			PrimitiveStatement<?> numberAssignment = createPrimitiveStatement(varClass,
			                                                                  value);
			testCase.addStatement(numberAssignment);
			return numberAssignment.getReturnValue();
		}
		throw new RuntimeException("Not implemented!");
	}

	private Class<?> loadClass(String className) {
		// TODO Implement loading classes from Properties.CLASSPATH that are not
		// on BugEx's classpath
		String simpleName = className;
		if (className.startsWith("[")) {
			Pattern pattern = Pattern.compile("\\[L([\\.\\w]+);");
			Matcher matcher = pattern.matcher(className);
			if (matcher.find()) {
				simpleName = matcher.group(1);
			}
		}
		try {
			if (BytecodeInstrumentation.isSharedClass(simpleName)) {
				return Class.forName(className);
			}
			if (!BytecodeInstrumentation.isTargetProject(simpleName)) {
				return Class.forName(className);
			}
			return Class.forName(className, true, TestGenerationContext.getClassLoader());
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}

	private Number negate(Number value) {
		if (value instanceof Integer) {
			return (Integer) value * -1;
		}
		if (value instanceof Long) {
			return (Long) value * -1;
		}
		if (value instanceof Float) {
			return (Float) value * -1;
		}
		if (value instanceof Double) {
			return (Double) value * -1;
		}
		throw new UnsupportedOperationException("Number type " + value.getClass()
		        + " not implemented!");
	}

	private VariableReference retrieveCalleeReference(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			return retrieveVariableReference(expression, null);
		}
		throw new RuntimeException("Callee was null for expression: " + expression);
	}

	private VariableReference retrieveDefaultValueAssignment(Class<?> typeClass) {
		if (typeClass.isPrimitive()) {
			if (typeClass.equals(boolean.class)) {
				PrimitiveStatement<Boolean> charAssignment = new BooleanPrimitiveStatement(
				        testCase.getReference(), false);
				testCase.addStatement(charAssignment);
				return charAssignment.getReturnValue();
			}
			PrimitiveStatement<?> numberAssignment = createPrimitiveStatement(typeClass,
			                                                                  0);
			testCase.addStatement(numberAssignment);
			return numberAssignment.getReturnValue();
		}
		PrimitiveStatement<?> nullAssignment = new NullStatement(testCase.getReference(),
		        typeClass);
		testCase.addStatement(nullAssignment);
		return nullAssignment.getReturnValue();
	}

	private Method retrieveMethod(MethodInvocation methodInvocation,
	        IMethodBinding methodBinding, List<VariableReference> params) {
		assert methodBinding != null : "Sources missing!";
		String methodName = methodInvocation.getName().getIdentifier();
		Class<?> clazz = retrieveTypeClass(methodBinding.getDeclaringClass());
		Set<Method> methods = retrieveMethods(clazz, methodName, params);
		if (methods.size() == 1) {
			return methods.iterator().next();
		}
		// TODO Implement varargs methods
		try {
			Class<?>[] paramClasses = new Class<?>[methodInvocation.arguments().size()];
			assert methods.size() > 0;
			for (int idx = 0; idx < methodInvocation.arguments().size(); idx++) {
				ITypeBinding paramTypeBinding = methodBinding.getParameterTypes()[idx];
				paramClasses[idx] = retrieveTypeClass(paramTypeBinding);
				VariableReference param = params.get(idx);
				if (!param.isAssignableFrom(paramClasses[idx])) {
					if (methods.size() == 0) {
						throw new IllegalStateException(
						        "Param class and argument do not match!");
					}
					throw new WrongMethodBindingException();
				}
			}
			try {
				return clazz.getMethod(methodName, paramClasses);
			} catch (Exception exc) {
				try {
					return clazz.getDeclaredMethod(methodName, paramClasses);
				} catch (Exception exc2) {
					throw new RuntimeException(exc);
				}
			}
		} catch (WrongMethodBindingException exc) {
			logger.debug("The resolved method binding is wrong. Will manually correct it...");
			if (methods.size() > 1) {
				logger.warn("Cannot unambiguously determine the method '{}'.", methodName);
			}
			return methods.iterator().next();
		}
	}

	private Set<Method> retrieveMethods(Class<?> clazz, String methodName,
	        List<VariableReference> params) {
		Set<Method> result = new HashSet<Method>();
		Set<Method> possibleMethods = new HashSet<Method>();
		possibleMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		possibleMethods.addAll(Arrays.asList(clazz.getMethods()));
		outer: for (Method method : possibleMethods) {
			if (method.getName().equals(methodName)
			        && (method.getParameterTypes().length == params.size())) {
				Class<?>[] declaredParamClasses = method.getParameterTypes();
				for (int idx = 0; idx < params.size(); idx++) {
					VariableReference param = params.get(idx);
					Class<?> actualParamClass = param.getVariableClass();
					Class<?> declaredParamClass = declaredParamClasses[idx];
					if (!declaredParamClass.isAssignableFrom(actualParamClass)) {
						if (actualParamClass.isPrimitive()) {
							actualParamClass = doBoxing(actualParamClass);
							if (!declaredParamClasses[idx].isAssignableFrom(actualParamClass)) {
								continue outer;
							}
						} else {
							continue outer;
						}
					}
				}
				result.add(method);
			}
		}
		assert result.size() > 0 : "Param classes and arguments do not match!";
		return result;
	}

	private Class<?> retrievePrimitiveClass(String className) {
		assert className.length() == 1;
		Class<?> result = PRIMITIVE_SIGNATURE_MAPPING.get(className.charAt(0));
		if (result != null) {
			return result;
		}
		throw new RuntimeException("Primitive type of class '" + className
		        + "' is unknown.");
	}

	private VariableReference retrieveResultReference(MethodInvocation methodInvocation) {
		VariableReference result = calleeResultMap.get(methodInvocation.toString());
		if (result != null) {
			return result;
		}
		ASTNode parent = methodInvocation.getParent();
		if (parent instanceof VariableDeclarationFragment) {
			return retrieveVariableReference(parent, null);
		}
		if (parent instanceof Assignment) {
			Assignment assignment = (Assignment) parent;
			return retrieveVariableReference(assignment.getLeftHandSide(), null);
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding returnType = methodBinding.getReturnType();
		Class<?> resultClass = null;
		try {
			resultClass = retrieveTypeClass(returnType);
		} catch (Exception exc) {
			String localClass = methodBinding.getDeclaringClass().getQualifiedName()
			        + "." + returnType.getName();
			resultClass = loadClass(localClass);
		}
		result = retrieveVariableReference(returnType, resultClass);
		calleeResultMap.put(methodInvocation.toString(), result);
		return result;
	}

	private VariableReference retrieveResultReference(
	        SuperMethodInvocation superMethodInvocation) {
		// TODO Duplicate code from retrieveResultReference(MethodInvocation)
		// too bad they don't have a common matching interface
		VariableReference result = calleeResultMap.get(superMethodInvocation.toString());
		if (result != null) {
			return result;
		}
		ASTNode parent = superMethodInvocation.getParent();
		if (parent instanceof VariableDeclarationFragment) {
			return retrieveVariableReference(parent, null);
		}
		if (parent instanceof Assignment) {
			Assignment assignment = (Assignment) parent;
			return retrieveVariableReference(assignment.getLeftHandSide(), null);
		}
		IMethodBinding methodBinding = superMethodInvocation.resolveMethodBinding();
		result = retrieveVariableReference(methodBinding.getReturnType(), null);
		calleeResultMap.put(superMethodInvocation.toString(), result);
		return result;
	}

	private Class<?> retrieveTypeClass(ArrayType arrayType) {
		try {
			Class<?> componentType = retrieveTypeClass(arrayType.getComponentType());
			if (componentType.isPrimitive()) {
				String clazz = "[" + componentType.getName().toUpperCase().charAt(0);
				return Class.forName(clazz);
			}
			if (componentType.isArray()) {
				String clazz = "[" + componentType.getName();
				return Class.forName(clazz);
			}
			return Class.forName("[L" + componentType.getName() + ";");
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}

	private Class<?> retrieveTypeClass(ITypeBinding binding) {
		String className = binding.getBinaryName();
		if (className == null) {
			return null;
		}
		if (binding.isArray()) {
			return loadClass(className);
		}
		if (binding.isPrimitive()) {
			return retrievePrimitiveClass(binding.getBinaryName());
		}
		return loadClass(className);
	}

	private Class<?> retrieveTypeClass(NumberLiteral numberLiteral) {
		Object value = numberLiteral.resolveConstantExpressionValue();
		if (numberLiteral.resolveBoxing()) {
			return value.getClass();
		}
		if (value instanceof Integer) {
			return Integer.TYPE;
		}
		if (value instanceof Long) {
			return Long.TYPE;
		}
		if (value instanceof Double) {
			return Double.TYPE;
		}
		if (value instanceof Float) {
			return Float.TYPE;
		}
		if (value instanceof Short) {
			return Short.TYPE;
		}
		if (value instanceof Byte) {
			return Byte.TYPE;
		}
		throw new UnsupportedOperationException("Retrieval of type "
		        + numberLiteral.getClass() + " not implemented yet!");
	}

	private Class<?> retrieveTypeClass(SimpleType simpleType) {
		ITypeBinding binding = simpleType.resolveBinding();
		if (binding == null) {
			String result = imports.get(simpleType.toString());
			if (result != null) {
				try {
					return Class.forName(result);
				} catch (ClassNotFoundException exc) {
					throw new RuntimeException("Classpath incomplete?", exc);
				}
			}
		}
		assert binding != null : "Could not resolve binding for " + simpleType
		        + ". Missing sources folder?";
		return retrieveTypeClass(binding);
	}

	private String retrieveVariableName(Object argument) {
		if (argument instanceof SimpleName) {
			return ((SimpleName) argument).getIdentifier();
		}
		if (argument instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement variableDeclStmt = (VariableDeclarationStatement) argument;
			return retrieveVariableName(variableDeclStmt.fragments().get(0));
		}
		if (argument instanceof VariableDeclarationFragment) {
			return retrieveVariableName(((VariableDeclarationFragment) argument).getName());
		}

		throw new RuntimeException("Not implemented for type " + argument.getClass());
	}

	private VariableReference retrieveVariableReference(ArrayAccess arrayAccess) {
		Expression expr = arrayAccess.getArray();
		List<Integer> indices = new ArrayList<Integer>();
		// TODO This is a shortcut
		// we need a variable reference for the index value
		indices.add(getArrayIndex(arrayAccess.getIndex()));
		while (expr instanceof ArrayAccess) {
			ArrayAccess current = (ArrayAccess) expr;
			expr = current.getArray();
			indices.add(getArrayIndex(current.getIndex()));
		}
		Collections.reverse(indices);
		VariableReference varRef = retrieveVariableReference(expr, null);
		ArrayReference arrayReference = (ArrayReference) varRef;
		assert indices.size() == arrayReference.getArrayDimensions();
		ArrayIndex arrayIndex = new ArrayIndex(testCase.getReference(), arrayReference,
		        indices);
		return arrayIndex;
	}

	private VariableReference retrieveVariableReference(ArrayCreation arrayCreation) {
		Class<?> arrayType = retrieveTypeClass(arrayCreation.getType());
		List<?> dimensions = ((AbstractList<?>) arrayCreation.getStructuralProperty(ArrayCreation.DIMENSIONS_PROPERTY));
		if (dimensions.size() > 1) {
			throw new RuntimeException("Multidimensional arrays not implemented!");
		}
		Integer length = (Integer) ((NumberLiteral) dimensions.get(0)).resolveConstantExpressionValue();
		// ArrayReference arrayReference = new
		// ValidArrayReference(testCase.getReference(), arrayType, length);
		ArrayStatement arrayAssignment = new ArrayStatement(testCase.getReference(),
		        arrayType, length);
		testCase.addStatement(arrayAssignment);
		return arrayAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(BooleanLiteral boolLiteral) {
		boolean bool = boolLiteral.booleanValue();
		PrimitiveStatement<Boolean> charAssignment = new BooleanPrimitiveStatement(
		        testCase.getReference(), bool);
		testCase.addStatement(charAssignment);
		return charAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(CharacterLiteral characterLiteral) {
		char character = characterLiteral.charValue();
		PrimitiveStatement<Character> charAssignment = new CharPrimitiveStatement(
		        testCase.getReference(), character);
		testCase.addStatement(charAssignment);
		return charAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(
	        ClassInstanceCreation instanceCreation, Class<?> varType) {
		if ((instanceCreation.getParent() instanceof MethodInvocation)
		        || (instanceCreation.getParent() instanceof ClassInstanceCreation)) {
			VariableReference result = new ValidVariableReference(
			        testCase.getReference(),
			        retrieveTypeClass(instanceCreation.getType()));
			nestedCallResults.push(result);
			return result;
		}
		if ((instanceCreation.getParent() instanceof ExpressionStatement)
		        && (instanceCreation.getParent().getParent() instanceof Block)) {
			if (varType == null) {
				varType = retrieveTypeClass(instanceCreation);
			}
			VariableReference varRef = new ValidVariableReference(
			        testCase.getReference(), varType);
			return varRef;
		}
		return retrieveVariableReference(instanceCreation.getParent(), varType);
	}

	private VariableReference retrieveVariableReference(InfixExpression infixExpr,
	        Class<?> exprType) {
		if (exprType == null) {
			exprType = retrieveTypeClass(infixExpr);
		}
		VariableReference ref = new VariableReferenceImpl(testCase.getReference(),
		        exprType);
		VariableReference leftOperand = retrieveVariableReference(infixExpr.getLeftOperand(),
		                                                          null);
		leftOperand.setOriginalCode(infixExpr.getLeftOperand().toString());
		Operator operator = Operator.toOperator(infixExpr.getOperator().toString());
		VariableReference rightOperand = retrieveVariableReference(infixExpr.getRightOperand(),
		                                                           null);
		rightOperand.setOriginalCode(infixExpr.getRightOperand().toString());
		PrimitiveExpression expr = new PrimitiveExpression(testCase.getReference(), ref,
		        leftOperand, operator, rightOperand);
		testCase.addStatement(expr);
		return ref;
	}

	private VariableReference retrieveVariableReference(IVariableBinding varBinding,
	        Class<?> varClass) {
		if (varClass == null) {
			varClass = retrieveTypeClass(varBinding.getType());
		}
		VariableReference localVar = testCase.getVariableReference(varBinding);
		if (localVar != null) {
			return localVar;
		}
		if (!iterations.isEmpty()) {
			return getLoopVariable(varBinding, varClass);
		}
		logger.warn("No variable reference found for variable binding {}, creating new one.",
		            varBinding);
		return new BoundVariableReferenceImpl(testCase, varClass, varBinding.getName());
	}

	private VariableReference retrieveVariableReference(NullLiteral nullLiteral,
	        Class<?> varType) {
		if (varType == null) {
			varType = retrieveTypeClass(nullLiteral.getParent());
		}
		PrimitiveStatement<?> nullAssignment = new NullStatement(testCase.getReference(),
		        varType);
		testCase.addStatement(nullAssignment);
		return nullAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(NumberLiteral numberLiteral) {
		Class<?> numberClass = retrieveTypeClass(numberLiteral);
		return retrieveVariableReference(numberLiteral, numberClass);
	}

	private VariableReference retrieveVariableReference(NumberLiteral numberLiteral,
	        Class<?> numberClass) {
		Object value = numberLiteral.resolveConstantExpressionValue();
		PrimitiveStatement<?> numberAssignment = createPrimitiveStatement(numberClass,
		                                                                  value);
		testCase.addStatement(numberAssignment);
		return numberAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(PrefixExpression prefixExpr) {
		if (prefixExpr.getOperator() == org.eclipse.jdt.core.dom.PrefixExpression.Operator.MINUS) {
			Class<?> numberClass = retrieveTypeClass(prefixExpr.getOperand());
			return retrieveVariableReference(prefixExpr, numberClass);
		}
		throw new UnsupportedOperationException("Prefix " + prefixExpr
		        + " not implemented!");
	}

	private VariableReference retrieveVariableReference(PrefixExpression prefixExpr,
	        Class<?> numberClass) {
		if (prefixExpr.getOperator() == org.eclipse.jdt.core.dom.PrefixExpression.Operator.MINUS) {
			NumberLiteral numberLiteral = (NumberLiteral) prefixExpr.getOperand();
			Number value = (Number) numberLiteral.resolveConstantExpressionValue();
			value = negate(value);
			PrimitiveStatement<?> numberAssignment = createPrimitiveStatement(numberClass,
			                                                                  value);
			testCase.addStatement(numberAssignment);
			return numberAssignment.getReturnValue();
		}
		throw new UnsupportedOperationException("Prefix " + prefixExpr
		        + " not implemented!");
	}

	private VariableReference retrieveVariableReference(QualifiedName qualifiedName) {
		try {
			Class<?> referencedClass = retrieveTypeClass(qualifiedName.getQualifier().resolveTypeBinding());
			Field field = referencedClass.getField(qualifiedName.getName().getIdentifier());
			FieldReference fieldReference = new FieldReference(testCase.getReference(),
			        new GenericField(field, referencedClass));
			Class<?> resultClass = retrieveTypeClass(qualifiedName.resolveTypeBinding());
			FieldStatement fieldStatement = new FieldStatement(testCase.getReference(),
			        new GenericField(field, resultClass), fieldReference);
			testCase.addStatement(fieldStatement);
			return fieldStatement.getReturnValue();
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private VariableReference retrieveVariableReference(StringLiteral stringLiteral) {
		String string = stringLiteral.getLiteralValue();
		PrimitiveStatement<String> stringAssignment = new StringPrimitiveStatement(
		        testCase.getReference(), string);
		testCase.addStatement(stringAssignment);
		return stringAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(VariableDeclaration varDecl) {
		IVariableBinding variableBinding = varDecl.resolveBinding();
		Class<?> clazz = retrieveTypeClass(variableBinding.getType());
		VariableReference result = new BoundVariableReferenceImpl(testCase, clazz,
		        variableBinding.getName());
		testCase.addVariable(variableBinding, result);
		return result;
	}

	private VariableReference retrieveVariableReference(
	        VariableDeclarationFragment varDeclFrgmnt) {
		IVariableBinding variableBinding = varDeclFrgmnt.resolveBinding();
		Class<?> clazz = retrieveTypeClass(variableBinding.getType());
		if (clazz.isArray()) {
			ArrayReference arrayReference = new ValidArrayReference(
			        testCase.getReference(), clazz);
			arrayReference.setOriginalCode(varDeclFrgmnt.toString());
			testCase.addVariable(varDeclFrgmnt.resolveBinding(), arrayReference);
			return arrayReference;
		}
		VariableReference result = new BoundVariableReferenceImpl(testCase, clazz,
		        variableBinding.getName());
		testCase.addVariable(variableBinding, result);
		return result;
	}

	private boolean saveMethodCodeExtraction(MethodDeclaration methodDeclaration) {
		exceptionReadingMethod = false;
		logger.warn("Omitting acceptChild(visitor, getJavadoc());");
		logger.warn("Omitting acceptChildren(visitor, this.modifiers);");
		logger.warn("Omitting acceptChildren(visitor, this.typeParameters);");
		logger.warn("Omitting acceptChild(visitor, getReturnType2());");
		logger.warn("Omitting acceptChild(visitor, getName());");
		logger.warn("Omitting acceptChildren(visitor, this.parameters);");
		logger.warn("Omitting acceptChildren(visitor, this.thrownExceptions);");
		logger.warn("Method not accessible, would be: methodDeclaration.acceptChild(this, methodDeclaration.getBody());");
		try {
			Method acceptChild = ASTNode.class.getDeclaredMethod("acceptChild",
			                                                     ASTVisitor.class,
			                                                     ASTNode.class);
			acceptChild.setAccessible(true);
			acceptChild.invoke(methodDeclaration, this, methodDeclaration.getBody());
		} catch (SecurityException exc) {
			throw new RuntimeException(exc);
		} catch (NoSuchMethodException exc) {
			throw new RuntimeException(exc);
		} catch (IllegalArgumentException exc) {
			throw new RuntimeException(exc);
		} catch (IllegalAccessException exc) {
			throw new RuntimeException(exc);
		} catch (InvocationTargetException exc) {
			logger.error("Exception reading code of method '{}', skipping it!",
			             methodDeclaration.getName(), exc.getCause());
			exceptionReadingMethod = true;
		}
		return false;
	}
}
