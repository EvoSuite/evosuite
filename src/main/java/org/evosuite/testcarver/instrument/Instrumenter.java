package org.evosuite.testcarver.instrument;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Instrumenter
{
	private int captureId;
	
	public static final String WRAP_NAME_PREFIX = "_sw_prototype_original_";

	private static final Logger LOG = LoggerFactory.getLogger(Instrumenter.class);
	
	public Instrumenter()
	{
		this.captureId = Integer.MIN_VALUE;
	}
	
	
	public void instrument(final String className, final ClassNode cn)
	{
		if(! TransformerUtil.isClassConsideredForInstrumenetation(className))
		{
			LOG.debug("class {} has not been instrumented because its name is on the blacklist", className);
			return;
		}
		
		try
		{
			this.transformClassNode(cn, className);
		}
		catch(final Throwable t)
		{
			LOG.error("An errorn occurred while instrumenting class {} -> returning unmodified version", className, t);
		}	
			
	}

	public byte[] instrument(final String className, final byte[] classfileBuffer) throws IllegalClassFormatException 
	{
		LOG.debug("start instrumenting class {}", className);
		
		
		final ClassReader 		cr 			  = new ClassReader(classfileBuffer);
		final ClassWriter 		cw 			  = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		final ClassNode 		cn 			  = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_DEBUG);
		
		

		if(! TransformerUtil.isClassConsideredForInstrumenetation(className))
		{
			LOG.debug("class {} has not been instrumented because its name is on the blacklist", className);
			return classfileBuffer;
		}
		
		try
		{
			this.transformClassNode(cn, className);

//			final TraceClassVisitor traceVisitor = new TraceClassVisitor(
//					 new PrintWriter(System.out));
			
			
			cn.accept(cw);

			return cw.toByteArray();
		
		}
		catch(final Throwable t)
		{
			LOG.error("An errorn occurred while instrumenting class {} -> returning unmodified version", className, t);
			return classfileBuffer;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addFieldRegistryRegisterCall(final MethodNode methodNode)
	{
		AbstractInsnNode ins = null;
		ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();
		
		int numInvokeSpecials = 0; // number of invokespecial calls before actual constructor call
		
		while(iter.hasNext())
		{
			ins = iter.next();
			
			if(ins instanceof MethodInsnNode)
			{
				MethodInsnNode mins = (MethodInsnNode) ins;
				if(ins.getOpcode()== Opcodes.INVOKESPECIAL)
				{
					if(mins.name.startsWith("<init>"))
					{
						if(numInvokeSpecials == 0)
						{
							break;
						}
						else
						{
							numInvokeSpecials--;
						}
					}
				}
			}
			else if (ins instanceof TypeInsnNode)
			{
				TypeInsnNode typeIns = (TypeInsnNode) ins;
				if(typeIns.getOpcode() == Opcodes.NEW || typeIns.getOpcode() == Opcodes.NEWARRAY)
				{
					numInvokeSpecials++;
				}
			}
		}
		
		
		final InsnList instructions = new InsnList();
		
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
								  "org/evosuite/testcarver/capture/FieldRegistry", 
								  "register", 
								  "(Ljava/lang/Object;)V"));

		
		
		 methodNode.instructions.insert(ins, instructions);
	}
	
	@SuppressWarnings("unchecked")
	public void transformClassNode(ClassNode cn, final String internalClassName)
	{
		if(! TransformerUtil.isClassConsideredForInstrumenetation(internalClassName))
		{
			LOG.debug("class {} has not been instrumented because its name is on the blacklist", internalClassName);
			return;
		}
		
		// consider only public and protected classes which are not interfaces
		if(((cn.access & Opcodes.ACC_PUBLIC) == 0 && (cn.access & Opcodes.ACC_PROTECTED) == 0)|| 
		   (cn.access & Opcodes.ACC_INTERFACE) != 0)
		{
			return;
		}
		
		
		final ArrayList<MethodNode> wrappedMethods = new ArrayList<MethodNode>();
		MethodNode methodNode;
		
		final Iterator<MethodNode> methodIter = cn.methods.iterator();
		while(methodIter.hasNext())
		{
			methodNode = methodIter.next();
			
			if(methodNode.name.equals("<init>"))
			{
				this.addFieldRegistryRegisterCall(methodNode);
			}
			
			// consider only public methods which are not abstract or native
			if( ! TransformerUtil.isPrivate(methodNode.access)  &&
				! TransformerUtil.isAbstract(methodNode.access) &&
				! TransformerUtil.isNative(methodNode.access)   && 
				! methodNode.name.equals("<clinit>"))
			{
				this.instrumentPUTXXXFieldAccesses(cn, internalClassName, methodNode);
				this.instrumentGETXXXFieldAccesses(cn, internalClassName, methodNode);
				
				this.instrumentMethod(cn, internalClassName, methodNode, wrappedMethods);
			}
		}
		
		
		final int numWM = wrappedMethods.size();
		for(int i = 0; i < numWM; i++)
		{
			cn.methods.add(wrappedMethods.get(i));
		}
	}
	
	
	
	private void instrumentGETXXXFieldAccesses(final ClassNode cn, final String internalClassName, final MethodNode methodNode)
	{
		final InsnList instructions = methodNode.instructions;
		
		AbstractInsnNode ins      = null;
		FieldInsnNode    fieldIns = null;
		
		for(int i = 0; i < instructions.size(); i++)
		{
			ins = instructions.get(i);
			if(ins instanceof FieldInsnNode)
			{
				fieldIns = (FieldInsnNode) ins;

				/*
				 * Is field referencing outermost instance? if yes, ignore it
				 * http://tns-www.lcs.mit.edu/manuals/java-1.1.1/guide/innerclasses/spec/innerclasses.doc10.html
				 */
				if(fieldIns.name.endsWith("$0"))
				{
					continue;
				}
	
				
				final int opcode = ins.getOpcode();
				if(opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC ) 
				{
					final InsnList il = new InsnList();
		
					il.add(new LdcInsnNode(this.captureId));
					il.add(new LdcInsnNode(fieldIns.owner));
					il.add(new LdcInsnNode(fieldIns.name));
					il.add(new LdcInsnNode(fieldIns.desc));
					
					il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
							  "org/evosuite/testcarver/capture/FieldRegistry", 
							  "notifyReadAccess", 
							  "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));
					
					i += il.size();
					
					instructions.insert(fieldIns, il);
					this.captureId++;
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void instrumentPUTXXXFieldAccesses(final ClassNode cn, final String internalClassName, final MethodNode methodNode)
	{
		final InsnList instructions = methodNode.instructions;
		
		AbstractInsnNode ins      = null;
		FieldInsnNode    fieldIns = null;
		
		// needed get right receiver var in case of PUTFIELD
		
		for(int i = 0; i < instructions.size(); i++)
		{
			ins = instructions.get(i);
			if(ins instanceof FieldInsnNode)
			{
				fieldIns = (FieldInsnNode) ins;

				/*
				 * Is field referencing outermost instance? if yes, ignore it
				 * http://tns-www.lcs.mit.edu/manuals/java-1.1.1/guide/innerclasses/spec/innerclasses.doc10.html
				 */
				if(fieldIns.name.endsWith("$0"))
				{
					continue;
				}
				
				
				final int opcode = ins.getOpcode();
				if(opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC ) 
				{
					// construction of  
					//   Capturer.capture(final Object receiver, final String methodName, final Object[] methodParams)
					// call
					final InsnList il = new InsnList();
		
					il.add(new LdcInsnNode(this.captureId));
					
					
						il.add(new LdcInsnNode(fieldIns.owner));
						il.add(new LdcInsnNode(fieldIns.name));
						il.add(new LdcInsnNode(fieldIns.desc));
						
						il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
								  "org/evosuite/testcarver/capture/FieldRegistry", 
								  "notifyModification", 
								  "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));
						
						// PUTFIELDRegistry.notifyModification also adds corresponding GETFIELD capture instructions
						this.captureId++;
					i += il.size();
					
					instructions.insert(fieldIns, il);
					this.captureId++;
				}
			}
		}
	}
	

	
	private void instrumentMethod(final ClassNode cn, final String internalClassName, final MethodNode methodNode, final List<MethodNode> wrappedMethods)
	{
		wrappedMethods.add(this.wrapMethod(cn, internalClassName, methodNode));
		this.captureId++;
	}
	
	
	
	private InsnList addCaptureCall(final boolean isStatic, final String internalClassName, final String methodName, final String methodDesc,final Type[] argTypes)
	{
		// construction of  
		//   Capturer.capture(final Object receiver, final String methodName, final Object[] methodParams)
		// call
		final InsnList il = new InsnList();
		
		il.add(new LdcInsnNode(this.captureId));
		
		// --- load receiver argument
		int varIndex;
		if(isStatic)
		{
			// static method invocation
			il.add(new LdcInsnNode(internalClassName));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					  "org/evosuite/testcarver/capture/CaptureUtil", 
					  "loadClass", 
					  "(Ljava/lang/String;)Ljava/lang/Class;"));
			
			
			varIndex = 0;
		}
		else
		{
			// non-static method call
			il.add(new VarInsnNode(Opcodes.ALOAD, 0));
			varIndex = 1;
		}
		
		// --- load method name argument
		
		il.add(new LdcInsnNode(methodName));
		
		// --- load method description argument
		
		il.add(new LdcInsnNode(methodDesc));
		
		// --- load methodParams arguments
		
		// load methodParams length
		// TODO ICONST_1 to ICONST_5 would be more efficient
		il.add(new IntInsnNode(Opcodes.BIPUSH, argTypes.length));
		
		// create array object
		il.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
		
		// fill the array
		
		for(int i = 0; i < argTypes.length; i++)
		{
			il.add(new InsnNode(Opcodes.DUP));
			
			// TODO ICONST_1 to ICONST_5 would be more efficient
			il.add(new IntInsnNode(Opcodes.BIPUSH, i));
			
			//check for primitives
			this.loadAndConvertToObject(il, argTypes[i], varIndex++);
			il.add(new InsnNode(Opcodes.AASTORE));
			
			// long/double take two registers
			if(argTypes[i].equals(Type.LONG_TYPE) || argTypes[i].equals(Type.DOUBLE_TYPE) )
			{
				varIndex++;
			}
		}
		
		// --- construct Capture.capture() call
		
		il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
								  "org/evosuite/testcarver/capture/Capturer", 
								  "capture", 
								  "(ILjava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V"));
		
		return il;
	}
	
	
	
	private void addCaptureEnableStatement(final String className, final MethodNode mn, final InsnList il, final int returnValueVar)
	{
		il.add(new LdcInsnNode(this.captureId));
		
		
		if(TransformerUtil.isStatic(mn.access)) 
		{
			// static method
			
			il.add(new LdcInsnNode(className));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
					  "org/evosuite/testcarver/capture/CaptureUtil", 
					  "loadClass", 
					  "(Ljava/lang/String;)Ljava/lang/Class;"));
		}
		else
		{
			// non-static method
			
			il.add(new VarInsnNode(Opcodes.ALOAD, 0));
		}
		
		
		final Type returnType = Type.getReturnType(mn.desc);
		if(returnType.equals(Type.VOID_TYPE))
		{
			// load return value for VOID methods
			il.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/evosuite/testcarver/capture/CaptureLog", 
									 "RETURN_TYPE_VOID", 
									  Type.getDescriptor(Object.class)));
		}
		else
		{
			// load return value as object
			il.add(new VarInsnNode(Opcodes.ALOAD, returnValueVar));
		}
		
		il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
				  								  "org/evosuite/testcarver/capture/Capturer", 
												  "enable", 
												  "(ILjava/lang/Object;Ljava/lang/Object;)V"));	
	}
	
	
	
	
	/**
	 * 	public int myMethod(int i)
		{
			try
			{
				return _sw_prototype_original_myMethod(i)
			}
			finally
			{
				Capturer.enable();
			}
		}
	
	 * @param methodNode
	 * @param after
	 */
	@SuppressWarnings("unchecked")
	private MethodNode wrapMethod(final ClassNode classNode, final String className, final MethodNode methodNode)
	{
		methodNode.maxStack++;
		
		// create wrapper for original method
		final MethodNode wrappingMethodNode = new MethodNode(methodNode.access, 
															 methodNode.name, 
															 methodNode.desc, 
															 methodNode.signature, 
															 (String[])methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]));
		
		// assign annotations to wrapping method
		wrappingMethodNode.visibleAnnotations          = methodNode.visibleAnnotations;
		wrappingMethodNode.visibleParameterAnnotations = methodNode.visibleParameterAnnotations;
		
		// remove annotations from wrapped method to avoid wrong behavior controlled by annotations
		methodNode.visibleAnnotations          = null;
		methodNode.visibleParameterAnnotations = null;
		
		// rename original method
		methodNode.access =  TransformerUtil.modifyVisibility(methodNode.access, Opcodes.ACC_PRIVATE);
		
		final LabelNode l0 = new LabelNode();
		final LabelNode l1 = new LabelNode();
		final LabelNode l2 = new LabelNode();
		
		final InsnList wInstructions = wrappingMethodNode.instructions;
		
		if("<init>".equals(methodNode.name))
		{
			// wrap a constructor 
			
			methodNode.name   = WRAP_NAME_PREFIX + "init" + WRAP_NAME_PREFIX;
			
			// move call to other constructors to new method
			AbstractInsnNode ins = null;
			ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();
			
			int numInvokeSpecials = 0; // number of invokespecial calls before actual constructor call
			
			while(iter.hasNext())
			{
				ins = iter.next();
				iter.remove();
				wInstructions.add(ins);
				
				if(ins instanceof MethodInsnNode)
				{
					MethodInsnNode mins = (MethodInsnNode) ins;
					if(ins.getOpcode()== Opcodes.INVOKESPECIAL)
					{
						if(mins.name.startsWith("<init>"))
						{
							if(numInvokeSpecials == 0)
							{
								break;
							}
							else
							{
								numInvokeSpecials--;
							}
						}
					}
				}
				else if (ins instanceof TypeInsnNode)
				{
					TypeInsnNode typeIns = (TypeInsnNode) ins;
					if(typeIns.getOpcode() == Opcodes.NEW || typeIns.getOpcode() == Opcodes.NEWARRAY)
					{
						numInvokeSpecials++;
					}
				}
			}
		}
		else
		{
			methodNode.name = WRAP_NAME_PREFIX + methodNode.name;
		}
		
		
		int varReturnValue = 0;
		
		final Type returnType = Type.getReturnType(methodNode.desc);
		
		if(returnType.equals(Type.VOID_TYPE))
		{
			wrappingMethodNode.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l1, "java/lang/Throwable"));

		}
		else
		{
			
			wrappingMethodNode.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l2, "java/lang/Throwable"));
		
			//--- create "Object returnValue = null;"
			
			if( ! TransformerUtil.isStatic(methodNode.access))
			{
				// load "this"
				varReturnValue++;
			}
			
			// consider method arguments to find right variable index
			final Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
			for(int i = 0; i < argTypes.length; i++)
			{
				varReturnValue++;
				
				// long/double take two registers
				if(argTypes[i].equals(Type.LONG_TYPE) || argTypes[i].equals(Type.DOUBLE_TYPE) )
				{
					varReturnValue++;
				}
			}
			
			// push NULL on the stack and initialize variable for return value for it
			wInstructions.add(new InsnNode(Opcodes.ACONST_NULL));
			wInstructions.add(new VarInsnNode(Opcodes.ASTORE, varReturnValue));
		}
		
		
		
		int var = 0;
		
		// --- L0
		wInstructions.add(l0);
		
		wInstructions.add(this.addCaptureCall(TransformerUtil.isStatic(methodNode.access), className, wrappingMethodNode.name, wrappingMethodNode.desc, Type.getArgumentTypes(methodNode.desc)));
		
		
		
		
		// --- construct call to wrapped methode
		
		
		if( ! TransformerUtil.isStatic(methodNode.access))
		{
			// load "this" to call method
			wInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			var++;
		}
		
		
		final Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
		for(int i = 0; i < argTypes.length; i++)
		{
			this.addLoadInsn(wInstructions, argTypes[i], var++);
			
			// long/double take two registers
			if(argTypes[i].equals(Type.LONG_TYPE) || argTypes[i].equals(Type.DOUBLE_TYPE) )
			{
				var++;
			}
		}
		

		
		
		
		
		
		
		
		if(TransformerUtil.isStatic(methodNode.access))
		{
			wInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 
							classNode.name, 
							methodNode.name, 
							methodNode.desc));
		}
		else
		{
			wInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
							 classNode.name, 
							 methodNode.name, 
							 methodNode.desc));
		}



		var++;
		
		if(returnType.equals(Type.VOID_TYPE))
		{
			wInstructions.add(new JumpInsnNode(Opcodes.GOTO, l2));
			
			// --- L1
			
			wInstructions.add(l1);
			
			wInstructions.add(new FrameNode(Opcodes.F_SAME1, 0 ,null, 1, new Object[] {"java/lang/Throwable"}));
			
			wInstructions.add(new VarInsnNode(Opcodes.ASTORE, --var));

			this.addCaptureEnableStatement(className, methodNode, wInstructions, -1);
			
			wInstructions.add(new VarInsnNode(Opcodes.ALOAD, var));
			wInstructions.add(new InsnNode(Opcodes.ATHROW));
			
			// FIXME <--- DUPLICATE CODE
			
			// --- L2
			
			wInstructions.add(l2);
			wInstructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
			
			this.addCaptureEnableStatement(className, methodNode, wInstructions, -1);
			
			wInstructions.add(new InsnNode(Opcodes.RETURN));
			

			
			

		}
		else
		{
			// construct store of the wrapped method call's result
			
			this.addBoxingStmt(wInstructions, returnType);
			
			wInstructions.add(new VarInsnNode(Opcodes.ASTORE, varReturnValue));
			wInstructions.add(new VarInsnNode(Opcodes.ALOAD, varReturnValue));
			
			this.addUnBoxingStmt(wInstructions, returnType);
			
			final int  storeOpcode = returnType.getOpcode(Opcodes.ISTORE);
			wInstructions.add(new VarInsnNode(storeOpcode, ++var)); // might be only var
			
			
			
			// --- L1
			
			wInstructions.add(l1);
			
			this.addCaptureEnableStatement(className, methodNode, wInstructions, varReturnValue);

			
			// construct load of the wrapped method call's result
			int loadOpcode = returnType.getOpcode(Opcodes.ILOAD);
			wInstructions.add(new VarInsnNode(loadOpcode, var));
		
			// construct return of the wrapped method call's result
			this.addReturnInsn(wInstructions, returnType);
			
			//---- L2
			
			wInstructions.add(l2);
			
			wInstructions.add(new FrameNode(Opcodes.F_FULL, 2, new Object[]{ className, this.getInternalName(returnType) }, 1, new Object[] {"java/lang/Throwable"}));
			wInstructions.add(new VarInsnNode(Opcodes.ASTORE, --var));
			
			this.addCaptureEnableStatement(className, methodNode, wInstructions, varReturnValue);
			
			wInstructions.add(new VarInsnNode(Opcodes.ALOAD, var));
			wInstructions.add(new InsnNode(Opcodes.ATHROW));
		}

		return wrappingMethodNode;
	}
	
	
	
	private void addReturnInsn(final InsnList il, final Type type) 
	{
		if (type.equals(Type.BOOLEAN_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.IRETURN));
		} 
		else if (type.equals(Type.CHAR_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.IRETURN));
		} 
		else if (type.equals(Type.BYTE_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.IRETURN));
		} 
		else if (type.equals(Type.SHORT_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.IRETURN));
		} 
		else if (type.equals(Type.INT_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.IRETURN));
		} 
		else if (type.equals(Type.FLOAT_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.FRETURN));
		} 
		else if (type.equals(Type.LONG_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.LRETURN));
		} 
		else if (type.equals(Type.DOUBLE_TYPE)) 
		{
			il.add(new InsnNode(Opcodes.DRETURN));
		} 
		else 
		{
			il.add(new InsnNode(Opcodes.ARETURN));
		}
	}
	
	
	private void addLoadInsn(final InsnList il, final Type type, final int argLocation) 
	{
		if (type.equals(Type.BOOLEAN_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
		} 
		else if (type.equals(Type.CHAR_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
		} 
		else if (type.equals(Type.BYTE_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
		} 
		else if (type.equals(Type.SHORT_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
		} 
		else if (type.equals(Type.INT_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
		} 
		else if (type.equals(Type.FLOAT_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.FLOAD, argLocation));
		} 
		else if (type.equals(Type.LONG_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.LLOAD, argLocation));
		} 
		else if (type.equals(Type.DOUBLE_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.DLOAD, argLocation));
		} 
		else 
		{
			il.add(new VarInsnNode(Opcodes.ALOAD, argLocation));
		}
	}
	
	
	private String getInternalName(final Type type) 
	{
		if (type.equals(Type.BOOLEAN_TYPE)) 
		{
			return "java/lang/Boolean";
		} 
		else if (type.equals(Type.CHAR_TYPE)) 
		{
			return "java/lang/Character";
		} 
		else if (type.equals(Type.BYTE_TYPE)) 
		{
			return "java/lang/Byte";
		} 
		else if (type.equals(Type.SHORT_TYPE)) 
		{
			return "java/lang/Short";
		} 
		else if (type.equals(Type.INT_TYPE)) 
		{
			return "java/lang/Integer";
		} 
		else if (type.equals(Type.FLOAT_TYPE)) 
		{
			return "java/lang/Float";
		} 
		else if (type.equals(Type.LONG_TYPE)) 
		{
			return "java/lang/Long";
		} 
		else if (type.equals(Type.DOUBLE_TYPE)) 
		{
			return "java/lang/Double";
		} 
		else 
		{
			return type.getInternalName();
		}
	}
	
	
	private void loadAndConvertToObject(final InsnList il, final Type type, final int argLocation) 
	{
		if (type.equals(Type.BOOLEAN_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean",
					"valueOf", "(Z)Ljava/lang/Boolean;"));
		} 
		else if (type.equals(Type.CHAR_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character",
					"valueOf", "(C)Ljava/lang/Character;"));
		} 
		else if (type.equals(Type.BYTE_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte",
					"valueOf", "(B)Ljava/lang/Byte;"));
		} 
		else if (type.equals(Type.SHORT_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short",
					"valueOf", "(S)Ljava/lang/Short;"));
		} 
		else if (type.equals(Type.INT_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.ILOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer",
					"valueOf", "(I)Ljava/lang/Integer;"));
		} 
		else if (type.equals(Type.FLOAT_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.FLOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float",
					"valueOf", "(F)Ljava/lang/Float;"));
		} 
		else if (type.equals(Type.LONG_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.LLOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long",
					"valueOf", "(J)Ljava/lang/Long;"));
		} 
		else if (type.equals(Type.DOUBLE_TYPE)) 
		{
			il.add(new VarInsnNode(Opcodes.DLOAD, argLocation));
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double",
					"valueOf", "(D)Ljava/lang/Double;"));
		} 
		else 
		{
			il.add(new VarInsnNode(Opcodes.ALOAD, argLocation));
		}
	}
	
	
	
	private void addBoxingStmt(final InsnList il, final Type type) 
	{
		if (type.equals(Type.BOOLEAN_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean",
					"valueOf", "(Z)Ljava/lang/Boolean;"));
		} 
		else if (type.equals(Type.CHAR_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character",
					"valueOf", "(C)Ljava/lang/Character;"));
		} 
		else if (type.equals(Type.BYTE_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte",
					"valueOf", "(B)Ljava/lang/Byte;"));
		} 
		else if (type.equals(Type.SHORT_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short",
					"valueOf", "(S)Ljava/lang/Short;"));
		} 
		else if (type.equals(Type.INT_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer",
					"valueOf", "(I)Ljava/lang/Integer;"));
		} 
		else if (type.equals(Type.FLOAT_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float",
					"valueOf", "(F)Ljava/lang/Float;"));
		} 
		else if (type.equals(Type.LONG_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long",
					"valueOf", "(J)Ljava/lang/Long;"));
		} 
		else if (type.equals(Type.DOUBLE_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double",
					"valueOf", "(D)Ljava/lang/Double;"));
		} 
	}
	
	
	private void addUnBoxingStmt(final InsnList il, final Type type) 
	{
		if (type.equals(Type.BOOLEAN_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean",
					"booleanValue", "()Z"));
		} 
		else if (type.equals(Type.CHAR_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character",
					"charValue", "()C"));
		} 
		else if (type.equals(Type.BYTE_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Byte",
					"byteValue", "()B"));
		} 
		else if (type.equals(Type.SHORT_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Short",
					"shortValue", "()S"));
		} 
		else if (type.equals(Type.INT_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer",
					"intValue", "()I"));
		} 
		else if (type.equals(Type.FLOAT_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float",
					"floatValue", "()F"));
		} 
		else if (type.equals(Type.LONG_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long",
					"longValue", "()J"));
		} 
		else if (type.equals(Type.DOUBLE_TYPE)) 
		{
			il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double",
					"doubleValue", "()D"));
		} 
	}
	
}
