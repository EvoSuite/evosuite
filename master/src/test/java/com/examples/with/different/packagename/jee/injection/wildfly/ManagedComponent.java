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

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;


@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ManagedComponent {
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private UnManagedComponent helper;

    public String updateKeyValueDatabase(String key, String value) {
        try {
            userTransaction.begin();
            String result = helper.updateKeyValueDatabase(entityManager, key, value);
            userTransaction.commit();
            return result;
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            try {
                if (userTransaction.getStatus() == Status.STATUS_ACTIVE)
                    userTransaction.rollback();
            } catch (Throwable e) {
                // ignore
            }
        }
    }
}
