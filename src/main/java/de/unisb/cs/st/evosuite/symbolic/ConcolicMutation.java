/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.symbolic.expr.BinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.UnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Variable;
import de.unisb.cs.st.evosuite.symbolic.smt.cvc3.CVC3Solver;
import de.unisb.cs.st.evosuite.testcase.BooleanPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.BytePrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.CharPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.LongPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.ShortPrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class ConcolicMutation {

	protected static Logger logger = LoggerFactory.getLogger(ConcolicMutation.class);

	/**
	 * Generate new constraint and ask solver for solution
	 * 
	 * @param condition
	 * @param test
	 * @return
	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SuppressWarnings("unchecked")
	public static TestCase negateCondition(BranchCondition condition, TestCase test) {
		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		constraints.addAll(condition.reachingConstraints);
		//constraints.addAll(condition.localConstraints);
		Constraint<Long> c = (Constraint<Long>) condition.localConstraints.iterator().next();
		Constraint<Long> targetConstraint = new IntegerConstraint(c.getLeftOperand(),
		        c.getComparator().not(), c.getRightOperand());
		constraints.add(targetConstraint);

		if (!targetConstraint.isSolveable()) {
			logger.info("Found unsolvable constraint: " + targetConstraint);
			// TODO: This is usually the case when the same variable is used for several parameters of a method
			// Could we treat this as a special case?
			return null;
		}

		int size = constraints.size();
		if (size > 0) {
			constraints = reduce(constraints);
			//logger.info("Reduced constraints from " + size + " to " + constraints.size());
			//logger.info("Now solving: " + constraints);
		}

		CVC3Solver solver = new CVC3Solver();
		Map<String, Object> values = solver.getModel(constraints);

		if (values != null) {
			// logger.info(values.toString());
			TestCase newTest = test.clone();

			for (Object key : values.keySet()) {
				Object val = values.get(key);
				if (val != null) {
					if (val instanceof Long) {
						Long value = (Long) val;
						String name = ((String) key).replace("__SYM", "");
						logger.debug("New value for " + name + " is " + value);
						PrimitiveStatement p = getStatement(newTest, name);
						assert (p != null);
						if (p instanceof BooleanPrimitiveStatement)
							p.setValue(value.intValue() > 0);
						else if (p instanceof CharPrimitiveStatement)
							p.setValue((char) value.intValue());
						else if (p instanceof BytePrimitiveStatement)
							p.setValue((byte) value.intValue());
						else if (p instanceof ShortPrimitiveStatement)
							p.setValue((short) value.intValue());
						else if (p instanceof LongPrimitiveStatement)
							p.setValue(value);
						else
							p.setValue(value.intValue());
					} else {
						logger.debug("New value is not long " + val);
					}
				} else {
					logger.debug("New value is null");

				}
			}
			return newTest;
		} else {
			logger.debug("Got null :-(");
			return null;
		}
	}

	/**
	 * Get the statement that defines this variable
	 * 
	 * @param test
	 * @param name
	 * @return
	 */
	private static PrimitiveStatement<?> getStatement(TestCase test, String name) {
		for (StatementInterface statement : test) {
			if (statement instanceof PrimitiveStatement<?>) {
				if (statement.getReturnValue().getName().equals(name))
					return (PrimitiveStatement<?>) statement;
			}
		}
		return null;
	}

	/**
	 * Apply cone of influence reduction to constraints with respect to the last
	 * constraint in the list
	 * 
	 * @param constraints
	 * @return
	 */
	private static List<Constraint<?>> reduce(List<Constraint<?>> constraints) {

		Constraint<?> target = constraints.get(constraints.size() - 1);
		Set<Variable<?>> dependencies = getVariables(target);

		LinkedList<Constraint<?>> coi = new LinkedList<Constraint<?>>();
		coi.add(target);

		for (int i = constraints.size() - 2; i >= 0; i--) {
			Constraint<?> constraint = constraints.get(i);
			Set<Variable<?>> variables = getVariables(constraint);
			for (Variable<?> var : dependencies) {
				if (variables.contains(var)) {
					dependencies.addAll(variables);
					coi.addFirst(constraint);
					break;
				}
			}
		}
		return coi;
	}

	/**
	 * Determine the set of variable referenced by this constraint
	 * 
	 * @param constraint
	 * @return
	 */
	private static Set<Variable<?>> getVariables(Constraint<?> constraint) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		getVariables(constraint.getLeftOperand(), variables);
		getVariables(constraint.getRightOperand(), variables);
		return variables;
	}

	/**
	 * Recursively determine constraints in expression
	 * 
	 * @param expr
	 * @param variables
	 */
	private static void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
		if (expr instanceof Variable<?>) {
			variables.add((Variable<?>) expr);
		} else if (expr instanceof BinaryExpression<?>) {
			BinaryExpression<?> bin = (BinaryExpression<?>) expr;
			getVariables(bin.getLeftOperand(), variables);
			getVariables(bin.getRightOperand(), variables);
		} else if (expr instanceof UnaryExpression<?>) {
			UnaryExpression<?> un = (UnaryExpression<?>) expr;
			getVariables(un.getOperand(), variables);
		} else if (expr instanceof Constraint<?>) {
			// ignore

		}
	}
}
