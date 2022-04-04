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
package org.evosuite.dse;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

import org.evosuite.PackageInfo;

/**
 * Constant values
 *
 * <p>
 * Notice our convention of passing Z3PARAM_ parameters to Z3.
 * </p>
 * <p>
 * TODO: Can we merge this with the instrumentation's Config class?
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public class MainConfig {

    public static final String LAMBDA_CLASS_NAME_FRAGMENT = "$$Lambda$";

    private static MainConfig singleton;

    /**
     * Singleton
     */
    public static MainConfig get() {
        return singleton;
    }

    /**
     * If no config set yet, then set a new MainConfig.
     */
    public static MainConfig setInstance() {
        if (singleton == null)
            singleton = new MainConfig();
        return get();
    }

    /**
     * If no config set yet, then set conf. This method should only be called if
     * conf is an instance of a sub-class of MainConfig.
     */
    public static MainConfig setInstance(MainConfig conf) {
        if (singleton == null)
            singleton = conf;
        return get();
    }

    /**
     * Constructor.
     *
     * <p>
     * <b>Should only be called from subclass constructor</b> (or
     * {@link #setInstance()}).
     */
    protected MainConfig() {
        // empty
    }

    @Help("Do not instrument any of the classes that have any of these prefixes. "
            + "IMPORTANT: InstrumentConfig maintains the same list. "
            + "Both lists have to be updated for Dsc to work properly! "
            + "TODO: Merge these two lists.")
    public LinkedStringList DO_NOT_INSTRUMENT_PREFIXES = new LinkedStringList("java.", //$NON-NLS-1$
            "javax.", //$NON-NLS-1$  // FIXME: Allow instrumentation of javax: Deal with extension class loader
            "sun.", //$NON-NLS-1$
            "com.sun.", //$NON-NLS-1$
            "$", //$NON-NLS-1$

            "org.eclipse.", //$NON-NLS-1$

            // "gnu.trove.", //$NON-NLS-1$

            "org.junit.", //$NON-NLS-1$
            "junit.", //$NON-NLS-1$

            // "spoon.", //$NON-NLS-1$

            PackageInfo.getEvoSuitePackage() + ".", //$NON-NLS-1$
            "edu.uta.cse.dbtest.", //$NON-NLS-1$
            "edu.uta.cse.tada.", //$NON-NLS-1$
            "edu.uta.cse.z3.", //$NON-NLS-1$

            "org.objectweb.asm.", //$NON-NLS-1$

            "roops.util.", //$NON-NLS-1$       // Reachability benchmarks

            "org.apache.derby.", //$NON-NLS-1$ // TaDa
            "tada.sqlparser.", //$NON-NLS-1$
            "Zql.", //$NON-NLS-1$
            "org.jcp.", //$NON-NLS-1$

            "org.postgresql.", //$NON-NLS-1$

            "edu.umass.cs.", //$NON-NLS-1$       //Kaituo

            "com.yourkit.",
            //      "icse2010.",  //$NON-NLS-1$   // Ishtiaque: ICSE 2010
            "woda2010.", //$NON-NLS-1$   // Ishtiaque: WODA 2010
            //      "ecoop2010.",		 //$NON-NLS-1$// Ishtiaque: ECOOP 2010
            "com.accenture.lab.crest.vm.", //$NON-NLS-1$//ISHTIAQUE: CREST
            "com.mysql.", //$NON-NLS-1$     //Ishtiauqe MySQL access,

            "org.apache.oro.text.regex." //$NON-NLS-1$
    );

    @Help("Prefixes of classes that will always be explored symbolically, "
            + "regardless if they appear in DO_NOT_INSTRUMENT_PREFIXES. "
            + "IMPORTANT: InstrumentConfig maintains the same list. "
            + "Both lists have to be updated for Dsc to work properly! "
            + "TODO: Merge these two lists.")
    public LinkedStringList DO_INSTRUMENT_PREFIXES = new LinkedStringList(
            "com.sun.javadoc.", //$NON-NLS-1$
            "roops.util.RoopsArray" //$NON-NLS-1$
    );

    /* JUNIT options of generated JUnit test cases */

    /* MOCK options */

    /* General options */

    /*
     * DBTEST options FIXME(Mahbub): Move all DB-Test options into a separate
     * class
     */

    /* REPAIR options */

    /* CREST / CARFAST options */

    /* DUMP options */

    /* General exploration options */

    // TODO: implement MAX_SAT_CALLS_PATH
    // @Help("USE_MAX ==> Maximum number of SAT calls Dsc will issue for a single "
    // +
    // "execution path through user code")
    // public int MAX_SAT_CALLS_PATH = 5;

    @Help("Artificial maximum number of local variables in a method/constructor")
    public int MAX_LOCALS_DEFAULT = 200;

    public final static String CLINIT = "<clinit>"; //$NON-NLS-1$
    public final static String INIT = "<init>"; //$NON-NLS-1$

    // TODO: Implement this, should be similar to running all Roops methods
    // @Help("Run on all public static methods declared by public classes")
    // public boolean RUN_ON_ALL_PUBLIC_PUBLIC_STATIC_METHODS = false;

    // TODO: Refactor the Roops flags
    // @Help("Run on all Roops benchmark methods")
    // public boolean RUN_ON_ALL_ROOPS_BENCHMARK_METHODS = false;

    /**
     * TODO: Get a more precise answer by calling getAllLoadedClasses() on the
     * java.lang.instrument.Instrumentation instance passed to our JvmAgent and
     * diffing it with the list of classes we transformed.
     *
     * @param typeName some/package/SomeType
     * @return if we omit typeName from instrumentation
     */
    public boolean isIgnored(String type) {
        String typeName = type.replace('/', '.'); //$NON-NLS-1$ //$NON-NLS-2$

        for (String prefix : DO_INSTRUMENT_PREFIXES)
            // positive list overrides exclusions
            if (typeName.startsWith(prefix))
                return false;

        for (String prefix : DO_NOT_INSTRUMENT_PREFIXES)
            if (typeName.startsWith(prefix))
                return true;

        return false;
    }

    /* LOG options */

    // @Help("log trivially true conjuncts (x==x) etc. when logging the path condition in Dsc notation")
    // public boolean LOG_PATH_COND_DSC_TRIVIAL_TRUE = false;

    /* Convention: We represent a Z3 parameter PPP with as Z3PARAM_PPP */

    /*
     * Z3 configuration parameters.<br><br>
     *
     * The parameters (together with their default values and description) are
     * <b>copied verbatim from the output of Z3 version 1.3</b>: <pre> z3.exe
     * -ini? </pre>
     *
     * You have to set these flags before creating a Z3 context, otherwise your
     * settings will be ignored.
     */

    @Help("Class path for finding new classes")
    public String CLASS_PATH = System.getProperty("java.class.path");
}
