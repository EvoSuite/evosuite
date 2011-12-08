package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.ParseException;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
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
import japa.parser.ast.expr.UnaryExpr.Operator;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.Type;
import japa.parser.ast.visitor.GenericVisitorAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.FloatPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.IntPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.LongPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.NullStatement;
import de.unisb.cs.st.evosuite.testcase.ShortPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StringPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Yury Pavlov All visit method, where statement for EvoSuite created,
 *         must add it in new TestCase
 */
public class TestVisitor extends GenericVisitorAdapter<AbstractStatement, Object> {

	private static Logger logger = LoggerFactory.getLogger(TestParser.class);

	private TestCase newTC = new DefaultTestCase();

	private boolean valid = true;

	private final TypeTable tt = new TypeTable();

	private final TestCluster testCluster = TestCluster.getInstance();

	private final ArrayList<String> parsErrors = new ArrayList<String>();

	private final Editor editor;

	/**
	 * @param editor
	 */
	public TestVisitor(Editor editor) {
		this.editor = editor;
	}

	@Override
	public AbstractStatement visit(VariableDeclarationExpr n, Object arg) {
		// use right side to get properly Statement
		System.out.println("getInit(): " + n.getVars().get(0).getInit());
		System.out.println("getType(): " + n.getType());
		Expression init = n.getVars().get(0).getInit();
		String name = n.getVars().get(0).getId().getName();
		return separator(init, n.getType(), name);
	}

	/**
	 * @param n
	 * @param init
	 * @return
	 */
	private AbstractStatement separator(Expression init, Object arg, String name) {
		AbstractStatement res = null;
		if (init instanceof IntegerLiteralExpr) {
			res = visit((IntegerLiteralExpr) init, arg);
		} else if (init instanceof CharLiteralExpr) {
			res = visit((CharLiteralExpr) init, null);
		} else if (init instanceof LongLiteralExpr) {
			res = visit((LongLiteralExpr) init, null);
		} else if (init instanceof DoubleLiteralExpr) {
			res = visit((DoubleLiteralExpr) init, arg);
		} else if (init instanceof BooleanLiteralExpr) {
			res = visit((BooleanLiteralExpr) init, null);
		} else if (init instanceof StringLiteralExpr) {
			res = visit((StringLiteralExpr) init, null);
		} else if (init instanceof UnaryExpr) {
			res = parse((UnaryExpr) init, arg);
		} else if (init instanceof ObjectCreationExpr) {
			res = visit((ObjectCreationExpr) init, null);
		} else if (init instanceof FieldAccessExpr) {
			res = visit((FieldAccessExpr) init, null);
		} else if (init instanceof ArrayCreationExpr) {
			res = visit((ArrayCreationExpr) init, arg);
		} else if (init instanceof NullLiteralExpr) {
			res = parse((NullLiteralExpr) init, arg);
		} else if (init instanceof CastExpr) {
			res = parse((CastExpr) init, null);
		} else if (init instanceof MethodCallExpr) {
			res = visit((MethodCallExpr) init, null);
		} else if (init instanceof NameExpr) {
			res = parse((NameExpr) init, null);
		} else {
			System.out.println("Uppsss!");
			res = visit(init, arg);
		}
		if (res != null) {
			try {
				tt.addVar(new Var(name, (Type) arg, res.getReturnValue()));
			} catch (ParseException e) {
				addParsError(e.getMessage(), init);
				// When the TT is broken then the TC is broken too
				valid = false;
			}
		}
		return res;
	}

