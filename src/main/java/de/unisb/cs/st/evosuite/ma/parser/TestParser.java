package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.LiteralExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.Editor;
import de.unisb.cs.st.evosuite.setup.ResourceList;
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
import de.unisb.cs.st.evosuite.testcase.DefaultTestFactory;
import de.unisb.cs.st.evosuite.testcase.DoublePrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.FieldReference;
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.FloatPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.GenericClass;
import de.unisb.cs.st.evosuite.testcase.IntPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.LongPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.NullStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ShortPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StringPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.evosuite.testcase.VariableReferenceImpl;

/**
 * @author Yury Pavlov
 * 
 */
public class TestParser {

	private static Logger logger = LoggerFactory.getLogger(TestParser.class);

	private TypeTable tt;

	private final Editor editor;

	private TestCase newTestCase;

	private TestCase setupSequence = new DefaultTestCase();

	private TypeTable setupTypes = new TypeTable();

	private final Map<String, Class<?>> fieldClassTable = new HashMap<String, Class<?>>();

	private final Map<String, Type> fieldTypeTable = new HashMap<String, Type>();

	private final Map<String, Expression> fieldInitTable = new HashMap<String, Expression>();

	private final TestCluster testCluster = TestCluster.getInstance();

	public TestParser(Editor editor) {
		this.editor = editor;
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
	 * Parse a Java source file and convert each method to a test
	 * 
	 * @param fileName
	 * @return
	 */
	public Set<TestCase> parseFile(String fileName) throws IOException {
		Set<TestCase> tests = new HashSet<TestCase>();
		CompilationUnit cu = null;
		tt = new TypeTable();

		logger.debug("Parsing file " + fileName);
		InputStream inputStream = new FileInputStream(new File(fileName));

		try {
			cu = JavaParser.parse(inputStream);
			parseImports(cu);
			parseFields(cu);
			parseSetUpMethods(cu);
			tests.addAll(getAllTests(cu));
		} catch (ParseException e) {
			logger.debug("Error parsing file " + fileName + ": " + e);
		} catch (Throwable e) {
			logger.debug("*** Error parsing file " + fileName + ": " + e);
			e.printStackTrace();
		} finally {
			inputStream.close();
		}
		return tests;
	}

	/**
	 * Parse setup and static constructor
	 * 
	 * @param cu
	 */
	private void parseSetUpMethods(CompilationUnit cu) {
		List<TypeDeclaration> types = cu.getTypes();
		for (TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			for (BodyDeclaration member : members) {
				if (member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					if (method.getName().equals("setUp")) {
						try {
							TestCase test = parseTestMethod(method);
							if (!test.isEmpty()) {
								logger.debug("Parsed test: " + test.toCode());
								setupSequence = test;
								setupTypes = tt.clone();
							} else {
								logger.debug("Parsed test is empty: "
										+ test.toCode());
							}
						} catch (ParseException e) {
							logger.debug("Error parsing method "
									+ method.getName() + ": " + e);

						}
					}
				}
			}
		}
	}

	/**
	 * Try to convert all methods in a CompilationUnit into tests
	 * 
	 * @param cu
	 * @return
	 */
	private List<TestCase> getAllTests(CompilationUnit cu) {
		List<TestCase> tests = new ArrayList<TestCase>();

		List<TypeDeclaration> types = cu.getTypes();
		for (TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			for (BodyDeclaration member : members) {
				if (member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					if (method.getName().equals("setUp"))
						continue;

					try {
						TestCase test = parseTestMethod(method);
						if (!test.isEmpty()) {
							logger.debug("*** Parsed test: " + test.toCode());
							tests.add(test);
						} else {
							logger.debug("*** Parsed test is empty: "
									+ test.toCode());
						}
					} catch (ParseException e) {
						logger.debug("*** Error parsing test "
								+ method.getName() + ": " + e);

					}
				}
			}
		}

		return tests;
	}

	/**
	 * Import all classes we need in the test cluster
	 * 
	 * @param cu
	 */
	private void parseImports(CompilationUnit cu) {
		if (cu.getImports() == null)
			return;

		List<String> imports = new ArrayList<String>();
		for (ImportDeclaration imp : cu.getImports()) {
			String className = imp.getName().toString();
			if (imp.isAsterisk()) {
				// Load all classes in this package
				Pattern pattern = Pattern.compile(className.replace(".", "/")
						+ "/.*.class");
				for (String resource : ResourceList.getBootResources(pattern)) {
					imports.add(resource.replace(".class", "")
							.replace("/", "."));
				}
			} else {
				imports.add(className);
			}
		}

		for (String imp : imports) {
			logger.debug("Need to import " + imp);
			try {
				getClass(imp);
			} catch (ClassNotFoundException e1) {
				try {
					testCluster.importClass(imp);
				} catch (Throwable t) {
					logger.debug("Failed to import " + imp);
				}
			}
		}

	}

	/**
	 * Extract all fields of the class to have them accessible during parsing
	 * 
	 * @param cu
	 */
	private void parseFields(CompilationUnit cu) {
		List<TypeDeclaration> types = cu.getTypes();
		for (TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			for (BodyDeclaration member : members) {
				if (member instanceof FieldDeclaration) {
					FieldDeclaration field = (FieldDeclaration) member;
					Type fieldType = field.getType();
					try {
						Class<?> clazz = typeToClass(fieldType);
						for (VariableDeclarator var : field.getVariables()) {
							String name = var.getId().getName();
							logger.debug("Field name: " + name);
							logger.debug("Field init: " + var.getInit());
							fieldClassTable.put(name, clazz);
							fieldInitTable.put(name, var.getInit());
							fieldTypeTable.put(name, fieldType);
						}
					} catch (ParseException e) {
						logger.debug("Error parsing field " + field + ": " + e);
					}
				}
			}
		}
	}

	/**
	 * Parse a single table
	 * 
	 * @param expr
	 * @throws ParseException
	 */
	private VariableReference parseExpression(Expression expr)
			throws ParseException {
		logger.debug("Parsing expression " + expr);
		if (expr instanceof VariableDeclarationExpr) {
			VariableDeclarationExpr varDeclExpr = (VariableDeclarationExpr) expr;
			logger.debug("Creating new statement " + expr);
			AbstractStatement newSttm = createVarSttm(varDeclExpr);
			newTestCase.addStatement(newSttm);
			addNewVarToTT(varDeclExpr, newSttm);
			VariableReference retVar = newSttm.getReturnValue();

			if (varDeclExpr.getVars().get(0).getInit() instanceof ArrayCreationExpr) {
				ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) varDeclExpr
						.getVars().get(0).getInit();
				if (arrayCreationExpr.getInitializer() != null
						&& arrayCreationExpr.getInitializer().getValues() != null) {
					int num = 0;
					for (Expression valueExpr : arrayCreationExpr
							.getInitializer().getValues()) {
						VariableReference value = parseExpression(valueExpr);
						AssignmentStatement assignment = new AssignmentStatement(
								newTestCase, new ArrayIndex(newTestCase,
										(ArrayReference) retVar, num), value);
						newTestCase.addStatement(assignment);
						num++;
					}
				}
			}

			return newSttm.getReturnValue();
			// }
		} else if (expr instanceof AssignExpr) {
			AssignExpr assignExpr = (AssignExpr) expr;
			AbstractStatement newSttm = createAssignSttm(assignExpr);
			newTestCase.addStatement(newSttm);
			return newSttm.getReturnValue();
		} else if (expr instanceof MethodCallExpr) {
			logger.debug("Method call expression: " + expr);
			MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
			if (methodCallExpr.getName().startsWith("assert")) {
				VariableReference var = null;
				for (Expression subExpr : methodCallExpr.getArgs()) {
					if (subExpr instanceof MethodCallExpr) {
						try {
							var = parseExpression(subExpr);
						} catch (ParseException e) {
							// Don't care if this doesn't work
						}
					}
				}
				return var;
			} else if (methodCallExpr.getScope() != null
					&& !methodCallExpr.getScope().toString().equals("this")
					&& !methodCallExpr.getScope().toString().equals("super")) {
				AbstractStatement newSttm = createMethodSttm(methodCallExpr);
				newTestCase.addStatement(newSttm);
				logger.debug(newSttm.getCode());
				return newSttm.getReturnValue();
			} else {
				return null;
			}
		} else if (expr instanceof LiteralExpr) {
			AbstractStatement newSttm = createPrimitiveStatement((LiteralExpr) expr);
			newTestCase.addStatement(newSttm);
			return newSttm.getReturnValue();
		} else if (expr instanceof CastExpr) {
			return parseExpression(((CastExpr) expr).getExpr());
		} else if (expr instanceof ObjectCreationExpr) {
			ObjectCreationExpr oexpr = (ObjectCreationExpr) expr;
			AbstractStatement newSttm = createReferenceType(oexpr,
					oexpr.getType());
			newTestCase.addStatement(newSttm);
			return newSttm.getReturnValue();
		} else if (expr instanceof ArrayInitializerExpr) {
			// TODO:
			logger.debug("Array: " + expr);
		}
		throw new ParseException(null, "Unknown expression of type "
				+ expr.getClass() + ": " + expr);
	}

