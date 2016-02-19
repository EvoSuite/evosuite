/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.statistics.backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.OutputVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This statistics backend writes all (selected) output variables to a CSV file
 * 
 * @author gordon
 *
 */
public class CSVStatisticsBackend implements StatisticsBackend {

	private static Logger logger = LoggerFactory.getLogger(CSVStatisticsBackend.class);
	
	/**
	 * Retrieve header with variable names
	 * @param data
	 * @return
	 */
	private String getCSVHeader(Map<String, OutputVariable<?>> data) {
		StringBuilder r = new StringBuilder();
		Iterator<Entry<String, OutputVariable<?>>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, OutputVariable<?>> e = (Entry<String, OutputVariable<?>>)it.next();
			r.append(e.getKey());
			if (it.hasNext())
				r.append(",");
		}
		return r.toString();
	}


	/**
	 * Retrieve one line of data 
	 * @param data
	 * @return
	 */
	private String getCSVData(Map<String, OutputVariable<?>> data) {
		StringBuilder r = new StringBuilder();
		Iterator<Entry<String, OutputVariable<?>>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, OutputVariable<?>> e = (Entry<String, OutputVariable<?>>)it.next();
			r.append(e.getValue().getValue());
			if (it.hasNext())
				r.append(",");
		}
		return r.toString();
	}

	/**
	 * Return the folder of where reports should be generated.
	 * If the folder does not exist, try to create it
	 * 
	 * @return
	 * @throws RuntimeException if folder does not exist, and we cannot create it
	 */
	public static File getReportDir() throws RuntimeException{
		File dir = new File(Properties.REPORT_DIR);
		
		if(!dir.exists()){
			boolean created = dir.mkdirs();
			if(!created){
				String msg = "Cannot create report dir: "+Properties.REPORT_DIR;
				logger.error(msg);
				throw new RuntimeException(msg);
			}
		}
		
		return dir;			
	}
	
	@Override
	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data) {
		// Write to evosuite-report/statistics.csv
		try {
			File outputDir = getReportDir();			
			File f = new File(outputDir.getAbsolutePath() + File.separator + "statistics.csv");
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
