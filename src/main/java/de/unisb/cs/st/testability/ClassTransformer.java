package de.unisb.cs.st.testability;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Created by Yanchuan Li Date: 1/14/11 Time: 5:52 AM
 */
public class ClassTransformer {

	private static Logger log = Logger.getLogger(ClassTransformer.class);
	private static String javaLangObject = Type.getInternalName(Object.class);

	private static List<ClassUnit> interfaces = new ArrayList<ClassUnit>();
	private static List<ClassUnit> abstractClasses = new ArrayList<ClassUnit>();
	private static List<ClassUnit> classes = new ArrayList<ClassUnit>();

	public ClassTransformer() {

	}

	/*
	   1. process all basic classes who doesn't implement/extends anything, but simply the subject of java.lang.Object. Here we produce a hashmap.
	   2. process the classes who implements or extends the classes in hashmap until cannot proceed.

	*/
	@SuppressWarnings("unchecked") //access to external lib
	public void findAllMethods() {

		List<String> unprocessedClassSet = ScanProject.getClassSet();
		List<String> processedClassSet = new ArrayList<String>();
		int id = 0;

		//step 1
		log.debug("totally " + unprocessedClassSet.size()
		        + " classes waiting to be processed ...");
		Iterator<String> it = unprocessedClassSet.iterator();
		while (it.hasNext()) {
			String s = it.next();
			ClassUnit cu = ScanProject.classesMap.get(s);
			ClassNode cn = parseToClassNode(cu);
			if (cn.superName.equals(javaLangObject) && cn.interfaces.size() == 0) {
				List<String> fieldList = new ArrayList<String>();
				List<FieldNode> fieldNodes = cn.fields;
				for (FieldNode fn : fieldNodes) {
					if (fn.desc.equals("Z")) {
						fieldList.add(fn.name);
					}
				}
				TransformationHelper.fieldsMap.put(s, fieldList);

				//                log.debug("[step 1] " + cu.getClassname() + " extends " + cn.superName + " with " + cn.interfaces.size() + " interfaces ...");
				Map<String, MethodUnit> methodUnits = new HashMap<String, MethodUnit>();
				for (Object o : cn.methods) {
					MethodNode mn = (MethodNode) o;
					if (!(mn.name.equals("<init>") || mn.name.equals("<clinit>"))) {
						boolean booleanReturnType = false;
						boolean modified = false;
						Type[] types = Type.getArgumentTypes(mn.desc);
						String newdesc = "(";
						for (int i = 0; i < types.length; i++) {
							if (types[i].getDescriptor().equals("Z")) {
								newdesc = newdesc.concat("I");
								modified = true;
								//                        log.debug("parameter [" + i + "] is updated from " + types[i].getDescriptor() + " to I ...");
							} else {
								newdesc = newdesc.concat(types[i].getDescriptor());
								//                        log.debug("parameter [" + i + "] : " + types[i].getDescriptor() + " remains unchanged ...");
							}
						}

						if (mn.desc.endsWith("Z")) {
							newdesc = newdesc.concat(")I");
							modified = true;
							booleanReturnType = true;
						} else {
							newdesc = newdesc.concat(")").concat(Type.getReturnType(mn.desc).getDescriptor());
						}

						if (modified) {
							Map<String, Set<String>> superclasesMap = findAllSuperClassesWithMethods(cu.getClassname());
							String origin = findMethodOrigin(mn.name + "|" + mn.desc,
							                                 superclasesMap);

							String newName = mn.name.concat("_valkyrie_").concat(String.valueOf(id++));
							String newMethodSignature = newName + "|" + newdesc;

							MethodUnit mu = new MethodUnit();
							mu.setBooleanReturnType(booleanReturnType);
							mu.setOriginalName(mn.name + "|" + mn.desc);
							mu.setNewName(newMethodSignature);
							if (origin != null) {
								if (TransformationHelper.checkPackageWithDotName(origin)) {
									mu.setOrigin(MethodOrigin.inheritedFromInterInterface);
								} else {
									mu.setOrigin(MethodOrigin.inheritedFromOutterInterface);
								}
							} else {
								mu.setOrigin(MethodOrigin.internalMethod);
							}
							TransformationHelper.reverseMethodsMap.put(mu.getNewName(),
							                                           mu);
							methodUnits.put(mu.getOriginalName(), mu);
							//                            TransformationHelper.methodsMap.put(methodSignature, newMethodSignature);
							//String methodSignature = cu.getClassname() + "|" + mn.name  + "|" + mn.desc;
							//                            log.debug("  " + methodSignature + "->" + newMethodSignature);
						}
					}
				}
				//                log.debug("[step 1]" + s);

				TransformationHelper.methodsMap.put(s, methodUnits);
				processedClassSet.add(s);
				it.remove();

			}

		}
		//        log.debug(unprocessedClassSet.size() + " complex class left ...");

		//step 2

		while (unprocessedClassSet.size() != 0) {
			Iterator<String> it2 = unprocessedClassSet.iterator();
			while (it2.hasNext()) {
				String s = it2.next();
				//                log.debug("[step 2] processing " + s + " ...");
				ClassUnit cu = ScanProject.classesMap.get(s);
				ClassNode cn = parseToClassNode(cu);

				Map<String, Set<String>> superclassesMap = findAllSuperClassesWithMethods(cu.getClassname());
				boolean couldStartRename = couldStartRename(superclassesMap.keySet(),
				                                            processedClassSet);

				if (couldStartRename) {

					List<String> fieldList = new ArrayList<String>();
					List fieldNodes = cn.fields;
					for (Object o : fieldNodes) {
						FieldNode fn = (FieldNode) o;
						if (fn.desc.equals("Z")) {
							fieldList.add(fn.name);
						}
					}
					TransformationHelper.fieldsMap.put(s, fieldList);

					Map<String, MethodUnit> methodUnits = new HashMap<String, MethodUnit>();
					for (Object o : cn.methods) {
						MethodNode mn = (MethodNode) o;
						if (!(mn.name.equals("<init>") || mn.name.equals("<clinit>"))) {
							boolean modified = false;
							boolean booleanReturnType = false;

							Type[] types = Type.getArgumentTypes(mn.desc);
							String newdesc = "(";
							for (int i = 0; i < types.length; i++) {
								if (types[i].getDescriptor().equals("Z")) {
									newdesc = newdesc.concat("I");
									modified = true;
								} else {
									newdesc = newdesc.concat(types[i].getDescriptor());
								}
							}

							if (mn.desc.endsWith("Z")) {
								newdesc = newdesc.concat(")I");
								modified = true;
								booleanReturnType = true;

							} else {
								newdesc = newdesc.concat(")").concat(Type.getReturnType(mn.desc).getDescriptor());
							}

							if (modified) {
								String originalSignature = mn.name + "|" + mn.desc;
								String origin = findMethodOrigin(originalSignature,
								                                 superclassesMap);

								if (origin != null) {
									if (TransformationHelper.checkPackageWithDotName(origin)) {
										String alreadyRenamedSignature = TransformationHelper.methodsMap.get(origin).get(originalSignature).getNewName();
										String[] arr = alreadyRenamedSignature.split("\\|");
										String newName = arr[0];
										String newDesc = arr[1];
										String newSignature = newName + "|" + newDesc;
										MethodUnit mu = new MethodUnit();
										mu.setOriginalName(originalSignature);
										mu.setNewName(newSignature);
										mu.setOrigin(MethodOrigin.inheritedFromInterInterface);
										mu.setBooleanReturnType(booleanReturnType);
										methodUnits.put(originalSignature, mu);
										TransformationHelper.reverseMethodsMap.put(mu.getNewName(),
										                                           mu);
									} else {
										String newName = mn.name.concat("_valkyrie_").concat(String.valueOf(id++));
										String newMethodSignature = newName + "|"
										        + newdesc;
										MethodUnit mu = new MethodUnit();
										mu.setOriginalName(originalSignature);
										mu.setNewName(newMethodSignature);
										mu.setOrigin(MethodOrigin.inheritedFromOutterInterface);
										mu.setBooleanReturnType(booleanReturnType);
										methodUnits.put(originalSignature, mu);

										TransformationHelper.reverseMethodsMap.put(mu.getNewName(),
										                                           mu);
									}
								} else {
									//                                    log.debug("cannot find origin for method " + mn.name + "|" + mn.desc);
									String newName = mn.name.concat("_valkyrie_").concat(String.valueOf(id++));
									String newMethodSignature = newName + "|" + newdesc;
									MethodUnit mu = new MethodUnit();
									mu.setOriginalName(originalSignature);
									mu.setNewName(newMethodSignature);
									mu.setOrigin(MethodOrigin.internalMethod);
									mu.setBooleanReturnType(booleanReturnType);
									methodUnits.put(originalSignature, mu);
									TransformationHelper.reverseMethodsMap.put(mu.getNewName(),
									                                           mu);
								}

							}
						}
					}

					//                    log.debug("[step 2]" + s);

					TransformationHelper.methodsMap.put(s, methodUnits);
					processedClassSet.add(s);
					it2.remove();
				}
			}

		}
	}

