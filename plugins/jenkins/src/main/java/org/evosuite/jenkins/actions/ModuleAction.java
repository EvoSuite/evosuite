package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.evosuite.continuous.ContinuousTestGeneration;
import org.evosuite.xsd.ProjectInfo;
import org.evosuite.xsd.TestSuite;

public class ModuleAction implements Action {

	private final AbstractBuild<?, ?> build;

	private final String name;
	private ProjectInfo projectInfo;
	private List<ClassAction> classes;

	public ModuleAction(AbstractBuild<?, ?> build, String name) {
		this.name = name;
		this.build = build;

		this.classes = new ArrayList<ClassAction>();
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return this.name;
	}

	@Override
	public String getUrlName() {
		return null;
	}

	public Object getDynamic(String token) {
		for (ClassAction c : this.classes) {
			if (c.getName().equals(token)) {
				return c;
			}
		}

		return null;
	}

	public AbstractBuild<?, ?> getBuild() {
		return this.build;
	}

	public String getName() {
		return this.name;
	}

	public ProjectInfo getProjectInfo() {
		return this.projectInfo;
	}

	public List<ClassAction> getClasses() {
		return this.classes;
	}

	/**
	 * 
	 * @param project_info
	 * @return
	 */
	public boolean build(Path project_info) {
		try {
			File tempfile = new File(project_info.toString());
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

			for (TestSuite suite : this.projectInfo.getGeneratedTestSuites()) {
				ClassAction c = new ClassAction(suite, this.getBuild());

				String fullPathOfTestSuite = suite.getCoverageTestSuites().get( suite.getCoverageTestSuites().size() - 1 ).getFullPathOfTestSuite();
				c.highlightSource(fullPathOfTestSuite);

				this.classes.add(c);
			}
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

	// data for jelly template

	/**
	 * 
	 * @return
	 */
	public int getNumberOfTestableClasses() {
		return this.projectInfo.getTotalNumberOfTestableClasses().intValue();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfStatements() {
		if (this.classes.isEmpty()) {
			return 0;
		}

		int statements = 0;
		for (ClassAction c : this.classes) {
			statements += c.getNumberOfStatements();
		}

		return (int) Math.round( ((double) statements) / ((double) this.classes.size()) );
	}

	/**
	 * 
	 * @return
	 */
	public int getTotalEffort() {
		if (this.classes.isEmpty()) {
			return 0;
		}

		int effort = 0;
		for (ClassAction c : this.classes) {
			effort += c.getTotalEffort();
		}

		return (int) Math.round( ((double) effort) / ((double) this.classes.size()) );
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfTests() {
		if (this.classes.isEmpty()) {
			return 0;
		}

		int tests = 0;
		for (ClassAction c : this.classes) {
			tests += c.getNumberOfTests();
		}

		return (int) Math.round( ((double) tests) / ((double) this.classes.size()) );
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getCriteria() {
		Set<String> criteria = new LinkedHashSet<String>();
		if (this.classes.isEmpty()) {
			return criteria;
		}

		for (ClassAction c : this.classes) {
			criteria.addAll(c.getCriteria());
		}

		return criteria;
	}

	/**
	 * 
	 * @return
	 */
	public double getOverallCoverage() {
		if (this.classes.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ClassAction c : this.classes) {
			coverage += c.getOverallCoverage();
		}

		return coverage / this.classes.size();
	}

	/**
	 * 
	 * @param criterionName
	 * @return
	 */
	public double getCriterionCoverage(String criterionName) {
		if (this.classes.isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		for (ClassAction c : this.classes) {
			coverage += c.getCriterionCoverage(criterionName);
		}

		return coverage / this.classes.size();
	}

	/**
	 * 
	 * @return
	 */
	public String getURL() {
		return this.name.replace(":", "$");
	}
}
