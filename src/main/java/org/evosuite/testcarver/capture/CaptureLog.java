package org.evosuite.testcarver.capture;

import java.util.ArrayList;
import java.util.Arrays;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;


public final class CaptureLog implements Cloneable
{
	//--- LOG Table
	// REC_NO | OID | METHOD | PARAMS
	
	// rec_no is implied by index
	public final TIntArrayList       objectIds;
	public final TIntArrayList       captureIds;
	public final ArrayList<String>   methodNames;
	public final ArrayList<Object[]> params;
	public final ArrayList<Object>   returnValues;
	public final ArrayList<Boolean> isStaticCallList;

	public final ArrayList<String>   descList;
	
	
	//--- OID Info Table
	// OID | INIT_REC_NO | CLASS
	
	public final TIntIntHashMap    oidRecMapping; // oid -> index ==> oidInitReco.get(index) + oidClassNames.get(index)
	public final TIntArrayList 	  oidInitRecNo;
	public final ArrayList<String> oidClassNames;
	public final TIntArrayList 	  oids;

	public final TIntObjectHashMap<String> namesOfAccessedFields; // captureId -> field name

	
	//---
	
	private final XStream xstream;
	
	//---
	
	
	
	
	public static final Object[] NO_ARGS           = new Object[0];
	public static final String   OBSERVED_INIT     = "<init>";
	public static final String   PLAIN_INIT        = CaptureLog.class.getName() + ".PLAIN";
	public static final String   NOT_OBSERVED_INIT = CaptureLog.class.getName() + ".XINIT";

