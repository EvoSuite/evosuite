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

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.constraint.ConstraintEvaluator;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Interface for SMT solvers
 *
 * @author Gordon Fraser
 */
public abstract class Solver {

    private final boolean addMissingVariables;
    private final SolverCache solverCache;

    public Solver(boolean addMissingVariables) {
        this(addMissingVariables, SolverCache.getInstance());
    }

    public Solver() {
        //TODO: Replace the getInstance methods with a dependency injection framework later on (e.g guice).
        this(false, SolverCache.getInstance());
    }

    public Solver(boolean addMissingVariables, SolverCache solverCache) {
        this.addMissingVariables = addMissingVariables;
        this.solverCache = solverCache;
    }

    static Logger logger = LoggerFactory.getLogger(Solver.class);

    /**
     * @param constraints a constraint system to be solved
     * @return a non-null result that is SAT or UNSAT
     * @throws SolverTimeoutException    a timeout occurred while executing the solver
     * @throws IOException               an IOException occurred while executing the solver
     * @throws SolverParseException      the solver's result could not be parsed into a valid SolverResult
     * @throws SolverEmptyQueryException the solver
     * @throws SolverErrorException      the solver reported an error after its execution
     */
    public SolverResult solve(Collection<Constraint<?>> constraints) throws SolverTimeoutException, SolverParseException, SolverEmptyQueryException, SolverErrorException, IOException {
        if (solverCache.hasCachedResult(constraints)) {
            return solverCache.getCachedResult();
        }

        SolverResult solverResult;
        try {
            solverResult = executeSolver(constraints);

            if (solverResult != null && !solverResult.isUnknown()) {
                solverCache.saveSolverResult(constraints, solverResult);
            }
        } catch (IllegalArgumentException | IOException e) {
            solverResult = null;
        }

        return solverResult;
    }

    /**
     * @param constraints
     * @return
     * @throws SolverTimeoutException
     * @throws IOException
     * @throws SolverParseException
     * @throws SolverEmptyQueryException
     * @throws SolverErrorException
     */
    public abstract SolverResult executeSolver(Collection<Constraint<?>> constraints) throws SolverTimeoutException,
            IOException, SolverParseException, SolverEmptyQueryException, SolverErrorException;

    protected boolean addMissingVariables() {
        return addMissingVariables;
    }

    /**
     * Returns a mapping from variables to their current concrete values.
     *
     * @param variables
     * @return a mapping from variables to their current concrete values.
     */
    protected static Map<String, Object> getConcreteValues(Set<Variable<?>> variables) {

        Map<String, Object> concrete_values = new HashMap<>();
        for (Variable<?> v : variables) {
            String var_name = v.getName();
            Object concrete_value = v.getConcreteValue();
            concrete_values.put(var_name, concrete_value);
        }
        return concrete_values;
    }

    /**
     * Creates a set with all the variables in the constraints.
     *
     * @param constraints the constraint system
     * @return the set of variables in the constraint system
     */
    protected static Set<Variable<?>> getVariables(Collection<Constraint<?>> constraints) {
        Set<Variable<?>> variables = new HashSet<>();
        for (Constraint<?> c : constraints) {
            variables.addAll(c.getLeftOperand().getVariables());
            variables.addAll(c.getRightOperand().getVariables());
        }
        return variables;
    }

    /**
     * Restore all concrete values of the variables using the concrete_values
     * mapping.
     *
     * @param variables
     * @param concrete_values
     */
    protected static void setConcreteValues(Set<Variable<?>> variables, Map<String, Object> concrete_values) {
        for (Variable<?> v : variables) {

            String var_name = v.getName();

            if (!concrete_values.containsKey(var_name)) {
                continue;
            }

            Object concreteValue = concrete_values.get(var_name);

            if (v instanceof StringVariable) {
                StringVariable sv = (StringVariable) v;
                String concreteString = (String) concreteValue;
                sv.setConcreteValue(concreteString);
            } else if (v instanceof IntegerVariable) {
                IntegerVariable iv = (IntegerVariable) v;
                Long concreteInteger = (Long) concreteValue;
                iv.setConcreteValue(concreteInteger);
            } else if (v instanceof RealVariable) {
                RealVariable ir = (RealVariable) v;
                Double concreteReal = (Double) concreteValue;
                ir.setConcreteValue(concreteReal);
            } else if (v instanceof ArrayVariable) {
                ArrayVariable arr = (ArrayVariable) v;
                arr.setConcreteValue(
                        getResizedArray(
                                arr.getConcreteValue(),
                                concreteValue));
            } else {
                logger.warn("unknow variable type " + v.getClass().getName());
            }
        }
    }

    protected static boolean checkSAT(Collection<Constraint<?>> constraints, SolverResult satResult) {

        if (satResult == null) {
            throw new NullPointerException("satResult should be non-null");
        }

        if (!satResult.isSAT()) {
            throw new IllegalArgumentException("satResult should be SAT");
        }

        // back-up values
        Set<Variable<?>> variables = getVariables(constraints);
        Map<String, Object> initialValues = getConcreteValues(variables);
        // set new values
        Map<String, Object> newValues = satResult.getModel();
        setConcreteValues(variables, newValues);

        try {
            // check SAT with new values
            ConstraintEvaluator evaluator = new ConstraintEvaluator();
            for (Constraint<?> constraint : constraints) {
                Boolean evaluation = (Boolean) constraint.accept(evaluator, null);
                if (evaluation == null) {
                    throw new NullPointerException();
                }
                if (evaluation == false) {
                    return false;
                }
            }
            return true;
        } finally {
            // restore values
            setConcreteValues(variables, initialValues);
        }
    }

    /**
     * Returns a new array with the longest length.
     * <p>
     * TODO: Rework this in the future, the way we talk about lengths is probably not the best.
     *
     * @param originalArray
     * @param newArray
     * @return
     */
    private static Object getResizedArray(Object originalArray, Object newArray) {
        int originalLength = Array.getLength(originalArray);
        int newLength = Array.getLength(newArray);

        if (originalLength > newLength) {
            Object copyArr = Array.newInstance(newArray.getClass().getComponentType(), originalLength);
            System.arraycopy(newArray, 0, copyArr, 0, Math.min(originalLength, newLength));
            return copyArr;
        }

        return newArray;
    }

}
