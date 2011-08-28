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

import cvc3.ParserException;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.gui.IGUI;
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
	private static TypeTable tt = new TypeTable();
	private static Class<?> clazz = null;
	private static IGUI gui;

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
	public static TestCase parsTest(String testCode, TestCase currentTestCase,
			IGUI pgui) throws IOException {
		gui = pgui;
		try {
			clazz = TestCluster.classLoader.loadClass(Properties.TARGET_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		CompilationUnit cu = null;
		testCode = prepareTestCode(testCode);
		InputStream inputStream = createInputStream(testCode);

		try {
			cu = JavaParser.parse(inputStream);
		} catch (ParseException e) {
			gui.showParserException(e.getMessage());
			// e.printStackTrace();

		} finally {
			inputStream.close();
		}

		List<Expression> expressionStmts = getExpressionStmt(cu);
		TestCase newTestCase = new DefaultTestCase();
		try {
			for (Expression expression : expressionStmts) {

				if (expression instanceof VariableDeclarationExpr) {
					createNewVariableStatements(currentTestCase, expression,
							newTestCase);
				}

				if (expression instanceof AssignExpr) {
					createNewAssignStatments(currentTestCase, expression,
							newTestCase);
				}

				if (expression instanceof MethodCallExpr) {
					createNewMethodCallStatments(currentTestCase, expression,
							newTestCase);
				}
			}
			System.out.println("---------------------------------------------");
			System.out.println(newTestCase.toCode());
			return newTestCase;
		} catch (ParserException e) {
			gui.showParserException(e.getMessage());
		}
		return null;
	}

	/**
	 * @param currentTestCase
	 * @param expression
	 * @param newTestCase
	 * @param clazz
	 */
	private static void createNewMethodCallStatments(TestCase currentTestCase,
			Expression expression, TestCase newTestCase) {
		MethodCallExpr methodCallExpr = (MethodCallExpr) expression;

		List<Type> argTypes = getArgTypes(methodCallExpr.getArgs());
		Class<?>[] parameterTypes = convertParamsTypes(argTypes);

		String methodName = methodCallExpr.getName();
		methodCallExpr.getClass();
		Method method = null;
		try {
			method = clazz.getMethod(methodName, parameterTypes);
		} catch (SecurityException e) {
			System.out.println("In TestParser.createNewMethodCallStatments():");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			gui.showParserException(e.getMessage());
			// e.printStackTrace();
		}

		String calleeName = methodCallExpr.getScope().toString();
		VariableReference callee = retrieveVariableReference(calleeName,
				newTestCase);

		List<String> paramNames = getArgNames(methodCallExpr.getArgs());
		List<VariableReference> paramReferences = new ArrayList<VariableReference>();
		for (String paramName : paramNames) {
			paramReferences.add(retrieveVariableReference(paramName,
					newTestCase));
		}

		newTestCase.addStatement(new MethodStatement(newTestCase, method,
				callee, method.getGenericReturnType(), paramReferences));
	}

	/**
	 * Return names of arguments in the method.
	 * 
	 * @param args
	 * @return
	 */
	private static List<String> getArgNames(List<Expression> args) {
		List<String> res = new ArrayList<String>();
		if (args != null) {
			for (Expression expr : args) {
				if (expr instanceof NameExpr) {
					NameExpr nameExpr = (NameExpr) expr;
					res.add(nameExpr.getName());
				}
			}
		}
		return res;
	}

	/**
	 * Return parser's types of arguments in the method.
	 * 
	 * @param args
	 * @return
	 */
	private static List<Type> getArgTypes(List<Expression> args) {
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
	 * @param calleeName
	 * @param newTestCase
	 * @return
	 */
	private static VariableReference retrieveVariableReference(
			String calleeName, TestCase newTestCase) {
		List<VariableReference> listVariableReferences = new ArrayList<VariableReference>();
		for (int i = 0; i < newTestCase.size(); i++) {
			listVariableReferences.addAll(newTestCase.getStatement(i)
					.getVariableReferences());
		}

		for (VariableReference variableReference : listVariableReferences) {
			if (variableReference.getName().equals(calleeName)) {
				return variableReference;
			}
		}

		return null;
	}

	/**
	 * Create new Evosuite statements {@link VariableReference} from parsed code
	 * 
	 * @param currentTestCase
	 *            to insert new statements
	 * @param expression
	 *            from parser
	 * @param newTestCase
	 * @param clazz
	 */
	private static void createNewVariableStatements(TestCase currentTestCase,
			Expression expression, TestCase newTestCase) {
		VariableDeclarationExpr variableDeclarationExpr = (VariableDeclarationExpr) expression;
		Type parserType = variableDeclarationExpr.getType();

		// get all vars, but usually there is only one?
		List<VariableDeclarator> vars = variableDeclarationExpr.getVars();

		for (VariableDeclarator variableDeclarator : vars) {
			if (parserType instanceof PrimitiveType) {
				newTestCase.addStatement(newPrimitiveStatement(parserType,
						newTestCase, variableDeclarator));
			}
			if (parserType instanceof ClassOrInterfaceType) {
				System.out.println("ClassOrInterfaceType not implemented yet");
			}
			if (parserType instanceof ReferenceType) {
				ConstructorStatement newConstructorStatement = newReferenceType(
						parserType, newTestCase, variableDeclarator);
				if (newConstructorStatement != null) {
					newTestCase.addStatement(newConstructorStatement);
				}
			}
			if (parserType instanceof WildcardType) {
				System.out.println("WildcardType not implemented yet");
			}
		}
	}

	/**
	 * 
	 * @param parserType
	 * @param newTestCase
	 * @param variableDeclarator
	 * @param clazz
	 * @return
	 */
	private static ConstructorStatement newReferenceType(Type parserType,
			TestCase newTestCase, VariableDeclarator variableDeclarator) {
		ConstructorStatement res = null;
		Expression rightExpression = variableDeclarator.getInit();

		if (rightExpression != null) {
			// case of assign: methCall, just another var, some el of array etc
			if (variableDeclarator.getInit() instanceof MethodCallExpr) {
				System.out
						.println("TestParser.newConstructorStatement() Reference variable can get value in different ways");
			}

			// case of creation new Object
			if (variableDeclarator.getInit() instanceof ObjectCreationExpr) {
				ObjectCreationExpr objCreationExpr = (ObjectCreationExpr) variableDeclarator
						.getInit();
				Class<?>[] parameterTypes = convertParamsTypes(objCreationExpr
						.getTypeArgs());
				List<VariableReference> parameters = getParams(objCreationExpr
						.getArgs());

				Constructor<?> constructor = null;
				try {
					constructor = clazz.getConstructor(parameterTypes);
				} catch (SecurityException e) {
					System.out
							.println("In TestParser.newConstructorStatement():");
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					System.out
							.println("In TestParser.newConstructorStatement():");
					e.printStackTrace();
				}

				res = new ConstructorStatement(newTestCase, constructor, clazz,
						parameters);
			}

			if (variableDeclarator.getInit() instanceof ArrayCreationExpr) {

			}
		} else {
			throw new ParserException("There is no right side of declaration ecpression!");
		}

		return res;
	}

	/**
	 * @param args
	 * @return
	 */
	private static List<VariableReference> getParams(List<Expression> args) {
		List<VariableReference> res = new ArrayList<VariableReference>();
		return res;
	}

	/**
	 * @param typeArgs
	 * @return
	 */
	private static Class<?>[] convertParamsTypes(List<Type> typeArgs) {
		if (typeArgs == null) {
			return null;
		}
		if (typeArgs.size() == 0) {
			return null;
		}

		List<Class<?>> res = new ArrayList<Class<?>>();

		for (Type paramType : typeArgs) {
			if (paramType instanceof PrimitiveType) {
				PrimitiveType primitiveParamType = (PrimitiveType) paramType;

				switch (primitiveParamType.getType()) {
				case Char:
					res.add(Character.TYPE);
					break;
				case Byte:
					res.add(Byte.TYPE);
					break;
				case Short:
					res.add(Short.TYPE);
					break;
				case Int:
					res.add(Integer.TYPE);
					break;
				case Long:
					res.add(Long.TYPE);
					break;
				case Float:
					res.add(Float.TYPE);
					break;
				case Double:
					res.add(Double.TYPE);
					break;
				case Boolean:
					res.add(Boolean.TYPE);
					break;
				default:
					throw new IllegalArgumentException(
							"convertParams(Type parsType) can't obtain primitive Type");
				}
			}
		}

		Class<?>[] resArray = new Class<?>[res.size()];
		return res.toArray(resArray);
	}

	/**
	 * @param currentTestCase
	 * @param expression
	 * @param newTestCase
	 * @param clazz
	 */
	private static void createNewAssignStatments(TestCase currentTestCase,
			Expression expression, TestCase newTestCase) {
		// TODO
		System.out.println("Warning AssignExpr!");
	}

	/**
	 * Convert the variable statement in parser form to the Evosuite statement
	 * 
	 * Boolean, Char, Byte, Short, Int, Long, Float, Double
	 * 
	 * @param newTestCase
	 * @param variableDeclarator
	 * 
	 * @param parstype
	 *            type to convert
	 * @return res type of Evosuite
	 */
	private static PrimitiveStatement<?> newPrimitiveStatement(Type parserType,
			TestCase newTestCase, VariableDeclarator variableDeclarator) {

		PrimitiveType primitiveType = (PrimitiveType) parserType;
		String varName = variableDeclarator.getId().getName();
		String correctVarName = "var" + (variableDeclarator.getBeginLine() - 1);
		if (!varName.equals(correctVarName)) {
			throw new ParserException(varName + " has wrong name! It must be: "
					+ correctVarName);
		}

		Var var = new Var(variableDeclarator.getId().getName(), parserType);
		tt.addVar(var);

		switch (primitiveType.getType()) {
		case Char:
			return new CharPrimitiveStatement(newTestCase, variableDeclarator
					.getInit().toString().charAt(1));
		case Byte:
			return new BytePrimitiveStatement(newTestCase,
					Byte.parseByte(variableDeclarator.getInit().toString()));
		case Short:
			return new ShortPrimitiveStatement(newTestCase,
					Short.parseShort(variableDeclarator.getInit().toString()));
		case Int:
			return new IntPrimitiveStatement(newTestCase,
					Integer.parseInt(variableDeclarator.getInit().toString()));
		case Long:
			return new LongPrimitiveStatement(newTestCase,
					Long.parseLong(variableDeclarator.getInit().toString()));
		case Float:
			return new FloatPrimitiveStatement(newTestCase,
					Float.parseFloat(variableDeclarator.getInit().toString()));
		case Double:
			return new DoublePrimitiveStatement(newTestCase,
					Double.parseDouble(variableDeclarator.getInit().toString()));
		case Boolean:
			return new BooleanPrimitiveStatement(newTestCase,
					Boolean.parseBoolean(variableDeclarator.getInit()
							.toString()));
		default:
			throw new IllegalArgumentException(
					"convertPrimitiveType(Type parsType) can't obtain primitive Type");
		}
	}

	/**
	 * Return parsed expression statements of new {@link TestCase} from GUI
	 * Editor
	 * 
	 * @param cu
	 */
	private static List<Expression> getExpressionStmt(CompilationUnit cu) {
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
	 * Add class declaration string to test code, because {@link JavaParser}
	 * pars only complete java class
	 * 
	 * @param testCode
	 *            to modify
	 */
	private static String prepareTestCode(String testCode) {
		return "class DummyCl{void DummyMt(){" + testCode + "}}";
	}

	/**
	 * Convert {@link String} variable to {@link InputStream}
	 * 
	 * @param str
	 *            {@link String} to convert
	 * @return new {@link InputStream} variable
	 */
	private static InputStream createInputStream(String str) {
		InputStream res = new ByteArrayInputStream(str.getBytes());
		return res;
	}

}
