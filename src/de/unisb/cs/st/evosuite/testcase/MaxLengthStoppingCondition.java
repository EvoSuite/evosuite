/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.testcase;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.StoppingCondition;

/**
 * Stop search when a maximum (average) length has been reached.
 * Used for experiments on length bloat.
 * 
 * @author Gordon Fraser
 *
 */
public class MaxLengthStoppingCondition extends StoppingCondition {

	private double average_length = 0.0;
	
	private final static double MAX_LENGTH = Properties.getPropertyOrDefault("max_length", 1000.0); 
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.ga.StoppingCondition#isFinished()
	 */
	@Override
	public boolean isFinished() {
		if(average_length >= MAX_LENGTH) 
			logger.info("Maximum average length reached, stopping");
		return average_length >= MAX_LENGTH;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.ga.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		average_length = 0.0;
	}

	public void iteration(List<Chromosome> population) {
		double avg = 0.0;
		for(Chromosome c : population) {
			avg += c.size();
		}
		average_length = avg / population.size();
	}
}
