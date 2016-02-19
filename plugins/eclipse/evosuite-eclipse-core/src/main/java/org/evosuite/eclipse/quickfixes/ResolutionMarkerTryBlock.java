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
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;

public class ResolutionMarkerTryBlock implements IMarkerResolution {

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return "Surround with try/catch block";
	}

	@Override
	public void run(IMarker marker) {
		// TODO Auto-generated method stub
		IResource res = marker.getResource();

		try {
			String markerMessage = (String) marker
					.getAttribute(IMarker.MESSAGE);
			String exception = markerMessage.split(" ")[0];
			String[] exceptionPackageArray = exception.split("\\.");
			String exceptionPackage = "";
			for (int i = 0; i < exceptionPackageArray.length - 1; i++) {
				exceptionPackage += exceptionPackageArray[i] + ".";
			}
			exception = exception.substring(exceptionPackage.length());

			System.out.println("Package: " + exceptionPackage + ", Exception: "
					+ exception);

			ICompilationUnit icomp = CompilationUnitManager
					.getICompilationUnit(res);

			CompilationUnit compunit = CompilationUnitManager
					.getCompilationUnit(res);

			int position = marker.getAttribute(IMarker.CHAR_START, 0) + 1;
			int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
			if (position == 1) {
				position = compunit.getPosition(line, 0);
			}

			AST ast = compunit.getAST();
			ASTRewrite rewriter = ASTRewrite.create(ast);

			IJavaElement element = icomp.getElementAt(position);
			IJavaElement method = getMethod(element);

			// TypeDeclaration td = (TypeDeclaration) compunit.types().get(0);
			TypeDeclaration td = (TypeDeclaration) compunit.types().get(0);

			MethodDeclaration md = td.getMethods()[0];

			int counter = 1;
			while (!md.getName().getFullyQualifiedName()
					.equals(method.getElementName())
					&& counter < td.getMethods().length) {
				md = td.getMethods()[counter];
				System.out.println(md.getName().getFullyQualifiedName() + " "
						+ method.getElementName());
				counter++;
			}

			Block block = md.getBody();

			ListRewrite lr = rewriter.getListRewrite(block,
					Block.STATEMENTS_PROPERTY);
			ImportDeclaration id = ast.newImportDeclaration();
			id.setName(ast.newName(exceptionPackage + exception));
			ListRewrite lrClass = rewriter.getListRewrite(compunit,
					CompilationUnit.TYPES_PROPERTY);
			lrClass.insertAt(id, 0, null);
			ASTNode currentNode = null;
			List<ASTNode> list = new ArrayList<ASTNode>();
			for (Object o : lr.getOriginalList()) {
				if (o instanceof ASTNode) {
					list.add((ASTNode) o);
				}
			}
			for (int i = 0; i < list.size(); i++) {
				ASTNode node = list.get(i);
				int nodeLine = compunit.getLineNumber(node.getStartPosition());
				System.out.println(line + " " + nodeLine);
				if (line == nodeLine) {
					currentNode = node;
					break;
				}
				List childrenList = node.structuralPropertiesForType();
				for (int j = 0; j < childrenList.size(); j++) {
					StructuralPropertyDescriptor curr = (StructuralPropertyDescriptor) childrenList
							.get(j);
					Object child = node.getStructuralProperty(curr);
					if (child instanceof List) {
						for (Object ob : (List) child) {
							if (ob instanceof ASTNode) {
								list.add((ASTNode) ob);
							}
						}
					} else if (child instanceof ASTNode) {
						list.add((ASTNode) child);
					}
				}
			}

			TryStatement ts = ast.newTryStatement();
			Statement emptyStatement = (Statement) rewriter
					.createStringPlaceholder("\n",
							ASTNode.EMPTY_STATEMENT);
			Statement emptyStatement2 = (Statement) rewriter
					.createStringPlaceholder("\n\t",
							ASTNode.EMPTY_STATEMENT);
			Block b2 = ast.newBlock();
			b2.statements().add(0,
					ASTNode.copySubtree(b2.getAST(), currentNode));
			b2.statements().add(1, emptyStatement);
			b2.statements().add(0, emptyStatement2);
			ts.setBody(b2);
			CatchClause cc = ast.newCatchClause();
			SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
			svd.setName(ast.newSimpleName("e"));
			Type type = ast.newSimpleType(ast.newName(exception));
			svd.setType(type);
			cc.setException(svd);
			Block b3 = ast.newBlock();
			Statement printStatement = (Statement) rewriter
					.createStringPlaceholder("e.printStackTrace();",
							ASTNode.EMPTY_STATEMENT);
			b3.statements().add(0, printStatement);
			cc.setBody(b3);
			ListRewrite parentList = rewriter.getListRewrite(
					currentNode.getParent(), Block.STATEMENTS_PROPERTY);
			parentList.replace(currentNode, ts, null);
			parentList.insertAfter(cc, ts, null);

			ITextFileBufferManager bm = FileBuffers.getTextFileBufferManager();
			IPath path = compunit.getJavaElement().getPath();
			try {
				bm.connect(path, null, null);
				ITextFileBuffer textFileBuffer = bm.getTextFileBuffer(path,
						null);
				IDocument document = textFileBuffer.getDocument();
				TextEdit edits = rewriter.rewriteAST(document, null);
				edits.apply(document);
				textFileBuffer.commit(null, false);

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
			marker.delete();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public IJavaElement getMethod(IJavaElement e) {
		IJavaElement method = e;
		while (method != null && method.getElementType() != IJavaElement.METHOD) {
			method = method.getParent();
		}
		return method;

	}
}
