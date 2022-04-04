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
package org.evosuite.jenkins.actions;

import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.model.AbstractBuild;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.evosuite.continuous.ContinuousTestGeneration;
import org.evosuite.jenkins.recorder.EvoSuiteRecorder;
import org.evosuite.xsd.CUT;
import org.evosuite.xsd.Project;
import org.evosuite.xsd.ProjectUtil;

public class ModuleAction implements Action {

    private final AbstractBuild<?, ?> build;

    private final String name;

    private Project project;

    private final List<ClassAction> classes;

    public ModuleAction(AbstractBuild<?, ?> build, String name) {
        this.build = build;
        this.name = name;
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

    public Project getProject() {
        return this.project;
    }

    public List<ClassAction> getClasses() {
        return this.classes;
    }

    /**
     * @param project_info
     * @return
     */
    public boolean build(VirtualChannel channel, ByteArrayInputStream stream, BuildListener listener) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Project.class);
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(ContinuousTestGeneration.class.getResourceAsStream("/xsd/ctg_project_report.xsd")));
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(schema);
            this.project = (Project) jaxbUnmarshaller.unmarshal(stream);

            for (CUT cut : this.project.getCut()) {
                ClassAction c = new ClassAction(this.getBuild(), cut);
                c.highlightSource(channel, listener);
                this.classes.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // data for jelly template

    public int getNumberOfTestableClasses() {
        return ProjectUtil.getNumberTestableClasses(this.project);
    }

    public int getNumberOfTestedClasses() {
        return ProjectUtil.getNumberLatestTestedClasses(this.project);
    }

    public int getAverageNumberOfStatements() {
        return (int) ProjectUtil.getAverageNumberStatements(this.project);
    }

    public int getTotalEffort() {
        return ProjectUtil.getTotalEffort(this.project);
    }

    public int getTimeBudget() {
        return ProjectUtil.getTimeBudget(this.project);
    }

    public int getAverageNumberOfTests() {
        return (int) ProjectUtil.getAverageNumberTests(this.project);
    }

    public Set<String> getCriteria() {
        return ProjectUtil.getUnionCriteria(this.project);
    }

    public double getOverallCoverage() {
        DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
        formatter.applyPattern("#0.00");
        return Double.parseDouble(formatter.format(ProjectUtil.getOverallCoverage(this.project) * 100.0));
    }

    public double getAverageCriterionCoverage(String criterionName) {
        DecimalFormat formatter = EvoSuiteRecorder.decimalFormat;
        formatter.applyPattern("#0.00");
        return Double.parseDouble(formatter.format(ProjectUtil.getAverageCriterionCoverage(this.project, criterionName) * 100.0));
    }

    public String getURL() {
        return this.name.replace(":", "$");
    }
}
