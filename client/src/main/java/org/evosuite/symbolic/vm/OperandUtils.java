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
package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;

/**
 * @author Ilebrero
 */
public class OperandUtils {

    public static Expression<?> retrieveOperandExpression(Operand operand) {
        if (operand instanceof IntegerOperand) {
            IntegerOperand intOp = (IntegerOperand) operand;
            return intOp.getIntegerExpression();
        } else if (operand instanceof RealOperand) {
            RealOperand realOp = (RealOperand) operand;
            return realOp.getRealExpression();
        } else if (operand instanceof ReferenceOperand) {
            ReferenceOperand referenceOperand = (ReferenceOperand) operand;
            return referenceOperand.getReference();
        } else {
            throw new IllegalStateException("Unexpected operandType: " + operand.getClass().getName() + " is not a supported operand.");
        }
    }

    public static Operand expressionToOperand(Expression expression) {
        if (expression instanceof IntegerValue) {
            IntegerValue intExpression = (IntegerValue) expression;
            return new Bv64Operand(intExpression);
        } else if (expression instanceof RealValue) {
            RealValue realExpression = (RealValue) expression;
            return new Fp64Operand(realExpression);
        } else if (expression instanceof ReferenceExpression) {
            ReferenceExpression referenceExpression = (ReferenceExpression) expression;
            return new ReferenceOperand(referenceExpression);
        } else {
            throw new IllegalStateException("Unexpected expression type: " + expression.getClass().getName() + " is not a supported operand.");
        }
    }

}