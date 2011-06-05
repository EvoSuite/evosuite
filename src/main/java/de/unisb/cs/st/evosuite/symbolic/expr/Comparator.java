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

package de.unisb.cs.st.evosuite.symbolic.expr;

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

	public abstract Comparator not();

	public abstract Comparator swap();

	@Override
	public String toString() {
		return str;
	}
}