	/**
	 * Convert a single method into a test
	 * 
	 * @param method
	 * @return
	 * @throws ParseException
	 */
	private TestCase parseTestMethod(MethodDeclaration method)
			throws ParseException {
		newTestCase = setupSequence.clone();
		tt = setupTypes.clone();
		logger.debug("Parsing method " + method.getName());
		if (method.getBody() == null || method.getBody().getStmts() == null) {
			return newTestCase;
		}

		if (method.getParameters() != null) {
			for (Parameter param : method.getParameters()) {
				try {
					DefaultTestFactory factory = DefaultTestFactory
							.getInstance();
					VariableReference varRef = factory.attemptGeneration(
							newTestCase, typeToClass(param.getType()),
							newTestCase.size());
					Var var = new Var(param.getId().getName(), param.getType(),
							varRef);
					tt.addVar(var);
				} catch (ConstructionFailedException e) {
					throw new ParseException(null,
							"Could not instantiate parameter type");
				}
			}
		}

		for (Statement statement : method.getBody().getStmts()) {
			// probably all statements of TestCase are ExpressionStmt
			parseStatement(statement);
		}

		return newTestCase;
	}

	private void parseStatement(Statement statement) throws ParseException {
		logger.debug("Current statement: " + statement);
		if (statement instanceof ExpressionStmt) {
			Expression expr = ((ExpressionStmt) statement).getExpression();
			parseExpression(expr);
		} else if (statement instanceof TryStmt) {
			TryStmt tryStmt = (TryStmt) statement;
			for (Statement st : tryStmt.getTryBlock().getStmts()) {
				parseStatement(st);
			}
		} else if (statement instanceof ForeachStmt) {
			ForeachStmt fstmt = (ForeachStmt) statement;
			logger.debug("FOR LOOP: " + fstmt.getIterable());
			logger.debug("FOR LOOP: " + fstmt.getBody());
			logger.debug("FOR LOOP: " + fstmt.getVariable());
			// for(Statement s : fstmt) {
			parseStatement(fstmt.getBody());
			// }
		} else if (statement instanceof ForStmt) {
			ForStmt fstmt = (ForStmt) statement;
			for (Expression ex : fstmt.getInit())
				parseExpression(ex);
			parseStatement(fstmt.getBody());
			// }
		} else if (statement instanceof BlockStmt) {
			BlockStmt bstmt = (BlockStmt) statement;
			for (Statement s : bstmt.getStmts())
				parseStatement(s);
		} else {
			logger.debug("Found statement that is not an expression: "
					+ statement + ", " + statement.getClass().getName());
		}
	}

