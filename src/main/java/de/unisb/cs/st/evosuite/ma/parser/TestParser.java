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
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
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
import de.unisb.cs.st.evosuite.ma.Editor;
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
import de.unisb.cs.st.evosuite.testcase.NullStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ShortPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StringPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov
 * 
 */
public class TestParser {

	private TypeTable tt;

	private final Editor editor;

	private TestCase newTestCase;

	private TestCluster testCluster = TestCluster.getInstance();

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
	 * Parse a testCase in form of {@link String} to List<StatementInterface>
	 * statements, create {@link TestCase} and save it to the List of tests in
	 * {@link GeneticAlgorithm}
	 * 
	 * @param testCode
	 *            to parse
	 * @throws IOException
	 */
	public TestCase parsTest(String testCode) throws IOException {
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
			return createMethodSttm((MethodCallExpr) varDecl.getInit());
		} else if (parserType instanceof PrimitiveType) {
			return createPrimitiveStatement(varDeclExpr);
		} else if (parserType instanceof ReferenceType) {
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

		switch (primType.getType()) {
		case Char:
			return new CharPrimitiveStatement(newTestCase, init.charAt(1));
		case Byte:
			return new BytePrimitiveStatement(newTestCase, Byte.parseByte(init));
		case Short:
			return new ShortPrimitiveStatement(newTestCase,
					Short.parseShort(init));
		case Int:
			return new IntPrimitiveStatement(newTestCase,
					Integer.parseInt(init));
		case Long:
			return new LongPrimitiveStatement(newTestCase, Long.parseLong(init));
		case Float:
			return new FloatPrimitiveStatement(newTestCase,
					Float.parseFloat(init));
		case Double:
			return new DoublePrimitiveStatement(newTestCase,
					Double.parseDouble(init));
		case Boolean:
			return new BooleanPrimitiveStatement(newTestCase,
					Boolean.parseBoolean(init));
		default:
			throw new ParseException(null, "Can't obtain primitive type.");
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
	private AbstractStatement createReferenceType(
			VariableDeclarationExpr varDeclExpr) throws ParseException {
		AbstractStatement res = null;
		Expression initExpr = varDeclExpr.getVars().get(0).getInit();
		Type parsType = varDeclExpr.getType();

		if (initExpr != null) {
			if (initExpr instanceof ObjectCreationExpr) {
				ObjectCreationExpr objCreatExpr = (ObjectCreationExpr) initExpr;
				List<Expression> args = objCreatExpr.getArgs();
				Class<?> clazz = typeToClass(parsType);
				Class<?>[] paramClasses = getVarClasses(args);
				List<VariableReference> params = getVarRefs(args);
				Constructor<?> constructor = getConstructor(clazz, paramClasses);

				res = new ConstructorStatement(newTestCase, constructor, clazz,
						params);
			} else if (initExpr instanceof ArrayCreationExpr) {
				ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr) initExpr;
				int arraySize = Integer.parseInt(arrayCreationExpr
						.getDimensions().get(0).toString());
				Class<?> clazz = typeToClass(arrayCreationExpr.getType());
				Object array = Array.newInstance(clazz, arraySize);

				res = new ArrayStatement(newTestCase, array.getClass(),
						arraySize);
			} else if (initExpr instanceof CastExpr) {
				CastExpr castExpr = (CastExpr) initExpr;
				if (castExpr.getExpr() instanceof MethodCallExpr) {
					res = createMethodSttm((MethodCallExpr) castExpr.getExpr());
				}
			} else if (initExpr instanceof StringLiteralExpr) {
				res = new StringPrimitiveStatement(newTestCase,
						((StringLiteralExpr) initExpr).getValue());
			} else if (initExpr instanceof NullLiteralExpr) {
				res = new NullStatement(newTestCase, typeToClass(parsType));
			}

		} else {
			throw new ParseException(null,
					"There is no a right side of the declaration expression.");
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
		List<Expression> args = methodCallExpr.getArgs();
		Class<?> clazz = typeToClass(getType(scope));
		String methodName = methodCallExpr.getName();
		Class<?>[] paramClasses = getVarClasses(args);
		Method method = getMethod(clazz, methodName, paramClasses);
		VariableReference callee = getVarRef(scope);
		List<VariableReference> paramReferences = getVarRefs(args);

		return new MethodStatement(newTestCase, method, callee,
				method.getGenericReturnType(), paramReferences);
	}

	/**
	 * @param currentTestCase
	 * @param expression
	 * @param newTestCase
	 * @throws ParseException
	 */
	private AbstractStatement createAssignSttm(AssignExpr assignExpr)
			throws ParseException {
		VariableReference varRef = getVarRef(assignExpr.getTarget());
		VariableReference valRef = getVarRef(assignExpr.getValue());

		if (varRef == null) {
			throw new ParseException(null,
					"Can not create or find the var reference: "
							+ assignExpr.getTarget());
		}
		if (valRef == null) {
			throw new ParseException(null,
					"Can not create or find the var reference: "
							+ assignExpr.getValue());
		}

		return new AssignmentStatement(newTestCase, varRef, valRef);
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
				if (expr instanceof CastExpr) {
					res.add(getVarRef(((CastExpr) expr).getExpr()));
				} else {
					res.add(getVarRef(expr));
				}
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
		} else if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAccExpr = (FieldAccessExpr) expr;
			VariableReference varRef = null;

			// if VariableRef stay null EvoSuite make this call as static
			if (!isStatic(fieldAccExpr.getScope().toString())) {
				varRef = tt.getVarReference(fieldAccExpr.getScope().toString());
			}
			// TODO check if static from another class
			return new FieldReference(newTestCase, getField(fieldAccExpr),
					varRef);
		} else if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;

			ArrayReference arrayRef = (ArrayReference) tt
					.getVarReference(arrayAccExpr.getName().toString());
			int arrayInd = Integer.parseInt(arrayAccExpr.getIndex().toString());

			return new ArrayIndex(newTestCase, arrayRef, arrayInd);
		} else if (expr instanceof CastExpr) {
			return getVarRef(((CastExpr) expr).getExpr());
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
					String varName = ((NameExpr) expr).getName();
					tmpRes.add(tt.getVarReference(varName).getVariableClass());
				} else if (expr instanceof FieldAccessExpr) {
					tmpRes.add(getField(expr).getType());
				} else if (expr instanceof ArrayAccessExpr) {
					String arrayName = ((ArrayAccessExpr) expr).getName()
							.toString();
					tmpRes.add(tt.getVarReference(arrayName)
							.getComponentClass());
				} else if (expr instanceof CastExpr) {
					tmpRes.add(typeToClass(((CastExpr) expr).getType()));
				} else {
					throw new ParseException(null, "Can not find reference for \"" + expr + "\"");
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

		Class<?> clazz = typeToClass(getType(fieldExpr.getScope()));

		try {
			return clazz.getField(fieldExpr.getField());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			throw new ParseException(null, "Can not find the field.");
		}
		return null;
	}

