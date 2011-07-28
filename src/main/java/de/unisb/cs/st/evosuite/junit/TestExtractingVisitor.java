package de.unisb.cs.st.evosuite.junit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

import de.unisb.cs.st.evosuite.testcase.BooleanPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
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

public class TestExtractingVisitor extends ASTVisitor {
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

	protected static Logger logger = LoggerFactory.getLogger(TestExtractingVisitor.class);
	private List<StatementInterface> currentScope;
	private Map<IVariableBinding, VariableReference> localVars = new HashMap<IVariableBinding, VariableReference>();
	private final CompoundTestCase testCase;
	private final String unqualifiedTest;
	private final String unqualifiedTestMethod;

	public TestExtractingVisitor(CompoundTestCase testCase, String qualifiedTestMethod) {
		super();
		this.testCase = testCase;
		this.unqualifiedTest = qualifiedTestMethod.substring(qualifiedTestMethod.lastIndexOf(".") + 1,
				qualifiedTestMethod.indexOf("#"));
		this.unqualifiedTestMethod = qualifiedTestMethod.substring(qualifiedTestMethod.indexOf("#") + 1);
	}

	@Override
	public void endVisit(EnumDeclaration node) {
		// TODO-JRO Implement method endVisitEnumDeclaration
		logger.warn("Method endVisitEnumDeclaration not implemented!");
		super.endVisit(node);
	}

	@Override
	public void endVisit(Initializer node) {
		// TODO-JRO Implement method endVisitInitializer
		logger.warn("Method endVisitInitializer not implemented!");
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		// TODO-JRO Implement method endVisitMethodDeclaration such that static
		// code is treated
		logger.warn("Method endVisitMethodDeclaration only sparsly implemented!");
		currentScope = null;
		super.endVisit(node);
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		// TODO-JRO Implement method endVisitTypeDeclaration
		logger.warn("Method endVisitTypeDeclaration not implemented!");
		super.endVisit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		logger.warn("Method visitAnonymousClassDeclaration not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		logger.warn("Method visitArrayAccess not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		logger.warn("Method visitArrayCreation not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		logger.warn("Method visitArrayInitializer not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		logger.warn("Method visitArrayType not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		logger.warn("Method visitAssertStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		logger.warn("Method visitAssignment not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		logger.warn("Method visitBooleanLiteral not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		logger.warn("Method visitBreakStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		logger.warn("Method visitCastExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		logger.warn("Method visitCatchClause not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		logger.warn("Method visitCharacterLiteral not implemented!");
		return super.visit(node);
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
	public boolean visit(ConditionalExpression node) {
		logger.warn("Method visitConditionalExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		logger.warn("Method visitConstructorInvocation not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		logger.warn("Method visitContinueStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		logger.warn("Method visitEmptyStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		logger.warn("Method visitEmptyStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		logger.warn("Method visitEnhancedForStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		logger.warn("Method visitEnumConstantDeclaration not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		// TODO-JRO Implement method visitEnumDeclaration
		logger.warn("Method visitEnumDeclaration not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		logger.warn("Method visitFieldAccess not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration) {
		// TODO-JRO Implement method visitFieldDeclaration properly
		retrieveVariableReference(fieldDeclaration.fragments().get(0));
		logger.warn("Method visitFieldDeclaration not implemented!");
		return super.visit(fieldDeclaration);
	}

	@Override
	public boolean visit(ForStatement node) {
		logger.warn("Method visitForStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		logger.warn("Method visitIfStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		logger.warn("Method visitInitializer not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		logger.warn("Method visitInstanceofExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		logger.warn("Method visitLabeledStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberRef node) {
		logger.warn("Method visitMemberValuePair not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		logger.warn("Method visitMemberValuePair not implemented!");
		return super.visit(node);
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
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		Class<?> clazz = retrieveTypeClass(methodBinding.getDeclaringClass());
		Class<?>[] paramClasses = new Class<?>[methodInvocation.arguments().size()];
		for (int idx = 0; idx < methodInvocation.arguments().size(); idx++) {
			ITypeBinding paramTypeBinding = methodBinding.getParameterTypes()[idx];
			paramClasses[idx] = retrieveTypeClass(paramTypeBinding);
		}
		String methodName = methodInvocation.getName().getIdentifier();
		Method method;
		try {
			method = clazz.getMethod(methodName, paramClasses);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		VariableReference callee = retrieveVariableReference(methodInvocation);
		List<VariableReference> params = convertParams(methodInvocation.arguments());
		MethodStatement methodStatement = null;
		if (methodInvocation.getParent() instanceof VariableDeclarationFragment) {
			VariableReference retVal = retrieveVariableReference(methodInvocation.getParent());
			methodStatement = new ValidMethodStatement(testCase, method, callee, retVal, params);
		} else {
			methodStatement = new ValidMethodStatement(testCase, method, callee, method.getDeclaringClass(), params);
		}
		currentScope.add(methodStatement);
		return super.visit(methodInvocation);
	}

	@Override
	public boolean visit(MethodRef node) {
		logger.warn("Method visitMethodRef not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		logger.warn("Method visitMethodRefParameter not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		logger.warn("Method visitNullLiteral not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		logger.warn("Method visitParameterizedType not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		logger.warn("Method visitParenthesizedExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		logger.warn("Method visitPostfixExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		logger.warn("Method visitPrefixExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		logger.warn("Method visitQualifiedType not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		logger.warn("Method visitReturnStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		logger.warn("Method visitSingleMemberAnnotation not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		logger.warn("Method visitSingleVariableDeclaration not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		logger.warn("Method visitSuperFieldAccess not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		logger.warn("Method visitSuperMethodInvocation not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		logger.warn("Method visitSwitchCase not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		logger.warn("Method visitSwitchStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		logger.warn("Method visitSynchronizedStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		logger.warn("Method visitThisExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		logger.warn("Method visitThrowStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		logger.warn("Method visitTryStatement not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (!unqualifiedTest.equals(typeDeclaration.getName().getIdentifier())) {
			throw new UnsupportedOperationException(
					"Method visitTypeDeclaration not implemented for other types than the actual test!");
		}
		return super.visit(typeDeclaration);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		logger.warn("Method visitTypeLiteral not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		logger.warn("Method visitVariableDeclarationExpression not implemented!");
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		logger.warn("Method visitWhileStatement not implemented!");
		return super.visit(node);
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
			if ("I".equals(className)) {
				return Integer.TYPE;
			}
			if ("V".equals(className)) {
				return Void.TYPE;
			}
			if ("Z".equals(className)) {
				return Boolean.TYPE;
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
		if (argument instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) argument;
			if (methodInvocation.getExpression() != null) {
				return retrieveVariableReference(methodInvocation.getExpression());
			}
			throw new IllegalStateException(methodInvocation + " has null expression!");
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
			return new VariableReferenceImpl(testCase, retrieveTypeClass(argument));
		}
		throw new UnsupportedOperationException("Argument type " + argument.getClass() + " not implemented!");

	}

	private List<VariableReference> convertParams(List<?> arguments) {
		List<VariableReference> result = new ArrayList<VariableReference>();
		for (Object argument : arguments) {
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
}
