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

import org.evosuite.Properties;
import org.evosuite.utils.MD5;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * HistoryChanges class
 * <p>
 * <p>
 * On each execution of EvoSuite-Maven-Plugin this class checks which
 * files have changed since EvoSuite was invoked the last time
 * <p>
 * How? This class keeps a file with the following format
 * __absolute_file_path__ \t __md5_hash__
 * <p>
 * then, if a file has a different hash, i.e., has been changed, another
 * file called 'history_file' is keep with the following format
 * (Added)A/(Modified)M \t __absolute_file_path__
 * <p>
 * therefore, when EvoSuite-Maven-Plugin is executed with HistorySchedule
 * enabled, the 'history_file' will be used to identify which files have
 * added/modified and perform the schedule.
 *
 * @author José Campos
 */
public class HistoryChanges {

    public static void keepTrack(String basedir, List<File> files) throws Exception {

        File dot_evosuite = new File(basedir + File.separator + Properties.CTG_DIR);
        if (!dot_evosuite.exists()) {
            if (!dot_evosuite.mkdir()) {
                throw new Exception("No permission to create the directory '" + basedir + File.separator + Properties.CTG_DIR + "'");
            }
        }

        File hash_file = new File(basedir + File.separator + Properties.CTG_DIR + File.separator + "hash_file");
        File history_file = new File(basedir + File.separator + Properties.CTG_DIR + File.separator + "history_file");

        if (!hash_file.exists()) {
            try {
                // create the hash_file <Path, Hash>
                if (!hash_file.createNewFile()) {
                    throw new Exception("No permission to create the file '" + basedir + File.separator + Properties.CTG_DIR + File.separator + "hash_file" + "'");
                }
                // and the history_file as well
                if (!history_file.createNewFile()) {
                    throw new Exception("No permission to create the file '" + basedir + File.separator + Properties.CTG_DIR + File.separator + "history_file" + "'");
                }

                FileWriter hash_file_fw = new FileWriter(hash_file.getAbsoluteFile());
                BufferedWriter hash_file_bw = new BufferedWriter(hash_file_fw);
                FileWriter history_file_fw = new FileWriter(history_file.getAbsoluteFile());
                BufferedWriter history_file_bw = new BufferedWriter(history_file_fw);

                // add content to hash_file and to history_file
                for (File file : files) {
                    hash_file_bw.write(file.getAbsolutePath() + "\t" + MD5.hash(file) + "\n");
                    history_file_bw.write("A" + "\t" + file.getAbsolutePath() + "\n");
                }

                hash_file_bw.close();
                history_file_bw.close();
            } catch (IOException e) {
                throw new Exception("IOException: ", e);
            }
        } else {
            // read content of hash_file
            Map<String, String> hash_file_content = new LinkedHashMap<>();

            try (BufferedReader br = new BufferedReader(new FileReader(hash_file))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    String[] split = sCurrentLine.split("\t");
                    hash_file_content.put(split[0], split[1]);
                }
            } catch (IOException e) {
                throw new Exception("reading the content of hash_file ", e);
            }

            try {
                FileWriter hash_file_fw = new FileWriter(hash_file.getAbsoluteFile());
                BufferedWriter hash_file_bw = new BufferedWriter(hash_file_fw);
                hash_file_bw.write(""); // clean file

                FileWriter history_file_fw = new FileWriter(history_file.getAbsoluteFile());
                BufferedWriter history_file_bw = new BufferedWriter(history_file_fw);
                history_file_bw.write(""); // clean file

                // compare each hash
                for (File file : files) {
                    String hash = MD5.hash(file);
                    hash_file_bw.write(file.getAbsolutePath() + "\t" + hash + "\n");

                    if (!hash_file_content.containsKey(file.getAbsolutePath())) {
                        history_file_bw.write("A" + "\t" + file.getAbsolutePath() + "\n");
                    } else if (!hash_file_content.get(file.getAbsolutePath()).equals(hash)) {
                        history_file_bw.write("M" + "\t" + file.getAbsolutePath() + "\n");
                    }
                }

                hash_file_bw.close();
                history_file_bw.close();
            } catch (IOException e) {
                throw new Exception("IOException: ", e);
            }
        }
    }
}
