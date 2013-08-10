package org.evosuite.continuous.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.seeding.CastClassAnalyzer;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.InheritanceTreeGenerator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * <p>
 * Class representing the class graph. For each class (CUT and non-testable),
 * not only we want to know its hierarchy (parents, interfaces, subclasses, etc)
 * but also where in the project it is used by other classes as input.
 * </p>
 * 
 * <p>
 * For definition of CUT, see {@link ProjectStaticData}
 * </p>
 */
public class ProjectGraph {

	/*
	 * TODO: maybe best to build the graph once with all needed info, and then the public
	 * methods just querying the map data structures directly (instead of recalculating
	 * on the fly)
	 */

	private final InheritanceTree inheritanceTree;

	private final Map<String, Set<String>> castInformation = new HashMap<String, Set<String>>();

	public ProjectGraph(Collection<String> classNames) {
		inheritanceTree = InheritanceTreeGenerator.createFromClassList(classNames);
		System.out.println("Classes in inheritance tree: "
		        + inheritanceTree.getAllClasses());
	}

	/**
	 * <p>
	 * Return the full qualifying names of all classes that are CUTs and that
	 * are used as input in any of the public methods of <code>cut</code> (but
	 * not of any of its parent hierarchy).
	 * </p>
	 * 
	 * <p>
	 * If a method takes as input a reference of a non-SUT class (e.g.,
	 * <code>java.lang.Object</code>), but then there is a cast to a CUT (e.g. a
	 * class X), then X will be added in the returned set.
	 * </p>
	 * 
	 * <p>
	 * If a method takes as input a reference of a SUT class X that is not a CUT
	 * (e.g., an interface with no code), then X will <b>not</b> be added in the
	 * returned set, although based on <code>includeSubclasses</code> we might
	 * add its subclasses.
	 * </p>
	 * 
	 * @param cut
	 *            the class under test (CUT)
	 * @param includeSubclasses
	 *            If a class X is in the returned set, then normally no subclass
	 *            Y of X would be added in the returned set, unless Y is
	 *            directly used in the CUT as input.
	 * @return a set of full qualifying names of CUTs
	 * @throws IllegalArgumentException
	 *             if the input <code>cut</code> is not a CUT
	 */
	public Set<String> getCUTsDirectlyUsedAsInput(String cut, boolean includeSubclasses)
	        throws IllegalArgumentException {
		if (includeSubclasses) {
			Set<String> directlyUsed = getCUTsDirectlyUsedAsInput(cut, false); //recursion
			Set<String> all = new LinkedHashSet<String>(directlyUsed);
			for (String name : directlyUsed) {
				all.addAll(getAllCUTsSubclasses(name));
			}
			return all;
		}

		Set<String> parameterClasses = getParameterClasses(cut);
		parameterClasses.addAll(getCastClasses(cut));
		System.out.println("Parameter classes of " + cut + ": " + parameterClasses);
		return parameterClasses;
	}

	/**
	 * Calculate all the CUTs that use the given <code>cut</code> as input in
	 * any of their public methods
	 * 
	 * @param cut
	 *            the class under test (CUT)
	 * @param includeSuperClasses
	 *            not only using as input the
	 *            <code>cut</body>, but also any of its CUT ancestors/interfaces
	 * @return a set of full qualifying names of CUTs
	 * @throws IllegalArgumentException
	 *             if the input <code>cut</code> is not a CUT
	 */
	public Set<String> getCUTsThatUseThisCUTasInput(String cut,
	        boolean includeSuperClasses) throws IllegalArgumentException {
		if (includeSuperClasses) {
			Set<String> directlyUsed = getCUTsThatUseThisCUTasInput(cut, false); //recursion
			Set<String> all = new LinkedHashSet<String>(directlyUsed);
			for (String name : getAllCUTsParents(cut)) {
				all.addAll(getCUTsThatUseThisCUTasInput(name, false)); //recursion
			}
			return all;
		}

		Set<String> classNames = new LinkedHashSet<String>();
		for (String className : inheritanceTree.getAllClasses()) {
			if (className.equals(cut))
				continue;

			Set<String> inputClasses = getCUTsDirectlyUsedAsInput(className, false); // TODO: Include subclasses?
			if (inputClasses.contains(cut))
				classNames.add(className);
		}
		return classNames;
	}

