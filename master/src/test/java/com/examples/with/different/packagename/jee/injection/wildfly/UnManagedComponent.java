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
/*
 * From JBoss, Apache License Apache License, Version 2.0
 */
package com.examples.with.different.packagename.jee.injection.wildfly;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.List;

public class UnManagedComponent {
    @PersistenceUnit(unitName = "primary")
    private EntityManagerFactory entityManagerFactory;

    @Inject
    private UserTransaction userTransaction;

    public String updateKeyValueDatabase(String key, String value) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            userTransaction.begin();

            entityManager.joinTransaction();

            String result = updateKeyValueDatabase(entityManager, key, value);

            userTransaction.commit();

            return result;
        } catch (RollbackException e) {
            Throwable t = e.getCause();

            return t != null ? t.getMessage() : e.getMessage();
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            try {
                if (userTransaction.getStatus() == Status.STATUS_ACTIVE)
                    userTransaction.rollback();
            } catch (Throwable e) {
            }

            entityManager.close();
        }
    }

    public String updateKeyValueDatabase(EntityManager entityManager, String key, String value) {
        StringBuilder sb = new StringBuilder();

        if (key == null || key.length() == 0) {
            @SuppressWarnings("unchecked")
            final List<KVPair_2> list = entityManager.createQuery("select k from KVPair_2 k").getResultList();

            for (KVPair_2 kvPair : list)
                sb.append(kvPair.getKey()).append("=").append(kvPair.getValue()).append(',');

        } else {
            KVPair_2 kvPair;

            if (value == null) {
                kvPair = new KVPair_2(key, value);

                entityManager.refresh(kvPair);
            } else {
                kvPair = entityManager.find(KVPair_2.class, key);

                if (kvPair == null) {
                    kvPair = new KVPair_2(key, value);
                    entityManager.persist(kvPair);
                } else {
                    kvPair.setValue(value);
                    entityManager.persist(kvPair);
                }
            }

            sb.append(kvPair.getKey()).append("=").append(kvPair.getValue());
        }

        return sb.toString();
    }
}
