/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga.metaheuristics.mosa.structural;

import org.evosuite.ga.FitnessFunction;
import org.jgrapht.graph.DefaultEdge;

/**
 * 
 * 
 * @author Annibale Panichella
 */
public class DependencyEdge extends DefaultEdge{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//private static final Logger logger = LoggerFactory.getLogger(BranchFitnessGraph.class);

	public FitnessFunction<?> getSource(){
		return (FitnessFunction<?>) super.getSource();
	}
	
	public FitnessFunction<?> getTarget(){
		return (FitnessFunction<?>) super.getTarget();
	}

}
