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
import edu.uta.cse.dsc.ast.reference.LiteralReference;
import edu.uta.cse.dsc.ast.reference.ReferenceVariable;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;
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

public class ConstraintNodeTranslator extends ConstraintNodeVisitor {

	private final SymbolicExecState symbolicExecState;

	private final JvmExpressionTranslator expressionTranslator;

	public ConstraintNodeTranslator(Map<JvmVariable, String> symbolicVariables) {
		this.symbolicExecState = new SymbolicExecState(symbolicVariables);
		this.expressionTranslator = new JvmExpressionTranslator(
				symbolicExecState);
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

		LiteralReference index_literal_reference = lookUpReference((Reference) index_expr);
		LiteralNonNullReference index_literal = (LiteralNonNullReference) index_literal_reference;

		boolean hasConcolicMarker = this.symbolicExecState.isMarked(fresh_var);

		if (symbolicExecState.isSymbolicIntMapping(map_var)) {
			// int case
			BitVector32 bitVector32 = symbolicExecState.getSymbolicIntValue(
					map_var, index_literal);
			this.symbolicExecState.declareNewSymbolicVariable(fresh_var,
					bitVector32);

			if (hasConcolicMarker) {
				LiteralBitVector32 literalBitVector32 = (LiteralBitVector32) bitVector32;
				return buildNewSymbolicVariableDefinition(fresh_var,
						literalBitVector32);
			}

		} else if (symbolicExecState.isSymbolicLongMapping(map_var)) {
			// long case
			BitVector64 bitVector64 = symbolicExecState.getSymbolicLongValue(
					map_var, index_literal);
			this.symbolicExecState.declareNewSymbolicVariable(fresh_var,
					bitVector64);

			if (hasConcolicMarker) {
				LiteralBitVector64 literalBitVector64 = (LiteralBitVector64) bitVector64;
				return buildNewSymbolicVariableDefinition(fresh_var,
						literalBitVector64);
			}

		} else if (symbolicExecState.isSymbolicRefMapping(map_var)) {
			// object case
			LiteralReference literalReference = symbolicExecState
					.getSymbolicRefValue(map_var, index_literal);
			this.symbolicExecState.declareNewSymbolicVariable(fresh_var,
					literalReference);

			if (hasConcolicMarker) {
				// treat reference as String (no concolic marker for Object type)
				LiteralNonNullReference string_reference = (LiteralNonNullReference) literalReference;
				return buildNewSmbolicVariableDefinition(fresh_var,
						string_reference);
			}
		} else if (symbolicExecState.isSymbolicFloatMapping(map_var)) {
			// float
			FloatExpression floatExpression = symbolicExecState
					.getSymbolicFloatValue(map_var, index_literal);
			this.symbolicExecState.declareNewSymbolicVariable(fresh_var,
					floatExpression);

			if (hasConcolicMarker) {
				FloatLiteral floatLiteral = (FloatLiteral) floatExpression;
				return buildNewSymbolicVariableDefinition(fresh_var,
						floatLiteral);
			}

		} else if (symbolicExecState.isSymbolicDoubleMapping(map_var)) {
			// double
			DoubleExpression doubleExpression = symbolicExecState
					.getSymbolicDoubleValue(map_var, index_literal);
			this.symbolicExecState.declareNewSymbolicVariable(fresh_var,
					doubleExpression);

			if (hasConcolicMarker) {
				DoubleLiteral doubleLiteral = (DoubleLiteral) doubleExpression;
				return buildNewSymbolicVariableDefinition(fresh_var,
						doubleLiteral);
			}

		}

		else {
			throw new IllegalStateException("Unknown mapping: "
					+ map_var.getName());
		}

		return null;
	}

