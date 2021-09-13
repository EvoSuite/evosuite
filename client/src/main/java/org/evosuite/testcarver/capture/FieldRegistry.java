/*
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
package org.evosuite.testcarver.capture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class FieldRegistry {
    private static final Map<String, ReferenceQueue<?>> classRefQueueMapping = new LinkedHashMap<>();
    private static final Map<String, List<MyWeakRef<?>>> classInstanceMapping = new LinkedHashMap<>();

    private static final Map<Integer, Map<String, WeakReference<?>>> instanceRecentFieldValuesMapping = new LinkedHashMap<>();

    private static final Map<String, Map<String, Field>> classFieldsMapping = new LinkedHashMap<>();

    private static final Set<Class<?>> CLASSES = new LinkedHashSet<>();

    private static final Set<Integer> registeredObjects = new LinkedHashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(FieldRegistry.class);

    private static int captureId = Integer.MAX_VALUE;

    public static ClassLoader carvingClassLoader = null;

    private FieldRegistry() {
    }

    synchronized public static void register(final Object instance) {
        if (!Capturer.isCapturing()) {
            return;
        }
        try {
            final Class<?> clazz;

            if (instance instanceof Class) {
                clazz = (Class<?>) instance;
            } else {
                clazz = instance.getClass();
            }

            final String internalClassName = clazz.getName().replace('.', '/');
            registeredObjects.add(System.identityHashCode(instance));
            cleanUpReferences(internalClassName);

            Map<String, Field> observedFields = classFieldsMapping.get(internalClassName);
            if (observedFields == null) {
                // determine observable fields

                observedFields = new LinkedHashMap<>();

                collectAccessibleFields(observedFields, clazz, null);

                //if (observedFields.isEmpty()) {
                //	logger.debug("Class {} has no observable fields", clazz);
                //	classFieldsMapping.put(internalClassName, Collections.EMPTY_MAP);
                //} else {
                //	classFieldsMapping.put(internalClassName, observedFields);
                //}
            }
        } catch (Throwable t) {
            logger.debug("ARgh");
        }

//		if (!observedFields.isEmpty()) {
//			List<MyWeakRef<?>> instances = classInstanceMapping.get(internalClassName);
//			ReferenceQueue<?> refQueue = classRefQueueMapping.get(internalClassName);
//			if (instances == null) {
//				instances = new ArrayList<MyWeakRef<?>>();
//				refQueue = new ReferenceQueue();
//				classInstanceMapping.put(internalClassName, instances);
//				classRefQueueMapping.put(internalClassName, refQueue);
//			}
//			instances.add(new MyWeakRef(instance, refQueue));

        // determine current field values

//			final Map<String, WeakReference<?>> fieldValues = new LinkedHashMap<String, WeakReference<?>>();
//
//			Field f;
//			Object v;
//			for (Map.Entry<String, Field> entry : observedFields.entrySet()) {
//				try {
//					f = entry.getValue();
//					if (Modifier.isStatic(f.getModifiers())) {
//						v = f.get(null);
//						fieldValues.put(entry.getKey(), new WeakReference(v));
//
//						// TODO remove final fields from map of observed fields
//						if (v != null) {
//							// as PUTFIELD only access public (and protected) fields we can also add a corresponding GETFIELD entry to the log
//							// to know the instances stored in the static fields
//
//							final Object receiver = instance instanceof Class ? instance
//							        : instance.getClass();
//
//							Capturer.capture(captureId, receiver, CaptureLog.GETSTATIC,
//							                 Type.getDescriptor(f.getType()),
//							                 new Object[] { f.getName() });
//							Capturer.enable(captureId, receiver, v);
//
//							CLASSES.add((Class<?>) receiver);
//
//							// TODO proper capture id handling
//							captureId--;
//						}
//					} else {
//						// we can't collect instance field values from the class itself
//						if (!(instance instanceof Class)) {
//							fieldValues.put(entry.getKey(),
//							                new WeakReference(f.get(instance)));
//						}
//					}
//				} catch (final Exception e) {
//					logger.error("class={} field={} fieldOwner={} instance={}",
//					             new Object[] { internalClassName, entry.getKey(),
//					                     entry.getValue().getDeclaringClass().getName(),
//					                     instance });
//
//					logger.error("an error occurred while determining current field values",
//					             e);
//					throw new RuntimeException(e); // TODO better exception type
//				}
//			}
//
//			instanceRecentFieldValuesMapping.put(System.identityHashCode(instance),
//			                                     fieldValues);
//		}
    }

    private static Map<String, Field> collectAccessibleFields(Map<String, Field> accessibleFields,
                                                              final Class<?> clazz, final Package childPackage) {
        if (clazz == null || Object.class.equals(clazz)) {
            logger.debug("Cannot get fields for null class");
            return new LinkedHashMap<>();
        }
        logger.debug("Collecting accessible fields for {}", clazz.getCanonicalName());
        Map<String, Field> currentAccessibleFields = new LinkedHashMap<>();
        try {
            for (Field f : clazz.getDeclaredFields()) {
                try {
                    int modifier = f.getModifiers();
                    if (Modifier.isPublic(modifier)
                            || (Modifier.isProtected(modifier) && (childPackage == null || childPackage.equals(clazz.getPackage())))) {
                        f.setAccessible(true);
                        currentAccessibleFields.put(f.getName(), f);
                        logger.debug("Field {} is accessible", f.getName());
                    } else {
                        logger.debug("Field {} is NOT accessible", f.getName());
                    }
                } catch (Throwable t) {
                    logger.error("Exception caught while looking at field {}: {}", f.getName(), t.toString());
                }
                //			if(! Modifier.isPrivate(modifier) )
                //			{
                //			    f.setAccessible(true);
                //				accessibleFields.put(f.getName(), f);
                //			}
            }
        } catch (Throwable t) {
            logger.error("Exception caught while collecting fields from class {}: {}", clazz.getCanonicalName(), t.toString());
        }

        logger.debug("Looking at fields of superclass {}", clazz.getSuperclass().getCanonicalName());
        Map<String, Field> superFieldMap = collectAccessibleFields(accessibleFields, clazz.getSuperclass(),
                clazz.getPackage());
        currentAccessibleFields.putAll(superFieldMap);
        classFieldsMapping.put(clazz.getName().replace('.', '/'), currentAccessibleFields);
        logger.debug("Storing {} field(s) for {}: {}", currentAccessibleFields.size(),
                clazz.getCanonicalName(), currentAccessibleFields);
        return currentAccessibleFields;
    }

    private static void cleanUpReferences(final String internalClassName) {
        final List<MyWeakRef<?>> instances = classInstanceMapping.get(internalClassName);

        if (instances != null) {
            final ReferenceQueue<?> refQueue = classRefQueueMapping.get(internalClassName);

            // clean list of instances from garbagge collected references
            Reference<?> ref;
            while ((ref = refQueue.poll()) != null) {
                instances.remove(ref);

                instanceRecentFieldValuesMapping.remove(((MyWeakRef<?>) ref).oid);
            }

            if (instances.isEmpty()) {
                classRefQueueMapping.remove(internalClassName);
                classInstanceMapping.remove(internalClassName);
                classFieldsMapping.remove(internalClassName);
            }
        }
    }

    synchronized public static void notifyModification(Object receiver, final int captureId,
                                                       final String internalClassName, final String fieldName, final String desc) {
        cleanUpReferences(internalClassName);

        if (!Capturer.isCapturing()) {
            return;
        }
        Map<String, Field> observedFields = classFieldsMapping.get(internalClassName);
        if (observedFields == null) {
            // determine observable fields
            populateFieldMap(internalClassName, fieldName);
        }
        try {
            final Map<String, Field> fields = classFieldsMapping.get(internalClassName);
            if (fields == null) {
                logger.error("Fields map for class {} should not be null",
                        internalClassName);
                throw new IllegalStateException("Fields map for class "
                        + internalClassName + " should not be null");
            }

            if (fields.isEmpty()) {
                logger.debug(classFieldsMapping.toString());
                logger.debug("Done modify - no fields");
                return;
            }
            final Field targetField = fields.get(fieldName);


            if (targetField == null) {
                // happens if field is private
                logger.debug("Could not find field {} for class {}", fieldName,
                        internalClassName);
            } else {
                final Object currentValue;
                if (Modifier.isStatic(targetField.getModifiers())) {
                    currentValue = targetField.get(null);
                } else {
                    // we can't get instance field values from the class itself
                    if (receiver instanceof Class) {
                        return;
                    }
                    if (!registeredObjects.contains(System.identityHashCode(receiver))) {
                        return;
                    }

                    currentValue = targetField.get(receiver);
                }
                logger.debug("Notify modification of field {} on class {}", fieldName, internalClassName);
                if (Modifier.isStatic(targetField.getModifiers())) {
                    Capturer.capture(captureId, receiver,
                            CaptureLog.PUTSTATIC, desc,
                            new Object[]{fieldName,
                                    currentValue});
                    Capturer.enable(captureId, receiver,
                            CaptureLog.RETURN_TYPE_VOID);

                } else {
                    Capturer.capture(captureId, receiver,
                            CaptureLog.PUTFIELD, desc,
                            new Object[]{fieldName,
                                    currentValue});
                    Capturer.enable(captureId, receiver,
                            CaptureLog.RETURN_TYPE_VOID);

                }
//				
//								if (instance instanceof Class) {
//									// TODO error?
//									final WeakReference<?> recentRef = recentFieldValues.get(fieldName);
//									final Object recentValue = recentRef.get();
//
//									if ((recentValue != currentValue)
//									        || (recentValue != null && !recentValue.equals(currentValue))) {
//										Capturer.capture(captureId, instance,
//										                 CaptureLog.PUTSTATIC, desc,
//										                 new Object[] { fieldName,
//										                         currentValue });
//										Capturer.enable(captureId, instance,
//										                CaptureLog.RETURN_TYPE_VOID);
//
//										// as PUTFIELD only access public fields we can also add a corresponding GETFIELD entry to the log
//										Capturer.capture(captureId + 1, instance,
//										                 CaptureLog.GETSTATIC, desc,
//										                 new Object[] { fieldName });
//										Capturer.enable(captureId + 1, instance,
//										                currentValue);
//
//										break; // there can only be on field access at a time
//									}
//								} else {
//									final WeakReference<?> recentRef = recentFieldValues.get(fieldName);
//									final Object recentValue = recentRef.get();
//
//									if (recentValue != currentValue) //|| (recentValue != null && ! recentValue.equals(currentValue)))
//									{
//										Capturer.capture(captureId, instance,
//										                 CaptureLog.PUTFIELD, desc,
//										                 new Object[] { fieldName,
//										                         currentValue });
//										Capturer.enable(captureId, instance,
//										                CaptureLog.RETURN_TYPE_VOID);
//
//										// as PUTFIELD only access public fields we can also add a corresponding GETFIELD entry to the log
//										Capturer.capture(captureId + 1, instance,
//										                 CaptureLog.GETFIELD, desc,
//										                 new Object[] { fieldName });
//										Capturer.enable(captureId + 1, instance,
//										                currentValue);
//
//										break; // there can only be on field access at a time
//									}
//								}
//
//							} catch (final Exception e) {
//								logger.error("an error occurred while comparing field values for class {}",
//								             internalClassName, e);
//								throw new RuntimeException(e); // TODO better exception type
//							}
//
//						}
                //}
                //}
//			}
//		} else {
//			logger.debug("No observed fields for class {}  [MODIFY]", internalClassName);
//		}
            }
        } catch (final Throwable e) {
            logger.error("an error occurred while comparing field values for class {}",
                    internalClassName, e);
            throw new RuntimeException(e); // TODO better exception type
        }
        logger.debug("Done field write");

    }

    private static void populateFieldMap(String internalClassName, String fieldName) {
        Map<String, Field> observedFields = new LinkedHashMap<>();
        try {
            Class<?> clazz = Class.forName(internalClassName.replace('/', '.'), true, carvingClassLoader);

            collectAccessibleFields(observedFields, clazz, null);
            if (!observedFields.containsKey(fieldName)) {
                logger.debug("Field {} not observed", fieldName);
                return;
            }
            logger.debug("Trying to get field {} for class {}", fieldName, internalClassName);
            if (Modifier.isStatic(observedFields.get(fieldName).getModifiers())) {
                register(clazz);
            }

//			if (observedFields.isEmpty()) {
//				logger.debug("Class {} has no observable fields", internalClassName);
//				classFieldsMapping.put(internalClassName, Collections.EMPTY_MAP);
//			} else {
//				logger.debug("Setting field map for class "+internalClassName +" to "+observedFields);
//				classFieldsMapping.put(internalClassName, observedFields);
//			}

        } catch (ClassNotFoundException e) {
            logger.info("Error loading class " + internalClassName + ": " + e);
        } catch (Throwable e) {
            logger.debug("Carving classloader: " + carvingClassLoader);
            logger.info("TODO Error loading class " + internalClassName + ": " + e);
            logger.info("TODO Error loading class " + internalClassName + ": " + e.getCause());
            for (StackTraceElement elem : e.getStackTrace()) {
                logger.debug(elem.toString());
            }
            if (e.getCause() != null)
                for (StackTraceElement elem : e.getCause().getStackTrace()) {
                    logger.debug(elem.toString());
                }
        }
    }

    synchronized public static void notifyReadAccess(Object receiver, final int captureId,
                                                     final String internalClassName, final String fieldName, final String desc) {
        cleanUpReferences(internalClassName);

        if (!Capturer.isCapturing()) {
            return;
        }


        Map<String, Field> observedFields = classFieldsMapping.get(internalClassName);
        if (observedFields == null) {
            // determine observable fields
            logger.debug("Haven't seen {} {} yet", internalClassName, fieldName);
            populateFieldMap(internalClassName, fieldName);
        }
        try {
            final Map<String, Field> fields = classFieldsMapping.get(internalClassName);
            if (fields == null) {
                logger.error("Fields map for class {} should not be null",
                        internalClassName);
                throw new IllegalStateException("Fields map for class "
                        + internalClassName + " should not be null");
            }

            if (fields.isEmpty()) {
                logger.debug("Done read - no fields");
                return;
            }
            final Field targetField = fields.get(fieldName);
            if (targetField == null) {
                // happens if field is private
                logger.debug("Could not find field {} for class {}", fieldName,
                        internalClassName);
                return;
            }

            final Object currentValue;
            if (Modifier.isStatic(targetField.getModifiers())) {
                currentValue = targetField.get(null);
            } else {
                // we can't get instance field values from the class itself
                if (receiver instanceof Class) {
                    logger.debug("WTF read");
                    return;
                }
                if (!registeredObjects.contains(System.identityHashCode(receiver))) {
                    return;
                }

                currentValue = targetField.get(receiver);
            }
            logger.debug("Notify read access {}, {}, {}", internalClassName, fieldName, receiver == null ? "null" : receiver.getClass());

            if (receiver instanceof Class) {
                Capturer.capture(captureId, receiver,
                        CaptureLog.GETSTATIC, desc,
                        new Object[]{fieldName});
                Capturer.enable(captureId, receiver, currentValue);
            } else if (receiver == null) {
                Capturer.capture(captureId, targetField.getDeclaringClass(),
                        CaptureLog.GETSTATIC, desc,
                        new Object[]{fieldName});
                Capturer.enable(captureId, targetField.getDeclaringClass(), currentValue);
            } else {
                Capturer.capture(captureId, receiver,
                        CaptureLog.GETFIELD, desc,
                        new Object[]{fieldName});
                Capturer.enable(captureId, receiver, currentValue);
            }
            logger.debug("Done field read");
        } catch (final Throwable e) {
            logger.error("an error occurred while comparing field values for class {}",
                    internalClassName, e);
            throw new RuntimeException(e); // TODO better exception type
        }
    }

    synchronized public static void clear() {
        classInstanceMapping.clear();
        classFieldsMapping.clear();
        instanceRecentFieldValuesMapping.clear();
        classRefQueueMapping.clear();
        registeredObjects.clear();
        captureId = Integer.MAX_VALUE;
    }

    synchronized public static void restoreForegoingGETSTATIC() {
        for (Class<?> c : CLASSES) {
            register(c);
        }
    }

    public static String classFieldsMappinString() {
        final StringBuilder builder = new StringBuilder();

        String c;
        Map<String, Field> fieldMap;
        int fieldModifiers;

        for (Map.Entry<String, Map<String, Field>> entry : classFieldsMapping.entrySet()) {
            c = entry.getKey();
            fieldMap = entry.getValue();

            for (Map.Entry<String, Field> entry2 : fieldMap.entrySet()) {
                fieldModifiers = entry2.getValue().getModifiers();

                builder.append(c).append('.').append(entry2.getKey()).append(" public="
                        + Modifier.isPublic(fieldModifiers)).append(" private="
                        + Modifier.isPrivate(fieldModifiers)).append(" protected="
                        + Modifier.isProtected(fieldModifiers)).append('\n');
            }
        }

        return builder.toString();
    }

    public static boolean isKnownObject(Object obj) {
        return registeredObjects.contains(obj);
    }

    public static class MyWeakRef<T> extends WeakReference<T> {
        public final int oid;

        public MyWeakRef(T referent, ReferenceQueue<? super T> q) {
            super(referent, q);

            oid = System.identityHashCode(referent);
        }
    }

}
