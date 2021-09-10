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


public class TestCase72 {
    public static class Pat {
        public static String Reverse(String s) {
            // RTN REVERSE OF s
            int slen = s.length();
            if (slen < 2) {
                return s;
            }
            String result = "";
            // var result : System.Text.StringBuilder = new
            // System.Text.StringBuilder(slen);
            // var i : int = slen - 1;
            for (int i = slen - 1; i >= 0; i--) {
                // result.Append(s[i]);
                result += s.charAt(i);
            }
            // Console.WriteLine("s {0} revs {1}", s, result.ToString());
            return result;
        }

        public int Subject(String txt, String pat) {
            // SEARCH txt FOR FIRST OCCURRENCE OF pat OR REVERSE OF pat
            // IF pat (STRING OF LENGTH AT LEAST 3) OCCURS IN txt, RTN 1
            // IF REVERSE OF pat OCCURS IN txt, RTN 2
            // IF pat AND REVERSE OF pat OCCURS IN txt, RTN 3
            // IF PALINDROME CONSISTING OF pat FOLLOWED BY REVERSE pat OCCURS IN
            // txt, RTN 4
            // IF PALINDROME CONSISTING OF REVERSE pat FOLLOWED pat OCCURS IN
            // txt, RTN 5
            int result = 0;
            int i = 0;
            int j = 0;
            int txtlen = txt.length();
            int patlen = pat.length();
            String possmatch = null;

            if (patlen > 2) {
                String patrev = Reverse(pat);
                for (i = 0; i <= txtlen - patlen; i++) {
                    if (txt.charAt(i) == pat.charAt(0)) {
                        possmatch = txt.substring(i, patlen);
                        if (possmatch.equals(pat)) {
                            // FOUND pat
                            result = 1;
                            // CHECK IF txt CONTAINS REVERSE pat
                            for (j = i + patlen; j <= txtlen - patlen; j++) {
                                if (txt.charAt(j) == patrev.charAt(0)) {
                                    possmatch = txt.substring(j, patlen);
                                    if (possmatch.equals(patrev)) {
                                        if (j == i + patlen) {
                                            return i;// 4;
                                        } else {
                                            return i;// 3;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (txt.charAt(i) == patrev.charAt(0)) {
                        possmatch = txt.substring(i, patlen);
                        if (possmatch.equals(patrev)) {
                            // FOUND pat REVERSE
                            result = 2;
                            // CHECK IF txt CONTAINS pat
                            for (j = i + patlen; j <= txtlen - patlen; j++) {
                                if (txt.charAt(j) == pat.charAt(0)) {
                                    possmatch = txt.substring(j, patlen);
                                    if (possmatch.equals(pat)) {
                                        if (j == i + patlen) {
                                            return i;// 5;
                                        } else {
                                            return i;// 3;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // pat NOR REVERSE FOUND
            }
            return result;
        }

    }

    /**
     * @param args
     */
//	String string0 = ConcolicMarker.mark("enci", "string0");
//	String string9 = ConcolicMarker.mark("nov", "string9");
    public static void test(String string0, String string9) {
        String string1 = Pat.Reverse(string0);
        Pat pat0 = new Pat();
        int int0 = pat0.Subject(string0, string0);
        int int1 = pat0.Subject(string0, string0);
        int int2 = pat0.Subject(string0, string1);
        String string2 = Pat.Reverse(string0);
        String string3 = Pat.Reverse(string0);
        Pat pat1 = new Pat();
        String string4 = Pat.Reverse(string1);
        String string5 = Pat.Reverse(string0);
        int int3 = pat1.Subject(string2, string4);
        String string6 = Pat.Reverse(string0);
        Pat pat2 = new Pat();
        String string7 = Pat.Reverse(string3);
        Pat pat3 = new Pat();
        String string8 = Pat.Reverse(string3);
        Pat pat4 = new Pat();
        int int4 = pat1.Subject(string3, string1);
        int int5 = pat0.Subject(string3, string4);
        Pat pat5 = new Pat();
        int int6 = pat2.Subject(string4, string6);
        int int7 = pat1.Subject(string9, string1);
        int int8 = pat2.Subject(string7, string9);
        Pat pat6 = new Pat();
        String string10 = Pat.Reverse(string8);
    }

}
