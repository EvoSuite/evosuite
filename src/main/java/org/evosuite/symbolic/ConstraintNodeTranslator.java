package org.evosuite.symbolic;

import java.util.Map;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerVariable;
import org.evosuite.symbolic.expr.RealConstant;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealVariable;
import org.evosuite.symbolic.expr.StringConstant;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringVariable;

import edu.uta.cse.dsc.ast.BitVector32;
import edu.uta.cse.dsc.ast.BitVector64;
import edu.uta.cse.dsc.ast.BoundVariable;
import edu.uta.cse.dsc.ast.DoubleExpression;
import edu.uta.cse.dsc.ast.FloatExpression;
import edu.uta.cse.dsc.ast.JvmExpression;
import edu.uta.cse.dsc.ast.JvmVariable;
import edu.uta.cse.dsc.ast.Reference;
import edu.uta.cse.dsc.ast.bitvector.BitVector32Variable;
import edu.uta.cse.dsc.ast.bitvector.BitVector64Variable;
import edu.uta.cse.dsc.ast.bitvector.LiteralBitVector32;
import edu.uta.cse.dsc.ast.bitvector.LiteralBitVector64;
import edu.uta.cse.dsc.ast.fp.DoubleLiteral;
import edu.uta.cse.dsc.ast.fp.DoubleVariable;
import edu.uta.cse.dsc.ast.fp.FloatLiteral;
import edu.uta.cse.dsc.ast.fp.FloatVariable;
import edu.uta.cse.dsc.ast.reference.LiteralNonNullReference;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayLiteral;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayVariable;
import edu.uta.cse.dsc.pcdump.ast.AndConstraint;
import edu.uta.cse.dsc.pcdump.ast.ConstraintNodeVisitor;
import edu.uta.cse.dsc.pcdump.ast.EqConstraint;
import edu.uta.cse.dsc.pcdump.ast.LeqConstraint;
import edu.uta.cse.dsc.pcdump.ast.LessThanConstraint;
import edu.uta.cse.dsc.pcdump.ast.NotConstraint;
import edu.uta.cse.dsc.pcdump.ast.OrConstraint;
import edu.uta.cse.dsc.pcdump.ast.SelectConstraint;
import edu.uta.cse.dsc.pcdump.ast.UpdateConstraint;

public final class ConstraintNodeTranslator extends ConstraintNodeVisitor {

	private final ConcolicState concolicState;

	private final JvmExpressionTranslator expressionTranslator;

	public ConstraintNodeTranslator(Map<JvmVariable, String> symbolicVariables) {
		this.concolicState = new ConcolicState(symbolicVariables);
		this.expressionTranslator = new JvmExpressionTranslator(
				concolicState);
	}

