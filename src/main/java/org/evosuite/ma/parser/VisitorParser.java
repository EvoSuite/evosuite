/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ma.parser;

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
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.ma.Editor;
import org.evosuite.testcase.AbstractStatement;
import org.evosuite.testcase.ArrayIndex;
import org.evosuite.testcase.ArrayReference;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.AssignmentStatement;
import org.evosuite.testcase.BooleanPrimitiveStatement;
import org.evosuite.testcase.BytePrimitiveStatement;
import org.evosuite.testcase.CharPrimitiveStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.DoublePrimitiveStatement;
import org.evosuite.testcase.FieldReference;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.FloatPrimitiveStatement;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.LongPrimitiveStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.ShortPrimitiveStatement;
import org.evosuite.testcase.StringPrimitiveStatement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCluster;
import org.evosuite.testcase.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Visitor parser. If there is a error during parsing then {@code valid} will be
 * false, but it's still possible to get {@link TestCase} without invalid
 * instructions.
 * 
 * @author Yury Pavlov
 */
public class VisitorParser extends GenericVisitorAdapter<AbstractStatement, Object> {

	private static Logger logger = LoggerFactory.getLogger(VisitorParser.class);

	private TestCase newTC = new DefaultTestCase();

	private boolean valid = true;

	private final TypeTable tt = new TypeTable();

	private final TestCluster testCluster = TestCluster.getInstance();

	private final ArrayList<String> parsErrors = new ArrayList<String>();

	// call from GUI
	private boolean guiActive = false;

	public VisitorParser() {
		// to use it without GUI
	}

	public VisitorParser(boolean guiActive) {
		this.guiActive = guiActive;
	}

