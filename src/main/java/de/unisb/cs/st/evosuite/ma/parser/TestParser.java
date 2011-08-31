package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
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
import japa.parser.ast.type.WildcardType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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

	private final TypeTable tt = new TypeTable();

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
		TestCase res = null;

		CompilationUnit cu = null;
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
					createMethodCallStatments(expr);
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
				newStatement = createPrimitiveStatement(parserType,
						varDeclarator);
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
				tt.addVariableReference(newStatement.getVariableReferences());
				tt.addVar(new Var(varDeclarator.getId().toString(), parserType));
				newTestCase.addStatement(newStatement);
			}
		}
	}

	/**
	 * @param currentTestCase
	 * @param expression
	 * @param newTestCase
	 * @throws ParseException
	 */
	private void createMethodCallStatments(Expression expr)
			throws ParseException {
		MethodCallExpr methodCallExpr = (MethodCallExpr) expr;

		List<Type> argTypes = getVarTypesFromTT(methodCallExpr.getArgs());
		Class<?>[] parameterTypes = convertVarTypes(argTypes);

		String methodName = methodCallExpr.getName();

		// look in TT type of var and load right class
		Class<?> clazz = getClass(tt.getType(methodCallExpr.getScope()
				.toString()));

		Method method = null;
		try {
			method = clazz.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			System.out.println("In TestParser.createNewMethodCallStatments():");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			gui.showParseException(e.getMessage());
			// e.printStackTrace();
		}

		String calleeName = methodCallExpr.getScope().toString();
		VariableReference callee = tt.getVarReference(calleeName);

		List<VariableReference> paramReferences = getVarReferences(methodCallExpr
				.getArgs());

		newTestCase.addStatement(new MethodStatement(newTestCase, method,
				callee, method.getGenericReturnType(), paramReferences));
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
			varReference = getVarReference(assignExpr.getTarget());
			valReference = getVarReference(assignExpr.getValue());
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
		String varName = varDeclarator.getId().getName();
		String correctVarName = "var" + (varDeclarator.getBeginLine() - 1);
		if (!varName.equals(correctVarName)) {
			throw new ParseException(null, varName
					+ " has wrong name! It must be: " + correctVarName);
		}

		Var var = new Var(varDeclarator.getId().getName(), parserType);
		tt.addVar(var);

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
				System.out.println("TestParser.newConstructorStatement() "
						+ "Reference variable can get value in different ways");
			}

			// case of creation new Object
			if (rightExpression instanceof ObjectCreationExpr) {
				Class<?> clazz = getClass(parserType);
				ObjectCreationExpr objCreationExpr = (ObjectCreationExpr) rightExpression;
				Class<?>[] paramTypes = convertVarTypes(objCreationExpr
						.getTypeArgs());
				List<VariableReference> params = getVarReferences(objCreationExpr
						.getArgs());

				Constructor<?> constructor = null;
				try {
					constructor = clazz.getConstructor(paramTypes);
				} catch (SecurityException e) {
					System.out.println("317 TestParser.newConstructorStatemen");
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					System.out.println("321 TestParser.newConstructorStatemen");
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
				res = new ArrayStatement(newTestCase,
						getClass(arrayCreationExpr.getType()), arraySize);
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
	private List<VariableReference> getVarReferences(List<Expression> args)
			throws ParseException {
		List<VariableReference> res = new ArrayList<VariableReference>();
		if (args != null) {
			for (Expression expr : args) {
				res.add(getVarReference(expr));
			}
		}
		return res;
	}

	private VariableReference getVarReference(Expression expr)
			throws ParseException {
		String varName = "";
		if (expr instanceof NameExpr) {
			NameExpr nameExpr = (NameExpr) expr;
			varName = nameExpr.getName();
		} else {
			throw new ParseException(null, "noop!!!");
		}
		return tt.getVarReference(varName);
	}

	/**
	 * Return parser's types of arguments in the method.
	 * 
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private List<Type> getVarTypesFromTT(List<Expression> args)
			throws ParseException {
		List<Type> res = new ArrayList<Type>();
		if (args != null) {
			for (Expression expr : args) {
				if (expr instanceof NameExpr) {
					NameExpr nameExpr = (NameExpr) expr;
					res.add(tt.getType(nameExpr.getName()));
				}
			}
		}
		return res;
	}

	/**
	 * @param types
	 * @return
	 * @throws ParseException
	 */
	private Class<?>[] convertVarTypes(List<Type> types) throws ParseException {
		if (types == null) {
			return null;
		}
		if (types.size() == 0) {
			return null;
		}

		List<Class<?>> res = new ArrayList<Class<?>>();

		for (Type parsType : types) {
			res.add(getClass(parsType ));
		}

		Class<?>[] resArray = new Class<?>[res.size()];
		return res.toArray(resArray);
	}

	/**
	 * @param typeArgs
	 * @return
	 * @throws ParseException
	 */
	private Class<?> getClass(Type parsType) throws ParseException {
		if (parsType == null) {
			return null;
		}

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
				throw new ParseException(null, "Can not load class: "
						+ fullClassName);
				// e.printStackTrace();
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

}
