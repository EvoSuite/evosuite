/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.vm.ConstraintFactory;
import org.evosuite.symbolic.vm.ExpressionFactory;

import java.io.IOException;
import java.util.*;

/**
 * Outside use of the SMT solver related utils Logic.
 *
 * @author ilebrero
 */
public abstract class SolverUtils {

    /**
     * solves a given query (i.e. list of constraints).
     *
     * @param query
     * @return
     */
    public static SolverResult solveQuery(List<Constraint<?>> query) {
        Solver solver = SolverFactory.getInstance().buildNewSolver();
        SolverResult solverResult = null;

        try {
            solverResult = solver.solve(query);
        } catch (SolverTimeoutException | SolverParseException | SolverEmptyQueryException | SolverErrorException | IOException e) {
            // TODO: see how we are going to handle this later on.
            // TODO: add statistics about this.
            solverResult = null;
        }

        return solverResult;
    }

    /**
     * Creates boundaries for the SMT query variables.
     *
     * @param query
     * @return
     */
    public static Collection<? extends Constraint<?>> createBoundsForQueryVariables(List<Constraint<?>> query) {
        Set<Variable<?>> variables = new HashSet<>();
        for (Constraint<?> constraint : query) {
            variables.addAll(constraint.getVariables());
        }

        List<Constraint<?>> boundsForVariables = new ArrayList<>();
        for (Variable<?> variable : variables) {
            if (variable instanceof IntegerVariable) {
                IntegerVariable integerVariable = (IntegerVariable) variable;
                Long minValue = integerVariable.getMinValue();
                Long maxValue = integerVariable.getMaxValue();
                if (maxValue == Long.MAX_VALUE && minValue == Long.MIN_VALUE) {
                    // skip constraints for Long variables
                    continue;
                }
                IntegerConstant minValueExpr = ExpressionFactory.buildNewIntegerConstant(minValue);
                IntegerConstant maxValueExpr = ExpressionFactory.buildNewIntegerConstant(maxValue);
                IntegerConstraint minValueConstraint = ConstraintFactory.gte(integerVariable, minValueExpr);
                IntegerConstraint maxValueConstraint = ConstraintFactory.lte(integerVariable, maxValueExpr);
                boundsForVariables.add(minValueConstraint);
                boundsForVariables.add(maxValueConstraint);

            } else if (variable instanceof RealVariable) {
                // skip
            } else if (variable instanceof StringVariable) {
                // skip
            } else if (variable instanceof ArrayVariable) {
                // skip
            } else {
                throw new UnsupportedOperationException(
                        "Unknown variable type " + variable.getClass().getName());
            }
        }

        return boundsForVariables;
    }

    /**
     * Creates a Solver query give a branch condition
     *
     * @param pc
     * @return
     */
    public static List<Constraint<?>> buildQuery(PathCondition pc) {
        List<Constraint<?>> query = new LinkedList<>();

        for (BranchCondition branchCondition : pc.getBranchConditions()) {
            query.addAll(branchCondition.getSupportingConstraints());
            query.add(branchCondition.getConstraint());
        }

        // Compute cone of influence reduction
        List<Constraint<?>> simplified_query = reduce(query);
        return simplified_query;
    }

    /**
     * Creates a Solver query give a branch condition
     *
     * @param pc
     * @param conditionIndexToNegate
     * @return
     */
    public static List<Constraint<?>> buildQueryNegatingIthCondition(PathCondition pc, int conditionIndexToNegate) {
        List<Constraint<?>> query = new LinkedList<>();
        for (int i = 0; i < conditionIndexToNegate; i++) {
            BranchCondition b = pc.get(i);
            query.addAll(b.getSupportingConstraints());
            query.add(b.getConstraint());
        }

        BranchCondition targetBranch = pc.get(conditionIndexToNegate);
        Constraint<?> negation = targetBranch.getConstraint().negate();
        query.addAll(targetBranch.getSupportingConstraints());
        query.add(negation);

        // Compute cone of influence reduction
        List<Constraint<?>> simplified_query = reduce(query);
        return simplified_query;
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

        LinkedList<Constraint<?>> coi = new LinkedList<>();
        if (dependencies.size() <= 0)
            return coi;

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
        Set<Variable<?>> variables = new HashSet<>();
        getVariables(constraint.getLeftOperand(), variables);
        getVariables(constraint.getRightOperand(), variables);
        return variables;
    }

    /**
     * Recursively determine constraints in expression
     *
     * @param expr      a {@link org.evosuite.symbolic.expr.Expression} object.
     * @param variables a {@link java.util.Set} object.
     */
    private static void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
        variables.addAll(expr.getVariables());
    }
}