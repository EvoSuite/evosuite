/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.junit;

import java.util.ArrayList;
import java.util.List;

/**
 * The information regarding a failure from executing a JUnit test case needed
 * by the JUnitAnalyzer
 * 
 * @author galeotti
 *
 */
public class JUnitFailure {

	public JUnitFailure(String message, String exceptionClassName,
			String descriptionMethodName, boolean isAssertionError, String trace) {
		super();
		this.message = message;
		this.exceptionClassName = exceptionClassName;
		this.descriptionMethodName = descriptionMethodName;
		this.isAssertionError = isAssertionError;
		this.trace = trace;
	}

	private final String descriptionMethodName;
	private final String exceptionClassName;
	private final String message;
	private final boolean isAssertionError;
	private final String trace;
	private final ArrayList<String> exceptionStackTrace = new ArrayList<String>();

	public void addToExceptionStackTrace(String elemToString) {
		exceptionStackTrace.add(elemToString);
	}

	public String getDescriptionMethodName() {
		return descriptionMethodName;
	}

	public String getExceptionClassName() {
		return exceptionClassName;
	}

	public String getMessage() {
		return message;
	}

	public List<String> getExceptionStackTrace() {
		return this.exceptionStackTrace;
	}

	public String getTrace() {
		return trace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((exceptionClassName == null) ? 0 : exceptionClassName
						.hashCode());
		result = prime
				* result
				+ ((exceptionStackTrace == null) ? 0 : exceptionStackTrace
						.hashCode());
		result = prime * result + (isAssertionError ? 1231 : 1237);
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime
				* result
				+ ((descriptionMethodName == null) ? 0 : descriptionMethodName
						.hashCode());
		result = prime * result + ((trace == null) ? 0 : trace.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JUnitFailure other = (JUnitFailure) obj;
		if (exceptionClassName == null) {
			if (other.exceptionClassName != null)
				return false;
		} else if (!exceptionClassName.equals(other.exceptionClassName))
			return false;
		if (exceptionStackTrace == null) {
			if (other.exceptionStackTrace != null)
				return false;
		} else if (!exceptionStackTrace.equals(other.exceptionStackTrace))
			return false;
		if (isAssertionError != other.isAssertionError)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (descriptionMethodName == null) {
			if (other.descriptionMethodName != null)
				return false;
		} else if (!descriptionMethodName.equals(other.descriptionMethodName))
			return false;
		if (trace == null) {
			if (other.trace != null)
				return false;
		} else if (!trace.equals(other.trace))
			return false;
		return true;
	}

	public boolean isAssertionError() {
		return this.isAssertionError;
	}

}
