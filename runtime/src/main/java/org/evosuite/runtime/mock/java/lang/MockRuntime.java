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
package org.evosuite.runtime.mock.java.lang;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.evosuite.runtime.System.SystemExitException;
import org.evosuite.runtime.jvm.ShutdownHookHandler;
import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.io.MockIOException;


public class MockRuntime implements StaticReplacementMock{

	public String getMockedClassName(){
		return java.lang.Runtime.class.getName();
	}
	
	// ---- static methods -------
 		
	public static Runtime getRuntime() {
		/*
		 * return actual instance, because we cannot instantiate a new one,
		 * and anyway will never been used directly in an unsafe mode
		 */ 
		return java.lang.Runtime.getRuntime();
	}

	public static void runFinalizersOnExit(boolean value) {		
		//Shutdown.setRunFinalizersOnExit(value);
		//nothing to do
	}

	// ----- instance replacement methods -------------
	
	public static void exit(Runtime runtime, int status) {
		/*
		 * TODO: move this exception class here once we remove old System mock
		 */
		throw new SystemExitException();
	}

	public static void addShutdownHook(Runtime runtime,Thread hook) {
		/*
		 * this is going to be handled specially by ShutdownHookHandler.
		 * The mocking is implemented in this special way to handle all cases in which
		 * addShutdownHook is called by API that we do not mock and that cannot
		 * be instrumented
		 */
		runtime.addShutdownHook(hook);
	}

	public static boolean removeShutdownHook(Runtime runtime, Thread hook) {		
		/*
		 * this is going to be handled specially by ShutdownHookHandler
		 */
		return runtime.removeShutdownHook(hook);
	}

	public static void halt(Runtime runtime, int status) {		
		ShutdownHookHandler.getInstance().processWasHalted();
		throw new SystemExitException();
	}

	public static Process exec(Runtime runtime, String command) throws IOException {
		return exec(runtime, command, null, null);
	}

	public static Process exec(Runtime runtime, String command, String[] envp) throws IOException {
		return exec(runtime, command, envp, null);
	}

	public static Process exec(Runtime runtime, String command, String[] envp, File dir) throws IOException {
		
		if (command.length() == 0)
			throw new MockIllegalArgumentException("Empty command");

		StringTokenizer st = new StringTokenizer(command);
		String[] cmdarray = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
			cmdarray[i] = st.nextToken();
		return exec(runtime, cmdarray, envp, dir);
	}

	public static Process exec(Runtime runtime, String[] cmdarray) throws IOException {
		return exec(runtime, cmdarray, null, null);
	}

	public static Process exec(Runtime runtime, String[] cmdarray, String[] envp) throws IOException {
		return exec(runtime, cmdarray, envp, null);
	}


	public static Process exec(Runtime runtime, String[] cmdarray, String[] envp, File dir)
			throws IOException {
		/*
		return new ProcessBuilder(cmdarray)
		.environment(envp)
		.directory(dir)
		.start();
		*/
		//TODO mock ProcessBuilder 
		throw new MockIOException("Cannot start processes in a unit test");
	}

	public static  void gc(Runtime runtime){
		//do nothing
	}

	public static void runFinalization(Runtime runtime) {
		//runFinalization0();
		//do nothing
	}

	
	public static void traceInstructions(Runtime runtime, boolean on){
		//do nothing
	}

	public static void traceMethodCalls(Runtime runtime, boolean on){
		//do nothing
	}

	
	public static void load(Runtime runtime, String filename) {
		//load0(Reflection.getCallerClass(), filename);
		runtime.load(filename);  // we need to load the actuall stuff
	}

	public static void loadLibrary(Runtime runtime, String libname) {
		//loadLibrary0(Reflection.getCallerClass(), libname);
		runtime.loadLibrary(libname); // we need to load the actuall stuff
	}


	public static InputStream getLocalizedInputStream(Runtime runtime, InputStream in) {
		// inlined runtime.getLocalizedInputStream for Java 11 compatibility.
		return in;
	}

	public static OutputStream getLocalizedOutputStream(Runtime runtime, OutputStream out) {
		// inlined runtime.getLocalizedOutputStream for Java 11 compatibility.
		return out;
	}

	//-------------------------------------------------
	// for the following methods, we return reasonable values. Technically those returned values could
	// be part of the search. But most likely it would be not useful to increase coverage in typical cases
	// Note: we still need them to be deterministic, and not based on actual Runtime
	
	public static int availableProcessors(Runtime runtime){
		return 1; 
	}

	public static  long freeMemory(Runtime runtime){
		return 200;
	}


	public static  long totalMemory(Runtime runtime){
		return 400; 
	}


	public static  long maxMemory(Runtime runtime){
		return 500; 
	}
	
	//-------------------------------------------------
}
