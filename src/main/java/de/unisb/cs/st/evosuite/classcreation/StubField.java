/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.classcreation;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Type;

/**
 * Class to store information about stub's fields and field setters.
 * 
 * @author Andrey Tarasevich
 *
 */
public class StubField {
	
	private Expression fieldValue;
	private Type fieldType;
	private Type fieldSetterType;
	private String fieldName;
	
	public StubField(String fieldName, Expression fieldValue,
			Type fieldType, Type fieldSetterType){
		this.fieldName = fieldName;
		this.fieldSetterType = fieldSetterType;
		this.fieldType = fieldType;
		this.fieldValue = fieldValue;
	}

	/**
	 * @return the fieldValue
	 */
	public Expression getFieldValue() {
		return fieldValue;
	}

	/**
	 * @return the fieldType
	 */
	public Type getFieldType() {
		return fieldType;
	}

	/**
	 * @return the fieldSetterType
	 */
	public Type getFieldSetterType() {
		return fieldSetterType;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
}
