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
package org.evosuite.symbolic.vm.regex;

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

import java.util.regex.Matcher;

public final class Pattern_Matcher extends SymbolicFunction {

    private static final String MATCHER = "matcher";

    public Pattern_Matcher(SymbolicEnvironment env) {
        super(env, Types.JAVA_UTIL_REGEX_PATTERN, MATCHER,
                Types.CHARSEQ_TO_MATCHER);
    }

    @Override
    public Object executeFunction() {

        // receiver
        @SuppressWarnings("unused")
        ReferenceConstant symb_receiver = this.getSymbReceiver();

        // argument
        CharSequence conc_char_seq = (CharSequence) this.getConcArgument(0);
        ReferenceExpression symb_char_seq = this.getSymbArgument(0);

        // return value
        Matcher conc_matcher = (Matcher) this.getConcRetVal();
        ReferenceConstant symb_matcher = (ReferenceConstant) this.getSymbRetVal();

        if (conc_char_seq != null && conc_char_seq instanceof String) {
            assert symb_char_seq instanceof ReferenceConstant;
            ReferenceConstant symb_string = (ReferenceConstant) symb_char_seq;

            String string = (String) conc_char_seq;
            StringValue symb_input = env.heap.getField(Types.JAVA_LANG_STRING,
                    SymbolicHeap.$STRING_VALUE, string, symb_string, string);

            env.heap.putField(Types.JAVA_UTIL_REGEX_MATCHER,
                    SymbolicHeap.$MATCHER_INPUT, conc_matcher, symb_matcher,
                    symb_input);
        }
        return symb_matcher;
    }

}