	/**
	 * Parse a testCase in form of {@link String} to List<StatementInterface>
	 * statements, create {@link TestCase} and save it to the List of tests in
	 * {@link GeneticAlgorithm}
	 * 
	 * @param testCode
	 *            to parse
	 * @throws IOException
	 */
	public TestCase parseTest(String testCode) throws IOException {
		CompilationUnit cu = null;
		tt = new TypeTable();

		testCode = "class DummyCl{void DummyMt(){" + testCode + "}}";
		InputStream inputStream = new ByteArrayInputStream(testCode.getBytes());

		try {
			cu = JavaParser.parse(inputStream);
		} catch (ParseException e) {
			editor.showParseException(e.getMessage());
		} finally {
			inputStream.close();
		}

		List<Expression> exprStmts = getExpressionStmt(cu);
		newTestCase = new DefaultTestCase();
		for (Expression expr : exprStmts) {
			try {
				if (expr instanceof VariableDeclarationExpr) {
					VariableDeclarationExpr varDeclExpr = (VariableDeclarationExpr) expr;
					AbstractStatement newSttm = createVarSttm(varDeclExpr);
					newTestCase.addStatement(newSttm);
					addNewVarToTT(varDeclExpr, newSttm);
				}
				if (expr instanceof AssignExpr) {
					AssignExpr assignExpr = (AssignExpr) expr;
					newTestCase.addStatement(createAssignSttm(assignExpr));
				}
				if (expr instanceof MethodCallExpr) {
					MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
					newTestCase.addStatement(createMethodSttm(methodCallExpr));
				}
			} catch (ParseException e) {
				editor.showParseException("Error in line: "
						+ expr.getBeginLine() + "\nMessage: " + e.getMessage()
						+ "\nExpr: " + expr);

				// if res == null, editor&co. stay unchanged
				newTestCase = null;
				break;
			}
		}
		// logger.debug("\n-------------------------------------------");
		// logger.debug(newTestCase.toCode());
		// logger.debug("===========================================");
		// System.out.println("\n-------------------------------------------");
		// System.out.println(newTestCase.toCode());
		// System.out.println("===========================================");
		return newTestCase;
	}

