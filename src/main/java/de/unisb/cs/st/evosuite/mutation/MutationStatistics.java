/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.mutation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.FileTerminal;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;

import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.SearchListener;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.javalanche.mutation.analyze.html.HtmlAnalyzer;
import de.unisb.cs.st.javalanche.mutation.properties.MutationProperties;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class MutationStatistics implements SearchListener {

	private static MutationStatistics instance = null;

	private final Logger logger = Logger.getLogger(MutationStatistics.class);

	private static final File REPORT_DIR = new File(
	        MutationProperties.OUTPUT_DIR + "/report");

	/**
	 * Statistics about one test generation run
	 * 
	 * @author Gordon Fraser
	 * 
	 */
	public class StatisticEntry {
		/** Run id */
		int id = 0;

		/** Target mutation for this run */
		Mutation mutation;

		/** Other mutants killed */
		List<Mutation> other_mutants = new ArrayList<Mutation>();

		/** Resulting test case */
		TestCase test = null;

		/** History of best fitness values */
		List<Double> fitness_history = new ArrayList<Double>();

		/** History of best test length */
		List<Integer> length_history = new ArrayList<Integer>();

		/** Statistics about the mutations applied during search */
		public Map<String, Integer> test_mutations = new HashMap<String, Integer>();

		double fitness = 0.0;
		boolean unreachable = false;
		boolean method_executed = false;
		boolean mutant_executed = false;
		boolean has_exception = false;
		boolean has_assertion = false;
		boolean is_killed = false;
		Throwable exception = null;
		int primitive_assertions = 0;
		int compare_assertions = 0;
		int string_assertions = 0;
		int inspector_assertions = 0;
		// TODO: Time, generations, individuals
	};

	public List<StatisticEntry> statistics = new ArrayList<StatisticEntry>();

	private final HtmlAnalyzer html_analyzer = new HtmlAnalyzer();

	private MutationStatistics() {

	}

	public static MutationStatistics getInstance() {
		if (instance == null) {
			instance = new MutationStatistics();
		}
		return instance;
	}

	/*
	 * private void checkEntry(Mutation m) {
	 * if(!statistics.containsKey(m.getId())) statistics.put(m.getId(), new
	 * StatisticEntry()); if(m.isKilled()) statistics.get(m.getId()).is_killed =
	 * true; }
	 * 
	 * public void setUnreachable(Mutation m) { checkEntry(m);
	 * statistics.get(m.getId()).unreachable = true; }
	 * 
	 * public void setAsserted(Mutation m) { checkEntry(m);
	 * statistics.get(m.getId()).has_assertion = true; }
	 * 
	 * public void setKilled(Mutation m) { checkEntry(m);
	 * statistics.get(m.getId()).is_killed = true; }
	 * 
	 * public boolean isKilled(Mutation m) { checkEntry(m); return
	 * statistics.get(m.getId()).is_killed; }
	 * 
	 * public boolean isAsserted(Mutation m) { checkEntry(m); return
	 * statistics.get(m.getId()).has_assertion; }
	 * 
	 * public boolean hasException(Mutation m) { checkEntry(m); return
	 * statistics.get(m.getId()).has_exception; }
	 */

	/*
	 * public void setException(Mutation m, Throwable t) { checkEntry(m);
	 * statistics.get(m).exception = t; }
	 * 
	 * public int getNumException() { int num = 0; for(StatisticEntry entry :
	 * statistics.values()) { if(entry.exception != null) num++; } return num; }
	 */

	/*
	 * public int getNumUnreachable() { int num = 0; for(StatisticEntry entry :
	 * statistics.values()) { if(entry.unreachable) num++; } return num; }
	 * 
	 * public int getNumFunctionExecutedButNotMutation() { int num = 0;
	 * for(StatisticEntry entry : statistics.values()) {
	 * if(entry.function_executed && !entry.mutant_executed) num++; } return
	 * num; }
	 * 
	 * public int getNumExecuted() { int num = 0; for(StatisticEntry entry :
	 * statistics.values()) { if(entry.mutant_executed) num++; } return num; }
	 * 
	 * public void setMutantExecuted(Mutation m) { checkEntry(m);
	 * statistics.get(m.getId()).mutant_executed = true; }
	 * 
	 * public void setFunctionExecuted(Mutation m) { checkEntry(m);
	 * statistics.get(m.getId()).function_executed = true; }
	 * 
	 * public void setFitness(Mutation m, double fitness) { checkEntry(m);
	 * statistics.get(m.getId()).fitness = fitness; if(fitness > 0.33)
	 * setFunctionExecuted(m); if(fitness > 0.66) setMutantExecuted(m);
	 * 
	 * }
	 * 
	 * public void addAssertion(Mutation m, Assertion a) { //checkEntry(m);
	 * setAsserted(m); if(a instanceof PrimitiveAssertion)
	 * statistics.get(m.getId()).primitive_assertions++; else if(a instanceof
	 * CompareAssertion) statistics.get(m.getId()).compare_assertions++; else
	 * if(a instanceof StringAssertion)
	 * statistics.get(m.getId()).string_assertions++; else if(a instanceof
	 * InspectorAssertion) statistics.get(m.getId()).inspector_assertions++; }
	 * 
	 * public void printStatistics() { for(Entry<Long,StatisticEntry> entry :
	 * statistics.entrySet()) { System.out.println("Mutation "+entry.getKey());
	 * /
	 * /.getId()+": "+entry.getKey().getClassName()+"."+entry.getKey().getMethodName
	 * ()+":"+entry.getKey().getLineNumber());
	 * System.out.println("  Fitness: "+entry.getValue().fitness);
	 * System.out.println("  Unreachable: "+entry.getValue().unreachable);
	 * if(entry.getValue().exception != null)
	 * System.out.println("  Exception raised: "
	 * +entry.getValue().exception.getMessage()); else
	 * System.out.println("  No exception raised");
	 * System.out.println("  Function executed: "
	 * +entry.getValue().function_executed);
	 * System.out.println("  Mutant executed: "
	 * +entry.getValue().mutant_executed);
	 * System.out.println("  Primitive assertions: "
	 * +entry.getValue().primitive_assertions);
	 * System.out.println("  Compare assertions: "
	 * +entry.getValue().compare_assertions);
	 * System.out.println("  String assertions: "
	 * +entry.getValue().string_assertions);
	 * System.out.println("  Inspector assertions: "
	 * +entry.getValue().inspector_assertions); } }
	 * 
	 * public void logStatistics() {
	 * 
	 * for(Entry<Long,StatisticEntry> entry : statistics.entrySet()) {
	 * if(entry.getValue().is_killed) continue; //int exception =
	 * entry.getValue().exception != null ? 1 : 0;
	 * logger.info("Mutation "+entry.getKey()+"," //:
	 * "+entry.getKey().getClassName()+"
	 * ."+entry.getKey().getMethodName()+":"+entry.getKey().getLineNumber()+","
	 * +entry.getValue().fitness+"," +entry.getValue().unreachable+","
	 * +entry.getValue().function_executed +","
	 * +entry.getValue().mutant_executed+"," +entry.getValue().has_exception);
	 * //
	 * logger.info("  Primitive assertions: "+entry.getValue().primitive_assertions
	 * );
	 * //logger.info("  Compare assertions: "+entry.getValue().compare_assertions
	 * );
	 * //logger.info("  String assertions: "+entry.getValue().string_assertions
	 * ); } }
	 */

	protected String writeIntegerChart(List<Integer> values, Long id,
	        String title) {
		File file = new File(REPORT_DIR, "statistics_" + title + "_" + id
		        + ".png");
		JavaPlot plot = new JavaPlot();
		GNUPlotTerminal terminal = new FileTerminal("png", REPORT_DIR
		        + "/statistics_" + title + "_" + id + ".png");
		plot.setTerminal(terminal);

		plot.set("xlabel", "\"Generation\"");
		plot.set("ylabel", "\"" + title + "\"");
		// plot.set("xrange", "[0:100]");
		// plot.set("yrange", "[0:]");

		int[][] data = new int[values.size()][2];
		for (int i = 0; i < values.size(); i++) {
			data[i][0] = i;
			data[i][1] = values.get(i);
		}

		plot.addPlot(data);
		PlotStyle stl = ((AbstractPlot) plot.getPlots().get(0)).getPlotStyle();
		stl.setStyle(Style.LINESPOINTS);
		plot.setKey(JavaPlot.Key.OFF);
		plot.plot();

		return file.getName();
	}

	protected String writeDoubleChart(List<Double> values, Long id, String title) {
		File file = new File(REPORT_DIR, "statistics_" + title + "_" + id
		        + ".png");
		JavaPlot plot = new JavaPlot();
		GNUPlotTerminal terminal = new FileTerminal("png", REPORT_DIR
		        + "/statistics_" + title + "_" + id + ".png");
		plot.setTerminal(terminal);

		plot.set("xlabel", "\"Generation\"");
		plot.set("ylabel", "\"" + title + "\"");
		// plot.set("xrange", "[0:100]");
		// plot.set("yrange", "[0:]");

		double[][] data = new double[values.size()][2];
		for (int i = 0; i < values.size(); i++) {
			data[i][0] = i;
			data[i][1] = values.get(i);
		}
		plot.addPlot(data);

		PlotStyle stl = ((AbstractPlot) plot.getPlots().get(0)).getPlotStyle();
		stl.setStyle(Style.LINESPOINTS);
		plot.setKey(JavaPlot.Key.OFF);

		plot.plot();

		return file.getName();
	}

	/**
	 * HTML header
	 */
	protected void writeHTMLHeader(StringBuffer buffer, String title) {
		buffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n");
		buffer.append("<html>\n");
		buffer.append("<head>\n");
		buffer.append("<title>\n");
		buffer.append(title);
		buffer.append("\n</title>\n");

		buffer.append("<link href=\"prettify.css\" type=\"text/css\" rel=\"stylesheet\" />\n");
		buffer.append("<script type=\"text/javascript\" src=\"prettify.js\"></script>\n");
		/*
		 * buffer.append(
		 * "<link type=\"text/css\" rel=\"stylesheet\" href=\"SyntaxHighlighter.css\"></link>\n"
		 * ); buffer.append(
		 * "<script language=\"javascript\" src=\"shCore.js\"></script>\n");
		 * buffer.append(
		 * "<script language=\"javascript\" src=\"shBrushJava.js\"></script>\n"
		 * ); buffer.append("<script language=\"javascript\">\n");
		 * buffer.append("dp.SyntaxHighlighter.HighlightAll('code');\n");
		 * buffer.append("</script>\n");
		 */
		buffer.append("</head>\n");
		buffer.append("<body onload=\"prettyPrint()\">\n");
	}

	/**
	 * HTML footer
	 */
	protected void writeHTMLFooter(StringBuffer buffer) {
		buffer.append("</body>\n");
		buffer.append("</html>\n");
	}

	/**
	 * Write a file for a particular run
	 * 
	 * @param run
	 */
	protected String writeRunPage(StatisticEntry run) {

		StringBuffer sb = new StringBuffer();
		writeHTMLHeader(sb, "Mutation " + run.mutation.getId());

		sb.append("<h2>Target Mutation: " + run.mutation.getId() + "</h2>\n");
		sb.append("<p><a href=\"report-generation.html\">Overview</a></p>\n");
		sb.append("<p>\n");
		sb.append("<ul>\n");
		sb.append("<li>Class: " + run.mutation.getClassName() + "\n");
		sb.append("<li>Method: " + run.mutation.getMethodName() + "\n");
		sb.append("<li>Line: <a href=\"#" + run.mutation.getLineNumber()
		        + "\">" + run.mutation.getLineNumber() + "</a>\n");
		sb.append("<li>Type: " + run.mutation.getMutationType() + "\n");
		sb.append("</ul>\n");
		sb.append("</p>\n");

		// Resulting test case
		sb.append("<h2>Test case</h2>\n");
		sb.append("<pre  class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">\n");
		if (run.test != null) {
			int linecount = 1;
			for (String line : run.test.toCode().split("\n")) {
				sb.append(String
				        .format("<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
				                linecount, linecount));
				linecount++;
				sb.append(StringEscapeUtils.escapeHtml(line));
				sb.append(";\n");
			}

		} else {
			sb.append("No test case generated");
		}
		sb.append("</pre>\n");

		// Source code
		Iterable<String> source = html_analyzer.getClassContent(run.mutation
		        .getClassName());
		sb.append("<h2>Mutated Code</h2>\n");
		sb.append("<p>");
		sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
		int line_num = run.mutation.getLineNumber() - 3;
		if (line_num < 0)
			line_num = 0;
		int linecount = 1;
		for (String line : source) {
			if (linecount >= line_num && linecount < (line_num + 6)) {
				sb.append(String.format("<span class=\"nocode\">%3d: </span>",
				        linecount));
				sb.append(StringEscapeUtils.escapeHtml(line));
				sb.append("\n");
			}
			linecount++;
		}
		sb.append("</pre>\n");

		// Chart of fitness
		if (run.fitness_history.isEmpty()) {
			sb.append("<h2>No fitness history</h2>\n");
		} else {
			String filename = writeDoubleChart(run.fitness_history,
			        run.mutation.getId(), "Fitness");
			sb.append("<h2>Fitness</h2>\n");
			sb.append("<p>");
			sb.append("<img src=\"");
			sb.append(filename);
			sb.append("\">");
			sb.append("</p>\n");
		}

		// Chart of length
		if (run.length_history.isEmpty()) {
			sb.append("<h2>No length history</h2>\n");
		} else {
			String filename = writeIntegerChart(run.length_history,
			        run.mutation.getId(), "Length");
			sb.append("<h2>Length</h2>\n");
			sb.append("<p>");
			sb.append("<img src=\"");
			sb.append(filename);
			sb.append("\">");
			sb.append("</p>\n");
		}

		// GA statistics
		sb.append("<h2>Search statistics: </h2>\n");
		sb.append("<p>\n");
		sb.append("<ul>\n");
		for (Entry<String, Integer> entry : run.test_mutations.entrySet()) {
			sb.append("<li>" + entry.getKey() + ": " + entry.getValue());
			sb.append("</li>\n");
		}
		sb.append("</ul>\n");
		sb.append("</p>\n");

		// Assertion statistics
		sb.append("<h2>Assertion statistics: </h2>\n");
		sb.append("<p>\n");
		sb.append("<ul>\n");
		// TODO
		sb.append("</ul>\n");
		sb.append("</p>\n");

		// Source code
		sb.append("<h2>Source Code</h2>\n");
		sb.append("<p>");
		sb.append("<pre class=\"prettyprint\" style=\"border: 1px solid #888;padding: 2px\">");
		linecount = 1;
		for (String line : source) {
			sb.append(String.format(
			        "<span class=\"nocode\"><a name=\"%d\">%3d: </a></span>",
			        linecount, linecount));
			linecount++;
			sb.append(StringEscapeUtils.escapeHtml(line));
			sb.append("\n");
		}
		sb.append("</pre>\n");

		sb.append("</p>\n");

		writeHTMLFooter(sb);

		String filename = "report-" + run.mutation.getId() + ".html";
		File file = new File(REPORT_DIR, filename);
		Io.writeFile(sb.toString(), file);
		return filename;
	}

	/**
	 * Write some overall stats
	 * 
	 * @param buffer
	 */
	protected void writeSummaryTable(StringBuffer buffer) {
		buffer.append("<h2>Summary:</h2>\n");
		buffer.append("<ul>\n");
		buffer.append("<li>Number of runs: ");
		buffer.append(statistics.size());
		buffer.append("\n");
		int num_tests = 0;
		int num_exception = 0;
		int num_no_assertion = 0;
		int num_executed = 0;
		int num = 0;
		for (StatisticEntry entry : statistics) {
			if (entry.has_exception)
				num_exception++;
			else if (entry.has_assertion)
				num_tests++;
			else if (entry.mutant_executed)
				num_no_assertion++;
			else if (entry.method_executed)
				num_executed++;
			else
				num++;
		}
		buffer.append("<li>Tests with assertion: ");
		buffer.append(num_tests);
		buffer.append("\n");
		buffer.append("<li>Tests without assertion: ");
		buffer.append(num_no_assertion);
		buffer.append("\n");
		buffer.append("<li>Mutants with exception: ");
		buffer.append(num_exception);
		buffer.append("\n");
		buffer.append("<li>Mutants not reached: ");
		buffer.append(num_executed);
		buffer.append("\n");
		buffer.append("<li>Mutant methods not executed: ");
		buffer.append(num);
		buffer.append("\n");
		// buffer.append("<li>Mutants killed: ");
		// buffer.append("\n");
		buffer.append("</ul>\n");
	}

	/**
	 * Write some overall stats
	 * 
	 * @param buffer
	 */
	protected void writeParameterTable(StringBuffer buffer) {
		buffer.append("<h2>Search Parameters:</h2>\n");
		buffer.append("<ul>\n");
		buffer.append("<li>Algorithm: " + System.getProperty("GA.algorithm")
		        + "\n"); // TODO
		buffer.append("<li>Length: " + Properties.CHROMOSOME_LENGTH + "\n"); // TODO
		buffer.append("<li>Generations: "
		        + System.getProperty("GA.generations") + "\n");

		buffer.append("<li>Elite: " + System.getProperty("GA.elite") + "\n");
		buffer.append("<li>Mutation rate: "
		        + System.getProperty("GA.mutation_rate") + "\n");
		buffer.append("<li>Crossover: "
		        + System.getProperty("GA.crossover_rate") + "\n");
		buffer.append("<li>Kin Compensation: "
		        + System.getProperty("GA.kincompensation") + "\n");

		buffer.append("</ul>\n");
	}

	/**
	 * The big table of results
	 * 
	 * @param buffer
	 */
	protected void writeRunTable(StringBuffer buffer) {
		buffer.append("<h2>Test generation runs:</h2>\n");
		buffer.append("<table border=1>");
		buffer.append("<tr>");
		buffer.append("<td>Run</td>");
		buffer.append("<td>Target Mutation</td>");
		buffer.append("<td>Location</td>");
		buffer.append("<td>Also killed</td>");
		buffer.append("<td>Status</td>");
		buffer.append("<td></td>");
		buffer.append("</tr>\n");

		for (StatisticEntry entry : statistics) {
			buffer.append("<tr>");
			buffer.append("<td>" + entry.id + "</td>");
			buffer.append("<td>" + entry.mutation.getId() + "</td>");
			buffer.append("<td>" + entry.mutation.getClassName() + "."
			        + entry.mutation.getMethodName() + ":"
			        + entry.mutation.getLineNumber() + "</td>");
			buffer.append("<td>");
			for (Mutation m : entry.other_mutants) {
				buffer.append(m.getId());
				buffer.append(" ");
			}
			buffer.append("</td>");

			if (entry.has_exception)
				buffer.append("<td>Dead by exception</td>");
			else if (entry.has_assertion)
				buffer.append("<td>Killed by assertion</td>");
			else if (entry.mutant_executed)
				buffer.append("<td>Executed without infection</td>");
			else if (entry.method_executed)
				buffer.append("<td>Mutant not reached</td>");
			else
				buffer.append("<td>Method not executed</td>");

			buffer.append("<td><a href=\"");
			String filename = writeRunPage(entry);
			buffer.append(filename);
			buffer.append("\">Details</a></td>");
			buffer.append("</tr>\n");
		}

		buffer.append("</table>");
	}

	/**
	 * The big table of results
	 * 
	 * @param buffer
	 */
	protected void writeMutationTable(StringBuffer buffer) {
		buffer.append("<h2>Mutant status:</h2>\n");

		buffer.append("<table border=1>");
		buffer.append("<tr>");
		buffer.append("<td>Mutation</td>");
		buffer.append("<td>Class</td>");
		buffer.append("<td>Method</td>");
		buffer.append("<td>Line</td>");
		buffer.append("<td>Type</td>");
		buffer.append("<td>Status</td>");
		buffer.append("<td>Killed in run</td>");
		buffer.append("</tr>\n");

		for (StatisticEntry entry : statistics) {
			buffer.append("<tr>");
			buffer.append("<td>" + entry.mutation.getId() + "</td>");
			buffer.append("<td>" + entry.mutation.getClassName() + "</td>");
			buffer.append("<td>" + entry.mutation.getMethodName() + "</td>");
			buffer.append("<td>" + entry.mutation.getLineNumber() + "</td>");
			buffer.append("<td>" + entry.mutation.getMutationType() + "</td>");
			buffer.append("<td>Status</td>");
			buffer.append("</tr>\n");
		}

		buffer.append("</table>");
	}

	private void copyFile(URL src, File dest) {
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

	private void copyFile(String name) {
		URL systemResource = ClassLoader.getSystemResource("report/" + name);
		logger.debug("Copying from resource: " + systemResource);
		copyFile(systemResource, new File(REPORT_DIR, name));
	}

	/**
	 * Write an HTML report
	 */
	public void writeReport() {
		REPORT_DIR.mkdirs();
		copyFile("prettify.js");
		copyFile("prettify.css");
		copyFile("detected.png");
		copyFile("not_detected.png");
		// copyFile("SyntaxHighlighter.css");
		// copyFile("shCore.js");
		// copyFile("shBrushJava.js");

		StringBuffer report = new StringBuffer();
		writeHTMLHeader(report, "muTest Report for "
		        + MutationProperties.PROJECT_PREFIX);

		report.append("<h1>muTest Report for "
		        + MutationProperties.PROJECT_PREFIX + "</h1>\n");
		// report.append("<h2>Run on "+System.getenv("USERNAME")+"@"+System.getenv("HOSTNAME")+"</h2>\n");

		try {
			report.append("<h2>Run on "
			        + java.net.InetAddress.getLocalHost().getHostName()
			        + "</h2>\n");
		} catch (UnknownHostException e) {
		}

		writeSummaryTable(report);
		writeParameterTable(report);

		writeRunTable(report);
		writeMutationTable(report);
		writeHTMLFooter(report);

		File file = new File(REPORT_DIR, "report-generation.html");
		Io.writeFile(report.toString(), file);
	}

	@Override
	public void iteration(List<Chromosome> population) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		entry.fitness_history.add(population.get(0).getFitness());
		entry.length_history.add(population.get(0).size());
	}

	@Override
	public void searchFinished(List<Chromosome> result) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		if (result instanceof TestChromosome) {
			TestChromosome best = (TestChromosome) result.get(0);
			entry.test = best.test;
			entry.has_exception = best.hasException();
			entry.has_assertion = best.test.hasAssertions();
		} else {
			entry.test = null;
		}
		// entry.mutant_executed = best.isMutantExecuted(); // TODO
		// entry.method_executed = best.isMethodExecuted(); // TODO
	}

	@Override
	public void searchStarted(FitnessFunction objective) {
		StatisticEntry entry = new StatisticEntry();
		if (objective instanceof MutationTestFitness) {
			MutationTestFitness fitness = (MutationTestFitness) objective;
			entry.mutation = fitness.getTargetMutation();
		} else {
			entry.mutation = null;
		}
		entry.id = statistics.size();

		statistics.add(entry);

	}

	public void addMutation(String name) {
		StatisticEntry entry = statistics.get(statistics.size() - 1);
		if (!entry.test_mutations.containsKey(name))
			entry.test_mutations.put(name, 1);
		else
			entry.test_mutations.put(name, entry.test_mutations.get(name) + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.SearchListener#fitnessEvaluation(de.unisb
	 * .cs.st.javalanche.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome result) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#mutation(de.unisb.cs.st.evosuite
	 * .ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}
}
