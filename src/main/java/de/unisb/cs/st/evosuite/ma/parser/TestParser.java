package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LiteralExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.gui.IGUI;
import de.unisb.cs.st.evosuite.testcase.AbstractStatement;
import de.unisb.cs.st.evosuite.testcase.ArrayIndex;
import de.unisb.cs.st.evosuite.testcase.ArrayReference;
import de.unisb.cs.st.evosuite.testcase.ArrayStatement;
import de.unisb.cs.st.evosuite.testcase.AssignmentStatement;
import de.unisb.cs.st.evosuite.testcase.BooleanPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.BytePrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.CharPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.DoublePrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.FieldReference;
import de.unisb.cs.st.evosuite.testcase.FloatPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.IntPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.LongPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ShortPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov
 * 
 */
/**
 * @author Yury Pavlov
 * 
 */
public class TestParser {

	private TypeTable tt;

	private final IGUI gui;

	private TestCase newTestCase;

	public TestParser(IGUI pgui) {
		gui = pgui;
	}

	/**
	 * Parse a testCase in form of {@link String} to List<StatementInterface>
	 * statements, create {@link TestCase} and save it to List of tests in
	 * {@link GeneticAlgorithm}
	 * 
	 * @param testCode
	 *            to parse
	 * @param clazz
	 * @param sgui
	 * @param tests
	 *            destination to save results
	 * @throws IOException
	 * @throws ParseException
	 */
	public TestCase parsTest(String testCode) throws IOException {
		CompilationUnit cu = null;
		tt = new TypeTable();

		testCode = "class DummyCl{void DummyMt(){" + testCode + "}}";
		InputStream inputStream = new ByteArrayInputStream(testCode.getBytes());

		try {
			cu = JavaParser.parse(inputStream);
		} catch (ParseException e) {
			gui.showParseException(e.getMessage());
			// e.printStackTrace();
		} finally {
			inputStream.close();
		}

		List<Expression> exprStmts = getExpressionStmt(cu);
		newTestCase = new DefaultTestCase();
		for (Expression expr : exprStmts) {
			try {
				if (expr instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr varDeclarationExpr = (VariableDeclarationExpr) expr;
					createVariableStatements(varDeclarationExpr);
				}
				if (expr instanceof AssignExpr) {
					AssignExpr assignExpr = (AssignExpr) expr;
					createAssignStatments(assignExpr);
				}
				if (expr instanceof MethodCallExpr) {
					MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
					boolean addStatement = true;
					createMethodStatment(methodCallExpr, addStatement);
				}
			} catch (ParseException e) {
				gui.showParseException("Error in line: " + expr.getBeginLine()
						+ "\nMessage: " + e.getMessage());

				// if res == null, editor & co. stay unchanged
				newTestCase = null;
				break;
			}
		}
		System.out.println("\n-------------------------------------------");
		System.out.println(newTestCase.toCode());
		System.out.println("===========================================");
		return newTestCase;
	}

	/**
	 * Create new Evosuite statements {@link VariableReference} from parsed code
	 * 
	 * @param currentTestCase
	 *            to insert new statements
	 * @param expression
	 *            from parser
	 * @param newTestCase
	 * @throws ParseException
	 */
	private void createVariableStatements(
			VariableDeclarationExpr varDeclarationExpr) throws ParseException {
		Type parserType = varDeclarationExpr.getType();

		// get all vars, but usually there is only one?
		List<VariableDeclarator> vars = varDeclarationExpr.getVars();

		for (VariableDeclarator varDeclarator : vars) {
			AbstractStatement newStatement = null;

			if (parserType instanceof PrimitiveType) {
				if (varDeclarator.getInit() instanceof MethodCallExpr) {
					boolean addStatement = false;
					newStatement = createMethodStatment(
							(MethodCallExpr) varDeclarator.getInit(),
							addStatement);
				} else {
					newStatement = createPrimitiveStatement(parserType,
							varDeclarator);
				}
			}
			if (parserType instanceof ReferenceType) {
				newStatement = createReferenceType(parserType, varDeclarator);
			}

			if (newStatement != null) {
				newTestCase.addStatement(newStatement);
				addNewVarToTT(parserType, varDeclarator, newStatement);
			}
		}
	}

