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
//
//Copyright (C) 2005 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//

package org.evosuite.symbolic.expr;

public enum Comparator {

	EQ(" == ") {
		@Override
		public Comparator not() {
			return NE;
		}

		@Override
		public Comparator swap() {
			return EQ;
		}
	},
	NE(" != ") {
		@Override
		public Comparator not() {
			return EQ;
		}

		@Override
		public Comparator swap() {
			return NE;
		}
	},
	LT(" < ") {
		@Override
		public Comparator not() {
			return GE;
		}

		@Override
		public Comparator swap() {
			return GT;
		}
	},
	LE(" <= ") {
		@Override
		public Comparator not() {
			return GT;
		}

		@Override
		public Comparator swap() {
			return GE;
		}
	},
	GT(" > ") {
		@Override
		public Comparator not() {
			return LE;
		}

		@Override
		public Comparator swap() {
			return LT;
		}
	},
	GE(" >= ") {
		@Override
		public Comparator not() {
			return LT;
		}

		@Override
		public Comparator swap() {
			return LE;
		}
	};

	private final String str;

	Comparator(String str) {
		this.str = str;
	}

	/**
	 * <p>
	 * not
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Comparator} object.
	 */
	public abstract Comparator not();

	/**
	 * <p>
	 * swap
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Comparator} object.
	 */
	public abstract Comparator swap();

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return str;
	}
}
