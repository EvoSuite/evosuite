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

import org.evosuite.runtime.annotation.BoundInputVariable;
import org.evosuite.runtime.javaee.db.DBManager;
import org.evosuite.runtime.util.Inputs;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Class used to quickly check if a given class has an injectable field for the given
 * type and annotations
 *
 * Created by Andrea Arcuri on 16/06/15.
 */
public class InjectionCache {

    /**
     * Key -> class name,
     * Value -> name of injected field tagged with annotation
     */
    private final Map<String, String> cache = new LinkedHashMap<>();

    private final Class<?> fieldClass;

    private final List<Class<? extends Annotation>> annotations;


    public InjectionCache(Class<?> fieldClass, Class<? extends Annotation>... annotations){
        Inputs.checkNull(fieldClass,annotations);

        this.fieldClass = fieldClass;
        this.annotations = Collections.unmodifiableList(Arrays.asList(annotations));
    }


    public  String getFieldName( Class<?> clazz) throws IllegalArgumentException{

        Inputs.checkNull(clazz);

        if(!hasField(clazz)){
            throw new IllegalArgumentException("The class " + clazz.getName() +
                    " does not have a valid injectable field for " + fieldClass.getName());
        }

        String field = cache.get(clazz.getName());
        assert field != null;

        return field;
    }

    public Field getField(Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);

        if(!hasField(klass)){
            return null;
        }

        try {
            return klass.getDeclaredField(cache.get(klass.getName()));
        } catch (NoSuchFieldException e) {
            //should never happen
            return null;
        }
    }

    public  boolean hasField( Class<?> klass) throws IllegalArgumentException{

        Inputs.checkNull(klass);

        String className = klass.getName();
        if(! cache.containsKey(className)){
            String fieldName = null;
            outer : for(Field field : klass.getDeclaredFields()){
                if(! fieldClass.isAssignableFrom(field.getType()) ){
                    continue;
                }
                for(Annotation annotation : field.getDeclaredAnnotations()){
                    for(Class<? extends Annotation> valid : annotations){
                        if(valid.isAssignableFrom(annotation.getClass())){
                            fieldName = field.getName();
                            break outer;
                        }
                    }
                }
            }
            cache.put(className,fieldName); //can be null
        }

        String f = cache.get(className);
        return f != null;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }
}
