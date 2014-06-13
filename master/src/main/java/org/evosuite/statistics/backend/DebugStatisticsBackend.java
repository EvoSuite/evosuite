package org.evosuite.statistics.backend;

import java.util.LinkedHashMap;
import java.util.Map;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.OutputVariable;

/**
 * Backend to be used only for helping writing test cases
 * 
 * @author arcuri
 *
 */
public class DebugStatisticsBackend  extends ConsoleStatisticsBackend{

	private static Map<String, OutputVariable<?>> latestWritten;
	
	@Override
	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data) {
		super.writeData(result, data);
		latestWritten = new LinkedHashMap<>();
		latestWritten.putAll(data);
	}

	public static Map<String, OutputVariable<?>> getLatestWritten() {
		return latestWritten;
	}

}