	@Override
	public AbstractStatement visit(VariableDeclarationExpr n, Object arg) {
		// use right side to get properly Statement
		logger.debug("getInit(): " + n.getVars().get(0).getInit());
		logger.debug("getType(): " + n.getType());
		Expression init = n.getVars().get(0).getInit();
		String name = n.getVars().get(0).getId().getName();
		return separator(init, n.getType(), name);
	}

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
		} else {
			// if we can't parse exactly we try get reference for right side,
			// clone it assign current value and return
			logger.debug("Try to cheat with ref");
			res = parse(init, arg);
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
		logger.debug("i'm here IntegerLiteralExpr: " + n);
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
		logger.debug("i'm here CharLiteralExpr");
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
		logger.debug("i'm here LongLiteralExpr");
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
		logger.debug("i'm here DoubleLiteralExpr");
		String init = n.getValue();
		if (arg instanceof PrimitiveType) {
			PrimitiveType primType = (PrimitiveType) arg;
			try {
				switch (primType.getType()) {
				case Float:
					res = new FloatPrimitiveStatement(newTC, Float.parseFloat(init.replace("F", "")));
					break;
				case Double:
					res = new DoublePrimitiveStatement(newTC, Double.parseDouble(init.replace("D", "")));
					break;
				default:
					res = new DoublePrimitiveStatement(newTC, Double.parseDouble(init.replace("D", "")));
				}
			} catch (NumberFormatException e) {
				addParsError("Primitive is assigned a var, not implemented yet.", n);
			}
		} else {
			res = new DoublePrimitiveStatement(newTC, Double.parseDouble(init.replace("D", "")));
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(BooleanLiteralExpr n, Object arg) {
		AbstractStatement res = null;
		logger.debug("i'm here BooleanLiteralExpr");
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
		logger.debug("i'm here StringLiteralExpr");
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
		logger.debug("Visit new Object: " + n);
		AbstractStatement res = null;
		try {
			List<Expression> args = n.getArgs();
			Class<?> clazz = getClass(n.getType().getName());
			logger.debug("Clazz loaded: " + clazz);

			List<VariableReference> params = getVarRefs(args);
			for (VariableReference variableReference : params) {
				logger.debug("Ref of param: " + variableReference);
			}

			ArrayList<Class<?>> paramClasses = new ArrayList<Class<?>>();
			for (VariableReference paramRef : params) {
				if (paramRef != null) {
					paramClasses.add(paramRef.getVariableClass());
				}
			}
			for (Class<?> class1 : paramClasses) {
				logger.debug("Params clazzs loaded: " + class1);
			}
			Constructor<?> constructor = getConstructor(clazz, paramClasses.toArray(new Class<?>[paramClasses.size()]));
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
			logger.debug("Field is loaded: " + field);
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
				logger.debug("Field Sttm created: " + res);
			}
		} catch (ParseException e) {
			addParsError(e.getMessage(), n);
		}
		checkedAddNewSttm(n, res);
		return res;
	}

	@Override
	public AbstractStatement visit(ArrayCreationExpr n, Object arg) {
		logger.debug("i'm here ArrayCreationExpr: " + n);
		List<Expression> dims = n.getDimensions();
		logger.debug("getArrayCount: " + n.getArrayCount());
		int[] sizes;
		if (dims == null && n.getInitializer().getValues() != null) {
			// TODO: init over values
			sizes = new int[1];
			sizes[0] = n.getInitializer().getValues().size();
		} else {
			logger.debug("There are dims: " + dims.size());
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

	private AbstractStatement createMultiDimArray(ArrayCreationExpr n, int[] sizes) {
		AbstractStatement res = null;
		Class<?> clazz = null;
		try {
			logger.debug("Array's type: " + n.getType().toString());
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
			logger.debug("Creating: " + i + " level of arrays");
			logger.debug("ArraysAmount: " + arraysAmount);
			Object array = Array.newInstance(clazz, sizes[i]);
			// how much elemts (current arrays) has next dim
			for (int j = 0; j < arraysAmount; j++) {
				res = new ArrayStatement(newTC, array.getClass(), sizes[i]);
				checkedAddNewSttm(n, res);
				currentDim.add((ArrayStatement) res);
				logger.debug("Create new array");
			}
			// we are at least on the second dim of array
			if (!prevDim.isEmpty()) {
				// for all current level arrays add prev. level arrays
				for (int j = 0; j < currentDim.size(); j++) {
					for (int k = 0; k < sizes[i]; k++) {
						// logger.debug("Add array: " +
						// lowArrays.remove());
						VariableReference haElRef = new ArrayIndex(newTC, (ArrayReference) currentDim.get(j)
								.getReturnValue(), k);
						AbstractStatement tmpAs = new AssignmentStatement(newTC, haElRef, prevDim.remove()
								.getReturnValue());
						checkedAddNewSttm(n, tmpAs);
					}
					logger.debug("Switch to the next ha");
				}
			}
			// update number of arrays for the next level
			if (i + 1 < sizes.length) {
				arraysAmount /= sizes[i + 1];
			}
			prevDim.addAll(currentDim);
			logger.debug("la has size: " + prevDim.size());
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
		logger.debug("AssignExpr: " + n);
		logger.debug("AssignExpr.getTarget(): " + target);
		logger.debug("AssignExpr.getValue(): " + value);
		try {
			lhs = getVarRef(target);
		} catch (ParseException e) {
			// try to create var for this value
			logger.debug("Try create new RHS");
			AbstractStatement lhsSttm = separator(value, null,
					"tEmPoRaLvArIaBlE" + n.getBeginLine() + n.getBeginColumn());
			logger.debug("rhs created: " + lhsSttm);
			lhs = lhsSttm.getReturnValue();
			logger.debug("New RHS is: " + lhs);
		}
		try {
			rhs = getVarRef(value);
		} catch (ParseException e) {
			// try to create var for this value
			logger.debug("Try create new RHS");
			AbstractStatement rhsSttm = separator(value, null,
					"tEmPoRaLvArIaBlE" + n.getBeginLine() + n.getBeginColumn());
			logger.debug("rhs created: " + rhsSttm);
			rhs = rhsSttm.getReturnValue();
			logger.debug("New RHS is: " + rhs);
		}
		res = new AssignmentStatement(newTC, lhs, rhs);
		checkedAddNewSttm(n, res);
		logger.debug("Add new assign Sttm");
		return res;
	}

	@Override
	public AbstractStatement visit(MethodCallExpr n, Object arg) {
		AbstractStatement res = null;
		List<Expression> args = n.getArgs();
		try {
			logger.debug("MethodCallExpr method's args: " + args);
			List<VariableReference> paramReferences = getVarRefs(args);
			Class<?>[] paramClasses = getVarClasses(paramReferences);
			Method method = getMethod(n, paramClasses);
			VariableReference callee = getVarRef(n.getScope());
			res = new MethodStatement(newTC, method, callee, method.getReturnType(), paramReferences);
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
		logger.debug("i'm here UnaryExpr");
		logger.debug(n.getOperator().toString());
		res = separator(expr, arg, "tEmPoRaLvArIaBlE" + n.getBeginLine() + n.getBeginColumn());
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

	private AbstractStatement parse(Expression n, Object arg) {
		AbstractStatement res = null;
		logger.debug("i'm here cheating parse Expression: " + n);
		// create new var with negated value of current and assign new var
		try {
			// find ref of var -> find pos. of ref's sttm & get sttm it
			// self for manipulation
			VariableReference refRHS = getVarRef(n);
			AbstractStatement rhs = (AbstractStatement) newTC.getStatement(refRHS.getStPosition());
			// TODO must be inserted in TT??? check
			AbstractStatement lhs = (AbstractStatement) rhs.clone(newTC);
			checkedAddNewSttm(n, lhs);
			res = new AssignmentStatement(newTC, lhs.getReturnValue(), refRHS);
			checkedAddNewSttm(n, res);
		} catch (ParseException e) {
			e.printStackTrace();
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
			parsErrors.add("Expression: " + n + ".\nIn line: " + n.getBeginLine() + ".\nSay: " + msg);
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
			logger.debug("TT has var: " + name);
			logger.debug("TT return class for var: " + tt.getClass(name));
			return tt.getClass(name);
		}

		try {
			return testCluster.getClass(name);
		} catch (ClassNotFoundException e) {
			if (guiActive) {
				String className = null;

				// choosing part
				Collection<String> allClasses = testCluster.getMatchingClasses(name);
				String[] choices = new String[allClasses.size()];
				allClasses.toArray(choices);
				if (choices.length == 0) {
					choices = new String[1];
					choices[0] = "Nothing to choose :p";
				}
				while ((className = Editor.chooseClassName(choices, name)) != null) {
					logger.debug("ClassDialog return: " + className);
					{
						try {
							return testCluster.importClass(className);
						} catch (ClassNotFoundException e1) {
							// just try again or press cancel
						}
					}
				}

				// last chance to find class
				while ((className = Editor.enterClassName(name)) != null) {
					{
						try {
							return testCluster.importClass(className);
						} catch (ClassNotFoundException e1) {
							// just try again or press cancel
						}
					}
				}
			}
		}
		throw new ParseException(null, "Can't load class: " + name);
	}

	private Class<?>[] getVarClasses(List<VariableReference> args) throws ParseException {
		List<Class<?>> res = new ArrayList<Class<?>>();

		if (args != null) {
			for (VariableReference varRef : args) {
				if (varRef != null) {
					res.add(varRef.getVariableClass());
				}
			}
		}
		return res.toArray(new Class<?>[res.size()]);
	}

	private List<VariableReference> getVarRefs(List<Expression> args) throws ParseException {
		List<VariableReference> res = new ArrayList<VariableReference>();
		if (args != null) {
			for (Expression expr : args) {
				logger.debug("Arg: " + expr);
				res.add(getVarRef(expr));
			}
		}
		return res;
	}

	private VariableReference getVarRef(Expression expr) throws ParseException {
		logger.debug("Try to find ref for expr: " + expr);
		if (expr instanceof NameExpr) {
			logger.debug("getVarRef. This is NameExpr: " + expr);
			String varName = ((NameExpr) expr).getName();
			if (tt.hasVar(varName)) {
				return tt.getVarReference(varName);
			} else {
				// needed for static access
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
				// for static create first local var and then return it's ref
				return visit(fieldAccExpr, null).getReturnValue();
			}
		} else if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr arrayAccExpr = (ArrayAccessExpr) expr;
			logger.debug("Getting array variable for " + expr);
			VariableReference avRef = getVarRef(arrayAccExpr.getName());
			ArrayReference arrayRef = null;
			if (avRef instanceof ArrayReference) {
				logger.debug("2) Have array's variable of type: " + avRef.getVariableClass());
				arrayRef = (ArrayReference) avRef;
			} else {
				logger.debug("1) Have array variable of type: " + avRef.getVariableClass() + " / "
						+ avRef.getComponentType());
				int size = ((ArrayStatement) newTC.getStatement(avRef.getStPosition())).size();
				AbstractStatement newArray = new ArrayStatement(newTC, avRef.getVariableClass(), size);
				AbstractStatement assign = new AssignmentStatement(newTC, newArray.getReturnValue(), avRef);
				newTC.addStatement(newArray);
				arrayRef = (ArrayReference) newTC.addStatement(assign);
			}
			logger.debug("Got array reference " + arrayAccExpr.getName());

			int arrayInd = 0;
			try {
				arrayInd = Integer.parseInt(arrayAccExpr.getIndex().toString());
			} catch (NumberFormatException e) {
				// If we can't parse it, just use 0
			}
			logger.debug("Array reference: " + arrayRef + ", index " + arrayInd);
			return new ArrayIndex(newTC, arrayRef, arrayInd);
		} else if (expr instanceof CastExpr) {
			return getVarRef(((CastExpr) expr).getExpr());
		} else if (expr instanceof MethodCallExpr) {
			return visit((MethodCallExpr) expr, null).getReturnValue();
		} else if (expr instanceof LiteralExpr) {
			logger.debug("create Ref for literal");
			return separator(expr, null, "tEmPoRaLvArIaBlE" + expr.getBeginColumn()).getReturnValue();
		} else if (expr instanceof UnaryExpr) {
			return parse((UnaryExpr) expr, null).getReturnValue();
		} else if (expr instanceof BinaryExpr) {
			// Just use one of the two
			return getVarRef(((BinaryExpr) expr).getLeft());
		} else if (expr instanceof EnclosedExpr) {
			return getVarRef(((EnclosedExpr) expr).getInner());
		} else if (expr instanceof ObjectCreationExpr) {
			logger.debug("create Ref for objectCreat");
			return separator((ObjectCreationExpr) expr, null, "tEmPoRaLvArIaBlE" + expr.getBeginColumn())
					.getReturnValue();
		} else if (expr instanceof ArrayCreationExpr) {
			return separator((ArrayCreationExpr) expr, null, "tEmPoRaLvArIaBlE" + expr.getBeginColumn())
					.getReturnValue();
		}
		throw new ParseException(null, "Can't find reference for variable: " + expr);
	}

	private Constructor<?> getConstructor(Class<?> clazz, Class<?>[] paramClasses) throws ParseException {
		try {
			return clazz.getConstructor(paramClasses);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			for (Constructor<?> constr : TestCluster.getConstructors(clazz)) {
				if (constr.getParameterTypes().length == paramClasses.length) {
					Class<?>[] constrParams = constr.getParameterTypes();
					for (int i = 0; i < constrParams.length; i++) {
						if (paramClasses[i] == null || constrParams[i].isAssignableFrom(paramClasses[i])) {
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
		throw new ParseException(null, "No such constructor in class " + clazz.getName() + ": "
				+ Arrays.asList(paramClasses));
	}

	private Field getField(FieldAccessExpr expr) throws ParseException {
		String scope = expr.getScope().toString();
		logger.debug("Scope of field: " + scope);
		Class<?> clazz = getClass(scope);
		logger.debug("Clazz for field is loaded: " + clazz);

		try {
			return clazz.getField(expr.getField());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			throw new ParseException(null, "Can not find the field.");
		}
		return null;
	}

	private Method getMethod(MethodCallExpr expr, Class<?>[] paramClasses) throws ParseException {
		Expression scope = expr.getScope();
		logger.debug("getMethod(MethodCallExpr expr: " + expr);
		logger.debug("Method's scope: " + scope);
		Class<?> clazz = null;
		// jUnit assert and other
		if (scope != null) {
			clazz = getClass(scope.toString());
			logger.debug("Clazz for method is loaded: " + clazz);

			String methodName = expr.getName();
			StringBuilder classNames = new StringBuilder();
			try {
				return clazz.getMethod(methodName, paramClasses);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				logger.debug("Looking for method " + methodName + " in class " + clazz.getName() + " with parameters "
						+ Arrays.asList(paramClasses));
				for (Method meth : TestCluster.getMethods(clazz)) {
					if (meth.getName().equals(methodName)) {
						if (meth.getParameterTypes().length == paramClasses.length) {
							Class<?>[] methParams = meth.getParameterTypes();
							logger.debug("Checking " + Arrays.asList(methParams));
							for (int i = 0; i < methParams.length; i++) {
								if (paramClasses[i] == null || methParams[i].isAssignableFrom(paramClasses[i])
										|| (methParams[i].equals(int.class) && paramClasses[i].equals(char.class))) {
									logger.debug("Parameter " + i + " matches");
									if (i == methParams.length - 1) {
										return meth;
									}
								} else {
									logger.debug("Parameter " + i + " does not match");
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
			throw new ParseException(null, "Can not find the method: \"" + methodName + "\", with parameter(s): "
					+ classNames);
		}
		return null;
	}

}
