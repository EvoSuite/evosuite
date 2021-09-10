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
package org.evosuite.testcase.utils;

import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;

import java.util.function.Function;

/**
 * Class checker for different types of Statements
 *
 * @author Ignacio Lebrero
 */
public enum StatementClassChecker {
    ARRAY_STATEMENT((statement) -> (statement instanceof ArrayStatement)),
    PRIMITIVE_STATEMENT((statement) -> (statement instanceof PrimitiveStatement)),
    ASSIGNMENT_STATEMENT((statement) -> (statement instanceof AssignmentStatement));

    private final Function<Statement, Boolean> statementCheck;

    StatementClassChecker(final Function<Statement, Boolean> statementCheck) {
        this.statementCheck = statementCheck;
    }

    public Boolean checkClassType(Statement statement) {
        return statementCheck.apply(statement);
    }
}
