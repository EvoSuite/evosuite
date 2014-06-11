package org.evosuite.statistics.backend;

import java.util.Map;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.OutputVariable;

public interface StatisticsBackend {

	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data);
	
}
