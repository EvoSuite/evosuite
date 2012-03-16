package de.unisb.cs.st.evosuite.junit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.unisb.cs.st.evosuite.junit.CompoundTestCase.TestScope;
import de.unisb.cs.st.evosuite.testcase.ArrayIndex;
import de.unisb.cs.st.evosuite.testcase.ArrayReference;
import de.unisb.cs.st.evosuite.testcase.ArrayStatement;
import de.unisb.cs.st.evosuite.testcase.AssignmentStatement;
import de.unisb.cs.st.evosuite.testcase.BooleanPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.BytePrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.CharPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.DoublePrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.FieldReference;
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.FloatPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.IntPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.LongPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.NullStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveExpression;
import de.unisb.cs.st.evosuite.testcase.PrimitiveExpression.Operator;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ShortPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.StringPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.testcase.VariableReferenceImpl;

public class TestExtractingVisitor extends LoggingVisitor {

	public static interface TestReader {
		CompoundTestCase readTestCase(String clazz, CompoundTestCase reference);
	}

	private static class BoundVariableReferenceImpl extends VariableReferenceImpl {
		private static final long serialVersionUID = 1L;

		protected String name;

		public BoundVariableReferenceImpl(CompoundTestCase testCase, java.lang.reflect.Type type, String name) {
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

	private static class ValidConstructorStatement extends ConstructorStatement {
		private static final long serialVersionUID = 1L;

		public ValidConstructorStatement(TestCase tc, Constructor<?> constructor, java.lang.reflect.Type type,
				List<VariableReference> parameters) {
			super(tc, constructor, type, parameters);
		}

		public ValidConstructorStatement(TestCase tc, Constructor<?> constructor, VariableReference retVal,
				List<VariableReference> parameters) {
			super(tc, constructor, retVal, parameters);
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}

	private static class ValidMethodStatement extends MethodStatement {
		private static final long serialVersionUID = 1L;

		public ValidMethodStatement(TestCase tc, Method method, VariableReference callee, java.lang.reflect.Type type,
				List<VariableReference> parameters) {
			super(tc, method, callee, type, parameters);
		}

		public ValidMethodStatement(TestCase tc, Method method, VariableReference callee, VariableReference retVal,
				List<VariableReference> parameters) {
			super(tc, method, callee, retVal, parameters);
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}

	private static class ValidVariableReference extends VariableReferenceImpl {

		private static final long serialVersionUID = -59873293582106016L;

		// private static int counter = 0;

		public ValidVariableReference(TestCase testCase, java.lang.reflect.Type type) {
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

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestExtractingVisitor.class);
	private final CompoundTestCase testCase;
	private final TestReader testReader;
	private final String unqualifiedTest;
	private final String unqualifiedTestMethod;
	private Stack<VariableReference> nestedCallResults = new Stack<VariableReference>();

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

	private final static HashMap<Character, Class<?>> PRIMITIVE_ARRAY_MAPPING = new HashMap<Character, Class<?>>();

	static {
		PRIMITIVE_ARRAY_MAPPING.put('B', byte[].class);
		PRIMITIVE_ARRAY_MAPPING.put('C', char[].class);
		PRIMITIVE_ARRAY_MAPPING.put('D', double[].class);
		PRIMITIVE_ARRAY_MAPPING.put('F', float[].class);
		PRIMITIVE_ARRAY_MAPPING.put('I', int[].class);
		PRIMITIVE_ARRAY_MAPPING.put('J', long[].class);
		PRIMITIVE_ARRAY_MAPPING.put('S', short[].class);
		PRIMITIVE_ARRAY_MAPPING.put('Z', boolean[].class);
	}
	private final Map<String, VariableReference> calleeResultMap = new HashMap<String, VariableReference>();

	public TestExtractingVisitor(CompoundTestCase testCase, String testClass, String testMethod, TestReader testReader) {
		super();
		this.testCase = testCase;
		this.testReader = testReader;
		this.unqualifiedTest = testClass.substring(testClass.lastIndexOf(".") + 1, testClass.length());
		this.unqualifiedTestMethod = testMethod;
	}

	@Override
	public void endVisit(Block node) {
		if (testCase.getCurrentScope() == TestScope.STATIC) {
			testCase.setCurrentScope(TestScope.FIELDS);
		}
	}

	@Override
	public void endVisit(ClassInstanceCreation instanceCreation) {
		List<?> paramTypes = Arrays.asList(instanceCreation.resolveConstructorBinding().getParameterTypes());
		List<?> paramValues = instanceCreation.arguments();
		Constructor<?> constructor = retrieveConstructor(instanceCreation.getType(), paramTypes, paramValues);
		List<VariableReference> params = convertParams(instanceCreation.arguments(), paramTypes);
		VariableReference retVal = retrieveVariableReference(instanceCreation, null);
		ConstructorStatement statement = new ValidConstructorStatement(testCase.getReference(), constructor, retVal,
				params);
		testCase.addStatement(statement);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		testCase.finalizeMethod();
	}

	@Override
	public void endVisit(MethodInvocation methodInvocation) {
		// TODO If in constructor, treat calls to this() and super().
		List<?> paramTypes = Arrays.asList(methodInvocation.resolveMethodBinding().getParameterTypes());
		List<VariableReference> params = convertParams(methodInvocation.arguments(), paramTypes);
		Method method = retrieveMethod(methodInvocation, params);
		if (testCase.isDescendantOf(method.getDeclaringClass())) {
			List<StatementInterface> methodStatements = testCase.getMethod(method.getName());
			testCase.convertMethod(methodStatements, params);
			return;
		}
		VariableReference callee = null;
		if (!Modifier.isStatic(method.getModifiers())) {
			callee = retrieveCalleeReference(methodInvocation);
		}
		MethodStatement methodStatement = null;
		ASTNode parent = methodInvocation.getParent();
		if (parent instanceof ExpressionStatement) {
			VariableReference retVal = new ValidVariableReference(testCase.getReference(), method.getReturnType());
			methodStatement = new ValidMethodStatement(testCase.getReference(), method, callee, retVal, params);
			testCase.addStatement(methodStatement);
			return;
		}
		VariableReference retVal = retrieveResultReference(methodInvocation);
		methodStatement = new ValidMethodStatement(testCase.getReference(), method, callee, retVal, params);
		if (parent instanceof MethodInvocation) {
			nestedCallResults.push(retVal);
		}
		testCase.addStatement(methodStatement);
	}

	@Override
	public void endVisit(SuperMethodInvocation superMethodInvocation) {
		List<?> paramTypes = Arrays.asList(superMethodInvocation.resolveMethodBinding().getParameterTypes());
		List<VariableReference> params = convertParams(superMethodInvocation.arguments(), paramTypes);
		String name = superMethodInvocation.getName().getIdentifier();
		List<StatementInterface> methodStatements = testCase.getParent().getMethod(name);
		testCase.convertMethod(methodStatements, params);
	}

	@Override
	public boolean visit(Assignment assignment) {
		VariableReference varRef = retrieveVariableReference(assignment.getLeftHandSide(), null);
		VariableReference newAssignment = retrieveVariableReference(assignment.getRightHandSide(), null);
		testCase.variableAssignment(varRef, newAssignment);
		return true;
	}

	@Override
	public boolean visit(Block node) {
		if (testCase.getCurrentScope() == TestScope.FIELDS) {
			testCase.setCurrentScope(TestScope.STATIC);
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		if (Modifier.isStatic(fieldDeclaration.getModifiers())) {
			testCase.setCurrentScope(TestScope.STATICFIELDS);
		}
		VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		Expression expression = varDeclFrgmnt.getInitializer();
		VariableReference varRef = retrieveVariableReference(expression, null);
		// TODO Use the name here as well?
		// String name = varDeclFrgmt.getName().getIdentifier();
		// new BoundVariableReferenceImpl(testCase, varType, name);
		testCase.addVariable(varDeclFrgmnt.resolveBinding(), varRef);
		testCase.setCurrentScope(TestScope.FIELDS);
		return true;
	}

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

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		String methodName = methodDeclaration.getName().getIdentifier();
		testCase.newMethod(methodName);
		if ((unqualifiedTestMethod == null) || !unqualifiedTestMethod.equals(methodName)) {
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
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration variableDeclaration) {
		VariableReference varRef = retrieveVariableReference(variableDeclaration);
		testCase.addParameter(varRef);
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (!unqualifiedTest.equals(typeDeclaration.getName().getIdentifier())) {
			logger.warn("Type declarations are ignored for other types than the actual test!");
			return false;
		}
		Type supertype = typeDeclaration.getSuperclassType();
		if (supertype != null) {
			String superclass = retrieveTypeClass(supertype).getName();
			if (!superclass.startsWith("org.junit.") && !superclass.startsWith("junit.")) {
				testReader.readTestCase(superclass, testCase);
			}
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement variableDeclStmt) {
		Class<?> varType = retrieveTypeClass(variableDeclStmt.getType());
		if (varType.isPrimitive() || PRIMITIVE_CLASSES.contains(varType)) {
			// Can only happen to primitive types and String,
			// otherwise it is a constructor call which is handled elsewhere
			logger.debug("Variable has not been treated elsewhere...");
			VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) variableDeclStmt.fragments().get(
					0);
			Expression expression = varDeclFrgmnt.getInitializer();
			VariableReference varRef = retrieveVariableReference(expression, null);
			// TODO Use the name here as well?
			// String name = varDeclFrgmt.getName().getIdentifier();
			// new BoundVariableReferenceImpl(testCase, varType, name);
			testCase.addVariable(varDeclFrgmnt.resolveBinding(), varRef);
		}
		if (varType.isArray()
				&& (varType.getComponentType().isPrimitive() || varType.getComponentType().equals(String.class))) {
			// ... or to primitive and string arrays
			VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) variableDeclStmt.fragments().get(
					0);
			Expression expression = varDeclFrgmnt.getInitializer();
			if (expression instanceof ArrayInitializer) {
				ArrayStatement arrayStatement = new ArrayStatement(testCase.getReference(), varType);
				ArrayReference arrayRef = (ArrayReference) arrayStatement.getReturnValue();
				testCase.addStatement(arrayStatement);
				testCase.addVariable(varDeclFrgmnt.resolveBinding(), arrayRef);
				ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
				for (int idx = 0; idx < arrayInitializer.expressions().size(); idx++) {
					Expression expr = (Expression) arrayInitializer.expressions().get(idx);
					VariableReference valueRef;
					if (expr instanceof NumberLiteral) {
						valueRef = retrieveVariableReference((NumberLiteral) expr, varType.getComponentType());
					} else if (expr instanceof PrefixExpression) {
						valueRef = retrieveVariableReference((PrefixExpression) expr, varType.getComponentType());
					} else {
						valueRef = retrieveVariableReference(expr, null);
					}
					VariableReference arrayElementRef = new ArrayIndex(testCase.getReference(), arrayRef, idx);
					arrayStatement.getVariableReferences().add(arrayElementRef);
					AssignmentStatement arrayAssignment = new AssignmentStatement(testCase.getReference(),
							arrayElementRef, valueRef);
					testCase.addStatement(arrayAssignment);
				}
			}
		}
		return true;
	}

	protected Class<?>[] extractArgumentClasses(List<?> arguments) {
		Class<?>[] argClasses = new Class<?>[arguments.size()];
		for (int idx = 0; idx < arguments.size(); idx++) {
			Object arg = arguments.get(idx);
			argClasses[idx] = retrieveTypeClass(arg);
		}
		return argClasses;
	}

	protected Constructor<?> retrieveConstructor(Type type, List<?> argumentTypes, List<?> arguments) {
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

	protected Class<?> retrieveTypeClass(Object argument) {
		assert argument != null;
		if (argument instanceof SimpleType) {
			SimpleType simpleType = (SimpleType) argument;
			ITypeBinding binding = simpleType.resolveBinding();
			assert binding != null : "Could not resolve binding for " + simpleType + ". Missing sources folder?";
			return retrieveTypeClass(binding);
		}
		if (argument instanceof ITypeBinding) {
			ITypeBinding binding = (ITypeBinding) argument;
			String className = binding.getBinaryName();
			if (binding.isArray()) {
				if (binding.getElementType().isPrimitive()) {
					char arrayType = binding.getElementType().getBinaryName().charAt(0);
					Class<?> result = PRIMITIVE_ARRAY_MAPPING.get(arrayType);
					assert result != null;
					return result;
				}
				return Object[].class;
			}
			if (binding.isPrimitive()) {
				return retrievePrimitiveClass(binding.getBinaryName());
			}
			try {
				return loadClass(className);
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
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
			NumberLiteral numberLiteral = (NumberLiteral) argument;
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
			throw new UnsupportedOperationException("Retrieval of type " + argument.getClass()
					+ " not implemented yet!");
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
			try {
				Class<?> componentType = retrieveTypeClass(arrayType.getComponentType());
				if (componentType.isPrimitive()) {
					String clazz = "[" + componentType.getName().toUpperCase().charAt(0);
					return Class.forName(clazz);
				} else {
					return Class.forName("[L" + componentType.getName() + ";");
				}
			} catch (ClassNotFoundException exc) {
				throw new RuntimeException(exc);
			}
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
				throw new RuntimeException("Could not determine type class of infix expression '" + infixExpr + "'.");
			}
		}
		throw new UnsupportedOperationException("Retrieval of type " + argument.getClass() + " not implemented yet!");
	}

	protected VariableReference retrieveVariableReference(Object argument, Class<?> varType) {
		if (argument instanceof ClassInstanceCreation) {
			return retrieveVariableReference(varType, (ClassInstanceCreation) argument);
		}
		if (argument instanceof VariableDeclarationFragment) {
			return retrieveVariableReference((VariableDeclarationFragment) argument);
		}
		if (argument instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) argument;
			return retrieveVariableReference(simpleName.resolveBinding(), varType);
		}
		if (argument instanceof IVariableBinding) {
			return retrieveVariableReference((IVariableBinding) argument);
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
			return retrieveVariableReference((NullLiteral) argument);
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
			return new ValidVariableReference(testCase.getReference(), retrieveTypeClass(argument));
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
		throw new UnsupportedOperationException("Argument type " + argument.getClass() + " not implemented!");
	}

	private List<VariableReference> convertParams(List<?> arguments, List<?> argumentTypes) {
		List<VariableReference> result = new ArrayList<VariableReference>();
		int idx = 0;
		for (Object argument : arguments) {
			if ((argument instanceof MethodInvocation) || (argument instanceof ClassInstanceCreation)) {
				assert !nestedCallResults.isEmpty();
				result.add(nestedCallResults.pop());
				continue;
			}
			Class<?> argClass = retrieveTypeClass(argumentTypes.get(idx));
			result.add(retrieveVariableReference(argument, argClass));
			idx++;
		}
		return result;
	}

	private PrimitiveStatement<?> createPrimitiveStatement(Class<?> primitiveClass, Object value) {
		if (String.class.equals(primitiveClass)) {
			return new StringPrimitiveStatement(testCase.getReference(), (String) value);
		}
		if (primitiveClass.equals(Boolean.class) || primitiveClass.equals(boolean.class)) {
			return new BooleanPrimitiveStatement(testCase.getReference(), (Boolean) value);
		}
		if (primitiveClass.equals(Integer.class) || primitiveClass.equals(int.class)) {
			return new IntPrimitiveStatement(testCase.getReference(), ((Number) value).intValue());
		}
		if (primitiveClass.equals(Long.class) || primitiveClass.equals(long.class)) {
			return new LongPrimitiveStatement(testCase.getReference(), ((Number) value).longValue());
		}
		if (primitiveClass.equals(Byte.class) || primitiveClass.equals(byte.class)) {
			return new BytePrimitiveStatement(testCase.getReference(), ((Number) value).byteValue());
		}
		if (primitiveClass.equals(Float.class) || primitiveClass.equals(float.class)) {
			return new FloatPrimitiveStatement(testCase.getReference(), ((Number) value).floatValue());
		}
		if (primitiveClass.equals(Short.class) || primitiveClass.equals(short.class)) {
			return new ShortPrimitiveStatement(testCase.getReference(), ((Number) value).shortValue());
		}
		if (primitiveClass.equals(Double.class) || primitiveClass.equals(double.class)) {
			return new DoublePrimitiveStatement(testCase.getReference(), ((Number) value).doubleValue());
		}
		throw new UnsupportedOperationException("Not all primitives have been implemented!");
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
		throw new UnsupportedOperationException("Cannot doBoxing for class " + primitiveClass);
	}

	private Class<?> loadClass(String className) throws ClassNotFoundException {
		return TestCluster.classLoader.loadClass(className);
		// return Class.forName(className);
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
		throw new UnsupportedOperationException("Number type " + value.getClass() + " not implemented!");
	}

	private VariableReference retrieveCalleeReference(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			return retrieveVariableReference(expression, null);
		}
		throw new RuntimeException("Callee was null for expression: " + expression);
	}

	private Method retrieveMethod(MethodInvocation methodInvocation, List<VariableReference> params) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding != null) {
			return retrieveMethodFromSources(methodInvocation, methodBinding, params);
		}
		return retrieveMethodFromByteCode(methodInvocation);
	}

	private Method retrieveMethodFromByteCode(MethodInvocation methodInvocation) {
		String methodName = methodInvocation.getName().getIdentifier();
		Expression typeExpression = methodInvocation.getExpression();
		Class<?> declaringClass;
		if (typeExpression instanceof MethodInvocation) {
			MethodInvocation parentMethodInvocation = (MethodInvocation) typeExpression;
			IMethodBinding parentMethodBinding = parentMethodInvocation.resolveMethodBinding();
			declaringClass = retrieveTypeClass(parentMethodBinding.getDeclaringClass());
		} else {
			declaringClass = retrieveTypeClass(typeExpression);
		}
		List<Method> result = new ArrayList<Method>();
		outer: for (Method method : declaringClass.getDeclaredMethods()) {
			if (!method.getName().equals(methodName)) {
				continue;
			}
			if (method.getParameterTypes().length != methodInvocation.arguments().size()) {
				continue;
			}
			for (int idx = 0; idx < methodInvocation.arguments().size(); idx++) {
				Expression argExpr = (Expression) methodInvocation.arguments();
				Class<?> argType = retrieveTypeClass(argExpr);
				if (!method.getParameterTypes()[idx].isAssignableFrom(argType)) {
					continue outer;
				}
			}
			result.add(method);
		}
		assert result.size() == 1;
		return result.get(0);
	}

	private Method retrieveMethodFromSources(MethodInvocation methodInvocation, IMethodBinding methodBinding,
			List<VariableReference> params) {
		assert methodBinding != null : "Sources missing!";
		String methodName = methodInvocation.getName().getIdentifier();
		Class<?> clazz = retrieveTypeClass(methodBinding.getDeclaringClass());
		Set<Method> methods = retrieveMethods(clazz, methodName, params);
		try {
			Class<?>[] paramClasses = new Class<?>[methodInvocation.arguments().size()];
			assert methods.size() > 0;
			for (int idx = 0; idx < methodInvocation.arguments().size(); idx++) {
				ITypeBinding paramTypeBinding = methodBinding.getParameterTypes()[idx];
				paramClasses[idx] = retrieveTypeClass(paramTypeBinding);
				VariableReference param = params.get(idx);
				if (!param.isAssignableFrom(paramClasses[idx])) {
					if (methods.size() == 0) {
						throw new IllegalStateException("Param class and argument do not match!");
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

	private Set<Method> retrieveMethods(Class<?> clazz, String methodName, List<VariableReference> params) {
		Set<Method> result = new HashSet<Method>();
		Set<Method> possibleMethods = new HashSet<Method>();
		possibleMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		possibleMethods.addAll(Arrays.asList(clazz.getMethods()));
		outer: for (Method method : possibleMethods) {
			if (method.getName().equals(methodName) && (method.getParameterTypes().length == params.size())) {
				Class<?>[] declaredParamClasses = method.getParameterTypes();
				for (int idx = 0; idx < params.size(); idx++) {
					VariableReference param = params.get(idx);
					Class<?> actualParamClass = param.getVariableClass();
					if (!declaredParamClasses[idx].isAssignableFrom(actualParamClass)) {
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
		throw new RuntimeException("Primitive type of class '" + className + "' is unknown.");
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
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		result = retrieveVariableReference(methodBinding.getReturnType(), null);
		calleeResultMap.put(methodInvocation.toString(), result);
		return result;
	}

	private VariableReference retrieveVariableReference(ArrayCreation arrayCreation) {
		Class<?> arrayType = retrieveTypeClass(arrayCreation.getType());
		AbstractList<?> dimensions = ((AbstractList<?>) arrayCreation
				.getStructuralProperty(ArrayCreation.DIMENSIONS_PROPERTY));
		if (dimensions.size() > 1) {
			throw new RuntimeException("Multidimensional arrays not implemented!");
		}
		Integer length = (Integer) ((NumberLiteral) dimensions.get(0)).resolveConstantExpressionValue();
		ArrayStatement arrayAssignment = new ArrayStatement(testCase.getReference(), arrayType, length);
		testCase.addStatement(arrayAssignment);
		return arrayAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(BooleanLiteral boolLiteral) {
		boolean bool = boolLiteral.booleanValue();
		PrimitiveStatement<Boolean> charAssignment = new BooleanPrimitiveStatement(testCase.getReference(), bool);
		testCase.addStatement(charAssignment);
		return charAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(CharacterLiteral characterLiteral) {
		char character = characterLiteral.charValue();
		PrimitiveStatement<Character> charAssignment = new CharPrimitiveStatement(testCase.getReference(), character);
		testCase.addStatement(charAssignment);
		return charAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(Class<?> varType, ClassInstanceCreation instanceCreation) {
		if ((instanceCreation.getParent() instanceof MethodInvocation)
				|| (instanceCreation.getParent() instanceof ClassInstanceCreation)) {
			VariableReference result = new ValidVariableReference(testCase.getReference(),
					retrieveTypeClass(instanceCreation.getType()));
			nestedCallResults.push(result);
			return result;
		}
		return retrieveVariableReference(instanceCreation.getParent(), varType);
	}

	private VariableReference retrieveVariableReference(InfixExpression infixExpr, Class<?> exprType) {
		if (exprType == null) {
			exprType = retrieveTypeClass(infixExpr);
		}
		VariableReference ref = new VariableReferenceImpl(testCase.getReference(), exprType);
		VariableReference leftOperand = retrieveVariableReference(infixExpr.getLeftOperand(), null);
		Operator operator = Operator.toOperator(infixExpr.getOperator().toString());
		VariableReference rightOperand = retrieveVariableReference(infixExpr.getRightOperand(), null);
		PrimitiveExpression expr = new PrimitiveExpression(testCase.getReference(), ref, leftOperand, operator,
				rightOperand);
		testCase.addStatement(expr);
		return ref;
	}

	private VariableReference retrieveVariableReference(IVariableBinding varBinding) {
		Class<?> varClass = retrieveTypeClass(varBinding.getType());
		VariableReference localVar = testCase.getVariableReference(varBinding);
		if (localVar != null) {
			return localVar;
		}
		return new BoundVariableReferenceImpl(testCase, varClass, varBinding.getName());
	}

	private VariableReference retrieveVariableReference(NullLiteral nullLiteral) {
		Class<?> clazz = retrieveTypeClass(nullLiteral.getParent());
		PrimitiveStatement<?> nullAssignment = new NullStatement(testCase.getReference(), clazz);
		testCase.addStatement(nullAssignment);
		return nullAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(NumberLiteral numberLiteral) {
		Class<?> numberClass = retrieveTypeClass(numberLiteral);
		return retrieveVariableReference(numberLiteral, numberClass);
	}

	private VariableReference retrieveVariableReference(NumberLiteral numberLiteral, Class<?> numberClass) {
		Object value = numberLiteral.resolveConstantExpressionValue();
		PrimitiveStatement<?> numberAssignment = createPrimitiveStatement(numberClass, value);
		testCase.addStatement(numberAssignment);
		return numberAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(PrefixExpression prefixExpr) {
		if (prefixExpr.getOperator() == org.eclipse.jdt.core.dom.PrefixExpression.Operator.MINUS) {
			Class<?> numberClass = retrieveTypeClass(prefixExpr.getOperand());
			return retrieveVariableReference(prefixExpr, numberClass);
		}
		throw new UnsupportedOperationException("Prefix " + prefixExpr + " not implemented!");
	}

	private VariableReference retrieveVariableReference(PrefixExpression prefixExpr, Class<?> numberClass) {
		if (prefixExpr.getOperator() == org.eclipse.jdt.core.dom.PrefixExpression.Operator.MINUS) {
			NumberLiteral numberLiteral = (NumberLiteral) prefixExpr.getOperand();
			Number value = (Number) numberLiteral.resolveConstantExpressionValue();
			value = negate(value);
			PrimitiveStatement<?> numberAssignment = createPrimitiveStatement(numberClass, value);
			testCase.addStatement(numberAssignment);
			return numberAssignment.getReturnValue();
		}
		throw new UnsupportedOperationException("Prefix " + prefixExpr + " not implemented!");
	}

	private VariableReference retrieveVariableReference(QualifiedName qualifiedName) {
		try {
			Class<?> referencedClass = retrieveTypeClass(qualifiedName.getQualifier().resolveTypeBinding());
			Field field = referencedClass.getField(qualifiedName.getName().getIdentifier());
			FieldReference fieldReference = new FieldReference(testCase.getReference(), field);
			Class<?> resultClass = retrieveTypeClass(qualifiedName.resolveTypeBinding());
			FieldStatement fieldStatement = new FieldStatement(testCase.getReference(), field, fieldReference,
					resultClass);
			testCase.addStatement(fieldStatement);
			return fieldStatement.getReturnValue();
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	private VariableReference retrieveVariableReference(StringLiteral stringLiteral) {
		String string = stringLiteral.getLiteralValue();
		PrimitiveStatement<String> stringAssignment = new StringPrimitiveStatement(testCase.getReference(), string);
		testCase.addStatement(stringAssignment);
		return stringAssignment.getReturnValue();
	}

	private VariableReference retrieveVariableReference(VariableDeclaration varDecl) {
		IVariableBinding variableBinding = varDecl.resolveBinding();
		Class<?> clazz = retrieveTypeClass(variableBinding.getType());
		VariableReference result = new BoundVariableReferenceImpl(testCase, clazz, variableBinding.getName());
		testCase.addVariable(variableBinding, result);
		return result;
	}

	private VariableReference retrieveVariableReference(VariableDeclarationFragment varDeclFrgmnt) {
		IVariableBinding variableBinding = varDeclFrgmnt.resolveBinding();
		Class<?> clazz = retrieveTypeClass(variableBinding.getType());
		VariableReference result = new BoundVariableReferenceImpl(testCase, clazz, variableBinding.getName());
		testCase.addVariable(variableBinding, result);
		return result;
	}
}
