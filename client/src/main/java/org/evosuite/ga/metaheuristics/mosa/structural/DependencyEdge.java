package org.evosuite.ga.metaheuristics.mosa.structural;

import org.evosuite.ga.FitnessFunction;
import org.jgrapht.graph.DefaultEdge;

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
