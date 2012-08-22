package org.evosuite.symbolic;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.IntegerVariableFactory;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.RealVariableFactory;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringVariable;
import org.evosuite.symbolic.expr.StringVariableFactory;
import org.evosuite.symbolic.vm.IntegerOperand;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.RealOperand;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.ReferenceOperand;
import org.evosuite.symbolic.vm.StringReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.DoublePrimitiveStatement;
import org.evosuite.testcase.ExecutionObserver;
import org.evosuite.testcase.IntPrimitiveStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.StringPrimitiveStatement;
import org.evosuite.testcase.VariableReference;
import org.objectweb.asm.Type;

import edu.uta.cse.dsc.VM;

import gnu.trove.map.hash.THashMap;

public class SymbolicExecutionObserver extends ExecutionObserver {

	private SymbolicEnvironment env;

	public SymbolicExecutionObserver(SymbolicEnvironment env) {
		this.env = env;
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	// ConstructorStatement
	private static final String V_V = Type.getMethodDescriptor(Type.VOID_TYPE,
			Type.VOID_TYPE);

	private void beforeStatement(ConstructorStatement stmt) {
		String className = stmt.getConstructor().getDeclaringClass().getName();
		VM.NEW(className);
		VM.DUP();
		this.pushParameterList(stmt.parameters);
		VM.INVOKESPECIAL(className.replace(".", "/"), "<init>", V_V);
	}

	private Operand clearAndGetBottom() {
		Operand lastOp = null;

		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		while (it.hasNext()) {
			lastOp = it.next();
		}
		return lastOp;
	}

	private void afterStatement(ConstructorStatement stmt) {
		String className = stmt.getConstructor().getDeclaringClass().getName();
		VM.CALL_RESULT(className.replace(".", "/"), "<init>", V_V);
		VariableReference varRef = stmt.getReturnValue();
		Operand stackBottom = clearAndGetBottom();

		NonNullReference nonNullRef = (NonNullReference) ((ReferenceOperand) stackBottom)
				.getReference();
		String varName = varRef.getName();
		symb_references.put(varName, nonNullRef);

	}

	@Override
	public void beforeStatement(StatementInterface statement, Scope scope) {
		if (env.isEmpty()) {
			env.prepareStack(null);
		}

		if (statement instanceof ConstructorStatement) {
			beforeStatement((ConstructorStatement) statement);
		} else if (statement instanceof IntPrimitiveStatement) {
			beforeStatement((IntPrimitiveStatement) statement);
		} else if (statement instanceof StringPrimitiveStatement) {
			beforeStatement((StringPrimitiveStatement) statement);
		} else if (statement instanceof MethodStatement) {
			beforeStatement((MethodStatement) statement);
		} else if (statement instanceof DoublePrimitiveStatement) {
			beforeStatement((DoublePrimitiveStatement) statement);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void beforeStatement(DoublePrimitiveStatement statement) {
		/* do nothing */
	}

	private void beforeStatement(MethodStatement statement) {
		boolean needThis = statement.getCallee() != null;

		if (needThis) {
			VariableReference callee = statement.getCallee();
			String calleeVarName = callee.getName();
			Reference ref = symb_references.get(calleeVarName);
			this.env.topFrame().operandStack.pushRef(ref);
		}

		List<VariableReference> parameters = statement.getParameterReferences();
		pushParameterList(parameters);
		int i = 10;
	}

	private void pushParameterList(List<VariableReference> parameters) {
		for (VariableReference varRef : parameters) {
			String varName = varRef.getName();
			if (symb_expressions.containsKey(varName)) {
				Expression<?> symb_expr = symb_expressions.get(varName);
				if (symb_expr instanceof StringExpression) {
					StringExpression strExpr = (StringExpression) symb_expr;
					env.topFrame().operandStack.pushStringRef(strExpr);
				} else if (symb_expr instanceof RealExpression) {
					RealExpression realExpr = (RealExpression) symb_expr;
					if (isFp32(varRef.getVariableClass())) {
						env.topFrame().operandStack.pushFp32(realExpr);
					} else {
						env.topFrame().operandStack.pushFp64(realExpr);
					}
				} else if (symb_expr instanceof IntegerExpression) {
					IntegerExpression integerExpr = (IntegerExpression) symb_expr;
					if (isBv32(varRef.getVariableClass())) {
						env.topFrame().operandStack.pushBv32(integerExpr);
					} else {
						env.topFrame().operandStack.pushBv64(integerExpr);
					}

				}
			} else if (symb_references.containsKey(varName)) {
				Reference ref = symb_references.get(varName);
				env.topFrame().operandStack.pushRef(ref);
			} else {
				throw new IllegalStateException("Unknown symbolic value for "
						+ varName);
			}

		}
	}

	private static boolean isFp32(Class<?> clazz) {
		return clazz.equals(float.class);
	}

	private void beforeStatement(StringPrimitiveStatement statement) {
		/* do nothing */
	}

	private void beforeStatement(IntPrimitiveStatement statement) {
		/* do nothing */
	}

	@Override
	public void afterStatement(StatementInterface statement, Scope scope,
			Throwable exception) {
		if (statement instanceof ConstructorStatement) {
			afterStatement((ConstructorStatement) statement);
		} else if (statement instanceof IntPrimitiveStatement) {
			afterStatement((IntPrimitiveStatement) statement);
		} else if (statement instanceof StringPrimitiveStatement) {
			afterStatement((StringPrimitiveStatement) statement);
		} else if (statement instanceof MethodStatement) {
			afterStatement((MethodStatement) statement);
		} else if (statement instanceof DoublePrimitiveStatement) {
			afterStatement((DoublePrimitiveStatement) statement);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void afterStatement(DoublePrimitiveStatement statement) {
		double valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		RealVariable integerVariable = RealVariableFactory.buildRealVariable(
				varRefName, valueOf, Double.MIN_VALUE, Double.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);

	}

	private void afterStatement(MethodStatement statement) {
		VariableReference varRef = statement.getReturnValue();
		String varName = varRef.getName();
		Operand stackOperand = env.topFrame().operandStack.popOperand();
		if (stackOperand instanceof IntegerOperand) {
			IntegerOperand integerOperand = (IntegerOperand) stackOperand;
			IntegerExpression integerExpression = integerOperand
					.getIntegerExpression();
			symb_expressions.put(varName, integerExpression);

		} else if (stackOperand instanceof RealOperand) {
			RealOperand realOperand = (RealOperand) stackOperand;
			RealExpression realExpression = realOperand.getRealExpression();
			symb_expressions.put(varName, realExpression);

		} else if (stackOperand instanceof ReferenceOperand) {
			ReferenceOperand ref_operand = (ReferenceOperand) stackOperand;
			Reference ref = ref_operand.getReference();
			if (ref instanceof StringReference) {
				StringReference str_ref = (StringReference) ref;
				StringExpression str_expr = str_ref.getStringExpression();
				symb_expressions.put(varName, str_expr);
			} else {
				symb_references.put(varName, ref);
			}
		}

		// dispose all other arguments
		env.topFrame().operandStack.clearOperands();

	}

	private static boolean isBv32(Class<?> clazz) {
		return clazz.equals(int.class) || clazz.equals(char.class)
				|| clazz.equals(byte.class) || clazz.equals(short.class)
				|| clazz.equals(boolean.class);
	}

	private void afterStatement(StringPrimitiveStatement statement) {
		String valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		StringVariable stringVariable = StringVariableFactory
				.buildStringVariable(varRefName, valueOf, valueOf, valueOf);
		symb_expressions.put(varRefName, stringVariable);
	}

	private Map<String, Expression<?>> symb_expressions = new THashMap<String, Expression<?>>();
	private Map<String, Reference> symb_references = new THashMap<String, Reference>();

	private void afterStatement(IntPrimitiveStatement statement) {
		int valueOf = statement.getValue();
		VariableReference varRef = statement.getReturnValue();
		String varRefName = varRef.getName();
		IntegerVariable integerVariable = IntegerVariableFactory
				.buildIntegerVariable(varRefName, valueOf, Integer.MIN_VALUE,
						Integer.MAX_VALUE);
		symb_expressions.put(varRefName, integerVariable);
	}

	@Override
	public void clear() {
		symb_expressions.clear();
		symb_references.clear();
	}

}
