/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.seeding;

import org.evosuite.Properties;
import org.evosuite.testcarver.extraction.CarvingManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ObjectPoolManager extends ObjectPool {

    private static final long serialVersionUID = 6287216639197977371L;

    private static ObjectPoolManager instance = null;

    private ObjectPoolManager() {
        initialisePool();
    }

    public static ObjectPoolManager getInstance() {
        if (instance == null)
            instance = new ObjectPoolManager();
        return instance;
    }

    public void addPool(ObjectPool pool) {
        for (GenericClass<?> clazz : pool.getClasses()) {
            Set<TestCase> tests = pool.getSequences(clazz);
            if (this.pool.containsKey(clazz))
                this.pool.get(clazz).addAll(tests);
            else
                this.pool.put(clazz, tests);
        }
    }

    public void initialisePool() {
        if (!Properties.OBJECT_POOLS.isEmpty()) {
            String[] poolFiles = Properties.OBJECT_POOLS.split(File.pathSeparator);
            if (poolFiles.length > 1)
                LoggingUtils.getEvoLogger().info("* Reading object pools:");
            else
                LoggingUtils.getEvoLogger().info("* Reading object pool:");
            for (String fileName : poolFiles) {
                logger.info("Adding object pool from file " + fileName);
                ObjectPool pool = ObjectPool.getPoolFromFile(fileName);
                if (pool == null) {
                    logger.error("Failed to load object from " + fileName);
                } else {
                    LoggingUtils.getEvoLogger().info(" - Object pool " + fileName + ": " + pool.getNumberOfSequences() + " sequences for " + pool.getNumberOfClasses() + " classes");
                    addPool(pool);
                }
            }
            if (logger.isDebugEnabled()) {
                for (GenericClass<?> key : pool.keySet()) {
                    logger.debug("Have sequences for " + key + ": " + pool.get(key).size());
                }
            }
        }
        if (Properties.CARVE_OBJECT_POOL) {
            CarvingManager manager = CarvingManager.getInstance();
            for (Class<?> targetClass : manager.getClassesWithTests()) {
                List<TestCase> tests = manager.getTestsForClass(targetClass);
                logger.info("Carved tests for {}: {}", targetClass.getName(), tests.size());
                GenericClass<?> cut = GenericClassFactory.get(targetClass);
                for (TestCase test : tests) {
                    this.addSequence(cut, test);
                }
            }
            logger.info("Pool after carving: " + this.getNumberOfClasses() + "/" + this.getNumberOfSequences());
        }
    }

    public void reset() {
        pool.clear();
        ObjectPoolManager.instance = null;
    }

}
