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
package org.evosuite.testcarver.codegen;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;


@Deprecated
public class CodeGenerator 
{
	private final CaptureLog log;

	//--- source generation
	private final TIntObjectHashMap<String>   oidToVarMapping;
	private final TIntObjectHashMap<Class<?>> oidToTypeMapping;
	
	private int         varCounter;
	private TIntHashSet failedRecords;
	
	private boolean isNewInstanceMethodNeeded;
	private boolean isCallMethodMethodNeeded;
	private boolean isSetFieldMethodNeeded;
	private boolean isGetFieldMethodNeeded;
	private boolean isXStreamNeeded;
	
	public CodeGenerator(final CaptureLog log)
	{
		if(log == null)
		{
			throw new NullPointerException();
		}
		
		this.log              = log.clone();
		this.oidToVarMapping  = new TIntObjectHashMap<String>();
		this.oidToTypeMapping = new TIntObjectHashMap<Class<?>>();
		this.varCounter       = 0;
		
		
		this.isNewInstanceMethodNeeded = false;
		this.isCallMethodMethodNeeded  = false;
		this.isSetFieldMethodNeeded    = false;
		this.isGetFieldMethodNeeded    = false;
		this.isXStreamNeeded           = false;
	}

	
	
	public CompilationUnit generateCodeForPostProcessing(final String cuName, final String packageName, final Class<?>...observedClasses)
	{
		return this.generateCode(cuName, packageName, true, observedClasses);
	}
	