	/**
	 * Create new Evosuite statements {@link VariableReference} from parsed code
	 * 
	 * @param varDeclExpr
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createVarSttm(VariableDeclarationExpr varDeclExpr)
			throws ParseException {
		Type parserType = varDeclExpr.getType();
		VariableDeclarator varDecl = varDeclExpr.getVars().get(0);

		if (varDecl.getInit() instanceof MethodCallExpr) {
			logger.debug("Righthand side is a method call");
			return createMethodSttm((MethodCallExpr) varDecl.getInit());
		} else if (parserType instanceof PrimitiveType) {
			logger.debug("Righthand side is a primitive");
			return createPrimitiveStatement(varDeclExpr);
		} else if (parserType instanceof ReferenceType) {
			logger.debug("Righthand side is a reference type: "
					+ varDecl.getInit());
			return createReferenceType(varDeclExpr);
		}

		throw new ParseException(null, "Can not create variable statement.");
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
	private PrimitiveStatement<?> createPrimitiveStatement(
			VariableDeclarationExpr varDeclExpr) throws ParseException {

		PrimitiveType primType = (PrimitiveType) varDeclExpr.getType();
		String init = varDeclExpr.getVars().get(0).getInit().toString();

		logger.debug("Variable: " + varDeclExpr.getVars().get(0).getClass());
		// This might be assigned the value of another variable, not necessarily
		// a constant
		try {
			switch (primType.getType()) {
			case Char:
				return new CharPrimitiveStatement(newTestCase, init.charAt(1));
			case Byte:
				return new BytePrimitiveStatement(newTestCase,
						Byte.parseByte(init));
			case Short:
				return new ShortPrimitiveStatement(newTestCase,
						Short.parseShort(init));
			case Int:
				return new IntPrimitiveStatement(newTestCase,
						Integer.parseInt(init));
			case Long:
				return new LongPrimitiveStatement(newTestCase,
						Long.parseLong(init.replace("L", "")));
			case Float:
				return new FloatPrimitiveStatement(newTestCase,
						Float.parseFloat(init.replace("F", "")));
			case Double:
				return new DoublePrimitiveStatement(newTestCase,
						Double.parseDouble(init.replace("D", "")));
			case Boolean:
				return new BooleanPrimitiveStatement(newTestCase,
						Boolean.parseBoolean(init));
			default:
				throw new ParseException(null, "Can't obtain primitive type.");
			}
		} catch (NumberFormatException e) {
			throw new ParseException(null,
					"Primitive is assigned a variable, not implemented yet.");
		}
	}

	/**
	 * Retrieve the class of a literal expression
	 * 
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createPrimitiveStatement(LiteralExpr expr)
			throws ParseException {
		String init = getValue(expr);
		Class<?> valueClass = literalExprToClass(expr);
		if (valueClass == null || init.equals("null"))
			return new NullStatement(newTestCase,
					valueClass == null ? Object.class : valueClass);
		else if (valueClass.equals(char.class))
			return new CharPrimitiveStatement(newTestCase, init.charAt(0));
		else if (valueClass.equals(byte.class))
			return new BytePrimitiveStatement(newTestCase, Byte.parseByte(init));
		else if (valueClass.equals(short.class))
			return new ShortPrimitiveStatement(newTestCase,
					Short.parseShort(init));
		else if (valueClass.equals(int.class))
			if (init.startsWith("0x"))
				return new IntPrimitiveStatement(newTestCase, Integer.parseInt(
						init.replace("0x", ""), 16));
			else
				return new IntPrimitiveStatement(newTestCase,
						Integer.parseInt(init));
		else if (valueClass.equals(long.class))
			return new LongPrimitiveStatement(newTestCase, Long.parseLong(init
					.replace("L", "").replace("l", "")));
		else if (valueClass.equals(float.class))
			return new FloatPrimitiveStatement(newTestCase,
					Float.parseFloat(init.replace("F", "").replace("f", "")));
		else if (valueClass.equals(double.class))
			return new DoublePrimitiveStatement(newTestCase,
					Double.parseDouble(init.replace("D", "").replace("d", "")));
		else if (valueClass.equals(boolean.class))
			return new BooleanPrimitiveStatement(newTestCase,
					Boolean.parseBoolean(init));
		else if (valueClass.equals(String.class)) {
			return new StringPrimitiveStatement(newTestCase, init);
		} else
			throw new ParseException(null, "Can't obtain primitive type.");
	}

	/**
	 * Retrieve the class of a literal expression
	 * 
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createPrimitiveStatement(UnaryExpr uexpr)
			throws ParseException {
		LiteralExpr expr = (LiteralExpr) uexpr.getExpr();

		String init = "-" + getValue(expr);
		Class<?> valueClass = literalExprToClass(expr);
		if (valueClass == null || init.equals("null"))
			return new NullStatement(newTestCase,
					valueClass == null ? Object.class : valueClass);
		else if (valueClass.equals(char.class))
			return new CharPrimitiveStatement(newTestCase, init.charAt(0));
		else if (valueClass.equals(byte.class))
			return new BytePrimitiveStatement(newTestCase, Byte.parseByte(init));
		else if (valueClass.equals(short.class))
			return new ShortPrimitiveStatement(newTestCase,
					Short.parseShort(init));
		else if (valueClass.equals(int.class))
			return new IntPrimitiveStatement(newTestCase,
					Integer.parseInt(init));
		else if (valueClass.equals(long.class))
			return new LongPrimitiveStatement(newTestCase, Long.parseLong(init
					.replace("L", "")));
		else if (valueClass.equals(float.class))
			return new FloatPrimitiveStatement(newTestCase,
					Float.parseFloat(init.replace("F", "")));
		else if (valueClass.equals(double.class))
			return new DoublePrimitiveStatement(newTestCase,
					Double.parseDouble(init.replace("D", "")));
		else if (valueClass.equals(boolean.class))
			return new BooleanPrimitiveStatement(newTestCase,
					Boolean.parseBoolean(init));
		else if (valueClass.equals(String.class))
			return new StringPrimitiveStatement(newTestCase, init);
		else
			throw new ParseException(null, "Can't obtain primitive type.");
	}

	/**
	 * 
	 * @param parserType
	 * @param newTestCase
	 * @param variableDeclarator
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createReferenceType(
			VariableDeclarationExpr varDeclExpr) throws ParseException {
		Expression initExpr = varDeclExpr.getVars().get(0).getInit();
		Type parsType = varDeclExpr.getType();

		return createReferenceType(initExpr, parsType);
	}

	private AbstractStatement createReferenceType(Expression initExpr,
			Type parsType) throws ParseException {
		AbstractStatement res = null;

		if (initExpr != null) {
			if (initExpr instanceof ObjectCreationExpr) {
				logger.debug("Looking for constructor: " + initExpr.toString());
				ObjectCreationExpr objCreatExpr = (ObjectCreationExpr) initExpr;
				List<Expression> args = objCreatExpr.getArgs();
				// Use type of RHS
				Class<?> clazz = typeToClass(objCreatExpr.getType());
				logger.debug("Loaded class " + clazz.getName());
				Class<?>[] paramClasses = getVarClasses(args);
				List<VariableReference> params = getVarRefs(args);
				Constructor<?> constructor = getConstructor(clazz, paramClasses);

				res = new ConstructorStatement(newTestCase, constructor, clazz,
						params);
			} else if (initExpr instanceof ArrayCreationExpr) {
				ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) initExpr;

				int arraySize = 0;
				if (arrayCreationExpr.getDimensions() == null
						&& arrayCreationExpr.getInitializer().getValues() != null)
					arraySize = arrayCreationExpr.getInitializer().getValues()
							.size();
				else {
					try {
						if (arrayCreationExpr.getDimensions() != null
								&& !arrayCreationExpr.getDimensions().isEmpty())
							arraySize = Integer.parseInt(arrayCreationExpr
									.getDimensions().get(0).toString());
					} catch (NumberFormatException e) {
						arraySize = 1; // TODO: Better value
					}
				}
				// TODO: Support for multi-dimensional arrays?

				Class<?> clazz = typeToClass(arrayCreationExpr.getType());
				Object array = Array.newInstance(clazz, arraySize);

				res = new ArrayStatement(newTestCase, array.getClass(),
						arraySize);
			} else if (initExpr instanceof CastExpr) {
				CastExpr castExpr = (CastExpr) initExpr;
				logger.debug("Cast expression: " + castExpr);
				if (castExpr.getExpr() instanceof MethodCallExpr) {
					logger.debug("Cast expression in method call: "
							+ castExpr.getExpr());
					res = createMethodSttm((MethodCallExpr) castExpr.getExpr());
				} else {
					logger.debug("Cast expression not in method call: "
							+ castExpr.getExpr());
					return createReferenceType(castExpr.getExpr(), parsType);
				}
			} else if (initExpr instanceof StringLiteralExpr) {
				res = new StringPrimitiveStatement(newTestCase,
						((StringLiteralExpr) initExpr).getValue());
			} else if (initExpr instanceof NullLiteralExpr) {
				res = new NullStatement(newTestCase, typeToClass(parsType));
			} else if (initExpr instanceof ArrayAccessExpr) {
				VariableReference rhs = getVarRef(initExpr);
				VariableReference lhs = new VariableReferenceImpl(newTestCase,
						typeToClass(parsType));
				return new AssignmentStatement(newTestCase, lhs, rhs);
			} else if (initExpr instanceof BinaryExpr) {
				BinaryExpr bexpr = (BinaryExpr) initExpr;
				// Just use one of the two
				return createReferenceType(bexpr.getLeft(), parsType);
			} else if (initExpr instanceof ArrayInitializerExpr) {
				ArrayInitializerExpr arrayCreationExpr = (ArrayInitializerExpr) initExpr;

				int arraySize = 0;
				if (arrayCreationExpr.getValues() != null) {
					arraySize = arrayCreationExpr.getValues().size();
				}
				// TODO: Support for multi-dimensional arrays?

				Class<?> clazz = typeToClass(parsType).getComponentType();
				Object array = Array.newInstance(clazz, arraySize);

				res = new ArrayStatement(newTestCase, array.getClass(),
						arraySize);
			} else if (initExpr instanceof NameExpr) {
				VariableReference rhs = getVarRef(initExpr);
				VariableReference lhs = new VariableReferenceImpl(newTestCase,
						typeToClass(parsType));
				res = new AssignmentStatement(newTestCase, lhs, rhs);
			} else if (initExpr instanceof ConditionalExpr) {
				ConditionalExpr cexpr = (ConditionalExpr) initExpr;
				return createReferenceType(cexpr.getThenExpr(), parsType);
				// } else {
				// assert (false) : "Expression not implemented: " +
				// initExpr.getClass();
			} else if (initExpr instanceof FieldAccessExpr) {
				Field field = getField(initExpr);
				Class<?> type = field.getType();
				VariableReference source = null;
				source = tt.getVarReference(((FieldAccessExpr) initExpr)
						.getScope().toString());
				res = new FieldStatement(newTestCase, field, source, type);
			}

		} else {
			// assert (false) : "Expression is null: " + initExpr;
			// return null;
			res = new NullStatement(newTestCase, typeToClass(parsType));
			// If there is no right side, there might still be an assignment
			// later
			// throw new ParseException(null,
			// "There is no a right side of the declaration expression.");
		}

		if (res == null) {
			throw new ParseException(null, "Can not create the reference type.");
		}
		return res;
	}

	/**
	 * @param methodCallExpr
	 * @param addStatement
	 * @return
	 * @throws ParseException
	 */
	private AbstractStatement createMethodSttm(MethodCallExpr methodCallExpr)
			throws ParseException {
		Expression scope = methodCallExpr.getScope();
		if (scope == null)
			throw new ParseException(null, "Scope of method call is null");
		List<Expression> args = methodCallExpr.getArgs();

		Class<?> clazz = null;
		try {
			ClassOrInterfaceType type = new ClassOrInterfaceType(0, 0, 0, 0,
					null, methodCallExpr.toString(), null);
			clazz = typeToClass(type);
		} catch (ParseException e) {
			clazz = typeToClass(getType(scope));
		}

		String methodName = methodCallExpr.getName();
		Class<?>[] paramClasses = getVarClasses(args);
		Method method = getMethod(clazz, methodName, paramClasses);
		VariableReference callee = getVarRef(scope);
		List<VariableReference> paramReferences = getVarRefs(args);

		return new MethodStatement(newTestCase, method, callee,
				method.getReturnType(), paramReferences);
	}

