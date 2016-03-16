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
package org.evosuite.runtime.javaee.javax.persistence;

import org.evosuite.runtime.util.Inputs;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.Map;

/**
 * This class is wrapper over an actual EntityManager.
 * It is used to detect at runtime which of its methods are called,
 * and react accordingly
 *
 * Created by Andrea Arcuri on 16/06/15.
 */
public class EvoEntityManager implements EntityManager{

    /*
     *  TODO intercept SQL queries, and create data accordingly as
     *  part of the search. However, it is very likely that already
     *  the SUT has methods to create such data
     */


    private final EntityManager em;

    private final EntityManagerFactory factory;

    public EvoEntityManager(EntityManager em, EntityManagerFactory factory) throws IllegalArgumentException{
        Inputs.checkNull(em,factory);
        this.em = em;
        this.factory = factory;
    }


    // ----------  EntityManager methods ----------------

    @Override
    public void persist(Object entity) {
        em.persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        return em.merge(entity);
    }

    @Override
    public void remove(Object entity) {
        em.remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return em.find(entityClass,primaryKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return em.find(entityClass,primaryKey,properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return em.find(entityClass,primaryKey,lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return em.find(entityClass,primaryKey,lockMode,properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return em.getReference(entityClass,primaryKey);
    }

    @Override
    public void flush() {
        em.flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        em.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return em.getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        em.lock(entity,lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        em.lock(entity,lockMode,properties);
    }

    @Override
    public void refresh(Object entity) {
        em.refresh(entity);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        em.refresh(entity,properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        em.refresh(entity,lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        em.refresh(entity,lockMode,properties);
    }

    @Override
    public void clear() {
        em.clear();
    }

    @Override
    public void detach(Object entity) {
        em.detach(entity);
    }

    @Override
    public boolean contains(Object entity) {
        return em.contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return em.getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        em.setProperty(propertyName,value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return em.getProperties();
    }

    @Override
    public Query createQuery(String qlString) {
        return em.createQuery(qlString);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return em.createQuery(criteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return em.createQuery(updateQuery);
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return em.createQuery(deleteQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return em.createQuery(qlString,resultClass);
    }

    @Override
    public Query createNamedQuery(String name) {
        return em.createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return em.createNamedQuery(name,resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return em.createNativeQuery(sqlString);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return em.createNativeQuery(sqlString,resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return em.createNativeQuery(sqlString,resultSetMapping);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return em.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return em.createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return em.createStoredProcedureQuery(procedureName,resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return em.createStoredProcedureQuery(procedureName,resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        em.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return em.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return em.unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return em.getDelegate();
    }

    @Override
    public void close() {
        em.close();
    }

    @Override
    public boolean isOpen() {
        return em.isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return em.getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return factory;
        //return em.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return em.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return em.getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return em.createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return em.createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return em.getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return em.getEntityGraphs(entityClass);
    }
}
