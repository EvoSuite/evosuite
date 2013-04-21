package org.evosuite.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

public class GenericArrayTypeImpl implements GenericArrayType {

	 private Type componentType;
     
     public static Class<?> createArrayType(Class<?> componentType) {
             // there's no (clean) other way to create a array class, than creating an instance of it
             return Array.newInstance(componentType, 0).getClass();
     }
     
     public static Type createArrayType(Type componentType) {
             if (componentType instanceof Class) {
                     return createArrayType((Class<?>)componentType);
             } else {
                     return new GenericArrayTypeImpl(componentType);
             }
     }
     
     private GenericArrayTypeImpl(Type componentType) {
             super();
             this.componentType = componentType;
     }

     public Type getGenericComponentType() {
             return componentType;
     }
     
     @Override
     public boolean equals(Object obj) {
             if (!(obj instanceof GenericArrayType))
                     return false;
             return componentType.equals(((GenericArrayType)obj).getGenericComponentType());
     }
     
     @Override
     public int hashCode() {
             return componentType.hashCode() * 7;
     }
     
     @Override
     public String toString() {
             return componentType + "[]";
     }
}
