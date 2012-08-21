package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase46 {

	public static class FileSuffix
	{
		public int  Subject(String directory , String file ) 
		{
			//EG pathname = "...WORD/FILE.DOC";
			// files : Object[];
			String[] fileparts = null;
			//var lastfile : int = 0;
			int lastpart  = 0;
			String suffix = null;  
			fileparts = file.split(".");
			lastpart = fileparts.length - 1;
			if (lastpart > 0) {
				suffix = fileparts[lastpart];
				//Console.WriteLine("{0}, {1}", directory, suffix);
				if ("text".equals(directory)) {
					if ("txt".equals(suffix)) {
						//print("text");
					}
				}
				if ("acrobat".equals(directory)) {
					if ("pdf".equals(suffix)) {					 
						//print("acrobat");
					}
				}
				if ("word".equals(directory)) {
					if ("doc".equals(suffix)) {
						//print("word");
					}
				}
				if ("bin".equals(directory)) {
					if ("exe".equals(suffix)) {
						//print("bin");
					}
				}
				if ("lib".equals(directory)) {
					if ("dll".equals(suffix)) {
						//print("lib");
					}
				}
			}
			return 1;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FileSuffix fileSuffix0 = new FileSuffix();
		String string0 = ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string0");
		String string1 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string1");
		String string2 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string2");
		String string3 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string3");
		String string4 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string4");
		String string5 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string5");
		String string6 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string6");
		String string7 =  ConcolicMarker.mark("\\5b;\u001C,?\u0011\u0010\u001E]\"","string7");
		int int0 = fileSuffix0.Subject(string0, string7);
		FileSuffix fileSuffix1 = new FileSuffix();
		int int1 = fileSuffix0.Subject(string6, string5);
		FileSuffix fileSuffix2 = new FileSuffix();
		FileSuffix fileSuffix3 = new FileSuffix();
		String string8 =  ConcolicMarker.mark("ness","string8");
		int int2 = fileSuffix1.Subject(string4, string8);
		String string9 =  ConcolicMarker.mark("iciti","string9");
		String string10 =  ConcolicMarker.mark("iciti","string10");
		int int3 = fileSuffix2.Subject(string3, string9);
		String string11 =  ConcolicMarker.mark("doc","string11");
		int int4 = fileSuffix1.Subject(string11, string10);
		String string12 =  ConcolicMarker.mark("text","string12");
		String string13 =  ConcolicMarker.mark("text","string13");
		String string14 =  ConcolicMarker.mark("text","string14");
		String string15 = ConcolicMarker.mark( "text","string15");
		String string16 =  ConcolicMarker.mark("text","string16");
		String string17 =  ConcolicMarker.mark("text","string17");
		int int5 = fileSuffix3.Subject(string12, string17);
		FileSuffix fileSuffix4 = new FileSuffix();
		int int6 = fileSuffix2.Subject(string2, string16);
		String string18 = ConcolicMarker.mark( "","string18");
		String string19 =  ConcolicMarker.mark("","string19");
		int int7 = fileSuffix2.Subject(string18, string19);
		int int8 = fileSuffix3.Subject(string1, string15);
		String string20 = ConcolicMarker.mark( "urVf3T\r\t\u0019\u000B0 eiM I","string20");
		String string21 = ConcolicMarker.mark( "urVf3T\r\t\u0019\u000B0 eiM I","string21");
		String string22 =  ConcolicMarker.mark("urVf3T\r\t\u0019\u000B0 eiM I","string22");
		FileSuffix fileSuffix5 = new FileSuffix();
		int int9 = fileSuffix3.Subject(string14, string20);
		String string23 =  ConcolicMarker.mark("ical","string23");
		int int10 = fileSuffix5.Subject(string23, string13);
		int int11 = fileSuffix3.Subject(string22, string21);

	}

}
