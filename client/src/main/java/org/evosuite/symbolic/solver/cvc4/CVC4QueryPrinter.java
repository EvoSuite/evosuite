/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.smt.SmtAssertion;
import org.evosuite.symbolic.solver.smt.SmtCheckSatQuery;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
import org.evosuite.symbolic.solver.smt.SmtFunctionDeclaration;
import org.evosuite.symbolic.solver.smt.SmtFunctionDefinition;

class CVC4QueryPrinter {

	private static final String CVC4_LOGIC = "SLIRA"; // previously QF_SLIRA

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
