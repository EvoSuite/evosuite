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
package com.examples.with.different.packagename.concolic;

public class TestCase46 {

    public static class FileSuffix {
        public int Subject(String directory, String file) {
            // EG pathname = "...WORD/FILE.DOC";
            // files : Object[];
            String[] fileparts = null;
            // var lastfile : int = 0;
            int lastpart = 0;
            String suffix = null;
            fileparts = file.split(".");
            lastpart = fileparts.length - 1;
            if (lastpart > 0) {
                suffix = fileparts[lastpart];
                // Console.WriteLine("{0}, {1}", directory, suffix);
                if ("text".equals(directory)) {
                    if ("txt".equals(suffix)) {
                        // print("text");
                    }
                }
                if ("acrobat".equals(directory)) {
                    if ("pdf".equals(suffix)) {
                        // print("acrobat");
                    }
                }
                if ("word".equals(directory)) {
                    if ("doc".equals(suffix)) {
                        // print("word");
                    }
                }
                if ("bin".equals(directory)) {
                    if ("exe".equals(suffix)) {
                        // print("bin");
                    }
                }
                if ("lib".equals(directory)) {
                    if ("dll".equals(suffix)) {
                        // print("lib");
                    }
                }
            }
            return 1;
        }
    }

    /**
     * @param args
     */

    public static void test(String string0, String string1, String string2,
                            String string3, String string4, String string5, String string6,
                            String string7, String string8, String string9, String string10,
                            String string11, String string12, String string13, String string14,
                            String string15, String string16, String string17, String string18,
                            String string19, String string20, String string21, String string22,
                            String string23) {

        FileSuffix fileSuffix0 = new FileSuffix();
        int int0 = fileSuffix0.Subject(string0, string7);
        FileSuffix fileSuffix1 = new FileSuffix();
        int int1 = fileSuffix0.Subject(string6, string5);
        FileSuffix fileSuffix2 = new FileSuffix();
        FileSuffix fileSuffix3 = new FileSuffix();
        int int2 = fileSuffix1.Subject(string4, string8);
        int int3 = fileSuffix2.Subject(string3, string9);
        int int4 = fileSuffix1.Subject(string11, string10);
        int int5 = fileSuffix3.Subject(string12, string17);
        FileSuffix fileSuffix4 = new FileSuffix();
        int int6 = fileSuffix2.Subject(string2, string16);
        int int7 = fileSuffix2.Subject(string18, string19);
        int int8 = fileSuffix3.Subject(string1, string15);
        FileSuffix fileSuffix5 = new FileSuffix();
        int int9 = fileSuffix3.Subject(string14, string20);
        int int10 = fileSuffix5.Subject(string23, string13);
        int int11 = fileSuffix3.Subject(string22, string21);

    }

}
