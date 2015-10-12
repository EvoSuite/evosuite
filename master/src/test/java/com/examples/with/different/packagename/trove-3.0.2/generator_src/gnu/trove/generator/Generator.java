/*
 * ///////////////////////////////////////////////////////////////////////////////
 * // Copyright (c) 2009, Rob Eden All Rights Reserved.
 * // Copyright (c) 2009, Jeff Randall All Rights Reserved.
 * //
 * // This library is free software; you can redistribute it and/or
 * // modify it under the terms of the GNU Lesser General Public
 * // License as published by the Free Software Foundation; either
 * // version 2.1 of the License, or (at your option) any later version.
 * //
 * // This library is distributed in the hope that it will be useful,
 * // but WITHOUT ANY WARRANTY; without even the implied warranty of
 * // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * // GNU General Public License for more details.
 * //
 * // You should have received a copy of the GNU Lesser General Public
 * // License along with this program; if not, write to the Free Software
 * // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * ///////////////////////////////////////////////////////////////////////////////
 */
package gnu.trove.generator;

import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * <p>Generator class that builds final source files from templates. It does so by
 * replacing patterns in the template files.</p>
 *
 * <p><b>Patterns</b></p>
 *
 * <p>In map-like classes, the letters "k" and "v" are used to indicate "key" and "value".
 * In classes with a single element type, "e" is used. The patterns start and end with a
 * hash sign and come in these variants:
 * <ul>
 *     <li>lowercase (eg: "#e#")- the primitive type (eg: "int" or "float")</li>
 *     <li>T (eg: "#ET#")- the class type (eg: "Integer" or "Float")</li>
 *     <li>uppercase (eg: "#E#")- the abbreviated class type (eg: "Int" or "Float")</li>
 *     <li>C (eg: "#EC#") - uppercase primitive (eg: "INT" or "FLOAT")</li>
 *     <li>MAX (eg: "#EMAX#") - max value for the type (eg: "Integer.MAX_VALUE" or
 *         Float.POSITIVE_INFINITY")</li>
 *     <li>MIN (eg: "#EMIN#") - min value for the type (eg: "Integer.MIN_VALUE" or
 *         Float.NEGATIVE_INFINITY")</li>
 *     <li>underbar (eg: "_E_") - Only applicable in file names, same as "uppercase".</li>
 * </ul>
 * </p>
 *
 * <p><b>Block Replication</b></p>
 *
 * <p>In addition to regular patterns, some classes use block replication. This allows
 * replicating a block of code for each type. A block starts with
 * <code>====START_REPLICATED_CONTENT #&lt;number&gt;====</code> and ends with
 * <code>=====END_REPLICATED_CONTENT #&lt;number&gt;=====</code> (each on a new line) where
 * "&lt;number&gt;" is an integer. Then, that content is replicated for each type where
 * the pattern "#REPLICATED<number>#" is found.</p>
 *
 */
public class Generator {
    private static final WrapperInfo[] WRAPPERS = new WrapperInfo[]{
            new WrapperInfo("double", "Double", "POSITIVE_INFINITY", "NEGATIVE_INFINITY"),
            new WrapperInfo("float", "Float", "POSITIVE_INFINITY", "NEGATIVE_INFINITY"),
            new WrapperInfo("int", "Integer", "MAX_VALUE", "MIN_VALUE"),
            new WrapperInfo("long", "Long", "MAX_VALUE", "MIN_VALUE"),
            new WrapperInfo("byte", "Byte", "MAX_VALUE", "MIN_VALUE"),
            new WrapperInfo("short", "Short", "MAX_VALUE", "MIN_VALUE"),
            new WrapperInfo("char", "Character", "MAX_VALUE", "MIN_VALUE")};

    private static final Pattern PATTERN_v;
    private static final Pattern PATTERN_V;
    private static final Pattern PATTERN_VC;
    private static final Pattern PATTERN_VT;
    private static final Pattern PATTERN_VMAX;
    private static final Pattern PATTERN_VMIN;
    private static final Pattern PATTERN_V_UNDERBAR;

    private static final Pattern PATTERN_k;
    private static final Pattern PATTERN_K;
    private static final Pattern PATTERN_KC;
    private static final Pattern PATTERN_KT;
    private static final Pattern PATTERN_KMAX;
    private static final Pattern PATTERN_KMIN;
    private static final Pattern PATTERN_K_UNDERBAR;

    private static final Pattern PATTERN_e;
    private static final Pattern PATTERN_E;
    private static final Pattern PATTERN_EC;
    private static final Pattern PATTERN_ET;
    private static final Pattern PATTERN_EMAX;
    private static final Pattern PATTERN_EMIN;
    private static final Pattern PATTERN_E_UNDERBAR;

    static {
        PATTERN_v = Pattern.compile("#v#");
        PATTERN_V = Pattern.compile("#V#");
        PATTERN_VC = Pattern.compile("#VC#");
        PATTERN_VT = Pattern.compile("#VT#");
        PATTERN_VMAX = Pattern.compile("#VMAX#");
        PATTERN_VMIN = Pattern.compile("#VMIN#");
        PATTERN_V_UNDERBAR = Pattern.compile("_V_");

        PATTERN_k = Pattern.compile("#k#");
        PATTERN_K = Pattern.compile("#K#");
        PATTERN_KC = Pattern.compile("#KC#");
        PATTERN_KT = Pattern.compile("#KT#");
        PATTERN_KMAX = Pattern.compile("#KMAX#");
        PATTERN_KMIN = Pattern.compile("#KMIN#");
        PATTERN_K_UNDERBAR = Pattern.compile("_K_");

        PATTERN_e = Pattern.compile("#e#");
        PATTERN_E = Pattern.compile("#E#");
        PATTERN_EC = Pattern.compile("#EC#");
        PATTERN_ET = Pattern.compile("#ET#");
        PATTERN_EMAX = Pattern.compile("#EMAX#");
        PATTERN_EMIN = Pattern.compile("#EMIN#");
        PATTERN_E_UNDERBAR = Pattern.compile("_E_");
    }


    private static File root_output_dir;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: Generator [-c] <input_dir> <output_dir>");
            return;
        }

        int arg_index = 0;
        boolean clean_output = false;
        if (args[0].equalsIgnoreCase("-c")) {
            clean_output = true;
            arg_index++;
        }

        File input_directory = new File(args[arg_index++]);
        File output_directory = new File(args[arg_index++]);
        if (!input_directory.exists()) {
            System.err.println("Directory \"" + input_directory + "\" not found.");
            System.exit(-1);
            return;
        }
        if (!output_directory.exists()) {
            makeDirs(output_directory);
        }

        root_output_dir = output_directory;

        if (clean_output) {
            System.out.println("Removing contents of \"" + output_directory + "\"...");
            cleanDir(output_directory);
        }

        scanForFiles(input_directory, output_directory);
    }

    /**
     * Creates dirs, throw IllegalArgumentException or IllegalStateException if
     * argument is invalid or creation fails
     *
     * @param directory
     */
    private static void makeDirs(File directory) {
        if (directory.exists() && !directory.isDirectory())
            throw new IllegalArgumentException(directory + " not a directory");

        if (directory.exists())
            return;

        if (!directory.mkdirs())
            throw new IllegalStateException("Could not create directories " + directory);
    }


    private static void scanForFiles(File input_directory, File output_directory)
            throws IOException {

        File[] files = input_directory.listFiles();
        for (File file : files) {
            // Ignore hidden files
            if (file.isHidden()) continue;

            if (file.isDirectory()) {
                // Ignore CVS directories
                if (file.getName().equals("CVS")) continue;

                scanForFiles(file, new File(output_directory, file.getName()));
                continue;
            }

            processFile(file, output_directory);
        }
    }


    private static void processFile(File input_file, File output_directory)
            throws IOException {

        System.out.println("Process file: " + input_file);

        String content = readFile(input_file);

        String file_name = input_file.getName();
        file_name = file_name.replaceAll("\\.template", ".java");

        File output_file = new File(output_directory, file_name);

        // See what kind of template markers it's using, either e or k/v. No marker
        // indicates a replication-only class.
        if (file_name.contains("_K_")) {
            processKVMarkers(content, output_directory, file_name);
        } else if (file_name.contains("_E_")) {
            processEMarkers(content, output_directory, file_name);
        } else {
            if (input_file.lastModified() < output_file.lastModified()) {
                System.out.println("File " + output_file + " up to date, not processing input");
                return;
            }

            // Replication only
            StringBuilder processed_replication_output = new StringBuilder();
            Map<Integer, String> replicated_blocks =
                    findReplicatedBlocks(content, processed_replication_output);
            if (replicated_blocks != null) {
                content = processReplication(processed_replication_output.toString(),
                        replicated_blocks);
            }

            writeFile(content, output_file);
        }
    }


    private static void processKVMarkers(String content, File output_dir,
                                         String file_name) throws IOException {

        for (WrapperInfo info : WRAPPERS) {
            String k = info.primitive;
            String KT = info.class_name;
            String K = abbreviate(KT);
            String KC = K.toUpperCase();
            String KMAX = info.max_value;
            String KMIN = info.min_value;

            String out = content;
            out = PATTERN_k.matcher(out).replaceAll(k);
            out = PATTERN_K.matcher(out).replaceAll(K);
            out = PATTERN_KC.matcher(out).replaceAll(KC);
            out = PATTERN_KT.matcher(out).replaceAll(KT);
            out = PATTERN_KMAX.matcher(out).replaceAll(KMAX);
            out = PATTERN_KMIN.matcher(out).replaceAll(KMIN);

            String out_file_name = "T" + file_name;
            out_file_name = PATTERN_K_UNDERBAR.matcher(out_file_name).replaceAll(K);

            for (WrapperInfo jinfo : WRAPPERS) {
                String v = jinfo.primitive;
                String VT = jinfo.class_name;
                String V = abbreviate(VT);
                String VC = V.toUpperCase();
                String VMAX = jinfo.max_value;
                String VMIN = jinfo.min_value;

                String vout = out;

                vout = PATTERN_v.matcher(vout).replaceAll(v);
                vout = PATTERN_V.matcher(vout).replaceAll(V);
                vout = PATTERN_VC.matcher(vout).replaceAll(VC);
                vout = PATTERN_VT.matcher(vout).replaceAll(VT);
                vout = PATTERN_VMAX.matcher(vout).replaceAll(VMAX);
                String processed_output = PATTERN_VMIN.matcher(vout).replaceAll(VMIN);

                StringBuilder processed_replication_output = new StringBuilder();
                Map<Integer, String> replicated_blocks =
                        findReplicatedBlocks(processed_output, processed_replication_output);
                if (replicated_blocks != null) {
                    processed_output = processReplication(
                            processed_replication_output.toString(), replicated_blocks);
                }

                String processed_filename =
                        PATTERN_V_UNDERBAR.matcher(out_file_name).replaceAll(V);

                writeFile(processed_output, new File(output_dir, processed_filename));
            }
        }
    }


    private static void processEMarkers(String content, File output_dir,
                                        String file_name) throws IOException {

        for (WrapperInfo info : WRAPPERS) {
            String e = info.primitive;
            String ET = info.class_name;
            String E = abbreviate(ET);
            String EC = E.toUpperCase();
            String EMAX = info.max_value;
            String EMIN = info.min_value;

            String out = content;
            out = PATTERN_e.matcher(out).replaceAll(e);
            out = PATTERN_E.matcher(out).replaceAll(E);
            out = PATTERN_EC.matcher(out).replaceAll(EC);
            out = PATTERN_ET.matcher(out).replaceAll(ET);
            out = PATTERN_EMAX.matcher(out).replaceAll(EMAX);
            String processed_output = PATTERN_EMIN.matcher(out).replaceAll(EMIN);

            String out_file_name = "T" + file_name;
            out_file_name = PATTERN_E_UNDERBAR.matcher(out_file_name).replaceAll(E);

            StringBuilder processed_replication_output = new StringBuilder();
            Map<Integer, String> replicated_blocks =
                    findReplicatedBlocks(processed_output, processed_replication_output);
            if (replicated_blocks != null) {
                processed_output = processReplication(
                        processed_replication_output.toString(), replicated_blocks);
            }

            writeFile(processed_output, new File(output_dir, out_file_name));
        }
    }


    static String processReplication(String content,
                                     Map<Integer, String> replicated_blocks) {

        for (Map.Entry<Integer, String> entry : replicated_blocks.entrySet()) {
            // Replace the markers in the replicated content

            StringBuilder entry_buffer = new StringBuilder();

            boolean first_loop = true;
            for (int i = 0; i < WRAPPERS.length; i++) {
                WrapperInfo info = WRAPPERS[i];

                String k = info.primitive;
                String KT = info.class_name;
                String K = abbreviate(KT);
                String KC = K.toUpperCase();
                String KMAX = info.max_value;
                String KMIN = info.min_value;

                for (int j = 0; j < WRAPPERS.length; j++) {
                    WrapperInfo jinfo = WRAPPERS[j];

                    String v = jinfo.primitive;
                    String VT = jinfo.class_name;
                    String V = abbreviate(VT);
                    String VC = V.toUpperCase();
                    String VMAX = jinfo.max_value;
                    String VMIN = jinfo.min_value;

                    String out = entry.getValue();
                    String before_e = out;
                    out = Pattern.compile("#e#").matcher(out).replaceAll(k);
                    out = Pattern.compile("#E#").matcher(out).replaceAll(K);
                    out = Pattern.compile("#ET#").matcher(out).replaceAll(KT);
                    out = Pattern.compile("#EC#").matcher(out).replaceAll(KC);
                    out = Pattern.compile("#EMAX#").matcher(out).replaceAll(KMAX);
                    out = Pattern.compile("#EMIN#").matcher(out).replaceAll(KMIN);
                    boolean uses_e = !out.equals(before_e);

                    // If we use "e" (instead of "k" & "v", then we don't need the inner
                    // map. Yeah, this is ugly I know... but it works.
                    if (uses_e && j != 0) break;

                    out = Pattern.compile("#v#").matcher(out).replaceAll(v);
                    out = Pattern.compile("#V#").matcher(out).replaceAll(V);
                    out = Pattern.compile("#VT#").matcher(out).replaceAll(VT);
                    out = Pattern.compile("#VC#").matcher(out).replaceAll(VC);
                    out = Pattern.compile("#VMAX#").matcher(out).replaceAll(VMAX);
                    out = Pattern.compile("#VMIN#").matcher(out).replaceAll(VMIN);

                    out = Pattern.compile("#k#").matcher(out).replaceAll(k);
                    out = Pattern.compile("#K#").matcher(out).replaceAll(K);
                    out = Pattern.compile("#KT#").matcher(out).replaceAll(KT);
                    out = Pattern.compile("#KC#").matcher(out).replaceAll(KC);
                    out = Pattern.compile("#KMAX#").matcher(out).replaceAll(KMAX);
                    out = Pattern.compile("#KMIN#").matcher(out).replaceAll(KMIN);

                    if (first_loop) first_loop = false;
                    else {
                        entry_buffer.append("\n\n");
                    }

                    entry_buffer.append(out);
                }
            }

            content = Pattern.compile("#REPLICATED" + entry.getKey() + "#").matcher(
                    content).replaceAll(entry_buffer.toString());
        }

        return content;
    }


    private static void writeFile(String content, File output_file)
            throws IOException {

        File parent = output_file.getParentFile();
        makeDirs(parent);

        // Write to a temporary file
        File temp = File.createTempFile("trove", "gentemp",
                new File(System.getProperty("java.io.tmpdir")));
        Writer writer = new BufferedWriter(new FileWriter(temp));
        writer.write(content);
        writer.close();


        // Now determine if it should be moved to the final location
        final boolean need_to_move;
        if (output_file.exists()) {
            boolean matches;
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");

                byte[] current_file = digest(output_file, digest);
                byte[] new_file = digest(temp, digest);

                matches = Arrays.equals(current_file, new_file);
            } catch (NoSuchAlgorithmException ex) {
                System.err.println(
                        "WARNING: Couldn't load digest algorithm to compare " +
                                "new and old template. Generation will be forced.");
                matches = false;
            }

            need_to_move = !matches;
        } else need_to_move = true;


        // Now move it if we need to move it
        if (need_to_move) {
            delete(output_file);
	        copyFile( temp, output_file );
	        System.out.println("  Wrote: " + simplifyPath(output_file));
        } else {
            System.out.println("  Skipped: " + simplifyPath(output_file));
            delete(temp);
        }
    }

    /**
     * Delete the given file, throws IllegalStateException if delete operation fails
     *
     * @param output_file
     */
    private static void delete(File output_file) {
        if (!output_file.exists())
            return;

        if (!output_file.delete())
            throw new IllegalStateException("Could not delete " + output_file);
    }


    private static byte[] digest(File file, MessageDigest digest) throws IOException {
        digest.reset();

        byte[] buffer = new byte[1024];
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            int read = in.read(buffer);
            while (read >= 0) {
                digest.update(buffer, 0, read);

                read = in.read(buffer);
            }

            return digest.digest();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }


    /**
     * Abbreviate the type for Integer and Character
     */
    private static String abbreviate(String type) {
        if (type.equals("Integer")) {
            return "Int";
        } else if (type.equals("Character")) {
            return "Char";
        }
        return type;
    }

    private static String readFile(File input_file) throws IOException {
        if (!input_file.exists()) {
            throw new NullPointerException("Couldn't find: " + input_file);
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(input_file));
            StringBuilder out = new StringBuilder();

            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                out.append(line);
                out.append("\n");
            }
            return out.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }


    /**
     * Find replicated block definitions at the end of content.
     *
     * @param content_in  The content
     * @param content_out A StringBuffer into which the content (without the
     *                    definition blocks is placed). This will be untouched
     *                    if no definition blocks are found.
     * @return Null if no definition blocks are found, otherwise a map
     *         containing the blocks, keyed by their number.
     */
    static Map<Integer, String> findReplicatedBlocks(String content_in,
                                                     StringBuilder content_out) throws IOException {

        Map<Integer, String> to_return = null;

        // Find the replicated content blocks in the template. For ease, read this line
        // by line.
        BufferedReader reader = new BufferedReader(new StringReader(content_in));
        String line;
        StringBuilder buffer = new StringBuilder();
        boolean in_replicated_block = false;
        boolean need_newline = false;
        while ((line = reader.readLine()) != null) {
            if (!in_replicated_block &&
                    line.startsWith("====START_REPLICATED_CONTENT #")) {

                in_replicated_block = true;
                need_newline = false;

                if (content_out.length() == 0) {
                    content_out.append(buffer.toString());
                }

                buffer = new StringBuilder();
            } else if (in_replicated_block &&
                    line.startsWith("=====END_REPLICATED_CONTENT #")) {
                int number_start_index = "=====END_REPLICATED_CONTENT #".length();
                int number_end_index = line.indexOf("=", number_start_index);

                String number = line.substring(number_start_index, number_end_index);
                Integer number_obj = Integer.valueOf(number);

                if (to_return == null) to_return = new HashMap<Integer, String>();
                to_return.put(number_obj, buffer.toString());

                in_replicated_block = false;
                need_newline = false;
            } else {
                if (need_newline) buffer.append("\n");
                else need_newline = true;

                buffer.append(line);
            }
        }

        return to_return;
    }


    private static String simplifyPath(File file) {
        String output_string = root_output_dir.toString();

        String file_string = file.toString();
        return file_string.substring(output_string.length() + 1);
    }


    private static void cleanDir(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                cleanDir(file);
                delete(file);
            }

            delete(file);
        }
    }


	private static void copyFile( File source, File dest ) throws IOException {
		FileChannel srcChannel = new FileInputStream( source ).getChannel();
		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream( dest ).getChannel();
		// Copy file contents from source to destination
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
		// Close the channels
		srcChannel.close();
		dstChannel.close();
	}


    private static class WrapperInfo {
        final String primitive;
        final String class_name;
        final String max_value;
        final String min_value;

        WrapperInfo(String primitive, String class_name, String max_value,
                    String min_value) {

            this.primitive = primitive;
            this.class_name = class_name;
            this.max_value = class_name + "." + max_value;
            this.min_value = class_name + "." + min_value;
        }
    }
}
