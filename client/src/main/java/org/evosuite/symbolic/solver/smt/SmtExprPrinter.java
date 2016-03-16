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
package org.evosuite.symbolic.solver.smt;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public final class SmtExprPrinter implements SmtExprVisitor<String, Void> {

	@Override
	public String visit(SmtIntConstant n, Void arg) {
		long longValue = n.getConstantValue();
		if (longValue < 0) {
			long absoluteValue = Math.abs(longValue);
			return "(- " + String.valueOf(absoluteValue) + ")";
		} else {
			return String.valueOf(longValue);
		}
	}

	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"################0.0################");

	@Override
	public String visit(SmtRealConstant n, Void arg) {
		double doubleVal = n.getConstantValue();
		if (doubleVal < 0) {
			String magnitudeStr = DECIMAL_FORMAT.format(Math.abs(doubleVal));
			return "(- " + magnitudeStr + ")";
		} else {
			String doubleStr = DECIMAL_FORMAT.format(doubleVal);
			return doubleStr;
		}
	}

	@Override
	public String visit(SmtStringConstant n, Void arg) {
		return "\"" + n.getConstantValue() + "\"";
	}

	@Override
	public String visit(SmtIntVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtRealVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtStringVariable n, Void arg) {
		String varName = n.getName();
		return varName;
	}

	@Override
	public String visit(SmtOperation n, Void arg) {
		List<String> retValues = new LinkedList<String>();
		for (SmtExpr argument : n.getArguments()) {
			String retValue = argument.accept(this, null);
			retValues.add(retValue);
		}

		StringBuffer result = new StringBuffer();
		if (!retValues.isEmpty()) {
			result.append("(");
		}
		result.append(n.getOperator().toString());
		for (String retValue : retValues) {
			result.append(" ");
			result.append(retValue);
		}
		if (!retValues.isEmpty()) {
			result.append(")");
		}
		return result.toString();
	}

	@Override
	public String visit(SmtBooleanConstant n, Void arg) {
		if (n.booleanValue()==true) {
			return "true";
		} else {
			return "false";
		}
	}

}
