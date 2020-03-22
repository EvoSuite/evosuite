package org.evosuite.utils;

import org.evosuite.runtime.classhandling.ClassResetter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ClassUtil {

     /**
       * Returns a set with the static methods of a class
       *
       * @param targetClass a class instance
       * @return
       */
     public static List<Method> getTargetClassStaticMethods(Class<?> targetClass) {
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        List<Method> targetStaticMethods = new LinkedList<Method>();
        for (Method m : declaredMethods) {

          if (!Modifier.isStatic(m.getModifiers())) {
            continue;
          }

          if (Modifier.isPrivate(m.getModifiers())) {
            continue;
          }

          if (m.getName().equals(ClassResetter.STATIC_RESET)) {
            continue;
          }

          targetStaticMethods.add(m);
        }
        return targetStaticMethods;
     }

}
