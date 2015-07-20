package org.evosuite.runtime.javaee.injection;

import org.evosuite.runtime.util.Inputs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to determine which fields should be injected
 * in a "general" way: ie, instance object is created in the
 * test as any other input parameter. This is different
 * from "special" fields that need to be adhoc initialized
 * by EvoSuite, like EntityManager that needs to be conned
 * to an actual database
 *
 * Created by Andrea Arcuri on 13/07/15.
 */
public class GeneralInjection {

    private final Map<Class<?>, List<Field>> cache;

    /**
     *  Caches of injected fields that are treated specially
     */
    private final Map<Class<?>, InjectionCache> specials;

    public GeneralInjection(InjectionCache... specials) throws IllegalArgumentException{
        this.cache = new LinkedHashMap<>();
        this.specials = new LinkedHashMap<>();
        for(InjectionCache ic : specials){
            Class<?> fieldClass = ic.getFieldClass();
            if(this.specials.containsKey(fieldClass)){
                throw new IllegalArgumentException("Field of type "+
                        fieldClass + "has more than one cache");
            }
            this.specials.put(fieldClass , ic);
        }
    }

    public void reset(){
        cache.clear();
    }

    public List<Field> getFieldsToInject(Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);

        List<Field> fields = cache.get(klass);
        if(fields==null){
            fields = new ArrayList<>();
            List<Class<? extends Annotation>> list = InjectionList.getList();
            for(Field f : klass.getDeclaredFields()){
                Class<?> fieldClass = f.getType();
                if(specials.containsKey(fieldClass)){
                    //this is already handled, so skip it
                    continue;
                }
                for(Annotation annotation : f.getDeclaredAnnotations()){
                    Class<? extends Annotation> annotationClass = annotation.annotationType();
                    if(list.contains(annotationClass)){
                        fields.add(f);
                        break;
                    }
                }
            }
            cache.put(klass, fields);
        }

        return fields;
    }
}
