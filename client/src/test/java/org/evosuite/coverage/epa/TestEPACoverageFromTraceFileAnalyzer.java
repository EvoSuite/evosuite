package org.evosuite.coverage.epa;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Created by pantonio on 2/20/16.
 */
public class TestEPACoverageFromTraceFileAnalyzer {

	private static String SAMPLE1 = "1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS127()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS95()Z\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isPreviousEnabled()Z\n" +
			"1-isRemoveEnabled()Z\n" +
			"1-isSetEnabled()Z\n" +
			"1-isStateS119()Z\n" +
			"1-reportStateS119()V\n" +
			"1-reportState()V\n" +
			"1-<init>(Lar/uba/dc/listitr/MyArrayList;I)V\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS127()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS95()Z\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isPreviousEnabled()Z\n" +
			"1-isRemoveEnabled()Z\n" +
			"1-isSetEnabled()Z\n" +
			"1-isStateS119()Z\n" +
			"1-reportStateS119()V\n" +
			"1-reportState()V\n" +
			"1-hasNext()Z";

	private static String SAMPLE2 = "1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS127()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS95()Z\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isPreviousEnabled()Z\n" +
			"1-isRemoveEnabled()Z\n" +
			"1-isSetEnabled()Z\n" +
			"1-isStateS119()Z\n" +
			"1-reportStateS119()V\n" +
			"1-reportState()V\n" +
			"1-<init>(Lar/uba/dc/listitr/MyArrayList;I)V\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS127()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS95()Z\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isPreviousEnabled()Z\n" +
			"1-isRemoveEnabled()Z\n" +
			"1-isSetEnabled()Z\n" +
			"1-isStateS119()Z\n" +
			"1-reportStateS119()V\n" +
			"1-reportState()V\n" +
			"1-hasNext()Z\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS127()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isStateS95()Z\n" +
			"1-isAddEnabled()Z\n" +
			"1-isNextEnabled()Z\n" +
			"1-isPreviousEnabled()Z\n" +
			"1-isRemoveEnabled()Z\n" +
			"1-isSetEnabled()Z\n" +
			"1-isStateS119()Z\n" +
			"1-reportStateS119()V\n" +
			"1-reportState()V\n" +
			"1-hasPrevious()Z";

	private static String SAMPLE3 = "2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS127()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS95()Z\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isPreviousEnabled()Z\n" +
			"2-isRemoveEnabled()Z\n" +
			"2-isSetEnabled()Z\n" +
			"2-isStateS119()Z\n" +
			"2-reportStateS119()V\n" +
			"2-reportState()V\n" +
			"2-<init>(Lar/uba/dc/listitr/MyArrayList;I)V\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS127()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS95()Z\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isPreviousEnabled()Z\n" +
			"2-isRemoveEnabled()Z\n" +
			"2-isSetEnabled()Z\n" +
			"2-isStateS119()Z\n" +
			"2-reportStateS119()V\n" +
			"2-reportState()V\n" +
			"2-hasNext()Z\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS127()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS95()Z\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isPreviousEnabled()Z\n" +
			"2-isRemoveEnabled()Z\n" +
			"2-isSetEnabled()Z\n" +
			"2-isStateS119()Z\n" +
			"2-reportStateS119()V\n" +
			"2-reportState()V\n" +
			"2-hasPrevious()Z\n" +
			"2-checkForComodification()V\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS127()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isStateS95()Z\n" +
			"2-isAddEnabled()Z\n" +
			"2-isNextEnabled()Z\n" +
			"2-isPreviousEnabled()Z\n" +
			"2-isRemoveEnabled()Z\n" +
			"2-isSetEnabled()Z\n" +
			"2-isStateS119()Z\n" +
			"2-reportStateS119()V\n" +
			"2-reportState()V\n" +
			"2-add(Ljava/lang/Object;)V";

