package org.evosuite.symbolic.vm;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerComparison;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealComparison;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringComparison;

/**
 * 
 * @author galeotti
 * 
 */
public final class PathConstraint {

	private BranchCondition previousBranchCondition = null;

	private final Stack<BranchCondition> branchConditions = new Stack<BranchCondition>();

	private final LinkedList<Constraint<?>> currentSupportingConstraints = new LinkedList<Constraint<?>>();

	private Constraint<?> normalizeConstraint(IntegerConstraint c) {
		if (c.getLeftOperand() instanceof StringComparison
		        || c.getRightOperand() instanceof StringComparison)
			return removeStringComparison(c);
		else
			return removeCMPFormConstraint(c);
	}

	public void pushSupportingConstraint(IntegerConstraint c) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);
		currentSupportingConstraints.add(normalizedConstraint);

	}

	public void pushBranchCondition(String className, String methName, int branchIndex,
	        IntegerConstraint c) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);

		LinkedList<Constraint<?>> branch_supporting_constraints = new LinkedList<Constraint<?>>(
		        currentSupportingConstraints);

		BranchCondition new_branch = new BranchCondition(previousBranchCondition,
		        className, methName, branchIndex, normalizedConstraint,
		        branch_supporting_constraints);

		previousBranchCondition = new_branch;

		branchConditions.push(new_branch);

		currentSupportingConstraints.clear();
	}

	public List<BranchCondition> getBranchConditions() {
		return new LinkedList<BranchCondition>(branchConditions);
	}

	private IntegerConstraint createNormalizedIntegerConstraint(IntegerExpression left,
	        Comparator comp, IntegerExpression right) {
		IntegerConstant integerConstant = (IntegerConstant) right;
		StringComparison stringComparison = (StringComparison) left;

		IntegerConstraint c = new IntegerConstraint(stringComparison, comp,
		        integerConstant);
		return c;

	}

	private static boolean isStringConstraint(IntegerExpression left, Comparator comp,
	        IntegerExpression right) {

		return ((comp.equals(Comparator.NE) || comp.equals(Comparator.EQ))
		        && (left instanceof StringComparison) && (right instanceof IntegerConstant));

	}

	private Constraint<?> removeStringComparison(IntegerConstraint c) {
		IntegerExpression left = (IntegerExpression) c.getLeftOperand();
		Comparator comp = c.getComparator();
		IntegerExpression right = (IntegerExpression) c.getRightOperand();

		if (isStringConstraint(left, comp, right))
			return createNormalizedIntegerConstraint(left, comp, right);

		else if (isStringConstraint(right, comp, left))
			return createNormalizedIntegerConstraint(right, comp, left);

		return c;
	}

	private Constraint<?> removeCMPFormConstraint(IntegerConstraint c) {

		if (c.getLeftOperand() instanceof IntegerComparison) {
			IntegerComparison cmp = (IntegerComparison) c.getLeftOperand();
			int value = ((Number) c.getRightOperand().getConcreteValue()).intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		} else if (c.getLeftOperand() instanceof RealComparison) {
			RealComparison cmp = (RealComparison) c.getLeftOperand();

			int value = ((Number) c.getRightOperand().getConcreteValue()).intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		} else if (c.getRightOperand() instanceof IntegerComparison) {
			IntegerComparison cmp = (IntegerComparison) c.getRightOperand();
			int value = ((Number) c.getLeftOperand().getConcreteValue()).intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		} else if (c.getRightOperand() instanceof RealComparison) {
			RealComparison cmp = (RealComparison) c.getRightOperand();

			int value = ((Number) c.getLeftOperand().getConcreteValue()).intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		}
		return (c);
	}

}
