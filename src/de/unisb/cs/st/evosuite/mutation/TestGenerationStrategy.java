/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.mutation;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.FailedTestSet;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.javalanche.HOM.HOMSwitcher;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * Abstract test generation strategy class.
 * 
 * @author Gordon Fraser
 *
 */
public abstract class TestGenerationStrategy {

	protected static Logger logger = Logger.getLogger(TestGenerator.class);
	
	protected boolean only_live = Boolean.parseBoolean(System.getProperty("exclude.killed"));
	
	List<Mutation> mutants;
	
	public TestGenerationStrategy() {		
		//mutants = QueryManager.getMutationIdListFromDb(new HOMSwitcher().getNumMutants());
		mutants = new HOMSwitcher().getMutants();
		//suite = new MaximalFitnessTestSuite(mutants);
		//failed_tests = new TestSet();
	}
	
	abstract public void generateTests();

	abstract public void writeTestSuite(String filename, String directory);

	public void writeFailedTests(String filename, String directory) {
		FailedTestSet bugs = FailedTestSet.getInstance();
		bugs.writeTestSuite(filename, directory);
	}

	abstract public List<TestCase> getFailedTests();
	
	abstract public List<TestCase> getTests();

}
