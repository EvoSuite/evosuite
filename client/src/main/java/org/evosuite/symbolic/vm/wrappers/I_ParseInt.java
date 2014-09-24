package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class I_ParseInt extends SymbolicFunction {

	private static final String PARSE_INT = "parseInt";

	public I_ParseInt(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_INTEGER, PARSE_INT,
				Types.STR_TO_INT_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		NonNullReference symb_string_ref = (NonNullReference) this
				.getSymbArgument(0);
		String conc_string = (String) this.getConcArgument(0);

		int conc_integer = this.getConcIntRetVal();

		StringValue symb_string_value = env.heap.getField(
				org.evosuite.symbolic.vm.regex.Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_string, symb_string_ref,
				conc_string);

		long longValue = (long) conc_integer;

		StringToIntegerCast parse_int_value = new StringToIntegerCast(
				symb_string_value, longValue);

		return parse_int_value;
	}

}
