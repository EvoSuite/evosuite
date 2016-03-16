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

import java.util.HashSet;
import java.util.Set;

public final class SmtConstantCollector implements SmtExprVisitor<Void, Void> {

	@Override
	public Void visit(SmtIntConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}

	@Override
	public Void visit(SmtRealConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}

	@Override
	public Void visit(SmtStringConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}

	private final Set<SmtConstant> smtConstants = new HashSet<SmtConstant>();

	@Override
	public Void visit(SmtIntVariable n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtRealVariable n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtStringVariable n, Void arg) {
		return null;
	}

	@Override
	public Void visit(SmtOperation n, Void arg) {
		for (SmtExpr expr : n.getArguments()) {
			expr.accept(this, null);
		}
		return null;
	}

	public Set<SmtConstant> getSmtConstants() {
		return smtConstants;
	}

	@Override
	public Void visit(SmtBooleanConstant n, Void arg) {
		smtConstants.add(n);
		return null;
	}
}
