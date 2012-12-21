package org.evosuite.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVStatisticsBackend implements StatisticsBackend {

	private static Logger logger = LoggerFactory.getLogger(CSVStatisticsBackend.class);
	
	private String getCSVHeader(List<OutputVariable<?>> data) {
		StringBuilder r = new StringBuilder();

		if (!data.isEmpty()) {
			r.append(data.get(0).getName());
		}

		for (int i = 1; i < data.size(); i++) {
			r.append(",");
			r.append(data.get(i).getName());
		}

		return r.toString();
	}

	private String getCSVData(List<OutputVariable<?>> data) {
		StringBuilder r = new StringBuilder();

		if (!data.isEmpty()) {
			r.append(data.get(0).getValue());
		}

		for (int i = 1; i < data.size(); i++) {
			r.append(",");
			r.append(data.get(i).getValue());
		}

		return r.toString();
	}

	@Override
	public void writeData(List<OutputVariable<?>> data) {
		// Write to evosuite-report/statistics.csv
		try {
			File f = new File(Properties.REPORT_DIR + File.separator + "statistics.csv");
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			if (f.length() == 0L) {
				out.write(getCSVHeader(data) + "\n");
			}
			out.write(getCSVData(data) + "\n");
			out.close();

		} catch (IOException e) {
			logger.warn("Error while writing statistics: " + e.getMessage());
		}
	}


}
