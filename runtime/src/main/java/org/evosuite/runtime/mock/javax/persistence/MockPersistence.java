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
package org.evosuite.runtime.mock.javax.persistence;

import org.evosuite.runtime.javaee.db.DBManager;
import org.evosuite.runtime.mock.OverrideMock;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUtil;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrea Arcuri on 30/05/15.
 */
public class MockPersistence extends Persistence implements OverrideMock{


    // -------- mocked methods  ----------------------

    public MockPersistence() {
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        return DBManager.getInstance().getDefaultFactory();
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        return DBManager.getInstance().getDefaultFactory();    }

    public static PersistenceUtil getPersistenceUtil() {
        return Persistence.getPersistenceUtil();
    }

    // -------  EvoSuite methods ---------------------


}