	public void transformAll() {

		for (String s : ScanProject.classesMap.keySet()) {
			transformClass(ScanProject.classesMap.get(s));
		}
	}

	public void transformClass(ClassUnit cu) {

		InputStream fin = null;
		try {
			fin = new FileInputStream(cu.getFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int actual;
			do {
				actual = fin.read(buf);
				if (actual > 0) {
					out.write(buf, 0, actual);
				}
			} while (actual > 0);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ClassNode cn = new ClassNode();
		//        log.debug("class:" + cu.getClassname() + " access:" + cn.access);
		ClassReader treeparser = new ClassReader(out.toByteArray());
		treeparser.accept(cn, ClassReader.EXPAND_FRAMES);

		if (!cn.superName.endsWith(Type.getInternalName(Exception.class))) {
			try {
				//transform all fields;
				ClassNodeTransformer cnt = new ClassNodeTransformer(cn);

				//                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				ClassVisitor cv = new TraceClassVisitor(writer, new PrintWriter(
				        System.out));
				cnt.transform().accept(cv);
				//                ClassVisitor cca = new CheckClassAdapter(cv);
				//                cnt.transform().accept(cca);
				cu.setTransformedBytes(writer.toByteArray());
				//                FileOutputStream fos = new FileOutputStream(cu.getFile());
				//                fos.write(cu.getTransformedBytes());
				log.debug(cu.getClassname() + " updated ...");
				cu.setTransformed(true);
				ScanProject.classesMap.put(cu.getClassname(), cu);

			} catch (Throwable t) {
				log.error("error");
				log.fatal("Transformation of class " + cu.getClassname() + " failed", t);
				StringWriter swriter = new StringWriter();
				t.printStackTrace(new PrintWriter(swriter));
				log.fatal(swriter.getBuffer().toString());
				t.printStackTrace();
				System.exit(0);

			}
		}
	}

