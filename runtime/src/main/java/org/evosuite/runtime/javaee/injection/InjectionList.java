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
package org.evosuite.runtime.javaee.injection;

import org.evosuite.PackageInfo;
import org.evosuite.runtime.util.Inputs;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provide a list of JavaEE tags which we handle for dependency injection
 *
 * Created by Andrea Arcuri on 30/05/15.
 */
public class InjectionList {

    private static final List<Class<? extends Annotation>> list =
            Collections.unmodifiableList(Arrays.<Class<? extends Annotation>>asList(
                    javax.inject.Inject.class,
                    javax.persistence.PersistenceContext.class,
                    javax.persistence.PersistenceUnit.class,
                    javax.annotation.Resource.class,
                    org.springframework.beans.factory.annotation.Autowired.class,
                    javax.ejb.EJB.class,
                    javax.xml.ws.WebServiceRef.class,
                    javax.faces.bean.ManagedProperty.class,
                    javax.ws.rs.core.Context.class
    ));

    public static List<Class<? extends Annotation>> getList(){
        return list;
    }

    public static boolean isValidForInjection(Class<? extends Annotation> annotation) {
        return isValidForInjection(annotation,list);
    }

    public static boolean isValidForInjection(Class<? extends Annotation> annotation,
                                              List<Class<? extends Annotation>> tagsToCheck){
        Inputs.checkNull(annotation);
        String name = annotation.getName();

        String shadedPrefix = PackageInfo.getShadedPackageForThirdPartyLibraries()+".";
        for(Class<?> c : tagsToCheck){
            String cn = c.getName();
            if(name.equals(cn)){
                return true;
            }
            if(cn.startsWith(shadedPrefix)){
                if((shadedPrefix + name).equals(cn)){
                    return true;
                }
            }
        }

        return false;
    }
}