	private StringConstraint buildNewSmbolicVariableDefinition(JvmVariable fresh_var,
			LiteralNonNullReference string_reference) {
		String var_name_str;
		if (this.symbolicExecState.isMarked(fresh_var)) {
			var_name_str = this.symbolicExecState.getSymbolicName(fresh_var);
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
		if (this.symbolicExecState.isMarked(fresh_var)) {
			var_name_str = this.symbolicExecState.getSymbolicName(fresh_var);
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
		if (this.symbolicExecState.isMarked(fresh_var)) {
			var_name_str = this.symbolicExecState.getSymbolicName(fresh_var);
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
		if (this.symbolicExecState.isMarked(fresh_var)) {
			var_name_str = this.symbolicExecState.getSymbolicName(fresh_var);
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
		if (this.symbolicExecState.isMarked(fresh_var)) {
			var_name_str = this.symbolicExecState.getSymbolicName(fresh_var);
		} else {
			var_name_str = fresh_var.getName();
		}
		long concreteValue = valueOf.getValue();
		IntegerVariable v = new IntegerVariable(var_name_str, concreteValue,
				Long.MIN_VALUE, Long.MAX_VALUE);
		IntegerConstant c = new IntegerConstant(concreteValue);

		return new IntegerConstraint(v, Comparator.EQ, c);
	}

	private LiteralReference lookUpReference(Reference reference) {
		if (reference instanceof LiteralReference) {
			LiteralReference literalReference = (LiteralReference) reference;
			return literalReference;
		} else if (reference instanceof ReferenceVariable) {
			return symbolicExecState.getSymbolicRefValue(reference);
		} else
			throw new IllegalArgumentException(reference.getClass().getName()
					+ " is not a supported type of Reference");
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

		LiteralReference index_literal_reference = lookUpReference((Reference) index_expr);
		LiteralNonNullReference index_literal_non_null_reference = (LiteralNonNullReference) index_literal_reference;

		if (map_expr instanceof JavaFieldVariable) {
			// create empty mapping case
			JavaFieldVariable javaFieldVariable = (JavaFieldVariable) map_expr;
			symbolicExecState.declareNewSymbolicMapping(fresh_map_variable,
					javaFieldVariable);

		} else if (map_expr instanceof Z3ArrayVariable<?, ?>) {
			// update existing mapping case
			Z3ArrayVariable<?, ?> map_variable = (Z3ArrayVariable<?, ?>) map_expr;
			symbolicExecState.declareNewSymbolicMapping(fresh_map_variable,
					map_variable);

		} else {
			throw new IllegalArgumentException("Implement this:"
					+ map_expr.getClass().getName());
		}

		if (value_expr instanceof BitVector32) {
			// int case
			BitVector32 bitVector32 = (BitVector32) value_expr;
			symbolicExecState.updateSymbolicMapping(fresh_map_variable,
					index_literal_non_null_reference, bitVector32);

		} else if (value_expr instanceof BitVector64) {
			// long case
			BitVector64 bitVector64 = (BitVector64) value_expr;
			symbolicExecState.updateSymbolicMapping(fresh_map_variable,
					index_literal_non_null_reference, bitVector64);

		} else if (value_expr instanceof Reference) {
			// Object case
			Reference reference = (Reference) value_expr;
			LiteralReference literalReference = lookUpReference(reference);
			symbolicExecState.updateSymbolicMapping(fresh_map_variable,
					index_literal_non_null_reference, literalReference);

		} else if (value_expr instanceof FloatExpression) {
			// float case
			FloatExpression floatExpression = (FloatExpression) value_expr;
			symbolicExecState.updateSymbolicMapping(fresh_map_variable,
					index_literal_non_null_reference, floatExpression);

		} else if (value_expr instanceof DoubleExpression) {
			// double case
			DoubleExpression doubleExpression = (DoubleExpression) value_expr;
			symbolicExecState.updateSymbolicMapping(fresh_map_variable,
					index_literal_non_null_reference, doubleExpression);

		} else {
			// unknown case
			throw new IllegalStateException(" update case for "
					+ value_expr.getClass().getName() + " is not implemented!");
		}

		// no recursive visit
		return null;
	}

	@Override
	public Object visit(EqConstraint c) {
		edu.uta.cse.dsc.ast.Expression left_expr = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right_expr = c.getRight();
		Comparator comp = Comparator.EQ;

		if (isIntegerOrLong(left_expr) && isIntegerOrLong(right_expr)) {
			if (left_expr instanceof BitVector32Variable) {
				BitVector32Variable bitVector32Variable = (BitVector32Variable) left_expr;

				if (!this.symbolicExecState
						.isAlreadyDefined(bitVector32Variable)) {
					BitVector32 symbolic_value = (BitVector32) right_expr;
					this.symbolicExecState.declareNewSymbolicVariable(
							bitVector32Variable, symbolic_value);
				}
			} else if (left_expr instanceof BitVector64Variable) {
				BitVector64Variable bitVector64Variable = (BitVector64Variable) left_expr;
				if (!this.symbolicExecState
						.isAlreadyDefined(bitVector64Variable)) {
					BitVector64 symbolic_value = (BitVector64) right_expr;
					this.symbolicExecState.declareNewSymbolicVariable(
							bitVector64Variable, symbolic_value);
				}

			} else if (left_expr instanceof FloatVariable) {
				FloatVariable floatVariable = (FloatVariable) left_expr;
				if (!this.symbolicExecState.isAlreadyDefined(floatVariable)) {
					FloatExpression symbolic_value = (FloatExpression) right_expr;
					this.symbolicExecState.declareNewSymbolicVariable(
							floatVariable, symbolic_value);
				}

			} else if (left_expr instanceof DoubleVariable) {
				DoubleVariable doubleVariable = (DoubleVariable) left_expr;
				if (!this.symbolicExecState.isAlreadyDefined(doubleVariable)) {
					FloatExpression symbolic_value = (FloatExpression) right_expr;
					this.symbolicExecState.declareNewSymbolicVariable(
							doubleVariable, symbolic_value);
				}
			}

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
		throw new UnsupportedOperationException("Not yet implemented");
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
		symbolicExecState.clear();
	}

}
