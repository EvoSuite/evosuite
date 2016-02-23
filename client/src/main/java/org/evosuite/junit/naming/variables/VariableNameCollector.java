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
package org.evosuite.junit.naming.variables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by gordon on 23/12/2015.
 */
public class VariableNameCollector {

    private static final Logger logger = LoggerFactory.getLogger(VariableNameCollector.class);

    private static VariableNameCollector ourInstance = new VariableNameCollector();

    public static VariableNameCollector getInstance() {
        return ourInstance;
    }

    private Map<String, Map<String, List<String>>> parameterMap = new LinkedHashMap<>();

    private Map<String, Map<String, Integer>> typeNameMap = new LinkedHashMap<>();

    private VariableNameCollector() {
    }

    public void addParameterName(String className, String methodName, int numParam, String name) {
        if(!parameterMap.containsKey(className))
            parameterMap.put(className, new LinkedHashMap<>());
        if(!parameterMap.get(className).containsKey(methodName))
            parameterMap.get(className).put(methodName, new ArrayList<>());
        parameterMap.get(className).get(methodName).add(name);
    }

    public boolean hasParameterName(String className, String methodName, int numParam) {
        if(!parameterMap.containsKey(className)) {
            logger.debug("Don't have class "+className +": "+parameterMap.keySet());
            return false;
        }
        if(!parameterMap.get(className).containsKey(methodName)) {
            logger.debug("Don't have method "+methodName +": "+parameterMap.get(className).keySet());
            return false;
        }
        if(parameterMap.get(className).get(methodName).size() <= numParam) {
            logger.debug("Don't have parameter "+numParam+": "+parameterMap.get(className).get(methodName));
            return false;
        }
        return true;
    }

    public String getParameterName(String className, String methodName, int numParam) {

        return parameterMap.get(className).get(methodName).get(numParam);
    }

    public void addVariableName(String typeName, String name) {
        logger.debug("Adding name "+name+" for type "+typeName);
        if(name.equals("this"))
            return;

        if(!typeNameMap.containsKey(typeName))
            typeNameMap.put(typeName, new LinkedHashMap<>());

        Map<String, Integer> nameMap = typeNameMap.get(typeName);

        if(!nameMap.containsKey(name))
            nameMap.put(name, 1);
        else
            nameMap.put(name, nameMap.get(name) + 1);
    }

    public Map<String, Integer> getNameMap(String typeName) {
        return typeNameMap.get(typeName);
    }

    public Set<String> getTypeNames() {
        return typeNameMap.keySet();
    }
}
