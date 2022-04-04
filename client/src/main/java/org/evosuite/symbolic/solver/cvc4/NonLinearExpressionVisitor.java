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
package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.bv.*;
import org.evosuite.symbolic.expr.fp.*;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.GetFieldExpression;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.ref.array.ArrayConstant;
import org.evosuite.symbolic.expr.ref.array.ArraySelect;
import org.evosuite.symbolic.expr.ref.array.ArrayStore;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.reftype.LambdaSyntheticType;
import org.evosuite.symbolic.expr.reftype.LiteralClassType;
import org.evosuite.symbolic.expr.reftype.LiteralNullType;
import org.evosuite.symbolic.expr.str.*;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;

import java.util.ArrayList;

final class NonLinearExpressionVisitor implements ExpressionVisitor<Boolean, Void> {

    @Override
    public Boolean visit(IntegerBinaryExpression n, Void arg) {
        Boolean left = n.getLeftOperand().accept(this, null);
        Boolean right = n.getRightOperand().accept(this, null);
        if (left || right)
            return true;

        switch (n.getOperator()) {
            case MUL:
            case DIV:
            case REM: {
                boolean isLeftSymbolic = n.getLeftOperand().containsSymbolicVariable();
                boolean isRightSymbolic = n.getRightOperand().containsSymbolicVariable();

                return isLeftSymbolic && isRightSymbolic;
            }
            default:
                return false;
        }
    }

    @Override
    public Boolean visit(IntegerComparison n, Void arg) {
        Boolean left_ret_val = n.getLeftOperant().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperant().accept(this, null);
        return right_ret_val;
    }

