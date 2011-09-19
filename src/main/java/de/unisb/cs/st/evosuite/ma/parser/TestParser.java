package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
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
public class TestParser {

	/**
	 * @uml.property name="tt"
	 * @uml.associationEnd
	 */
	private TypeTable tt;

	/**
	 * @uml.property name="gui"
	 * @uml.associationEnd
	 */
	private final IGUI gui;

	/**
	 * @uml.property name="newTestCase"
	 * @uml.associationEnd
	 */
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
		TestCase res = null;
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
		try {
			for (Expression expr : exprStmts) {

				if (expr instanceof VariableDeclarationExpr) {
					createVariableStatements(expr);
				}

				if (expr instanceof AssignExpr) {
					createAssignStatments(expr);
				}

				if (expr instanceof MethodCallExpr) {
					boolean addStatement = true;
					createMethodStatment(expr, addStatement);
				}
			}
			System.out.println("\n-------------------------------------------");
			System.out.println(newTestCase.toCode());
			System.out.println("===========================================");
			res = newTestCase;
		} catch (ParseException e) {
			gui.showParseException(e.getMessage());
		}
		return res;
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
	private void createVariableStatements(Expression expr)
			throws ParseException {
		VariableDeclarationExpr varDeclarationExpr = (VariableDeclarationExpr) expr;
		Type parserType = varDeclarationExpr.getType();

		// get all vars, but usually there is only one?
		List<VariableDeclarator> vars = varDeclarationExpr.getVars();

		for (VariableDeclarator varDeclarator : vars) {
			AbstractStatement newStatement = null;

			if (parserType instanceof PrimitiveType) {
				if (varDeclarator.getInit() instanceof MethodCallExpr) {
					boolean addStatement = false;
					newStatement = createMethodStatment(
							varDeclarator.getInit(), addStatement);
				} else {
					newStatement = createPrimitiveStatement(parserType,
							varDeclarator);
				}
			}
			if (parserType instanceof ClassOrInterfaceType) {
				throw new ParseException(null,
						"ClassOrInterfaceType not implemented yet");
			}
			if (parserType instanceof ReferenceType) {
				newStatement = createReferenceType(parserType, varDeclarator);
			}
			if (parserType instanceof WildcardType) {
				throw new ParseException(null,
						"WildcardType not implemented yet");
			}

			if (newStatement != null) {
				newTestCase.addStatement(newStatement);

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
		}
	}

	private AbstractStatement createMethodStatment(Expression expr,
			boolean addStatement) throws ParseException {
		MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
		String methodName = methodCallExpr.getName();
		String scope = methodCallExpr.getScope().toString();
		System.out.println("Scope: " + scope);
		// load class of method
//		Class<?> clazz = getClass(scope);
		Class<?> clazz = getClass(scope);
		// get callee if it's not a static method
		VariableReference callee = getCallee(scope);
		// get method's arguments and retrieve class
		Class<?>[] parameterTypes = getVarTypes(methodCallExpr.getArgs());
		// load properly method from a class
		Method method = getMethod(clazz, methodName, parameterTypes);

		List<VariableReference> paramReferences = getVarRefs(methodCallExpr
				.getArgs());

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
	private void createAssignStatments(Expression expr) throws ParseException {
		AssignExpr assignExpr = (AssignExpr) expr;

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
				System.out.println("Can't assigen value by methad call");
			}

			// case of creation new Object
			if (rightExpression instanceof ObjectCreationExpr) {
				ObjectCreationExpr objCreatExpr = (ObjectCreationExpr) rightExpression;

				Class<?> clazz = getClassForType(parserType);
				Class<?>[] paramTypes = getClassesForTypes(objCreatExpr
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
				Class<?> clazz = getClassForType(arrayCreationExpr.getType());
				Object array = Array.newInstance(clazz, arraySize);

				res = new ArrayStatement(newTestCase, array.getClass(),
						arraySize);
				// try {
				// res = new ArrayStatement(newTestCase, Class.forName("[I"),
				// arraySize);
				// } catch (ClassNotFoundException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

			}
		} else {
			throw new ParseException(null,
					"There is no right side of declaration ecpression!");
		}

		return res;
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
		String varName = "";
		if (expr instanceof NameExpr) {
			NameExpr nameExpr = (NameExpr) expr;

			varName = nameExpr.getName();

			return tt.getVarReference(varName);
		}
		if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAcExpr = (FieldAccessExpr) expr;

			Field field = getField(fieldAcExpr);
			VariableReference varRef = null;
			// if VariableRef stay null EvoSuite make this call as static
			if (!isStatic(fieldAcExpr.getScope().toString())) {
				varRef = tt.getVarReference(fieldAcExpr.getScope().toString());
			}

			return new FieldReference(newTestCase, field, varRef);
		}
		if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;

			System.out.println("Array access line " + arrayAccExpr);
		}
		return null;
	}

	/**
	 * Return parser's types of arguments in the method.
	 * 
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private Class<?>[] getVarTypes(List<Expression> args) throws ParseException {
		List<Class<?>> tmpRes = new ArrayList<Class<?>>();
		if (args != null) {
			for (Expression expr : args) {
				if (expr instanceof NameExpr) {
					NameExpr nameExpr = (NameExpr) expr;
					tmpRes.add(getClass(nameExpr.getName()));
				}
				if (expr instanceof FieldAccessExpr) {
					tmpRes.add(getField(expr).getType());
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
		String scopeStr = fieldExpr.getScope().toString();
		String fieldStr = fieldExpr.getField().toString();

		// load class
		Class<?> clazz = getClass(scopeStr);

		try {
			return clazz.getField(fieldStr);
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
	private Class<?>[] getClassesForTypes(List<Type> typeArgs)
			throws ParseException {
		if (typeArgs == null) {
			return null;
		}

		List<Class<?>> tmpRes = new ArrayList<Class<?>>();
		for (Type type : typeArgs) {
			tmpRes.add(getClassForType(type));
		}

		Class<?>[] res = new Class<?>[tmpRes.size()];
		res = tmpRes.toArray(res);
		return res;
	}

	/**
	 * Rerurn Class<?> of variable. In both case (if static or non static)
	 * 
	 * @throws ParseException
	 * 
	 */
	private Class<?> getClass(String calleeName) throws ParseException {
		if (isStatic(calleeName)) {
			// load static class
			return getClassForClName(calleeName);
		} else {
			// look in TT type of var and load class
			return getClassForType(tt.getType(calleeName));
		}
	}

	/**
	 * @param typeArgs
	 * @return
	 * @throws ParseException
	 */
	private Class<?> getClassForType(Type parsType) throws ParseException {
		if (parsType instanceof PrimitiveType) {
			PrimitiveType primitiveParamType = (PrimitiveType) parsType;

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
				throw new IllegalArgumentException(
						"convertParams(Type parsType) can't obtain primitive Type");
			}
		}
		if (parsType instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) parsType;
			String fullClassName = Properties.PROJECT_PREFIX + "."
					+ refType.getType();
			try {
				return TestCluster.classLoader.loadClass(fullClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new ParseException(null, "Can not load class: "
						+ fullClassName);
			}
		}
		if (parsType instanceof ClassOrInterfaceType) {
			String fullClassName = Properties.PROJECT_PREFIX + "." + parsType;
			try {
				return TestCluster.classLoader.loadClass(fullClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new ParseException(null, "Can not load class: "
						+ fullClassName);
			}
		}
		if (parsType instanceof VoidType) {
			throw new ParseException(null, "Can not load void type:");
		}
		if (parsType instanceof WildcardType) {
			throw new ParseException(null, "Can not load WildcardType: "
					+ parsType);
		}
		return null;
	}

	private Class<?> getClassForClName(String statClsName)
			throws ParseException {
		String fullClassName = Properties.PROJECT_PREFIX + "." + statClsName;
		try {
			return TestCluster.classLoader.loadClass(fullClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ParseException(null, "Can not load class: "
					+ fullClassName);
		}
	}

	/**
	 * @param clazz
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	private Method getMethod(Class<?> clazz, String methodName,
			Class<?>[] parameterTypes) {
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			System.out.println("In TestParser.createNewMethodCallStatments():");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			gui.showParseException(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param calleeName
	 * @return
	 * @throws ParseException
	 */
	private VariableReference getCallee(String calleeName)
			throws ParseException {
		if (!isStatic(calleeName)) {
			return tt.getVarReference(calleeName);
		}
		return null;
	}

	/**
	 * 
	 */
	private boolean isStatic(String calleeName) {
		// TODO there is a another way to check static instances (with modif.)
		return Character.isUpperCase(calleeName.charAt(0));
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

}
