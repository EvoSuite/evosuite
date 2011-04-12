/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMSwitcher;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * The CFGClassAdapter calls a CFG generator for relevant methods
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGClassAdapter extends ClassAdapter {

	private static Logger logger = Logger.getLogger(CFGClassAdapter.class);

	/** Current class */
	private final String className;

	/**
	 * Constructor
	 * 
	 * @param visitor
	 * @param className
	 */
	public CFGClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public MethodVisitor visitMethod(int methodAccess, String name, String descriptor,
	        String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor, signature,
		                                     exceptions);

		if ((methodAccess & Opcodes.ACC_SYNTHETIC) > 0
		        || (methodAccess & Opcodes.ACC_BRIDGE) > 0) {
			return mv;
		}

		if (!Properties.USE_DEPRECATED
		        && (methodAccess & Opcodes.ACC_DEPRECATED) == Opcodes.ACC_DEPRECATED) {
			logger.info("Skipping deprecated method " + name);
			return mv;
		}
		String classNameWithDots = className.replace('/', '.');
		List<Mutation> mutants = new ArrayList<Mutation>();
		if (Properties.MUTATION) {
			HOMSwitcher switcher = new HOMSwitcher();
			mutants = switcher.getMutants();
		}

		mv = new CFGMethodAdapter(classNameWithDots, methodAccess, name, descriptor,
		        signature, exceptions, mv, mutants);
		/*
				if (!exclude) {
					if(false) {
						MutationForRun mm = MutationForRun.getFromDefaultLocation();
						mv = new CFGMethodAdapter(className, methodAccess, name, descriptor, signature, exceptions, mv, mm.getMutations());
					}
					else {
						mv = new CFGMethodAdapter(className, methodAccess, name, descriptor, signature, exceptions, mv, new ArrayList<Mutation>());
					}
				}
				*/
		return mv;
	}
}
