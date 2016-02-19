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
package org.evosuite.testcase.jee;

import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods used to do JEE dependency injection.
 *
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSupport {

    private static final Logger logger = LoggerFactory.getLogger(InjectionSupport.class);

    private static volatile GenericMethod entityManager;
    private static volatile GenericMethod entityManagerFactory;
    private static volatile GenericMethod userTransaction;
    private static volatile GenericMethod event;
    private static volatile GenericMethod postConstruct;
    private static volatile GenericMethod generalField;
    private static volatile GenericMethod validateBean;


    public static GenericMethod getValidateBean(){
        if(validateBean==null){
            try {
                validateBean = new GenericMethod(
                        Injector.class.getDeclaredMethod("validateBean",Object.class, Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return validateBean;
    }

    public static GenericMethod getInjectorForGeneralField(){
        if(generalField == null){
            try {
                generalField = new GenericMethod(
                        Injector.class.getDeclaredMethod("inject",
                                Object.class,Class.class, String.class, Object.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return generalField;
    }


    public static GenericMethod getPostConstruct(){
        if(postConstruct == null){
            try {
                postConstruct = new GenericMethod(
                        Injector.class.getDeclaredMethod("executePostConstruct",Object.class, Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return postConstruct;
    }

    public static GenericMethod getInjectorForEntityManager(){
        if(entityManager == null){
            try {
                entityManager = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectEntityManager",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return entityManager;
    }

    public static GenericMethod getInjectorForEntityManagerFactory(){
        if(entityManagerFactory == null){
            try {
                entityManagerFactory = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectEntityManagerFactory",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return entityManagerFactory;
    }

    public static GenericMethod getInjectorForUserTransaction(){
        if(userTransaction == null){
            try {
                userTransaction = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectUserTransaction",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return userTransaction;
    }

    public static GenericMethod getInjectorForEvent(){
        if(event == null){
            try {
                event = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectEvent",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return event;
    }

}