	public ClassNode parseToClassNode(ClassUnit cu) {

		InputStream fin = null;
		try {
			fin = new FileInputStream(cu.getFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int actual;
			do {
				actual = fin.read(buf);
				if (actual > 0) {
					out.write(buf, 0, actual);
				}
			} while (actual > 0);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(out.toByteArray());
		cr.accept(cn, ClassReader.SKIP_FRAMES);
		return cn;
	}

	public ClassNode parseToClassNode(String className) {
		ClassNode cn = new ClassNode();
		ClassReader cr = null;
		try {
			cr = new ClassReader(className);
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cn;
	}

	public Map<String, Set<String>> findAllSuperClassesWithMethods(String className) {

		Map<String, Set<String>> interfaces = new HashMap<String, Set<String>>();
		List<String> unprocessedInterfaces = new ArrayList<String>();
		ClassNode cn = parseToClassNode(className);

		String superName = cn.superName.replace('/', '.');
		interfaces.put(superName, new HashSet<String>());
		unprocessedInterfaces.add(superName);
		for (Object objInterface : cn.interfaces) {
			String strInterface = ((String) objInterface).replace('/', '.');
			interfaces.put(strInterface, new HashSet<String>());
			unprocessedInterfaces.add(strInterface);
		}

		List<String> tmpList = new ArrayList<String>();
		while (unprocessedInterfaces.size() != 0) {
			tmpList.clear();
			for (Iterator<String> it = unprocessedInterfaces.iterator(); it.hasNext();) {
				String targetInterface = it.next();
				ClassNode targetInterfaceNode = parseToClassNode(targetInterface);
				if (targetInterfaceNode != null) {
					if (targetInterfaceNode.superName == null) {
						// we found java.lang.Object
						//                        log.debug(targetInterface + " has null supername");
					} else {
						interfaces.put(targetInterface,
						               extractMethodSignatures(targetInterfaceNode));
						String targetInterfaceSuperName = targetInterfaceNode.superName.replace('/',
						                                                                        '.');
						tmpList.add(targetInterfaceSuperName);
						for (Object objInterface : targetInterfaceNode.interfaces) {
							String strInterface = ((String) objInterface).replace('/',
							                                                      '.');
							tmpList.add(strInterface);
						}
					}
				}
				it.remove();
			}
			unprocessedInterfaces.addAll(tmpList);
		}

		//add the java.lang.Object methods
		ClassNode javaLangObject = parseToClassNode("java.lang.Object");
		interfaces.put("java.lang.Object", extractMethodSignatures(javaLangObject));
		return interfaces;
	}

	public Set<String> extractMethodSignatures(ClassNode cn) {
		Set<String> methods = new HashSet<String>();
		for (Object o : cn.methods) {
			MethodNode mn = (MethodNode) o;
			if (!(mn.name.equals("<init>") || mn.name.equals("<clinit>"))) {
				String tempSignature = mn.name + "|" + mn.desc;
				methods.add(tempSignature);
			}
		}
		return methods;
	}

	public String findMethodOrigin(String methodSignature,
	        Map<String, Set<String>> superClasses) {
		String result = null;
		for (String classname : superClasses.keySet()) {
			Set<String> methods = superClasses.get(classname);
			if (methods.contains(methodSignature)) {
				result = classname;
				break;
			}
		}
		return result;
	}

	public static boolean couldStartRename(Set<String> interfaces,
	        List<String> processedClassSet) {
		boolean result = true;
		for (String i : interfaces) {
			if (i.equals(javaLangObject)
			        || !TransformationHelper.checkPackageWithDotName(i)
			        || processedClassSet.contains(i)) {

			} else {
				result = false;
			}
		}
		return result;
	}

	public static Map<String, Set<String>> manageOutsiderClassNodes(
	        List<String> interfaces) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		for (String className : interfaces) {
			ClassNode cn = new ClassNode();
			ClassReader cr = null;
			try {
				cr = new ClassReader(className);
				cr.accept(cn, ClassReader.EXPAND_FRAMES);
				Set<String> methods = new HashSet<String>();
				for (Object o : cn.methods) {
					MethodNode mn = (MethodNode) o;
					methods.add(mn.name + "|" + mn.desc);
				}
				result.put(className, methods);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return result;
	}

	public void listLoadedClasses() {

		final ClassLoader[] loaders = ClassScope.getCallerClassLoaderTree();
		final Class<?>[] classes = ClassScope.getLoadedClasses(loaders);
		for (int c = 0; c < classes.length; ++c) {
			final Class<?> cls = classes[c];
			log.info("[" + cls.getName() + "]:" + "  loaded by ["
			        + cls.getClassLoader().getClass().getName() + "]");

		}
	}
}
