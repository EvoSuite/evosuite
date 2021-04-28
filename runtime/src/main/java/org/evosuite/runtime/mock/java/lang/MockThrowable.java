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

import java.io.PrintStream;
import java.io.PrintWriter;

import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;

public class MockThrowable extends Throwable  implements OverrideMock {

	private static final long serialVersionUID = 4078375023919805371L;

	private StackTraceElement[]  stackTraceElements;

	private Class<?> originClass;

	// ------ constructors -------------

	public MockThrowable() {
		super();
		init();
	}

	public MockThrowable(String message) {
		super(message);
		init();
	}

	public MockThrowable(Throwable cause) {
		super(cause);
		init();
	}

	public MockThrowable(String message, Throwable cause) {
		super(message, cause);
		init();
	}

	protected MockThrowable(String message, Throwable cause,
			boolean enableSuppression,
			boolean writableStackTrace) {
		super(message,cause,enableSuppression,writableStackTrace);
		init();
	}


	// ----- just for mock --------

	private void init(){
		stackTraceElements = getDefaultStackTrace();

		StackTraceElement[] original = super.getStackTrace();
		if(original.length > 0) {
			stackTraceElements[0] = original[0]; //only copy over the first element, which points to the source
		}
	}

	public void setOriginForDelegate(StackTraceElement origin) throws IllegalArgumentException{
		stackTraceElements[0] = origin;
	}

	/*
	 *  ------ special replacements static methods ------------
	 *  
	 *  WARN: don't modify the name of these methods, as they are used by reflection in instrumentator
	 */

    public static StackTraceElement[] getDefaultStackTrace(){
        StackTraceElement[] v =  new StackTraceElement[3];
        v[0] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
        v[1] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
        v[2] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
        return v;
    }

    public static StackTraceElement[] replacement_getStackTrace(Throwable source){
		if(!MockFramework.isEnabled() || source instanceof EvoSuiteMock){
			return source.getStackTrace();
		}
		
		return getDefaultStackTrace();
	}
	
	public static void replacement_printStackTrace(Throwable source, PrintWriter p) {
		if(!MockFramework.isEnabled() || source instanceof EvoSuiteMock){
			source.printStackTrace(p);
		}		
		for(StackTraceElement elem : getDefaultStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}
	}
	
	public static void replacement_printStackTrace(Throwable source, PrintStream p) {
		if(!MockFramework.isEnabled() || source instanceof EvoSuiteMock){
			source.printStackTrace(p);
		}
		for(StackTraceElement elem : getDefaultStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}
	}
	
	// ----- unmodified public methods ----------

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public String getLocalizedMessage() {
		return super.getLocalizedMessage();
	}

	@Override
	public synchronized Throwable getCause() {
		return super.getCause();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}


	// ------  mocked public methods --------

	@Override
	public synchronized Throwable initCause(Throwable cause) {

		if(!MockFramework.isEnabled()){
			return super.initCause(cause);
		}

		try{
			return super.initCause(cause);
		} catch(IllegalStateException e){
			throw new MockIllegalStateException(e.getMessage());
		} catch(IllegalArgumentException e){
			throw new MockIllegalArgumentException(e.getMessage()); //FIXME
		}
	}

	@Override
	public void printStackTrace(PrintStream p) {

		if(!MockFramework.isEnabled()){
			super.printStackTrace(p);
			return;
		}

		for(StackTraceElement elem : getStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}
	}

	@Override
	public void printStackTrace(PrintWriter p) {

		if(!MockFramework.isEnabled()){
			super.printStackTrace(p);
			return;
		}

		for(StackTraceElement elem : getStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}		
	}

	/**
	@Override
	public synchronized Throwable fillInStackTrace() {
		if(!MockFramework.isEnabled()){
			return super.fillInStackTrace();
		}

		return this;
	}
	*/

	@Override
	public StackTraceElement[] getStackTrace() {		

		if(!MockFramework.isEnabled()){
			return super.getStackTrace();
		}


		return stackTraceElements;
	}

	@Override
	public void setStackTrace(StackTraceElement[] stackTrace) {

		if(!MockFramework.isEnabled()){
			super.setStackTrace(stackTrace);
			return;
		}


		StackTraceElement[] defensiveCopy = stackTrace.clone();
		for (int i = 0; i < defensiveCopy.length; i++) {
			if (defensiveCopy[i] == null)
				throw new MockNullPointerException("stackTrace[" + i + "]");  //FIXME
		}

		synchronized (this) {            
			this.stackTraceElements = defensiveCopy;
		}
	}

	/*
	 *  getSuppressed() and addSuppressed() are final, and likely not
	 *  needed to be mocked anyway
	 *
	 */
}