	public static final String END_CAPTURE_PSEUDO_METHOD =  CaptureLog.class.getName() + ".END_CAPTURE";
	public static final int    PSEUDO_CAPTURE_ID		 = Integer.MAX_VALUE; // for internally created statement (PLAIN_INIT and NOT_OBSERVED_INIT)
	
	
	private static final String EMPTY_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{});
	
	
	public static final String PUTFIELD  = "PUTFIELD";
	public static final String PUTSTATIC = "PUTSTATIC";
	public static final String GETFIELD  = "GETFIELD";
	public static final String GETSTATIC = "GETSTATIC";
	
	
	public static final Object RETURN_TYPE_VOID = CaptureLog.class.getName() + ".RETURN_VOID";
	
	
	private static final Logger LOG = LoggerFactory.getLogger(CaptureLog.class);
	
	public CaptureLog()
	{	
		this.objectIds      = new TIntArrayList();
		this.methodNames    = new ArrayList<String>();
		this.params         = new ArrayList<Object[]>();
		this.captureIds     = new TIntArrayList();
		this.returnValues   = new ArrayList<Object>();
		this.descList       = new ArrayList<String>();
		
		
		this.oidRecMapping = new TIntIntHashMap();
		this.oidInitRecNo  = new TIntArrayList();
		this.oidClassNames = new ArrayList<String>();
		this.oids          = new TIntArrayList();
		
        this.isStaticCallList = new ArrayList<Boolean>();
		
		this.namesOfAccessedFields = new TIntObjectHashMap<String>();
		
		this.xstream = new XStream();
	}
	
	@Override
	public CaptureLog clone()
	{
		final CaptureLog log = new CaptureLog();
		
		log.objectIds.addAll(this.objectIds);
		log.methodNames.addAll(this.methodNames);
		log.params.addAll(this.params);
		log.captureIds.addAll(this.captureIds);
		log.returnValues.addAll(this.returnValues);
		log.descList.addAll(this.descList);
		
		
		log.oidRecMapping.putAll(this.oidRecMapping);
		log.oidInitRecNo.addAll(this.oidInitRecNo);
		log.oidClassNames.addAll(this.oidClassNames);
		log.oids.addAll(this.oids);
		log.namesOfAccessedFields.putAll(this.namesOfAccessedFields);
		log.isStaticCallList.addAll(this.isStaticCallList);

		return log;
	}
	

	public void clear()
	{
		this.objectIds.resetQuick();
		this.methodNames.clear();
		this.params.clear();
		this.captureIds.resetQuick();
		this.returnValues.clear();
		this.descList.clear();
	
		this.oidRecMapping.clear();
		this.oidInitRecNo.resetQuick();
		this.oidClassNames.clear();
		this.oids.resetQuick();
		
		this.namesOfAccessedFields.clear();
	}
	
	
	private boolean updateInfoTable(final int oid, final Object receiver, final boolean replace)
	{
		// update oid info table, if necessary
		// -> we assume that USUALLY the first record belonging to an object belongs to its instanciation
		if(this.oidRecMapping.containsKey(oid))
		{
			if(replace)
			{
				final int infoRecNo = this.oidRecMapping.get(oid);
				final int logRecNo  = this.objectIds.size();
				this.oidInitRecNo.set(infoRecNo, -logRecNo);
				
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			final int logRecNo  = this.objectIds.size();
			final int infoRecNo = this.oidInitRecNo.size();
			this.oidRecMapping.put(oid, infoRecNo);
			// negative log rec no indicates obj construction
			this.oidInitRecNo.add(-logRecNo);

			if(receiver instanceof Class) //this can only happen, if there is a static method call 
			{
				final Class<?> c = (Class<?>) receiver;
				this.oidClassNames.add(c.getName());//.replaceFirst("\\$\\d+$", ""));
			}
			else if(this.isPlain(receiver))
			{
				// we don't need fully qualified name for plain types
				this.oidClassNames.add(receiver.getClass().getSimpleName());//.replaceFirst("\\$\\d+$", ""));
			}
			else
			{
				this.oidClassNames.add(receiver.getClass().getName());//.replaceFirst("\\$\\d+$", ""));
			}
			
			
			this.oids.add(oid);
			
			return true;
		}
	}
	
	
	private boolean isPlain(final Object o)
	{
		return //o instanceof Class   ||
			   o instanceof String  ||
			   o instanceof Integer || 
			   o instanceof Double  || 
			   o instanceof Float   || 
			   o instanceof Long    ||
			   o instanceof Byte    ||
			   o instanceof Short   ||
			   o instanceof Boolean ||
			   o instanceof Character;
	}
	
	public void logEnd(final int captureId, final Object receiver, final Object returnValue)
	{
		// if there is an return value and the return value creation has not been logged before 
		// (may happen, if, for example, the constructor is private), save the information that 
		// the value comes from a finished method call
		if(returnValue != null && returnValue != RETURN_TYPE_VOID)
		{
			 
			 final int returnValueOID = System.identityHashCode(returnValue);
			 
			 
			 if(! this.oidRecMapping.containsKey(returnValueOID)) // was an accessible constructor call belonging to this object logged before?
			 {
				 if(! isPlain(returnValue) && ! (returnValue instanceof Class))
				 {
					 final int oid = System.identityHashCode(receiver);
					 
					 int currentRecord = captureIds.size() - 1;

					 int nestedCalls = 0;
					 while(true)
					 {
						 if(this.captureIds.getQuick(currentRecord) == captureId &&
						    this.objectIds.getQuick(currentRecord)  == oid)
						 {
							if(this.methodNames.get(currentRecord).equals(END_CAPTURE_PSEUDO_METHOD))
							{
								nestedCalls++;
							}
							else
							{
								if(nestedCalls == 0)
								{
									break;
								}
								else
								{
									nestedCalls--;
								}
							}
						 }
						 
						 currentRecord--;
					 }

					 this.returnValues.set(currentRecord, returnValueOID); // oid as integer works here as we exclude plain values
					 
					 final int infoRecNo = this.oidInitRecNo.size();
					 this.oidRecMapping.put(returnValueOID, infoRecNo);
					 // negative log rec no indicates obj construction
					 this.oidInitRecNo.add(-currentRecord);
	
					 this.oidClassNames.add(returnValue.getClass().getName());
					 this.oids.add(returnValueOID);
				 }
			 }
		}
		
		this.captureIds.add(captureId);
		this.objectIds.add(System.identityHashCode(receiver));
		this.methodNames.add(END_CAPTURE_PSEUDO_METHOD);
		this.descList.add(EMPTY_DESC);
		this.params.add(NO_ARGS);
		this.returnValues.add(RETURN_TYPE_VOID);
		this.isStaticCallList.add(Boolean.FALSE);
	}
	
	
	public void log(final int captureId, final Object receiver, final String methodName, final String methodDesc, Object...methodParams)
	{
		// TODO find nicer way
		if(PUTSTATIC.equals(methodName) || PUTFIELD.equals(methodName))
		{
			/*
			 * The first param always specifies the name of the accessed field.
			 * The second param represents the actual value.
			 */
			this.namesOfAccessedFields.put(captureId, (String) methodParams[0]);
			final Object assignedValue = methodParams[1];
			methodParams               = new Object[1];
			methodParams[0]            = assignedValue; 
		}
		else if(GETSTATIC.equals(methodName) || GETFIELD.equals(methodName))
		{
			/*
			 * The param always specifies the name of the accessed field.
			 */
			this.namesOfAccessedFields.put(captureId, (String) methodParams[0]);
			methodParams = new Object[0];
		}
		
		
		
		final int oid = System.identityHashCode(receiver);
		
		// update info table if necessary
		// in case of constructor calls, we want to remember the last one
		final boolean replace = OBSERVED_INIT.equals(methodName);
		this.updateInfoTable(oid, receiver, replace);	
		
		
		// save receiver class -> might be reference in later calls e.g. doSth(Person.class)
		if(receiver instanceof Class)
		{
			this.objectIds.add(oid);
			this.descList.add(EMPTY_DESC);
			this.methodNames.add(PLAIN_INIT);
			this.params.add(new Object[]{ receiver});
			this.returnValues.add(RETURN_TYPE_VOID);
			this.captureIds.add(PSEUDO_CAPTURE_ID);
			this.isStaticCallList.add(Boolean.FALSE);
			this.logEnd(PSEUDO_CAPTURE_ID, receiver, RETURN_TYPE_VOID);
		}

		
		//--- handle method params
		Object param;
		int paramOID;
		for(int i = 0; i < methodParams.length; i++)
		{
			param = methodParams[i];
			
			// null and plain params have PLAIN init stmts such as
			// Integer var0 = 122
			// Float var1 = 2.3
			// String var2 = "Hello World"
			// e.g. o.myMethod(null, var0, var1, var2);
			if(param != null)
			{
				// we assume that all classes (besides java and sun classes) are instrumented.
				// So if there is no foregoing entry in the oid info table, the param is a new and
				// not monitored instance. That's why this param has to be serialized.
				paramOID = System.identityHashCode(param);
				if(this.updateInfoTable(paramOID, param, false))
				{
					this.objectIds.add(paramOID);
					
					if(isPlain(param) || param instanceof Class)
					{
						// exemplary output in test code: Integer number = 123;
						this.methodNames.add(PLAIN_INIT);
						this.params.add(new Object[]{ param});
					}
					else
					{
						// create new serialization record for first emersion
						// exemplary output in test code: Person newJoe = (Person) xstream.fromXML(xml); 
						this.methodNames.add(NOT_OBSERVED_INIT);

						try
						{
							this.params.add(new Object[]{ this.xstream.toXML(param) });
						}
						catch(final ConversionException e)
						{
							LOG.warn("an error occurred while serializing param '{}' -> adding null as param instead", param, e);
							
							// param can not be serialized -> add null as param
							this.params.add(new Object[]{ null });
						}
					}
					
					this.descList.add(EMPTY_DESC);
					this.returnValues.add(RETURN_TYPE_VOID);
					this.captureIds.add(PSEUDO_CAPTURE_ID);
					this.isStaticCallList.add(Boolean.FALSE);
					this.logEnd(PSEUDO_CAPTURE_ID, param, RETURN_TYPE_VOID);
				}
			
			
				 // method param  has been created before so we link to it
				 // NECESSARY as the object might be modified in between
				 // exemplary output in test code: 
				 // Object a = new Object();
				 // ...
				 // o.m(a);
				 methodParams[i] = paramOID;
			}
		}
		
		//--- create method call record
		this.objectIds.add(oid);
		this.methodNames.add(methodName);
		this.descList.add(methodDesc);
		this.params.add(methodParams);
		this.returnValues.add(RETURN_TYPE_VOID);
		this.captureIds.add(captureId);
		this.isStaticCallList.add(receiver instanceof Class);
	}
	
	@Override
	public String toString()
	{
		
		
		final String delimiter = "\t|\t";
		
		final StringBuilder builder = new StringBuilder(1000);
		
		builder.append("LOG:\n")
			   .append("-------------------------------------------------------------------")
			   .append('\n')
			   .append("RECNO").append(delimiter)
			   .append("OID").append(delimiter)
		       .append("CID").append(delimiter)
			   .append("METHOD").append(delimiter)
			   .append("PARAMS").append(delimiter)
			   .append("RETURN").append(delimiter)
			   .append("IS STATIC").append(delimiter)
			   .append("DESC").append(delimiter)
			   .append("ACCESSED FIELDS")
			   .append('\n')
			   .append("-------------------------------------------------------------------")
			   .append('\n');
		
		int captureId;
		
		final int numRecords = this.objectIds.size();
		for(int i = 0; i < numRecords; i++)
		{
			captureId = this.captureIds.getQuick(i);
			
			 builder.append(i)									.append(delimiter) // RECNO
			 		.append(this.objectIds.getQuick(i))			.append(delimiter) // OID
			        .append(captureId)	 					    .append(delimiter) // CID
				    .append(this.methodNames.get(i))            .append(delimiter) // METHOD
				    .append(Arrays.toString(this.params.get(i))).append(delimiter) // PARAMS
				    .append(this.returnValues.get(i))			.append(delimiter) // RETURN
				    .append(this.isStaticCallList.get(i))		.append(delimiter) // IS STATIC
				    .append(this.descList.get(i))				.append(delimiter) // DESC
				    .append(this.namesOfAccessedFields.get(captureId))			   // ACCESSED FIELDS
				    .append('\n');
		}
		
		builder.append('\n').append('\n');
		
		builder.append("META INF:\n")
		       .append("-------------------------------------------------------------------")
		       .append('\n')
		       .append("OID").append(delimiter)
		       .append("INIT RECNO").append(delimiter)
		       .append("OID CLASS").append(delimiter)
		       .append("ACCESSED FIELDS")
		       .append('\n')
		       .append("-------------------------------------------------------------------")
		       .append('\n');
		       
		final int numMetaInfRecords = this.oids.size();
		for(int i = 0; i < numMetaInfRecords; i++)
		{
			      builder.append(this.oids.getQuick(i)).append(delimiter)         // OID
			             .append(this.oidInitRecNo.getQuick(i)).append(delimiter) // INIT RECNO
			       		 .append(this.oidClassNames.get(i)).append(delimiter)     // OID CLASS 
			       		 .append(this.namesOfAccessedFields.get(i))               // ACCESSED FIELDS
			       		 .append('\n');
		}
		
		return builder.toString();
	}
}
