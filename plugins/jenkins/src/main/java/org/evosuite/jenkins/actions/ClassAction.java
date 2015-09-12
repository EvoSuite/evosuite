/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.xsd.CriterionCoverage;
import org.evosuite.xsd.TestSuite;
import org.evosuite.xsd.TestSuiteCoverage;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

public class ClassAction implements Action {

	private final AbstractBuild<?, ?> build;

	private final TestSuite suite;
	private String testSourceCode;

	public ClassAction(TestSuite suite, AbstractBuild<?, ?> build) {
		this.build = build;

		this.suite = suite;
		this.testSourceCode = "";
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return this.suite.getFullNameOfTargetClass();
	}

	@Override
	public String getUrlName() {
		return null;
	}

	public AbstractBuild<?, ?> getBuild() {
		return this.build;
	}

	public TestSuite getSuite() {
		return this.suite;
	}

	public String getName() {
		return this.suite.getFullNameOfTargetClass();
	}

	public String getTestSuiteName() {
		return this.suite.getFullNameOfTestSuite();
	}

	public void highlightSource(final String javafile) throws IOException {
		InputStream file = new FileInputStream(new File(javafile));
		JavaSource source = new JavaSourceParser().parse(new InputStreamReader(file, Charset.forName("UTF-8")));

		JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
		options.setShowLineNumbers(true);
		options.setAddLineAnchors(true);

		JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
		StringWriter writer = new StringWriter();
		converter.convert(source, options, writer);

		this.testSourceCode = writer.toString();
	}

	// data for jelly template

	/**
	 * 
	 * @return
	 */
	public int getNumberOfStatements() {
		if (this.suite.getCoverageTestSuites().isEmpty()) {
			return 0;
		}

		TestSuiteCoverage suiteCoverage = this.suite.getCoverageTestSuites().get( this.suite.getCoverageTestSuites().size() - 1 );
		return suiteCoverage.getTotalNumberOfStatements().intValue();
	}

	/**
	 * 
	 * @return
	 */
	public int getTotalEffort() {
		if (this.suite.getCoverageTestSuites().isEmpty()) {
			return 0;
		}

		return suite.getTotalEffortInSeconds().intValue();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumberOfTests() {
		if (this.suite.getCoverageTestSuites().isEmpty()) {
			return 0;
		}

		TestSuiteCoverage suiteCoverage = this.suite.getCoverageTestSuites().get( this.suite.getCoverageTestSuites().size() - 1 );
		return suiteCoverage.getNumberOfTests().intValue();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getTestSourceCode() throws IOException {
		return this.testSourceCode;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getCriteria() {
		Set<String> criteria = new LinkedHashSet<String>();
		if (this.suite.getCoverageTestSuites().isEmpty()) {
			return criteria;
		}

		for (TestSuiteCoverage suiteCoverage : this.suite.getCoverageTestSuites()) {
			for (CriterionCoverage criterionCoverage : suiteCoverage.getCoverage()) {
				criteria.add(criterionCoverage.getCriterion());
			}
		}

		return criteria;
	}

	/**
	 *  
	 * 
	 * @return
	 */
	public double getOverallCoverage() {
		if (this.suite.getCoverageTestSuites().isEmpty()) {
			return 0.0;
		}

		double coverage = 0.0;
		TestSuiteCoverage suiteCoverage = this.suite.getCoverageTestSuites().get( this.suite.getCoverageTestSuites().size() - 1 );

		for (CriterionCoverage criterionCoverage : suiteCoverage.getCoverage()) {
			coverage += criterionCoverage.getCoverageValue();
		}

		NumberFormat formatter = new DecimalFormat("#0.00");
		return Double.parseDouble(formatter.format(coverage / suiteCoverage.getCoverage().size() * 100.0));
		//return coverage / suiteCoverage.getCoverage().size() * 100.0;
	}

	/**
	 * 
	 * 
	 * @param criterionName
	 * @return
	 */
	public double getCriterionCoverage(String criterionName) {
		if (this.suite.getCoverageTestSuites().isEmpty()) {
			return 0.0;
		}

		TestSuiteCoverage suiteCoverage = this.suite.getCoverageTestSuites().get( this.suite.getCoverageTestSuites().size() - 1 );
		for (CriterionCoverage criterionCoverage : suiteCoverage.getCoverage()) {
			if (criterionCoverage.getCriterion().equals(criterionName)) {
				NumberFormat formatter = new DecimalFormat("#0.00");
				return Double.parseDouble(formatter.format(criterionCoverage.getCoverageValue() * 100.0));
				//return criterionCoverage.getCoverageValue() * 100.0;
			}
		}

		return 0.0;
	}
}
