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
package org.evosuite.classcreation;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

/**
 * Class to store information about stub's fields and field setters.
 *
 * @author Andrey Tarasevich
 */
public class StubField {
	
	private Expression fieldValue;
	private Type fieldType;
	private Type fieldSetterType;
	private String fieldName;
	
	/**
	 * <p>Constructor for StubField.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object.
	 * @param fieldValue a {@link org.eclipse.jdt.core.dom.Expression} object.
	 * @param fieldType a {@link org.eclipse.jdt.core.dom.Type} object.
	 * @param fieldSetterType a {@link org.eclipse.jdt.core.dom.Type} object.
	 */
	public StubField(String fieldName, Expression fieldValue,
			Type fieldType, Type fieldSetterType){
		this.fieldName = fieldName;
		this.fieldSetterType = fieldSetterType;
		this.fieldType = fieldType;
		this.fieldValue = fieldValue;
	}

	/**
	 * <p>Getter for the field <code>fieldValue</code>.</p>
	 *
	 * @return the fieldValue
	 */
	public Expression getFieldValue() {
		return fieldValue;
	}

	/**
	 * <p>Getter for the field <code>fieldType</code>.</p>
	 *
	 * @return the fieldType
	 */
	public Type getFieldType() {
		return fieldType;
	}

	/**
	 * <p>Getter for the field <code>fieldSetterType</code>.</p>
	 *
	 * @return the fieldSetterType
	 */
	public Type getFieldSetterType() {
		return fieldSetterType;
	}

	/**
	 * <p>Getter for the field <code>fieldName</code>.</p>
	 *
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
}
