package org.evosuite.continuous.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.Properties;
import org.evosuite.continuous.persistency.CsvJUnitData;
import org.evosuite.utils.ReportGenerator.RuntimeVariable;

import au.com.bytecode.opencsv.CSVReader;

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

	/**
	 * Map from CUT full class name (key) to ClassInfo object (value)
	 */
	private final Map<String, ClassInfo> classes;

	/**
     * 
     */
    private final Set<String> filesChanged;

    /**
     * 
     */
    private final HashMap<String, List<Double>> coverage;

	private ProjectGraph graph = null;

	public ProjectStaticData() {
		classes = new ConcurrentHashMap<String, ClassInfo>();

		this.filesChanged = new HashSet<String>();
        this.coverage = new HashMap<String, List<Double>>();

        // Load history file
        BufferedReader br = null;
        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(Properties.CTG_HISTORY_FILE));
            while ((sCurrentLine = br.readLine()) != null) {
                String[] split = sCurrentLine.split(",");
                this.filesChanged.add(split[0]);
            }
        } catch (IOException e) {
            // ok, if the CTG_HISTORY_FILE does not exists
            // is to generate test cases to all classes
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Load Previous Coverage
        File tmp = new File(Properties.CTG_FOLDER + "/" + Properties.CTG_TMP_FOLDER);

        File[] tmp_dirs = tmp.listFiles();
        if (tmp_dirs == null || tmp_dirs.length == 0)
            return ;

        Arrays.sort(tmp_dirs);
        for (File tmp_dir : tmp_dirs)
        {
            if (tmp_dir.getName().equals(Properties.SEED_DIR))
                continue ;

            if (tmp_dir.isDirectory())
            {
                File subjects = new File(tmp_dir.getAbsolutePath() + "/reports");
                if (subjects.listFiles() == null)
                    continue ;

                for (File subject : subjects.listFiles())
                {
                    if (subject.isDirectory())
                    {
                        List<String[]> rows = null;
                        try {
                            CSVReader reader = new CSVReader(new FileReader(subject.getAbsolutePath() + "/statistics.csv"));
                            rows = reader.readAll();
                            reader.close();
                        }
                        catch (Exception e) {
                            continue;
                        }
                        if (rows == null || rows.isEmpty())
                            continue ;

                        String targetClass = CsvJUnitData.getValue(rows, "TARGET_CLASS").trim();
                        double branchCoverage = Double.parseDouble(CsvJUnitData.getValue(rows, RuntimeVariable.BranchCoverage.toString()));

                        List<Double> class_coverages = this.coverage.containsKey(targetClass) ? this.coverage.get(targetClass) : new ArrayList<Double>();
                        class_coverages.add(branchCoverage);
                        this.coverage.put(targetClass, class_coverages);
                    }
                }
            }
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

		private boolean hasChanged = true;
        private boolean hasCoverageImproved = true;

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

        public void setCoverageImproved(boolean coverage) {
            this.hasCoverageImproved = coverage;
        }
        public boolean hasCoverageImproved() {
            return this.hasCoverageImproved;
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
        return this.filesChanged.contains(className);
    }

    /**
     * Return previous test coverage
     */
    public List<Double> getPreviousCoverage(String className) {
        return this.coverage.get(className);
    }

    /**
     * Has the coverage improved in last N commits
     */
    public boolean hasCoverageImproved(String className, int n)
    {
        double lastCoverage = 0.0;
        try {
            lastCoverage = this.coverage.get(className).get( this.coverage.get(className).size() - 1 );
            if (lastCoverage == 1.0) // if we achieve 100% coverage we don't want to test this class
                return false;
        } catch (NullPointerException e) {
            // ok, we get an exception here because we aren't no using HistorySchedule or we don't have yet history coverage,
            // so lets return true
            return true;
        }

        if (this.coverage.get(className).size() < n)
            return true;

        for (int i = this.coverage.get(className).size() - 2; i > this.coverage.get(className).size() - 1 - n; i--) {
            if (i < 0)
                return false;

            if (this.coverage.get(className).get(i) < lastCoverage)
                return true;
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
