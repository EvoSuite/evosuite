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
package org.evosuite.continuous.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.Properties;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.xsd.CriterionCoverage;
import org.evosuite.xsd.ProjectInfo;
import org.evosuite.xsd.TestSuite;
import org.evosuite.xsd.TestSuiteCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Class used to contain all the static data/info of the target project (SUT),
 * like number of classes, branches per class, etc.
 * </p>
 * 
 * <p>
 * A class under test (CUT) is any <i>public</i> class in the SUT, regardless of
 * whether it is concrete, abstract or an interface, but as long as they have
 * any code to test (eg, an abstract class with only abstract methods will not
 * be a CUT). Anonymous and private classes are not CUTs. Protected and package
 * level classes are bit tricky, and at least for the moment they are not
 * considered as CUTs (might change in the future though).
 * </p>
 * 
 * <p>
 * Note: till Java 7, there would be no point in considering an interface as
 * CUT. But, from Java 8 onward, interfaces might have code. Furthermore, when
 * we build the CUT graph, we need to consider interfaces as well, regardless of
 * whether they have any branch (ie code) or not.
 * </p>
 * 
 * @author arcuri
 * 
 */
public class ProjectStaticData {

	private static Logger logger = LoggerFactory.getLogger(ProjectStaticData.class);

	/**
	 * Map from CUT full class name (key) to ClassInfo object (value)
	 */
	private final Map<String, ClassInfo> classes;

	/**
     * 
     */
    private final Set<String> modifiedFiles;

    /**
     * 
     */
    private final HashMap<String, List<Map<String, Double>>> coverages;

    /**
     * 
     */
	private ProjectGraph graph = null;

	/**
	 * 
	 */
	public ProjectStaticData() {
		classes = new ConcurrentHashMap<String, ClassInfo>();

		this.modifiedFiles = new LinkedHashSet<String>();
        this.coverages = new LinkedHashMap<String, List<Map<String, Double>>>();
	}

