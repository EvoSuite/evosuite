/**
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
package com.examples.with.different.packagename.jee.db;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by gordon on 28/04/2017.
 */
public class SimpleDBWrite {
    @PersistenceContext
    private EntityManager em;


    public String persist( String key , String value){

        KVPair_0 pair = new KVPair_0(key, value);

        em.persist(pair);
        KVPair_0 pair2 = em.find(KVPair_0.class , key);
        return pair2.getValue();

    }
}