	private static String SAMPLE4 = "3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS127()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS95()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isPreviousEnabled()Z\n" +
			"3-isRemoveEnabled()Z\n" +
			"3-isSetEnabled()Z\n" +
			"3-isStateS119()Z\n" +
			"3-reportStateS119()V\n" +
			"3-reportState()V\n" +
			"3-<init>(Lar/uba/dc/listitr/MyArrayList;I)V\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS127()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS95()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isPreviousEnabled()Z\n" +
			"3-isRemoveEnabled()Z\n" +
			"3-isSetEnabled()Z\n" +
			"3-isStateS119()Z\n" +
			"3-reportStateS119()V\n" +
			"3-reportState()V\n" +
			"3-hasNext()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS127()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS95()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isPreviousEnabled()Z\n" +
			"3-isRemoveEnabled()Z\n" +
			"3-isSetEnabled()Z\n" +
			"3-isStateS119()Z\n" +
			"3-reportStateS119()V\n" +
			"3-reportState()V\n" +
			"3-hasNext()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS127()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS95()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isPreviousEnabled()Z\n" +
			"3-isRemoveEnabled()Z\n" +
			"3-isSetEnabled()Z\n" +
			"3-isStateS119()Z\n" +
			"3-reportStateS119()V\n" +
			"3-reportState()V\n" +
			"3-hasNext()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS127()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS95()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isPreviousEnabled()Z\n" +
			"3-isRemoveEnabled()Z\n" +
			"3-isSetEnabled()Z\n" +
			"3-isStateS119()Z\n" +
			"3-reportStateS119()V\n" +
			"3-reportState()V\n" +
			"3-hasNext()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS127()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isStateS95()Z\n" +
			"3-isAddEnabled()Z\n" +
			"3-isNextEnabled()Z\n" +
			"3-isPreviousEnabled()Z\n" +
			"3-isRemoveEnabled()Z\n" +
			"3-isSetEnabled()Z\n" +
			"3-isStateS119()Z\n" +
			"3-reportStateS119()V\n" +
			"3-reportState()V\n" +
			"3-hasNext()Z";

	@Test
	public void testEPACoverageFromSamples() throws IOException, SAXException, ParserConfigurationException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final EPA epa = EPAFactory.buildEPA(xmlFilename);

		final Stream<String> lines1 = Arrays.stream(SAMPLE1.split("\n"));
		final Map<String, List<String>> idToTraceMap1 = EPACoverageFromTraceFileAnalyzer.getIdToTraceMap(lines1);
		final float coverage1 = EPACoverageFromTraceFileAnalyzer.getCoverage(epa, idToTraceMap1.values());
		assertEquals((float)2/69, coverage1, 0.001);

		final Stream<String> lines2 = Arrays.stream(SAMPLE2.split("\n"));
		final Map<String, List<String>> idToTraceMap2 = EPACoverageFromTraceFileAnalyzer.getIdToTraceMap(lines2);
		final float coverage2 = EPACoverageFromTraceFileAnalyzer.getCoverage(epa, idToTraceMap2.values());
		assertEquals((float)3/69, coverage2, 0.001);

		final Stream<String> lines3 = Arrays.stream(SAMPLE3.split("\n"));
		final Map<String, List<String>> idToTraceMap3 = EPACoverageFromTraceFileAnalyzer.getIdToTraceMap(lines3);
		final float coverage3 = EPACoverageFromTraceFileAnalyzer.getCoverage(epa, idToTraceMap3.values());
		assertEquals((float)4/69, coverage3, 0.001);

		final Stream<String> lines4 = Arrays.stream(SAMPLE4.split("\n"));
		final Map<String, List<String>> idToTraceMap4 = EPACoverageFromTraceFileAnalyzer.getIdToTraceMap(lines4);
		final float coverage4 = EPACoverageFromTraceFileAnalyzer.getCoverage(epa, idToTraceMap4.values());
		assertEquals((float)2/69, coverage4, 0.001);
	}
}