	/**
	 * @param currentTestCase
	 * @param expression
	 * @param newTestCase
	 * @throws ParseException
	 */
	private AbstractStatement createAssignSttm(AssignExpr assignExpr)
			throws ParseException {
		VariableReference tarRef = getVarRef(assignExpr.getTarget());
		VariableReference valRef = getVarRef(assignExpr.getValue());
		if (tarRef == null) {
			throw new ParseException(null,
					"Can not create or find the var reference: "
							+ assignExpr.getTarget());
		}
		if (valRef == null) {
			throw new ParseException(null,
					"Can not create or find the var reference: "
							+ assignExpr.getValue());
		}

		return new AssignmentStatement(newTestCase, tarRef, valRef);
	}

	/**
	 * @param parserType
	 * @param varDeclarator
	 * @param newStatement
	 * @throws ParseException
	 */
	private void addNewVarToTT(VariableDeclarationExpr varDeclExpr,
			AbstractStatement newStatement) throws ParseException {
		ArrayList<VariableReference> varRefArray = new ArrayList<VariableReference>();
		varRefArray.addAll(newStatement.getVariableReferences());
		VariableReference varRef = newStatement.getReturnValue();

		tt.addVar(new Var(varDeclExpr.getVars().get(0).getId().getName(),
				varDeclExpr.getType(), varRef));
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

	private VariableReference resolve(String varName) throws ParseException {
		if (tt.hasVar(varName)) {
			// It might be a local variable
			return tt.getVarReference(varName);
		} else {
			// Else it might be a class variable which we need to convert to a
			// local var
			if (fieldInitTable.containsKey(varName)) {
				if (fieldInitTable.get(varName) == null
						|| fieldInitTable.get(varName) instanceof NullLiteralExpr) {
					AbstractStatement nullStatement = new NullStatement(
							newTestCase, fieldClassTable.get(varName));
					VariableReference var = newTestCase
							.addStatement(nullStatement);
					return var;
				} else {
					// TODO: What if init is primitive?
					VariableReference var = parseExpression(fieldInitTable
							.get(varName));
					return var;
				}
			} else {
				// Else this is not a variable
				return null;
				// throw new ParseException(null, "Var ref of: " + varName
				// + " not found in TT and fields.");
			}
		}
	}

	private VariableReference getVarRef(Expression expr) throws ParseException {
		logger.debug("Checking var ref of " + expr + ", "
				+ expr.getClass().getName());
		if (expr instanceof NameExpr) {
			String name = ((NameExpr) expr).getName();
			logger.debug("NameExpr: " + name);
			return resolve(name);
		} else if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAccExpr = (FieldAccessExpr) expr;
			logger.debug("Found field access: " + expr + " with scope "
					+ fieldAccExpr.getScope().toString());
			if (!tt.hasVar(fieldAccExpr.getScope().toString())) {
				try {
					ClassOrInterfaceType type = new ClassOrInterfaceType(0, 0,
							0, 0, null, fieldAccExpr.toString(), null);
					typeToClass(type);
					return null; // Static access!
				} catch (ParseException e) {
					// Expected behavior
				}
			}
			if (fieldAccExpr.getScope().toString().equals("this")) {
				return resolve(fieldAccExpr.getField());
			}
			VariableReference varRef = resolve(fieldAccExpr.getScope()
					.toString());
			logger.debug("Field source: " + varRef);
			// TODO check if static from another class
			Field field = getField(fieldAccExpr);
			logger.debug("Field: " + field);
			return new FieldReference(newTestCase, field, varRef);
		} else if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;
			logger.debug("Getting array variable for " + expr);
			VariableReference arrayVar = getVarRef(arrayAccExpr.getName());
			ArrayReference arrayRef = null;
			if (!(arrayVar instanceof ArrayReference)) {
				logger.debug("1) Have array variable of type: "
						+ arrayVar.getVariableClass() + " / "
						+ arrayVar.getComponentType());
				Object array = Array
						.newInstance(arrayVar.getVariableClass(), 0);
				AbstractStatement assign = new AssignmentStatement(newTestCase,
						new ArrayReference(newTestCase, new GenericClass(
								array.getClass()), 0), arrayVar);
				arrayRef = (ArrayReference) newTestCase.addStatement(assign);
			} else {
				logger.debug("2) Have array variable of type: "
						+ arrayVar.getVariableClass());
				arrayRef = (ArrayReference) getVarRef(arrayAccExpr.getName());
			}
			logger.debug("Got array reference " + arrayAccExpr.getName());
			// resolve(arrayAccExpr.getName().toString());
			int arrayInd = 0;
			try {
				arrayInd = Integer.parseInt(arrayAccExpr.getIndex().toString());
			} catch (NumberFormatException e) {
				// If we can't parse it, just use 0
			}

			logger.debug("Array reference: " + arrayRef + ", index " + arrayInd);
			return new ArrayIndex(newTestCase, arrayRef, arrayInd);
		} else if (expr instanceof CastExpr) {
			logger.debug("CastExpr: " + expr.toString());

			return getVarRef(((CastExpr) expr).getExpr());
		} else if (expr instanceof MethodCallExpr) {
			AbstractStatement methodCall = createMethodSttm((MethodCallExpr) expr);
			return newTestCase.addStatement(methodCall);
		} else if (expr instanceof LiteralExpr) {
			logger.debug("LiteralExpr: " + expr.toString());

			AbstractStatement value = createPrimitiveStatement((LiteralExpr) expr);
			return newTestCase.addStatement(value);
		} else if (expr instanceof UnaryExpr) {
			UnaryExpr uexpr = (UnaryExpr) expr;
			if (uexpr.getExpr() instanceof LiteralExpr
					&& uexpr.getOperator() == UnaryExpr.Operator.negative) {
				AbstractStatement value = createPrimitiveStatement(uexpr);
				return newTestCase.addStatement(value);

			} else {
				throw new ParseException(null, "Unary operator not implemented");
			}
			// TODO: Implement
		} else if (expr instanceof BinaryExpr) {
			BinaryExpr bexpr = (BinaryExpr) expr;
			logger.debug("BinaryExpr: " + bexpr.toString());

			return getVarRef(bexpr.getLeft()); // Just use one of the two
		} else if (expr instanceof EnclosedExpr) {
			return getVarRef(((EnclosedExpr) expr).getInner());
		} else if (expr instanceof ObjectCreationExpr) {
			AbstractStatement value = createReferenceType(expr,
					((ObjectCreationExpr) expr).getType());
			return newTestCase.addStatement(value);
		} else if (expr instanceof ArrayCreationExpr) {
			ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) expr;

