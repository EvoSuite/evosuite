package org.evosuite.symbolic.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.evosuite.symbolic.expr.Constraint;

public final class ConstraintCache {

	private static final ConstraintCache instance = new ConstraintCache();

	private HashMap<Collection<Constraint<?>>, Map<String, Object>> cached_sat_constraints = new HashMap<Collection<Constraint<?>>, Map<String, Object>>();
	private int cached_sat_constraints_count = 0;

	private HashSet<Collection<Constraint<?>>> cached_unsat_constraints = new HashSet<Collection<Constraint<?>>>();
	private int cached_unsat_constraints_count = 0;

	public int getNumberOfUNSATs() {
		return cached_unsat_constraints_count;
	}

	public int getNumberOfSATs() {
		return cached_sat_constraints_count;
	}

	private int number_of_accesses = 0;
	private int number_of_hits = 0;

	private ConstraintCache() {
		/* empty constructor */
	}

	public static ConstraintCache getInstance() {
		return instance;
	}

	public void addUNSAT(Collection<Constraint<?>> unsat_constraints) {
		cached_unsat_constraints.add(unsat_constraints);
		cached_unsat_constraints_count++;
	}

	public void addSAT(Collection<Constraint<?>> sat_constraints,
			Map<String, Object> solution) {
		cached_sat_constraints.put(sat_constraints, solution);
		cached_sat_constraints_count++;
	}

	private boolean valid_cached_solution = false;
	private Map<String, Object> cached_solution = null;

	public boolean hasCachedResult(Collection<Constraint<?>> constraints) {
		number_of_accesses++;

		if (this.cached_sat_constraints.containsKey(constraints)) {
			valid_cached_solution = true;
			cached_solution = this.cached_sat_constraints.get(constraints);
			number_of_hits++;
			return true;
		}
		if (this.cached_unsat_constraints.contains(constraints)) {
			valid_cached_solution = true;
			cached_solution = null;
			number_of_hits++;
			return true;
		}

		valid_cached_solution = false;
		return false;

	}

	public double getHitRate() {
		return (double) this.number_of_hits / (double) this.number_of_accesses;
	}

	/**
	 * If not in cache returns IllegalArgumentException()
	 * 
	 * @param constraints
	 * @return
	 */
	public Map<String, Object> getCachedResult() {

		if (valid_cached_solution == false) {
			throw new IllegalArgumentException("The constraint is not cached!");
		}

		valid_cached_solution = false;
		return this.cached_solution;
	}

}
