package org.evosuite.symbolic.solver.z3str2;

import org.evosuite.symbolic.solver.smt.SmtAssertion;
import org.evosuite.symbolic.solver.smt.SmtCheckSatQuery;
import org.evosuite.symbolic.solver.smt.SmtConstantDeclaration;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
import org.evosuite.symbolic.solver.smt.SmtFunctionDefinition;

class Z3Str2QueryPrinter {

	public String print(SmtCheckSatQuery smtQuery) {

		StringBuffer buff = new StringBuffer();
		buff.append("\n");

		for (SmtConstantDeclaration constantDeclaration : smtQuery
				.getConstantDeclarations()) {
			String str = String.format("(declare-const %s %s)",
					constantDeclaration.getConstantName(),
					constantDeclaration.getConstantSort());
			buff.append(str);
			buff.append("\n");
		}

		for (SmtFunctionDefinition smtFunctionDefinition : smtQuery
				.getFunctionDefinitions()) {
			String str = String.format("(define-fun %s)",
					smtFunctionDefinition.getFunctionDefinition());
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

		return buff.toString();
	}
}
