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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.HtmlAnalyzer;
import org.evosuite.utils.FileIOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLStatisticsBackend implements StatisticsBackend {

	protected static final Logger logger = LoggerFactory.getLogger(HTMLStatisticsBackend.class);
	
	protected static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	protected static final HtmlAnalyzer html_analyzer = new HtmlAnalyzer();

	@Override
	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data) {

		new File(getReportDir().getAbsolutePath() + "/img").mkdirs();
		new File(getReportDir().getAbsolutePath() + "/html/files/").mkdirs();
		new File(getReportDir().getAbsolutePath() + "/data/").mkdirs();
		new File(getReportDir().getAbsolutePath() + "/files/").mkdirs();

		copyFile("prettify.js");
		copyFile("prettify.css");
		copyFile("style.css");
		copyFile("foldButton.js");
		copyFile("foldButton.css");
		copyFile("jquery.js");
		copyFile("detected.png");
		copyFile("not_detected.png");
		copyFile("img01.jpg");
		copyFile("img02.jpg");
		copyFile("img03.jpg");
		copyFile("img04.png");
		copyFile("evosuite.png");
		File file = new File(getReportDir(), "report-generation.html");
		StringBuffer report = new StringBuffer();

		if (file.exists()) {
			List<String> lines = FileIOUtils.readFile(file);
			for (String line : lines) {
				if (line.contains("<!-- EVOSUITE INSERTION POINT -->")) {
					break;
				}
				report.append(line);
			}
		} else {

			writeHTMLHeader(report, Properties.PROJECT_PREFIX);
			report.append("<div id=\"header\">\n<div id=\"logo\">");
			/*
			if (!Properties.PROJECT_PREFIX.isEmpty()) {
				report.append("<h1 class=title>EvoSuite: " + Properties.PROJECT_PREFIX
				        + "</h1>\n");
			}
			*/
			report.append("\n</div><br></div>");
			try {
				report.append("Run on "
				        + java.net.InetAddress.getLocalHost().getHostName() + "\n");
			} catch (Exception e) {
			}

			report.append("<div id=\"page\">\n");
			report.append("<div id=\"page-bgtop\">\n");
			report.append("<div id=\"page-bgbtm\">\n");
			report.append("<div id=\"content\">\n");
			report.append("<div id=\"post\">");
			report.append("<h2 class=\"title\">Test generation runs:</h2>\n");
			report.append("<div style=\"clear: both;\">&nbsp;</div><div class=\"entry\">");
			report.append("<table cellspacing=0>"); // border=0 cellspacing=0 cellpadding=3>");
			report.append("<tr class=\"top bottom\">");
			// report.append("<td>Run</td>");
			report.append("<td>Date</td>");
			report.append("<td>Time</td>");
			report.append("<td>Coverage</td>");
			report.append("<td>Class</td>");
			// report.append("<td></td>");
			report.append("</tr>\n");
		}
		writeRunTable((TestSuiteChromosome)result, data, report);
		report.append("</div></div></div></div></div></div>");

		writeHTMLFooter(report);

		FileIOUtils.writeFile(report.toString(), file);
	}
	
	public static void copyFile(URL src, File dest) {
		try {
			InputStream in;
			in = src.openStream();
			OutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyFile(String name) {
		URL systemResource = ClassLoader.getSystemResource("report/" + name);
		logger.debug("Copying from resource: " + systemResource);
		copyFile(systemResource, new File(getReportDir(), "files" + File.separator + name));
		copyFile(systemResource, new File(getReportDir().getAbsolutePath()
				+ File.separator+ "html" + File.separator + "files" + File.separator + name));
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
	
	/**
	 * HTML header
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 * @param title
	 *            a {@link java.lang.String} object.
	 */
	public static void writeHTMLHeader(StringBuffer buffer, String title) {
		buffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n");
		buffer.append("<html>\n");
		buffer.append("<head>\n");
		buffer.append("<title>\n");
		buffer.append(title);
		buffer.append("\n</title>\n");

		buffer.append("<link href=\"files/prettify.css\" type=\"text/css\" rel=\"stylesheet\" />\n");
		buffer.append("<link href=\"files/style.css\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\" />\n");
		buffer.append("<script type=\"text/javascript\" src=\"files/prettify.js\"></script>\n");
		buffer.append("<script type=\"text/javascript\" src=\"files/jquery.js\"></script>\n");
		buffer.append("<script type=\"text/javascript\" src=\"files/foldButton.js\"></script>\n");
		buffer.append("<script type=\"text/javascript\">\n");
		buffer.append("  $(document).ready(function() {\n");
		//buffer.append("    $('div.tests').foldButton({'closedText':'open TITLE' });\n");
		//buffer.append("    $('div.source').foldButton({'closedText':'open TITLE' });\n");
		//buffer.append("    $('div.statistics').foldButton({'closedText':'open TITLE' });\n");
		buffer.append("    $('H2#tests').foldButton();\n");
		buffer.append("    $('H2#source').foldButton();\n");
		buffer.append("    $('H2#parameters').foldButton();\n");
		buffer.append("  });");
		buffer.append("</script>\n");
		buffer.append("<link href=\"files/foldButton.css\" rel=\"stylesheet\" type=\"text/css\">\n");
		buffer.append("</head>\n");
		buffer.append("<body onload=\"prettyPrint()\">\n");
		buffer.append("<div id=\"wrapper\">\n");
		buffer.append("<img src=\"files/evosuite.png\" height=\"40\"/>\n");
	}

	/**
	 * HTML footer
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 */
	public static void writeHTMLFooter(StringBuffer buffer) {
		buffer.append("</div>\n");
		buffer.append("</body>\n");
		buffer.append("</html>\n");
	}
	
	/**
	 * The big table of results
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 */
	protected void writeRunTable(TestSuiteChromosome suite, Map<String, OutputVariable<?>> data, StringBuffer buffer) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

		buffer.append("<tr>");
		// buffer.append("<td>" + entry.id + "</td>");
		buffer.append("<td>");
		buffer.append(sdf.format(new Date()));
		buffer.append("</td>");
		buffer.append("<td>");
		if (data.containsKey(RuntimeVariable.Total_Time.name())) {
			long duration = (Long)data.get(RuntimeVariable.Total_Time.name()).getValue() / 1000L;
			buffer.append(String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60)));
		} else
			buffer.append("UNKNOWN");
		buffer.append("</td>");
		buffer.append("<td>");
		Double coverage = (Double)getOutputVariableValue(data, RuntimeVariable.Coverage.name());  
		buffer.append((coverage != null) ? NumberFormat.getPercentInstance().format(coverage) : "UNKNOWN");
		buffer.append("</td>");
		buffer.append("<td><a href=\"html/");
		String filename = writeRunPage(suite, data);
		buffer.append(filename);
		buffer.append("\">");
		buffer.append(data.get("TARGET_CLASS").getValue());
		buffer.append("</tr>\n");
		
		buffer.append("<!-- EVOSUITE INSERTION POINT -->\n");
		buffer.append("<tr class=\"top\"><td colspan=\"3\">&nbsp;<td></tr>\n");
		buffer.append("</table>");
	}
	
	/**
	 * Write a file for a particular run
	 * 
	 * @param run
	 *            a {@link org.evosuite.utils.ReportGenerator.StatisticEntry}
	 *            object.
	 * @return a {@link java.lang.String} object.
	 */
	@SuppressWarnings("deprecation")
	protected String writeRunPage(TestSuiteChromosome suite, Map<String, OutputVariable<?>> data) {

		StringBuffer sb = new StringBuffer();
		String className = (String)data.get("TARGET_CLASS").getValue();
		writeHTMLHeader(sb, className);
		
		sb.append("<br><br><h2 class=title>Summary</h2>\n");
		
		sb.append("<ul><li>Target class: ");
		sb.append(getOutputVariableValue(data, "TARGET_CLASS"));
		sb.append(": ");
		sb.append(suite.getCoverage());
		sb.append("</ul>\n");

		writeResultTable(suite, sb, data);
		
		// writeMutationTable(sb);
		sb.append("<div id=\"page\">\n");
		sb.append("<div id=\"page-bgtop\">\n");
		sb.append("<div id=\"page-bgbtm\">\n");
		sb.append("<div id=\"content\">\n");
		
		sb.append("<div id=\"post\">\n");

		// Resulting test case
		sb.append("<h2 class=title id=tests>Test suite</h2>\n");
		sb.append("<div class=tests>\n");
		int num = 0;
		
		for (TestChromosome testChromosome : suite.getTestChromosomes()) {
			TestCase test = testChromosome.getTestCase();
			sb.append("<h3>Test case ");
			sb.append(++num);
			sb.append("</h3>\n");
			/*
			 * if(test.exceptionThrown != null) { sb.append("<p>Raises:");
			 * sb.append(test.exceptionThrown); sb.append("</p>"); }
			 */
			sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">\n");
			int linecount = 1;
			String code = null;
			if (testChromosome.getLastExecutionResult() != null) {
				code = test.toCode(testChromosome.getLastExecutionResult().exposeExceptionMapping());
			}
			else
				code = test.toCode();

			for (String line : code.split("\n")) {
				sb.append(String.format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
						linecount, linecount));
				/*
				 * if(test.exceptionsThrown != null &&
				 * test.exception_statement == test_line)
				 * sb.append("<span style=\"background: #FF0000\">");
				 */
				sb.append(StringEscapeUtils.escapeHtml4(line));
				/*
				 * if(test.exceptionThrown != null &&
				 * test.exception_statement == test_line)
				 * sb.append("</span>");
				 */
				linecount++;
				sb.append("\n");
			}
			sb.append("</pre>\n");
		}
		sb.append("</div>");
		sb.append("<div id=\"post\">\n");

		OutputVariable<?> ov_covered_lines = data.get(RuntimeVariable.Covered_Lines.name()); 
		@SuppressWarnings("unchecked")
		Set<Integer> coveredLines = (ov_covered_lines != null) ? (Set<Integer>) ov_covered_lines.getValue() : new HashSet<Integer>();
				
		// Source code
		try {
			Iterable<String> source = html_analyzer.getClassContent(className);
			sb.append("<h2 class=title id=source>Source Code</h2>\n");
			sb.append("<div class=source>\n");
			sb.append("<p>");
			sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
			int linecount = 1;
			for (String line : source) {
				sb.append(String.format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
				                        linecount, linecount));
				if (coveredLines.contains(linecount)) {
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
			sb.append("</p>\n");
		} catch (Exception e) {
			// Don't display source if there is an error
		}
		sb.append("</div>\n");
		sb.append("<div id=\"post\">\n");
		writeParameterTable(sb, data);
		sb.append("</div>\n");
		sb.append("<p><br><a href=\"../report-generation.html\">Back to Overview</a></p>\n");
		writeHTMLFooter(sb);

		String filename = "report-" + className + "-" + getNumber(className) + ".html";
		File file = new File(getReportDir().getAbsolutePath() + "/html/" + filename);
		FileIOUtils.writeFile(sb.toString(), file);
		// return file.getAbsolutePath();
		return filename;
	}
	
	protected int getNumber(final String className) {
		int num = 0;
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// report-ncs.Triangle-0.html
				return name.startsWith("report-" + className)
				        && (name.endsWith(".html"));
			}
		};
		List<String> filenames = new ArrayList<String>();

		File[] files = (new File(getReportDir().getAbsolutePath() + "/html")).listFiles(filter);
		if (files != null) {
			for (File f : files)
				filenames.add(f.getName());
			while (filenames.contains("report-" + className + "-" + num + ".html"))
				num++;
		}

		return num;
	}


	protected Object getOutputVariableValue(Map<String, OutputVariable<?>> data, String key) {
		OutputVariable<?> ov = data.get(key);
		return (ov != null) ? ov.getValue() : null;
	}


	/**
	 * Write some overall stats
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 * @param entry
	 *            a {@link org.evosuite.utils.ReportGenerator.StatisticEntry}
	 *            object.
	 */
	protected void writeResultTable(TestSuiteChromosome suite, StringBuffer buffer, Map<String, OutputVariable<?>> data) {

		//buffer.append("<h2>Statistics</h2>\n");
		buffer.append("<ul>\n");

		buffer.append("<li>");
		buffer.append(suite.getFitness());
		buffer.append(" fitness evaluations, "); 
		buffer.append(suite.getAge());
		buffer.append(" generations, "); 
		buffer.append(getOutputVariableValue(data, RuntimeVariable.Statements_Executed.name()));
		buffer.append(" statements, "); 
		buffer.append(suite.size());
		buffer.append(" tests.\n");

		/*
		long duration_GA = (entry.end_time - entry.start_time) / 1000;
		long duration_MI = (entry.minimized_time - entry.end_time) / 1000;
		long duration_TO = (entry.minimized_time - entry.start_time) / 1000;

		buffer.append("<li>Time: "
		        + String.format("%d:%02d:%02d", duration_TO / 3600,
		                        (duration_TO % 3600) / 60, (duration_TO % 60)));

		buffer.append("(Search: "
		        + String.format("%d:%02d:%02d", duration_GA / 3600,
		                        (duration_GA % 3600) / 60, (duration_GA % 60)) + ", ");
		buffer.append("minimization: "
		        + String.format("%d:%02d:%02d", duration_MI / 3600,
		                        (duration_MI % 3600) / 60, (duration_MI % 60)) + ")\n");
*/

		buffer.append("<li>Covered " + getOutputVariableValue(data, RuntimeVariable.Covered_Branches.name()) + "/"
		        + getOutputVariableValue(data, RuntimeVariable.Total_Branches.name()) + " branches, ");
		buffer.append("<li>Covered "+ getOutputVariableValue(data, RuntimeVariable.Covered_Methods.name()) + "/" 
		        + getOutputVariableValue(data, RuntimeVariable.Total_Methods.name()) + " methods, ");
		buffer.append("<li>Covered "+ getOutputVariableValue(data, RuntimeVariable.Covered_Goals.name()) + "/" 
		        + getOutputVariableValue(data, RuntimeVariable.Total_Goals.name()) + " total goals\n");
		if(data.containsKey(RuntimeVariable.MutationScore.name()))
				buffer.append("<li>Mutation score: "
						+ NumberFormat.getPercentInstance().format((Double)data.get(RuntimeVariable.MutationScore.name()).getValue()) + "\n");

		buffer.append("</ul>\n");
	}
	
	/**
	 * Write some overall stats
	 * 
	 * @param buffer
	 *            a {@link java.lang.StringBuffer} object.
	 * @param entry
	 *            a {@link org.evosuite.utils.ReportGenerator.StatisticEntry}
	 *            object.
	 */
	protected void writeParameterTable(StringBuffer buffer, Map<String, OutputVariable<?>> data) {
		buffer.append("<h2 id=parameters>EvoSuite Parameters</h2>\n");
		buffer.append("<div class=statistics><ul>\n");
		for (String key : data.keySet()) {
			buffer.append("<li>" + key + ": " + data.get(key).getValue() + "\n");
		}
		buffer.append("</ul></div>\n");

	} 
}
