/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

public interface SmtExprVisitor<K, V> {

    K visit(SmtBooleanConstant n, V arg);

    K visit(SmtIntConstant n, V arg);

    K visit(SmtRealConstant n, V arg);

    K visit(SmtStringConstant n, V arg);

    K visit(SmtIntVariable n, V arg);

    K visit(SmtRealVariable n, V arg);

    K visit(SmtStringVariable n, V arg);

    K visit(SmtOperation n, V arg);

    K visit(SmtArrayVariable.SmtIntegerArrayVariable n, V arg);

    K visit(SmtArrayVariable.SmtRealArrayVariable n, V arg);

    K visit(SmtArrayVariable.SmtStringArrayVariable n, V arg);

    K visit(SmtArrayVariable.SmtReferenceArrayVariable n, V arg);

    K visit(SmtArrayConstant.SmtIntegerArrayConstant n, V arg);

    K visit(SmtArrayConstant.SmtRealArrayConstant n, V arg);

    K visit(SmtArrayConstant.SmtStringArrayConstant n, V arg);

    K visit(SmtArrayConstant.SmtReferenceArrayConstant n, V arg);

}
