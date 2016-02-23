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
package org.evosuite.eclipse.quickfixes;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Thomas White
 * @category version 0.0
 */
public class CompilationUnitManager {

	public static CompilationUnit getCompilationUnit(IResource r)
			throws IOException, JavaModelException {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(getICompilationUnit(r));

		final CompilationUnit compClass = (CompilationUnit) parser
				.createAST(null);
		return compClass;
	}

	public static ICompilationUnit getICompilationUnit(IResource r)
			throws JavaModelException {
		IJavaElement jEle = JavaCore.create(r);

		if (jEle instanceof ICompilationUnit) {
			ICompilationUnit icomp = null;
			icomp = ((ICompilationUnit) jEle).getWorkingCopy(null);
			return icomp;

		}
		return null;
	}

}