	public CompilationUnit generateFinalCode(final String cuName, final String packageName, final TIntArrayList failedRecords, final Class<?>...observedClasses)
	{
		if(failedRecords == null)
		{
			throw new NullPointerException("list of failed records must not be null");
		}
		
		this.failedRecords = new TIntHashSet(failedRecords);
		return this.generateCode(cuName, packageName, false, observedClasses);
	}
	
	
	// FIXME specifying classes here might not be needed anymore, if we only instrument observed classes...
	@SuppressWarnings("unchecked")
	private CompilationUnit generateCode(final String cuName, final String packageName, final boolean postprocessing, final Class<?>...observedClasses)
	{
		if(cuName == null || cuName.isEmpty())
		{
			throw new IllegalArgumentException("Illegal compilation unit name: " + cuName);
		}
	
		if(observedClasses == null || observedClasses.length == 0)
		{
			throw new IllegalArgumentException("No observed classes specified");
		}
		
		if(packageName == null)
		{
			throw new NullPointerException("package name must not be null");
		}
		
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
		final int numInfoRecs = this.log.oidClassNames.size();
		for(int i = 0; i < numInfoRecs; i++)
		{
			if(observedClassNames.contains(this.log.oidClassNames.get(i)))
			{
				targetOIDs.add(this.log.oids.getQuick(i));
			}
		}

		
		//--- 3. step: init compilation unit

		final AST ast = AST.newAST(AST.JLS3);
		CompilationUnit cu = ast.newCompilationUnit();

		// package declaration
		final PackageDeclaration p1 = ast.newPackageDeclaration();
		p1.setName(ast.newName(packageName.split("\\.")));
		cu.setPackage(p1);

		// import specifications
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "org", "junit", "Test" }));
		cu.imports().add(id);
		
		// class declaration
		final TypeDeclaration td = ast.newTypeDeclaration();
		td.setName(ast.newSimpleName(cuName));
		td.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		cu.types().add(td);
		

		
		// test method construction
		final MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(ast.newSimpleName("test"));
		md.thrownExceptions().add(ast.newSimpleName("Exception"));

		
		
		// sets @Test annotation to test method
		final NormalAnnotation annotation = ast.newNormalAnnotation();
		annotation.setTypeName(ast.newSimpleName("Test"));
		md.modifiers().add(annotation);
		
		// sets method to public
		md.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		td.bodyDeclarations().add(md);
		
		final Block block = ast.newBlock();
		md.setBody(block);

		// no invocations on objects of observed classes -> return empty but compilable CompilationUnit
		if(targetOIDs.isEmpty())
		{
			return cu;
		}
		
		//--- 4. step: generating code starting with OID with lowest log rec no.

		final int numLogRecords = this.log.objectIds.size();
		int currentOID = targetOIDs.getQuick(0);
		
		
		try
		{
			// TODO knowing last logRecNo for termination criterion belonging to an observed instance would prevent processing unnecessary statements
			for(int currentRecord = this.log.oidRecMapping.get(currentOID); currentRecord < numLogRecords; currentRecord++)
			{
				currentOID = this.log.objectIds.getQuick(currentRecord);

				if(targetOIDs.contains(currentOID))
				{
					this.restorceCodeFromLastPosTo(packageName, currentOID, currentRecord, postprocessing, ast, block);

					// forward to end of method call sequence
					currentRecord = findEndOfMethod(currentRecord, currentOID);
					
					// each method call is considered as object state modification -> so save last object modification
					log.updateWhereObjectWasInitializedFirst(currentOID, currentRecord);		
					
				}
			}		
			
		}
		catch(Exception e)
		{
//			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+this.log);
			throw new RuntimeException(e.getMessage(), e);
		}

		
		if(this.isXStreamNeeded)
		{
			id = ast.newImportDeclaration();
			id.setName(ast.newName(new String[] { "com", "thoughtworks", "xstream", "XStream" }));
			cu.imports().add(id);
			
			// create XSTREAM constant: private static final XStream XSTREAM = new XStream();
			final VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
			vd.setName(ast.newSimpleName("XSTREAM"));			
			final ClassInstanceCreation ci = ast.newClassInstanceCreation();
			ci.setType(this.createAstType("XStream", ast));
			vd.setInitializer(ci);
			final FieldDeclaration xstreamConst = ast.newFieldDeclaration(vd);
			xstreamConst.setType(this.createAstType("XStream", ast));
			xstreamConst.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			xstreamConst.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
			xstreamConst.modifiers().add(ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
			td.bodyDeclarations().add(xstreamConst);
			
			this.isXStreamNeeded = false;
		}
		
		
		//-- creates utility method to set field value via reflections
		if(this.isSetFieldMethodNeeded)
		{
			this.createSetFieldMethod(td, cu, ast);
			this.isSetFieldMethodNeeded = false;
		}
		
		//-- creates utility method to get field value via reflections
		if(this.isGetFieldMethodNeeded)
		{
			this.createGetFieldMethod(td, cu, ast);
			this.isGetFieldMethodNeeded = false;
		}
		
		//-- creates utility method to call method via reflections
		if(this.isCallMethodMethodNeeded)
		{
			this.createCallMethod(td, cu, ast);
			this.isCallMethodMethodNeeded = false;
		}
		
		//-- creates utility method to call constructor via reflections
		if(this.isNewInstanceMethodNeeded)
		{
			this.createNewInstanceMethod(td, cu, ast);
			this.isNewInstanceMethodNeeded = false;
		}
		
		return cu;
	}
	
	
	private void updateInitRec(final int currentOID, final int currentRecord)
	{
		
		if(currentRecord > log.getRecordIndexOfWhereObjectWasInitializedFirst(currentOID))
		{
			log.updateWhereObjectWasInitializedFirst(currentOID, currentRecord);		

		}
	}

    
    private int findEndOfMethod(final int currentRecord, final int currentOID)
    {
    	int record = currentRecord;
		
    	final int captureId = this.log.captureIds.getQuick(currentRecord);
		while(! (this.log.objectIds.getQuick(record) == currentOID &&
				 this.log.captureIds.getQuick(record) == captureId && 
				 this.log.methodNames.get(record).equals(CaptureLog.END_CAPTURE_PSEUDO_METHOD)))
		{
			record++;
		}
		
		return record;
    }
	
	@SuppressWarnings("unchecked")
	private void restorceCodeFromLastPosTo(final String packageName, final int oid, final int end, final boolean postprocessing, final AST ast, final Block block)
	{
		final int oidInfoRecNo = this.log.oidRecMapping.get(oid);
		
		// start from last OID modification point
		
		int currentRecord = log.getRecordIndexOfWhereObjectWasInitializedFirst(oid);
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
			currentOID     = this.log.objectIds.getQuick(currentRecord);
			returnValueObj = this.log.returnValues.get(currentRecord);
			returnValue    = returnValueObj.equals(CaptureLog.RETURN_TYPE_VOID) ? -1 : (Integer) returnValueObj;
			
			if(oid == currentOID ||	returnValue == oid)
			{
				methodName = this.log.methodNames.get(currentRecord);
				
				if(CaptureLog.PLAIN_INIT.equals(methodName)) // e.g. String var = "Hello World";
				{
					PostProcessor.notifyRecentlyProcessedLogRecNo(currentRecord);
					this.createPlainInitStmt(currentRecord, block, ast);
					
					
					
					currentRecord = findEndOfMethod(currentRecord, currentOID);
					
					this.updateInitRec(currentOID, currentRecord);
				}
				else if(CaptureLog.NOT_OBSERVED_INIT.equals(methodName)) // e.g. Person var = (Person) XSTREAM.fromXML("<xml/>");
				{
					PostProcessor.notifyRecentlyProcessedLogRecNo(currentRecord);
					this.createUnobservedInitStmt(currentRecord, block, ast);
					
					currentRecord = findEndOfMethod(currentRecord, currentOID);
				}
				else if(CaptureLog.PUTFIELD.equals(methodName) || CaptureLog.PUTSTATIC.equals(methodName) || // field write access such as p.id = id or Person.staticVar = "something"
						CaptureLog.GETFIELD.equals(methodName) || CaptureLog.GETSTATIC.equals(methodName))   // field READ access such as "int a =  p.id" or "String var = Person.staticVar"
				{
					
					if(CaptureLog.PUTFIELD.equals(methodName) || CaptureLog.PUTSTATIC.equals(methodName))
					{
						// a field assignment has always one argument
						methodArgs = this.log.params.get(currentRecord);
						methodArgOID = (Integer) methodArgs[0];
						if(methodArgOID != null && methodArgOID != oid)
						{
							// create history of assigned value
							this.restorceCodeFromLastPosTo(packageName, methodArgOID, currentRecord, postprocessing, ast, block);
						}

						this.createFieldWriteAccessStmt(packageName, currentRecord, block, ast);
					}
					else
					{
						this.createFieldReadAccessStmt(packageName, currentRecord, block, ast);
					}
					
					
					currentRecord = findEndOfMethod(currentRecord, currentOID);
					
					if(CaptureLog.GETFIELD.equals(methodName) || CaptureLog.GETSTATIC.equals(methodName))
					{
						// GETFIELD and GETSTATIC should only happen, if we obtain an instance whose creation has not been observed
						this.updateInitRec(currentOID, currentRecord);
						
						if(returnValue != -1)
						{
							this.updateInitRec(returnValue, currentRecord);
						}
					}
				}
				else // var0.call(someArg) or Person var0 = new Person()
				{
					methodArgs = this.log.params.get(currentRecord);
					
					for(int i = 0; i < methodArgs.length; i++)
					{
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						if(methodArgOID != null && methodArgOID != oid)
						{
							this.restorceCodeFromLastPosTo(packageName, methodArgOID, currentRecord, postprocessing, ast, block);
						}
					}
					
					PostProcessor.notifyRecentlyProcessedLogRecNo(currentRecord);
					this.createMethodCallStmt(packageName, currentRecord, postprocessing, block, ast);
					block.statements().add(ast.newEmptyStatement());
					
					// forward to end of method call sequence
					
					currentRecord = findEndOfMethod(currentRecord, currentOID);
					
					// each method call is considered as object state modification -> so save last object modification
					this.updateInitRec(currentOID, currentRecord);
					
					if(returnValue != -1)
					{
						// if returnValue has not type VOID, mark current log record as record where the return value instance was created
						// --> if an object is created within an observed method, it would not be semantically correct
						//     (and impossible to handle properly) to create an extra instance of the return value type outside this method
						this.updateInitRec(returnValue, currentRecord);
					}
					
					
					// consider each passed argument as being modified at the end of the method call sequence
					for(int i = 0; i < methodArgs.length; i++)
					{
						// there can only be OIDs or null
						methodArgOID = (Integer) methodArgs[i];
						if(methodArgOID != null && methodArgOID != oid) 
						{
							this.updateInitRec(methodArgOID, currentRecord);
						}
					}
				}
			}
		}
	}

	
	
	private String createNewVarName(final int oid, final String typeName)
	{
		if(this.oidToVarMapping.containsKey(oid))
		{
			throw new IllegalStateException("There is already an oid to var mapping for oid " + oid);
		}
		
		try
		{
			this.oidToTypeMapping.put(oid, Class.forName(typeName));
		}
		catch(final ClassNotFoundException e)
		{
			throw new IllegalArgumentException(e);
		}
		
		
		final String varName = "var" + this.varCounter++;
		this.oidToVarMapping.put(oid, varName);
		
		return varName;
	}
	
	
	// TODO easy but inefficient implementation
	private Type createAstArrayType(final String type, final AST ast)
	{
		// NOTE: see asm4 guide p. 11 for more insights about internal type representation
		
		final int arrayDim = type.lastIndexOf('[') + 1;
		
		if(type.contains("[L")) 
		{
			// --- object array
			
			// TODO use regex for extraction
			
			String extractedType = type.substring(type.indexOf('L') + 1, type.length() - 1);
			extractedType        = extractedType.replaceFirst("java\\.lang\\.", "");
			
			return ast.newArrayType(this.createAstType(extractedType, ast), arrayDim);
		}
		else
		{
			// --- primitive type array
			
			if(type.contains("[I")) // int[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("int")), arrayDim);
			}
			else if(type.contains("[B")) // byte[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("byte")), arrayDim);
			}
			else if(type.contains("[C")) // char[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("char")), arrayDim);
			}
			else if(type.contains("[D")) // double[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("double")), arrayDim);
			}
			else if(type.contains("[Z")) // boolean[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("boolean")), arrayDim);
			}
			else if(type.contains("[F")) // float[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("float")), arrayDim);
			}
			else if(type.contains("[S")) // short[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("short")), arrayDim);
			}
			else if(type.contains("[J")) // long[]
			{
				return ast.newArrayType(ast.newSimpleType(ast.newSimpleName("long")), arrayDim);
			}
			else
			{
				throw new RuntimeException("Can not create array type for " + type);
			}
		}
	}
	
	
	
	private Type createAstType(final String type, final AST ast)
	{
		if(type.startsWith("[")) // does type specify an array?
		{
			return this.createAstArrayType(type, ast);
		}
		else
		{
			final String[] fragments = type.split("\\.");
			if(fragments.length == 1)
			{
				return ast.newSimpleType(ast.newSimpleName(fragments[0]));
			}
			
		
			final String[] pkgArray = new String[fragments.length - 1];
			System.arraycopy(fragments, 0, pkgArray, 0, pkgArray.length);
			final SimpleType pkgType = ast.newSimpleType(ast.newName(pkgArray));
			
			return  ast.newQualifiedType( pkgType,  ast.newSimpleName(fragments[fragments.length - 1]));
		}
	}
	
	
	/*
	 * Needed to preserve program flow: 
	 * 
	 * try
	 * {
	 *    var0.doSth();
	 * }
	 * catch(Throwable t) {}
	 */
	@SuppressWarnings("unchecked")
	private TryStatement createTryStmtWithEmptyCatch(final AST ast, Statement stmt)
	{
		final TryStatement tryStmt = ast.newTryStatement();
		tryStmt.getBody().statements().add(stmt);
		
		final CatchClause cc = ast.newCatchClause();
		SingleVariableDeclaration excDecl = ast.newSingleVariableDeclaration();
		excDecl.setType(ast.newSimpleType(ast.newSimpleName("Throwable")));
		excDecl.setName(ast.newSimpleName("t"));
		cc.setException(excDecl);
		tryStmt.catchClauses().add(cc);
		
		return tryStmt;
	}
	
	/*
	 * try
	 * {
	 *    var0.doSth();
	 * }
	 * catch(Throwable t)
	 * {
	 *    org.uni.saarland.sw.prototype.capture.PostProcessor.captureException(<logRecNo>);
	 * }
	 */
	@SuppressWarnings("unchecked")
	private TryStatement createTryStatementForPostProcessing(final AST ast, final Statement stmt, final int logRecNo)
	{
		final TryStatement     tryStmt = this.createTryStmtWithEmptyCatch(ast, stmt);
		final CatchClause      cc      = (CatchClause) tryStmt.catchClauses().get(0);
		final MethodInvocation m       = ast.newMethodInvocation();
		
		m.setExpression(ast.newName(new String[]{"de","unisb", "cs","st","testcarver","capture", "PostProcessor"}));
		m.setName(ast.newSimpleName("captureException"));
		m.arguments().add(ast.newNumberLiteral(String.valueOf(logRecNo)));
		cc.getBody().statements().add(ast.newExpressionStatement(m));
		
		
		MethodInvocation m2 = ast.newMethodInvocation();
		m2.setExpression(ast.newSimpleName("t"));
		m2.setName(ast.newSimpleName("printStackTrace"));
		cc.getBody().statements().add(ast.newExpressionStatement(m2));
		
		return tryStmt;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	private MethodInvocation createCallMethodOrNewInstanceCallStmt(final boolean isConstructorCall, final AST ast, final String varName, final String targetTypeName, final String methodName, final Object[] methodArgs, final org.objectweb.asm.Type[] paramTypes)
	{
		//-- construct getField() call
		final MethodInvocation callMethodCall = ast.newMethodInvocation();
		
		if(isConstructorCall)
		{
			callMethodCall.setName(ast.newSimpleName("newInstance"));
		}
		else
		{
			callMethodCall.setName(ast.newSimpleName("callMethod"));
		}
		
		
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(targetTypeName);
		callMethodCall.arguments().add(stringLiteral);              // class name
		
		if(! isConstructorCall)
		{
			stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue(methodName);
			callMethodCall.arguments().add(stringLiteral);              // method name
	
			if(varName == null)
			{
				callMethodCall.arguments().add(ast.newNullLiteral()); // static call -> no receiver
			}
			else
			{
				callMethodCall.arguments().add(ast.newSimpleName(varName)); // receiver
			}
		}
		
		// method arguments
		ArrayCreation arrCreation = ast.newArrayCreation();
		arrCreation.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Object"))));
		ArrayInitializer arrInit = ast.newArrayInitializer();
		arrCreation.setInitializer(arrInit);
		callMethodCall.arguments().add(arrCreation);
		
		Integer  arg; // is either an oid or null
		for(int i = 0; i < methodArgs.length; i++)
		{
			arg = (Integer) methodArgs[i];
			if(arg == null)
			{
				arrInit.expressions().add(ast.newNullLiteral());
			}
			else
			{
				arrInit.expressions().add(ast.newSimpleName(this.oidToVarMapping.get(arg)));
			}
		}	
		
		
		// paramTypes
		arrCreation = ast.newArrayCreation();
		arrCreation.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Class"))));
		arrInit = ast.newArrayInitializer();
		arrCreation.setInitializer(arrInit);
		callMethodCall.arguments().add(arrCreation);
		
		org.objectweb.asm.Type type;
		for(int i = 0; i < paramTypes.length; i++)
		{
			type = paramTypes[i];
			
			if(type.equals(org.objectweb.asm.Type.BOOLEAN_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Boolean"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.BYTE_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Byte"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.CHAR_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Character"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.DOUBLE_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Double"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.FLOAT_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Float"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.INT_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Integer"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.LONG_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Long"));
				arrInit.expressions().add(facc);
			}
			else if(type.equals(org.objectweb.asm.Type.SHORT_TYPE))
			{
				FieldAccess facc = ast.newFieldAccess();
				facc.setName(ast.newSimpleName("TYPE"));
				facc.setExpression(ast.newSimpleName("Short"));
				arrInit.expressions().add(facc);
			}
			else
			{
				final TypeLiteral clazz = ast.newTypeLiteral();
				clazz.setType(ast.newSimpleType(ast.newName(type.getClassName())));

				arrInit.expressions().add(clazz);
			}
		}	
		
		return callMethodCall;
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	private void createMethodCallStmt(final String packageName, final int logRecNo, final boolean postprocessing, final Block methodBlock, final AST ast)
	{
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int      oid        = this.log.objectIds.get(logRecNo);
		final Object[] methodArgs = this.log.params.get(logRecNo);
		final String   methodName = this.log.methodNames.get(logRecNo);
		
		
		final  String                   methodDesc       = this.log.descList.get(logRecNo);
		final  org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);
		
		final Class<?>[] methodParamTypeClasses = new Class[methodParamTypes.length];
		for(int i = 0; i < methodParamTypes.length; i++)
		{
			methodParamTypeClasses[i] = getClassForName(methodParamTypes[i].getClassName());
		}
		
		final String  typeName  = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
		Class<?> type = getClassForName(typeName);
		
//		Class<?> type;
//		try {
//			type = Class.forName(typeName);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		}
		
		final boolean haveSamePackage = type.getPackage().getName().equals(packageName); // TODO might be nicer...
		
		
		
		
		
		final Statement finalStmt;
		
		@SuppressWarnings("rawtypes")
		final List arguments;
		if(CaptureLog.OBSERVED_INIT.equals(methodName))
		{
			/*
			 * Person var0 = null;
			 * {
			 *    var0 = new Person();
			 * }
			 * catch(Throwable t)
			 * {
			 *    org.uni.saarland.sw.prototype.capture.PostProcessor.captureException(<logRecNo>);
			 * }
			 */
			
			
			// e.g. Person var0 = null;
			final String varName = this.createNewVarName(oid, typeName);
			VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
			vd.setName(ast.newSimpleName(varName));			
			VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(vd);
			stmt.setType(this.createAstType(typeName, ast));
			vd.setInitializer(ast.newNullLiteral());
			methodBlock.statements().add(stmt);	
			
			try
			{
				this.getConstructorModifiers(type,methodParamTypeClasses);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			final int     constructorTypeModifiers = this.getConstructorModifiers(type,methodParamTypeClasses);
			final boolean isPublic                 = java.lang.reflect.Modifier.isPublic(constructorTypeModifiers);
			final boolean isReflectionAccessNeeded = ! isPublic && ! haveSamePackage;
			
			if(isReflectionAccessNeeded)
			{
				this.isNewInstanceMethodNeeded = true;
				final MethodInvocation mi	   = this.createCallMethodOrNewInstanceCallStmt(true, ast, varName, typeName, methodName, methodArgs, methodParamTypes);
				arguments 				  	   = null;
				
				final Assignment assignment = ast.newAssignment();
				assignment.setLeftHandSide(ast.newSimpleName(varName));
				assignment.setOperator(Operator.ASSIGN);
				
				final CastExpression cast = ast.newCastExpression();
				cast.setType(this.createAstType(typeName, ast));
				cast.setExpression(mi);
				assignment.setRightHandSide(cast);
				
				finalStmt = ast.newExpressionStatement(assignment);
			}
			else
			{
				// e.g. var0 = new Person();
				final ClassInstanceCreation ci = ast.newClassInstanceCreation();
				ci.setType(this.createAstType(typeName, ast));

				final Assignment assignment = ast.newAssignment();
				assignment.setLeftHandSide(ast.newSimpleName(varName));
				assignment.setOperator(Operator.ASSIGN);
				assignment.setRightHandSide(ci);
				
				finalStmt = ast.newExpressionStatement(assignment);
				arguments = ci.arguments();
			}
		}
		else //------------------ handling for ordinary method calls e.g. var1 = var0.doSth();
		{
			String returnVarName = null;
			
			final String desc       = this.log.descList.get(logRecNo);
			final String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
			
			
			final Object returnValue = this.log.returnValues.get(logRecNo);
			if(! CaptureLog.RETURN_TYPE_VOID.equals(returnValue))
			{
				Integer returnValueOID = (Integer) returnValue;
				
				// e.g. Person var0 = null;
				returnVarName = this.createNewVarName(returnValueOID, returnType);

				
				VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
				vd.setName(ast.newSimpleName(returnVarName));			
				VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(vd);
				stmt.setType(this.createAstType(returnType, ast));
				vd.setInitializer(ast.newNullLiteral());
				methodBlock.statements().add(stmt);	
			}

			
			final String varName = this.oidToVarMapping.get(oid);
			

			
			final int     methodTypeModifiers      = this.getMethodModifiers(type, methodName, methodParamTypeClasses);
			final boolean isPublic                 = java.lang.reflect.Modifier.isPublic(methodTypeModifiers);
			final boolean isReflectionAccessNeeded = ! isPublic && ! haveSamePackage;
			
			// e.g. Person var0 = var1.getPerson("Ben");
			
			final MethodInvocation mi;
			
			if(isReflectionAccessNeeded)
			{
				this.isCallMethodMethodNeeded = true;
				mi                            = this.createCallMethodOrNewInstanceCallStmt(false, ast, varName, typeName, methodName, methodArgs, methodParamTypes);
				arguments                     = null;
				
				if(returnVarName != null)
				{
					final Assignment assignment = ast.newAssignment();
					assignment.setLeftHandSide(ast.newSimpleName(returnVarName));
					assignment.setOperator(Operator.ASSIGN);
					
					final CastExpression cast = ast.newCastExpression();
					cast.setType(this.createAstType(returnType, ast));
					cast.setExpression(mi);
					assignment.setRightHandSide(cast);
					
					finalStmt = ast.newExpressionStatement(assignment);
				}
				else
				{
					finalStmt = ast.newExpressionStatement(mi);
				}
			}	
			else
			{
			    mi = ast.newMethodInvocation();
				 
				if( this.log.isStaticCallList.get(logRecNo)) 
				{
					// can only happen, if this is a static method call (because constructor statement has been reported)
					final String tmpType = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
					mi.setExpression(ast.newName(tmpType.split("\\.")));
				}
				else
				{
					mi.setExpression(ast.newSimpleName(varName));
				}
				
				mi.setName(ast.newSimpleName(methodName));
				
				
				arguments = mi.arguments();
				
				if(returnVarName != null)
				{
					final Assignment assignment = ast.newAssignment();
					assignment.setLeftHandSide(ast.newSimpleName(returnVarName));
					assignment.setOperator(Operator.ASSIGN);
					assignment.setRightHandSide(mi);
					
					finalStmt = ast.newExpressionStatement(assignment);
				}
				else
				{
					finalStmt = ast.newExpressionStatement(mi);
				}
			}
		}
		
		
		
		
		
		if(postprocessing)
		{
			final TryStatement tryStmt = this.createTryStatementForPostProcessing(ast, finalStmt, logRecNo);
			methodBlock.statements().add(tryStmt);
		}
		else
		{
			if(this.failedRecords.contains(logRecNo))
			{
				// we just need an empty catch block to preserve program flow
				final TryStatement tryStmt = this.createTryStmtWithEmptyCatch(ast, finalStmt);
				methodBlock.statements().add(tryStmt);
			}
			else
			{
				 methodBlock.statements().add(finalStmt);	
			}
		}
		
		
		if(arguments != null)
		{
//			final  String                   methodDesc       = this.log.descList.get(logRecNo);
//			final  org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);

			Class<?> methodParamType;
			Class<?> argType;
			
			Integer  arg; // is either an oid or null
			for(int i = 0; i < methodArgs.length; i++)
			{
				arg = (Integer) methodArgs[i];
				if(arg == null)
				{
					arguments.add(ast.newNullLiteral());
				}
				else
				{
					methodParamType = CaptureUtil.getClassFromDesc(methodParamTypes[i].getDescriptor());
					argType			= this.oidToTypeMapping.get(arg);
							
					if(methodParamType.isAssignableFrom(argType))
					{
						arguments.add(ast.newSimpleName(this.oidToVarMapping.get(arg)));
					}
					else
					{
						// we need an up-cast
						final CastExpression cast = ast.newCastExpression();
						cast.setType(ast.newSimpleType(ast.newName(methodParamType.getName())));
						cast.setExpression(ast.newSimpleName(this.oidToVarMapping.get(arg)));
						arguments.add(cast);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void createPlainInitStmt(final int logRecNo, final Block methodBlock, final AST ast)
	{
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int    oid   = this.log.objectIds.get(logRecNo);
		
		if(this.oidToVarMapping.containsKey(oid))
		{
			// TODO this might happen because of Integer.valueOf o.ä. . Is this approach ok?
			return;
		}
		
		final String type  = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
		final Object value = this.log.params.get(logRecNo)[0];
		
		
		final VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
		vd.setName(ast.newSimpleName(this.createNewVarName(oid, value.getClass().getName())));
		
		final VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(vd);

		if(value instanceof Class)
		{
			stmt.setType(ast.newSimpleType(ast.newSimpleName("Class")));
		}
		else
		{
			stmt.setType(this.createAstType(type, ast));
		}
		
		
		if(value instanceof Number)
		{
			if(value instanceof Long)
			{
				vd.setInitializer(ast.newNumberLiteral(String.valueOf(value) + 'l'));
			}
			else if(value instanceof Double)
			{
				vd.setInitializer(ast.newNumberLiteral(String.valueOf(value) + 'd'));
			}
			else
			{
				vd.setInitializer(ast.newNumberLiteral(String.valueOf(value)));
			}
			
		}
		else if(value instanceof String)
		{
			final StringLiteral literal = ast.newStringLiteral();
			literal.setLiteralValue((String) value);
			vd.setInitializer(literal);
			
		}
		else if(value instanceof Character)
		{
			final CharacterLiteral literal = ast.newCharacterLiteral();
			literal.setCharValue((Character) value);
			vd.setInitializer(literal);	
		}
		else if(value instanceof Boolean)
		{
			final BooleanLiteral literal = ast.newBooleanLiteral((Boolean)value);
			vd.setInitializer(literal);
		}
		else if(value instanceof Class)
		{
			final TypeLiteral clazz = ast.newTypeLiteral();
			clazz.setType(ast.newSimpleType(ast.newName(((Class<?>) value).getName())));
			vd.setInitializer(clazz);
		}
		else
		{
			throw new IllegalStateException("An error occurred while creating a plain statement: unsupported type: " + value.getClass().getName());
		}	
				
		methodBlock.statements().add(stmt);
	}
	
	
	@SuppressWarnings("unchecked")
	private void createUnobservedInitStmt(final int logRecNo, final Block methodBlock, final AST ast)
	{
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int    oid     = this.log.objectIds.get(logRecNo);
		final String type    = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
		final Object value   = this.log.params.get(logRecNo)[0];
		this.isXStreamNeeded = true;
		
		final VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
		// handling because there must always be a new instantiation statement for pseudo inits
		this.oidToVarMapping.remove(oid);
		vd.setName(ast.newSimpleName(this.createNewVarName(oid, type)));
		
		final MethodInvocation methodInvocation = ast.newMethodInvocation();
		final Name name = ast.newSimpleName("XSTREAM");
		methodInvocation.setExpression(name);
		methodInvocation.setName(ast.newSimpleName("fromXML")); 
		
		final StringLiteral xmlParam = ast.newStringLiteral();
		xmlParam.setLiteralValue((String) value);
		methodInvocation.arguments().add(xmlParam);
		
		final CastExpression castExpr = ast.newCastExpression();
		castExpr.setType(this.createAstType(type, ast));
		castExpr.setExpression(methodInvocation);
		
		vd.setInitializer(castExpr);
		
		final VariableDeclarationStatement vs = ast.newVariableDeclarationStatement(vd);
		vs.setType(this.createAstType(type, ast));
				
		methodBlock.statements().add(vs);
	}	
	
	
	
	@SuppressWarnings("unchecked")
	private void createFieldWriteAccessStmt(final String packageName, final int logRecNo, final Block methodBlock, final AST ast)
	{
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		
		final Object[] methodArgs = this.log.params.get(logRecNo);
		final String   methodName = this.log.methodNames.get(logRecNo);
		final int      oid        = this.log.objectIds.get(logRecNo);
		final int      captureId  = this.log.captureIds.get(logRecNo);
		
		final String  fieldName = this.log.namesOfAccessedFields.get(captureId);
		final String  typeName  = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
		
		Class<?> type = getClassForName(typeName);
//		try {
//			type = Class.forName(typeName);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		}
		
		final int     fieldTypeModifiers       = this.getFieldModifiers(type, fieldName);
		final boolean isPublic                 = java.lang.reflect.Modifier.isPublic(fieldTypeModifiers);
		final boolean haveSamePackage          = type.getPackage().getName().equals(packageName); // TODO might be nicer...
		final boolean isReflectionAccessNeeded = ! isPublic && ! haveSamePackage;
		
		
		if(isReflectionAccessNeeded)
		{
			this.isSetFieldMethodNeeded = true;
			final String varName        = this.oidToVarMapping.get(oid);
			
			final MethodInvocation setFieldCall = ast.newMethodInvocation();
			setFieldCall.setName(ast.newSimpleName("setField"));
			
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue(typeName);
			setFieldCall.arguments().add(stringLiteral);              // class name
			
			stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue(fieldName);
			setFieldCall.arguments().add(stringLiteral);              // field name
			
			setFieldCall.arguments().add(ast.newSimpleName(varName)); // receiver
			 
			final Integer arg = (Integer) methodArgs[0];
			if(arg == null)
			{
				setFieldCall.arguments().add(ast.newNullLiteral());  // value
			}
			else
			{
				setFieldCall.arguments().add(ast.newSimpleName(this.oidToVarMapping.get(arg))); // value
			}
			
			methodBlock.statements().add(ast.newExpressionStatement(setFieldCall));
		}
		else
		{
			FieldAccess fa = ast.newFieldAccess();
			if(CaptureLog.PUTSTATIC.equals(methodName))
			{
//				final String type = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
				fa.setExpression(ast.newName(typeName));//.split("\\.")));
			}
			else
			{
				final String varName = this.oidToVarMapping.get(oid);
				fa.setExpression(ast.newSimpleName(varName));
			}
			
			fa.setName(ast.newSimpleName(fieldName));
			
			
			final Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide(fa);
			
			final Integer arg = (Integer) methodArgs[0];
			if(arg == null)
			{
				assignment.setRightHandSide(ast.newNullLiteral());
			}
			else
			{
				final Class<?> argType   = this.oidToTypeMapping.get(arg);
				final String   fieldDesc = this.log.descList.get(logRecNo);
				final Class<?> fieldType = CaptureUtil.getClassFromDesc(fieldDesc);
				
				if(fieldType.isAssignableFrom(argType))
				{
					assignment.setRightHandSide(ast.newSimpleName(this.oidToVarMapping.get(arg)));
				}
				else
				{
					// we need an up-cast
					
					final CastExpression cast = ast.newCastExpression();
					cast.setType(ast.newSimpleType(ast.newName(fieldType.getName())));
					cast.setExpression(ast.newSimpleName(this.oidToVarMapping.get(arg)));
					assignment.setRightHandSide(cast);
				}
			}
			
			methodBlock.statements().add(ast.newExpressionStatement(assignment));
		}
	}	
	
	
	
	@SuppressWarnings("unchecked")
	private void createFieldReadAccessStmt(final String packageName, final int logRecNo, final Block methodBlock, final AST ast)
	{
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		
		final String   methodName = this.log.methodNames.get(logRecNo);
		final int      oid        = this.log.objectIds.get(logRecNo);
		final int      captureId  = this.log.captureIds.get(logRecNo);
		
		
		
		String returnVarName = null;
		
		final Object returnValue = this.log.returnValues.get(logRecNo);
		if(! CaptureLog.RETURN_TYPE_VOID.equals(returnValue))
		{
			Integer 	  returnValueOID  = (Integer) returnValue;
			final String  descriptor 	  = this.log.descList.get(logRecNo);
			final String  fieldTypeName   = org.objectweb.asm.Type.getType(descriptor).getClassName();
			final String  typeName        = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
			final String  fieldName       = this.log.namesOfAccessedFields.get(captureId);
			final String  receiverVarName = this.oidToVarMapping.get(oid);
			
			final Class<?> type = getClassForName(typeName);
//			try {
//				type = Class.forName(typeName);
//			} catch (ClassNotFoundException e) {
//				throw new RuntimeException(e);
//			}
			
			final int     fieldTypeModifiers       = this.getFieldModifiers(type, fieldName);
			final boolean isPublic                 = java.lang.reflect.Modifier.isPublic(fieldTypeModifiers);
			final boolean haveSamePackage          = type.getPackage().getName().equals(packageName); // TODO might be nicer...
			final boolean isReflectionAccessNeeded = ! isPublic && ! haveSamePackage;
			
			// e.g. Person var0 = Person.BEN;
			returnVarName                  = this.createNewVarName(returnValueOID, fieldTypeName);
			VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
			vd.setName(ast.newSimpleName(returnVarName));	
			
			if(isReflectionAccessNeeded)
			{
				this.isGetFieldMethodNeeded = true;
				
				//-- construct getField() call
				final MethodInvocation getFieldCall = ast.newMethodInvocation();
				getFieldCall.setName(ast.newSimpleName("getField"));
				
				StringLiteral stringLiteral = ast.newStringLiteral();
				stringLiteral.setLiteralValue(fieldTypeName);
				getFieldCall.arguments().add(stringLiteral);              // class name
				
				stringLiteral = ast.newStringLiteral();
				stringLiteral.setLiteralValue(fieldName);
				getFieldCall.arguments().add(stringLiteral);              // field name

				if(receiverVarName == null)
				{
					getFieldCall.arguments().add(ast.newNullLiteral()); // static call -> no receiver
				}
				else
				{
					getFieldCall.arguments().add(ast.newSimpleName(receiverVarName)); // receiver
				}
				
				
				
				// cast from object to field type
				final CastExpression cast = ast.newCastExpression();
				cast.setType(ast.newSimpleType(ast.newName(fieldTypeName)));
				cast.setExpression(getFieldCall);
				
				vd.setInitializer(cast);
			}
			else
			{
				FieldAccess fa = ast.newFieldAccess();
				if(CaptureLog.GETSTATIC.equals(methodName))
				{
					final String classType = this.log.oidClassNames.get(this.log.oidRecMapping.get(oid));
					fa.setExpression(ast.newName(classType.split("\\.")));
				}
				else
				{
					fa.setExpression(ast.newSimpleName(receiverVarName));
				}
				fa.setName(ast.newSimpleName(fieldName));
				
				vd.setInitializer(fa);
			}
			


			
			VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(vd);
			stmt.setType(this.createAstType(fieldTypeName, ast));
			
			methodBlock.statements().add(stmt);	
		}
	}	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createSetFieldMethod(final TypeDeclaration td, final CompilationUnit cu, final AST ast)
	{
//		public static void setField(final String clazzName, final String fieldName, final Object receiver, final Object value) throws Exception
//		{
//			final Class<?> clazz = Class.forName(clazzName);
//			final Field    f     = clazz.getDeclaredField(fieldName);
//			f.setAccessible(true);
//			f.set(receiver, value);
//		}
		
		
		//-- add necessary import statements
		List imports = cu.imports();
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "java", "lang", "reflect", "Field" }));
		imports.add(id);
		
		//-- create method frame: "public static void setProtectedField(final String clazzName, final String fieldName, final Object receiver, final Object value) throws Exception"
		final MethodDeclaration md = ast.newMethodDeclaration();
		td.bodyDeclarations().add(md);
		md.setName(ast.newSimpleName("setField"));
		
		List modifiers = md.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
			
		md.thrownExceptions().add(ast.newSimpleName("Exception"));
		
		List parameters = md.parameters();
		
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("clazzName"));
		parameters.add(svd);

		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("fieldName"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
		svd.setName(ast.newSimpleName("receiver"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
		svd.setName(ast.newSimpleName("value"));
		parameters.add(svd);
		
		//-- create method body
		//		final Class<?> clazz = Class.forName(clazzName);
		//		final Field    f     = clazz.getDeclaredField(fieldName);
		//		f.setAccessible(true);
		//		f.set(receiver, value);
		
		final Block methodBlock = ast.newBlock();
		md.setBody(methodBlock);
		final List methodStmts = methodBlock.statements();
		
		// final Class clazz = Class.forName(clazzName);		
		MethodInvocation init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("forName"));
		init.setExpression(ast.newSimpleName("Class"));
		init.arguments().add(ast.newSimpleName("clazzName"));
		VariableDeclarationFragment  varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("clazz"));
		varDeclFrag.setInitializer(init);
		VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Class")));
		methodStmts.add(varDeclStmt);
		
		// final Field f = clazz.getDeclaredField(fieldName);
		init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("getDeclaredField"));
		init.setExpression(ast.newSimpleName("clazz"));
		init.arguments().add(ast.newSimpleName("fieldName"));
		varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("f"));
		varDeclFrag.setInitializer(init);
		varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Field")));
		methodStmts.add(varDeclStmt);		
		
		// f.setAccessible(true);
		MethodInvocation minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("setAccessible"));
		minv.setExpression(ast.newSimpleName("f"));
		minv.arguments().add(ast.newBooleanLiteral(true));
		methodStmts.add(ast.newExpressionStatement(minv));
		
		// f.set(receiver, value);
		minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("set"));
		minv.setExpression(ast.newSimpleName("f"));
		minv.arguments().add(ast.newSimpleName("receiver"));
		minv.arguments().add(ast.newSimpleName("value"));
		methodStmts.add(ast.newExpressionStatement(minv));
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createGetFieldMethod(final TypeDeclaration td, final CompilationUnit cu, final AST ast)
	{
//		public static void setField(final String clazzName, final String fieldName, final Object receiver, final Object value) throws Exception
//		{
//			final Class<?> clazz = Class.forName(clazzName);
//			final Field    f     = clazz.getDeclaredField(fieldName);
//			f.setAccessible(true);
//			f.set(receiver, value);
//		}
		
		
		//-- add necessary import statements
		List imports = cu.imports();
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "java", "lang", "reflect", "Field" }));
		imports.add(id);
		
		//-- create method frame: "public static Object setProtectedField(final String clazzName, final String fieldName, final Object receiver) throws Exception"
		final MethodDeclaration md = ast.newMethodDeclaration();
		td.bodyDeclarations().add(md);
		md.setName(ast.newSimpleName("getField"));
		
		List modifiers = md.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
			
		md.thrownExceptions().add(ast.newSimpleName("Exception"));
		
		List parameters = md.parameters();
		
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("clazzName"));
		parameters.add(svd);

		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("fieldName"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
		svd.setName(ast.newSimpleName("receiver"));
		parameters.add(svd);
		
		md.setReturnType2(ast.newSimpleType(ast.newSimpleName("Object")));
		
		//-- create method body
		//		final Class<?> clazz = Class.forName(clazzName);
		//		final Field    f     = clazz.getDeclaredField(fieldName);
		//		f.setAccessible(true);
		//		return f.get(receiver);
		
		final Block methodBlock = ast.newBlock();
		md.setBody(methodBlock);
		final List methodStmts = methodBlock.statements();
		
		// final Class clazz = Class.forName(clazzName);		
		MethodInvocation init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("forName"));
		init.setExpression(ast.newSimpleName("Class"));
		init.arguments().add(ast.newSimpleName("clazzName"));
		VariableDeclarationFragment  varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("clazz"));
		varDeclFrag.setInitializer(init);
		VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Class")));
		methodStmts.add(varDeclStmt);
		
		// final Field f = clazz.getDeclaredField(fieldName);
		init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("getDeclaredField"));
		init.setExpression(ast.newSimpleName("clazz"));
		init.arguments().add(ast.newSimpleName("fieldName"));
		varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("f"));
		varDeclFrag.setInitializer(init);
		varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Field")));
		methodStmts.add(varDeclStmt);		
		
		// f.setAccessible(true);
		MethodInvocation minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("setAccessible"));
		minv.setExpression(ast.newSimpleName("f"));
		minv.arguments().add(ast.newBooleanLiteral(true));
		methodStmts.add(ast.newExpressionStatement(minv));
		
		// return f.get(receiver);
		minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("get"));
		minv.setExpression(ast.newSimpleName("f"));
		minv.arguments().add(ast.newSimpleName("receiver"));
		final ReturnStatement returnStmt = ast.newReturnStatement();
		returnStmt.setExpression(minv);
		methodStmts.add(returnStmt);
	}	
	
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createCallMethod(final TypeDeclaration td, final CompilationUnit cu, final AST ast)
	{
//		public static Object callMethod(final String clazzName, final String methodName, final Object receiver, final Object...args) throws Exception
//		{
//			final Class<?> clazz = Class.forName(clazzName);
//			final Method   m     = clazz.getDeclaredMethod(methodName);
//			m.setAccessible(true);
//			return m.invoke(receiver, args);
//		}
		
		
		//-- add necessary import statements
		List imports = cu.imports();
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "java", "lang", "reflect", "Method" }));
		imports.add(id);
		
		//-- create method frame: "public static Object callMethod(final String clazzName, final String methodName, final Object receiver, final Object[] args, Class[] paramTypes) throws Exception"
		final MethodDeclaration md = ast.newMethodDeclaration();
		td.bodyDeclarations().add(md);
		md.setName(ast.newSimpleName("callMethod"));
		
		List modifiers = md.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
			
		md.thrownExceptions().add(ast.newSimpleName("Exception"));
		
		List parameters = md.parameters();
		
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("clazzName"));
		parameters.add(svd);

		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("methodName"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("Object")));
		svd.setName(ast.newSimpleName("receiver"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Object"))));
		svd.setName(ast.newSimpleName("args"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Class"))));
		svd.setName(ast.newSimpleName("paramTypes"));
		parameters.add(svd);
		
		md.setReturnType2(ast.newSimpleType(ast.newSimpleName("Object")));
		
		
		
		//-- create method body
		//		final Class<?> clazz = Class.forName(clazzName);
		//		final Method   m     = clazz.getDeclaredMethod(methodName, paramTypes);
		//		m.setAccessible(true);
		//		return m.invoke(receiver, args);
		
		final Block methodBlock = ast.newBlock();
		md.setBody(methodBlock);
		final List methodStmts = methodBlock.statements();
		
		// final Class clazz = Class.forName(clazzName);		
		MethodInvocation init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("forName"));
		init.setExpression(ast.newSimpleName("Class"));
		init.arguments().add(ast.newSimpleName("clazzName"));
		VariableDeclarationFragment  varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("clazz"));
		varDeclFrag.setInitializer(init);
		VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Class")));
		methodStmts.add(varDeclStmt);
		
		// final Method m = clazz.getDeclaredMethod(methodName);
		init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("getDeclaredMethod"));
		init.setExpression(ast.newSimpleName("clazz"));
		init.arguments().add(ast.newSimpleName("methodName"));
		init.arguments().add(ast.newSimpleName("paramTypes"));
		varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("m"));
		varDeclFrag.setInitializer(init);
		varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Method")));
		methodStmts.add(varDeclStmt);		
		
		// f.setAccessible(true);
		MethodInvocation minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("setAccessible"));
		minv.setExpression(ast.newSimpleName("m"));
		minv.arguments().add(ast.newBooleanLiteral(true));
		methodStmts.add(ast.newExpressionStatement(minv));
		
		// return m.invoke(receiver, args);
		minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("invoke"));
		minv.setExpression(ast.newSimpleName("m"));
		minv.arguments().add(ast.newSimpleName("receiver"));
		minv.arguments().add(ast.newSimpleName("args"));
		final ReturnStatement returnStmt = ast.newReturnStatement();
		returnStmt.setExpression(minv);
		methodStmts.add(returnStmt);
	}	
	
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createNewInstanceMethod(final TypeDeclaration td, final CompilationUnit cu, final AST ast)
	{
//		public static Object newInstance(final String clazzName, final Object receiver, final Object[] args, final Class[] parameterTypes) throws Exception
//		{
//			final Class<?>     clazz = Class.forName(clazzName);
//			final Constructor   c    = clazz.getDeclaredConstructor(parameterTypes);
//			c.setAccessible(true);
//			
//			return c.newInstance(args);
//		}
		
		
		//-- add necessary import statements
		List imports = cu.imports();
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "java", "lang", "reflect", "Constructor" }));
		imports.add(id);
		
		//-- create method frame: "public static Object newInstance(final String clazzName, final Object[] args, Class[] paramTypes) throws Exception"
		final MethodDeclaration md = ast.newMethodDeclaration();
		td.bodyDeclarations().add(md);
		md.setName(ast.newSimpleName("newInstance"));
		
		List modifiers = md.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
			
		md.thrownExceptions().add(ast.newSimpleName("Exception"));
		
		List parameters = md.parameters();
		
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("String")));
		svd.setName(ast.newSimpleName("clazzName"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Object"))));
		svd.setName(ast.newSimpleName("args"));
		parameters.add(svd);
		
		svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Class"))));
		svd.setName(ast.newSimpleName("paramTypes"));
		parameters.add(svd);
		
		md.setReturnType2(ast.newSimpleType(ast.newSimpleName("Object")));
		
		//-- create method body
		//		final Class<?>     clazz = Class.forName(clazzName);
		//		final Constructor   c    = clazz.getDeclaredConstructor(parameterTypes);
		//		c.setAccessible(true);
		//		
		//		return c.newInstance(args);
		
		final Block methodBlock = ast.newBlock();
		md.setBody(methodBlock);
		final List methodStmts = methodBlock.statements();
		
		// final Class clazz = Class.forName(clazzName);		
		MethodInvocation init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("forName"));
		init.setExpression(ast.newSimpleName("Class"));
		init.arguments().add(ast.newSimpleName("clazzName"));
		VariableDeclarationFragment  varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("clazz"));
		varDeclFrag.setInitializer(init);
		VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Class")));
		methodStmts.add(varDeclStmt);
		
		// final Constructor c = clazz.getDeclaredConstructor(parameterTypes);
		init = ast.newMethodInvocation();
		init.setName(ast.newSimpleName("getDeclaredConstructor"));
		init.setExpression(ast.newSimpleName("clazz"));
		init.arguments().add(ast.newSimpleName("paramTypes"));
		varDeclFrag = ast.newVariableDeclarationFragment();
		varDeclFrag.setName(ast.newSimpleName("c"));
		varDeclFrag.setInitializer(init);
		varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
		varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("Constructor")));
		methodStmts.add(varDeclStmt);		
		
		// c.setAccessible(true);
		MethodInvocation minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("setAccessible"));
		minv.setExpression(ast.newSimpleName("c"));
		minv.arguments().add(ast.newBooleanLiteral(true));
		methodStmts.add(ast.newExpressionStatement(minv));
		
		// return c.newInstance(args);
		minv = ast.newMethodInvocation();
		minv.setName(ast.newSimpleName("newInstance"));
		minv.setExpression(ast.newSimpleName("c"));
		minv.arguments().add(ast.newSimpleName("args"));
		final ReturnStatement returnStmt = ast.newReturnStatement();
		returnStmt.setExpression(minv);
		methodStmts.add(returnStmt);
	}	
		
	
	
	
	private int getFieldModifiers(final Class<?> clazz, final String fieldName)
	{
		try
		{
			final Field    f     = clazz.getDeclaredField(fieldName);
			return f.getModifiers();
		}
		catch(final NoSuchFieldException e)
		{
			final Class<?> superClazz = clazz.getSuperclass();
			if(superClazz == null)
			{
				throw new RuntimeException(e);
			}
			else
			{
				return this.getFieldModifiers(superClazz, fieldName);
			}
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	private int getMethodModifiers(final Class<?> clazz, final String methodName, final Class<?>[] paramTypes)
	{
		try
		{
			final Method   m  = clazz.getDeclaredMethod(methodName, paramTypes);
			return m.getModifiers();
		}
		catch(final NoSuchMethodException e)
		{
			final Class<?> superClazz = clazz.getSuperclass();
			if(superClazz == null)
			{
				throw new RuntimeException(e);
			}
			else
			{
				return this.getMethodModifiers(superClazz, methodName, paramTypes);
			}
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	private int getConstructorModifiers(final Class<?> clazz, final Class<?>[] paramTypes)
	{
		try
		{
			final  Constructor<?> c  = clazz.getDeclaredConstructor(paramTypes);
			return c.getModifiers();
		}
		catch(final NoSuchMethodException e)
		{
			final Class<?> superClazz = clazz.getSuperclass();
			if(superClazz == null)
			{
				throw new RuntimeException(e);
			}
			else
			{
				return this.getConstructorModifiers(superClazz, paramTypes);
			}
		}
		catch(final Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	
	
//	private final Class<?> getClassFromType(final org.objectweb.asm.Type type)
//	{
//		
//		if(type.equals(org.objectweb.asm.Type.BOOLEAN_TYPE))
//		{
//			return Boolean.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.BYTE_TYPE))
//		{
//			return Byte.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.CHAR_TYPE))
//		{
//			return Character.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.DOUBLE_TYPE))
//		{
//			return Double.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.FLOAT_TYPE))
//		{
//			return Float.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.INT_TYPE))
//		{
//			return Integer.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.LONG_TYPE))
//		{
//			return Long.TYPE;
//		}
//		else if(type.equals(org.objectweb.asm.Type.SHORT_TYPE))
//		{
//			return Short.TYPE;
//		}
//		else if(type.getSort() == org.objectweb.asm.Type.ARRAY)
//		{
//			final org.objectweb.asm.Type elementType = type.getElementType();
//			
//			if(elementType.equals(org.objectweb.asm.Type.BOOLEAN_TYPE))
//			{
//				return boolean[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.BYTE_TYPE))
//			{
//				return byte[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.CHAR_TYPE))
//			{
//				return char[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.DOUBLE_TYPE))
//			{
//				return double[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.FLOAT_TYPE))
//			{
//				return float[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.INT_TYPE))
//			{
//				return int[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.LONG_TYPE))
//			{
//				return long[].class;
//			}
//			else if(elementType.equals(org.objectweb.asm.Type.SHORT_TYPE))
//			{
//				return short[].class;
//			}
//		}
//		
//		try 
//		{
//			return Class.forName(type.getClassName(), true, StaticTestCluster.classLoader);
//		} 
//		catch (final ClassNotFoundException e) 
//		{
//			throw new RuntimeException(e);
//		}
//	}
	
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
		
//			if(type.endsWith(";") && ! type.startsWith("["))
//			{
//				type = type.replaceFirst("L", "");
//				type = type.replace(";", "");
//			}
			
			if(type.endsWith("[]"))
			{
				type = type.replace("[]", "");
				return Class.forName("[L" + type + ";");
			}
			else
			{
				return Class.forName(type);
			}
		} 
		catch (final ClassNotFoundException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	public static void main(String[] args)
	{
		System.out.println("$2CalculatorPanel$1".replaceFirst("\\$\\d+$", ""));
	}
	
}