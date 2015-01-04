package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.str.IntegerToStringCast;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public static abstract class ValueOf_Int extends ValueOf {

		public ValueOf_Int(SymbolicEnvironment env, String desc) {
			super(env, desc);
		}

		@Override
		public final Object executeFunction() {

			IntegerValue symb_arg = this.getSymbIntegerArgument(0);

			Reference symb_ret_val = this.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			if (symb_arg.containsSymbolicVariable()) {
				StringValue symbExpr = new IntegerToStringCast(symb_arg,
						conc_ret_val);

				NonNullReference symb_non_null_ret_val = (NonNullReference) symb_ret_val;

				env.heap.putField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_ret_val,
						symb_non_null_ret_val, symbExpr);
			}
			return this.getSymbRetVal();

		}

	}

	public ValueOf(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING, VALUE_OF, desc);
	}

	public static final class ValueOf_O extends ValueOf {

		public ValueOf_O(SymbolicEnvironment env) {
			super(env, Types.OBJECT_TO_STR_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			Reference symb_arg = this.getSymbArgument(0);
			Object conc_arg = this.getConcArgument(0);

			Reference symb_ret_val = this.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			if (conc_arg != null && conc_arg instanceof String) {

				String conc_str_arg = (String) conc_arg;
				NonNullReference symb_non_null_str = (NonNullReference) symb_arg;

				StringValue strExpr = env.heap.getField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_str_arg,
						symb_non_null_str, conc_str_arg);

				NonNullReference symb_non_null_ret_val = (NonNullReference) symb_ret_val;

				env.heap.putField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_ret_val,
						symb_non_null_ret_val, strExpr);
			}

			return this.getSymbRetVal();
		}

	}

	public static final class ValueOf_J extends ValueOf_Int {

		public ValueOf_J(SymbolicEnvironment env) {
			super(env, Types.LONG_TO_STR_DESCRIPTOR);
		}
	}

	public static final class ValueOf_I extends ValueOf_Int {

		public ValueOf_I(SymbolicEnvironment env) {
			super(env, Types.INT_TO_STR_DESCRIPTOR);
		}
	}

	public static final class ValueOf_C extends ValueOf_Int {

		public ValueOf_C(SymbolicEnvironment env) {
			super(env, Types.CHAR_TO_STR_DESCRIPTOR);
		}
	}

	public static final class ValueOf_B extends ValueOf_Int {

		public ValueOf_B(SymbolicEnvironment env) {
			super(env, Types.BOOLEAN_TO_STR_DESCRIPTOR);
		}
	}

}
