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
package org.evosuite.executionmode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;
import org.evosuite.Properties;
import org.evosuite.Properties.Parameter;
import org.evosuite.utils.LoggingUtils;

/**
 * Class used to list on the console all the options in Properties
 * 
 * @author arcuri
 *
 */
public class ListParameters {

	public static final String NAME = "listParameters";

	public static Option getOption(){
		return new Option(NAME,"list all the parameters that can be set with -D");
	}

	public static Object execute(){

		List<Row> rows = new ArrayList<Row>();

		/*
		 * This is necessary, as reading from evosuite-files properties
		 * can change the defaults
		 */
		Properties.getInstance().resetToDefaults();
		
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				Parameter p = f.getAnnotation(Parameter.class);

				String description = p.description();
				Class<?> type = f.getType();
				
				if(type.isEnum()){
					description += " (Values: "+Arrays.toString(type.getEnumConstants())+")";
				}
				
				String def;
				try {
					Object obj = f.get(null);
					if(obj==null){
						def = "";
					} else {
						if(type.isArray()){
							def = Arrays.toString((Object[])obj);
						} else {
							def =  obj.toString();
						}
					}
				} catch (Exception e) {
					def = "";
				} 
				
				rows.add(new Row(p.key(), type.getSimpleName(), description, def));
			}
		}

		Collections.sort(rows);
		
		String name = "Name";
		String type = "Type";
		String defaultValue = "Default";
		String description = "Description";
		String space = "   ";
		
		int maxName = Math.max(name.length(),getMaxNameLength(rows));
		int maxType = Math.max(type.length(),getMaxTypeLength(rows));
		int maxDefault = Math.max(defaultValue.length(),getMaxDefaultLength(rows));
		
		LoggingUtils.getEvoLogger().info(name + getGap(name,maxName) + space + type + getGap(type,maxType) + 
				space + defaultValue + getGap(defaultValue,maxDefault) + space + description);

		for(Row row : rows){
			LoggingUtils.getEvoLogger().info(row.name + getGap(row.name,maxName) + space + row.type + getGap(row.type,maxType) + 
					space + row.defaultValue + getGap(row.defaultValue,maxDefault) + space + row.description);
		}

		return null;
	}

	private static String getGap(String s, int max){
		String gap = "";
		for(int i=0; i < (max-s.length()); i++){
			gap += " ";
		}
		return gap;
	}
	
	private static int getMaxNameLength(List<Row> rows){
		int max = 0;
		for(Row row : rows){
			if(row.name.length() > max){
				max = row.name.length();
			}
		}
		return max;
	}

	private static int getMaxDefaultLength(List<Row> rows){
		int max = 0;
		for(Row row : rows){
			if(row.defaultValue.length() > max){
				max = row.defaultValue.length();
			}
		}
		return max;
	}
	private static int getMaxTypeLength(List<Row> rows){
		int max = 0;
		for(Row row : rows){
			if(row.type.length() > max){
				max = row.type.length();
			}
		}
		return max;
	}
	
	private static class Row implements Comparable<Row>{
		public final String name;
		public final String type;
		public final String description;
		public final String defaultValue;

		public Row(String name, String type, String description, String defaultValue) {
			super();
			this.name = name;
			this.type = type;
			this.description = description;
			this.defaultValue = defaultValue;
		}

		public int compareTo(Row other){
			return this.name.compareTo(other.name);
		}
	}
}
