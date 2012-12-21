package org.evosuite.statistics;

import java.util.List;

public interface StatisticsBackend {

	public void writeData(List<OutputVariable<?>> data);
	
}
