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
package org.evosuite.symbolic.vm.string.builder;

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class StringBuilder_ToString extends SymbolicFunction {

    private static final String TO_STRING = "toString";

    public StringBuilder_ToString(SymbolicEnvironment env) {
        super(env, Types.JAVA_LANG_STRING_BUILDER, TO_STRING,
                Types.TO_STR_DESCRIPTOR);
    }

    @Override
    public Object executeFunction() {
        ReferenceConstant symb_str_builder = this
                .getSymbReceiver();

        // receiver
        StringBuilder conc_str_builder = (StringBuilder) this.getConcReceiver();

        // return value
        String res = (String) this.getConcRetVal();

        if (res != null) {
            ReferenceConstant symb_ret_val = (ReferenceConstant) this
                    .getSymbRetVal();

            StringValue symb_value = env.heap.getField(
                    Types.JAVA_LANG_STRING_BUILDER,
                    SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
                    symb_str_builder, conc_str_builder.toString());

            String conc_receiver = res;
            env.heap.putField(Types.JAVA_LANG_STRING,
                    SymbolicHeap.$STRING_VALUE, conc_receiver, symb_ret_val,
                    symb_value);
        }

        return this.getSymbRetVal();
    }
}
