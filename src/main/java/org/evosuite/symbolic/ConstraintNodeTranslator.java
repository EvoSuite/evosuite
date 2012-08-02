package org.evosuite.symbolic;

import java.util.Map;
import java.util.Vector;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.StringToIntCast;
import org.objectweb.asm.Type;

import edu.uta.cse.dsc.MainConfig;
import edu.uta.cse.dsc.ast.ArrayReference;
import edu.uta.cse.dsc.ast.BitVector32;
import edu.uta.cse.dsc.ast.BitVector64;
import edu.uta.cse.dsc.ast.BoundVariable;
import edu.uta.cse.dsc.ast.DoubleExpression;
import edu.uta.cse.dsc.ast.FloatExpression;
import edu.uta.cse.dsc.ast.JvmExpression;
import edu.uta.cse.dsc.ast.JvmVariable;
import edu.uta.cse.dsc.ast.Reference;
import edu.uta.cse.dsc.ast.fp.DoubleLiteral;
import edu.uta.cse.dsc.ast.fp.FloatLiteral;
import edu.uta.cse.dsc.ast.z3array.DumpingZ3ArrayFactory;
import edu.uta.cse.dsc.ast.z3array.JavaArrayVariable;
import edu.uta.cse.dsc.ast.z3array.JavaFieldVariable;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayLiteral;
import edu.uta.cse.dsc.ast.z3array.Z3ArrayVariable;
import edu.uta.cse.dsc.pcdump.ast.AndConstraint;
import edu.uta.cse.dsc.pcdump.ast.ConstraintNodeVisitor;
import edu.uta.cse.dsc.pcdump.ast.EqConstraint;
import edu.uta.cse.dsc.pcdump.ast.ForEachConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsAbstractConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsArrayConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsFinalConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsInterfaceConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsPublicConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsSubTypeOfConstraint;
import edu.uta.cse.dsc.pcdump.ast.IsSuperTypeConstraint;
import edu.uta.cse.dsc.pcdump.ast.LeqConstraint;
import edu.uta.cse.dsc.pcdump.ast.LessThanConstraint;
import edu.uta.cse.dsc.pcdump.ast.NotConstraint;
import edu.uta.cse.dsc.pcdump.ast.OrConstraint;
import edu.uta.cse.dsc.pcdump.ast.SelectArrayConstraint;
import edu.uta.cse.dsc.pcdump.ast.SelectFieldConstraint;
import edu.uta.cse.dsc.pcdump.ast.UpdateArrayConstraint;
import edu.uta.cse.dsc.pcdump.ast.UpdateFieldConstraint;

public final class ConstraintNodeTranslator implements ConstraintNodeVisitor {

	private static Vector<Constraint<?>> buildEmptyVector() {
		return new Vector<Constraint<?>>();
	}

	private final ConcolicState concolicState;

	private final JvmExpressionTranslator expressionTranslator;

