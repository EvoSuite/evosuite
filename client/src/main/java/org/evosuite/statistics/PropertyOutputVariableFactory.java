package org.evosuite.statistics;

import org.evosuite.Properties;

/**
 * Output variable that represents a value stored in the properties
 * 
 * @author gordon
 *
 */
public class PropertyOutputVariableFactory {

	private String propertyName;
	
	public PropertyOutputVariableFactory(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public OutputVariable<String> getVariable() {
		try {
			return new OutputVariable<String>(propertyName, Properties.getStringValue(propertyName));
		} catch (Exception e) {
			// TODO: What would be better?
			return new OutputVariable<String>(propertyName, "error");
		}
	}

}
