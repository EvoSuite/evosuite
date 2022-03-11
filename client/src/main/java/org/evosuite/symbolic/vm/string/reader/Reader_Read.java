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
package org.evosuite.symbolic.vm.string.reader;

import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

import java.io.Reader;
import java.io.StringReader;

public final class Reader_Read extends SymbolicFunction {

    private static final String READ = "read";

    public Reader_Read(SymbolicEnvironment env) {
        super(env, Types.JAVA_IO_READER, READ, Types.TO_INT_DESCRIPTOR);
    }

    @Override
    public Object executeFunction() {

        Reader conc_reader = (Reader) this.getConcReceiver();

        if (conc_reader instanceof StringReader) {
            ReferenceConstant symb_str_reader = this.getSymbReceiver();
            StringReader conc_str_reader = (StringReader) conc_reader;

            StringReaderExpr stringReaderExpr = (StringReaderExpr) env.heap
                    .getField(Types.JAVA_IO_STRING_READER,
                            SymbolicHeap.$STRING_READER_VALUE, conc_str_reader,
                            symb_str_reader);

            if (stringReaderExpr != null
                    && stringReaderExpr.containsSymbolicVariable()) {

                StringValue symb_string = stringReaderExpr.getString();
                String conc_string = symb_string.getConcreteValue();

                int currPosition = stringReaderExpr.getReaderPosition();

                if (currPosition < conc_string.length()) {
                    // update symbolic string reader
                    currPosition++;

                    int conc_string_reader_value;
                    if (currPosition >= conc_string.length()) {
                        conc_string_reader_value = -1;
                    } else {
                        conc_string_reader_value = conc_string
                                .charAt(currPosition);
                    }

                    StringReaderExpr newStringReaderExpr = new StringReaderExpr(
                            (long) conc_string_reader_value, symb_string,
                            currPosition);
                    env.heap.putField(Types.JAVA_IO_STRING_READER,
                            SymbolicHeap.$STRING_READER_VALUE, conc_str_reader,
                            symb_str_reader, newStringReaderExpr);

                }

                // returns STRING_READER(string,currPosition)
                return stringReaderExpr;

            }

        }

        return this.getSymbIntegerRetVal();
    }
}
