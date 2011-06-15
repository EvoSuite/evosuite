/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.smt.smtlib;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.smtlib.IExpr;

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
public class SMTConverter {

	private static Logger logger = Logger.getLogger(SMTConverter.class);

	private final Set<IExpr.ISymbol> symbols = new HashSet<IExpr.ISymbol>();

	public Set<IExpr.ISymbol> getSymbols() {
		return symbols;
	}

	public IExpr visit(Constraint<?> constraint) {
		logger.info("Converting " + constraint);
		return visitConstraint(constraint.getComparator(), constraint.getLeftOperand(), constraint.getRightOperand());
	}

	private IExpr visit(BinaryExpression<?> expr) {
		switch (expr.getOperator()) {
		case PLUS:
			return SMTExpr.Plus(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case MINUS:
			return SMTExpr.Minus(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case MUL:
			return SMTExpr.Mul(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case DIV:
			return SMTExpr.Div(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case REM:
			throw new RuntimeException("Unsupported operator: " + expr.getOperator());

		case IAND:
			return SMTExpr.BvAnd(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case IOR:
			return SMTExpr.BvOr(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case SHR:
			return SMTExpr.BvShr(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case SHL:
			return SMTExpr.BvShl(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case IXOR:
			return SMTExpr.BvXor(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));

		case AND:
			return SMTExpr.And(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case OR:
			return SMTExpr.Or(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));
		case XOR:
			return SMTExpr.Xor(visit(expr.getLeftOperand()), visit(expr.getRightOperand()));

		default:
			throw new RuntimeException("Unsupported operator: " + expr.getOperator());
		}
	}

	private IExpr visit(Expression<?> expr) {
		if (expr instanceof IntegerConstant) {
			return visit((IntegerConstant) expr);
		} else if (expr instanceof RealConstant) {
			return visit((RealConstant) expr);
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

	private IExpr visit(IntegerConstant expr) {
		return SMTExpr.getNumeral(expr.getConcreteValue());
	}

	private IExpr visit(RealConstant expr) {
		return SMTExpr.getDecimal("" + expr.getConcreteValue()); // FIXXME
	}

	private IExpr visit(UnaryExpression<?> expr) {
		switch (expr.getOperator()) {
		case NEG:
			return SMTExpr.Neg(visit(expr.getOperand()));
		default:
			throw new RuntimeException("Unsupported operator: " + expr.getOperator());
		}
	}

	private IExpr visit(Variable<?> expr) {
		IExpr.ISymbol symbol = SMTExpr.getSymbol(expr.getName());
		symbols.add(symbol); // TODO: Distinguish between int and float types!
		return symbol;
	}

	private IExpr visitConstraint(Comparator comparator, Expression<?> left, Expression<?> right) {

		switch (comparator) {
		case EQ:
			return SMTExpr.Eq(visit(left), visit(right));
		case NE:
			return SMTExpr.Not(SMTExpr.Eq(visit(left), visit(right)));
		case GE:
			return SMTExpr.Gte(visit(left), visit(right));
		case GT:
			return SMTExpr.Gt(visit(left), visit(right));
		case LE:
			return SMTExpr.Lte(visit(left), visit(right));
		case LT:
			return SMTExpr.Lt(visit(left), visit(right));
		default:
			throw new RuntimeException("Unsupported comparison: " + comparator);
		}
	}
}
