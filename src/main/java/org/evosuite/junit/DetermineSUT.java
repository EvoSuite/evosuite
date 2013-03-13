/**
 * 
 */
package org.evosuite.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.javaagent.BytecodeInstrumentation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * For a given JUnit test class, determine which class it seems to be testing.
 * 
 * @author Gordon Fraser
 * 
 */
public class DetermineSUT {

	private String targetName = "";

	private class TargetClassSorter implements Comparator<String> {
		private final String targetClass;

		public TargetClassSorter(String target) {
			this.targetClass = target;
		}

		@Override
		public int compare(String arg0, String arg1) {
			double distance1 = StringUtils.getLevenshteinDistance(targetClass, arg0);
			double distance2 = StringUtils.getLevenshteinDistance(targetClass, arg1);
			return Double.compare(distance1, distance2);
		}
	}

	public String getSUTName(String fullyQualifiedTargetClass) {
		this.targetName = fullyQualifiedTargetClass;
		Set<String> candidateClasses = new HashSet<String>();
		try {
			candidateClasses.addAll(determineCalledClasses(fullyQualifiedTargetClass));
		} catch (ClassNotFoundException e) {
			// Ignore, the set will be empty?
			System.err.println("Class not found: " + e);
			return "";
		}

		List<String> sortedNames = new ArrayList<String>(candidateClasses);
		Collections.sort(sortedNames, new TargetClassSorter(fullyQualifiedTargetClass));

		System.out.println("Sorted candidate classes: " + sortedNames);
		return sortedNames.get(0);
	}

	public Set<String> determineCalledClasses(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		Set<String> calledClasses = new HashSet<String>();

		String className = fullyQualifiedTargetClass.replace('.', '/');
		try {

			InputStream is = ClassLoader.getSystemResourceAsStream(className + ".class");
			if (is == null) {
				throw new ClassNotFoundException("Class '" + className + ".class"
				        + "' should be in target project, but could not be found!");
			}
			ClassReader reader = new ClassReader(is);
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, ClassReader.SKIP_FRAMES);
			handleClassNode(calledClasses, classNode);
		} catch (IOException e) {
			e.printStackTrace();
		}

		calledClasses.remove("java.lang.Object");
		calledClasses.remove(fullyQualifiedTargetClass);

		return calledClasses;
	}

	@SuppressWarnings("unchecked")
	private void handleClassNode(Set<String> calledClasses, ClassNode cn) {
		List<MethodNode> methods = cn.methods;
		for (MethodNode mn : methods) {
			handleMethodNode(calledClasses, cn, mn);
		}
	}

	private boolean isValidClass(String name) {
		if (BytecodeInstrumentation.isJavaClass(name))
			return false;

		if (name.startsWith(targetName))
			return false;

		return true;
	}

	@SuppressWarnings("unchecked")
	private void handleMethodNode(Set<String> calledClasses, ClassNode cn, MethodNode mn) {
		InsnList instructions = mn.instructions;
		Iterator<AbstractInsnNode> iterator = instructions.iterator();

		while (iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof MethodInsnNode) {
				String name = ((MethodInsnNode) insn).owner.replace('/', '.');
				if (isValidClass(name))
					calledClasses.add(name);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DetermineSUT det = new DetermineSUT();
		for (String className : args) {
			System.out.println(className + ": " + det.getSUTName(className));
		}

	}

}
