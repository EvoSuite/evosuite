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
package de.unisb.cs.st.evosuite.primitives;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.setup.ResourceList;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Pool of interesting method sequences for different objects
 * 
 * @author Gordon Fraser
 * 
 */
public class ObjectPool {

	/** The actual object pool */
	private final Map<Type, Set<TestCase>> pool = new HashMap<Type, Set<TestCase>>();

	/** Singleton instance */
	private static ObjectPool instance;

	/** Wrapper class for sequence */
	private static class ObjectSequence {
		Type type;
		TestCase test;
	}

	private static Logger logger = LoggerFactory.getLogger(ObjectPool.class);

	/**
	 * Private constructor for singleton
	 */
	private ObjectPool() {
		if (Properties.OBJECT_POOL > 0.0)
			readPool();
	}

	/**
	 * Singleton accessor
	 * 
	 * @return
	 */
	public static ObjectPool getInstance() {
		if (instance == null)
			instance = new ObjectPool();

		return instance;
	}

	/**
	 * Check if there are sequences for given Type
	 * 
	 * @param clazz
	 * @return
	 */
	public boolean hasSequence(Type clazz) {
		return pool.containsKey(clazz);
	}

	/**
	 * Randomly choose a sequence for a given Type
	 * 
	 * @param clazz
	 * @return
	 */
	public TestCase getRandomSequence(Type clazz) {
		return Randomness.choice(pool.get(clazz));
	}

	/**
	 * Retrieve all possible sequences for a given Type
	 * 
	 * @param clazz
	 * @return
	 */
	public Set<TestCase> getSequences(Type clazz) {
		return pool.get(clazz);
	}

	/**
	 * Insert a new sequence for given Type
	 * 
	 * @param clazz
	 * @param sequence
	 */
	public void addSequence(Type clazz, TestCase sequence) {
		if (!pool.containsKey(clazz))
			pool.put(clazz, new HashSet<TestCase>());

		pool.get(clazz).add(sequence);
		logger.info("Added new sequence for " + clazz);
		logger.info(sequence.toCode());
	}

	private String getFileName(final Type clazz) {
		int num = 0;
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(((Class<?>) clazz).getName())
				        && name.endsWith(".seq");
			}
		};

		File[] files = (new File(Properties.OUTPUT_DIR + "/evosuite-pool")).listFiles(filter);
		num = files.length;
		return Properties.OUTPUT_DIR + "/evosuite-pool/" + ((Class<?>) clazz).getName()
		        + "_" + num + ".seq";
	}

	/**
	 * Write given sequence to disk
	 * 
	 * @param clazz
	 * @param sequence
	 */
	public void storeSequence(Type clazz, TestCase sequence) {
		try {
			File directory = new File(Properties.OUTPUT_DIR + "/evosuite-pool");
			if (!directory.exists())
				directory.mkdirs();

			OutputStream out = new FileOutputStream(getFileName(clazz));
			XStream xstream = new XStream();
			ObjectSequence os = new ObjectSequence();
			os.test = sequence;
			os.type = clazz;
			xstream.toXML(os, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read sequence from disk and add to pool
	 */
	private void loadSequence(String resourceName) {
		URL src = ClassLoader.getSystemResource(resourceName);
		try {
			InputStream in;
			in = src.openStream();
			XStream xstream = new XStream();
			ObjectSequence sequence = (ObjectSequence) xstream.fromXML(in);
			in.close();
			addSequence(sequence.type, sequence.test);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read pool of objects from disk
	 */
	private void readPool() {
		logger.info("Loading sequences from file");
		Pattern pattern = Pattern.compile("evosuite-pool/.*.seq");
		for (String resource : ResourceList.getResources(pattern)) {
			loadSequence(resource);
		}
	}

}