	public ConstraintNodeTranslator(Map<JvmVariable, String> symbolicVariables) {
		this.concolicState = new ConcolicState(symbolicVariables);
		this.expressionTranslator = new JvmExpressionTranslator(concolicState);
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
	 * This constraint is only meant to update the concolic state. The
	 * translation is the empty vector.
	 * 
	 * Axiomatic constraints are ignored and have no effect on the concolic
	 * state.
	 * 
	 */
	@Override
	public Object visit(SelectFieldConstraint c) {

		// get constraint components
		edu.uta.cse.dsc.ast.Expression fresh_var_expr = c.getVar();
		edu.uta.cse.dsc.ast.Expression map_expr = c.getMap();
		edu.uta.cse.dsc.ast.Expression index_expr = c.getIndex();

		if (index_expr.toString().startsWith("#var")) {
			// ignore input parameters
			return buildEmptyVector();

		}

		if (index_expr instanceof BoundVariable.Ref) {
			// ignore axiomatic constraints
			return buildEmptyVector();
		}

		JvmVariable fresh_var = (JvmVariable) fresh_var_expr;
		Z3ArrayVariable<?, ?> map_var = (Z3ArrayVariable<?, ?>) map_expr;
		JvmExpression index_jvm_expr = (JvmExpression) index_expr;

		JvmExpression evalIndex = evaluateConcrete(index_jvm_expr);
		JvmExpression symbolicValue;
		JvmExpression concreteValue;

		// field access

		symbolicValue = this.concolicState.getSymbolicValue(map_var, evalIndex);
		concreteValue = this.concolicState.getConcreteValue(map_var, evalIndex);

		this.concolicState.updateJvmVariable(fresh_var, symbolicValue,
				concreteValue);

		return buildEmptyVector();
	}

	/**
	 * This constraint is only meant to update the concolic state. The
	 * translation is the empty vector.
	 * 
	 */
	@Override
	public Object visit(UpdateFieldConstraint c) {

		edu.uta.cse.dsc.ast.Expression fresh_var_expr = c.getVar();
		edu.uta.cse.dsc.ast.Expression map_expr = c.getMap();
		edu.uta.cse.dsc.ast.Expression index_expr = c.getIndex();
		edu.uta.cse.dsc.ast.Expression value_expr = c.getValue();

		Z3ArrayVariable<?, ?> fresh_map_variable = (Z3ArrayVariable<?, ?>) fresh_var_expr;

		JvmExpression value_jvm_expr = (JvmExpression) value_expr;
		JvmExpression index_jvm_expr = (JvmExpression) index_expr;

		JvmExpression symbolic_value = evaluateSymbolic(value_jvm_expr);
		JvmExpression concrete_value = evaluateConcrete(value_jvm_expr);
		JvmExpression eval_index = evaluateConcrete(index_jvm_expr);

		if (map_expr.getClass().equals(JavaFieldVariable.class)) {

			// new field
			Reference reference_index = (Reference) eval_index;
			JavaFieldVariable javaFieldVariable = (JavaFieldVariable) map_expr;
			concolicState.updateJavaFieldVariable(fresh_map_variable,
					javaFieldVariable, reference_index, symbolic_value,
					concrete_value);

		} else if (map_expr.getClass().equals(Z3ArrayVariable.class)) {

			if (map_expr.equals(DumpingZ3ArrayFactory.EMPTY_ARRAY_LENGTHS)) {

				// array lengths
				Reference arrayRef = (Reference) eval_index;
				concolicState.updateJavaFieldVariable(fresh_map_variable, null,
						arrayRef, symbolic_value, concrete_value);

			} else {

				// existing mapping
				Z3ArrayVariable<?, ?> map_variable = (Z3ArrayVariable<?, ?>) map_expr;
				// field access
				concolicState.updateZ3ArrayVariable(fresh_map_variable,
						map_variable, eval_index, symbolic_value,
						concrete_value);
			}
		} else {

			throw new IllegalStateException(
					"Cannot handle update for map argument of type "
							+ map_expr.getClass().getName());
		}

		// no recursive visit
		return buildEmptyVector();
	}

	/**
	 * Only translates those equality constraints where:
	 * <ul>
	 * <li>left,right are integers(BitVector32,BitVector64)</li>
	 * <li>left,right are real(FloatExpression,DoubleExpression)</li>
	 * </ul>
	 * Object comparisons are explicitly ignored.
	 * 
	 */
	@Override
	public Object visit(EqConstraint c) {
		edu.uta.cse.dsc.ast.Expression left_expr = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right_expr = c.getRight();
		Comparator comp = Comparator.EQ;

		if ((left_expr instanceof JvmVariable)
				&& (!this.concolicState
						.containsVariable((JvmVariable) left_expr))) {
			declareNewVariable((JvmVariable) left_expr, right_expr);

			// this constraint is only meant for declaring a new concrete value
			// (it is not an actual constraint)
			return buildEmptyVector();
		}

		if (isIntegerOrLong(left_expr) && isIntegerOrLong(right_expr)) {

			return buildNewIntegerConstraint(left_expr, comp, right_expr);

		} else if (isFloatOrDouble(left_expr) && isFloatOrDouble(right_expr)) {

			return buildNewRealConstraint(left_expr, comp, right_expr);

		} else if (isObject(left_expr) && isObject(right_expr)) {

			// ignore this constraint
			return buildEmptyVector();

		} else {
			throw new IllegalArgumentException(
					"Cannot handle comparison between "
							+ left_expr.getClass().getName() + " and "
							+ right_expr.getClass().getName());
		}
	}

	private void declareNewVariable(JvmVariable left_variable,
			edu.uta.cse.dsc.ast.Expression right_expr) {
		JvmExpression right_jvm_expr = (JvmExpression) right_expr;
		JvmExpression symbolic_eval_right = evaluateSymbolic(right_jvm_expr);
		JvmExpression concrete_eval_right = evaluateConcrete(right_jvm_expr);

		this.concolicState.updateJvmVariable(left_variable,
				symbolic_eval_right, concrete_eval_right);

	}

	private Object buildNewRealConstraint(
			edu.uta.cse.dsc.ast.Expression left_expr, Comparator comp,
			edu.uta.cse.dsc.ast.Expression right_expr) {
		RealExpression left_integer_expression = translateToRealExpr(left_expr);
		RealExpression right_integer_expression = translateToRealExpr(right_expr);

		return new RealConstraint(left_integer_expression, comp,
				right_integer_expression);
	}

	private static Vector<Constraint<?>> buildSingletonVector(Constraint<?> c) {
		Vector<Constraint<?>> v = buildEmptyVector();
		v.add(c);
		return v;
	}

	private Vector<Constraint<?>> buildNewIntegerConstraint(
			edu.uta.cse.dsc.ast.Expression left_expr, Comparator comp,
			edu.uta.cse.dsc.ast.Expression right_expr) {
		IntegerExpression left_integer_expression = translateToIntegerExpr(left_expr);
		IntegerExpression right_integer_expression = translateToIntegerExpr(right_expr);

		if (isStringConstraint(left_integer_expression, comp,
				right_integer_expression)) {

			return createNormalizedIntegerConstraint(left_integer_expression,
					comp, right_integer_expression);
		} else

		if (isStringConstraint(right_integer_expression, comp,
				left_integer_expression)) {
			return createNormalizedIntegerConstraint(right_integer_expression,
					comp, left_integer_expression);

		} else {

			IntegerConstraint c = new IntegerConstraint(
					left_integer_expression, comp, right_integer_expression);
			return buildSingletonVector(c);
		}
	}

	private static boolean isStringConstraint(IntegerExpression left,
			Comparator comp, IntegerExpression right) {

		return ((comp.equals(Comparator.NE) || comp.equals(Comparator.EQ))
				&& (right instanceof IntegerConstant)
				&& (left instanceof StringToIntCast) && ((StringToIntCast) left)
					.getParam() instanceof StringComparison);

	}

	private Vector<Constraint<?>> createNormalizedIntegerConstraint(
			IntegerExpression left, Comparator comp, IntegerExpression right) {
		IntegerConstant integerConstant = (IntegerConstant) right;
		StringComparison stringComparison = (StringComparison) ((StringToIntCast) left)
				.getParam();

		IntegerConstraint c = new IntegerConstraint(stringComparison, comp,
				integerConstant);
		return buildSingletonVector(c);

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

	private static boolean isObject(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof Reference;
	}

	@Override
	public Object visit(LeqConstraint c) {
		edu.uta.cse.dsc.ast.Expression left = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right = c.getRight();
		Comparator comp = Comparator.LE;
		return translateNumericComparison(left, comp, right);
	}

	private Object translateNumericComparison(
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

	private static boolean isIntegerOrLong(edu.uta.cse.dsc.ast.Expression e) {
		return (isInteger(e) || isLong(e));
	}

	private static boolean isLong(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof BitVector64;
	}

	private static boolean isInteger(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof BitVector32;
	}

	@Override
	public Object visit(LessThanConstraint c) {
		edu.uta.cse.dsc.ast.Expression left = c.getLeft();
		edu.uta.cse.dsc.ast.Expression right = c.getRight();
		Comparator comp = Comparator.LT;
		return translateNumericComparison(left, comp, right);
	}

	private static boolean isFloatOrDouble(edu.uta.cse.dsc.ast.Expression e) {
		return (isFloat(e) || isDouble(e));

	}

	private static boolean isFloat(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof FloatLiteral;
	}

	private static boolean isDouble(edu.uta.cse.dsc.ast.Expression e) {
		return e instanceof DoubleLiteral;
	}

	/**
	 * Translates a NotConstraint. If the negated constraint is a conjunction,
	 * the result is ignored by returning the empty vector.
	 */
	@Override
	public Object visit(NotConstraint c) {

		Vector<Constraint<?>> ret_val = (Vector<Constraint<?>>) c.getParam()
				.accept(this);

		if (ret_val.size() == 1) {

			Constraint<?> constraint = (Constraint<?>) ret_val.get(0);
			Comparator comp = constraint.getComparator();
			Comparator not_comp = comp.not();

			if (constraint instanceof StringConstraint) {

				StringConstraint str_constraint = (StringConstraint) constraint;
				Expression<String> left = str_constraint.getLeftOperand();
				Expression<String> right = str_constraint.getRightOperand();
				StringExpression left_str_expr = (StringExpression) left;
				StringExpression right_str_expr = (StringExpression) right;
				StringConstraint ret_constraint = new StringConstraint(
						left_str_expr, not_comp, right_str_expr);

				return buildSingletonVector(ret_constraint);

			} else if (constraint instanceof IntegerConstraint) {

				IntegerConstraint int_constraint = (IntegerConstraint) constraint;
				Expression<?> left = int_constraint.getLeftOperand();
				Expression<?> right = int_constraint.getRightOperand();
				IntegerConstraint ret_constraint = new IntegerConstraint(left,
						not_comp, right);

				return buildSingletonVector(ret_constraint);

			} else if (constraint instanceof RealConstraint) {

				RealConstraint int_constraint = (RealConstraint) constraint;
				Expression<Double> left = int_constraint.getLeftOperand();
				Expression<Double> right = int_constraint.getRightOperand();
				RealConstraint ret_constraint = new RealConstraint(left,
						not_comp, right);

				return buildSingletonVector(ret_constraint);

			} else {
				throw new RuntimeException("Unknown constraint class "
						+ constraint.getClass().getName());
			}

		} else {

			return buildEmptyVector();
		}

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
			// arithmetic simplify
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

	/**
	 * Translates an AndConstraint into a the vector of left and right.
	 */
	@Override
	public Object visit(AndConstraint c) {
		Vector<Constraint<?>> left = (Vector<Constraint<?>>) c.getLeft()
				.accept(this);
		Vector<Constraint<?>> right = (Vector<Constraint<?>>) c.getRight()
				.accept(this);

		Vector<Constraint<?>> result = buildEmptyVector();
		result.addAll(left);
		result.addAll(right);

		return result;

	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object accept(ForEachConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsAbstractConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsArrayConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsFinalConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsInterfaceConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsPublicConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsSubTypeOfConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(IsSuperTypeConstraint c) {
		return buildEmptyVector();
	}

	/**
	 * This DSC constraint is ignored. The result of the translation is an empty
	 * Vector object.
	 */
	@Override
	public Object visit(OrConstraint c) {
		return buildEmptyVector();
	}

	@Override
	public Object visit(SelectArrayConstraint c) {

		ArrayReference<?> arrayRef = (ArrayReference<?>) evaluateConcrete(c
				.getArrayRef());
		JvmExpression index = evaluateConcrete(c.getIndex());
		Z3ArrayVariable<?, ?> map_var = (Z3ArrayVariable<?, ?>) c.getMap();

		// array access
		JvmExpression symbolicValue = this.concolicState.getSymbolicValue(
				map_var, arrayRef, index);
		JvmExpression concreteValue = this.concolicState.getConcreteValue(
				map_var, arrayRef, index);

		this.concolicState.updateJvmVariable(c.getVar(), symbolicValue,
				concreteValue);

		return buildEmptyVector();
	}

	@Override
	public Object visit(UpdateArrayConstraint c) {

		JvmExpression symbolic_value = evaluateSymbolic(c.getValue());
		JvmExpression concrete_value;
		if (!symbolic_value.containsJvmVariable()) {
			concrete_value = symbolic_value;
		} else {
			concrete_value = evaluateConcrete(c.getValue());
		}

		JvmExpression index = evaluateConcrete(c.getIndex());

		ArrayReference<?> arrayRef = (ArrayReference<?>) evaluateConcrete(c
				.getArrayRef());

		Z3ArrayVariable<?, ?> map = (Z3ArrayVariable<?, ?>) c.getMap();

		if (map.equals(DumpingZ3ArrayFactory.EMPTY_BV32_ARRAY_CONTENTS)) {
			// new int[], then update
			concolicState.updateArrayContents(c.getVar(), arrayRef, index,
					symbolic_value, concrete_value);
		} else if (map.equals(DumpingZ3ArrayFactory.EMPTY_BV64_ARRAY_CONTENTS)) {
			// new long[], then update
			concolicState.updateArrayContents(c.getVar(), arrayRef, index,
					symbolic_value, concrete_value);
		} else if (map.equals(DumpingZ3ArrayFactory.EMPTY_FP32_ARRAY_CONTENTS)) {
			// new float[], then update
			concolicState.updateArrayContents(c.getVar(), arrayRef, index,
					symbolic_value, concrete_value);
		} else if (map.equals(DumpingZ3ArrayFactory.EMPTY_FP64_ARRAY_CONTENTS)) {
			// new double[], then update
			concolicState.updateArrayContents(c.getVar(), arrayRef, index,
					symbolic_value, concrete_value);
		} else if (map.equals(DumpingZ3ArrayFactory.EMPTY_REF_ARRAY_CONTENTS)) {
			// new Object[], then update
			concolicState.updateArrayContents(c.getVar(), arrayRef, index,
					symbolic_value, concrete_value);
		} else {
			// array update
			concolicState.updateZ3ArrayVariable(c.getVar(), map, arrayRef,
					index, symbolic_value, concrete_value);
		}

		return buildEmptyVector();
	}

}
