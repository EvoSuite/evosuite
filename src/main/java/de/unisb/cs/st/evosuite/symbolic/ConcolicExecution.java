/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of EvoSuite.
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

package de.unisb.cs.st.evosuite.symbolic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Gordon Fraser
 * 
 */
public class ConcolicExecution {

	private List<gov.nasa.jpf.Error> errors;

	private static Logger logger = Logger.getLogger(ConcolicExecution.class);

	private PathConstraintCollector pcg;

	public List<BranchCondition> executeConcolic(String targetName, String classPath) {
		logger.info("Setting up JPF");

		String[] strs = new String[0];
		Config config = JPF.createConfig(strs);
		config.setProperty("classpath", config.getProperty("classpath") + "," + classPath);
		config.setTarget(targetName);

		config.setProperty("vm.insn_factory.class",
		                   "de.unisb.cs.st.evosuite.symbolic.bytecode.IntegerConcolicInstructionFactory");
		config.setProperty("peer_packages",
		                   "de.unisb.cs.st.evosuite.symbolic.nativepeer,"
		                           + config.getProperty("peer_packages"));

		// We don't want JPF output
		config.setProperty("report.class",
		                   "de.unisb.cs.st.evosuite.symbolic.SilentReporter");

		//Configure the search class;
		config.setProperty("search.class", "de.unisb.cs.st.evosuite.symbolic.PathSearch");
		config.setProperty("jm.numberOfIterations", "1");

		//Generate the JPF Instance
		JPF jpf = new JPF(config);

		this.pcg = new PathConstraintCollector();
		jpf.getVM().addListener(pcg);
		jpf.getSearch().addListener(pcg);

		//Run the SUT
		logger.info("Running concolic execution");
		jpf.run();
		logger.info("Finished concolic execution");
		logger.info("Conditions collected: " + pcg.conditions.size());

		this.errors = jpf.getSearch().getErrors();

		return pcg.conditions;
	}
}