	public boolean isInterface(String cut) throws IllegalArgumentException {
		ClassNode node = getClassNode(cut);
		if ((node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE)
			return true;

		return false;
	}

	public boolean isConcrete(String cut) throws IllegalArgumentException {
		return !isInterface(cut) && !isAbstract(cut);
	}

	public boolean isAbstract(String cut) throws IllegalArgumentException {
		ClassNode node = getClassNode(cut);
		return (node.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
	}

	/**
	 * Return all the child hierarchy of <code>cut</code>. Include only classes
	 * that are CUTs
	 * 
	 * @param cut
	 *            the class under test (CUT)
	 * @return a set of full qualifying names of CUTs
	 * @throws IllegalArgumentException
	 *             if the input <code>cut</code> is not a CUT
	 */
	public Set<String> getAllCUTsSubclasses(String cut) throws IllegalArgumentException {
		return inheritanceTree.getSubclasses(cut);
	}

	/**
	 * Return all the CUT classes that this <code>cut</code> extends/implements
	 * (ie, parent hierarchy).
	 * This <code>cut</code> will not be part of the returned set
	 * 
	 * @param cut
	 *            the class under test (CUT)
	 * @return a set of full qualifying names of CUTs
	 * @throws IllegalArgumentException
	 *             if the input <code>cut</code> is not a CUT
	 */
	public Set<String> getAllCUTsParents(String cut) throws IllegalArgumentException {
		Set<String>  set =  inheritanceTree.getSuperclasses(cut);
		set.remove(cut); //it seems inheritanceTree returns 'cut' in the set
		return set;
	}

	/**
	 * For now use the cache provided by dependency analysis
	 * 
	 * @param className
	 * @return
	 */
	private ClassNode getClassNode(String className) {
		return DependencyAnalysis.getClassNode(className);
	}

	/**
	 * Determine the set of classes that are used in casts in a CUT
	 * 
	 * @param className
	 * @return
	 */
	private Set<String> getCastClasses(String className) {
		if (!castInformation.containsKey(className)) {
			CastClassAnalyzer analyzer = new CastClassAnalyzer();
			// The analyzer gets the classnode from the DependencyAnalysis classnode cache
			Map<Type, Integer> castMap = analyzer.analyze(className);
			Set<String> castClasses = new LinkedHashSet<String>();
			for (Type type : castMap.keySet()) {
				String name = type.getClassName();
				if (inheritanceTree.hasClass(name)) {
					castClasses.add(name);
				}
			}
			castInformation.put(className, castClasses);
		}

		return castInformation.get(className);
	}

	@SuppressWarnings("unchecked")
	private Set<String> getParameterClasses(String cut) {
		Set<String> parameters = new LinkedHashSet<String>();
		ClassNode node = getClassNode(cut);
		List<MethodNode> methods = node.methods;
		for (MethodNode methodNode : methods) {
			addParameterClasses(methodNode, parameters);
		}

		List<FieldNode> fields = node.fields;
		for (FieldNode fieldNode : fields) {
			addParameterClasses(fieldNode, parameters);
		}
		return parameters;
	}

	private void addParameterClasses(MethodNode methodNode, Set<String> classNames) {
		// TODO: Only including public methods for now. Should this be refined to match
		//       TestClusterGenerator.canUse?
		if ((methodNode.access & Opcodes.ACC_PUBLIC) != Opcodes.ACC_PUBLIC)
			return;

		for (Type parameterType : Type.getArgumentTypes(methodNode.desc)) {
			String name = parameterType.getClassName();
			if (inheritanceTree.hasClass(name)) {
				classNames.add(name);
			}
		}
	}

	private void addParameterClasses(FieldNode fieldNode, Set<String> classNames) {
		// TODO: Only including public fields for now. Should this be refined to match
		//       TestClusterGenerator.canUse?
		if ((fieldNode.access & Opcodes.ACC_PUBLIC) != Opcodes.ACC_PUBLIC)
			return;
		String name = Type.getType(fieldNode.desc).getClassName();
		if (inheritanceTree.hasClass(name)) {
			classNames.add(name);
		}
	}

}
