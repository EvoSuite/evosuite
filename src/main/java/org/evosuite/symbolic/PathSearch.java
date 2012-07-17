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
 */
package org.evosuite.symbolic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.VMState;
import gov.nasa.jpf.search.Search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;


/**
 * <p>PathSearch class.</p>
 *
 * @author Jan Malburg
 */
public class PathSearch extends Search {

	private final Map<Integer, List<gov.nasa.jpf.Error>> errormap;

	/**
	 * <p>Constructor for PathSearch.</p>
	 *
	 * @param config a {@link gov.nasa.jpf.Config} object.
	 * @param vm a {@link gov.nasa.jpf.jvm.JVM} object.
	 * @param iterations a int.
	 */
	public PathSearch(Config config, JVM vm, int iterations) {
		super(config, vm);
		if (iterations <= 0) {
			throw new IllegalArgumentException();
		}
		this.numberOfIterations = iterations;
		errormap = new HashMap<Integer, List<gov.nasa.jpf.Error>>();
	}

	/**
	 * <p>Constructor for PathSearch.</p>
	 *
	 * @param config a {@link gov.nasa.jpf.Config} object.
	 * @param vm a {@link gov.nasa.jpf.jvm.JVM} object.
	 */
	public PathSearch(Config config, JVM vm) {
		this(config, vm, config.getInt("jm.numberOfIterations", 1));
	}

	/**
	 * <p>getErrorMap</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<Integer, List<gov.nasa.jpf.Error>> getErrorMap() {
		return errormap;
	}

	private final int numberOfIterations;
	private final long maxTime = Properties.TIMEOUT; // TODO: Configuration.MAX_TEST_EXECUTION_TIME_MSEC;
	@SuppressWarnings("unused")
	private long startTime;

	/** {@inheritDoc} */
	@Override
	public void search() {
		int path = 1;

		if (hasPropertyTermination()) {
			return;
		}

		//vm.forward();
		//RestorableVMState init_state = vm.getRestorableState();
		VMState init_state = vm.getState();

		notifySearchStarted();
		boolean termination;
		int lastIndex = 0;
		this.startTime = System.currentTimeMillis();
		TimeOutListener to = new TimeOutListener(this.maxTime);
		vm.addListener(to);
		while (true) {
			if (!(termination = hasPropertyTermination()) && hasNextState()
			        && !isEndState() && !to.isTimeOut()) {
				if (forward()) {
					notifyStateAdvanced();
				}
			} else {
				if (to.isTimeOut()) {
					//TODO TimeOut-Error
				}
				if (termination) {
					List<gov.nasa.jpf.Error> errors = getErrors();
					List<gov.nasa.jpf.Error> error = errors.subList(lastIndex,
					                                                errors.size());
					lastIndex = errors.size();
					errormap.put(path, error);
				}
				if (path >= numberOfIterations) {
					break;
				} else {
					path++;
				}
				properties = getProperties(config);
				vm.restoreState(init_state);
				notifyStateRestored();
				vm.resetNextCG();
			}
		}

		notifySearchFinished();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEndState() {
		//vm.restoreState don`t reset the original impl.
		return vm.isEndState();
	}

	/** {@inheritDoc} */
	@Override
	public boolean supportsBacktrack() {
		return false;
	}

}
