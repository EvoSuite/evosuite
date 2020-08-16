/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

package org.evosuite.junit;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ReportGenerator;
import org.evosuite.utils.Utils;

/**
 * <p>
 * JUnitReportGenerator class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class JUnitReportGenerator extends ReportGenerator {

	private static final long serialVersionUID = -7460948425298766706L;

	private final List<Class<?>> classes;

	/**
	 * <p>
	 * Constructor for JUnitReportGenerator.
	 * </p>
	 * 
	 * @param coveredGoals
	 *            a int.
	 * @param totalGoals
	 *            a int.
	 * @param coverage
	 *            a {@link java.util.Set} object.
	 * @param classes
	 *            a {@link java.util.List} object.
	 * @param startTime
	 *            a long.
	 */
	public JUnitReportGenerator(int coveredGoals, int totalGoals, Set<Integer> coverage,
	        List<Class<?>> classes, long startTime, int tests) {
		this.classes = classes;
		StatisticEntry entry = new StatisticEntry();
		entry.total_goals = totalGoals;
		entry.covered_goals = coveredGoals;
		entry.coverage = coverage;
		entry.className = Properties.TARGET_CLASS;
		entry.id = getNumber(entry.className) + 1;
		entry.end_time = System.currentTimeMillis();
		entry.minimized_time = entry.end_time;
		entry.start_time = startTime;
		entry.size_final = tests;

		statistics.add(entry);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Write a file for a particular run
	 */
	@Override
	protected String writeRunPage(StatisticEntry run) {

		StringBuffer sb = new StringBuffer();
		writeHTMLHeader(sb, run.className);

		sb.append("<div id=\"header\"><div id=\"logo\">");
		sb.append("<h2>");
		sb.append(run.className);
		sb.append(": ");
		sb.append(String.format("%.2f", 100.0 * run.covered_goals / run.total_goals));
		sb.append("%");
		sb.append("</h2></div></div>\n");
		sb.append("<p><a href=\"../report-generation.html\">Overview</a></p>\n");

		// writeResultTable(sb, run);
		// writeMutationTable(sb);
		sb.append("<div id=\"page\"><div id=\"page-bgtop\"><div id=\"page-bgbtm\"><div id=\"content\">");
		sb.append("<div id=\"post\">");

		// Resulting test case
		sb.append("<h2 class=title id=tests>JUnit Test suites</h2>\n<div class=tests>\n");
		for (Class<?> clazz : classes) {
			// Source code
			try {
				Iterable<String> source = html_analyzer.getClassContent(clazz.getName());
				sb.append("<h3 class=title id=source>" + clazz.getName() + "</h3>\n");
				sb.append("<div class=source><p>");
				sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
				int linecount = 1;
				for (String line : source) {
					sb.append(String.format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
					                        linecount, linecount));
					sb.append(StringEscapeUtils.escapeHtml4(line));
					sb.append("\n");
					linecount++;
				}
				sb.append("</pre>\n");

				sb.append("</p></div>\n");
			} catch (Exception e) {
				// Don't display source if there is an error
				LoggingUtils.getEvoLogger().info("Cannot find class: " + clazz.getName());
			}
			//sb.append("</div>");
		}

		sb.append("</div></div>");

		sb.append("<div id=\"post\">");

		// Source code
		try {
			Iterable<String> source = html_analyzer.getClassContent(run.className);
			sb.append("<h2 class=title id=source>Source Code</h2>\n");
			sb.append("<div class=source><p>");
			sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
			int linecount = 1;
			for (String line : source) {
				sb.append(String.format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
				                        linecount, linecount));
				if (run.coverage.contains(linecount)) {
					sb.append("<span style=\"background-color: #ffffcc\">");
					sb.append(StringEscapeUtils.escapeHtml4(line));
					sb.append("</span>");
				}

				else
					sb.append(StringEscapeUtils.escapeHtml4(line));
				sb.append("\n");
				linecount++;
			}
			sb.append("</pre>\n");

			sb.append("</p></div>\n");
		} catch (Exception e) {
			// Don't display source if there is an error
		}
		sb.append("</div>");
		writeHTMLFooter(sb);

		String filename = "report-" + run.className + "-" + run.id + ".html";
		File file = new File(getReportDir().getAbsolutePath() + "/html/" + filename);
		Utils.writeFile(sb.toString(), file);
		// return file.getAbsolutePath();
		return filename;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
	 */
	/** {@inheritDoc} */
	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.utils.ReportGenerator#minimized(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void minimized(Chromosome result) {
		// TODO Auto-generated method stub

	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
