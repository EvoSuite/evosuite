/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.setup;

/**
 * @author gordon
 * 
 */
public class CallTreeEntry {

	private String sourceClass;

	private String sourceMethod;

	private String targetClass;

	private String targetMethod;

	public CallTreeEntry(String sourceClass, String sourceMethod, String targetClass,
	        String targetMethod) {
		this.sourceClass = sourceClass.replaceAll("/", ".");
		this.sourceMethod = sourceMethod;
		this.targetClass = targetClass.replaceAll("/", ".");
		this.targetMethod = targetMethod;
	}

	/**
	 * @return the sourceClass
	 */
	public String getSourceClass() {
		return sourceClass;
	}

	/**
	 * @param sourceClass
	 *            the sourceClass to set
	 */
	public void setSourceClass(String sourceClass) {
		this.sourceClass = sourceClass;
	}

	/**
	 * @return the sourceMethod
	 */
	public String getSourceMethod() {
		return sourceMethod;
	}

	/**
	 * @param sourceMethod
	 *            the sourceMethod to set
	 */
	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	/**
	 * @return the targetClass
	 */
	public String getTargetClass() {
		return targetClass;
	}

	/**
	 * @param targetClass
	 *            the targetClass to set
	 */
	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * @return the targetMethod
	 */
	public String getTargetMethod() {
		return targetMethod;
	}

	/**
	 * @param targetMethod
	 *            the targetMethod to set
	 */
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return sourceClass + "." + sourceMethod + " -> " + targetClass + "."
		        + targetMethod;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceClass == null) ? 0 : sourceClass.hashCode());
		result = prime * result + ((sourceMethod == null) ? 0 : sourceMethod.hashCode());
		result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
		result = prime * result + ((targetMethod == null) ? 0 : targetMethod.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CallTreeEntry other = (CallTreeEntry) obj;
		if (sourceClass == null) {
			if (other.sourceClass != null)
				return false;
		} else if (!sourceClass.equals(other.sourceClass))
			return false;
		if (sourceMethod == null) {
			if (other.sourceMethod != null)
				return false;
		} else if (!sourceMethod.equals(other.sourceMethod))
			return false;
		if (targetClass == null) {
			if (other.targetClass != null)
				return false;
		} else if (!targetClass.equals(other.targetClass))
			return false;
		if (targetMethod == null) {
			if (other.targetMethod != null)
				return false;
		} else if (!targetMethod.equals(other.targetMethod))
			return false;
		return true;
	}

}
