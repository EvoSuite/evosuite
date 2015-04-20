package org.evosuite.symbolic.vm.string.tokenizer;

import java.util.StringTokenizer;

import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.TokenizerExpr;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class HasMoreTokens extends SymbolicFunction {

	private static final String HAS_MORE_TOKENS = "hasMoreTokens";

	public HasMoreTokens(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_STRING_TOKENIZER, HAS_MORE_TOKENS,
				Types.TO_BOOLEAN_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		StringTokenizer conc_tokenizer = (StringTokenizer) this
				.getConcReceiver();
		NonNullReference symb_tokenizer = this.getSymbReceiver();

		boolean res = this.getConcBooleanRetVal();

		TokenizerExpr tokenizerExpr = (TokenizerExpr) env.heap.getField(
				Types.JAVA_UTIL_STRING_TOKENIZER,
				SymbolicHeap.$STRING_TOKENIZER_VALUE, conc_tokenizer,
				symb_tokenizer);

		if (tokenizerExpr != null && tokenizerExpr.containsSymbolicVariable()) {
			HasMoreTokensExpr hasMoreTokenExpr = new HasMoreTokensExpr(
					tokenizerExpr, (long) (res ? 1L : 0L));

			return hasMoreTokenExpr;

		}

		return this.getSymbIntegerRetVal();
	}
}