	// /**
	// * @param typeArgs
	// * @return
	// * @throws ParseException
	// */
	// private Class<?>[] typesToClasses(List<Type> typeArgs)
	// throws ParseException {
	// if (typeArgs == null) {
	// return null;
	// }
	//
	// List<Class<?>> tmpRes = new ArrayList<Class<?>>();
	// for (Type type : typeArgs) {
	// tmpRes.add(typeToClass(type));
	// }
	//
	// Class<?>[] res = new Class<?>[tmpRes.size()];
	// res = tmpRes.toArray(res);
	// return res;
	// }

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
						if (constrParams[i].isAssignableFrom(paramClasses[i])) {
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
		throw new ParseException(null, "No such constructor in this class.");
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
			for (Method meth : clazz.getMethods()) {
				if (meth.getName().equals(methodName)) {
					if (meth.getParameterTypes().length == paramClasses.length) {
						Class<?>[] methParams = meth.getParameterTypes();
						for (int i = 0; i < methParams.length; i++) {
							if (methParams[i].isAssignableFrom(paramClasses[i])) {
								if (i == methParams.length - 1) {
									return meth;
								}
							} else {
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
	 * @param typeArgs
	 * @return
	 * @throws ParseException
	 */
	private Class<?> typeToClass(Type parsType) throws ParseException {
		if (parsType instanceof PrimitiveType) {
			PrimitiveType primitiveParamType = (PrimitiveType) parsType;
			return primitiveTypeToClass(primitiveParamType);
		}
		if (parsType instanceof ReferenceType) {
			ReferenceType refType = (ReferenceType) parsType;
			return coiTypeToClass(refType.getType());
		}
		if (parsType instanceof ClassOrInterfaceType) {
			return coiTypeToClass(parsType);
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
	 * @param parsType
	 * @return
	 * @throws ParseException
	 */
	private Class<?> coiTypeToClass(Type parsType) throws ParseException {
		String fullClassName = Properties.CLASS_PREFIX + "." + parsType;

		try {
			return TestCluster.classLoader.loadClass(fullClassName);
		} catch (ClassNotFoundException e) {
			try {
				return getClass(parsType.toString());
			} catch (ClassNotFoundException e1) {
				try {
					String className = editor.showChooseFileMenu(parsType
							.toString());
					if (className != null) {
						return testCluster.importClass(className);
					} else {
						throw new ParseException(null,
								"Can not load class for ClassOrInterfaceType: "
										+ parsType);
					}
				} catch (ClassNotFoundException e2) {
					throw new ParseException(null,
							"Can not load class for ClassOrInterfaceType: "
									+ parsType);
				}
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

	/**
	 * @param varName
	 * @return
	 */
	public static boolean isStatic(String varName) {
		return Character.isUpperCase(varName.charAt(0));
	}

}
