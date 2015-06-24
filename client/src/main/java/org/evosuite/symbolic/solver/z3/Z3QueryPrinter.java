package org.evosuite.symbolic.solver.z3;

import org.evosuite.symbolic.solver.smt.SmtAssertion;
import org.evosuite.symbolic.solver.smt.SmtCheckSatQuery;
import org.evosuite.symbolic.solver.smt.SmtConstantDeclaration;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;

class Z3QueryPrinter {

	public String print(SmtCheckSatQuery smtQuery, long timeout) {

		StringBuffer buff = new StringBuffer();
		buff.append("(set-option :timeout " + timeout + ")");
		buff.append("\n");

		for (SmtConstantDeclaration constantDeclaration : smtQuery
				.getConstantDeclarations()) {
			String str = String.format("(declare-const %s %s)",
					constantDeclaration.getConstantName(),
					constantDeclaration.getConstantSort());
			buff.append(str);
			buff.append("\n");
		}

		SmtExprPrinter printer = new SmtExprPrinter();
		for (SmtAssertion assertionDeclaration : smtQuery.getAssertions()) {

			SmtExpr formula = assertionDeclaration.getFormula();
			String formulaStr = formula.accept(printer, null);
			String str = String.format("(assert %s)", formulaStr);
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
