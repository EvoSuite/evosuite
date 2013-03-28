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
package org.evosuite.instrumentation;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

/**
 * <p>AnnotatedLabel class.</p>
 *
 * @author fraser
 */
public class AnnotatedLabel extends Label {

	protected LabelNode parent;

	/**
	 * <p>Constructor for AnnotatedLabel.</p>
	 */
	public AnnotatedLabel() {
		this.parent = null;
	}

	/**
	 * <p>Constructor for AnnotatedLabel.</p>
	 *
	 * @param parent a {@link org.objectweb.asm.tree.LabelNode} object.
	 */
	public AnnotatedLabel(LabelNode parent) {
		this.parent = parent;
	}

}