	/**
	 * @param methodCallExpr
	 * @param addStatement
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createMethodStatment(
			MethodCallExpr methodCallExpr, boolean addStatement)
			throws ParseException {
		Expression scope = methodCallExpr.getScope();
		List<Expression> args = methodCallExpr.getArgs();

		Class<?> clazz = converTypeToClass(getType(scope));

		String methodName = methodCallExpr.getName();
		Class<?>[] paramClasses = getVarClasses(args);

		Method method = getMethod(clazz, methodName, paramClasses);

		VariableReference callee = getVarRef(scope);
		List<VariableReference> paramReferences = getVarRefs(args);

		// there is only 3 poss. to call this fun.
		// 1. to call without any assigm. return value
		// 2. assigm. return value to var
		// 3. call for for new var
		AbstractStatement res = null;
		if (addStatement) {
			res = new MethodStatement(newTestCase, method, callee,
					method.getGenericReturnType(), paramReferences);
			newTestCase.addStatement(res);
		} else {
			res = new MethodStatement(newTestCase, method, callee,
					method.getGenericReturnType(), paramReferences);
		}
		return res;
	}

	/**
	 * @param currentTestCase
	 * @param expression
	 * @param newTestCase
	 * @throws ParseException
	 */
	private void createAssignStatments(AssignExpr assignExpr)
			throws ParseException {
		VariableReference varReference = null;
		VariableReference valReference = null;
		switch (assignExpr.getOperator()) {
		case assign:
			varReference = getVarRef(assignExpr.getTarget());
			valReference = getVarRef(assignExpr.getValue());
			break;
		}

		if (varReference != null && valReference != null) {
			newTestCase.addStatement(new AssignmentStatement(newTestCase,
					varReference, valReference));
		} else {
			throw new ParseException(null, "Can not create or find var ref.");
		}
	}

	/**
	 * Convert the variable statement in parser form to the Evosuite statement
	 * 
	 * Boolean, Char, Byte, Short, Int, Long, Float, Double
	 * 
	 * @param newTestCase
	 * @param varDeclarator
	 * 
	 * @param parstype
	 *            type to convert
	 * @return res type of Evosuite
	 * @throws ParseException
	 */
	private PrimitiveStatement<?> createPrimitiveStatement(Type parserType,
			VariableDeclarator varDeclarator) throws ParseException {

		PrimitiveType primType = (PrimitiveType) parserType;

		switch (primType.getType()) {
		case Char:
			return new CharPrimitiveStatement(newTestCase, varDeclarator
					.getInit().toString().charAt(1));
		case Byte:
			return new BytePrimitiveStatement(newTestCase,
					Byte.parseByte(varDeclarator.getInit().toString()));
		case Short:
			return new ShortPrimitiveStatement(newTestCase,
					Short.parseShort(varDeclarator.getInit().toString()));
		case Int:
			return new IntPrimitiveStatement(newTestCase,
					Integer.parseInt(varDeclarator.getInit().toString()));
		case Long:
			return new LongPrimitiveStatement(newTestCase,
					Long.parseLong(varDeclarator.getInit().toString()));
		case Float:
			return new FloatPrimitiveStatement(newTestCase,
					Float.parseFloat(varDeclarator.getInit().toString()));
		case Double:
			return new DoublePrimitiveStatement(newTestCase,
					Double.parseDouble(varDeclarator.getInit().toString()));
		case Boolean:
			return new BooleanPrimitiveStatement(newTestCase,
					Boolean.parseBoolean(varDeclarator.getInit().toString()));
		default:
			throw new IllegalArgumentException(
					"convertPrimitiveType(Type parsType) can't obtain primitive Type");
		}
	}

