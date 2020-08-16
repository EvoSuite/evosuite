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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
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
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.CaptureUtil;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class JUnitCodeGenerator implements ICodeGenerator<CompilationUnit>
{
	private static final Logger logger = LoggerFactory.getLogger(JUnitCodeGenerator.class);
	
	//--- source generation
	private final TIntObjectHashMap<String>   oidToVarMapping;
	private final TIntObjectHashMap<Class<?>> oidToTypeMapping;
	
	private int         varCounter;
	private TIntHashSet failedRecords;
	
	private boolean postprocessing;
	
	
	private boolean isNewInstanceMethodNeeded;
	private boolean isCallMethodMethodNeeded;
	private boolean isSetFieldMethodNeeded;
	private boolean isGetFieldMethodNeeded;
	private boolean isXStreamNeeded;
	
	
	private String cuName;
	private String packageName;
	
	public JUnitCodeGenerator(final String cuName, final String packageName)
	{
		if(cuName == null || cuName.isEmpty())
		{
			throw new IllegalArgumentException("Illegal compilation unit name: " + cuName);
		}
		
		if(packageName == null)
		{
			throw new NullPointerException("package name must not be null");
		}
		
		this.packageName = packageName;
		this.cuName = cuName;
		
		this.oidToVarMapping  = new TIntObjectHashMap<String>();
		this.oidToTypeMapping = new TIntObjectHashMap<Class<?>>();
		this.failedRecords    = new TIntHashSet();
		
		this.init();
	}

	
	private void init()
	{
		this.oidToVarMapping.clear();
		this.oidToTypeMapping.clear();
		this.failedRecords.clear();
		
		this.varCounter       = 0;
		
		this.postprocessing = false;
		
		this.isNewInstanceMethodNeeded = false;
		this.isCallMethodMethodNeeded  = false;
		this.isSetFieldMethodNeeded    = false;
		this.isGetFieldMethodNeeded    = false;
		this.isXStreamNeeded           = false;
	}
	
	public void enablePostProcessingCodeGeneration()
	{
		this.postprocessing = true;
	}
	
	public void disablePostProcessingCodeGeneration(final TIntArrayList failedRecords)
	{
		this.postprocessing = false;
		this.failedRecords.addAll(failedRecords);
	}
	
	
	

	private AST ast;
	private CompilationUnit cu;
	private Block methodBlock;
	TypeDeclaration td;
	
	@SuppressWarnings("unchecked")
	@Override
	public void before(CaptureLog log) {
		ast = AST.newAST(AST.JLS3);
		cu = ast.newCompilationUnit();

		// package declaration
		final PackageDeclaration p1 = ast.newPackageDeclaration();
		p1.setName(ast.newName(packageName.split("\\.")));
		cu.setPackage(p1);

		// import specifications
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "org", "junit", "Test" }));
		cu.imports().add(id);
		
		// class declaration
		td = ast.newTypeDeclaration();
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
		
		methodBlock = ast.newBlock();
		md.setBody(methodBlock);
		
	}





	@SuppressWarnings("unchecked")
	@Override
	public void after(CaptureLog log) 
	{
		if(this.isXStreamNeeded)
		{
			ImportDeclaration id = ast.newImportDeclaration();
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
	}



	@Override
	public CompilationUnit getCode() {
		return this.cu;
	}


	@Override
	public void clear() 
	{
		this.init();
	}

	private String createNewVarName(final int oid, final String typeName)
	{
		return this.createNewVarName(oid, typeName, false);
	}
	
	private String createNewVarName(final int oid, final String typeName, final boolean asObject)
	{
		if(this.oidToVarMapping.containsKey(oid))
		{
			throw new IllegalStateException("There is already an oid to var mapping for oid " + oid);
		}
		
//		try
//		{
			if(asObject)
			{
				this.oidToTypeMapping.put(oid, Object.class);
			}
			else
			{
				this.oidToTypeMapping.put(oid, getClassForName(typeName));
			}
//		}
//		catch(final ClassNotFoundException e)
//		{
//			throw new IllegalArgumentException(e);
//		}
//		
		
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
			extractedType        = extractedType.replace('$', '.');
			
			return ast.newArrayType(this.createAstType(extractedType, ast), arrayDim);
		}
		else
		{
			// --- primitive type array
			
			if(type.contains("[I")) // int[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT), arrayDim);
			}
			else if(type.contains("[B")) // byte[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE), arrayDim);
			}
			else if(type.contains("[C")) // char[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.CHAR), arrayDim);

			}
			else if(type.contains("[D")) // double[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.DOUBLE), arrayDim);
			}
			else if(type.contains("[Z")) // boolean[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BOOLEAN), arrayDim);
			}
			else if(type.contains("[F")) // float[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.FLOAT), arrayDim);
			}
			else if(type.contains("[S")) // short[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.SHORT), arrayDim);
			}
			else if(type.contains("[J")) // long[]
			{
				return ast.newArrayType(ast.newPrimitiveType(PrimitiveType.LONG), arrayDim);
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
				if(fragments[0].contains("$"))
				{
					return ast.newSimpleType(ast.newName(fragments[0].replace('$', '.')));
				}
				else
				{
					return ast.newSimpleType(ast.newSimpleName(fragments[0]));
				}
			}
			
			if(type.startsWith("java.lang"))
			{
				return ast.newSimpleType(ast.newSimpleName(fragments[2]));
			}
			
			final String[] pkgArray = new String[fragments.length - 1];
			System.arraycopy(fragments, 0, pkgArray, 0, pkgArray.length);
			final SimpleType pkgType = ast.newSimpleType(ast.newName(pkgArray));
			
			final String clazzName = fragments[fragments.length - 1];
			if(clazzName.contains("$"))
			{
				final String[] clazzSplit = clazzName.split("\\$");
				
				final String[] newPkgType = new String[pkgArray.length + clazzSplit.length - 1];
				System.arraycopy(pkgArray,   0, newPkgType, 0,               pkgArray.length);
				System.arraycopy(clazzSplit, 0, newPkgType, pkgArray.length, clazzSplit.length - 1);

				if(clazzName.endsWith("[]"))
				{
					final QualifiedType t = ast.newQualifiedType(  ast.newSimpleType(ast.newName(newPkgType)), ast.newSimpleName(clazzSplit[clazzSplit.length - 1].replace("[]", "")));
					return ast.newArrayType(t);
				}
				else
				{
					return  ast.newQualifiedType(  ast.newSimpleType(ast.newName(newPkgType)), ast.newSimpleName(clazzSplit[clazzSplit.length - 1]));
				}
			}
			else
			{
		    	final String[] clazzSplit = clazzName.split("\\$");
				
				final String[] newPkgType = new String[pkgArray.length + clazzSplit.length - 1];
				System.arraycopy(pkgArray,   0, newPkgType, 0,               pkgArray.length);
				System.arraycopy(clazzSplit, 0, newPkgType, pkgArray.length, clazzSplit.length - 1);

				if(clazzName.endsWith("[]"))
				{
					final QualifiedType t = ast.newQualifiedType(  ast.newSimpleType(ast.newName(newPkgType)), ast.newSimpleName(clazzSplit[clazzSplit.length - 1].replace("[]", "")));
					return ast.newArrayType(t);
				}
				else
				{
					return  ast.newQualifiedType(  ast.newSimpleType(ast.newName(newPkgType)), ast.newSimpleName(clazzSplit[clazzSplit.length - 1]));
				}
			}
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
		
		m.setExpression(ast.newName(new String[]{"org", "evosuite","testcarver","codegen", "PostProcessor"}));
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
	
	
	
	
	
	/* (non-Javadoc)
	 * @see org.evosuite.testcarver.codegen.ICodeGenerator#createMethodCallStmt(org.evosuite.testcarver.capture.CaptureLog, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void createMethodCallStmt(CaptureLog log, int logRecNo) 
	{
		PostProcessor.notifyRecentlyProcessedLogRecNo(logRecNo);
		
		// assumption: all necessary statements are created and there is one variable for reach referenced object
		final int      oid        = log.objectIds.get(logRecNo);
	    Object[] methodArgs = log.params.get(logRecNo);
		final String   methodName = log.methodNames.get(logRecNo);
		
		
		final  String                   methodDesc       = log.descList.get(logRecNo);
		org.objectweb.asm.Type[] methodParamTypes = org.objectweb.asm.Type.getArgumentTypes(methodDesc);
		
		Class<?>[] methodParamTypeClasses = new Class[methodParamTypes.length];
		for(int i = 0; i < methodParamTypes.length; i++)
		{
			methodParamTypeClasses[i] = getClassForName(methodParamTypes[i].getClassName());
		}
		
		final String  typeName  = log.oidClassNames.get(log.oidRecMapping.get(oid));
		Class<?> type = getClassForName(typeName);
		
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
			
			//FIXME this does not make any sense
			try
			{
				this.getConstructorModifiers(type,methodParamTypeClasses);
			}
			catch(Exception e)
			{
				logger.error(""+e,e);
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
								
				final int recNo         = log.oidRecMapping.get(oid);
				final int dependencyOID = log.dependencies.getQuick(recNo);
				
				if(dependencyOID == CaptureLog.NO_DEPENDENCY)
				{
					ci.setType(this.createAstType(typeName, ast));
				}
				else
				{
//					final String varTypeName = oidToVarMapping.get(dependencyOID) + "." + typeName.substring(typeName.indexOf('$') + 1);
//					ci.setType(this.createAstType(varTypeName, ast));
					/*
					 * e.g.
					 * OuterClass.InnerClass innerObject = outerObject.new InnerClass();
					 */
					ci.setType(this.createAstType(typeName.substring(typeName.indexOf('$') + 1), ast));
					ci.setExpression(ast.newSimpleName(oidToVarMapping.get(dependencyOID)));
										
					final int index = Arrays.binarySearch(methodArgs, dependencyOID);
					
					if(index > -1)
					{
						logger.debug(varName + " xxxx3 " + index);
						
						final Object[] newArgs = new Object[methodArgs.length - 1];
						System.arraycopy(methodArgs, 0,         newArgs, 0,     index);
						System.arraycopy(methodArgs, index + 1, newArgs, index, methodArgs.length - index - 1);
						methodArgs = newArgs;
						
						
						final Class<?>[] newParamTypeClasses = new Class<?>[methodParamTypeClasses.length - 1]; 
						System.arraycopy(methodParamTypeClasses, 0,         newParamTypeClasses, 0,     index);
						System.arraycopy(methodParamTypeClasses, index + 1, newParamTypeClasses, index, methodParamTypeClasses.length - index - 1);
						methodParamTypeClasses = newParamTypeClasses;
						
						
						final org.objectweb.asm.Type[] newParamTypes = new org.objectweb.asm.Type[methodParamTypes.length - 1]; 
						System.arraycopy(methodParamTypes, 0,         newParamTypes, 0,     index);
						System.arraycopy(methodParamTypes, index + 1, newParamTypes, index, methodParamTypes.length - index - 1);
						methodParamTypes = newParamTypes;
					}
				}
				
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
			
			final String desc       = log.descList.get(logRecNo);
			final String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
			
			
			final Object returnValue = log.returnValues.get(logRecNo);
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

			
			final String  varName                  = this.oidToVarMapping.get(oid);
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
				 
				if( log.isStaticCallList.get(logRecNo)) 
				{
					// can only happen, if this is a static method call (because constructor statement has been reported)
					final String tmpType = log.oidClassNames.get(log.oidRecMapping.get(oid));
					mi.setExpression(ast.newName(tmpType.split("\\.")));
				}
				else
				{
					try
					{
						mi.setExpression(ast.newSimpleName(varName));	
					}
					catch(final IllegalArgumentException ex)
					{

						String msg = "";

						msg += "--recno-- " + logRecNo + "\n";
						msg += "--oid-- " + oid + "\n";
						msg += "--method-- " + methodName + "\n";
						msg += "--varName-- " + varName + "\n";
						msg += "--oidToVarMap-- " +  this.oidToVarMapping + "\n";										
						msg += (log) + "\n";
						
						logger.error(msg);
						throw new RuntimeException(msg);
					}
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
							
					// TODO: Warten was Florian und Gordon dazu sagen. Siehe Mail 04.08.2012
					if(argType == null)
					{
						logger.error("Call within constructor needs instance of enclosing object as parameter -> ignored: " + arg);
						methodBlock.statements().remove(methodBlock.statements().size() - 1);
						return;
					}
					
					final CastExpression cast = ast.newCastExpression();
					
					if(methodParamType.isPrimitive())
					{
						// cast to ensure that right method is called
						// --> see doSth(int) and doSth(Integer)
						
						if(methodParamType.equals(boolean.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
						}
						else if(methodParamType.equals(byte.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.BYTE));
						}
						else if(methodParamType.equals(char.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.CHAR));
						}
						else if(methodParamType.equals(double.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
						}
						else if(methodParamType.equals(float.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.FLOAT));
						}
						else if(methodParamType.equals(int.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.INT));
						}
						else if(methodParamType.equals(long.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.LONG));
						}
						else if(methodParamType.equals(short.class))
						{
							cast.setType(ast.newPrimitiveType(PrimitiveType.SHORT));
						}
						else
						{
							throw new RuntimeException("unknown primitive type: " + methodParamType);
						}

					}
					else
					{
						// we need an up-cast
						if(methodParamType.getName().contains("."))
						{
							cast.setType(this.createAstType(methodParamType.getName(), ast));
						}
						else
						{
							cast.setType(createAstType(methodParamType.getName(), ast));
						}
					}
					
					cast.setExpression(ast.newSimpleName(this.oidToVarMapping.get(arg)));
					arguments.add(cast);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void createPlainInitStmt(CaptureLog log, int logRecNo) 
	{
		PostProcessor.notifyRecentlyProcessedLogRecNo(logRecNo);
		
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int    oid   = log.objectIds.get(logRecNo);
		
		if(this.oidToVarMapping.containsKey(oid))
		{
			// TODO this might happen because of Integer.valueOf o.ä. . Is this approach ok?
			return;
		}
		
		final String type  = log.oidClassNames.get(log.oidRecMapping.get(oid));
		final Object value = log.params.get(logRecNo)[0];
		
		
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
	@Override
	public void createUnobservedInitStmt(CaptureLog log, int logRecNo) 
	{
		PostProcessor.notifyRecentlyProcessedLogRecNo(logRecNo);
		
		// NOTE: PLAIN INIT: has always one non-null param
		// TODO: use primitives
		final int    oid     = log.objectIds.get(logRecNo);
//		final String type    = log.oidClassNames.get(log.oidRecMapping.get(oid));
		final Object value   = log.params.get(logRecNo)[0];
		this.isXStreamNeeded = true;
		
		final VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
		// handling because there must always be a new instantiation statement for pseudo inits
		this.oidToVarMapping.remove(oid);
		vd.setName(ast.newSimpleName(this.createNewVarName(oid, log.oidClassNames.get(log.oidRecMapping.get(oid)), true)));
		
		final MethodInvocation methodInvocation = ast.newMethodInvocation();
		final Name name = ast.newSimpleName("XSTREAM");
		methodInvocation.setExpression(name);
		methodInvocation.setName(ast.newSimpleName("fromXML")); 
		
		final StringLiteral xmlParam = ast.newStringLiteral();
		xmlParam.setLiteralValue((String) value);
		methodInvocation.arguments().add(xmlParam);
		
//		final CastExpression castExpr = ast.newCastExpression();
//		castExpr.setType(this.createAstType(type, ast));
//		castExpr.setExpression(methodInvocation);
		
//		vd.setInitializer(castExpr);
		vd.setInitializer(methodInvocation);
		
		final VariableDeclarationStatement vs = ast.newVariableDeclarationStatement(vd);
		vs.setType(this.createAstType("Object", ast));
				
		methodBlock.statements().add(vs);
	}	
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void createFieldWriteAccessStmt(CaptureLog log, int logRecNo) {

		// assumption: all necessary statements are created and there is one variable for reach referenced object
		
		final Object[] methodArgs = log.params.get(logRecNo);
		final String   methodName = log.methodNames.get(logRecNo);
		final int      oid        = log.objectIds.get(logRecNo);
		final int      captureId  = log.captureIds.get(logRecNo);
		
		final String  fieldName = log.namesOfAccessedFields.get(captureId);
		final String  typeName  = log.oidClassNames.get(log.oidRecMapping.get(oid));
		
		final Class<?> type                     = getClassForName(typeName);
		final int      fieldTypeModifiers       = this.getFieldModifiers(type, fieldName);
		final boolean  isPublic                 = java.lang.reflect.Modifier.isPublic(fieldTypeModifiers);
		final boolean  haveSamePackage          = type.getPackage().getName().equals(packageName); // TODO might be nicer...
		final boolean  isReflectionAccessNeeded = ! isPublic && ! haveSamePackage;
		
		
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
				fa.setExpression(ast.newName(typeName));
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
				final String   fieldDesc = log.descList.get(logRecNo);
				final Class<?> fieldType = CaptureUtil.getClassFromDesc(fieldDesc);
				
				if(fieldType.isAssignableFrom(argType) || fieldType.isPrimitive())
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
	@Override
	public void createFieldReadAccessStmt(CaptureLog log, int logRecNo) 
	{
		final String   methodName = log.methodNames.get(logRecNo);
		final int      oid        = log.objectIds.get(logRecNo);
		final int      captureId  = log.captureIds.get(logRecNo);
		
		String returnVarName = null;
		
		final Object returnValue = log.returnValues.get(logRecNo);
		if(! CaptureLog.RETURN_TYPE_VOID.equals(returnValue))
		{
			Integer 	  returnValueOID  = (Integer) returnValue;
			final String  descriptor 	  = log.descList.get(logRecNo);
			final String  fieldTypeName   = org.objectweb.asm.Type.getType(descriptor).getClassName();
			final String  typeName        = log.oidClassNames.get(log.oidRecMapping.get(oid));
			final String  fieldName       = log.namesOfAccessedFields.get(captureId);
			final String  receiverVarName = this.oidToVarMapping.get(oid);
			
			final Class<?> type = getClassForName(typeName);
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
					final String classType = log.oidClassNames.get(log.oidRecMapping.get(oid));
					fa.setExpression(ast.newName(classType.replace('$', '.').split("\\.")));
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
	

	@Override
	public void createArrayInitStmt(final CaptureLog log, final int logRecNo) {
		final int  oid  = log.objectIds.get(logRecNo);
		
		final Object[] params      = log.params.get(logRecNo);
		final String   arrTypeName = log.oidClassNames.get(log.oidRecMapping.get(oid));
		final Class<?> arrType     = getClassForName(arrTypeName);
		
		// --- create array instance creation e.g. int[] var = new int[10];
		
		final ArrayType     arrAstType      = (ArrayType) createAstArrayType(arrTypeName, ast);
		final ArrayCreation arrCreationExpr = ast.newArrayCreation();
		arrCreationExpr.setType(arrAstType);
		arrCreationExpr.dimensions().add(ast.newNumberLiteral(String.valueOf(params.length)));
			
		final String 					  arrVarName = this.createNewVarName(oid, arrTypeName);
		final VariableDeclarationFragment vd         = ast.newVariableDeclarationFragment();
		final SimpleName arrVarNameExpr = ast.newSimpleName(arrVarName); 
		vd.setName(arrVarNameExpr);	
		vd.setInitializer(arrCreationExpr);
		
		final VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(vd);
		varDeclStmt.setType(this.createAstType(arrTypeName, ast));
		
		methodBlock.statements().add(varDeclStmt);	
		
		// create array access statements var[0] = var1;
		Integer paramOID;
		Assignment assign;
		ArrayAccess arrAccessExpr;
		
		for(int i = 0; i < params.length; i++)
		{
			assign       = ast.newAssignment();
			arrAccessExpr = ast.newArrayAccess();
			arrAccessExpr.setIndex(ast.newNumberLiteral(String.valueOf(i)));
			arrAccessExpr.setArray(arrVarNameExpr);
			
			assign.setLeftHandSide(arrAccessExpr);
			
			paramOID = (Integer) params[i];
			if(paramOID == null)
			{
			   assign.setRightHandSide(ast.newNullLiteral());
			}
			else
			{
				assign.setRightHandSide(ast.newSimpleName(this.oidToVarMapping.get(paramOID)));
			}
			
			methodBlock.statements().add(assign);	
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public void createCollectionInitStmt(final CaptureLog log, final int logRecNo) 
	{
		final int      oid          = log.objectIds.get(logRecNo);
		final Object[] params       = log.params.get(logRecNo);
		String         collTypeName = log.oidClassNames.get(log.oidRecMapping.get(oid));
		final Class<?> collType     = getClassForName(collTypeName);

		final String varName;
		
		// -- determine if an alternative collection must be used for code generation
		final boolean isPrivate = java.lang.reflect.Modifier.isPrivate(collType.getModifiers());
		if(isPrivate || ! hasDefaultConstructor(collType))
		{
			if(Set.class.isAssignableFrom(collType))
			{
				collTypeName = HashSet.class.getName();
			}
			else if (List.class.isAssignableFrom(collType))
			{
				collTypeName = ArrayList.class.getName();
			}
			else if(Queue.class.isAssignableFrom(collType))
			{
				collTypeName = ArrayDeque.class.getName();
			}
			else
			{
				throw new RuntimeException("Collection " + collType + " is not supported");
			}
		}

		// -- create code for instantiating collection
		varName = this.createNewVarName(oid, collTypeName);
		
		final VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
		final SimpleName varNameExpr = ast.newSimpleName(varName); 
		vd.setName(varNameExpr);	
		
		final ClassInstanceCreation ci = ast.newClassInstanceCreation();
 	    ci.setType(this.createAstType(collTypeName, ast));
 	    vd.setInitializer(ci);
		
		final VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(vd);
		stmt.setType(this.createAstType(collTypeName, ast));
		
		methodBlock.statements().add(stmt);	

		// --- create code for filling the collection
		Integer paramOID;
		MethodInvocation mi;
		for(int i = 0; i < params.length; i++)
		{
			mi = ast.newMethodInvocation();
			mi.setName(ast.newSimpleName("add"));
			
			paramOID = (Integer) params[i];
			if(paramOID == null)
			{
				mi.arguments().add(ast.newNullLiteral());
			}
			else
			{
				mi.arguments().add(ast.newSimpleName(this.oidToVarMapping.get(paramOID)));
			}
			
			methodBlock.statements().add(mi);	
		}
	}

	private boolean hasDefaultConstructor(final Class<?> clazz)
	{
		for(final Constructor<?> c : clazz.getConstructors())
		{
			if(c.getParameterTypes().length == 0)
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void createMapInitStmt(final CaptureLog log, final int logRecNo) {
		final int      oid          = log.objectIds.get(logRecNo);
		final Object[] params       = log.params.get(logRecNo);
		String         collTypeName = log.oidClassNames.get(log.oidRecMapping.get(oid));
		final Class<?> collType     = getClassForName(collTypeName);


		final String varName;
		
		// -- determine if an alternative collection must be used for code generation
		final boolean isPrivate = java.lang.reflect.Modifier.isPrivate(collType.getModifiers());
		if(isPrivate || ! hasDefaultConstructor(collType))
		{
			collTypeName = HashMap.class.getName();
		}

		// -- create code for instantiating collection
		varName = this.createNewVarName(oid, collTypeName);
		
		final VariableDeclarationFragment vd = ast.newVariableDeclarationFragment();
		final SimpleName varNameExpr = ast.newSimpleName(varName); 
		vd.setName(varNameExpr);	
		
		final ClassInstanceCreation ci = ast.newClassInstanceCreation();
 	    ci.setType(this.createAstType(collTypeName, ast));
 	    vd.setInitializer(ci);
		
		final VariableDeclarationStatement stmt = ast.newVariableDeclarationStatement(vd);
		stmt.setType(this.createAstType(collTypeName, ast));
		
		methodBlock.statements().add(stmt);	

		// --- create code for filling the collection
		Integer valueOID;
		Integer keyOID;
		
		MethodInvocation mi;
		for(int i = 0; i + 1< params.length; i+=2)
		{
			mi = ast.newMethodInvocation();
			mi.setName(ast.newSimpleName("put"));
			
			keyOID = (Integer) params[i];
		    mi.arguments().add(ast.newSimpleName(this.oidToVarMapping.get(keyOID)));
			
			valueOID = (Integer) params[i + 1];
			if(valueOID == null)
			{
				mi.arguments().add(ast.newNullLiteral());
			}
			else
			{
				mi.arguments().add(ast.newSimpleName(this.oidToVarMapping.get(valueOID)));
			}
			
			methodBlock.statements().add(mi);	
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createSetFieldMethod(final TypeDeclaration td, final CompilationUnit cu, final AST ast)
	{
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
				final Class<?> baseClass = getClassForName(type);
				if(baseClass.isPrimitive())
				{
					if(int.class.equals(baseClass))
					{
						return int[].class;
					}
					else if(byte.class.equals(baseClass))
					{
						return byte[].class;
					}
					else if(char.class.equals(baseClass))
					{
						return char[].class;
					}
					else if(double.class.equals(baseClass))
					{
						return double[].class;
					}
					else if(boolean.class.equals(baseClass))
					{
						return boolean[].class;
					}
					else if(float.class.equals(baseClass))
					{
						return float[].class;
					}
					else if(short.class.equals(baseClass))
					{
						return short[].class;
					}
					else
					{
						return long[].class;
					}
				}
				else if(baseClass.isArray()) 
				{
					return Class.forName("[" + type);
				}
				else
				{
					return Class.forName("[L" + type + ";");
				}
				
				
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
	
	

}