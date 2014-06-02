package org.evosuite.ga.problems;

import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * 
 * @author José Campos
 */
public interface Problem<T extends Chromosome>
{
	public List<FitnessFunction<T>> getFitnessFunctions();
}
