package com.examples.with.different.packagename.concolic;

public class TestCase70 {

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
	public static void main(String[] args) {
		String string0 = "";
		String string1 = "";
		Pat pat0 = new Pat();
		String string2 = Pat.Reverse(string0);
		int int0 = pat0.Subject(string2, string1);
	}

}