    @Override
    public Boolean visit(IntegerConstant n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(IntegerUnaryExpression n, Void arg) {
        Boolean ret_val = n.getOperand().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(IntegerVariable n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(RealComparison n, Void arg) {
        Boolean left_ret_val = n.getLeftOperant().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperant().accept(this, null);
        return right_ret_val;
    }

    @Override
    public Boolean visit(RealToIntegerCast n, Void arg) {
        Boolean ret_val = n.getArgument().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(RealUnaryToIntegerExpression n, Void arg) {
        Boolean ret_val = n.getOperand().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(StringBinaryComparison n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        return right_ret_val;
    }

    @Override
    public Boolean visit(StringBinaryToIntegerExpression n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        return right_ret_val;
    }

    @Override
    public Boolean visit(StringMultipleComparison n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        if (right_ret_val) {
            return true;
        }

        ArrayList<Expression<?>> exprs = n.getOther();
        for (Expression<?> expression : exprs) {
            Boolean isNonLinear = expression.accept(this, null);
            if (isNonLinear) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Boolean visit(StringMultipleToIntegerExpression n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        if (right_ret_val) {
            return true;
        }

        ArrayList<Expression<?>> exprs = n.getOther();
        for (Expression<?> expression : exprs) {
            Boolean isNonLinear = expression.accept(this, null);
            if (isNonLinear) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Boolean visit(StringToIntegerCast n, Void arg) {
        Boolean ret_val = n.getArgument().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(StringUnaryToIntegerExpression n, Void arg) {
        Boolean ret_val = n.getOperand().accept(this, null);
        return ret_val;

    }

    @Override
    public Boolean visit(IntegerToRealCast n, Void arg) {
        Boolean ret_val = n.getArgument().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(RealBinaryExpression n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        if (right_ret_val) {
            return true;
        }

        switch (n.getOperator()) {
            case MUL:
            case DIV: {
                boolean isLeftSymbolic = n.getLeftOperand().containsSymbolicVariable();
                boolean isRightSymbolic = n.getRightOperand().containsSymbolicVariable();

                return isLeftSymbolic && isRightSymbolic;
            }
            default:
                return false;
        }
    }

    @Override
    public Boolean visit(RealConstant n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(RealUnaryExpression n, Void arg) {
        Boolean ret_val = n.getOperand().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(RealVariable n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(IntegerToStringCast n, Void arg) {
        Boolean ret_val = n.getArgument().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(RealToStringCast n, Void arg) {
        Boolean ret_val = n.getArgument().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(StringBinaryExpression n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        return right_ret_val;
    }

    @Override
    public Boolean visit(StringConstant n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(StringMultipleExpression n, Void arg) {
        Boolean left_ret_val = n.getLeftOperand().accept(this, null);
        if (left_ret_val) {
            return true;
        }
        Boolean right_ret_val = n.getRightOperand().accept(this, null);
        if (right_ret_val) {
            return true;
        }

        ArrayList<Expression<?>> exprs = n.getOther();
        for (Expression<?> expression : exprs) {
            Boolean isNonLinear = expression.accept(this, null);
            if (isNonLinear) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Boolean visit(StringUnaryExpression n, Void arg) {
        Boolean ret_val = n.getOperand().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(StringVariable n, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(HasMoreTokensExpr n, Void arg) {
        Boolean ret_val = n.getTokenizerExpr().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(NewTokenizerExpr n, Void arg) {
        Boolean string_ret_val = n.getString().accept(this, null);
        if (string_ret_val) {
            return true;
        }
        Boolean delimVal_ret_val = n.getDelimiter().accept(this, null);
        return delimVal_ret_val;
    }

    @Override
    public Boolean visit(NextTokenizerExpr n, Void arg) {
        Boolean ret_val = n.getTokenizerExpr().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(StringNextTokenExpr n, Void arg) {
        Boolean ret_val = n.getTokenizerExpr().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(StringReaderExpr n, Void arg) {
        Boolean ret_val = n.getString().accept(this, null);
        return ret_val;
    }

    @Override
    public Boolean visit(ReferenceConstant referenceConstant, Void arg) {
        throw new UnsupportedOperationException(
                "Removal of Non-Linear expressions for ReferenceConstant is not yet implemented!");
    }

    @Override
    public Boolean visit(ReferenceVariable r, Void arg) {
        throw new UnsupportedOperationException(
                "Removal of Non-Linear expressions for ReferenceVariable is not yet implemented!");
    }

    @Override
    public Boolean visit(GetFieldExpression r, Void arg) {
        throw new UnsupportedOperationException(
                "Removal of Non-Linear expressions for GetFieldExpression is not yet implemented!");
    }

    @Override
    public Boolean visit(ArraySelect.IntegerArraySelect r, Void arg) {
        Boolean array = r.getSymbolicArray().accept(this, null);
        if (array) return true;

        Boolean index = r.getSymbolicIndex().accept(this, null);
        if (index) return true;

        Boolean value = r.getSymbolicSelectedValue().accept(this, null);
        return value;
    }

    @Override
    public Boolean visit(ArraySelect.RealArraySelect r, Void arg) {
        Boolean array = r.getSymbolicArray().accept(this, null);
        if (array) return true;

        Boolean index = r.getSymbolicIndex().accept(this, null);
        if (index) return true;

        Boolean value = r.getSymbolicSelectedValue().accept(this, null);
        return value;
    }

    @Override
    public Boolean visit(ArraySelect.StringArraySelect r, Void arg) {
        Boolean array = r.getSymbolicArray().accept(this, null);
        if (array) return true;

        Boolean index = r.getSymbolicIndex().accept(this, null);
        if (index) return true;

        Boolean value = r.getSymbolicSelectedValue().accept(this, null);
        return value;
    }

    @Override
    public Boolean visit(ArrayStore.RealArrayStore r, Void arg) {
        Boolean array = r.getSymbolicArray().accept(this, null);
        if (array) return true;

        Boolean index = r.getSymbolicIndex().accept(this, null);
        if (index) return true;

        Boolean value = r.getSymbolicValue().accept(this, null);
        return value;
    }

    @Override
    public Boolean visit(ArrayStore.StringArrayStore r, Void arg) {
        Boolean array = r.getSymbolicArray().accept(this, null);
        if (array) return true;

        Boolean index = r.getSymbolicIndex().accept(this, null);
        if (index) return true;

        Boolean value = r.getSymbolicValue().accept(this, null);
        return value;
    }

    @Override
    public Boolean visit(ArrayStore.IntegerArrayStore r, Void arg) {
        Boolean array = r.getSymbolicArray().accept(this, null);
        if (array) return true;

        Boolean index = r.getSymbolicIndex().accept(this, null);
        if (index) return true;

        Boolean value = r.getSymbolicValue().accept(this, null);
        return value;
    }

    @Override
    public Boolean visit(ArrayConstant.IntegerArrayConstant r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayConstant.RealArrayConstant r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayConstant.StringArrayConstant r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayConstant.ReferenceArrayConstant r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayVariable.IntegerArrayVariable r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayVariable.RealArrayVariable r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayVariable.StringArrayVariable r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(ArrayVariable.ReferenceArrayVariable r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(LambdaSyntheticType r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(LiteralNullType r, Void arg) {
        return false;
    }

    @Override
    public Boolean visit(LiteralClassType r, Void arg) {
        return false;
    }
}
