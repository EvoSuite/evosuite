package org.evosuite.statistics;

import java.util.List;

/**
 * Simple dummy backend that just outputs all output variables to the console
 *  
 * @author gordon
 *
 */
public class ConsoleStatisticsBackend implements StatisticsBackend {

	@Override
	public void writeData(List<OutputVariable<?>> data) {
		for(OutputVariable<?> var : data) {
			System.out.println(var.getName()+": "+var.getValue());
		}

	}

}
