/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.evosuite.parameterize.InputVariable;
import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jose Rojas
 */
public abstract class AbstractVariableNamingStrategy implements VariableNamingStrategy {

	private static final Logger logger = LoggerFactory.getLogger(AbstractVariableNamingStrategy.class);

	protected final ImportsTestCodeVisitor itv;

    protected Map<VariableReference,VariableNamePair> variableNames = new HashMap<>();

	public AbstractVariableNamingStrategy(ImportsTestCodeVisitor itv) {
		this.itv = itv;
	}

	public String getConstantName(ConstantValue cval) {
		if(cval.getValue() != null && cval.getVariableClass().equals(Class.class)) {
			return this.itv.getClassNames().get((Class<?>)cval.getValue())+".class";
		}
		return cval.getName();
	}

	public String getFieldReferenceName(TestCase testCase, FieldReference var) {
		VariableReference source = var.getSource();
		GenericField field = var.getField();
		if (source != null)
			return getVariableName(testCase, source) + "." + field.getName();
		else
			return this.itv.getClassNames().get(field.getField().getDeclaringClass()) + "."
					+ field.getName();
	}

	public String getArrayIndexName(TestCase testCase, ArrayIndex var) {
		VariableReference array = var.getArray();
		List<Integer> indices = var.getArrayIndices();
		String result = getVariableName(testCase, array);
		for (Integer index : indices) {
			result += "[" + index + "]";
		}
		return result;
	}

	public abstract String getArrayReferenceName(TestCase testCase, ArrayReference var);

	public abstract String getVariableName(TestCase testCase, VariableReference var);

	@Override
	public String getName(TestCase testCase, VariableReference var) {
        if (var instanceof ConstantValue) {
			return getConstantName((ConstantValue) var);
		} else if (var instanceof InputVariable) {
			return var.getName();
		} else if (var instanceof FieldReference) {
			return getFieldReferenceName(testCase, (FieldReference) var);
		} else if (var instanceof ArrayIndex) {
			return getArrayIndexName(testCase, (ArrayIndex) var);
		} else if (variableNames.containsKey(var)) {
	        return variableNames.get(var).name;
        } else if (var instanceof ArrayReference) {
            return getArrayReferenceName(testCase, (ArrayReference) var);
		} else {
            return getVariableName(testCase, var);
		}
	}

	@Override
	public String getPlaceholder(TestCase testCase, VariableReference var) {
		String name = getName(testCase, var);
		if (variableNames.containsKey(var))
			return String.format("{%d}",  variableNames.get(var).placeholderIndex);
		else {
			name = name.replaceAll("[']", "''"); // escape single quotes
			name = "'" + name + "'"; // make sure string is ignored for placeholders
		}
		return name;
	}

	public void reset() {
		variableNames.clear();
	}

	public String finalize(String testCode) {
		Map<Integer, String> auxMap = new HashMap<>();

		for (VariableNamePair entry : variableNames.values())
			auxMap.put(entry.placeholderIndex, entry.name);

		String[] args = new String[Collections.max(auxMap.keySet()) + 1];
		for (Map.Entry<VariableReference, VariableNamePair> entry : variableNames.entrySet()) {
			String name = entry.getValue().name;
			String newName = checkUnique(auxMap, name);
			if (! name.equals(newName))
				variableNames.put(entry.getKey(), new VariableNamePair(entry.getValue().placeholderIndex, newName));
			args[entry.getValue().placeholderIndex] = newName;
		}
		logger.debug("Finalizing testCode:\n" + testCode);
		System.out.println("Finalizing testCode:" + testCode);
		logger.debug("Args: " + Arrays.toString(args));
		System.out.println("Args: " + Arrays.toString(args));
		System.out.println("---");
		return MessageFormat.format(testCode, args);
	}

	private String checkUnique(Map<Integer, String> map, String name) {
		if (name.endsWith("0")) {
			String prefix = name.substring(0, name.length()-1);
			if (! map.values().contains(prefix + "1") && ! isJavaKeyword(prefix))
				return prefix;
		}
		return name;
	}

	protected void put(VariableReference var, String name) {
		VariableNamePair namePair = variableNames.get(var);
		if (namePair == null) {
			int newIndex = variableNames.size();
			variableNames.put(var, new VariableNamePair(newIndex, name));
			logger.debug("Put new variable reference: {}; placeholderIndex: {}; name: {}",
					var.toString(), newIndex, name);
		} else {
			logger.error("Ignored attempt to replace variable name: {}; placeholderIndex: {}; name: {}; new name: {}",
					var.toString(), namePair.placeholderIndex, namePair.name, name);
			assert(false) : "Cannot replace final variable name.";
		}

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		String format = "%-10s | %s | %s\n";
		sb.append(String.format(format, "varRef", "placeholder", "name"));
		for (Map.Entry<VariableReference, VariableNamePair> varEntry : variableNames.entrySet()) {
			VariableReference var = varEntry.getKey();
			sb.append(String.format(format, var, varEntry.getValue().placeholderIndex, varEntry.getValue().name));
		}
		return sb.toString();
	}

	static final String keywords[] = { "abstract", "assert", "boolean",
			"break", "byte", "case", "catch", "char", "class", "const",
			"continue", "default", "do", "double", "else", "extends", "false",
			"final", "finally", "float", "for", "goto", "if", "implements",
			"import", "instanceof", "int", "interface", "long", "native",
			"new", "null", "package", "private", "protected", "public",
			"return", "short", "static", "strictfp", "super", "switch",
			"synchronized", "this", "throw", "throws", "transient", "true",
			"try", "void", "volatile", "while" };

	public static boolean isJavaKeyword(String keyword) {
		return (Arrays.binarySearch(keywords, keyword) >= 0);
	}

	/**
	 * Class VariableNamePair
	 *
	 * Pair of variable name and placeholder index.
	 *
	 */
	public class VariableNamePair {
		public final String name;
		public final int placeholderIndex;

		public VariableNamePair(int placeHolderIndex, String name) {
			this.placeholderIndex = placeHolderIndex;
			this.name = name;
		}
	}




}
