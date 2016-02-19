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
package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class NextAfter {

	private static final String NEXT_AFTER = "nextAfter";

	public final static class NextAfter_D extends SymbolicFunction {

		public NextAfter_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, NEXT_AFTER, Types.DD2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue nextAfterExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.NEXTAFTER;
				nextAfterExpr = new RealBinaryExpression(left, op, right, res);
			} else {
				nextAfterExpr = this.getSymbRealRetVal();
			}
			return nextAfterExpr;
		}

	}

	public static class NextAfter_F extends SymbolicFunction {

		public NextAfter_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, NEXT_AFTER, Types.FD2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue nextAfterExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.NEXTAFTER;
				nextAfterExpr = new RealBinaryExpression(left, op, right,
						(double) res);
			} else {
				nextAfterExpr = this.getSymbRealRetVal();
			}
			return nextAfterExpr;
		}
	}

}
