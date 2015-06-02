package org.evosuite.jenkins.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.evosuite.continuous.ContinuousTestGeneration;
import org.evosuite.xsd.CriterionCoverage;
import org.evosuite.xsd.ProjectInfo;
import org.evosuite.xsd.TestSuite;
import org.evosuite.xsd.TestSuiteCoverage;

import hudson.model.Action;

public class ModuleAction implements Action {

	private final String name;
	private final Path path;
	private ProjectInfo projectInfo = null;

	public ModuleAction(String name, Path path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return this.name;
	}

	public String getURL() {
		return this.name.replace(":", "$");
	}

	public Path getPath() {
		return this.path;
	}

	public ProjectInfo getProjectInfo() {
		return this.projectInfo;
	}

	public boolean build() {
		try {
			File tempfile = new File(this.path.toString());
			InputStream stream = new FileInputStream(tempfile);

			JAXBContext jaxbContext = JAXBContext.newInstance(ProjectInfo.class);
			// the following statement does not compile on Eclipse because of
			// the issue JENKINS-28580 (more info at https://issues.jenkins-ci.org/browse/JENKINS-28580)
			// however, everything should work if compiled with maven
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(ContinuousTestGeneration.class.getResourceAsStream("/xsd/ctg_project_report.xsd")));
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setSchema(schema);
			this.projectInfo = (ProjectInfo) jaxbUnmarshaller.unmarshal(stream);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public String getIconFileName() {
		return "/plugin/evosuite-jenkins-plugin/icons/evosuite.png";
	}

	@Override
	public String getDisplayName() {
		return "EvoSuite Module Statistics";
	}

	@Override
	public String getUrlName() {
		return "evosuite-module";
	}

	public double getOverallCoverage() {
		if (this.projectInfo == null) {
			return 0.0;
		}
		return this.projectInfo.getOverallCoverage() * 100;
	}

	public Map<String, List<Double>> getCoverageValues() {
		Map<String, List<Double>> coverageValues = new LinkedHashMap<String, List<Double>>();

		for (TestSuite testSuite : this.projectInfo.getGeneratedTestSuites()) {
			// get the last coverage report
			TestSuiteCoverage testSuiteCoverage = testSuite.getCoverageTestSuites().get( testSuite.getCoverageTestSuites().size() - 1 );

			for (CriterionCoverage criterionCoverage : testSuiteCoverage.getCoverage()) {
				List<Double> coverages = new ArrayList<Double>();
				if (coverageValues.containsKey(criterionCoverage.getCriterion())) {
					coverages = coverageValues.get(criterionCoverage.getCriterion());
				}

				coverages.add(criterionCoverage.getCoverageValue());
				coverageValues.put(criterionCoverage.getCriterion(), coverages);
			}
		}

		return coverageValues;
	}

	// data for jelly template

	public int getNumberOfTestableClasses() {
		return this.projectInfo.getTotalNumberOfTestableClasses().intValue();
	}

	public int getNumberOfTests() {
		return this.projectInfo.getGeneratedTestSuites().size();
	}

	public int getNumberOfStatements() {
		int num_statements = 0;
		for (TestSuite suite : this.projectInfo.getGeneratedTestSuites()) {
			for (TestSuiteCoverage coverage_suite : suite.getCoverageTestSuites()) {
				num_statements += coverage_suite.getTotalNumberOfStatements().intValue();
			}
		}
		return num_statements;
	}

	public int getTotalEffort() {
		int total_effort = 0;
		for (TestSuite suite : this.projectInfo.getGeneratedTestSuites()) {
			total_effort += suite.getTotalEffortInSeconds().intValue();
		}
		return total_effort;
	}

	public Set<String> getCriterion() {
		Set<String> names = new LinkedHashSet<String>();
		for (TestSuite testSuite : this.projectInfo.getGeneratedTestSuites()) {
			for (TestSuiteCoverage testSuiteCoverage : testSuite.getCoverageTestSuites()) {
				for (CriterionCoverage criterionCoverage : testSuiteCoverage.getCoverage()) {
					names.add(criterionCoverage.getCriterion());
				}
			}
		}
		return names;
	}

	public double getCriterionCoverage(String criterion) {
		double coverage = 0.0;
		int count = 0;

		for (TestSuite testSuite : this.projectInfo.getGeneratedTestSuites()) {
			// get the last coverage report
			TestSuiteCoverage testSuiteCoverage = testSuite.getCoverageTestSuites().get( testSuite.getCoverageTestSuites().size() - 1 );

			for (CriterionCoverage criterionCoverage : testSuiteCoverage.getCoverage()) {
				if (criterionCoverage.getCriterion().equals(criterion)) {
					coverage += criterionCoverage.getCoverageValue();
					count++;
				}
			}
		}

		return coverage / count * 100.0;
	}
}
