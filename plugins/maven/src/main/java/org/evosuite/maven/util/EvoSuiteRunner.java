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
package org.evosuite.maven.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.RepositorySystemSession;
import org.evosuite.EvoSuite;
import org.evosuite.runtime.util.JavaExecCmdUtil;
import org.evosuite.utils.LoggingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Note: we cannot call EvoSuite directly on same JVM, like the following:
 *
 * <p>
 * <code>
 * ContinuousTestGeneration ctg = new ContinuousTestGeneration(target,cp,prefix,conf);
 * ctg.execute();
 * </code>
 *
 * <p>
 * Reason is that Maven uses its own classloaders, and setting their classpath
 * becomes very messy, if possible at all.
 * So, we need to call EvoSuite on separated process
 *
 * <p>
 * TODO: most likely this code should be moved to a library, as other plugins (eg Netbeans)
 * will use it as well
 */
public class EvoSuiteRunner {

    public static final String EVOSUITE_HOME_VARIABLE = "EVOSUITE_HOME";

    /**
     * The maven logger of the plugin
     */
    private final Log logger;

    private final List<Artifact> artifacts;

    private final ProjectBuilder projectBuilder;

    private final RepositorySystemSession repoSession;

    private Process process;

    public EvoSuiteRunner(Log logger, List<Artifact> artifacts,
                          ProjectBuilder projectBuilder, RepositorySystemSession repoSession) {
        super();
        this.logger = logger;
        this.artifacts = artifacts;
        this.projectBuilder = projectBuilder;
        this.repoSession = repoSession;
    }

    public void registerShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (process != null) {
                    process.destroy();
                }
            }
        });
    }

    /**
     * This is blocking
     *
     * @param params
     * @return
     */
    public boolean runEvoSuite(String dir, List<String> params) {
        List<String> cmd = getCommandToRunEvoSuite();
        if (cmd == null) {
            return false;
        }

        cmd.addAll(params);

        return runProcess(dir, cmd);
    }

    /**
     * We run the EvoSuite that is provided with the plugin
     *
     * @return
     */
    private List<String> getCommandToRunEvoSuite() {

        logger.debug("EvoSuite Maven Plugin Artifacts: " + Arrays.toString(artifacts.toArray()));

        Artifact evosuite = null;

        for (Artifact art : artifacts) {
            //first find the main EvoSuite jar among the dependencies
            if (art.getArtifactId().equals("evosuite-master")) {
                evosuite = art;
                break;
            }
        }

        if (evosuite == null) {
            logger.error("CRITICAL ERROR: plugin can detect EvoSuite executable");
            return null;
        }

        logger.debug("EvoSuite located at: " + evosuite.getFile());

        /*
         * now, build a project descriptor for evosuite, which is needed to
         * query all of its dependencies
         */
        DefaultProjectBuildingRequest req = new DefaultProjectBuildingRequest();
        req.setRepositorySession(repoSession);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        req.setSystemProperties(System.getProperties());
        req.setResolveDependencies(true);

        ProjectBuildingResult res;

        try {
            res = projectBuilder.build(evosuite, req);
        } catch (ProjectBuildingException e) {
            logger.error("Failed: " + e.getMessage(), e);
            return null;
        }

        //build the classpath to run EvoSuite
        String cp = evosuite.getFile().getAbsolutePath();

        for (Artifact dep : res.getProject().getArtifacts()) {
            cp += File.pathSeparator + dep.getFile().getAbsolutePath();
        }
        logger.debug("EvoSuite classpath: " + cp);

        String entryPoint = EvoSuite.class.getName();

        List<String> cmd = new ArrayList<>();
        cmd.add(JavaExecCmdUtil.getJavaBinExecutablePath()/*"java"*/);
        cmd.add("-D" + LoggingUtils.USE_DIFFERENT_LOGGING_XML_PARAMETER + "=logback-ctg-entry.xml");
        cmd.add("-Dlogback.configurationFile=logback-ctg-entry.xml");
        cmd.add("-cp");
        cmd.add(cp);
        cmd.add(entryPoint);
        return cmd;
    }


    /**
     * Check if there is a valid installation of EvoSuite on the current machine
     */
    @Deprecated
    private List<String> getCommandToRunExternalEvoSuite() {

        /*
         * Note: we keep this code in case we want to implement
         * the option of running an external EvoSuite
         */

        String home = System.getenv(EVOSUITE_HOME_VARIABLE);
        if (home == null || home.isEmpty()) {
            logger.error("Need to set the environment variable " + EVOSUITE_HOME_VARIABLE +
                    " pointing to where EvoSuite is installed");
            return null;
        }

        File folder = new File(home);
        if (!folder.exists()) {
            logger.error("EvoSuite home " + home + " does not exist");
            return null;
        }
        if (!folder.isDirectory()) {
            logger.error("EvoSuite home " + home + " is not a folder");
            return null;
        }

        /*
         * TODO: this will need to be changed once we finalize how to
         * distribute EvoSuite
         */
        File[] jars = folder.listFiles((dir, name) -> {
            String lc = name.toLowerCase();
            return lc.startsWith("evosuite") && lc.endsWith(".jar");
        });

        if (jars.length == 0) {
            logger.error("No evosuite jar in " + folder.getPath());
            return null;
        }
        if (jars.length > 1) {
            /*
             * sort in way the largest file is first.
             * this is needed if we put as HOME where we compile EvoSuite
             */
            Arrays.sort(jars, (o1, o2) -> (int) (o2.length() - o1.length()));
        }

        String evo = jars[0].getAbsolutePath();
        logger.info("Going to use EvoSuite jar: " + evo);

        List<String> cmd = new ArrayList<>();
        cmd.add(JavaExecCmdUtil.getJavaBinExecutablePath()/*"java"*/);
        cmd.add("-jar");
        cmd.add("" + evo);
        return cmd;
    }

    private boolean runProcess(String baseDir, List<String> cmd) {

        try {
            if (baseDir == null) {
                baseDir = System.getProperty("user.dir");
            }

            logger.debug("Working directory: " + baseDir);
            if (logger.isDebugEnabled()) {
                logger.debug("Going to execute command: " + String.join(" ", cmd));
            }

            File dir = new File(baseDir);

            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.directory(dir);
            builder.redirectErrorStream(true);

            process = builder.start();
            handleProcessOutput(process, logger);

            //output
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.error("Error in EvoSuite");
                return false;
            } else {
                logger.debug("EvoSuite terminated");
            }

        } catch (IOException e) {
            logger.error("Failed to start EvoSuite: " + e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            if (process != null) {
                try {
                    //be sure streamers are closed, otherwise process might hang on Windows
                    process.getOutputStream().close();
                    process.getInputStream().close();
                    process.getErrorStream().close();
                } catch (Exception t) {
                    logger.error("Failed to close process stream: " + t);
                }
                process.destroy();
            }
            return false;
        }

        process = null;

        return true;
    }

    private void handleProcessOutput(final Process process, final Log logger) {

        Thread reader = new Thread() {
            @Override
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    while (!this.isInterrupted()) {
                        String line = in.readLine();
                        if (line != null && !line.isEmpty()) {
                            logger.info(line);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Exception while reading spawn process output: " + e);
                }
            }
        };

        reader.start();
        logger.debug("Started thread to read spawn process output");
    }
}
