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
package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.FilePath;
import hudson.model.AbstractBuild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Set;

import org.evosuite.jenkins.recorder.EvoSuiteRecorder;
import org.evosuite.xsd.CUT;
import org.evosuite.xsd.CUTUtil;
import org.evosuite.xsd.Generation;
import org.evosuite.xsd.TestSuite;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

public class ClassAction implements Action {

	private final AbstractBuild<?, ?> build;

	private final CUT cut;

	private String testSourceCode;

	public ClassAction(AbstractBuild<?, ?> build, CUT cut) {
		this.build = build;
		this.cut = cut;
		this.testSourceCode = "";
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return this.cut.getFullNameOfTargetClass();
	}

	@Override
	public String getUrlName() {
		return null;
	}

	public AbstractBuild<?, ?> getBuild() {
		return this.build;
	}

	public String getName() {
		return this.cut.getFullNameOfTargetClass();
	}

	public String getTestSuiteName() {
		return this.cut.getFullNameOfTestSuite();
	}

	public void highlightSource(VirtualChannel channel, BuildListener listener) throws InterruptedException {
	    Generation latestGeneration = CUTUtil.getLatestGeneration(this.cut);
	    if (latestGeneration.isFailed()) {
	      StringBuilder str = new StringBuilder();
	      str.append("<h3>std_err_CLIENT</h3>");
	      str.append("<p>" + this.getLog(channel, latestGeneration.getStdErrCLIENT()) + "</p>");
	      str.append("<h3>std_out_CLIENT</h3>");
	      str.append("<p>" + this.getLog(channel, latestGeneration.getStdOutCLIENT()) + "</p>");
	      str.append("<h3>std_err_MASTER</h3>");
	      str.append("<p>" + this.getLog(channel, latestGeneration.getStdErrMASTER()) + "</p>");
	      str.append("<h3>std_out_MASTER</h3>");
	      str.append("<p>" + this.getLog(channel, latestGeneration.getStdOutMASTER()) + "</p>");
	      this.testSourceCode = str.toString();
          return ;
	    }

	    Generation latestSuccessfulGeneration = CUTUtil.getLatestSuccessfulGeneration(this.cut);
	    if (latestSuccessfulGeneration == null) { 
	      this.testSourceCode = "<p>There was not a single successful generation "
              + "for this class. Likely this is an EvoSuite bug.</p>";
          return ;
        }

	    TestSuite suite = latestSuccessfulGeneration.getSuite();
	    if (suite == null) { 
	      this.testSourceCode = "<p>Test suite of the latest successful generation "
              + "is null. Likely this is an EvoSuite bug.</p>";
	      return ;
	    }

		try {
		    String javaFile = suite.getFullPathOfTestSuite();
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "JavaFile: " + javaFile);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new FilePath(channel, javaFile).copyTo(out);

			InputStream file = new ByteArrayInputStream(out.toByteArray());
			JavaSource source = new JavaSourceParser().parse(new InputStreamReader(file, Charset.forName("UTF-8")));

			JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
			options.setShowLineNumbers(true);
			options.setAddLineAnchors(true);

			JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
			StringWriter writer = new StringWriter();
			converter.convert(source, options, writer);

			this.testSourceCode = writer.toString();
		} catch (IOException e) {
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + e.getMessage());
			listener.getLogger().println(EvoSuiteRecorder.LOG_PREFIX + "Returning a empty source-code");
			this.testSourceCode = e.getMessage();
		}
	}

    private String getLog(VirtualChannel channel, String filePath) throws InterruptedException {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new FilePath(channel, filePath).copyTo(out);
        return new String(out.toByteArray(), Charset.forName("UTF-8"));
      } catch (IOException e) {
        return "It was not possible to open/read '" + filePath + "' due to " + e.getMessage();
      }
    }

	// data for jelly template

	public int getNumberOfStatements() {
	    return CUTUtil.getNumberStatements(this.cut);
	}

	public int getTotalEffort() {
        return CUTUtil.getTotalEffort(this.cut);
	}

	public int getTimeBudget() {
        return CUTUtil.getTimeBudget(this.cut);
    }

	public int getNumberOfTests() {
        return CUTUtil.getNumberTests(this.cut);
	}

	public String getTestSourceCode() {
		return this.testSourceCode;
	}

	public Set<String> getCriteria() {
        return CUTUtil.getCriteria(this.cut);
	}

	public double getOverallCoverage() {
	    DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
        formatter.applyPattern("#0.00");
        return Double.parseDouble(formatter.format(CUTUtil.getOverallCoverage(this.cut) * 100.0));
	}

	public double getCriterionCoverage(String criterionName) {
	    DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
        formatter.applyPattern("#0.00");
	    return Double.parseDouble(formatter.format(CUTUtil.getCriterionCoverage(this.cut, criterionName) * 100.0));
	}
}