	/**
	 * 
	 * @param parserType
	 * @param newTestCase
	 * @param variableDeclarator
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createReferenceType(Type parserType,
			VariableDeclarator variableDeclarator) throws ParseException {
		AbstractStatement res = null;
		Expression rightExpression = variableDeclarator.getInit();

		if (rightExpression != null) {
			// case of assign: methCall, just another var, some el of array etc
			if (rightExpression instanceof MethodCallExpr) {
				// TODO can be part of MethodCallExpr of Primitive type!!!
				System.out.println("Can not assigen value by methad call");
			}

			// case of creation new Object
			if (rightExpression instanceof ObjectCreationExpr) {
				ObjectCreationExpr objCreatExpr = (ObjectCreationExpr) rightExpression;

				Class<?> clazz = converTypeToClass(parserType);
				Class<?>[] paramTypes = convertTypesToClasses(objCreatExpr
						.getTypeArgs());
				List<VariableReference> params = getVarRefs(objCreatExpr
						.getArgs());

				Constructor<?> constructor = null;
				try {
					constructor = clazz.getConstructor(paramTypes);
				} catch (SecurityException e) {
					System.out.println("345 TestParser.newConstructorStatemen");
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					System.out.println("348 TestParser.newConstructorStatemen");
					e.printStackTrace();
				}

				res = new ConstructorStatement(newTestCase, constructor, clazz,
						params);
			}

			// Array
			if (rightExpression instanceof ArrayCreationExpr) {
				ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) rightExpression;

				// Array can't be created with var. length
				int arraySize = Integer.parseInt(arrayCreationExpr
						.getDimensions().get(0).toString());
				Class<?> clazz = converTypeToClass(arrayCreationExpr.getType());
				Object array = Array.newInstance(clazz, arraySize);

				res = new ArrayStatement(newTestCase, array.getClass(),
						arraySize);
			}
		} else {
			throw new ParseException(null,
					"There is no right side of declaration ecpression!");
		}

		return res;
	}

	/**
	 * @param parserType
	 * @param varDeclarator
	 * @param newStatement
	 */
	private void addNewVarToTT(Type parserType,
			VariableDeclarator varDeclarator, AbstractStatement newStatement) {
		ArrayList<VariableReference> varRefArray = new ArrayList<VariableReference>();
		varRefArray.addAll(newStatement.getVariableReferences());
		String varBinding = "var" + (varDeclarator.getBeginLine() - 1);
		VariableReference varRef = null;
		for (VariableReference tVarRef : varRefArray) {
			if (varBinding.equals(tVarRef.getName())) {
				varRef = tVarRef;
			}
		}
		tt.addVar(new Var(varDeclarator.getId().getName(), varBinding,
				parserType, varRef));
	}

