package org.evosuite.statistics;

import java.util.List;

public class ConsoleStatisticsBackend implements StatisticsBackend {

	@Override
	public void writeData(List<OutputVariable<?>> data) {
		for(OutputVariable<?> var : data) {
			System.out.println(var.getName()+": "+var.getValue());
		}

	}

}