			int arraySize = 0;
			if (arrayCreationExpr.getDimensions() == null
					&& arrayCreationExpr.getInitializer() != null
					&& arrayCreationExpr.getInitializer().getValues() != null)
				arraySize = arrayCreationExpr.getInitializer().getValues()
						.size();
			else if (arrayCreationExpr.getDimensions() != null) {
				try {
					arraySize = Integer.parseInt(arrayCreationExpr
							.getDimensions().get(0).toString());
				} catch (NumberFormatException e) {
					// Ignore
				}
			}

			Class<?> clazz = typeToClass(arrayCreationExpr.getType());
			Object array = Array.newInstance(clazz, arraySize);

			VariableReference arrayVar = newTestCase
					.addStatement(new ArrayStatement(newTestCase, array
							.getClass(), arraySize));

			if (arrayCreationExpr.getInitializer() != null
					&& arrayCreationExpr.getInitializer().getValues() != null) {
				int num = 0;
				for (Expression valueExpr : arrayCreationExpr.getInitializer()
						.getValues()) {
					VariableReference value = parseExpression(valueExpr);
					AssignmentStatement assignment = new AssignmentStatement(
							newTestCase, new ArrayIndex(newTestCase,
									(ArrayReference) arrayVar, num), value);
					newTestCase.addStatement(assignment);
					num++;
				}
			}

