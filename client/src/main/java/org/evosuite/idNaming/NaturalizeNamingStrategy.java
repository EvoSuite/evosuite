/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p/>
 * This file is part of EvoSuite.
 * <p/>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 * <p/>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import codemining.java.tokenizers.JavaTokenizer;
import codemining.languagetools.Scope;
import org.evosuite.Properties;
import org.evosuite.testcase.ImportsTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import renaming.renamers.AbstractIdentifierRenamings;
import renaming.renamers.BaseIdentifierRenamings;
import renaming.renamers.INGramIdentifierRenamer.Renaming;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Jose Rojas
 */
public class NaturalizeNamingStrategy extends DefaultNamingStrategy {

	private static final Logger logger = LoggerFactory.getLogger(NaturalizeNamingStrategy.class);

	private Map<TestCase, String> testToCodeMap = new HashMap<>();

	private final AbstractIdentifierRenamings renamer;

	public NaturalizeNamingStrategy(ImportsTestCodeVisitor itv) {
		super(itv);

		// initialize Naturalize renamer
		renamer = initRenamingModel();
	}

	private AbstractIdentifierRenamings initRenamingModel() {
		AbstractIdentifierRenamings renamer = new BaseIdentifierRenamings(new JavaTokenizer());
		Collection<File> trainingFiles = getTrainingFiles();
		if (! trainingFiles.isEmpty())
			renamer.buildRenamingModel(trainingFiles);
		else
			logger.error("Training data directory \"{}\" does not contain any .java file.", Properties.VARIABLE_NAMING_TRAINING_DATA_DIR);
		return renamer;
	}

	@Override
	public String getArrayReferenceName(TestCase testCase, ArrayReference var) {
		return getVariableName(testCase, var);
	}

	@Override
	public String getVariableName(TestCase testCase, VariableReference var) {
		if (! testToCodeMap.containsKey(testCase)) {
			TestCodeVisitor visitor = new TestCodeVisitor();
			testCase.accept(visitor);
			String code = visitor.getCode();
			testToCodeMap.put(testCase, code);
		}
		String dummyName = super.getVariableName(testCase, var);
		String name = getNaturalizeRenaming(testToCodeMap.get(testCase), dummyName);
		variableNames.put(var, name);
		return name;
	}

	private String getNaturalizeRenaming(String code, String id){
		Scope scope = new Scope(code, Scope.ScopeType.SCOPE_LOCAL, null, -1, -1);

		try {
			logger.debug("Calling Naturalize for variable {0}:", id);
			final SortedSet<Renaming> renamings = renamer.getRenamings(scope, id);
			logger.debug("Renamings for variable {}: {}", id, renamings);
			String newId = renamings.first().name;
			return newId.equals("UNK_SYMBOL") ? id : newId;
		} catch (NullPointerException e) {
			// swallow exception
			logger.debug("Naturalize failed to generate any renaming for {}.", id);
			return id;
		}

	}

	@Override
	public void reset() {
		super.reset();
		testToCodeMap.clear();
	}

	private Collection<File> getTrainingFiles() {
		if (Properties.VARIABLE_NAMING_TRAINING_DATA_DIR == null) {
			logger.error("Nonexisting or empty training data directory.");
			return new LinkedList<>();
		}
		File folder = new File(Properties.VARIABLE_NAMING_TRAINING_DATA_DIR);
		if (! folder.exists()) {
			logger.error("Training data directory \"{}\" does not exist.", Properties.VARIABLE_NAMING_TRAINING_DATA_DIR);
			return new LinkedList<>();
		}
		Collection<File> listOfFiles = listFiles(folder, (dir, name) -> { return name.endsWith(".java"); });
		return listOfFiles;
	}

	private Collection<File> listFiles(File directory, FilenameFilter fileFilter) {
		Collection<File> files = new LinkedList<>();
		File[] listOfFiles = directory.listFiles();
		for (File f : listOfFiles) {
			if (fileFilter.accept(directory, f.getName())) {
				files.add(f);
			} else if (f.isDirectory()) {
				Collection<File> innerFiles = listFiles(f, fileFilter);
				files.addAll(innerFiles);
			}
		}
		logger.debug("Naturalize training files: {}", files);
		return files;
	}
}
