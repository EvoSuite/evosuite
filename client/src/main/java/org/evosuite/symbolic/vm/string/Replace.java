package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

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
			NonNullReference symb_receiver = this.getSymbReceiver();
			String conc_receiver = (String) this.getConcReceiver();

			// old char
			IntegerValue oldCharExpr = this.getSymbIntegerArgument(0);

			// new char
			IntegerValue newCharExpr = this.getSymbIntegerArgument(1);

			// return value
			Reference symb_ret_val = this.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			StringValue stringReceiverExpr = env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_receiver, symb_receiver, conc_receiver);

			if (symb_ret_val instanceof NonNullReference) {

				NonNullReference non_null_symb_ret_val = (NonNullReference) symb_ret_val;

				StringMultipleExpression symb_value = new StringMultipleExpression(
						stringReceiverExpr, Operator.REPLACEC, oldCharExpr,
						new ArrayList<Expression<?>>(Collections
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
			NonNullReference symb_receiver = this.getSymbReceiver();
			String conc_receiver = (String) this.getConcReceiver();

			// old string
			Reference symb_old_str = this.getSymbArgument(0);
			CharSequence conc_old_char_seq = (CharSequence) this
					.getConcArgument(0);

			// new string
			Reference symb_new_str = this.getSymbArgument(1);
			CharSequence conc_new_char_seq = (CharSequence) this
					.getConcArgument(1);

			// return value
			Reference symb_ret_val = this.getSymbRetVal();
			String conc_ret_val = (String) this.getConcRetVal();

			StringValue stringReceiverExpr = env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_receiver, symb_receiver, conc_receiver);

			if (symb_old_str instanceof NonNullReference
					&& symb_new_str instanceof NonNullReference
					&& symb_ret_val instanceof NonNullReference) {

				NonNullReference non_null_symb_old_str = (NonNullReference) symb_old_str;
				NonNullReference non_null_symb_new_str = (NonNullReference) symb_new_str;
				NonNullReference non_null_symb_ret_val = (NonNullReference) symb_ret_val;

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
							oldStringExpr, new ArrayList<Expression<?>>(
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
