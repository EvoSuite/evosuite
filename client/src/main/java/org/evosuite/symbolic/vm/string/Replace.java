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
package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Replace extends SymbolicFunction {

    private static final String REPLACE = "replace";

    public Replace(SymbolicEnvironment env, String desc) {
        super(env, Types.JAVA_LANG_STRING, REPLACE, desc);
    }

    public static final class Replace_C extends Replace {

        public Replace_C(SymbolicEnvironment env) {
            super(env, Types.CHAR_CHAR_TO_STR_DESCRIPTOR);
        }

        @Override
        public Object executeFunction() {

            // string receiver
            ReferenceConstant symb_receiver = this.getSymbReceiver();
            String conc_receiver = (String) this.getConcReceiver();

            // old char
            IntegerValue oldCharExpr = this.getSymbIntegerArgument(0);

            // new char
            IntegerValue newCharExpr = this.getSymbIntegerArgument(1);

            // return value
            ReferenceExpression symb_ret_val = this.getSymbRetVal();
            String conc_ret_val = (String) this.getConcRetVal();

            StringValue stringReceiverExpr = env.heap.getField(
                    Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                    conc_receiver, symb_receiver, conc_receiver);

            if (symb_ret_val instanceof ReferenceConstant) {

                ReferenceConstant non_null_symb_ret_val = (ReferenceConstant) symb_ret_val;

                StringMultipleExpression symb_value = new StringMultipleExpression(
                        stringReceiverExpr, Operator.REPLACEC, oldCharExpr,
                        new ArrayList<>(Collections
                                .singletonList(newCharExpr)),
                        conc_ret_val);

                env.heap.putField(Types.JAVA_LANG_STRING,
                        SymbolicHeap.$STRING_VALUE, conc_ret_val,
                        non_null_symb_ret_val, symb_value);

            }

            return this.getSymbRetVal();
        }
    }

    public static final class Replace_CS extends Replace {

        public Replace_CS(SymbolicEnvironment env) {
            super(env, Types.CHARSEQ_CHARSEQ_TO_STR_DESCRIPTOR);
        }

        @Override
        public Object executeFunction() {

            // string receiver
            ReferenceConstant symb_receiver = this.getSymbReceiver();
            String conc_receiver = (String) this.getConcReceiver();

            // old string
            ReferenceExpression symb_old_str = this.getSymbArgument(0);
            CharSequence conc_old_char_seq = (CharSequence) this
                    .getConcArgument(0);

            // new string
            ReferenceExpression symb_new_str = this.getSymbArgument(1);
            CharSequence conc_new_char_seq = (CharSequence) this
                    .getConcArgument(1);

            // return value
            ReferenceExpression symb_ret_val = this.getSymbRetVal();
            String conc_ret_val = (String) this.getConcRetVal();

            StringValue stringReceiverExpr = env.heap.getField(
                    Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                    conc_receiver, symb_receiver, conc_receiver);

            if (symb_old_str instanceof ReferenceConstant
                    && symb_new_str instanceof ReferenceConstant
                    && symb_ret_val instanceof ReferenceConstant) {

                ReferenceConstant non_null_symb_old_str = (ReferenceConstant) symb_old_str;
                ReferenceConstant non_null_symb_new_str = (ReferenceConstant) symb_new_str;
                ReferenceConstant non_null_symb_ret_val = (ReferenceConstant) symb_ret_val;

                if (conc_old_char_seq instanceof String
                        && conc_new_char_seq instanceof String) {

                    String conc_old_str = (String) conc_old_char_seq;

                    StringValue oldStringExpr = env.heap.getField(
                            Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                            conc_old_str, non_null_symb_old_str, conc_old_str);

                    String conc_new_str = (String) conc_new_char_seq;

                    StringValue newStringExpr = env.heap.getField(
                            Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                            conc_new_str, non_null_symb_new_str, conc_new_str);

                    StringMultipleExpression symb_value = new StringMultipleExpression(
                            stringReceiverExpr, Operator.REPLACECS,
                            oldStringExpr, new ArrayList<>(
                            Collections.singletonList(newStringExpr)),
                            conc_ret_val);

                    env.heap.putField(Types.JAVA_LANG_STRING,
                            SymbolicHeap.$STRING_VALUE, conc_ret_val,
                            non_null_symb_ret_val, symb_value);

                }
            }

            return symb_ret_val;
        }
    }

}
