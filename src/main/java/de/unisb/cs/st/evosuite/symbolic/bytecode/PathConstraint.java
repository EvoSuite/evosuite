/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of EvoSuite, but based on the SymbC extension of JPF
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.search.Search;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.symbolic.HashTableSet;
import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.RealComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstraint;

public class PathConstraint {

	private static PathConstraint ins = null;

	private static Logger logger = Logger.getLogger(PathConstraint.class);

	private PathConstraint() {
		this.pathConstraints = new HashTableSet<Constraint<?>>();
		this.storedStateMap = new HashMap<Integer, HashTableSet<Constraint<?>>>();
	};

	public static PathConstraint getInstance() {
		if (ins == null) {
			throw new RuntimeException("PathConstraint not initalized");
		}
		return ins;
	}

	private HashTableSet<Constraint<?>> pathConstraints;
	private final HashMap<Integer, HashTableSet<Constraint<?>>> storedStateMap;

	public static void init() {
		logger.info("Setting up path constraints");
		ins = new PathConstraint();
	}

	@SuppressWarnings("unchecked")
	public HashTableSet<Constraint<?>> getCurrentConstraints() {
		return (HashTableSet<Constraint<?>>) pathConstraints.clone();
	}

	public void log() {
		logger.info("Working so far");
	}

	public void addConstraint(Constraint<?> c) {
		if (c != null) {
			Constraint<?> c_opt = removeCMPFormConstraint(c);
			pathConstraints.add(c_opt);
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * Called from Dummy
	 * 
	 * @param search
	 *            the search object
	 */
	@SuppressWarnings("unchecked")
	protected void stateStored(Search search) {
		int i = JVM.getVM().getStateId();
		storedStateMap.put(i, (HashTableSet<Constraint<?>>) this.pathConstraints.clone());
	}

	/**
	 * Called from Dummy
	 * 
	 * @param search
	 *            the search object
	 */
	@SuppressWarnings("unchecked")
	protected void stateRestored(Search search) {
		int i = JVM.getVM().getStateId();
		HashTableSet<Constraint<?>> restore = storedStateMap.get(i);
		if (restore == null) {
			throw new RuntimeException("tried to restore a not stored state");
		}
		this.pathConstraints = (HashTableSet<Constraint<?>>) restore.clone();
	}

	private Constraint<?> removeCMPFormConstraint(Constraint<?> c) {
		if (c.getLeftOperand() instanceof IntegerComparison) {
			IntegerComparison cmp = (IntegerComparison) c.getLeftOperand();
			int value = c.getRightOperand().getConcreteValue().intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value < 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		} else if (c.getLeftOperand() instanceof RealComparison) {
			RealComparison cmp = (RealComparison) c.getLeftOperand();
			int value = c.getRightOperand().getConcreteValue().intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value < 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value < 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		} else if (c.getRightOperand() instanceof IntegerComparison) {
			IntegerComparison cmp = (IntegerComparison) c.getRightOperand();
			int value = c.getRightOperand().getConcreteValue().intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value > 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new IntegerConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		} else if (c.getRightOperand() instanceof RealComparison) {
			RealComparison cmp = (RealComparison) c.getRightOperand();
			int value = c.getRightOperand().getConcreteValue().intValue();
			Comparator op = c.getComparator();
			switch (op) {
			case EQ:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.EQ,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case NE:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.NE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case LE:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			case LT:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LT,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.LE,
					        cmp.getRightOperant()));
				}
			case GE:
				if (value > 0) {
					throw new RuntimeException("Unexpected Constraint");
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				}
			case GT:
				if (value > 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GE,
					        cmp.getRightOperant()));
				} else if (value == 0) {
					return (new RealConstraint(cmp.getLeftOperant(), Comparator.GT,
					        cmp.getRightOperant()));
				} else {
					throw new RuntimeException("Unexpected Constraint");
				}
			}
		}
		return (c);
	}
}
