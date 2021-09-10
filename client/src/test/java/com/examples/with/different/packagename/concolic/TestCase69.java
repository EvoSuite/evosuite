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

public class TestCase69 {
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

    public static void test() {
        Pat pat0 = new Pat();
        String string0 = "r1{";
        String string1 = "r1{";
        String string2 = "r1{";
        String string3 = "r1{";
        String string4 = "r1{";
        String string5 = "r1{";
        String string6 = "r1{";
        String string7 = "r1{";
        String string8 = "r1{";
        String string9 = Pat.Reverse(string0);
        int int0 = pat0.Subject(string8, string7);
        String string10 = Pat.Reverse(string6);
        int int1 = pat0.Subject(string5, string4);
        String string11 = "h:\u0018n mH]a";
        String string12 = "h:\u0018n mH]a";
        String string13 = "h:\u0018n mH]a";
        String string14 = "h:\u0018n mH]a";
        String string15 = "h:\u0018n mH]a";
        String string16 = Pat.Reverse(string11);
        String string17 = Pat.Reverse(string3);
        Pat pat1 = new Pat();
        int int2 = pat1.Subject(string16, string15);
        int int3 = pat1.Subject(string14, string16);
        String string18 = Pat.Reverse(string2);
        int int4 = pat1.Subject(string16, string16);
        int int5 = pat0.Subject(string10, string9);
        Pat pat2 = new Pat();
        String string19 = Pat.Reverse(string16);
        int int6 = pat1.Subject(string16, string10);
        int int7 = pat1.Subject(string16, string16);
        int int8 = pat1.Subject(string16, string16);
        String string20 = Pat.Reverse(string16);
        String string21 = Pat.Reverse(string19);
        String string22 = Pat.Reverse(string13);
        Pat pat3 = new Pat();
        int int9 = pat0.Subject(string20, string1);
        int int10 = pat2.Subject(string17, string12);
        Pat pat4 = new Pat();
        Pat pat5 = new Pat();
    }
}