	@Override
	public AbstractStatement visit(IntegerLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here IntegerLiteralExpr: " + n);
		String init = n.getValue();
		if (arg instanceof PrimitiveType) {
			PrimitiveType primType = (PrimitiveType) arg;
			try {
				switch (primType.getType()) {
				case Byte:
					res = new BytePrimitiveStatement(newTC, Byte.parseByte(init));
					break;
				case Short:
					res = new ShortPrimitiveStatement(newTC, Short.parseShort(init));
					break;
				case Int:
					res = new IntPrimitiveStatement(newTC, Integer.parseInt(init));
					break;
				default:
					res = new IntPrimitiveStatement(newTC, Integer.parseInt(init));
				}
			} catch (NumberFormatException e) {
				addParsError("Primitive is assigned a var, not implemented yet.", n);
			}
		} else {
			res = new IntPrimitiveStatement(newTC, Integer.parseInt(init));
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(CharLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here CharLiteralExpr");
		String init = n.getValue();
		try {
			res = new CharPrimitiveStatement(newTC, init.charAt(0));
		} catch (NumberFormatException e) {
			addParsError("Primitive is assigned a var, not implemented yet.", n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(LongLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here LongLiteralExpr");
		String init = n.getValue();
		try {
			res = new LongPrimitiveStatement(newTC, Long.parseLong(init.replace("L", "")));
		} catch (NumberFormatException e) {
			addParsError("Primitive is assigned a var, not implemented yet.", n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(DoubleLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here DoubleLiteralExpr");
		String init = n.getValue();
		if (arg instanceof PrimitiveType) {
			PrimitiveType primType = (PrimitiveType) arg;
			try {
				switch (primType.getType()) {
				case Float:
					res = new FloatPrimitiveStatement(newTC,
					        Float.parseFloat(init.replace("F", "")));
					break;
				case Double:
					res = new DoublePrimitiveStatement(newTC,
					        Double.parseDouble(init.replace("D", "")));
					break;
				default:
					res = new DoublePrimitiveStatement(newTC,
					        Double.parseDouble(init.replace("D", "")));
				}
			} catch (NumberFormatException e) {
				addParsError("Primitive is assigned a var, not implemented yet.", n);
			}
		} else {
			res = new DoublePrimitiveStatement(newTC,
			        Double.parseDouble(init.replace("D", "")));
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(BooleanLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here BooleanLiteralExpr");
		try {
			res = new BooleanPrimitiveStatement(newTC, n.getValue());
		} catch (NumberFormatException e) {
			addParsError("Primitive is assigned a var, not implemented yet.", n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(StringLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here StringLiteralExpr");
		try {
			String value = StringEscapeUtils.unescapeJava(n.getValue());
			res = new StringPrimitiveStatement(newTC, value);
		} catch (NumberFormatException e) {
			addParsError("Primitive is assigned a var, not implemented yet.", n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(ObjectCreationExpr n, Object arg) {
		System.out.println("Visit new Object: " + n);
		AbstractStatement res = null;
		try {
			List<Expression> args = n.getArgs();
			Class<?> clazz = getClass(n.getType().getName());
			System.out.println("Clazz loaded: " + clazz);

			List<VariableReference> params = getVarRefs(args);
			for (VariableReference variableReference : params) {
				System.out.println("Ref of param: " + variableReference);
			}

			ArrayList<Class<?>> paramClasses = new ArrayList<Class<?>>();
			for (VariableReference refs : params) {
				paramClasses.add(refs.getVariableClass());
			}
			for (Class<?> class1 : paramClasses) {
				System.out.println("Params clazzs loaded: " + class1);
			}
			Constructor<?> constructor = getConstructor(clazz,
			                                            paramClasses.toArray(new Class<?>[paramClasses.size()]));
			res = new ConstructorStatement(newTC, constructor, clazz, params);
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(FieldAccessExpr n, Object arg) {
		AbstractStatement res = null;
		Field field = null;
		String scope = n.getScope().toString();
		try {
			field = getField(n);
			System.out.println("Field is loaded: " + field);
			if (field != null) {
				Class<?> type = field.getType();
				VariableReference source = null;
				if (tt.hasVar(scope)) {
					// dynamic field
					source = tt.getVarReference(scope);
				}
				if (!(Modifier.isStatic(field.getModifiers())) && source == null) {
					addParsError("Can't load dynamic field in static way.", n);
				}
				res = new FieldStatement(newTC, field, source, type);
				System.out.println("Field Sttm created: " + res);
			}
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(ArrayCreationExpr n, Object arg) {
		System.out.println("i'm here ArrayCreationExpr: " + n);
		List<Expression> dims = n.getDimensions();
		System.out.println("getArrayCount: " + n.getArrayCount());
		int[] sizes;
		if (dims == null && n.getInitializer().getValues() != null) {
			// TODO: init over values
			sizes = new int[1];
			sizes[0] = n.getInitializer().getValues().size();
		} else {
			System.out.println("There are dims: " + dims.size());
			sizes = new int[dims.size()];
			int i = 0;
			if (dims != null && !dims.isEmpty()) {
				for (Expression expr : dims) {
					try {
						// only int value is possible see ArraySttm
						sizes[i++] = Integer.parseInt(expr.toString());
					} catch (NumberFormatException e) {
						// TODO: Better value
						sizes[i++] = 1;
					}
				}
			}
		}
		return createMultiDimArray(n, sizes);
	}

	/**
	 * @param n
	 * @param res
	 * @param sizes
	 * @return
	 */
	private AbstractStatement createMultiDimArray(ArrayCreationExpr n, int[] sizes) {
		AbstractStatement res = null;
		Class<?> clazz = null;
		try {
			System.out.println("Array's type: " + n.getType().toString());
			clazz = getClass(n.getType().toString());
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		// for array of arrays without dim information
		for (int i = 0; i < n.getArrayCount(); i++) {
			Object array = Array.newInstance(clazz, 0);
			clazz = array.getClass();
		}
		Queue<ArrayStatement> prevDim = new ArrayDeque<ArrayStatement>();
		ArrayList<ArrayStatement> currentDim = new ArrayList<ArrayStatement>();
		// how much first (1-dim) level arrays we need
		int arraysAmount = 1;
		for (int i = 1; i < sizes.length; i++) {
			arraysAmount *= sizes[i];
		}
		// how much dims has array
		for (int i = 0; i < sizes.length; i++) {
			System.out.println("Creating: " + i + " level of arrays");
			System.out.println("ArraysAmount: " + arraysAmount);
			Object array = Array.newInstance(clazz, sizes[i]);
			// how much elemts (current arrays) has next dim
			for (int j = 0; j < arraysAmount; j++) {
				res = new ArrayStatement(newTC, array.getClass(), sizes[i]);
				checkedAddNewSttm(n, res);
				currentDim.add((ArrayStatement) res);
				System.out.println("Create new array");
			}
			// we are at least on the second dim of array
			if (!prevDim.isEmpty()) {
				// for all current level arrays add prev. level arrays
				for (int j = 0; j < currentDim.size(); j++) {
					for (int k = 0; k < sizes[i]; k++) {
						// System.out.println("Add array: " +
						// lowArrays.remove());
						VariableReference haElRef = new ArrayIndex(newTC,
						        (ArrayReference) currentDim.get(j).getReturnValue(), k);
						AbstractStatement tmpAs = new AssignmentStatement(newTC, haElRef,
						        prevDim.remove().getReturnValue());
						checkedAddNewSttm(n, tmpAs);
					}
					System.out.println("Switch to the next ha");
				}
			}
			// update number of arrays for the next level
			if (i + 1 < sizes.length) {
				arraysAmount /= sizes[i + 1];
			}
			prevDim.addAll(currentDim);
			System.out.println("la has size: " + prevDim.size());
			currentDim.clear();
			clazz = res.getReturnClass();
		}
		return res;
	}

	@Override
	public AbstractStatement visit(AssignExpr n, Object arg) {
		AbstractStatement res = null;
		VariableReference rhs = null, lhs = null;
		Expression target = n.getTarget();
		Expression value = n.getValue();
		System.out.println("AssignExpr: " + n);
		System.out.println("AssignExpr.getTarget(): " + target);
		System.out.println("AssignExpr.getValue(): " + value);
		try {
			lhs = getVarRef(target);
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		try {
			rhs = getVarRef(value);
		} catch (ParseException e) {
			// try to create var for this value
			System.out.println("Try create new RHS");
			AbstractStatement rhsSttm = separator(value, null,
			                                      "tEmPoRaLvArIaBlE" + n.getBeginLine()
			                                              + n.getBeginColumn());
			System.out.println("rhs created: " + rhsSttm);
			rhs = rhsSttm.getReturnValue();
			System.out.println("New RHS is: " + rhs);
		}
		res = new AssignmentStatement(newTC, lhs, rhs);
		checkedAddNewSttm(n, res);
		System.out.println("Add new assign Sttm");
		return res;
	}

	@Override
	public AbstractStatement visit(MethodCallExpr n, Object arg) {
		AbstractStatement res = null;
		List<Expression> args = n.getArgs();
		try {
			System.out.println("MethodCallExpr method's args: " + args);
			List<VariableReference> paramReferences = getVarRefs(args);
			Class<?>[] paramClasses = getVarClasses(paramReferences);
			Method method = getMethod(n, paramClasses);
			VariableReference callee = getVarRef(n.getScope());
			res = new MethodStatement(newTC, method, callee, method.getReturnType(),
			        paramReferences);
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	private AbstractStatement parse(UnaryExpr n, Object arg) {
		AbstractStatement res = null;
		Expression expr = n.getExpr();
		Operator op = n.getOperator();
		System.out.println("i'm here UnaryExpr");
		System.out.println(n.getOperator());
		res = separator(expr, arg,
		                "tEmPoRaLvArIaBlE" + n.getBeginLine() + n.getBeginColumn());
		if (res != null) {
			if (op == Operator.negative || op == Operator.not) {
				res.negate();
			}
		} else {
			addParsError("Can't parse unary expr.", n);
		}
		return res;
	}

	private AbstractStatement parse(NullLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		try {
			res = new NullStatement(newTC, getClass(arg.toString()));
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	private AbstractStatement parse(CastExpr n, Object arg) {
		// TODO check later
		if (n.getExpr() instanceof MethodCallExpr) {
			return visit((MethodCallExpr) n.getExpr(), null);
		} else {
			return visit(n.getExpr(), null);
		}
	}

	private AbstractStatement parse(NameExpr n, Object arg) {
		AbstractStatement res = null;
		System.out.println("i'm here parse NameExpr: " + n);
		String varName = n.getName();
		// create new var with negated value of current and assign new var
		if (tt.hasVar(varName)) {
			try {
				tt.getVarReference(varName);
				// find ref of var -> find pos. of ref's sttm & get sttm it
				// self for manipulation
				AbstractStatement tmpAS = (AbstractStatement) newTC.getStatement(tt.getVarReference(varName).getStPosition());
				// TODO must be inserted in TT??? check later
				res = (AbstractStatement) tmpAS.clone(newTC);
				checkedAddNewSttm(n, res);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	private void checkedAddNewSttm(Expression n, AbstractStatement res) {
		if (res == null) {
			addParsError("Error by parsing the expression: ", n);
		} else {
			newTC.addStatement(res);
		}
	}

	private void addParsError(String msg, Expression n) {
		if (n != null) {
			parsErrors.add("Expression: " + n + ".\nIn line: " + n.getBeginLine()
			        + ".\nSay: " + msg);
		} else {
			parsErrors.add(msg);
		}
		valid = false;
	}

	public TestCase getNewTC() {
		return newTC;
	}

	public void reset() {
		newTC = new DefaultTestCase();
		valid = true;
		tt.reset();
		parsErrors.clear();
	}

	public boolean isValid() {
		return valid;
	}

	public ArrayList<String> getParsErrors() {
		return parsErrors;
	}

	public Class<?> getClass(String name) throws ParseException {
		// First try to find exact match
		if (name.equals("int"))
			return int.class;
		else if (name.equals("boolean"))
			return boolean.class;
		else if (name.equals("float"))
			return float.class;
		else if (name.equals("char"))
			return char.class;
		else if (name.equals("byte"))
			return byte.class;
		else if (name.equals("short"))
			return short.class;
		else if (name.equals("long"))
			return long.class;
		else if (name.equals("double"))
			return double.class;

		// dynamic access over variable
		if (tt.hasVar(name)) {
			System.out.println("TT has var: " + name);
			System.out.println("TT return class for var: " + tt.getClass(name));
			return tt.getClass(name);
		}

		try {
			return testCluster.getClass(name);
		} catch (ClassNotFoundException e) {
			String className = editor.chooseTargetFile(name).getName();
			if (className != null) {
				try {
					return testCluster.importClass(className);
				} catch (ClassNotFoundException e1) {
					throw new ParseException(null, "Class not found: " + e1.getMessage());
				}
			} else {
				throw new ParseException(null, "Class not found: " + e.getMessage());
			}
		}
	}

	private Class<?>[] getVarClasses(List<VariableReference> args) throws ParseException {
		List<Class<?>> res = new ArrayList<Class<?>>();

		if (args != null) {
			for (VariableReference varRef : args) {
				res.add(varRef.getVariableClass());
			}
		}
		return res.toArray(new Class<?>[res.size()]);
	}

	private List<VariableReference> getVarRefs(List<Expression> args) {
		List<VariableReference> res = new ArrayList<VariableReference>();
		if (args != null) {
			for (Expression expr : args) {
				System.out.println("Arg: " + expr);
				try {
					res.add(getVarRef(expr));
				} catch (ParseException e) {
					addParsError(e.getMessage(), null);
				}
			}
		}
		return res;
	}

	private VariableReference getVarRef(Expression expr) throws ParseException {
		System.out.println("Try to find ref for expr: " + expr);
		if (expr instanceof NameExpr) {
			System.out.println("getVarRef. This is NameExpr: " + expr);
			String varName = ((NameExpr) expr).getName();
			if (tt.hasVar(varName)) {
				return tt.getVarReference(varName);
			} else {
				return null;
			}
		} else if (expr instanceof FieldAccessExpr) {
			FieldAccessExpr fieldAccExpr = (FieldAccessExpr) expr;
			String scope = fieldAccExpr.getScope().toString();
			Field field = getField(fieldAccExpr);
			VariableReference varRef = null;
			if (tt.hasVar(scope)) {
				// dynamic access
				varRef = tt.getVarReference(scope);
				return new FieldReference(newTC, field, varRef);
			} else {
				// for static create first local var and return it's ref
				return visit(fieldAccExpr, null).getReturnValue();
			}
		} else if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;
			System.out.println("arrayAccExpr.getClass(): " + arrayAccExpr.getClass());
			System.out.println("arrayAccExpr.getData(): " + arrayAccExpr.getData());
			System.out.println("arrayAccExpr.getIndex(): " + arrayAccExpr.getIndex());
			System.out.println("arrayAccExpr.getName(): " + arrayAccExpr.getName());
			System.out.println("-----Getting array variable for " + expr);
			VariableReference avRef = getVarRef(arrayAccExpr.getName());
			ArrayReference arrayRef = null;
			if (avRef instanceof ArrayReference) {
				System.out.println("2) Have array's variable of type: "
				        + avRef.getVariableClass());
				arrayRef = (ArrayReference) avRef;
			} else {
				System.out.println("1) Have array variable of type: "
				        + avRef.getVariableClass() + " / " + avRef.getComponentType());
				int size = ((ArrayStatement) newTC.getStatement(avRef.getStPosition())).size();
				AbstractStatement newArray = new ArrayStatement(newTC,
				        avRef.getVariableClass(), size);
				AbstractStatement assign = new AssignmentStatement(newTC,
				        newArray.getReturnValue(), avRef);
				newTC.addStatement(newArray);
				arrayRef = (ArrayReference) newTC.addStatement(assign);
			}
			System.out.println("Got array reference " + arrayAccExpr.getName());

			int arrayInd = 0;
			try {
				arrayInd = Integer.parseInt(arrayAccExpr.getIndex().toString());
			} catch (NumberFormatException e) {
				// If we can't parse it, just use 0
			}
			System.out.println("Array reference: " + arrayRef + ", index " + arrayInd);
			return new ArrayIndex(newTC, arrayRef, arrayInd);

		} else if (expr instanceof CastExpr) {
			return getVarRef(((CastExpr) expr).getExpr());
		} else if (expr instanceof MethodCallExpr) {
			return visit((MethodCallExpr) expr, null).getReturnValue();
		} else if (expr instanceof LiteralExpr) {
			System.out.println("create Ref for literal");
			return separator(expr, null, "tEmPoRaLvArIaBlE" + expr.getBeginColumn()).getReturnValue();
		} else if (expr instanceof UnaryExpr) {
			return parse((UnaryExpr) expr, null).getReturnValue();
		} else if (expr instanceof BinaryExpr) {
			// Just use one of the two
			return getVarRef(((BinaryExpr) expr).getLeft());
		} else if (expr instanceof EnclosedExpr) {
			return getVarRef(((EnclosedExpr) expr).getInner());
		} else if (expr instanceof ObjectCreationExpr) {
			System.out.println("create Ref for objectCreat");
			return separator(expr, null, "tEmPoRaLvArIaBlE" + expr.getBeginColumn()).getReturnValue();
		} else if (expr instanceof ArrayCreationExpr) {
			return separator(expr, null, "tEmPoRaLvArIaBlE" + expr.getBeginColumn()).getReturnValue();
		}
		throw new ParseException(null, "Can't find reference for variable: " + expr);
	}

	private Constructor<?> getConstructor(Class<?> clazz, Class<?>[] paramClasses)
	        throws ParseException {
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
						        || constrParams[i].isAssignableFrom(paramClasses[i])) {
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
		throw new ParseException(null, "No such constructor in class " + clazz.getName()
		        + ": " + Arrays.asList(paramClasses));
	}

	private Field getField(FieldAccessExpr expr) throws ParseException {
		String scope = expr.getScope().toString();
		System.out.println("Scope of field: " + scope);
		Class<?> clazz = getClass(scope);
		System.out.println("Clazz for field is loaded: " + clazz);

		try {
			return clazz.getField(expr.getField());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			throw new ParseException(null, "Can not find the field.");
		}
		return null;
	}

	/**
	 * @param expr
	 * @return
	 * @throws ParseException
	 */
	private Method getMethod(MethodCallExpr expr, Class<?>[] paramClasses)
	        throws ParseException {
		Expression scope = expr.getScope();
		System.out.println("Method's scope: " + scope);
		Class<?> clazz = getClass(scope.toString());
		System.out.println("Clazz for method is loaded: " + clazz);

		String methodName = expr.getName();
		StringBuilder classNames = new StringBuilder();
		try {
			return clazz.getMethod(methodName, paramClasses);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			System.out.println("Looking for method " + methodName + " in class "
			        + clazz.getName() + " with parameters " + Arrays.asList(paramClasses));
			for (Method meth : TestCluster.getMethods(clazz)) {
				if (meth.getName().equals(methodName)) {
					if (meth.getParameterTypes().length == paramClasses.length) {
						Class<?>[] methParams = meth.getParameterTypes();
						System.out.println("Checking " + Arrays.asList(methParams));
						for (int i = 0; i < methParams.length; i++) {
							if (paramClasses[i] == null
							        || methParams[i].isAssignableFrom(paramClasses[i])
							        || (methParams[i].equals(int.class) && paramClasses[i].equals(char.class))) {
								System.out.println("Parameter " + i + " matches");
								if (i == methParams.length - 1) {
									return meth;
								}
							} else {
								System.out.println("Parameter " + i + " does not match");
								break;
							}
						}
					}
				}
			}
			for (Class<?> paramType : paramClasses) {
				classNames.append(paramType.getName() + "; ");
			}
		}
		throw new ParseException(null, "Can not find the method: \"" + methodName
		        + "\", with parameter(s): " + classNames);
	}

}
