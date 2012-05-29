/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.primitives;

/**
 * @author Gordon Fraser
 *
 */
public class StringReplacementFunctions {

	public static int isEmptyDistance(String str) {
		if(str.isEmpty())
			return 0;
		else
			return str.length() + 1;
	}
	
	public static int min(int a, int b, int c) {
		if(a < b)
			return Math.min(a, c);
		else
			return Math.min(b, c);
	}
	
	public static int OED(String s1, String s2) {
		// The edit distance of an empty string and a given string is the length of the given string
		int n = s1.length ();
		int m = s2.length ();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		
		String s = s1.substring(0, n - 1);
		char a = s1.charAt(n - 1);

		String t = s2.substring(0, m - 1);
		char b = s2.charAt(m - 1);

		int cost = 0;
		if(Math.abs(a - b) > 0)
			cost = 127/4 +3 * Math.abs(a - b)/4;
		
		return min(OED(s+a, t) + 127, OED(s, t+b) + 127, OED(s, t) + cost);
	}

	public static int editDistance (String s, String t)
	{
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost
		
		int k = 127;
		
		// Step 1

		n = s.length ();
		m = t.length ();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n+1][m+1];

		// Step 2

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3

		for (i = 1; i <= n; i++) {

			s_i = s.charAt (i - 1);

			// Step 4

			for (j = 1; j <= m; j++) {

				t_j = t.charAt (j - 1);

				// Step 5

				if (s_i == t_j) {
					cost = 0;
				}
				else {
//					cost = 127/4 + 3 * Math.abs(s_i - t_j)/4;
					cost = 127;
				}

				// Step 6

				d[i][j] = min (d[i-1][j]+k, d[i][j-1]+k, d[i-1][j-1] + cost);

			}

		}

	// Step 7

		return d[n][m];
	}
	
	public static int getLevenshteinDistance (String s, String t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		    The difference between this impl. and the previous is that, rather 
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking 
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really 
		     copied anymore, just switched...this is clearly much better than cloning an array 
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not 
		     cause an out of memory condition when calculating the LD over two very large strings.  		
		 */		

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n+1]; //'previous' cost array, horizontally
		int d[] = new int[n+1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i<=n; i++) {
			p[i] = i;
		}

		for (j = 1; j<=m; j++) {
			t_j = t.charAt(j-1);
			d[0] = j;

			for (i=1; i<=n; i++) {
				cost = s.charAt(i-1)==t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
				d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		} 

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}
	
	public static int equalsDistance(String first, String second) {
		if(first.equals(second))
			return 0; // Identical
		else {
			//return OED(first, second);
			return editDistance(first, second);
//			return getLevenshteinDistance(first, second);
			/*
			int l1 = first.length();
			int l2 = second.length();
			int distance = Math.abs(l1 - l2);
			for(int i = 0; i<Math.min(l1, l2); i++) {
				if(first.charAt(i) != second.charAt(i))
					distance++;
			}
			return distance;
			*/
		}
	}
	
	public static int equalsIgnoreCaseDistance(String first, String second) {
		return equalsDistance(first.toLowerCase(), second.toLowerCase());
	}
	
	public static int startsWith(String value, String prefix) {
		int len = Math.min(prefix.length(), value.length());
		return equalsDistance(value.substring(0, len), prefix);
	}
	
}
