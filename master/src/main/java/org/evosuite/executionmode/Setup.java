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
package org.evosuite.executionmode;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.utils.LoggingUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Setup {

    public static final String NAME = "setup";

    public static Option getOption() {
        return new Option(NAME, true, "Create evosuite-files with property file");
    }

    public static Object execute(List<String> javaOpts, CommandLine line) {
        boolean inheritanceTree = line.hasOption("inheritanceTree");
        setup(line.getOptionValue("setup"), line.getArgs(), javaOpts, inheritanceTree);
        return null;
    }

    private static void addEntryToCP(String entry) {
        if (!Properties.CP.isEmpty()) {
            Properties.CP += File.pathSeparator;
        }
        Properties.CP += entry;
    }

    private static void setup(String target, String[] args, List<String> javaArgs,
                              boolean doInheritance) {

        Properties.CP = "";

		/*
			Important that target will be first on the CP.
			Otherwise, if for some reasons a dependency uses a same class,
			that would take precedence
		 */
        File targetFile = new File(target);
        if (targetFile.exists()) {
            if (targetFile.isDirectory() || target.endsWith(".jar")) {
                addEntryToCP(target);
            } else if (target.endsWith(".class")) {
                String pathName = targetFile.getParent();
                addEntryToCP(pathName);
            } else {
                LoggingUtils.getEvoLogger().info("Failed to set up classpath for "
                        + target);
                return;
            }
        }

        if (args.length > 0) {
            for (final String arg : args) {
                String element = arg.trim();
                if (element.isEmpty()) {
                    continue;
                }
                addEntryToCP(element);
            }
        }

        Properties.MIN_FREE_MEM = 0; //TODO why this is done???
        File directory = new File(EvoSuite.base_dir_path + File.separator + Properties.OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }

        if (doInheritance) {
            try {
                String fileName = EvoSuite.generateInheritanceTree(Properties.CP);
                FileUtils.copyFile(new File(fileName), new File(Properties.OUTPUT_DIR
                        + File.separator + "inheritance.xml.gz"));

                /*
                 * we need to use '/' instead of File.separator because this value will be written on a text file.
                 * As the relative path will be given to a File object, this will work also on a Windows machine
                 */
                Properties.getInstance().setValue("inheritance_file",
                        Properties.OUTPUT_DIR + "/"
                                + "inheritance.xml.gz");
            } catch (IOException | IllegalArgumentException | NoSuchParameterException | IllegalAccessException e) {
                LoggingUtils.getEvoLogger().error("* Error while creating inheritance tree: " + e.getMessage());
            }
        }

        LoggingUtils.getEvoLogger().info("* Creating new evosuite.properties in "
                + EvoSuite.base_dir_path + File.separator
                + Properties.OUTPUT_DIR);
        LoggingUtils.getEvoLogger().info("* Classpath: " + Properties.CP);
        Properties.getInstance().writeConfiguration(EvoSuite.base_dir_path + File.separator
                + Properties.OUTPUT_DIR
                + File.separator
                + "evosuite.properties");
    }


}
