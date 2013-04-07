package org.evosuite.testcarver.codegen;

import gnu.trove.list.array.TIntArrayList;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class CaptureLogAnalyzer implements ICaptureLogAnalyzer
{
	private static Logger logger = LoggerFactory.getLogger(CaptureLogAnalyzer.class);
	
	@SuppressWarnings("rawtypes")
	@Override
	public void analyze(final CaptureLog originalLog, final ICodeGenerator generator, final Class<?>... observedClasses) 
	{
		this.analyze(originalLog, generator, new HashSet<Class<?>>(), observedClasses);
	}
	
	@SuppressWarnings("rawtypes")
	public void analyze(final CaptureLog originalLog, final ICodeGenerator generator, final Set<Class<?>> blackList, final Class<?>... observedClasses) 
	{
		CaptureLog log = originalLog.clone();
		
		HashSet<String> observedClassNames = extracObservedClassNames(observedClasses);
			
		TIntArrayList targetOIDs = getTargetOIDs(log, observedClassNames);
				
		int[] oidExchange = analyzeLog(generator, blackList, log, targetOIDs);		
		
		postProcessLog(originalLog, generator, blackList, log, oidExchange, observedClasses);
	}

	private void postProcessLog(final CaptureLog originalLog,
			final ICodeGenerator generator, final Set<Class<?>> blackList,
			CaptureLog log, int[] oidExchange,
			final Class<?>... observedClasses) throws RuntimeException {
		
		if(oidExchange == null){
			generator.after(log);
		} else {
			try
			{
				final Class<?> origClass = this.getClassFromOID(log, oidExchange[0]);
				final Class<?> destClass = this.getClassFromOID(log, oidExchange[1]);
				
				for(int i = 0; i < observedClasses.length; i++)
				{
					if(origClass.equals(observedClasses[i]))
					{
						observedClasses[i] = destClass;
					}
				}

				generator.clear();
				this.analyze(originalLog, generator, blackList, observedClasses);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	private int[] analyzeLog(final ICodeGenerator generator,
			final Set<Class<?>> blackList, CaptureLog log,
			TIntArrayList targetOIDs) {
		//--- 3. step: analyze log
		
		generator.before(log);
		
		final int numLogRecords = log.objectIds.size();
		int currentOID          = targetOIDs.getQuick(0);
		int[] oidExchange       = null;
		
		// TODO knowing last logRecNo for termination criterion belonging to an observed instance would prevent processing unnecessary statements
		for(int currentRecord = log.oidRecMapping.get(currentOID); currentRecord < numLogRecords; currentRecord++)
		{
			currentOID = log.objectIds.getQuick(currentRecord);
			
			if(targetOIDs.contains(currentOID) && ! blackList.contains(getClassFromOID(log, currentOID)))
			{
				logger.debug("Analyzing record in position "+currentRecord);
				
				oidExchange = this.restorceCodeFromLastPosTo(log, generator, currentOID, currentRecord, blackList);
				if(oidExchange != null){
					break;
				}
				
				// forward to end of method call sequence
				currentRecord = findEndOfMethod(log, currentRecord, currentOID);
				
				// each method call is considered as object state modification -> so save last object modification
				log.oidInitRecNo.setQuick(log.oidRecMapping.get(currentOID), currentRecord);
			} else {
				logger.debug("Skipping record in position "+currentRecord);
			}
		}
		return oidExchange;
	}

	private TIntArrayList getTargetOIDs(final CaptureLog log,
			final HashSet<String> observedClassNames) {
		//--- 2. step: get all oids of the instances of the observed classes
		//    NOTE: They are implicitly sorted by INIT_REC_NO because of the natural object creation order captured by the 
		//    instrumentation
		final TIntArrayList targetOIDs = new TIntArrayList();
		final int numInfoRecs = log.oidClassNames.size();
		for(int i = 0; i < numInfoRecs; i++) {
			if(observedClassNames.contains(log.oidClassNames.get(i))){
				targetOIDs.add(log.oids.getQuick(i));
			}
		}
		return targetOIDs;
	}

	private HashSet<String> extracObservedClassNames(
			final Class<?>... observedClasses) {
		//--- 1. step: extract class names
		final HashSet<String> observedClassNames = new HashSet<String>();
		for(int i = 0; i < observedClasses.length; i++){
			observedClassNames.add(observedClasses[i].getName());
		}
		return observedClassNames;
	}
	
	
	private Class<?> getClassFromOID(final CaptureLog log,  final int oid)
	{
		try
		{
			final int rec = log.oidRecMapping.get(oid);
			return this.getClassForName(log.oidClassNames.get(rec));
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private final Class<?> getClassForName(String type)
	{
		try 
		{
			if( type.equals("boolean"))
			{
				return Boolean.TYPE;
			}
			else if(type.equals("byte"))
			{
				return Byte.TYPE;
			}
			else if( type.equals("char"))
			{
				return Character.TYPE;
			}
			else if( type.equals("double"))
			{
				return Double.TYPE;
			}
			else if(type.equals("float"))
			{
				return Float.TYPE;
			}
			else if(type.equals("int"))
			{
				return Integer.TYPE;
			}
			else if( type.equals("long"))
			{
				return Long.TYPE;
			}
			else if(type.equals("short"))
			{
				return Short.TYPE;
			}
			else if(type.equals("String") ||type.equals("Boolean") ||type.equals("Boolean") || type.equals("Short") ||type.equals("Long") ||
					type.equals("Integer") || type.equals("Float") || type.equals("Double") ||type.equals("Byte") || 
					type.equals("Character") )
			{
				return Class.forName("java.lang." + type);
			}
		
			if(type.endsWith("[]"))
			{
				type = type.replace("[]", "");
				return Class.forName("[L" + type + ";");
			}
			else
			{
				return Class.forName(Utils.getClassNameFromResourcePath(type));
			}
		} 
		catch (final ClassNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * @param log
	 * @param currentRecord
	 * @return -1, if there is no caller (very first method call),
	 * 		caller oid otherwise
	 */
	private int findCaller(final CaptureLog log, final int currentRecord)
	{
		final int numRecords = log.objectIds.size();
		
		//--- look for the end of the calling method
		int record = currentRecord;
		do
		{
	    	record = this.findEndOfMethod(log, record, log.objectIds.getQuick(record));
	    	record++;
		}
		while(  record < numRecords &&
				! log.methodNames.get(record).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD));  // is not the end of the calling method
			     

    	
		if(record == numRecords)
		{
			// did not find any caller -> must be very first method call
			return -1;
		}
		else
		{
			// found caller
			return log.objectIds.getQuick(record);
		}
	}
	
	
    private void updateInitRec(final CaptureLog log, final int currentOID, final int currentRecord)
    {
    	final int infoRec = log.oidRecMapping.get(currentOID); 
    	if(currentRecord > log.oidInitRecNo.getQuick(infoRec))
    	{
    		log.oidInitRecNo.setQuick(infoRec, currentRecord);
    	}
    }
	
    
    private int findEndOfMethod(final CaptureLog log, final int currentRecord, final int currentOID)
    {
    	final int numRecords = log.objectIds.size();
    	
    	int record = currentRecord;
    	
    	final int captureId = log.captureIds.getQuick(currentRecord);
		while(   record < numRecords &&
			  ! (log.objectIds.getQuick(record) == currentOID &&
				 log.captureIds.getQuick(record) == captureId && 
				 log.methodNames.get(record).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD)))
		{
			record++;
		}
		
		return record;
    }
	
    @SuppressWarnings({ "rawtypes" })
    private void restoreArgs(final Object[] args, final int currentRecord, final CaptureLog log, final ICodeGenerator generator, final Set<Class<?>> blackList)
    {
    	Integer oid;
		for(int i = 0; i < args.length; i++)
		{
			// there can only be OIDs or null
			oid = (Integer) args[i];
			
			if(oid != null)
			{
				this.restorceCodeFromLastPosTo(log, generator, oid, currentRecord, blackList);
			}
		}
    }
    
    
	@SuppressWarnings({ "rawtypes" })
	private int[] restorceCodeFromLastPosTo(final CaptureLog log, final ICodeGenerator generator,final int oid, final int end, final Set<Class<?>> blackList){
		
		final int oidInfoRecNo  = log.oidRecMapping.get(oid);
		final int dependencyOID = log.dependencies.getQuick(oidInfoRecNo);
		
		// start from last OID modification point
		int currentRecord = log.oidInitRecNo.getQuick(oidInfoRecNo);
		if(currentRecord > 0){
			// last modification of object happened here
			// -> we start looking for interesting records after retrieved record
			currentRecord++;				
		} else {
			// object new instance statement
			// -> retrieved loc record no is included
   		    currentRecord = -currentRecord;
		}
		
		String methodName;
		int    currentOID;
		Object[] methodArgs;
		Integer  methodArgOID;
		
		Integer returnValue;
		Object returnValueObj;
		
		for(; currentRecord <= end; currentRecord++) {
			currentOID     = log.objectIds.getQuick(currentRecord);
			returnValueObj = log.returnValues.get(currentRecord);
			returnValue    = returnValueObj.equals(CaptureLog.RETURN_TYPE_VOID) ? -1 : (Integer) returnValueObj;
			
			if(oid == currentOID ||	returnValue == oid) {
//				// TODO in arbeit
//				if(isBlackListed(currentOID, blackList, log))
//				{
//					System.out.println("-> is blacklisted... " + blackList + " oid: " + currentOID);
//					return getExchange(log, currentRecord, currentOID, blackList);
//				}
				
				methodName = log.methodNames.get(currentRecord);
				
				if(CaptureLog.PLAIN_INIT.equals(methodName)) {
					currentRecord = handlePlainInit(log, generator, currentRecord, currentOID);
				}
				else if(CaptureLog.COLLECTION_INIT.equals(methodName)){
					currentRecord = handleCollectionInit(log, generator, blackList, currentRecord, currentOID);
				}
				else if(CaptureLog.MAP_INIT.equals(methodName)){
					currentRecord = handleMapInit(log, generator, blackList, currentRecord, currentOID);
				}
				else if(CaptureLog.ARRAY_INIT.equals(methodName)){
					currentRecord = handleArrayInit(log, generator, blackList, currentRecord, currentOID);
				}
				else if(CaptureLog.NOT_OBSERVED_INIT.equals(methodName)) {
					// e.g. Person var = (Person) XSTREAM.fromXML("<xml/>");
					if(dependencyOID != CaptureLog.NO_DEPENDENCY)
					{
						final int[] exchange = this.restorceCodeFromLastPosTo(log, generator, dependencyOID, currentRecord, blackList);
						if(exchange != null)
						{
							return exchange;
						}
					}
					
					generator.createUnobservedInitStmt(log, currentRecord);
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
				}
//				else if(CaptureLog.DEPENDENCY.equals(methodName))
//				{
//					methodArgs = log.params.get(currentRecord);
//					this.restorceCodeFromLastPosTo(log, generator, (Integer) methodArgs[0], currentRecord);
//					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
//				}
				else if(CaptureLog.PUTFIELD.equals(methodName) || CaptureLog.PUTSTATIC.equals(methodName) || // field write access such as p.id = id or Person.staticVar = "something"
						CaptureLog.GETFIELD.equals(methodName) || CaptureLog.GETSTATIC.equals(methodName))   // field READ access such as "int a =  p.id" or "String var = Person.staticVar"
				{
					
					if(dependencyOID != CaptureLog.NO_DEPENDENCY)
					{
						int[] exchange = this.restorceCodeFromLastPosTo(log, generator, dependencyOID, currentRecord, blackList);
						if(exchange != null)
						{
							return exchange;
						}
					}
				
					if(CaptureLog.PUTFIELD.equals(methodName) || CaptureLog.PUTSTATIC.equals(methodName))
					{
						// a field assignment has always one argument
						methodArgs = log.params.get(currentRecord);
						methodArgOID = (Integer) methodArgs[0];
						if(methodArgOID != null && methodArgOID != oid)
						{
							// create history of assigned value
							int[] exchange = this.restorceCodeFromLastPosTo(log, generator, methodArgOID, currentRecord, blackList);
							if(exchange != null) {
								return exchange;
							}
						}

						generator.createFieldWriteAccessStmt(log, currentRecord);

					} else {
						generator.createFieldReadAccessStmt(log, currentRecord);
					}
					
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
					
					if(CaptureLog.GETFIELD.equals(methodName) || CaptureLog.GETSTATIC.equals(methodName))
					{
						// GETFIELD and GETSTATIC should only happen, if we obtain an instance whose creation has not been observed
						this.updateInitRec(log, currentOID, currentRecord);
						
						if(returnValue != -1) {
							this.updateInitRec(log, returnValue, currentRecord);
						}
					}
				} else {
					
					//the rest
					
					// var0.call(someArg) or Person var0 = new Person()
					if(dependencyOID != CaptureLog.NO_DEPENDENCY)
					{
						int[] exchange = this.restorceCodeFromLastPosTo(log, generator, dependencyOID, currentRecord, blackList);
						if(exchange != null)
						{
							return exchange;
						}
					}
				
					// TODO in arbeit
					int callerOID = this.findCaller(log, currentRecord);
					
					methodArgs = log.params.get(currentRecord);
					
					for(int i = 0; i < methodArgs.length; i++)
					{
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
					
						//====================================================
						
						// TODO in arbeit
						if(methodArgOID != null && (methodArgOID == callerOID )) {
							int r = currentRecord;
							while(isBlackListed(callerOID, blackList, log)){
								callerOID = this.findCaller(log, ++r);
							}
							
							// replace class to which the current oid belongs to with callerOID
							blackList.add(this.getClassFromOID(log, oid));
							
							return new int[]{oid, callerOID};
						}
						else if(methodArgOID != null && isBlackListed(methodArgOID, blackList, log)){
							logger.debug("arg in blacklist >>>> " + blackList.contains(this.getClassFromOID(log, methodArgOID)));
						
							return getExchange(log, currentRecord, oid, blackList); //new int[]{oid, callerOID};
						}
						//====================================================
						
						if(methodArgOID != null && methodArgOID != oid) {
							int[] exchange = this.restorceCodeFromLastPosTo(log, generator, methodArgOID, currentRecord, blackList);
							if(exchange != null) {
								// we can not resolve all dependencies because they rely on other unresolvable object
								blackList.add(this.getClassFromOID(log, oid));
								return exchange;
							}
						}
					}
					
					// TODO in arbeit
					if(isBlackListed(currentOID, blackList, log)) {
						logger.debug("-> is blacklisted... " + blackList + " oid: " + currentOID + " class: " + getClassFromOID(log, currentOID));
						
						// we can not resolve all dependencies because they rely on other unresolvable object
						blackList.add(this.getClassFromOID(log, oid));
						return getExchange(log, currentRecord, currentOID, blackList);
					}
					
					generator.createMethodCallStmt(log, currentRecord);
					
					// forward to end of method call sequence
					
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
					
					// each method call is considered as object state modification -> so save last object modification
					this.updateInitRec(log, currentOID, currentRecord);
					
					if(returnValue != -1){
						// if returnValue has not type VOID, mark current log record as record where the return value instance was created
						// --> if an object is created within an observed method, it would not be semantically correct
						//     (and impossible to handle properly) to create an extra instance of the return value type outside this method
						this.updateInitRec(log, returnValue, currentRecord);
					}
					

					
					// consider each passed argument as being modified at the end of the method call sequence
					for(int i = 0; i < methodArgs.length; i++){
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						
						if(methodArgOID != null && methodArgOID != oid) {
							this.updateInitRec(log, methodArgOID, currentRecord);
						}
					}
				}
			}
		}
		
		return null;
	}

	private int handleArrayInit(final CaptureLog log,
			final ICodeGenerator generator, final Set<Class<?>> blackList,
			int currentRecord, int currentOID) {
		Object[] methodArgs;
		methodArgs = log.params.get(currentRecord);
		restoreArgs(methodArgs, currentRecord, log, generator, blackList);
		generator.createArrayInitStmt(log, currentRecord);
		currentRecord = findEndOfMethod(log, currentRecord, currentOID);
		this.updateInitRec(log, currentOID, currentRecord);
		return currentRecord;
	}

	private int handleMapInit(final CaptureLog log,
			final ICodeGenerator generator, final Set<Class<?>> blackList,
			int currentRecord, int currentOID) {
		Object[] methodArgs;
		methodArgs = log.params.get(currentRecord);
		restoreArgs(methodArgs, currentRecord, log, generator, blackList);
		generator.createMapInitStmt(log, currentRecord);
		currentRecord = findEndOfMethod(log, currentRecord, currentOID);
		this.updateInitRec(log, currentOID, currentRecord);
		return currentRecord;
	}

	private int handleCollectionInit(final CaptureLog log,
			final ICodeGenerator generator, final Set<Class<?>> blackList,
			int currentRecord, int currentOID) {
		Object[] methodArgs;
		methodArgs = log.params.get(currentRecord);
		restoreArgs(methodArgs, currentRecord, log, generator, blackList);
		generator.createCollectionInitStmt(log, currentRecord);
		currentRecord = findEndOfMethod(log, currentRecord, currentOID);
		this.updateInitRec(log, currentOID, currentRecord);
		return currentRecord;
	}

	private int handlePlainInit(final CaptureLog log,
			final ICodeGenerator generator, int currentRecord, int currentOID) {
		// e.g. String var = "Hello World";
		generator.createPlainInitStmt(log, currentRecord);
		currentRecord = findEndOfMethod(log, currentRecord, currentOID);
		this.updateInitRec(log, currentOID, currentRecord);
		return currentRecord;
	}
	
	
	private boolean isBlackListed(final int oid, final Set<Class<?>> blackList, final CaptureLog log)
	{
		return blackList.contains(this.getClassFromOID(log, oid));
	}
	
	private int[] getExchange(final CaptureLog log, final int currentRecord, final int oid, final Set<Class<?>> blackList)
	{
			int callerOID;
			int r = currentRecord;
			
			do 
			{
				callerOID = this.findCaller(log, ++r);
			}
			while(this.isBlackListed(callerOID, blackList, log)); //   blackList.contains(this.getClassFromOID(log, callerOID)));
			
			blackList.add(this.getClassFromOID(log, oid));
			
			return new int[]{oid, callerOID};
	}
	
}