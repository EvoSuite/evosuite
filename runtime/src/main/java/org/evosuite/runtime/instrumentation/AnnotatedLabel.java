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
/**
 * 
 */
package org.evosuite.runtime.instrumentation;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

/**
 * Annotated labels are used to identify instrumented code
 * such that EvoSuite knows how to deal with  
 *
 * @author fraser
 */
public class AnnotatedLabel extends Label {

	private boolean isStart = false;
	
	private boolean ignore = false;
	
	private LabelNode parent = null;
	
	public AnnotatedLabel(boolean ignore, boolean start) {
		this.ignore = ignore;
		this.isStart = start;
	}
	
	public AnnotatedLabel(boolean ignore, boolean start, LabelNode parent) {
		this.ignore = ignore;
		this.isStart = start;
		this.parent = parent;
	}

	public boolean isStartTag() {
		return isStart;
	}
	
	public boolean shouldIgnore() {
		return ignore;
	}

	public LabelNode getParent() {
		return parent;
	}
	
	public void setParent(LabelNode parent) {
		this.parent = parent;
	}
}
