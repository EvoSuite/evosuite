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
package org.evosuite.runtime.javaee.db;

import org.hibernate.internal.SessionImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Andrea Arcuri on 31/05/15.
 */
public class DBManagerIntTest {

    @Before
    public void init(){
        DBManager.getInstance().initDB();
    }

    @Test
    public void testTableInitialization() throws SQLException {
        Connection c = ((SessionImpl) DBManager.getInstance().getCurrentEntityManager().getDelegate()).connection();
        Statement s = c.createStatement();

        Set<String> tables = new LinkedHashSet<>();
        ResultSet rs = s.executeQuery("select table_name " +
                "from INFORMATION_SCHEMA.system_tables " +
                "where table_type='TABLE' and table_schem='PUBLIC'");
        while (rs.next()) {
            if (!rs.getString(1).startsWith("DUAL_")) {
                tables.add(rs.getString(1));
            }
        }
        rs.close();
        s.close();

        Assert.assertTrue(tables.contains("KVPair_table".toUpperCase()));
    }

    @Test
    public void testDirectSQLModifications() throws SQLException {

        String tableName = "KVPair_table";

        Connection c = ((SessionImpl) DBManager.getInstance().getCurrentEntityManager().getDelegate()).connection();
        Statement s = c.createStatement();

        ResultSet rs = s.executeQuery("select * from "+tableName);
        Assert.assertFalse(rs.next()); // no data
        rs.close();

        s.executeUpdate("INSERT INTO KVPair_table VALUES 'a', 'b'");

        rs = s.executeQuery("SELECT * from "+tableName);
        Assert.assertTrue(rs.next());
        String a = rs.getString(1);
        String b = rs.getString(2);
        rs.close();

        Assert.assertEquals("a", a);
        Assert.assertEquals("b", b);

        s.executeUpdate("DELETE from " + tableName);
        rs = s.executeQuery("select * from "+tableName);
        Assert.assertFalse(rs.next()); // no data

        s.close();
    }


    @Test
    public void testDirectSQLInsertFollowedByClear() throws SQLException {

        String tableName = "KVPair_table";

        Connection c = ((SessionImpl) DBManager.getInstance().getCurrentEntityManager().getDelegate()).connection();
        Statement s = c.createStatement();

        ResultSet rs = null;

        s.executeUpdate("INSERT INTO KVPair_table VALUES 'a', 'b'");

        rs = s.executeQuery("SELECT * from "+tableName);
        Assert.assertTrue(rs.next());
        rs.close();
        s.close();

        DBManager.getInstance().clearDatabase();

        s = c.createStatement();
        rs = s.executeQuery("SELECT * from " + tableName);
        Assert.assertFalse(rs.next()); // no data
        rs.close();
        s.close();
    }


    @Test
    public void testClearDatabase() throws Exception {

        boolean cleared = false;

        //cleaning up should work
        DBManager.getInstance().initDB();
        cleared = DBManager.getInstance().clearDatabase();
        Assert.assertTrue(cleared);

        String key = "foo";
        String value = "bar";
        KVPair pair = new KVPair(key,value);

        EntityManager em = DBManager.getInstance().getCurrentEntityManager();

        em.getTransaction().begin();

        KVPair queried = null;

        queried = em.find(KVPair.class,key);
        Assert.assertNull(queried); //not inserted yet

        em.persist(pair);
        queried = em.find(KVPair.class,key);
        Assert.assertNotNull(queried); //now it should be found
        Assert.assertEquals(value, queried.getValue());

        em.getTransaction().commit();

        EntityManager secondEM = DBManager.getInstance().getDefaultFactory().createEntityManager();
        queried = secondEM.find(KVPair.class, key);
        Assert.assertNotNull(queried); //should be found
        secondEM.close();

        //clean the db again
        cleared = DBManager.getInstance().clearDatabase();
        Assert.assertTrue(cleared);


        EntityManager thirdEM = DBManager.getInstance().getDefaultFactory().createEntityManager();
        queried = thirdEM.find(KVPair.class, key);
        Assert.assertNull(queried); //should not be found
        thirdEM.close();


        //object should be found in first em, as its cache is not updated
        queried = em.find(KVPair.class, key);
        Assert.assertNotNull(queried);
    }
}