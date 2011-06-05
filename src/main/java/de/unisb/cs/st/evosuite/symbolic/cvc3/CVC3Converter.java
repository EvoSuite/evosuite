/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.cvc3;

import java.util.Collection;

import org.apache.log4j.Logger;

import cvc3.Expr;
import cvc3.ValidityChecker;
import de.unisb.cs.st.evosuite.symbolic.expr.BinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerUnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.RealUnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.UnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Variable;

/**
 * @author Gordon Fraser
 * 
 */
public class CVC3Converter {
	private static Logger logger = Logger.getLogger(CVC3Converter.class);

	private final CVC3Expr cvc3;

	public CVC3Converter(ValidityChecker vc) {
		cvc3 = new CVC3Expr(vc);
	}

	private Expr visitConstraint(Comparator comparator, Expression<?> left,
	        Expression<?> right) {

		switch (comparator) {
		case EQ:
			return cvc3.Eq(visit(left), visit(right));
		case NE:
			return cvc3.Neq(visit(left), visit(right));
		case GE:
			return cvc3.Gte(visit(left), visit(right));
		case GT:
			return cvc3.Gt(visit(left), visit(right));
		case LE:
			return cvc3.Lte(visit(left), visit(right));
		case LT:
			return cvc3.Lt(visit(left), visit(right));
		default:
			throw new RuntimeException("Unsupported comparison: " + comparator);
		}
	}

	private Expr visit(Expression<?> expr) {
		if (expr instanceof IntegerConstant) {
			return visit((IntegerConstant) expr);
		} else if (expr instanceof RealConstant) {
			return visit(expr);
		} else if (expr instanceof IntegerVariable) {
			return visit((Variable<?>) expr);
		} else if (expr instanceof RealVariable) {
			return visit((Variable<?>) expr);
		} else if (expr instanceof IntegerUnaryExpression) {
			return visit((UnaryExpression<?>) expr);
		} else if (expr instanceof IntegerBinaryExpression) {
			return visit((BinaryExpression<?>) expr);
		} else if (expr instanceof RealUnaryExpression) {
			return visit((UnaryExpression<?>) expr);
		} else if (expr instanceof RealBinaryExpression) {
			return visit((BinaryExpression<?>) expr);
		}
		return null;
	}

	private Expr visit(Variable<?> expr) {
		Expr symbol = cvc3.getSymbol(expr.getName());
		//symbols.add(symbol); // TODO: Distinguish between int and float types!
		return symbol;
	}

	private Expr visit(IntegerConstant expr) {
		return cvc3.getNumeral(expr.getConcreteValue());
	}

	private Expr visit(UnaryExpression<?> expr) {
		switch (expr.getOperator()) {
		case NEG:
			return cvc3.Neg(visit(expr.getOperand()));
		default:
			throw new RuntimeException("Unsupported operator: " + expr.getOperator());
		}
	}

	private Expr visit(BinaryExpression<?> expr) {
		switch (expr.getOperator()) {
		case PLUS:
			return cvc3.Plus(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case MINUS:
			return cvc3.Minus(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case MUL:
			return cvc3.Mul(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case DIV:
			return cvc3.Div(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case REM:
			throw new RuntimeException("Unsupported operator: " + expr.getOperator());

		case IAND:
			return cvc3.BvAnd(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case IOR:
			return cvc3.BvOr(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case SHR:
			throw new RuntimeException("Shift not yet supported");
			//			return cvc3.BvShr(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case SHL:
			throw new RuntimeException("Shift not yet supported");
			//			return cvc3.BvShl(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case IXOR:
			return cvc3.BvXor(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));

		case AND:
			return cvc3.And(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case OR:
			return cvc3.Or(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case XOR:
			return cvc3.Xor(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));

		default:
			throw new RuntimeException("Unsupported operator: " + expr.getOperator());
		}
	}

	public Expr visit(Constraint<?> constraint) {
		logger.info("Converting " + constraint);
		return visitConstraint(constraint.getComparator(), constraint.getLeftOperand(),
		                       constraint.getRightOperand());
	}

	public Expr convert(Collection<Constraint> constraints) {
		Expr expr = null;
		for (Constraint c : constraints) {
			Expr e = visit(c);
			if (expr == null)
				expr = e;
			else
				expr = cvc3.And(expr, e);
		}
		return expr;
	}

}
