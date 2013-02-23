package org.evosuite.testcarver.codegen;

import gnu.trove.list.array.TIntArrayList;

import java.util.HashSet;

import org.evosuite.testcarver.capture.CaptureLog;



public final class CaptureLogAnalyzer implements ICaptureLogAnalyzer
{
	@SuppressWarnings("rawtypes")
	@Override
	public void analyze(final CaptureLog originalLog, final ICodeGenerator generator, final Class<?>... observedClasses) 
	{
		final CaptureLog log = originalLog.clone();
		
		//--- 1. step: extract class names
		final HashSet<String> observedClassNames = new HashSet<String>();
		for(int i = 0; i < observedClasses.length; i++)
		{
			observedClassNames.add(observedClasses[i].getName());
		}
			
		
		//--- 2. step: get all oids of the instances of the observed classes
		//    NOTE: They are implicitly sorted by INIT_REC_NO because of the natural object creation order captured by the 
		//    instrumentation
		final TIntArrayList targetOIDs = new TIntArrayList();
		final int numInfoRecs = log.oidClassNames.size();
		for(int i = 0; i < numInfoRecs; i++)
		{
			if(observedClassNames.contains(log.oidClassNames.get(i)))
			{
				targetOIDs.add(log.oids.getQuick(i));
			}
		}
		
		
		//--- 3. step: analyze log
		
		generator.before(log);
		
		final int numLogRecords = log.objectIds.size();
		int currentOID          = targetOIDs.getQuick(0);
		
		// TODO knowing last logRecNo for termination criterion belonging to an observed instance would prevent processing unnecessary statements
		for(int currentRecord = log.oidRecMapping.get(currentOID); currentRecord < numLogRecords; currentRecord++)
		{
			currentOID = log.objectIds.getQuick(currentRecord);

			if(targetOIDs.contains(currentOID))
			{
				this.restorceCodeFromLastPosTo(log, generator, currentOID, currentRecord);

				// forward to end of method call sequence
				currentRecord = findEndOfMethod(log, currentRecord, currentOID);
				
				// each method call is considered as object state modification -> so save last object modification
				log.oidInitRecNo.setQuick(log.oidRecMapping.get(currentOID), currentRecord);
			}
		}		
		
		generator.after(log);
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
		while(! (log.objectIds.getQuick(record) == currentOID &&
				 log.captureIds.getQuick(record) == captureId && 
				 log.methodNames.get(record).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD))
				&& record < numRecords)
		{
			record++;
		}
		
		return record;
    }
	
	@SuppressWarnings({ "rawtypes" })
	private void restorceCodeFromLastPosTo(final CaptureLog log, final ICodeGenerator generator,final int oid, final int end)
	{
		final int oidInfoRecNo = log.oidRecMapping.get(oid);
		
		// start from last OID modification point
		int currentRecord = log.oidInitRecNo.get(oidInfoRecNo);
		if(currentRecord > 0)
		{
			// last modification of object happened here
			// -> we start looking for interesting records after retrieved record
			currentRecord++;				
		}
		else
		{
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
		
		for(; currentRecord <= end; currentRecord++)
		{
			currentOID     = log.objectIds.getQuick(currentRecord);
			returnValueObj = log.returnValues.get(currentRecord);
			returnValue    = returnValueObj.equals(CaptureLog.RETURN_TYPE_VOID) ? -1 : (Integer) returnValueObj;
			
			if(oid == currentOID ||	returnValue == oid)
			{
				methodName = log.methodNames.get(currentRecord);
				
				if(CaptureLog.PLAIN_INIT.equals(methodName)) // e.g. String var = "Hello World";
				{
					generator.createPlainInitStmt(log, currentRecord);
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
					this.updateInitRec(log, currentOID, currentRecord);
				}
				else if(CaptureLog.NOT_OBSERVED_INIT.equals(methodName)) // e.g. Person var = (Person) XSTREAM.fromXML("<xml/>");
				{
					generator.createUnobservedInitStmt(log, currentRecord);
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
				}
				else if(CaptureLog.PUTFIELD.equals(methodName) || CaptureLog.PUTSTATIC.equals(methodName) || // field write access such as p.id = id or Person.staticVar = "something"
						CaptureLog.GETFIELD.equals(methodName) || CaptureLog.GETSTATIC.equals(methodName))   // field READ access such as "int a =  p.id" or "String var = Person.staticVar"
				{
					
					if(CaptureLog.PUTFIELD.equals(methodName) || CaptureLog.PUTSTATIC.equals(methodName))
					{
						// a field assignment has always one argument
						methodArgs = log.params.get(currentRecord);
						methodArgOID = (Integer) methodArgs[0];
						if(methodArgOID != null && methodArgOID != oid)
						{
							// create history of assigned value
							this.restorceCodeFromLastPosTo(log, generator, methodArgOID, currentRecord);
						}

						generator.createFieldWriteAccessStmt(log, currentRecord);

					}
					else
					{
						generator.createFieldReadAccessStmt(log, currentRecord);
					}
					
					
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
					
					if(CaptureLog.GETFIELD.equals(methodName) || CaptureLog.GETSTATIC.equals(methodName))
					{
						// GETFIELD and GETSTATIC should only happen, if we obtain an instance whose creation has not been observed
						this.updateInitRec(log, currentOID, currentRecord);
						
						if(returnValue != -1)
						{
							this.updateInitRec(log, returnValue, currentRecord);
						}
					}
				}
				else // var0.call(someArg) or Person var0 = new Person()
				{
					methodArgs = log.params.get(currentRecord);
					
					for(int i = 0; i < methodArgs.length; i++)
					{
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						if(methodArgOID != null && methodArgOID != oid)
						{
							this.restorceCodeFromLastPosTo(log, generator, methodArgOID, currentRecord);
						}
					}
					generator.createMethodCallStmt(log, currentRecord);
					
					// forward to end of method call sequence
					
					currentRecord = findEndOfMethod(log, currentRecord, currentOID);
					
					// each method call is considered as object state modification -> so save last object modification
					this.updateInitRec(log, currentOID, currentRecord);
					
					if(returnValue != -1)
					{
						// if returnValue has not type VOID, mark current log record as record where the return value instance was created
						// --> if an object is created within an observed method, it would not be semantically correct
						//     (and impossible to handle properly) to create an extra instance of the return value type outside this method
						this.updateInitRec(log, returnValue, currentRecord);
					}
					
					
					// consider each passed argument as being modified at the end of the method call sequence
					for(int i = 0; i < methodArgs.length; i++)
					{
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						if(methodArgOID != null && methodArgOID != oid) 
						{
							this.updateInitRec(log, methodArgOID, currentRecord);
						}
					}
				}
			}
		}
	}
}