
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.search.Search;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.symbolic.HashTableSet;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerComparison;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealComparison;
import org.evosuite.symbolic.expr.RealConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class PathConstraint {

	private static PathConstraint ins = null;

	private static Logger logger = LoggerFactory.getLogger(PathConstraint.class);

	private PathConstraint() {
		this.pathConstraints = new HashTableSet<Constraint<?>>();
		this.storedStateMap = new HashMap<Integer, HashTableSet<Constraint<?>>>();
	};

	/**
	 * <p>getInstance</p>
	 *
	 * @return a {@link org.evosuite.symbolic.bytecode.PathConstraint} object.
	 */
	public static PathConstraint getInstance() {
		if (ins == null) {
			throw new RuntimeException("PathConstraint not initalized");
		}
		return ins;
	}

	private HashTableSet<Constraint<?>> pathConstraints;
	private final Map<Integer, HashTableSet<Constraint<?>>> storedStateMap;

	/**
	 * <p>init</p>
	 */
	public static void init() {
		ins = new PathConstraint();
	}

	/**
	 * <p>getCurrentConstraints</p>
	 *
	 * @return a {@link org.evosuite.symbolic.HashTableSet} object.
	 */
	@SuppressWarnings("unchecked")
	public HashTableSet<Constraint<?>> getCurrentConstraints() {
		return (HashTableSet<Constraint<?>>) pathConstraints.clone();
	}

	/**
	 * <p>log</p>
	 */
	public void log() {
		logger.info("Working so far");
	}

	/**
	 * <p>addConstraint</p>
	 *
	 * @param c a {@link org.evosuite.symbolic.expr.Constraint} object.
	 */
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
			int value = ( (Number) c.getRightOperand().getConcreteValue()).intValue();
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
			
			//FIXME shouldn't value be Real here???
			int value = ((Number) c.getRightOperand().getConcreteValue()).intValue();
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
			int value = ((Number) c.getRightOperand().getConcreteValue()).intValue();
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
			
			//FIXME shouldn't value be Real here???
			int value = ((Number) c.getRightOperand().getConcreteValue()).intValue();
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