	private IntegerExpression translateToIntegerExpr(
			edu.uta.cse.dsc.ast.Expression e) {
		if (!isIntegerOrLong(e)) {
			throw new IllegalArgumentException(e.toString()
					+ " is not a valid integer expression");
		}

		if (e instanceof BitVector32) {
			BitVector32 bitVector32 = (BitVector32) e;
			Object e_visit = bitVector32.accept(expressionTranslator);
			IntegerExpression integerExpr = (IntegerExpression) e_visit;
			return integerExpr;
		} else if (e instanceof BitVector64) {
			BitVector64 bitVector64 = (BitVector64) e;
			IntegerExpression integerExpr = (IntegerExpression) bitVector64
					.accept(expressionTranslator);
			return integerExpr;

		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Given a fresh_var=s(map_var,index_expr) constraint stores
	 * fresh_var->eval(map_var)[eval(index_expr)]
	 * 
	 */
	@Override
	public Object visit(SelectConstraint c) {
		// get constraint components
		edu.uta.cse.dsc.ast.Expression fresh_var_expr = c.getVar();
		edu.uta.cse.dsc.ast.Expression map_expr = c.getMap();
		edu.uta.cse.dsc.ast.Expression index_expr = c.getIndex();

		if (index_expr instanceof BoundVariable.Ref) {
			// ignore axiomatic constraints
			return null;
		}

		JvmVariable fresh_var = (JvmVariable) fresh_var_expr;
		Z3ArrayVariable<?, ?> map_var = (Z3ArrayVariable<?, ?>) map_expr;
		JvmExpression index_jvm_expr = (JvmExpression) index_expr;

		JvmExpression evalIndex = evaluateConcrete(index_jvm_expr);
		JvmExpression symbolicValue = this.concolicState.getSymbolicValue(
				map_var, evalIndex);
		JvmExpression concreteValue = this.concolicState.getConcreteValue(
				map_var, evalIndex);

		this.concolicState.declareNewSymbolicVariable(fresh_var,
				symbolicValue, concreteValue);

		if (this.concolicState.isMarked(fresh_var)) {
			return buildNewSymbolicVariableDefinition(fresh_var, symbolicValue);
		} else {
			return null;
		}

	}

	private Object buildNewSymbolicVariableDefinition(JvmVariable fresh_var,
			JvmExpression symbolicValue) {

		if (symbolicValue instanceof LiteralBitVector32) {
			// int
			LiteralBitVector32 arg = (LiteralBitVector32) symbolicValue;
			return buildNewSymbolicVariableDefinition(fresh_var, arg);

		} else if (symbolicValue instanceof LiteralBitVector64) {
			// long
			LiteralBitVector64 arg = (LiteralBitVector64) symbolicValue;
			return buildNewSymbolicVariableDefinition(fresh_var, arg);
		}
		if (symbolicValue instanceof FloatExpression) {
			// float
			FloatLiteral arg = (FloatLiteral) symbolicValue;
			return buildNewSymbolicVariableDefinition(fresh_var, arg);

		} else if (symbolicValue instanceof DoubleExpression) {
			// double
			DoubleLiteral arg = (DoubleLiteral) symbolicValue;
			return buildNewSymbolicVariableDefinition(fresh_var, arg);

		} else if (symbolicValue instanceof LiteralNonNullReference) {
			// Object
			LiteralNonNullReference arg = (LiteralNonNullReference) symbolicValue;
			return buildNewSymbolicVariableDefinition(fresh_var, arg);

		} else {
			throw new IllegalArgumentException(
					"Cannot handle symbolic value definition of class "
							+ symbolicValue.getClass().getName());
		}
	}

	private StringConstraint buildNewSymbolicVariableDefinition(
			JvmVariable fresh_var, LiteralNonNullReference string_reference) {
		String var_name_str;
		if (this.concolicState.isMarked(fresh_var)) {
			var_name_str = this.concolicState.getSymbolicName(fresh_var);
		} else {
			var_name_str = fresh_var.getName();
		}
		String str = string_reference.getStringConstant();
		StringVariable v = new StringVariable(var_name_str, str, str, str);
		StringConstant c = new StringConstant(str);

		return new StringConstraint(v, Comparator.EQ, c);
	}

	private RealConstraint buildNewSymbolicVariableDefinition(
			JvmVariable fresh_var, FloatLiteral valueOf) {
		String var_name_str;
		if (this.concolicState.isMarked(fresh_var)) {
			var_name_str = this.concolicState.getSymbolicName(fresh_var);
		} else {
			var_name_str = fresh_var.getName();
		}
		RealVariable v = new RealVariable(var_name_str, valueOf.getValue(),
				Float.MIN_VALUE, Float.MAX_VALUE);
		RealConstant c = new RealConstant(valueOf.getValue());

		return new RealConstraint(v, Comparator.EQ, c);
	}

	private RealConstraint buildNewSymbolicVariableDefinition(
			JvmVariable fresh_var, DoubleLiteral valueOf) {
		String var_name_str;
		if (this.concolicState.isMarked(fresh_var)) {
			var_name_str = this.concolicState.getSymbolicName(fresh_var);
		} else {
			var_name_str = fresh_var.getName();
		}
		RealVariable v = new RealVariable(var_name_str, valueOf.getValue(),
				Double.MIN_VALUE, Double.MAX_VALUE);
		RealConstant c = new RealConstant(valueOf.getValue());

		return new RealConstraint(v, Comparator.EQ, c);
	}

	private IntegerConstraint buildNewSymbolicVariableDefinition(
			JvmVariable fresh_var, LiteralBitVector32 valueOf) {
		String var_name_str;
		if (this.concolicState.isMarked(fresh_var)) {
			var_name_str = this.concolicState.getSymbolicName(fresh_var);
		} else {
			var_name_str = fresh_var.getName();
		}
		IntegerVariable v = new IntegerVariable(var_name_str,
				valueOf.getValue(), Integer.MIN_VALUE, Integer.MAX_VALUE);
		IntegerConstant c = new IntegerConstant(valueOf.getValue());

		return new IntegerConstraint(v, Comparator.EQ, c);
	}

	private IntegerConstraint buildNewSymbolicVariableDefinition(
			JvmVariable fresh_var, LiteralBitVector64 valueOf) {

		String var_name_str;
		if (this.concolicState.isMarked(fresh_var)) {
			var_name_str = this.concolicState.getSymbolicName(fresh_var);
		} else {
			var_name_str = fresh_var.getName();
		}
		long concreteValue = valueOf.getValue();
		IntegerVariable v = new IntegerVariable(var_name_str, concreteValue,
				Long.MIN_VALUE, Long.MAX_VALUE);
		IntegerConstant c = new IntegerConstant(concreteValue);

		return new IntegerConstraint(v, Comparator.EQ, c);
	}

	/**
	 * Given a fresh_map_var=update(map_var,index_expr,value_expr) stores
	 * fresh_map_var=>eval(map_var)++(eval(index_expr)->eval(value_expr))
	 */
	@Override
	public Object visit(UpdateConstraint c) {

		edu.uta.cse.dsc.ast.Expression fresh_var_expr = c.getVar();
		edu.uta.cse.dsc.ast.Expression map_expr = c.getMap();
		edu.uta.cse.dsc.ast.Expression index_expr = c.getIndex();
		edu.uta.cse.dsc.ast.Expression value_expr = c.getValue();

		Z3ArrayVariable<?, ?> fresh_map_variable = (Z3ArrayVariable<?, ?>) fresh_var_expr;

		JvmExpression value_jvm_expr = (JvmExpression) value_expr;
		JvmExpression index_jvm_expr = (JvmExpression) index_expr;

		JvmExpression symbolic_value = evaluateSymbolic(value_jvm_expr);
		JvmExpression concrete_value;
		if (!symbolic_value.containsJvmVariable()) {
			concrete_value = symbolic_value;
		} else {
			concrete_value = evaluateConcrete(value_jvm_expr);
		}

		JvmExpression eval_index = evaluateConcrete(index_jvm_expr);

		if (eval_index.containsJvmVariable()) {
			System.out.println("index contains var!");
		}

		if (map_expr.getClass().equals(Z3ArrayVariable.class)) {

			// existing mapping
			Z3ArrayVariable<?, ?> map_variable = (Z3ArrayVariable<?, ?>) map_expr;

			concolicState.updateExistingMapping(fresh_map_variable,
					map_variable, eval_index, symbolic_value, concrete_value);

		} else if (map_expr.getClass().equals(JavaFieldVariable.class)) {

			// new field
			Reference reference_index = (Reference) eval_index;
			JavaFieldVariable javaFieldVariable = (JavaFieldVariable) map_expr;
			concolicState.createNewFieldMapping(fresh_map_variable,
					javaFieldVariable, reference_index, symbolic_value,
					concrete_value);

		} else if (map_expr.getClass().equals(Z3ArrayLiteral.class)) {

			// new array
			BitVector32 int_index = (BitVector32) eval_index;
			Z3ArrayLiteral<?, ?> arrayLiteral = (Z3ArrayLiteral<?, ?>) map_expr;
			concolicState.createNewArrayMapping(fresh_map_variable,
					arrayLiteral, int_index, symbolic_value, concrete_value);

		} else {

			throw new IllegalStateException(
					"Cannot handle update for map argument of type "
							+ map_expr.getClass().getName());
		}

		// no recursive visit
		return null;
	}

	@Override
	public Object visit(EqConstraint c) {
		edu.uta.cse.dsc.ast.Expression left_expr = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right_expr = c.getRight();
		Comparator comp = Comparator.EQ;

		if (left_expr instanceof JvmVariable) {
			JvmVariable left_variable = (JvmVariable) left_expr;
			if (!this.concolicState.isAlreadyDefined(left_variable)) {
				JvmExpression right_jvm_expr = (JvmExpression) right_expr;
				JvmExpression symbolic_eval_right = evaluateSymbolic(right_jvm_expr);
				JvmExpression concrete_eval_right;
				if (!symbolic_eval_right.containsJvmVariable()) {
					concrete_eval_right = symbolic_eval_right;
				} else {
					concrete_eval_right = evaluateConcrete(right_jvm_expr);
				}

				this.concolicState
						.declareNewSymbolicVariable(left_variable,
								symbolic_eval_right, concrete_eval_right);

			}
		}

		if (isIntegerOrLong(left_expr) && isIntegerOrLong(right_expr)) {
			return buildNewIntegerConstraint(left_expr, comp, right_expr);
		} else if (isFloatOrDouble(left_expr) && isFloatOrDouble(right_expr)) {
			return buildNewRealConstraint(left_expr, comp, right_expr);
		} else if (isObject(left_expr) && isObject(right_expr)) {
			return null;
		} else {
			throw new IllegalArgumentException(
					"Cannot handle comparison between "
							+ left_expr.getClass().getName() + " and "
							+ right_expr.getClass().getName());
		}
	}

	private Object buildNewRealConstraint(
			edu.uta.cse.dsc.ast.Expression left_expr, Comparator comp,
			edu.uta.cse.dsc.ast.Expression right_expr) {
		RealExpression left_integer_expression = translateToRealExpr(left_expr);
		RealExpression right_integer_expression = translateToRealExpr(right_expr);

		return new RealConstraint(left_integer_expression, comp,
				right_integer_expression);
	}

	private Object buildNewIntegerConstraint(
			edu.uta.cse.dsc.ast.Expression left_expr, Comparator comp,
			edu.uta.cse.dsc.ast.Expression right_expr) {
		IntegerExpression left_integer_expression = translateToIntegerExpr(left_expr);
		IntegerExpression right_integer_expression = translateToIntegerExpr(right_expr);

		return new IntegerConstraint(left_integer_expression, comp,
				right_integer_expression);
	}

	private RealExpression translateToRealExpr(edu.uta.cse.dsc.ast.Expression e) {
		if (!isFloatOrDouble(e)) {
			throw new IllegalArgumentException(e.toString()
					+ " is not a valid real expression");
		}

		if (e instanceof FloatExpression) {
			FloatExpression fp32 = (FloatExpression) e;
			Object e_visit = fp32.accept(expressionTranslator);
			RealExpression realExpr = (RealExpression) e_visit;
			return realExpr;
		} else if (e instanceof BitVector64) {
			DoubleExpression fp64 = (DoubleExpression) e;
			Object e_visit = fp64.accept(expressionTranslator);
			RealExpression realExpr = (RealExpression) e_visit;
			return realExpr;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private boolean isObject(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof Reference;
	}

	@Override
	public Object visit(AndConstraint andConstraint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visit(LeqConstraint c) {
		edu.uta.cse.dsc.ast.Expression left = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right = c.getRight();
		Comparator comp = Comparator.LE;
		return visitNumericComparison(left, comp, right);
	}

	private Object visitNumericComparison(
			edu.uta.cse.dsc.ast.Expression left_expr, Comparator comp,
			edu.uta.cse.dsc.ast.Expression right_expr) {

		if (isIntegerOrLong(left_expr) && isIntegerOrLong(right_expr)) {
			return buildNewIntegerConstraint(left_expr, comp, right_expr);
		} else if (isFloatOrDouble(left_expr) && isFloatOrDouble(right_expr)) {
			return buildNewRealConstraint(left_expr, comp, right_expr);
		} else {
			throw new IllegalArgumentException(
					"Cannot handle comparison between "
							+ left_expr.getClass().getName() + " and "
							+ right_expr.getClass().getName());
		}
	}

	private boolean isIntegerOrLong(edu.uta.cse.dsc.ast.Expression e) {
		return (isInteger(e) || isLong(e));
	}

	private boolean isLong(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof BitVector64;
	}

	private boolean isInteger(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof BitVector32;
	}

	@Override
	public Object visit(LessThanConstraint c) {
		edu.uta.cse.dsc.ast.Expression left = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right = c.getRight();
		Comparator comp = Comparator.LT;
		return visitNumericComparison(left, comp, right);
	}

	private boolean isFloatOrDouble(edu.uta.cse.dsc.ast.Expression e) {
		return (isFloat(e) || isDouble(e));

	}

	private boolean isFloat(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof FloatLiteral;
	}

	private boolean isDouble(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof DoubleLiteral;
	}

	@Override
	public Object visit(NotConstraint c) {
		Object ret_val = c.getParam().accept(this);
		if (ret_val != null) {
			Constraint<?> constraint = (Constraint<?>) ret_val;
			Comparator comp = constraint.getComparator();
			Comparator not_comp = comp.not();

			if (constraint instanceof StringConstraint) {

				StringConstraint str_constraint = (StringConstraint) constraint;
				Expression<String> left = str_constraint.getLeftOperand();
				Expression<String> right = str_constraint.getRightOperand();
				StringExpression left_str_expr = (StringExpression) left;
				StringExpression right_str_expr = (StringExpression) right;
				return new StringConstraint(left_str_expr, not_comp,
						right_str_expr);
			} else if (constraint instanceof IntegerConstraint) {

				IntegerConstraint int_constraint = (IntegerConstraint) constraint;
				Expression<?> left = int_constraint.getLeftOperand();
				Expression<?> right = int_constraint.getRightOperand();
				return new IntegerConstraint(left, not_comp, right);

			} else if (constraint instanceof RealConstraint) {

				RealConstraint int_constraint = (RealConstraint) constraint;
				Expression<Double> left = int_constraint.getLeftOperand();
				Expression<Double> right = int_constraint.getRightOperand();
				return new RealConstraint(left, not_comp, right);

			} else {
				throw new RuntimeException("Unknown constraint class "
						+ constraint.getClass().getName());
			}

		} else
			return null;
	}

	@Override
	public Object visit(OrConstraint orConstraint) {
		return super.visit(orConstraint);
	}

	public void clear() {
		concolicState.clear();
	}

	private JvmExpression evaluateConcrete(JvmExpression e) {
		ConcreteEvaluator concreteEvaluator = new ConcreteEvaluator(
				this.concolicState);
		if (e instanceof BitVector32) {
			BitVector32 bv32 = (BitVector32) e;
			BitVector32 eval_bv32 = (BitVector32) bv32
					.accept(concreteEvaluator);
			return eval_bv32;
		} else if (e instanceof BitVector64) {
			BitVector64 bv64 = (BitVector64) e;
			BitVector64 eval_bv64 = (BitVector64) bv64
					.accept(concreteEvaluator);
			return eval_bv64;
		} else if (e instanceof FloatExpression) {
			FloatExpression fp32 = (FloatExpression) e;
			FloatExpression eval_fp32 = (FloatExpression) fp32
					.accept(concreteEvaluator);
			return eval_fp32;
		} else if (e instanceof DoubleExpression) {
			DoubleExpression fp64 = (DoubleExpression) e;
			DoubleExpression eval_fp64 = (DoubleExpression) fp64
					.accept(concreteEvaluator);
			return eval_fp64;
		} else if (e instanceof Reference) {
			Reference ref = (Reference) e;
			Reference eval_ref = (Reference) ref.accept(concreteEvaluator);
			return eval_ref;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private JvmExpression evaluateSymbolic(JvmExpression e) {

		SymbolicEvaluator symbolicEvaluator = new SymbolicEvaluator(
				this.concolicState);
		if (e instanceof BitVector32) {
			BitVector32 bv32 = (BitVector32) e;
			BitVector32 eval_bv32 = (BitVector32) bv32
					.accept(symbolicEvaluator);
			return eval_bv32;
		} else if (e instanceof BitVector64) {
			BitVector64 bv64 = (BitVector64) e;
			BitVector64 eval_bv64 = (BitVector64) bv64
					.accept(symbolicEvaluator);
			return eval_bv64;
		} else if (e instanceof FloatExpression) {
			FloatExpression fp32 = (FloatExpression) e;
			FloatExpression eval_fp32 = (FloatExpression) fp32
					.accept(symbolicEvaluator);
			return eval_fp32;
		} else if (e instanceof DoubleExpression) {
			DoubleExpression fp64 = (DoubleExpression) e;
			DoubleExpression eval_fp64 = (DoubleExpression) fp64
					.accept(symbolicEvaluator);
			return eval_fp64;
		} else if (e instanceof Reference) {
			Reference ref = (Reference) e;
			Reference eval_ref = (Reference) ref.accept(symbolicEvaluator);
			return eval_ref;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
