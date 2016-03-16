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
package org.evosuite.eclipse.quickfixes;

import java.io.IOException;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;

public class ResolutionMarkerEvoIgnoreForMethod implements IMarkerResolution {

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return "Add @EvoIgnore annotation to method";
	}

	@Override
	public void run(IMarker marker) {
		// TODO Auto-generated method stub
		IResource res = marker.getResource();

		try {

			ICompilationUnit icomp = CompilationUnitManager
					.getICompilationUnit(res);

			CompilationUnit compunit = CompilationUnitManager
					.getCompilationUnit(res);

			int position = marker.getAttribute(IMarker.CHAR_START, 0) + 1;
			if (position == 1) {
				int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
				position = compunit.getPosition(line, 0);
			}

			AST ast = compunit.getAST();
			ASTRewrite rewriter = ASTRewrite.create(ast);
			
			IJavaElement element = icomp.getElementAt(position);
			IJavaElement method = getMethod(element);
			
			
//			TypeDeclaration td = (TypeDeclaration) compunit.types().get(0);
			TypeDeclaration td = (TypeDeclaration) compunit.types().get(0);
			
			MethodDeclaration md = td.getMethods()[0];
			

			int counter = 1;
			while (!md.getName().getFullyQualifiedName().equals(method.getElementName()) && counter < td.getMethods().length){
				md = td.getMethods()[counter];
				System.out.println(md.getName().getFullyQualifiedName() + " " + method.getElementName());
				counter++;
			}
			
			Annotation annotation = ast.newNormalAnnotation();
			annotation.setTypeName(ast.newName("EvoIgnore"));
			ImportDeclaration id = ast.newImportDeclaration();
			
			id.setName(ast.newName("org.evosuite.quickfixes.annotations.EvoIgnore"));
			ListRewrite lr = rewriter.getListRewrite(md.getParent(), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			ListRewrite lr2 = rewriter.getListRewrite(compunit, CompilationUnit.TYPES_PROPERTY);
			
			//lr.insertFirst(annotation, null);
			lr.insertBefore(annotation, md, null);
			lr2.insertAt(id, 0, null);
			ITextFileBufferManager bm = FileBuffers.getTextFileBufferManager();
			IPath path = compunit.getJavaElement().getPath();
			try {
				bm.connect(path, null, null);
				ITextFileBuffer textFileBuffer = bm.getTextFileBuffer(path, null);
				IDocument document = textFileBuffer.getDocument();
				TextEdit edits = rewriter.rewriteAST(document, null);
				edits.apply(document);
				textFileBuffer
					.commit(null, false);

			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedTreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					bm.disconnect(path, null, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // (4)
			}
			System.out.println(lr.getRewrittenList() + "\nPosition: " + position + "\nEdits: " + rewriter.toString());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			marker.delete();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public IJavaElement getMethod(IJavaElement e){
		IJavaElement method = e;
		while (method != null && method.getElementType() != IJavaElement.METHOD){
			method = method.getParent();
		}
		return method;
		
	}
}
