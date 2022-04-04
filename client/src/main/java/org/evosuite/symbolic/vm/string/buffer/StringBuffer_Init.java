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
package org.evosuite.symbolic.vm.string.buffer;

import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;

public abstract class StringBuffer_Init extends SymbolicFunction {

    private static final String INIT = "<init>";

    public StringBuffer_Init(SymbolicEnvironment env, String desc) {
        super(env, Types.JAVA_LANG_STRING_BUFFER, INIT, desc);
    }

    public static final class StringBufferInit_S extends StringBuffer_Init {

        public StringBufferInit_S(SymbolicEnvironment env) {
            super(env, Types.STR_TO_VOID_DESCRIPTOR);

        }

        /**
         * new StringBuffer(String)
         */
        @Override
        public Object executeFunction() {
            ReferenceConstant symb_str_buffer = this.getSymbReceiver();
            ReferenceConstant symb_string = (ReferenceConstant) this
                    .getSymbArgument(0);
            String conc_string = (String) this.getConcArgument(0);

            // get symbolic value for string argument
            StringValue string_value = this.env.heap.getField(
                    Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
                    conc_string, symb_string, conc_string);

            // update symbolic heap
            this.env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
                    SymbolicHeap.$STRING_BUFFER_CONTENTS, null,
                    symb_str_buffer, string_value);

            // return void
            return null;
        }

    }

}
