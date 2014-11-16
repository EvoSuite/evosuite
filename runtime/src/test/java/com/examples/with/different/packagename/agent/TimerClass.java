package com.examples.with.different.packagename.agent;

import java.util.Timer;

public class TimerClass {

	public static final String NAME = "foo";
	
	private Timer timer;
	
	public TimerClass(){
		timer = new Timer(NAME);
	}
}
