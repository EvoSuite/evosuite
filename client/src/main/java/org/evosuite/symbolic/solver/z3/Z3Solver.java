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
package org.evosuite.symbolic.solver.z3;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.solver.*;
import org.evosuite.symbolic.solver.smt.*;
import org.evosuite.symbolic.solver.smt.SmtOperation.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Z3Solver extends SmtSolver {

    public Z3Solver() {
        super();
    }

    public Z3Solver(boolean addMissingVariables) {
        super(addMissingVariables);
    }

    static Logger logger = LoggerFactory.getLogger(Z3Solver.class);

    @Override
    public SolverResult executeSolver(Collection<Constraint<?>> constraints) throws SolverTimeoutException, IOException,
            SolverParseException, SolverEmptyQueryException, SolverErrorException {

        long hard_timeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

        Set<Variable<?>> variables = new HashSet<>();
        for (Constraint<?> c : constraints) {
            Set<Variable<?>> c_variables = c.getVariables();
            variables.addAll(c_variables);
        }

        SmtQuery query = buildSmtQuery(constraints, hard_timeout);

        if (query.getConstantDeclarations().isEmpty()) {
            logger.debug("Z3 SMT query has no variables");
            throw new SolverEmptyQueryException("Z3 SMT query has no variables");
        }

        if (query.getAssertions().isEmpty()) {
            Map<String, Object> emptySolution = new HashMap<>();
            SolverResult emptySAT = SolverResult.newSAT(emptySolution);
            return emptySAT;
        }

        SmtQueryPrinter printer = new SmtQueryPrinter();
        String queryStr = printer.print(query);

        logger.debug("Z3 Query:");
        logger.debug(queryStr);

        if (Properties.Z3_PATH == null) {
            String errMsg = "Property Z3_PATH should be setted in order to use the Z3 Solver!";
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        }

        String z3Cmd = Properties.Z3_PATH + " -smt2 -in ";

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        String output;
        try {
            launchNewSolvingProcess(z3Cmd, queryStr, (int) hard_timeout, stdout);
            output = stdout.toString("UTF-8");
        } catch (SolverErrorException ex) {
            output = stdout.toString("UTF-8");
            if (!output.startsWith("unsat")) {
                throw ex;
            }
        }

        Map<String, Object> initialValues = getConcreteValues(variables);
        SmtModelParser parser;
        if (this.addMissingVariables()) {
            parser = new SmtModelParser(initialValues);
        } else {
            parser = new SmtModelParser();
        }

        SolverResult result = parser.parse(output);

        if (result.isSAT()) {
            // check if solution is correct, otherwise return UNSAT
            boolean check = checkSAT(constraints, result);
            if (!check) {
                logger.debug("Z3 solution fails to solve the constraint system!");
                SolverResult unsatResult = SolverResult.newUNSAT();
                return unsatResult;
            }
        }

        return result;
    }

    private final static int ASCII_TABLE_LENGTH = 90;

    private static String encodeString(String str) {
        char[] charArray = str.toCharArray();
        String ret_val = "";
        for (char c : charArray) {
            // if (Character.isISOControl(c)) {
            if (Integer.toHexString(c).length() == 1) {
                // padding
                ret_val += "\\x0" + Integer.toHexString(c);
            } else {
                ret_val += "\\x" + Integer.toHexString(c);
            }
            // } else {
            // ret_val += c;
            // }
        }
        return ret_val;
    }

    private static String buildIntToCharFunction() {
        StringBuffer buff = new StringBuffer();
        buff.append(SmtOperation.Operator.INT_TO_CHAR + "((x!1 Int)) String");
        buff.append("\n");
        for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
            char c = (char) i;
            String str = String.valueOf(c);
            String encodedStr = encodeString(str);
            if (i < ASCII_TABLE_LENGTH - 1) {
                String iteStr = String.format("(ite (= x!1 %s) \"%s\"", i, encodedStr);
                buff.append(iteStr);
                buff.append("\n");
            } else {
                buff.append("\"" + encodedStr + "\"");
            }
        }
        for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
            buff.append(")");
        }
        buff.append("\n");
        return buff.toString();
    }

    private static String buildCharToIntFunction() {
        StringBuffer buff = new StringBuffer();
        buff.append(SmtOperation.Operator.CHAR_TO_INT + "((x!1 String)) Int");
        buff.append("\n");
        for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
            char c = (char) i;
            String str = String.valueOf(c);
            String encodedStr = encodeString(str);
            if (i < ASCII_TABLE_LENGTH - 1) {
                String iteStr = String.format("(ite (= x!1 \"%s\") %s", encodedStr, i);
                buff.append(iteStr);
                buff.append("\n");
            } else {
                buff.append(i);
            }
        }
        for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
            buff.append(")");
        }
        buff.append("\n");
        return buff.toString();
    }

    private static SmtQuery buildSmtQuery(Collection<Constraint<?>> constraints, long timeout) {

        SmtQuery query = new SmtQuery();

        query.addOption(":timeout", String.valueOf(timeout));

        ConstraintToZ3Visitor v = new ConstraintToZ3Visitor();

        SmtVariableCollector varCollector = new SmtVariableCollector();
        SmtOperatorCollector opCollector = new SmtOperatorCollector();

        for (Constraint<?> c : constraints) {
            SmtExpr smtExpr = c.accept(v, null);
            if (smtExpr != null) {
                SmtAssertion smtAssertion = new SmtAssertion(smtExpr);
                query.addAssertion(smtAssertion);
                smtExpr.accept(varCollector, null);
                smtExpr.accept(opCollector, null);
            }
        }

        Set<SmtVariable> smtVariables = varCollector.getSmtVariables();
        Set<Operator> smtOperators = opCollector.getOperators();

        Set<SmtVariable> smtVariablesToDeclare = new HashSet<>(smtVariables);

        for (SmtVariable v1 : smtVariablesToDeclare) {
            String varName = v1.getName();
            if (v1 instanceof SmtIntVariable) {
                SmtConstantDeclaration constantDecl = SmtExprBuilder.mkIntConstantDeclaration(varName);
                query.addConstantDeclaration(constantDecl);

            } else if (v1 instanceof SmtRealVariable) {
                SmtConstantDeclaration constantDecl = SmtExprBuilder.mkRealConstantDeclaration(varName);
                query.addConstantDeclaration(constantDecl);

            } else if (v1 instanceof SmtStringVariable) {
                SmtConstantDeclaration constantDecl = SmtExprBuilder.mkStringConstantDeclaration(varName);
                query.addConstantDeclaration(constantDecl);

            } else if (v1 instanceof SmtArrayVariable.SmtRealArrayVariable) {
                SmtConstantDeclaration arrayVar = SmtExprBuilder.mkRealArrayConstantDeclaration(varName);
                query.addConstantDeclaration(arrayVar);

            } else if (v1 instanceof SmtArrayVariable.SmtIntegerArrayVariable) {
                SmtConstantDeclaration arrayVar = SmtExprBuilder.mkIntegerArrayConstantDeclaration(varName);
                query.addConstantDeclaration(arrayVar);

            } else if (v1 instanceof SmtArrayVariable.SmtStringArrayVariable) {
                SmtConstantDeclaration arrayVar = SmtExprBuilder.mkStringArrayConstantDeclaration(varName);
                query.addConstantDeclaration(arrayVar);

            } else {
                throw new RuntimeException("Unknown variable type " + v1.getClass().getCanonicalName());
            }
        }

        if (smtOperators.contains(SmtOperation.Operator.CHAR_TO_INT)) {
            String charToInt = buildCharToIntFunction();
            SmtFunctionDefinition newFunctionDef = new SmtFunctionDefinition(charToInt);
            query.addFunctionDefinition(newFunctionDef);
        }

        if (smtOperators.contains(SmtOperation.Operator.INT_TO_CHAR)) {
            String intToChar = buildIntToCharFunction();
            SmtFunctionDefinition newFunctionDef = new SmtFunctionDefinition(intToChar);
            query.addFunctionDefinition(newFunctionDef);
        }

        return query;

    }

}
