package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.smt.SmtAssertion;
import org.evosuite.symbolic.solver.smt.SmtCheckSatQuery;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
import org.evosuite.symbolic.solver.smt.SmtFunctionDeclaration;
import org.evosuite.symbolic.solver.smt.SmtFunctionDefinition;

class CVC4QueryPrinter {

	private static final String CVC4_LOGIC = "QF_SLIRA";

	public String print(SmtCheckSatQuery smtQuery) {
		StringBuffer buff = new StringBuffer();
		buff.append("\n");
		buff.append("(set-logic " + CVC4_LOGIC + ")");
		buff.append("\n");
		buff.append("(set-option :produce-models true)");
		buff.append("\n");
		buff.append("(set-option :strings-exp true)");
		buff.append("\n");

		for (SmtFunctionDeclaration functionDeclaration : smtQuery
				.getFunctionDeclarations()) {
			String str = String.format("(declare-fun %s () %s)",
					functionDeclaration.getFunctionName(),
					functionDeclaration.getFunctionSort());
			buff.append(str);
			buff.append("\n");
		}

		for (SmtFunctionDefinition functionDeclaration : smtQuery
				.getFunctionDefinitions()) {
			String str = String.format("(define-fun %s)",
					functionDeclaration.getFunctionDefinition());
			buff.append(str);
			buff.append("\n");
		}

		SmtExprPrinter printer = new SmtExprPrinter();
		for (SmtAssertion smtAssertion : smtQuery.getAssertions()) {
			SmtExpr smtExpr = smtAssertion.getFormula();
			String smtExprStr = smtExpr.accept(printer, null);

			String str = String.format("(assert %s)", smtExprStr);
			buff.append(str);
			buff.append("\n");
		}

		buff.append("(check-sat)");
		buff.append("\n");

		buff.append("(get-model)");
		buff.append("\n");

		buff.append("(exit)");
		buff.append("\n");

		return buff.toString();

	}
}
