package org.evosuite.coverage.epa;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EPACoverageFromTraceFileAnalyzer {
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		final String epaXMLfilePath = args[0];
		final String traceFilePath = args[1];
		final String resultFilePath = args[2];
		
		// Gather all different traces
		final Map<String, List<String>> idToTraceMap = getIdToTraceMap(Files.lines(Paths.get(traceFilePath)));
		
		EPA epa = EPAFactory.buildEPA(epaXMLfilePath);

		final Collection<List<String>> traces = idToTraceMap.values();
		final float coverage = getCoverage(epa, traces);

		final PrintWriter printWriter = new PrintWriter(resultFilePath);
		printWriter.println(coverage);
		printWriter.close();
	}

	public static float getCoverage(EPA epa, Collection<List<String>> traces) {
		final Set<EPATransition> tracedEpaTransitions = getTracedEPATransitions(epa, traces);
		final int epaTransitionSize = epa.getTransitions().size();
		return (float) tracedEpaTransitions.size() / epaTransitionSize;
	}

	private static Set<EPATransition> getTracedEPATransitions(EPA epa, Collection<List<String>> traces) {
		return traces.stream()
				.map(trace -> {
					try {
						return EPATraceFactory.buildEPATrace(trace, epa);
					} catch (MalformedEPATraceException e) {
						e.printStackTrace();
						return null;
					}
				})
				.map(EPATrace::getEpaTransitions)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	public static Map<String, List<String>> getIdToTraceMap(Stream<String> lines) throws IOException {
		final Map<String, List<String>> idToTrace = new HashMap<>();
		lines.forEach(line -> {
			final String[] splittedLine = line.split("-");
			final String id = splittedLine[0];
			final String call = splittedLine[1];
			List<String> trace = idToTrace.get(id);
			if (trace == null) {
				trace = new ArrayList<>();
				idToTrace.put(id, trace);
			}
			trace.add(call);
		});
		return idToTrace;
	}
}
