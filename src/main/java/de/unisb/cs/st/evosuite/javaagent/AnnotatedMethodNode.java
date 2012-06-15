/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author fraser
 * 
 */
public class AnnotatedMethodNode extends MethodNode {

	/**
	 * @param access
	 * @param name
	 * @param desc
	 * @param signature
	 * @param exceptions
	 */
	public AnnotatedMethodNode(int access, String name, String desc, String signature,
	        String[] exceptions) {
		super(access, name, desc, signature, exceptions);
	}

	/**
	 * Returns the LabelNode corresponding to the given Label. Creates a new
	 * LabelNode if necessary. The default implementation of this method uses
	 * the {@link Label#info} field to store associations between labels and
	 * label nodes.
	 * 
	 * @param l
	 *            a Label.
	 * @return the LabelNode corresponding to l.
	 */
	@Override
	protected LabelNode getLabelNode(final Label l) {
		if (l instanceof AnnotatedLabel) {
			AnnotatedLabel al = (AnnotatedLabel) l;
			al.parent = new LabelNode(al);
			return al.parent;
		} else {
			if (!(l.info instanceof LabelNode)) {
				l.info = new LabelNode(l);
			}
			return (LabelNode) l.info;
		}
	}
}