			return arrayVar;
		}
		return null;
	}

	private Class<?> getVarClass(Expression expr) throws ParseException {
		logger.debug("Checking class of " + expr);
		if (expr instanceof NameExpr) {
			String varName = ((NameExpr) expr).getName();
			if (tt.hasVar(varName)) {
				return tt.getVarReference(varName).getVariableClass();
			} else {
				// If not in tt, then it is a class member
				if (fieldClassTable.containsKey(varName)) {
					return fieldClassTable.get(varName);
				} else {
					return typeToClass(new ClassOrInterfaceType(0, 0, 0, 0,
							null, varName, null));
				}
			}

		} else if (expr instanceof FieldAccessExpr) {
			return getField(expr).getType();
		} else if (expr instanceof ArrayAccessExpr) {
			String arrayName = ((ArrayAccessExpr) expr).getName().toString();
			return tt.getVarReference(arrayName).getComponentClass();
		} else if (expr instanceof CastExpr) {
			return typeToClass(((CastExpr) expr).getType());
		} else if (expr instanceof LiteralExpr) {
			return literalExprToClass((LiteralExpr) expr);
		} else if (expr instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = (MethodCallExpr) expr;
			Expression scope = methodCallExpr.getScope();
			if (scope == null)
				throw new ParseException(null,
						"Cannot yet handle calls to other methods");
			List<Expression> methodArgs = methodCallExpr.getArgs();
			Class<?> clazz = typeToClass(getType(scope));
			String methodName = methodCallExpr.getName();
			Class<?>[] paramClasses = getVarClasses(methodArgs);
			Method method = getMethod(clazz, methodName, paramClasses);
			return method.getReturnType();
		} else if (expr instanceof ObjectCreationExpr) {
			ObjectCreationExpr oexpr = (ObjectCreationExpr) expr;
			return typeToClass(oexpr.getType());
		} else if (expr instanceof ArrayCreationExpr) {
			ArrayCreationExpr aexpr = (ArrayCreationExpr) expr;
			Class<?> componentClass = typeToClass(aexpr.getType());
			Object o = Array.newInstance(componentClass, 0);
			return o.getClass();
		} else if (expr instanceof UnaryExpr) {
			UnaryExpr uexpr = (UnaryExpr) expr;
			return getVarClass(uexpr.getExpr());
		} else if (expr instanceof BinaryExpr) {
			BinaryExpr bexpr = (BinaryExpr) expr;
			return getVarClass(bexpr.getLeft()); // Just use one of the two
		} else {
			throw new ParseException(null, "Can not find reference for \""
					+ expr + "\" " + expr.getClass().getName());
		}
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
				tmpRes.add(getVarClass(expr));
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
		Expression scope = fieldExpr.getScope();

		Class<?> clazz = null;
		if (!tt.hasVar(scope.toString())) {
			ClassOrInterfaceType type = new ClassOrInterfaceType(0, 0, 0, 0,
					null, scope.toString(), null);
			clazz = typeToClass(type);

		} else {
			logger.debug(fieldExpr.getScope()
					+ " is not a class, looking further");
			clazz = typeToClass(getType(scope));
		}

		try {
			return clazz.getField(fieldExpr.getField());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			throw new ParseException(null, "Can not find the field.");
		}
		return null;
	}

	/**
	 * @throws ParseException
	 * 
	 */
	private Method getMethod(MethodCallExpr expr) throws ParseException {
		Class<?> clazz = null;
		try {
			ClassOrInterfaceType type = new ClassOrInterfaceType(0, 0, 0, 0,
					null, expr.getScope().toString(), null);
			clazz = typeToClass(type);

		} catch (Throwable t) {
			logger.debug(expr.getScope() + " is not a class, looking further");
			clazz = typeToClass(getType(expr.getScope()));
		}

		try {
			String methodName = expr.getName();
			List<Expression> args = expr.getArgs();
			Class<?>[] paramClasses = getVarClasses(args);
			logger.debug("Method 2 for class " + clazz.getName());
			Method method = getMethod(clazz, methodName, paramClasses);

			return method;
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param clazz
	 * @param paramClasses
	 * @return
	 * @throws ParseException
	 */
	private Constructor<?> getConstructor(Class<?> clazz,
			Class<?>[] paramClasses) throws ParseException {
		try {
			return clazz.getConstructor(paramClasses);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			for (Constructor<?> constr : TestCluster.getConstructors(clazz)) {
				if (constr.getParameterTypes().length == paramClasses.length) {
					Class<?>[] constrParams = constr.getParameterTypes();
					for (int i = 0; i < constrParams.length; i++) {
						if (paramClasses[i] == null
								|| constrParams[i]
										.isAssignableFrom(paramClasses[i])) {
							if (i == constrParams.length - 1) {
								return constr;
							}
						} else {
							break;
						}
					}
				}
			}
		}
		throw new ParseException(null, "No such constructor in class "
				+ clazz.getName() + ": " + Arrays.asList(paramClasses));
	}

	/**
	 * @param clazz
	 * @param methodName
	 * @param paramClasses
	 * @return
	 * @throws ParseException
	 */
	private Method getMethod(Class<?> clazz, String methodName,
			Class<?>[] paramClasses) throws ParseException {
		String classNames = "";
		try {
			return clazz.getMethod(methodName, paramClasses);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			logger.debug("Looking for method " + methodName + " in class "
					+ clazz.getName() + " with parameters "
					+ Arrays.asList(paramClasses));
			for (Method meth : TestCluster.getMethods(clazz)) {
				if (meth.getName().equals(methodName)) {
					if (meth.getParameterTypes().length == paramClasses.length) {
						Class<?>[] methParams = meth.getParameterTypes();
						logger.debug("Checking " + Arrays.asList(methParams));
						for (int i = 0; i < methParams.length; i++) {
							if (paramClasses[i] == null
									|| methParams[i]
											.isAssignableFrom(paramClasses[i])
									|| (methParams[i].equals(int.class) && paramClasses[i]
											.equals(char.class))) {
								logger.debug("Parameter " + i + " matches");
								if (i == methParams.length - 1) {
									return meth;
								}
							} else {
								logger.debug("Parameter " + i
										+ " does not match");
								break;
							}
						}
					}
				}
			}
			for (Class<?> paramType : paramClasses) {
				classNames += paramType.getName() + "; ";
			}
		}
		throw new ParseException(null, "Can not find the method: \""
				+ methodName + "\", with parameter(s): " + classNames);
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
			logger.debug("Scope is a NameExpr: " + expr);
			String name = ((NameExpr) expr).getName();

			if (tt.hasVar(name)) {
				logger.debug("Have variable in table");
				return tt.getType(name);
			} else if (fieldClassTable.containsKey(name)) {
				return fieldTypeTable.get(name);
			} else {
				logger.debug("Getting class " + name);
				return new ClassOrInterfaceType(0, 0, 0, 0, null, name, null);
			}
		} else if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAcExpr = (FieldAccessExpr) expr;
			if (fieldAcExpr.getScope().toString().equals("this")) {
				return fieldTypeTable.get(fieldAcExpr.getField());
			}
			logger.debug("Scope is a FieldAccessExpr: " + expr + " / "
					+ fieldAcExpr.getScope().toString());

			Class<?> clazz = null;
			try {
				ClassOrInterfaceType type = new ClassOrInterfaceType(0, 0, 0,
						0, null, fieldAcExpr.toString(), null);
				clazz = typeToClass(type);
				return new ClassOrInterfaceType(0, 0, 0, 0, null,
						clazz.getName(), null);
			} catch (ParseException e) {
				Field field = getField(fieldAcExpr);
				return new ClassOrInterfaceType(0, 0, 0, 0, null, field
						.getDeclaringClass().getName(), null);
			}

			/*
			 * if (tt.hasVar(fieldAcExpr.getScope().toString())) { return
			 * tt.getType(fieldAcExpr.getScope()); } else { throw new
			 * RuntimeException("Could not find field " +
			 * fieldAcExpr.getScope()); }
			 */
			// if (!isStatic(fieldAcExpr.getScope().toString())) {
			// return tt.getType(fieldAcExpr.getScope());
			// }
		} else if (expr instanceof MethodCallExpr) {
			MethodCallExpr methodExpr = (MethodCallExpr) expr;
			Method method = getMethod(methodExpr);
			return new ClassOrInterfaceType(0, 0, 0, 0, null, method
					.getReturnType().getName(), null);
		} else if (expr instanceof CastExpr) {
			return ((CastExpr) expr).getType();
		} else if (expr instanceof StringLiteralExpr) {
			// StringLiteralExpr sexpr = (StringLiteralExpr) expr;
			return new ClassOrInterfaceType(0, 0, 0, 0, null,
					"java.lang.String", null);
		} else {
			logger.debug("Scope is unknown expr: " + expr);
			logger.debug("Scope is unknown expr: " + expr.getClass());
			logger.debug("Scope is unknown expr: " + expr.getClass().getName());
		}
		return null;
	}

	/**
	 * @param typeArgs
	 * @return
	 * @throws ParseException
	 */
	private Class<?> typeToClass(Type parsType) throws ParseException {
		if (parsType instanceof PrimitiveType) {
			PrimitiveType primitiveParamType = (PrimitiveType) parsType;
			return primitiveTypeToClass(primitiveParamType);
		} else if (parsType instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) parsType;
			if (refType.getArrayCount() > 0) {
				Object array = Array.newInstance(
						typeToClass(refType.getType()), 0);
				return array.getClass();

			} else {
				Class<?> result = coiTypeToClass(refType.getType());
				logger.debug("Loaded class " + result.getName());
				return result;
			}
		} else if (parsType instanceof ClassOrInterfaceType) {
			return coiTypeToClass(parsType);
		} else if (parsType instanceof VoidType) {
			throw new ParseException(null, "Can not load class for VoidType.");
		} else if (parsType instanceof WildcardType) {
			throw new ParseException(null,
					"Can not load class for WildcardType: " + parsType);
		}
		throw new ParseException(null, "Can not find class for type "
				+ parsType);
	}

	/**
	 * @param primitiveParamType
	 * @return
	 * @throws ParseException
	 */
	private Class<?> primitiveTypeToClass(PrimitiveType primitiveParamType)
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
	 * Retrieve the class of a literal expression
	 * 
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	private Class<?> literalExprToClass(LiteralExpr expr) throws ParseException {
		logger.debug("Literal: " + expr);
		if (expr instanceof IntegerLiteralExpr) {
			return int.class;
		} else if (expr instanceof StringLiteralExpr) {
			StringLiteralExpr sexpr = (StringLiteralExpr) expr;
			if (sexpr instanceof IntegerLiteralExpr) {
				return int.class;
			} else if (sexpr instanceof CharLiteralExpr) {
				return char.class;
			} else if (sexpr instanceof DoubleLiteralExpr) {
				return double.class;
			} else if (sexpr instanceof LongLiteralExpr) {
				return long.class;
			}
			return String.class;
		} else if (expr instanceof BooleanLiteralExpr) {
			return boolean.class;
		} else if (expr instanceof NullLiteralExpr) {
			return null; // Object.class; // TODO: This is not quite right
		}
		throw new ParseException(null, "Cannot parse type of expression "
				+ expr);
	}

	/**
	 * Retrieve the class of a literal expression
	 * 
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	private String getValue(LiteralExpr expr) throws ParseException {
		if (expr instanceof IntegerLiteralExpr) {
			return ((IntegerLiteralExpr) expr).getValue();
		} else if (expr instanceof StringLiteralExpr) {
			return ((StringLiteralExpr) expr).getValue();
		} else if (expr instanceof BooleanLiteralExpr) {
			return ((BooleanLiteralExpr) expr).toString();
		} else if (expr instanceof NullLiteralExpr) {
			return "null";
		}
		throw new ParseException(null, "Cannot parse type of expression "
				+ expr);
	}

	/**
	 * @param parsType
	 * @return
	 * @throws ParseException
	 */
	private Class<?> coiTypeToClass(Type parsType) throws ParseException {
		if (parsType.toString().endsWith(")"))
			throw new ParseException(null, "String is not a classname: "
					+ parsType.toString());
		try {
			return TestCluster.getInstance().getClass(parsType.toString());
		} catch (ClassNotFoundException e1) {
			logger.debug("Class not found: " + e1);
			try {
				if (parsType.toString().equals("String")) {
					return TestCluster.getInstance().importClass(
							"java.lang.String");
				}
				if (editor == null) {
					logger.debug("Importing class " + parsType);
					// If we get to here, we can only guess.
					try {
						return testCluster.importClass(Properties.CLASS_PREFIX
								+ "." + parsType.toString());
					} catch (ClassNotFoundException e) {
						// It might also be in java.lang
						return testCluster.importClass("java.lang."
								+ parsType.toString());
					}
				}
				String className = editor
						.showChooseFileMenu(parsType.toString())
						.getSelectedFile().getName();
				if (className != null) {
					return testCluster.importClass(className);
				} else {
					throw new ParseException(null,
							"Can not load class for ClassOrInterfaceType: "
									+ parsType);
				}
			} catch (Throwable e2) {
				throw new ParseException(null,
						"Can not load class for ClassOrInterfaceType: "
								+ parsType + " " + e2);
			}
		}

	}

	public Class<?> getClass(String name) throws ClassNotFoundException {

		// First try to find exact match
		for (Class<?> clazz : testCluster.getAnalyzedClasses()) {
			if (clazz.getName().equals(name))
				return clazz;
		}

		throw new ClassNotFoundException(name);
	}

}