	/**
	 * Return names of arguments in the method.
	 * 
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private List<VariableReference> getVarRefs(List<Expression> args)
			throws ParseException {
		List<VariableReference> res = new ArrayList<VariableReference>();
		if (args != null) {
			for (Expression expr : args) {
				res.add(getVarRef(expr));
			}
		}
		return res;
	}

	private VariableReference getVarRef(Expression expr) throws ParseException {
		if (expr instanceof NameExpr) {
			String name = ((NameExpr) expr).getName();
			if (!isStatic(name)) {
				return tt.getVarReference(name);
			}
		}
		if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAccExpr = (FieldAccessExpr) expr;
			VariableReference varRef = null;

			// if VariableRef stay null EvoSuite make this call as static
			if (!isStatic(fieldAccExpr.getScope().toString())) {
				varRef = tt.getVarReference(fieldAccExpr.getScope().toString());
			}
			// TODO check if static from another class
			return new FieldReference(newTestCase, getField(fieldAccExpr),
					varRef);
		}
		if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;

			ArrayReference arrayRef = (ArrayReference) tt
					.getVarReference(arrayAccExpr.getName().toString());
			int arrayInd = Integer.parseInt(arrayAccExpr.getIndex().toString());

			return new ArrayIndex(newTestCase, arrayRef, arrayInd);
		}
		return null;
	}

	/**
	 * Return classes of vars.
	 * 
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private Class<?>[] getVarClasses(List<Expression> args)
			throws ParseException {
		List<Class<?>> tmpRes = new ArrayList<Class<?>>();
		if (args != null) {
			for (Expression expr : args) {
				if (expr instanceof NameExpr) {
					tmpRes.add(converTypeToClass(getType(expr)));
				}
				if (expr instanceof FieldAccessExpr) {
					tmpRes.add(getField(expr).getType());
				}
				if (expr instanceof ArrayAccessExpr) {
					ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;

					ArrayReference arrayRef = (ArrayReference) tt
							.getVarReference(arrayAccExpr.getName().toString());
					tmpRes.add(arrayRef.getComponentClass());
					System.out.println(arrayRef.getComponentClass());
				}
			}
		}
		Class<?>[] res = new Class<?>[tmpRes.size()];
		res = tmpRes.toArray(res);
		return res;
	}

	/**
	 * @throws ParseException
	 * 
	 */
	private Field getField(Expression expr) throws ParseException {
		FieldAccessExpr fieldExpr = (FieldAccessExpr) expr;

		Class<?> clazz = converTypeToClass(getType(fieldExpr.getScope()));

		try {
			return clazz.getField(fieldExpr.getField());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param typeArgs
	 * @return
	 * @throws ParseException
	 */
	private Class<?>[] convertTypesToClasses(List<Type> typeArgs)
			throws ParseException {
		if (typeArgs == null) {
			return null;
		}

		List<Class<?>> tmpRes = new ArrayList<Class<?>>();
		for (Type type : typeArgs) {
			tmpRes.add(converTypeToClass(type));
		}

		Class<?>[] res = new Class<?>[tmpRes.size()];
		res = tmpRes.toArray(res);
		return res;
	}

	/**
	 * @param typeArgs
	 * @return
	 * @throws ParseException
	 */
	private Class<?> converTypeToClass(Type parsType) throws ParseException {
		if (parsType instanceof PrimitiveType) {
			PrimitiveType primitiveParamType = (PrimitiveType) parsType;

			return convertPrimitiveType(primitiveParamType);
		}
		if (parsType instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) parsType;
			return convertRefType(refType);
		}
		if (parsType instanceof ClassOrInterfaceType) {
			return convertCOIType(parsType);
		}
		if (parsType instanceof VoidType) {
			throw new ParseException(null, "Can not load class for VoidType.");
		}
		if (parsType instanceof WildcardType) {
			throw new ParseException(null,
					"Can not load class for WildcardType: " + parsType);
		}
		return null;
	}

	/**
	 * @param primitiveParamType
	 * @return
	 * @throws ParseException
	 */
	private Class<?> convertPrimitiveType(PrimitiveType primitiveParamType)
			throws ParseException {
		switch (primitiveParamType.getType()) {
		case Char:
			return Character.TYPE;
		case Byte:
			return Byte.TYPE;
		case Short:
			return Short.TYPE;
		case Int:
			return Integer.TYPE;
		case Long:
			return Long.TYPE;
		case Float:
			return Float.TYPE;
		case Double:
			return Double.TYPE;
		case Boolean:
			return Boolean.TYPE;
		default:
			throw new ParseException(null,
					"convertParams(Type parsType) can't obtain primitive Type");
		}
	}

	/**
	 * @param refType
	 * @return
	 * @throws ParseException
	 */
	private Class<?> convertRefType(ReferenceType refType)
			throws ParseException {
		String fullClassName = Properties.PROJECT_PREFIX + "."
				+ refType.getType();
		try {
			return TestCluster.classLoader.loadClass(fullClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ParseException(null,
					"Can not load class for ReferenceType: " + fullClassName);
		}
	}

	/**
	 * @param parsType
	 * @return
	 * @throws ParseException
	 */
	private Class<?> convertCOIType(Type parsType) throws ParseException {
		String fullClassName = Properties.PROJECT_PREFIX + "." + parsType;
		try {
			return TestCluster.classLoader.loadClass(fullClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ParseException(null,
					"Can not load class for ClassOrInterfaceType: "
							+ fullClassName);
		}
	}

	/**
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 * @throws ParseException
	 */
	private Method getMethod(Class<?> clazz, String methodName,
			Class<?>[] parameterTypes) throws ParseException {
		System.out.println("adsasdzxc " + parameterTypes.length);
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new ParseException(null, "SecurityException by getMethod.");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			String classNames = "";
			for (Class<?> paramType : parameterTypes) {
				classNames += paramType.getName() + " ";
				System.out.println(paramType.getCanonicalName());
				System.out.println(paramType.getModifiers());
				System.out.println(paramType.getName());
				System.out.println(paramType.getSimpleName());
			}
			throw new ParseException(null, "Can not find the method: "
					+ methodName + " with parameter(s): " + classNames);
		}
	}

	/**
	 * Return parser's type of expr. F.e. int instance.fieldInt it's
	 * type(instance). It is impossible to obtain type of field here. But we can
	 * load class and get field's class from there. See getVarClasses.
	 * 
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	private Type getType(Expression expr) throws ParseException {
		if (expr instanceof NameExpr) {
			String name = ((NameExpr) expr).getName();
			if (isStatic(name)) {
				return new ClassOrInterfaceType(0, 0, 0, 0, null, name, null);
			} else {
				return tt.getType(name);
			}
		}
		if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAcExpr = (FieldAccessExpr) expr;

			if (!isStatic(fieldAcExpr.getScope().toString())) {
				return tt.getType(fieldAcExpr.getScope());
			}
		}
		return null;
	}

	/**
	 * Return parsed expression statements of new {@link TestCase} from GUI
	 * Editor
	 * 
	 * @param cu
	 */
	private List<Expression> getExpressionStmt(CompilationUnit cu) {
		List<Expression> res = new ArrayList<Expression>();
		TypeDeclaration typeDeclaration = cu.getTypes().get(0);
		BodyDeclaration member = typeDeclaration.getMembers().get(0);
		// There is only one method in new TestCase - Dummy
		MethodDeclaration method = null;

		if (member instanceof MethodDeclaration) {
			method = (MethodDeclaration) member;
		}

		for (Statement statement : method.getBody().getStmts()) {
			// probably all statements of TestCase are ExpressionStmt
			if (statement instanceof ExpressionStmt) {
				res.add(((ExpressionStmt) statement).getExpression());
			}
		}

		return res;
	}

	/**
	 * @param varName
	 * @return
	 */
	public static boolean isStatic(String varName) {
		return Character.isUpperCase(varName.charAt(0));
	}

	private void printTypeOfExpr(Expression expr) {
		if (expr instanceof AnnotationExpr) {
			System.out.println("Expr: " + expr + " is AnnotationExpr.");
		}
		if (expr instanceof ArrayAccessExpr) {
			System.out.println("Expr: " + expr + " is ArrayAccessExpr.");
		}
		if (expr instanceof ArrayCreationExpr) {
			System.out.println("Expr: " + expr + " is ArrayCreationExpr.");
		}
		if (expr instanceof ArrayInitializerExpr) {
			System.out.println("Expr: " + expr + " is ArrayInitializerExpr.");
		}
		if (expr instanceof AssignExpr) {
			System.out.println("Expr: " + expr + " is AssignExpr.");
		}
		if (expr instanceof BinaryExpr) {
			System.out.println("Expr: " + expr + " is BinaryExpr.");
		}
		if (expr instanceof BooleanLiteralExpr) {
			System.out.println("Expr: " + expr + " is BooleanLiteralExpr.");
		}
		if (expr instanceof CastExpr) {
			System.out.println("Expr: " + expr + " is CastExpr.");
		}
		if (expr instanceof CharLiteralExpr) {
			System.out.println("Expr: " + expr + " is CharLiteralExpr.");
		}
		if (expr instanceof ClassExpr) {
			System.out.println("Expr: " + expr + " is ClassExpr.");
		}
		if (expr instanceof ConditionalExpr) {
			System.out.println("Expr: " + expr + " is ConditionalExpr.");
		}
		if (expr instanceof DoubleLiteralExpr) {
			System.out.println("Expr: " + expr + " is DoubleLiteralExpr.");
		}
		if (expr instanceof EnclosedExpr) {
			System.out.println("Expr: " + expr + " is EnclosedExpr.");
		}
		if (expr instanceof FieldAccessExpr) {
			System.out.println("Expr: " + expr + " is FieldAccessExpr.");
		}
		if (expr instanceof InstanceOfExpr) {
			System.out.println("Expr: " + expr + " is InstanceOfExpr.");
		}
		if (expr instanceof IntegerLiteralExpr) {
			System.out.println("Expr: " + expr + " is IntegerLiteralExpr.");
		}
		if (expr instanceof IntegerLiteralMinValueExpr) {
			System.out.println("Expr: " + expr
					+ " is IntegerLiteralMinValueExpr.");
		}
		if (expr instanceof LiteralExpr) {
			System.out.println("Expr: " + expr + " is LiteralExpr.");
		}
		if (expr instanceof LongLiteralExpr) {
			System.out.println("Expr: " + expr + " is LongLiteralExpr.");
		}
		if (expr instanceof LongLiteralMinValueExpr) {
			System.out
					.println("Expr: " + expr + " is LongLiteralMinValueExpr.");
		}
		if (expr instanceof MarkerAnnotationExpr) {
			System.out.println("Expr: " + expr + " is MarkerAnnotationExpr.");
		}
		if (expr instanceof MethodCallExpr) {
			System.out.println("Expr: " + expr + " is MethodCallExpr.");
		}
		if (expr instanceof NameExpr) {
			System.out.println("Expr: " + expr + " is NameExpr.");
		}
		if (expr instanceof NormalAnnotationExpr) {
			System.out.println("Expr: " + expr + " is NormalAnnotationExpr.");
		}
		if (expr instanceof NullLiteralExpr) {
			System.out.println("Expr: " + expr + " is NullLiteralExpr.");
		}
		if (expr instanceof ObjectCreationExpr) {
			System.out.println("Expr: " + expr + " is ObjectCreationExpr.");
		}
		if (expr instanceof QualifiedNameExpr) {
			System.out.println("Expr: " + expr + " is QualifiedNameExpr.");
		}
		if (expr instanceof SingleMemberAnnotationExpr) {
			System.out.println("Expr: " + expr
					+ " is SingleMemberAnnotationExpr.");
		}
		if (expr instanceof StringLiteralExpr) {
			System.out.println("Expr: " + expr + " is StringLiteralExpr.");
		}
		if (expr instanceof SuperExpr) {
			System.out.println("Expr: " + expr + " is SuperExpr.");
		}
		if (expr instanceof ThisExpr) {
			System.out.println("Expr: " + expr + " is ThisExpr.");
		}
		if (expr instanceof UnaryExpr) {
			System.out.println("Expr: " + expr + " is UnaryExpr.");
		}
		if (expr instanceof VariableDeclarationExpr) {
			System.out
					.println("Expr: " + expr + " is VariableDeclarationExpr.");
		}
	}

}
