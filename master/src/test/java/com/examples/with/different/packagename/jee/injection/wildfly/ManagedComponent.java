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
