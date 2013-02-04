package org.evosuite.symbolic;

public class TestCase93 {


	public static class DateParse
	{
		public void Subject(String dayname , String monthname)  
		{
			int result = 0;
			//int month = -1;
			dayname = dayname.toLowerCase();
			monthname = monthname.toLowerCase();

			if ("mon".equals(dayname) || 
					"tue".equals(dayname) || 
					"wed".equals(dayname) || 
					"thur".equals(dayname) || 
					"fri".equals(dayname) || 
					"sat".equals(dayname) || 
					"sun".equals(dayname)) {
				result = 1; 
			}       
			if ("jan".equals(monthname)) {
				result += 1;
			}
			if ("feb".equals(monthname)) {
				result += 2;
			}
			if ("mar".equals(monthname)) {
				result += 3;
			}
			if ("apr".equals(monthname)) {
				result += 4;
			}
			if ("may".equals(monthname)) {
				result += 5;
			}
			if ("jun".equals(monthname)) {
				result += 6;
			}
			if ("jul".equals(monthname)) {
				result += 7;
			}
			if ("aug".equals(monthname)) {
				result += 8;
			}
			if ("sep".equals(monthname)) {
				result += 9;
			}
			if ("oct".equals(monthname)) {
				result += 10;
			}
			if ("nov".equals(monthname)) {
				result += 11;
			}
			if ("dec".equals(monthname)) {
				result += 12;
			}
		}

	}
	
	public static void test(String string0, String string1) {
		//String string0 = "Ou\u0016";
		//String string1 = "Ou\u0016";
		DateParse dateParse0 = new DateParse();
		DateParse dateParse1 = new DateParse();
		dateParse0.Subject(string0, string1);
	}
}
