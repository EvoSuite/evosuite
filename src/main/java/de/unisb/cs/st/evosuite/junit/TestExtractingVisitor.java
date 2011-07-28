package de.unisb.cs.st.evosuite.junit;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

import de.unisb.cs.st.evosuite.testcase.BooleanPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.FieldReference;
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.IntPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.LongPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveExpression;
import de.unisb.cs.st.evosuite.testcase.PrimitiveExpression.Operator;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.StringPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.testcase.VariableReferenceImpl;

public class TestExtractingVisitor extends LoggingVisitor {

	private static class BoundVariableReferenceImpl extends VariableReferenceImpl {
		protected String name;

		public BoundVariableReferenceImpl(CompoundTestCase testCase, java.lang.reflect.Type type, String name) {
			super(testCase, type);
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getStPosition() {
			if (((CompoundTestCase) testCase).isFinished()) {
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
		public ValidVariableReference(TestCase testCase, java.lang.reflect.Type type) {
			super(testCase, type);
		}

		@Override
		public int getStPosition() {
			if (((CompoundTestCase) testCase).isFinished()) {
				return super.getStPosition();
			}
			return -1;
		}
	}

	private static class WrongMethodBindingException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public WrongMethodBindingException() {
			// TODO Auto-generated constructor stub
		}
	}

	protected static Logger logger = Logger.getLogger(TestExtractingVisitor.class);
	private List<StatementInterface> currentScope;
	private Map<IVariableBinding, VariableReference> localVars = new HashMap<IVariableBinding, VariableReference>();
	private final CompoundTestCase testCase;
	private final String unqualifiedTest;
	private final String unqualifiedTestMethod;

	private VariableReference nestedCallResult;

	public TestExtractingVisitor(CompoundTestCase testCase, String qualifiedTestMethod) {
		super();
		this.testCase = testCase;
		this.unqualifiedTest = qualifiedTestMethod.substring(qualifiedTestMethod.lastIndexOf(".") + 1,
				qualifiedTestMethod.indexOf("#"));
		this.unqualifiedTestMethod = qualifiedTestMethod.substring(qualifiedTestMethod.indexOf("#") + 1);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		// TODO-JRO Implement method endVisitMethodDeclaration
		// such that static code is treated
		logger.warn("Method endVisitMethodDeclaration only sparsly implemented!");
		currentScope = null;
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodInvocation methodInvocation) {
		List<VariableReference> params = convertParams(methodInvocation.arguments());
		Method method = getMethod(methodInvocation, params);
		VariableReference callee = null;
		if (!Modifier.isStatic(method.getModifiers())) {
			callee = retrieveCalleeReference(methodInvocation);
		}
		MethodStatement methodStatement = null;
		ASTNode parent = methodInvocation.getParent();
		if (parent instanceof ExpressionStatement) {
			VariableReference retVal = new ValidVariableReference(testCase, method.getReturnType());
			methodStatement = new ValidMethodStatement(testCase, method, callee, retVal, params);
		} else {
			VariableReference retVal = retrieveResultReference(methodInvocation);
			methodStatement = new ValidMethodStatement(testCase, method, callee, retVal, params);
			if (parent instanceof MethodInvocation) {
				nestedCallResult = retVal;
			}
		}
		currentScope.add(methodStatement);
		super.visit(methodInvocation);
	}

	@Override
	public boolean visit(ClassInstanceCreation instanceCreation) {
		List<?> paramTypes = Arrays.asList(instanceCreation.resolveConstructorBinding().getParameterTypes());
		List<?> paramValues = instanceCreation.arguments();
		Constructor<?> constructor = retrieveConstructor(instanceCreation.getType(), paramTypes, paramValues);
		List<VariableReference> params = convertParams(instanceCreation.arguments());
		VariableReference retVal = retrieveVariableReference(instanceCreation.getParent());
		ConstructorStatement statement = new ValidConstructorStatement(testCase, constructor, retVal, params);
		currentScope.add(statement);
		return super.visit(instanceCreation);
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		// TODO-JRO Implement method visitFieldDeclaration properly
		retrieveVariableReference(fieldDeclaration.fragments().get(0));
		logger.warn("Method visitFieldDeclaration not properly implemented!");
		return super.visit(fieldDeclaration);
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (unqualifiedTestMethod.equals(methodDeclaration.getName().getIdentifier())) {
			currentScope = testCase.getTestMethod();
		} else {
			logger.warn("Test method is '" + unqualifiedTestMethod + "' but found method declaration '"
					+ methodDeclaration.getName().getIdentifier() + "'.");
			throw new UnsupportedOperationException("Method visitMethodDeclaration not completely implemented!");
		}
		return super.visit(methodDeclaration);
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (!unqualifiedTest.equals(typeDeclaration.getName().getIdentifier())) {
			throw new UnsupportedOperationException(
					"Method visitTypeDeclaration not implemented for other types than the actual test!");
		}
		return super.visit(typeDeclaration);
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
			// TODO-JRO Needs more sophisticated resolving of the constructor:
			// Since the concrete classes of the arguments can only be known at
			// runtime, we need to warn the user if we have an ambiguity.
			constructor = clazz.getConstructor(argClasses);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		return constructor;
	}

	protected Class<?> retrieveTypeClass(Object argument) {
		if (argument instanceof SimpleType) {
			SimpleType simpleType = (SimpleType) argument;
			return retrieveTypeClass(simpleType.resolveBinding());
		}
		if (argument instanceof ITypeBinding) {
			ITypeBinding binding = (ITypeBinding) argument;
			String className = binding.getBinaryName();
			if (className.length() == 1) {
				return retrievePrimitiveClass(className);
			}
			try {
				return Class.forName(className);
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
		throw new UnsupportedOperationException("Retrieval of type " + argument.getClass() + " not implemented yet!");
	}

	protected VariableReference retrieveVariableReference(Object argument) {
		if (argument instanceof ClassInstanceCreation) {
			return retrieveVariableReference(((ClassInstanceCreation) argument).getParent());
		}
		if (argument instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment varDeclFrgmnt = (VariableDeclarationFragment) argument;
			IVariableBinding variableBinding = varDeclFrgmnt.resolveBinding();
			Class<?> clazz = retrieveTypeClass(variableBinding.getType());
			VariableReference result = new BoundVariableReferenceImpl(testCase, clazz, variableBinding.getName());
			localVars.put(variableBinding, result);
			return result;
		}
		if (argument instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) argument;
			return retrieveVariableReference(simpleName.resolveBinding());
		}
		if (argument instanceof IVariableBinding) {
			IVariableBinding varBinding = (IVariableBinding) argument;
			Class<?> varClass = retrieveTypeClass(varBinding.getType());
			VariableReference localVar = localVars.get(varBinding);
			if (localVar != null) {
				return localVar;
			}
			return new BoundVariableReferenceImpl(testCase, varClass, varBinding.getName());
		}
		if (argument instanceof InfixExpression) {
			InfixExpression infixExpr = (InfixExpression) argument;
			ITypeBinding refTypeBinding = ASTResolving.guessBindingForReference(infixExpr);
			VariableReference ref = new VariableReferenceImpl(testCase, retrieveTypeClass(refTypeBinding));
			VariableReference leftOperand = retrieveVariableReference(infixExpr.getLeftOperand());
			Operator operator = Operator.toOperator(infixExpr.getOperator().toString());
			VariableReference rightOperand = retrieveVariableReference(infixExpr.getRightOperand());
			PrimitiveExpression expr = new PrimitiveExpression(testCase, ref, leftOperand, operator, rightOperand);
			currentScope.add(expr);
			return ref;
		}
		if (argument instanceof ExpressionStatement) {
			ExpressionStatement exprStmt = (ExpressionStatement) argument;
			Expression expression = exprStmt.getExpression();
			return retrieveVariableReference(expression);
		}
		if (argument instanceof StringLiteral) {
			String string = ((StringLiteral) argument).getLiteralValue();
			PrimitiveStatement<String> stringAssignment = new StringPrimitiveStatement(testCase, string);
			currentScope.add(stringAssignment);
			return stringAssignment.getReturnValue();
		}
		if (argument instanceof NumberLiteral) {
			NumberLiteral numberLiteral = (NumberLiteral) argument;
			Class<?> numberClass = retrieveTypeClass(numberLiteral);
			Object value = numberLiteral.resolveConstantExpressionValue();
			PrimitiveStatement<?> numberAssignment = createNumericalPrimitiveStatement(numberClass, value);
			currentScope.add(numberAssignment);
			return numberAssignment.getReturnValue();
		}
		if (argument instanceof ITypeBinding) {
			return new ValidVariableReference(testCase, retrieveTypeClass(argument));
		}
		if (argument instanceof QualifiedName) {
			try {
				QualifiedName qualifiedName = (QualifiedName) argument;
				Class<?> referencedClass = retrieveTypeClass(qualifiedName.getQualifier().resolveTypeBinding());
				Field field = referencedClass.getField(qualifiedName.getName().getIdentifier());
				FieldReference fieldReference = new FieldReference(testCase, field);
				Class<?> resultClass = retrieveTypeClass(qualifiedName.resolveTypeBinding());
				FieldStatement fieldStatement = new FieldStatement(testCase, field, fieldReference, resultClass);
				currentScope.add(fieldStatement);
				return fieldStatement.getReturnValue();
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		throw new UnsupportedOperationException("Argument type " + argument.getClass() + " not implemented!");
	}

	private List<VariableReference> convertParams(List<?> arguments) {
		List<VariableReference> result = new ArrayList<VariableReference>();
		for (Object argument : arguments) {
			if (argument instanceof MethodInvocation) {
				assert nestedCallResult != null;
				result.add(nestedCallResult);
				nestedCallResult = null;
				continue;
			}
			result.add(retrieveVariableReference(argument));
		}
		return result;
	}

	private NumericalPrimitiveStatement<?> createNumericalPrimitiveStatement(Class<?> numberClass, Object value) {
		if (numberClass.equals(Boolean.class) || numberClass.equals(boolean.class)) {
			return new BooleanPrimitiveStatement(testCase, (Boolean) value);
		}
		if (numberClass.equals(Integer.class) || numberClass.equals(int.class)) {
			return new IntPrimitiveStatement(testCase, (Integer) value);
		}
		if (numberClass.equals(Long.class) || numberClass.equals(long.class)) {
			return new LongPrimitiveStatement(testCase, (Long) value);
		}
		throw new UnsupportedOperationException("Not all primitives have been implemented!");
	}

	private Class<?> doBoxing(Class<?> clazz) {
		if (clazz.equals(long.class)) {
			return Long.class;
		}
		if (clazz.equals(int.class)) {
			return Integer.class;
		}
		if (clazz.equals(short.class)) {
			return Short.class;
		}
		if (clazz.equals(byte.class)) {
			return Byte.class;
		}
		if (clazz.equals(boolean.class)) {
			return Boolean.class;
		}
		if (clazz.equals(char.class)) {
			return Character.class;
		}
		if (clazz.equals(double.class)) {
			return Double.class;
		}
		if (clazz.equals(float.class)) {
			return Float.class;
		}
		throw new UnsupportedOperationException("Cannot doBoxing for class " + clazz);
	}

	private Method getMethod(MethodInvocation methodInvocation, List<VariableReference> params) {
		String methodName = methodInvocation.getName().getIdentifier();
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		Class<?> clazz = retrieveTypeClass(methodBinding.getDeclaringClass());
		List<Method> methods = getMethods(clazz, methodName, params.size());
		try {
			Class<?>[] paramClasses = new Class<?>[methodInvocation.arguments().size()];
			assert methods.size() > 0;
			for (int idx = 0; idx < methodInvocation.arguments().size(); idx++) {
				ITypeBinding paramTypeBinding = methodBinding.getParameterTypes()[idx];
				paramClasses[idx] = retrieveTypeClass(paramTypeBinding);
				VariableReference param = params.get(idx);
				if (!param.isAssignableFrom(paramClasses[idx])) {
					if (methods.size() == 1) {
						throw new IllegalStateException("Param class and argument do not match!");
					}
					throw new WrongMethodBindingException();
				}
			}
			try {
				return clazz.getMethod(methodName, paramClasses);
			} catch (Exception exc) {
				throw new RuntimeException(exc);
			}
		} catch (WrongMethodBindingException exc) {
			logger.debug("The resolved method binding is wrong. Will manually correct it...");
			return getMethodForParams(methods, params);
		}
	}

	private Method getMethodForParams(List<Method> methods, List<VariableReference> params) {
		outer: for (Method method : methods) {
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
			return method;
		}
		throw new IllegalStateException("Param classes and arguments do not match!");
	}

	private List<Method> getMethods(Class<?> clazz, String methodName, int size) {
		List<Method> result = new ArrayList<Method>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				if (method.getParameterTypes().length == size) {
					result.add(method);
				}
			}
		}
		return result;
	}

	private VariableReference retrieveCalleeReference(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			return retrieveVariableReference(expression);
		}
		throw new IllegalStateException(methodInvocation + " has null expression!");
	}

	private Class<?> retrievePrimitiveClass(String className) {
		if ("B".equals(className)) {
			return Byte.TYPE;
		}
		if ("C".equals(className)) {
			return Character.TYPE;
		}
		if ("D".equals(className)) {
			return Double.TYPE;
		}
		if ("F".equals(className)) {
			return Float.TYPE;
		}
		if ("I".equals(className)) {
			return Integer.TYPE;
		}
		if ("J".equals(className)) {
			return Long.TYPE;
		}
		if ("L".equals(className)) {
			return Class.class;
		}
		if ("S".equals(className)) {
			return Short.TYPE;
		}
		if ("V".equals(className)) {
			return Void.TYPE;
		}
		if ("Z".equals(className)) {
			return Boolean.TYPE;
		}
		if ("[".equals(className)) {
			return Array.class;
		}
		throw new RuntimeException("Primitive type of class '" + className + "' is unknown.");
	}

	private VariableReference retrieveResultReference(MethodInvocation methodInvocation) {
		ASTNode parent = methodInvocation.getParent();
		if (parent instanceof VariableDeclarationFragment) {
			return retrieveVariableReference(parent);
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		return retrieveVariableReference(methodBinding.getReturnType());
	}
}
