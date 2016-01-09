package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.xml.sax.SAXException;

public class EPATransitionCoverageFactory extends AbstractFitnessFactory<EPATransitionCoverageTestFitness> {

	private final EPA epa;
	private final String className;

	public EPATransitionCoverageFactory(String className, String epaXMLFilename) {
		this.className = className;

		if (epaXMLFilename == null) {
			throw new IllegalArgumentException("epa XML Filename cannot be null");
		}
		try {
			this.epa = EPAFactory.buildEPA(epaXMLFilename);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}
	}

	@Override
	public List<EPATransitionCoverageTestFitness> getCoverageGoals() {
		return computeCoverageGoals();
	}

	private List<EPATransitionCoverageTestFitness> computeCoverageGoals() {
		List<EPATransitionCoverageTestFitness> coverage_goals = epa.getTransitions().stream()
				.map(t -> new EPATransitionCoverageTestFitness(new EPATransitionCoverageGoal(className, epa, t)))
				.collect(Collectors.toList());
		return coverage_goals;
	}
}
