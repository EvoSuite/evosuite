package org.evosuite.statistics;

import java.util.Map;

import org.evosuite.ga.Chromosome;

/**
 * Simple dummy backend that just outputs all output variables to the console
 *  
 * @author gordon
 *
 */
public class ConsoleStatisticsBackend implements StatisticsBackend {

	@Override
	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data) {
		for(OutputVariable<?> var : data.values()) {
			System.out.println(var.getName()+": "+var.getValue());
		}

	}

}
