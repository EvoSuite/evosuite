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
package org.evosuite.continuous.project;

import org.evosuite.Properties;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.xsd.CUT;
import org.evosuite.xsd.Generation;
import org.evosuite.xsd.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
 */
public class ProjectStaticData {

    private static final Logger logger = LoggerFactory.getLogger(ProjectStaticData.class);

    /**
     * Map from CUT full class name (key) to ClassInfo object (value)
     */
    private final Map<String, ClassInfo> classes;


    private final Set<String> modifiedFiles;


    private Project project = null;


    private ProjectGraph graph = null;


    public ProjectStaticData() {
        classes = new ConcurrentHashMap<>();
        this.modifiedFiles = new LinkedHashSet<>();
    }


    public void initializeLocalHistory() {
        if (Properties.CTG_HISTORY_FILE == null) {
            logger.info("ctg history file is not set");
            return;
        }

        // Load history file
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

        this.project = StorageManager.getDatabaseProject();
    }

    /**
     * Immutable class representing all the info data for a class
     *
     * @author arcuri
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

        /**
         * time budget in seconds allocated to test this class
         */
        private int timeBudgetInSeconds = 0;

        /**
         * amount of memory in Megabytes used to test this class
         */
        private int memoryInMB = 0;

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

        public void setTimeBudgetInSeconds(int timeBudgetInSeconds) {
            this.timeBudgetInSeconds = timeBudgetInSeconds;
        }

        public int getTimeBudgetInSeconds() {
            return this.timeBudgetInSeconds;
        }

        public void setMemoryInMB(int memoryInMB) {
            this.memoryInMB = memoryInMB;
        }

        public int getMemoryInMB() {
            return memoryInMB;
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
    public Collection<String> getClassNames() {
        return Collections.unmodifiableCollection(classes.keySet());
    }

    /**
     * Returns true if a class has been changed, false otherwise
     */
    public boolean hasChanged(String javaFileName) {
        return this.modifiedFiles.parallelStream().anyMatch(m -> m.endsWith(javaFileName));
    }

    protected void setProject(Project project) {
        this.project = project;
    }

    /**
     * It checks whether EvoSuite was able to improve coverage,
     * test suite size, etc for 'className' in the last N
     * generations. I.e., it checks whether it is worth to test
     * 'className'.
     *
     * @param className
     * @param n
     * @return
     */
    public boolean isToTest(String className, int n) {

        if (this.project == null) {
            return true; // we don't have any previous data at all
        }

        CUT cut = this.project.getCut().parallelStream()
                .filter(p -> p.getFullNameOfTargetClass().equals(className))
                .findFirst().orElse(null);

        if (cut == null) {
            return true; // we don't have any coverage yet
        }

        int how_many_generations_so_far = cut.getGeneration().size();

        // did EvoSuite crashed?
        if (cut.getGeneration().get(how_many_generations_so_far - 1).isFailed()) {
            return true;
        }

        // not enough data to compare
        if (how_many_generations_so_far < n) {
            return true;
        }

        // we just keep track of the best test suite generated so far,
        // however if the coverage of any criterion did not increased
        // in the last N generations, there is no point continue testing.
        // if at some point the className is changed, then the class
        // should be tested again

        List<Generation> lastNGenerations = cut.getGeneration().stream()
                .filter(g -> g.getTimeBudgetInSeconds().intValue() > 0)
                .skip(how_many_generations_so_far - n)
                .collect(Collectors.toList());

        // project_info.xml is populated with many 'generation' elements,
        // however, not all have a 'suite' element. a 'generation' only
        // has a 'suite' if EvoSuite did not crash, or if the generated
        // test suite was not better than any previous manually-written/generated
        // test suite. therefore, if N previous generations include a
        // test suite, each improved (coverage, number of test cases, etc)
        // the previous one (N-1)
        for (int i = lastNGenerations.size() - 1; i >= 0; i--) {
            Generation generation = lastNGenerations.get(i);
            if (generation.isFailed()) {
                // if any of the previous N executions failed and modified
                // the cut, re-generate new test cases for it
                return true;
            }

            if (generation.getSuite() != null) {
                // if we got a Test Suite it's because it improved
                // the previous one
                return true;
            }
        }

        // it seems that we were not able to improve coverage, etc of 'className'
        // on the last N generations
        return false;
    }

    /**
     * Return a read-only view of the current project CUT graph
     *
     * @return
     */
    public ProjectGraph getProjectGraph() {
        if (graph == null) {
            graph = new ProjectGraph(this);
        }
        return graph; //FIXME should be a read-only view
    }
}
