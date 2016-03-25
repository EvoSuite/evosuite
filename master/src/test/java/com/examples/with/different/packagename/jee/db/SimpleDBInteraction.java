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
package com.examples.with.different.packagename.jee.db;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Andrea Arcuri on 17/08/15.
 */
public class SimpleDBInteraction {

    @PersistenceContext
    private EntityManager em;


    public void persist( String key , String value){

        KVPair_0 pair = new KVPair_0(key, value);

        em.persist(pair);
    }

    public boolean check(String key, String value){

        KVPair_0 pair = em.find(KVPair_0.class , key);
        if(pair==null){
            System.out.println("Not found");
            return false;
        }

        if(pair.getValue().equals(value)){
            System.out.println("Match");
            return true;
        } else {
            System.out.println("No match");
            return false;
        }
    }

}
