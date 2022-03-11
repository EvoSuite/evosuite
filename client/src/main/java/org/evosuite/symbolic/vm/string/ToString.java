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

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

public final class ToString extends SymbolicFunction {

    private static final String TO_STRING = "toString";

    public ToString(SymbolicEnvironment env) {
        super(env, Types.JAVA_LANG_STRING, TO_STRING, Types.TO_STR_DESCRIPTOR);
    }

    @Override
    public Object executeFunction() {

        // object receiver
        ReferenceConstant symb_str = this.getSymbReceiver();
        String conc_str = (String) this.getConcReceiver();

        // return value
        String conc_ret_val = (String) this.getConcRetVal();
        ReferenceConstant symb_ret_val = (ReferenceConstant) this.getSymbRetVal();

        StringValue string_expr = env.heap.getField(Types.JAVA_LANG_STRING,
                SymbolicHeap.$STRING_VALUE, conc_str, symb_str, conc_str);

        env.heap.putField(Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                conc_ret_val, symb_ret_val, string_expr);

        return this.getSymbRetVal();
    }
}
