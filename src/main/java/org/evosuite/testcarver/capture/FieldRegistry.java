package org.evosuite.testcarver.capture;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FieldRegistry 
{
	private static final HashMap<String, List<Object>>        classInstanceMapping            = new HashMap<String, List<Object>>();
	private static final HashMap<String, Map<String, Field>>  classFieldsMapping               = new HashMap<String, Map<String, Field>>();
	private static final HashMap<Object, Map<String, Object>> instanceRecentFieldValuesMapping = new HashMap<Object, Map<String,Object>>();
	
	private static final HashSet<Class<?>> CLASSES = new HashSet<Class<?>>();
	
	
	private static final Logger LOG = LoggerFactory.getLogger(FieldRegistry.class);

	private static int captureId = Integer.MAX_VALUE;
	
	private FieldRegistry(){}
	
	@SuppressWarnings("unchecked")
	synchronized
	public static void register(final Object instance)
	{
		final Class<?> clazz;//             = instance.getClass();
		
		
		
		if(instance instanceof Class)
		{
			clazz = (Class<?>)instance;
		}
		else
		{
			clazz = instance.getClass();
		}
		final String   internalClassName = clazz.getName().replace('.', '/');
		
		Map<String, Field> observedFields = classFieldsMapping.get(internalClassName);
		if(observedFields == null)
		{
			// determine observable fields
			
			observedFields = new HashMap<String, Field>();
			
			collectAccessibleFields(observedFields, clazz);

			if(observedFields.isEmpty())
			{
				LOG.debug("Class {} has no observable fields", clazz);
				classFieldsMapping.put(internalClassName, Collections.EMPTY_MAP);
			}
			else
			{
				classFieldsMapping.put(internalClassName, observedFields);
			}
		}
		
		
		
		if(! observedFields.isEmpty())
		{
			List<Object> instances = classInstanceMapping.get(internalClassName);
			if(instances == null)
			{
				instances = new ArrayList<Object>();
				classInstanceMapping.put(internalClassName, instances);
			}

			instances.add(instance);
			
			
			// determine current field values
			
			final HashMap<String, Object> fieldValues = new HashMap<String, Object>();
			
			
			Field  f;
			Object v;
			for(Map.Entry<String, Field> entry : observedFields.entrySet())
			{
				try
				{
					f = entry.getValue();
					if(Modifier.isStatic(f.getModifiers()))
					{
						v = f.get(null);
						fieldValues.put(entry.getKey(), v);
						
						// TODO remove final fields from map of observed fields
						if(v != null)
						{
							// as PUTFIELD only access public (and protected) fields we can also add a corresponding GETFIELD entry to the log
							// to know the instances stored in the static fields
							
							final Object receiver = instance instanceof Class ? instance : instance.getClass();
							
							Capturer.capture(captureId, receiver, CaptureLog.GETSTATIC, Type.getDescriptor(f.getType()), new Object[]{ f.getName() });
							Capturer.enable( captureId, receiver, v);

							CLASSES.add((Class<?>)receiver);
							
							// TODO proper capture id handling
							captureId--;
						}
					}
					else
					{
						// we can't collect instance field values from the class itself
						if(! (instance instanceof Class))
						{
							fieldValues.put(entry.getKey(), f.get(instance));
						}
					}
				} 
				catch (final Exception e) 
				{
					LOG.error("class={} field={} fieldOwner={} instance={}", new Object[]{ internalClassName, 
																			   entry.getKey(), 
																			   entry.getValue().getDeclaringClass().getName(),
																			   instance});
					
					LOG.error("an error occurred while determining current field values", e);
					throw new RuntimeException(e); // TODO better exception type
				}
			}
			
			instanceRecentFieldValuesMapping.put(System.identityHashCode(instance), fieldValues);
		}
	}

	
	
	

	private static void collectAccessibleFields(Map<String, Field> accessibleFields, final Class<?> clazz)
	{
		if(clazz == null || Object.class.equals(clazz))
		{
			return;
		}
		
		final Field[] fields = clazz.getDeclaredFields();
		
		int modifier;
		
		Field f;
		for(int i = 0; i < fields.length; i++)
		{
			f = fields[i];
			
			modifier = f.getModifiers();
			if(! Modifier.isPrivate(modifier))
			{
			    f.setAccessible(true);
				accessibleFields.put(f.getName(), f);
			}
		}
		
		collectAccessibleFields(accessibleFields, clazz.getSuperclass());
	}
	
	
	
	
	
	
	
	synchronized
	public static void notifyModification(final int captureId, final String internalClassName, final String fieldName, final String desc)
	{
		if(classInstanceMapping.containsKey(internalClassName))
		{
			final Map<String, Field> fields = classFieldsMapping.get(internalClassName);
			if(fields == null)
			{
				LOG.error("Fields map for class {} should not be null", internalClassName);
				throw new IllegalStateException("Fields map for class " + internalClassName + " should not be null");
			}
			
			if(! fields.isEmpty())
			{
				final Field targetField = fields.get(fieldName);
				if(targetField == null)
				{
					// happens if field is private
					LOG.debug("Could not find field {} for class {}", fieldName, internalClassName);
				}
				else
				{
					List<?> instances = classInstanceMapping.get(internalClassName);
					if(instances == null)
					{
						LOG.error("List of instances for class {} should not be null", internalClassName);
						throw new IllegalStateException("List of instances for class " + internalClassName + " should not be null");
					}
					
					Map<String, Object> recentFieldValues;
					for(Object instance : instances)
					{
						recentFieldValues = instanceRecentFieldValuesMapping.get(System.identityHashCode(instance));
						if(recentFieldValues == null)
						{
							LOG.error("map of recent field values (instance={} class={}) should not be null", instance, internalClassName);
							throw new IllegalStateException("map of recent field values (instance=" + instance +" class=" + internalClassName + ") should not be null");
						}
						else
						{
							try
							{
								final Object currentValue;
								if(Modifier.isStatic(targetField.getModifiers()))
								{
									currentValue = targetField.get(null);
								}
								else
								{
									// we can't get instance field values from the class itself
									if(instance instanceof Class)
									{
										continue;
									}
									
									currentValue = targetField.get(instance);
								}


								if(instance instanceof Class)
								{   
									final Object recentValue = recentFieldValues.get(null);
									
									if((recentValue != currentValue) || (recentValue != null && ! recentValue.equals(currentValue)))
									{
										Capturer.capture(captureId, instance, CaptureLog.PUTSTATIC, desc, new Object[]{fieldName, currentValue});
										Capturer.enable(captureId, instance, CaptureLog.RETURN_TYPE_VOID);
										
										// as PUTFIELD only access public fields we can also add a corresponding GETFIELD entry to the log
										Capturer.capture(captureId + 1, instance, CaptureLog.GETSTATIC, desc, new Object[]{fieldName});
										Capturer.enable( captureId + 1, instance, currentValue);
										
										break; // there can only be on field access at a time
									}
								}
								else
								{
									final Object recentValue = recentFieldValues.get(fieldName);
									
									if((recentValue != currentValue) || (recentValue != null && ! recentValue.equals(currentValue)))
									{
										Capturer.capture(captureId, instance, CaptureLog.PUTFIELD, desc, new Object[]{fieldName, currentValue});
										Capturer.enable(captureId, instance, CaptureLog.RETURN_TYPE_VOID);
										
										// as PUTFIELD only access public fields we can also add a corresponding GETFIELD entry to the log
										Capturer.capture(captureId + 1, instance, CaptureLog.GETFIELD, desc, new Object[]{fieldName});
										Capturer.enable( captureId + 1, instance, currentValue);
										
										break; // there can only be on field access at a time
									}
								}
			
							}
							catch(final Exception e)
							{
								LOG.error("an error occurred while comparing field values for class {}", internalClassName, e);
								throw new RuntimeException(e); // TODO better exception type
							}

						}
					}
				}
			}
		}
		else
		{
			LOG.debug("No observed fields for class {}", internalClassName);
		}
	}
	
	
	
	
	
	synchronized
	public static void notifyReadAccess(final int captureId, final String internalClassName, final String fieldName, final String desc)
	{
		if(classInstanceMapping.containsKey(internalClassName))
		{
			final Map<String, Field> fields = classFieldsMapping.get(internalClassName);
			if(fields == null)
			{
				LOG.error("Fields map for class {} should not be null", internalClassName);
				throw new IllegalStateException("Fields map for class " + internalClassName + " should not be null");
			}
			
			if(! fields.isEmpty())
			{
				final Field targetField = fields.get(fieldName);
				if(targetField == null)
				{
					// happens if field is private
					LOG.debug("Could not find field {} for class {}", fieldName, internalClassName);
				}
				else
				{
					List<?> instances = classInstanceMapping.get(internalClassName);
					if(instances == null)
					{
						LOG.error("List of instances for class {} should not be null", internalClassName);
						throw new IllegalStateException("List of instances for class " + internalClassName + " should not be null");
					}
					
					Map<String, Object> recentFieldValues;
					for(Object instance : instances)
					{
						recentFieldValues = instanceRecentFieldValuesMapping.get(System.identityHashCode(instance));
						if(recentFieldValues == null)
						{
							LOG.error("map of recent field values (instance={} class={}) should not be null", instance, internalClassName);
							throw new IllegalStateException("map of recent field values (instance=" + instance +" class=" + internalClassName + ") should not be null");
						}
						else
						{
							try
							{
								final Object currentValue;
								if(Modifier.isStatic(targetField.getModifiers()))
								{
									currentValue = targetField.get(null);
								}
								else
								{
									// we can't get instance field values from the class itself
									if(instance instanceof Class)
									{
										continue;
									}
									
									currentValue = targetField.get(instance);
								}


								if(instance instanceof Class)
								{   
									Capturer.capture(captureId, instance, CaptureLog.GETSTATIC, desc, new Object[]{fieldName});
									Capturer.enable( captureId, instance, currentValue);
									
									break; // there can only be on field access at a time
								}
								else
								{
									Capturer.capture(captureId, instance, CaptureLog.GETFIELD, desc, new Object[]{fieldName});
									Capturer.enable( captureId, instance, currentValue);
									
									break; // there can only be on field access at a time
								}
			
							}
							catch(final Exception e)
							{
								LOG.error("an error occurred while comparing field values for class {}", internalClassName, e);
								throw new RuntimeException(e); // TODO better exception type
							}

						}
					}
				}
			}
		}
		else
		{
			LOG.debug("No observed fields for class {}", internalClassName);
		}
	}
	
	
	synchronized
	public static void clear()
	{
		classInstanceMapping.clear();
		classFieldsMapping.clear();
		instanceRecentFieldValuesMapping.clear();
		captureId = Integer.MAX_VALUE;
	}
	
	
	
	synchronized
	public static void restoreForegoingGETSTATIC()
	{
		for(Class<?> c : CLASSES)
		{
			register(c);
		}
	}
	
	
	
	public static String classFieldsMappinString()
	{
		final StringBuilder builder = new StringBuilder();
		
		
		String c;
		Map<String, Field> fieldMap;
		int fieldModifiers;
		
		for(Map.Entry<String, Map<String, Field>> entry : classFieldsMapping.entrySet())
		{
			 c        = entry.getKey();
			 fieldMap = entry.getValue();
			 
			 for( Map.Entry<String, Field> entry2 : fieldMap.entrySet())
			 {
				 fieldModifiers = entry2.getValue().getModifiers();
				 
				 builder.append(c).append('.').append(entry2.getKey())
				 		.append(" public=" + Modifier.isPublic(fieldModifiers))
				 		.append(" private=" + Modifier.isPrivate(fieldModifiers))
				 		.append(" protected=" + Modifier.isProtected(fieldModifiers))
				 		.append('\n');
			 }
		}
		
		
		return builder.toString();
	}
}
