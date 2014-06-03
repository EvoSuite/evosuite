package org.evosuite.setup;

/**
 * The superclass of an entry of the static usage graph.
 * The static usage graph contains information regarding how static fields are used
 * by static methods.
 *  
 * @author galeotti
 */
abstract class GetStaticGraphEntry {

	private final String sourceClass;
	private final String sourceMethod;
	private final String targetClass;

	public GetStaticGraphEntry(String sourceClass, String sourceMethod,
			String targetClass) {
		super();
		this.sourceClass = sourceClass;
		this.sourceMethod = sourceMethod;
		this.targetClass = targetClass;
	}

	public String getSourceClass() {
		return sourceClass;
	}

	public String getSourceMethod() {
		return sourceMethod;
	}

	public String getTargetClass() {
		return targetClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sourceClass == null) ? 0 : sourceClass.hashCode());
		result = prime * result
				+ ((sourceMethod == null) ? 0 : sourceMethod.hashCode());
		result = prime * result
				+ ((targetClass == null) ? 0 : targetClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GetStaticGraphEntry))
			return false;
		GetStaticGraphEntry other = (GetStaticGraphEntry) obj;
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
		return true;
	}

}
