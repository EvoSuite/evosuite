package org.evosuite.executionmode;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Continuous {

	public static final String NAME = "continuous";
	
	public static Option getOption(){
		return new Option(NAME,true,"Run Continuous Test Generation");
	}

	public static Object execute(Options options, List<String> javaOpts,
			CommandLine line, String cp) {
		
		//TODO
		
		return null;
	}
	
}
