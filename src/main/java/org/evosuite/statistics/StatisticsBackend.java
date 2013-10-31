package org.evosuite.statistics;

import java.util.Map;

import org.evosuite.ga.Chromosome;

public interface StatisticsBackend {

	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data);
	
}
