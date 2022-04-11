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
package org.evosuite.symbolic.expr;

import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerComparison;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.bv.RealComparison;
import org.evosuite.symbolic.expr.bv.RealToIntegerCast;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.bv.StringMultipleToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.fp.IntegerToRealCast;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.ClassReferenceConstant;
import org.evosuite.symbolic.expr.ref.ClassReferenceVariable;
import org.evosuite.symbolic.expr.ref.GetFieldExpression;
import org.evosuite.symbolic.expr.ref.NullReferenceConstant;
import org.evosuite.symbolic.expr.ref.array.ArrayConstant;
import org.evosuite.symbolic.expr.ref.array.ArraySelect;
import org.evosuite.symbolic.expr.ref.array.ArrayStore;
import org.evosuite.symbolic.expr.ref.array.ArrayVariable;
import org.evosuite.symbolic.expr.reftype.ArrayTypeConstant;
import org.evosuite.symbolic.expr.reftype.ClassTypeConstant;
import org.evosuite.symbolic.expr.reftype.LambdaSyntheticTypeConstant;
import org.evosuite.symbolic.expr.reftype.NullTypeConstant;
import org.evosuite.symbolic.expr.str.IntegerToStringCast;
import org.evosuite.symbolic.expr.str.RealToStringCast;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringUnaryExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;

public interface ExpressionVisitor<K, V> {

    K visit(IntegerBinaryExpression n, V arg);

    K visit(IntegerComparison n, V arg);

    K visit(IntegerConstant n, V arg);

    K visit(IntegerUnaryExpression n, V arg);

    K visit(IntegerVariable n, V arg);

    K visit(RealComparison n, V arg);

    K visit(RealToIntegerCast n, V arg);

    K visit(RealUnaryToIntegerExpression n, V arg);

    K visit(StringBinaryComparison n, V arg);

    K visit(StringBinaryToIntegerExpression n, V arg);

    K visit(StringMultipleComparison n, V arg);

    K visit(StringMultipleToIntegerExpression n, V arg);

    K visit(StringToIntegerCast n, V arg);

    K visit(StringUnaryToIntegerExpression n, V arg);

    K visit(IntegerToRealCast n, V arg);

    K visit(RealBinaryExpression n, V arg);

    K visit(RealConstant n, V arg);

    K visit(RealUnaryExpression n, V arg);

    K visit(RealVariable n, V arg);

    K visit(StringReaderExpr n, V arg);

    K visit(IntegerToStringCast n, V arg);

    K visit(RealToStringCast n, V arg);

    K visit(StringBinaryExpression n, V arg);

    K visit(StringConstant n, V arg);

    K visit(StringMultipleExpression n, V arg);

    K visit(StringUnaryExpression n, V arg);

    K visit(StringVariable n, V arg);

    K visit(HasMoreTokensExpr n, V arg);

    K visit(NewTokenizerExpr n, V arg);

    K visit(NextTokenizerExpr n, V arg);

    K visit(StringNextTokenExpr n, V arg);

    /********************** Arrays *********************/

    K visit(ArrayStore.IntegerArrayStore r, V arg);

    K visit(ArrayStore.RealArrayStore r, V arg);

    K visit(ArrayStore.StringArrayStore r, V arg);

    K visit(ArraySelect.IntegerArraySelect r, V arg);

    K visit(ArraySelect.RealArraySelect r, V arg);

    K visit(ArraySelect.StringArraySelect r, V arg);

    K visit(ArrayConstant.IntegerArrayConstant r, V arg);

    K visit(ArrayConstant.RealArrayConstant r, V arg);

    K visit(ArrayConstant.StringArrayConstant r, V arg);

    K visit(ArrayConstant.ReferenceArrayConstant r, V arg);

    K visit(ArrayVariable.IntegerArrayVariable r, V arg);

    K visit(ArrayVariable.RealArrayVariable r, V arg);

    K visit(ArrayVariable.StringArrayVariable r, V arg);

    K visit(ArrayVariable.ReferenceArrayVariable r, V arg);

    /********************** Reference Types *********************/

    K visit(LambdaSyntheticTypeConstant r, V arg);

    K visit(NullTypeConstant r, V args);

    K visit(ClassTypeConstant r, V arg);

    K visit(ArrayTypeConstant r, V arg);

    /********************** References *********************/

    K visit(ClassReferenceVariable r, V arg);

    K visit(GetFieldExpression r, V arg);

    K visit(NullReferenceConstant r, V arg);

    K visit(ClassReferenceConstant r, V args);
}
