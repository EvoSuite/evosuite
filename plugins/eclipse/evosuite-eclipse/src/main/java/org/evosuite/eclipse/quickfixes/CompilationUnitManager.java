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