	/**
	 * 
	 */
	public void initializeLocalHistory() {
		if (Properties.CTG_HISTORY_FILE == null) {
			logger.info("ctg history file is not set");
			return ;
		}

		//
		// 1. Load history changes
		//
        BufferedReader br = null;
        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(Properties.CTG_HISTORY_FILE));
            while ((sCurrentLine = br.readLine()) != null) {
                String[] split = sCurrentLine.split("\t");

                switch (split[0]) {
                	case "A": // 'added' and 'modified' are treated equally
                	case "M":
                		// only consider .java entries
                		if (split[1].endsWith(".java"))
                			this.modifiedFiles.add(split[1].replace(File.separator, "."));
                		break;
                	case "D":
                		// ignore
                		break;
                	default:
                		logger.error("option '" + split[0] + "' in the " + Properties.CTG_HISTORY_FILE + " file not supported");
                		break;
                }
            }
        } catch (FileNotFoundException e) {
        	logger.error("'" + Properties.CTG_HISTORY_FILE + "' file not found");
        } catch (IOException e) {
            logger.error("error reading '" + Properties.CTG_HISTORY_FILE + "' file", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //
        // 2. Load previous CTG data
        //
        ProjectInfo p = StorageManager.getDatabaseProjectInfo();
        if (p.getGeneratedTestSuites().size() == 0) {
        	return ; // we still do not have coverage
        }

        for (TestSuite suite : p.getGeneratedTestSuites()) {
        	String targetClass = suite.getFullNameOfTargetClass();

        	List<Map<String, Double>> suite_coverages = new ArrayList<Map<String, Double>>();
        	for (TestSuiteCoverage suite_coverage : suite.getCoverageTestSuites()) {
        		Map<String, Double> previous_coverages = new LinkedHashMap<String, Double>();
        		for (CriterionCoverage coverage : suite_coverage.getCoverage()) {
        			previous_coverages.put(coverage.getCriterion(), coverage.getCoverageValue());
        		}

        		suite_coverages.add(previous_coverages);
        	}

        	this.coverages.put(targetClass, suite_coverages);
        }
	}

	/**
	 * Immutable class representing all the info data for a class
	 * 
	 * @author arcuri
	 * 
	 */
	public static class ClassInfo {
		public final Class<?> theClass;
		public final int numberOfBranches;
		/**
		 * we cannot only consider the number of branches, as there might be
		 * CUTs with code but no branches
		 */
		public final boolean hasCode;

		/**
		 * has the last commit added/modified the CUT?
		 */
		private boolean hasChanged = true;

		/**
		 * a class should be tested if we still don't have
		 * 100% coverage or if the coverage has improved
		 * in the last N generations
		 */
        private boolean isToTest = true;

		public ClassInfo(Class<?> theClass, int numberOfBranches, boolean hasCode) {
			super();
			this.theClass = theClass;
			this.numberOfBranches = numberOfBranches;
			this.hasCode = hasCode;
		}

		public String getClassName() {
			return theClass.getName();
		}

		public boolean isTestable() {
			return hasCode;
		}

		public void setChanged(boolean changed) {
            this.hasChanged = changed;
        }
        public boolean hasChanged() {
            return this.hasChanged;
        }

        public void isToTest(boolean isToTest) {
            this.isToTest = isToTest;
        }
        public boolean isToTest() {
            return this.isToTest;
        }
	}

	/**
	 * Add a new ClassInfo. Note: this is protected, as only classes in this
	 * package should be allowed to modify the state of this class
	 * 
	 * @param info
	 */
	protected void addNewClass(ClassInfo info) {
		classes.put(info.getClassName(), info);
	}

	public boolean containsClass(String c) {
		return classes.containsKey(c);
	}

	/**
	 * Return the class info of the given class (with full qualifying name),
	 * or <code>null</code> if missing
	 * 
	 * @param name
	 * @return
	 */
	public ClassInfo getClassInfo(String name) {
		return classes.get(name);
	}

	/**
	 * Return the number of classes in the project, including non-testable ones
	 * 
	 * @return
	 */
	public int getTotalNumberOfClasses() {
		return classes.size();
	}

	/**
	 * If an abstract class or interface has no coded/implemented method, then
	 * there would be no point in generating test cases for it
	 * 
	 * @return
	 */
	public int getTotalNumberOfTestableCUTs() {
		int total = 0;
		for (ClassInfo info : classes.values()) {
			if (info.isTestable()) {
				total++;
			}
		}
		return total;
	}

	public int getTotalNumberOfBranches() {
		int total = 0;
		for (ClassInfo info : classes.values()) {
			total += info.numberOfBranches;
		}
		return total;

	}

	/**
	 * Return an unmodifiable copy of the current data info of the classes in the SUT 
	 * 
	 * @return
	 */
	public Collection<ClassInfo> getClassInfos() {
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * Return an unmodifiable copy of the names of the classes in the SUT
	 * 
	 * @return
	 */
	public Collection<String> getClassNames(){
		return Collections.unmodifiableCollection(classes.keySet());
	}

	/**
     * Return the history statistics of a class
     */
    public boolean hasChanged(String className) {
    	for (String modified_file_name : this.modifiedFiles) {
    		if (modified_file_name.contains(className))
    			return true;
    	}
    	return false;
    }

    /**
     * Return previous test coverage
     */
    public List<Map<String, Double>> getPreviousCoverages(String className) {
        return this.coverages.get(className);
    }

    /**
     * Has the coverage improved in last N commits
     */
    public boolean isToTest(String className, int n) {

    	List<Map<String, Double>> classCoverage = this.coverages.get(className);
    	if (classCoverage == null) {
    		return true; // first time
    	}

    	Map<String, Double> previousCoverage = classCoverage.get( this.coverages.get(className).size() - 1 );

    	// check if all criteria have been covered
    	boolean one_hundred_percent_coverage = true;
    	for (String criterion : previousCoverage.keySet()) {
    		if (previousCoverage.get(criterion) < 1.0) {
    			one_hundred_percent_coverage = false;
    			break ;
    		}
    	}

    	// we've achieved 100% coverage (even if just one execution),
    	// so we don't want to test this class
        if (one_hundred_percent_coverage)
            return false;

        // not enough data
        if (this.coverages.get(className).size() < n)
            return true;

        // we just keep track of the best test suite generated so far,
        // however if the coverage of any criterion did not increased
        // in the last N commits, there is no point continue testing.
        // if at some point the className is changed, then the class
        // should be tested again

        // has the coverage of any criterion improved in the last N commits?
        for (int i = this.coverages.get(className).size() - 1; i > this.coverages.get(className).size() - 1 - n; i--) {
        	for (String criterion : this.coverages.get(className).get(i).keySet()) {
        		if (this.coverages.get(className).get(i).get(criterion) < previousCoverage.get(criterion)) {
        			return true;
        		}
        	}
        }

        return false;
    }

	/**
	 * Return a read-only view of the current project CUT graph
	 * 
	 * @return
	 */
	public ProjectGraph getProjectGraph() {
		if (graph == null){
			graph = new ProjectGraph(this);
		}
		return graph; //FIXME should be a read-only view
	}
}
