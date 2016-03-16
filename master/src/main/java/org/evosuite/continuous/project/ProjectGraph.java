/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.continuous.project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.evosuite.seeding.CastClassAnalyzer;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.InheritanceTreeGenerator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * Need to guarantee to build the graph once with all needed info, and then the public
	 * methods just query the map data structures directly (instead of recalculating
	 * on the fly)
	 */

	private static final Logger logger = LoggerFactory.getLogger(ProjectGraph.class);

	private final InheritanceTree inheritanceTree;

	/**
	 * FIXME
	 * Map from TODO (key) to TODO (value)
	 */
	private final Map<String, Set<String>> castInformation;

	private final ProjectStaticData data;


	/**
	 * Main constructor
	 * 
	 * @param data
	 */
	public ProjectGraph(ProjectStaticData data) {

		this.data = data;
		inheritanceTree = InheritanceTreeGenerator.createFromClassList(data.getClassNames());
		castInformation = new HashMap<String, Set<String>>();

		if(logger.isDebugEnabled()){
			logger.debug("Classes in inheritance tree: " + inheritanceTree.getAllClasses());
		}
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

		checkCUT(cut);

		Set<String> parameterClasses = recursionToSearchDirectInputs(cut,includeSubclasses);		
		parameterClasses.remove(cut); 
		removeNonCUT(parameterClasses); 

		logger.debug("Parameter classes of " + cut + ": " + parameterClasses);
		return parameterClasses;
	}

	protected Set<String> recursionToSearchDirectInputs(String aClass, boolean includeSubclasses){
		if (includeSubclasses) {
			Set<String> directlyUsed = recursionToSearchDirectInputs(aClass, false); //recursion
			Set<String> all = new LinkedHashSet<String>(directlyUsed);
			for (String name : directlyUsed) {
				all.addAll(getAllCUTsSubclasses(name));
			}
			return all;
		}

		Set<String> parameterClasses = getParameterClasses(aClass);
		parameterClasses.addAll(getCastClasses(aClass));

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

		checkCUT(cut);

		Set<String> classNames = recursionToSearchWhatUsesItAsInput(cut,includeSuperClasses);			
		classNames.remove(cut); 
		removeNonCUT(classNames); 

		return classNames;
	}


	protected Set<String> recursionToSearchWhatUsesItAsInput(String aClass, boolean includeSuperClasses){
		if (includeSuperClasses) {
			Set<String> directlyUsed = recursionToSearchWhatUsesItAsInput(aClass, false); //recursion
			Set<String> all = new LinkedHashSet<String>(directlyUsed);
			for (String name : getAllCUTsParents(aClass)) {
				all.addAll(recursionToSearchWhatUsesItAsInput(name, false)); //recursion
			}
			return all;
		}

		Set<String> classNames = new LinkedHashSet<String>();
		for (String className : inheritanceTree.getAllClasses()) {
			if (className.equals(aClass)){
				continue;
			}

			Set<String> inputClasses = getCUTsDirectlyUsedAsInput(className, true);  
			if (inputClasses.contains(aClass)){
				classNames.add(className);
			}
		}

		return classNames;
	}

	/**
	 * Is the given class name representing an interface in the SUT?
	 * @param className
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean isInterface(String className) throws IllegalArgumentException {
		checkClass(className);
		ClassNode node = getClassNode(className);
		if ((node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE)
			return true;

		return false;
	}

	/**
	 * Is the given class name representing a concrete class in the SUT?
	 * @param className
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean isConcrete(String className) throws IllegalArgumentException {
		checkClass(className);
		return !isInterface(className) && !isAbstract(className);
	}

	/**
	 * Is the given class name representing an abstract class in the SUT?
	 * @param className
	 * @return
	 * @throws IllegalArgumentException
	 */
	public boolean isAbstract(String className) throws IllegalArgumentException {
		checkClass(className);
		ClassNode node = getClassNode(className);
		return (node.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
	}

	/**
	 * Check if the given class does belong to the SUT.
	 * This is independent on whether it is a CUT (ie testable) or not.
	 * 
	 * @param className
	 * @throws IllegalArgumentException
	 */
	private void checkClass(String className) throws IllegalArgumentException{
		if(!data.containsClass(className)){
			throw new IllegalArgumentException("Class "+className+" is not part of the SUT");
		}
	}

	/**
	 * Check if the given class does belong to the SUT and if it is testable (ie a CUT).
	 * 
	 * @param cut
	 */
	private void checkCUT(String cut) throws IllegalArgumentException{
		ClassInfo info = data.getClassInfo(cut);
		if(info==null){
			throw new IllegalArgumentException("Class "+cut+" is not part of the SUT");
		}
		if(!info.isTestable()){
			throw new IllegalArgumentException("Class "+cut+" belongs to the SUT, but it is not a CUT (ie testable)");
		}
	}


	/**
	 * Return all the child hierarchy of the <code>aClass</code>. Include only classes
	 * that are CUTs.
	 * This <code>aClass</code> will not be part of the returned set, even if it is a CUT
	 * 
	 * @param aClass
	 *            a class belonging to the SUT, but not necessarily a CUT
	 * @return a set of full qualifying names of CUTs
	 * @throws IllegalArgumentException
	 *             if the input <code>aClass</code> does not belong to the SUT
	 */
	public Set<String> getAllCUTsSubclasses(String aClass) throws IllegalArgumentException {
		checkClass(aClass);

		Set<String>  set = null;
		try{ //FIXME
			set = inheritanceTree.getSubclasses(aClass);
		} catch(Exception e){
			logger.error("Bug in inheritanceTree: "+e);
			return new HashSet<String>();
		}
		set.remove(aClass); 
		removeNonCUT(set);
		return set;
	}

	/**
	 * Return all the CUT classes that this <code>aClass</code> extends/implements
	 * (ie, parent hierarchy).
	 * This <code>aClass</code> will not be part of the returned set, even if it is a CUT
	 * 
	 * @param aClass
	 *            a class belonging to the SUT, but not necessarily a CUT
	 * @return a set of full qualifying names of CUTs
	 * @throws IllegalArgumentException
	 *             if the input <code>aClass</code> does not belong to the SUT
	 */
	public Set<String> getAllCUTsParents(String aClass) throws IllegalArgumentException {
		checkClass(aClass);
		Set<String>  set = null;

		try{ //FIXME
			set =	inheritanceTree.getSuperclasses(aClass);
		}
		catch(Exception e){
			logger.error("Bug in inheritanceTree: "+e);
			return new HashSet<String>();
		}
		set.remove(aClass); //it seems inheritanceTree returns 'cut' in the set
		removeNonCUT(set);
		return set;
	}


	private void removeNonCUT(Set<String> set) throws IllegalArgumentException{

		Iterator<String> iter = set.iterator();
		while(iter.hasNext()){
			String name = iter.next();
			ClassInfo info = data.getClassInfo(name);
			if(info==null){
				throw new IllegalArgumentException("Class "+name+" does not belong to the SUT");
			}

			if(!info.isTestable()){
				iter.remove();
			}
		}
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
