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
package org.evosuite.junit;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * This is a implementation aid to show all visitor methods that have not been implemented.
 *
 * @author roessler
 */
public class LoggingVisitor extends ASTVisitor {

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoggingVisitor.class);

	/** {@inheritDoc} */
	@Override
	public void endVisit(AnnotationTypeDeclaration node) {
		logger.warn("Method endVisitAnnotationTypeDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(AnnotationTypeMemberDeclaration node) {
		logger.warn("Method endVisitAnnotationTypeMemberDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		logger.warn("Method endVisitAnonymousClassDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ArrayAccess node) {
		logger.warn("Method endVisitArrayAccess for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ArrayCreation node) {
		logger.warn("Method endVisitArrayCreation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ArrayInitializer node) {
		logger.warn("Method endVisitArrayInitializer for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ArrayType node) {
		logger.warn("Method endVisitArrayType for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(AssertStatement node) {
		logger.warn("Method endVisitAssertStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Assignment node) {
		logger.warn("Method endVisitAssignment for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Block node) {
		logger.warn("Method endVisitBlock for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(BlockComment node) {
		logger.warn("Method endVisitBlockComment for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(BooleanLiteral node) {
		logger.warn("Method endVisitBooleanLiteral for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(BreakStatement node) {
		logger.warn("Method endVisitBreakStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(CastExpression node) {
		logger.warn("Method endVisitCastExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(CatchClause node) {
		logger.warn("Method endVisitCatchClause for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(CharacterLiteral node) {
		logger.warn("Method endVisitCharacterLiteral for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ClassInstanceCreation node) {
		logger.warn("Method endVisitClassInstanceCreation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(CompilationUnit node) {
		logger.warn("Method endVisitCompilationUnit for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ConditionalExpression node) {
		logger.warn("Method endVisitConditionalExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ConstructorInvocation node) {
		logger.warn("Method endVisitConstructorInvocation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ContinueStatement node) {
		logger.warn("Method endVisitContinueStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(DoStatement node) {
		logger.warn("Method endVisitDoStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(EmptyStatement node) {
		logger.warn("Method endVisitEmptyStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(EnhancedForStatement node) {
		logger.warn("Method endVisitEnhancedForStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(EnumConstantDeclaration node) {
		logger.warn("Method endVisitEnumConstantDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(EnumDeclaration node) {
		logger.warn("Method endVisitEnumDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ExpressionStatement node) {
		logger.warn("Method endVisitExpressionStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(FieldAccess node) {
		logger.warn("Method endVisitFieldAccess for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(FieldDeclaration node) {
		logger.warn("Method endVisitFieldDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ForStatement node) {
		logger.warn("Method endVisitForStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(IfStatement node) {
		logger.warn("Method endVisitIfStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ImportDeclaration node) {
		logger.warn("Method endVisitImportDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(InfixExpression node) {
		logger.warn("Method endVisitInfixExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Initializer node) {
		logger.warn("Method endVisitInitializer for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(InstanceofExpression node) {
		logger.warn("Method endVisitInstanceofExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Javadoc node) {
		logger.warn("Method endVisitJavadoc for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(LabeledStatement node) {
		logger.warn("Method endVisitLabeledStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(LineComment node) {
		logger.warn("Method endVisitLineComment for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MarkerAnnotation node) {
		logger.warn("Method endVisitMarkerAnnotation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MemberRef node) {
		logger.warn("Method endVisitMemberRef for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MemberValuePair node) {
		logger.warn("Method endVisitMemberRef for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MethodDeclaration node) {
		logger.warn("Method endVisitMethodDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MethodInvocation node) {
		logger.warn("Method endVisitMethodInvocation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MethodRef node) {
		logger.warn("Method endVisitMethodRef for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(MethodRefParameter node) {
		logger.warn("Method endVisitMethodRefParameter for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(Modifier node) {
		logger.warn("Method endVisitModifier for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(NormalAnnotation node) {
		logger.warn("Method endVisitNormalAnnotation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(NullLiteral node) {
		logger.warn("Method endVisitNullLiteral for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(NumberLiteral node) {
		logger.warn("Method endVisitNumberLiteral for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(PackageDeclaration node) {
		logger.warn("Method endVisitPackageDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ParameterizedType node) {
		logger.warn("Method endVisitParameterizedType for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ParenthesizedExpression node) {
		logger.warn("Method endVisitParenthesizedExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(PostfixExpression node) {
		logger.warn("Method endVisitPostfixExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(PrefixExpression node) {
		logger.warn("Method endVisitPrefixExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(PrimitiveType node) {
		logger.warn("Method endVisitPrimitiveType for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(QualifiedName node) {
		logger.warn("Method endVisitQualifiedName for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(QualifiedType node) {
		logger.warn("Method endVisitQualifiedType for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ReturnStatement node) {
		logger.warn("Method endVisitReturnStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SimpleName node) {
		logger.warn("Method endVisitSimpleName for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SimpleType node) {
		logger.warn("Method endVisitSimpleType for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SingleMemberAnnotation node) {
		logger.warn("Method endVisitSingleMemberAnnotation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SingleVariableDeclaration node) {
		logger.warn("Method endVisitSingleVariableDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(StringLiteral node) {
		logger.warn("Method endVisitStringLiteral for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SuperConstructorInvocation node) {
		logger.warn("Method endVisitSuperConstructorInvocation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SuperFieldAccess node) {
		logger.warn("Method endVisitSuperFieldAccess for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SuperMethodInvocation node) {
		logger.warn("Method endVisitSuperMethodInvocation for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SwitchCase node) {
		logger.warn("Method endVisitSwitchCase for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SwitchStatement node) {
		logger.warn("Method endVisitSwitchStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(SynchronizedStatement node) {
		logger.warn("Method endVisitSynchronizedStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TagElement node) {
		logger.warn("Method endVisitTagElement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TextElement node) {
		logger.warn("Method endVisitTextElement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ThisExpression node) {
		logger.warn("Method endVisitThisExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(ThrowStatement node) {
		logger.warn("Method endVisitThrowStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TryStatement node) {
		logger.warn("Method endVisitTryStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TypeDeclaration node) {
		logger.warn("Method endVisitTypeDeclaration for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TypeDeclarationStatement node) {
		logger.warn("Method endVisitTypeDeclarationStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TypeLiteral node) {
		logger.warn("Method endVisitTypeLiteral for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(TypeParameter node) {
		logger.warn("Method endVisitTypeParameter for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(VariableDeclarationExpression node) {
		logger.warn("Method endVisitVariableDeclarationExpression for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(VariableDeclarationFragment node) {
		logger.warn("Method endVisitVariableDeclarationFragment for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		logger.warn("Method endVisitVariableDeclarationStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(WhileStatement node) {
		logger.warn("Method endVisitWhileStatement for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}

	/** {@inheritDoc} */
	@Override
	public void endVisit(WildcardType node) {
		logger.warn("Method endVisitWildcardType for " + node + " for " + node + " not implemented!");
		super.endVisit(node);
	}


	/** {@inheritDoc} */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		logger.warn("Method visitAnnotationTypeDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		logger.warn("Method visitAnnotationTypeMemberDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		logger.warn("Method visitAnonymousClassDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ArrayAccess node) {
		logger.warn("Method visitArrayAccess for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ArrayCreation node) {
		logger.warn("Method visitArrayCreation for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ArrayInitializer node) {
		logger.warn("Method visitArrayInitializer for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ArrayType node) {
		logger.warn("Method visitArrayType for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(AssertStatement node) {
		logger.warn("Method visitAssertStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(Assignment node) {
		logger.warn("Method visitAssignment for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(Block node) {
		logger.warn("Method visitBlock for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(BlockComment node) {
		logger.warn("Method visitBlockComment for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(BooleanLiteral node) {
		logger.warn("Method visitBooleanLiteral for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(BreakStatement node) {
		logger.warn("Method visitBreakStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(CastExpression node) {
		logger.warn("Method visitCastExpression for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(CatchClause node) {
		logger.warn("Method visitCatchClause for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(CharacterLiteral node) {
		logger.warn("Method visitCharacterLiteral for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		logger.warn("Method visitClassInstanceCreation for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(CompilationUnit node) {
		logger.warn("Method visitCompilationUnit for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ConditionalExpression node) {
		logger.warn("Method visitConditionalExpression for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ConstructorInvocation node) {
		logger.warn("Method visitConstructorInvocation for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ContinueStatement node) {
		logger.warn("Method visitContinueStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(DoStatement node) {
		logger.warn("Method visitEmptyStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(EmptyStatement node) {
		logger.warn("Method visitEmptyStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(EnhancedForStatement node) {
		logger.warn("Method visitEnhancedForStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		logger.warn("Method visitEnumConstantDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(EnumDeclaration node) {
		logger.warn("Method visitEnumDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ExpressionStatement node) {
		logger.warn("Method visitExpressionStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(FieldAccess node) {
		logger.warn("Method visitFieldAccess for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(FieldDeclaration node) {
		logger.warn("Method visitFieldDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ForStatement node) {
		logger.warn("Method visitForStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(IfStatement node) {
		logger.warn("Method visitIfStatement for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ImportDeclaration node) {
		logger.warn("Method visitImportDeclaration for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(InfixExpression node) {
		logger.warn("Method visitInfixExpression for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(Initializer node) {
		logger.warn("Method visitInitializer for " + node + " for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(InstanceofExpression node) {
		logger.warn("Method visitInstanceofExpression for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(Javadoc node) {
		logger.warn("Method visitJavadoc for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(LabeledStatement node) {
		logger.warn("Method visitLabeledStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(LineComment node) {
		logger.warn("Method visitLineComment for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MarkerAnnotation node) {
		logger.warn("Method visitMarkerAnnotation for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MemberRef node) {
		logger.warn("Method visitMemberValuePair for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MemberValuePair node) {
		logger.warn("Method visitMemberValuePair for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MethodDeclaration node) {
		logger.warn("Method visitMethodDeclaration for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MethodInvocation node) {
		logger.warn("Method visitMethodInvocation for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MethodRef node) {
		logger.warn("Method visitMethodRef for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(MethodRefParameter node) {
		logger.warn("Method visitMethodRefParameter for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(Modifier node) {
		logger.warn("Method visitModifier for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(NormalAnnotation node) {
		logger.warn("Method visitNormalAnnotation for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(NullLiteral node) {
		logger.warn("Method visitNullLiteral for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(NumberLiteral node) {
		logger.warn("Method visitNumberLiteral for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(PackageDeclaration node) {
		logger.warn("Method visitPackageDeclaration for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ParameterizedType node) {
		logger.warn("Method visitParameterizedType for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ParenthesizedExpression node) {
		logger.warn("Method visitParenthesizedExpression for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(PostfixExpression node) {
		logger.warn("Method visitPostfixExpression for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(PrefixExpression node) {
		logger.warn("Method visitPrefixExpression for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(PrimitiveType node) {
		logger.warn("Method visitPrimitiveType for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(QualifiedName node) {
		logger.warn("Method visitQualifiedName for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(QualifiedType node) {
		logger.warn("Method visitQualifiedType for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ReturnStatement node) {
		logger.warn("Method visitReturnStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SimpleName node) {
		logger.warn("Method visitSimpleName for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SimpleType node) {
		logger.warn("Method visitSimpleType for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SingleMemberAnnotation node) {
		logger.warn("Method visitSingleMemberAnnotation for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		logger.warn("Method visitSingleVariableDeclaration for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(StringLiteral node) {
		logger.warn("Method visitStringLiteral for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		logger.warn("Method visitSuperConstructorInvocation for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SuperFieldAccess node) {
		logger.warn("Method visitSuperFieldAccess for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SuperMethodInvocation node) {
		logger.warn("Method visitSuperMethodInvocation for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SwitchCase node) {
		logger.warn("Method visitSwitchCase for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SwitchStatement node) {
		logger.warn("Method visitSwitchStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(SynchronizedStatement node) {
		logger.warn("Method visitSynchronizedStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TagElement node) {
		logger.warn("Method visitTagElement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TextElement node) {
		logger.warn("Method visitTextElement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ThisExpression node) {
		logger.warn("Method visitThisExpression for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(ThrowStatement node) {
		logger.warn("Method visitThrowStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TryStatement node) {
		logger.warn("Method visitTryStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TypeDeclaration node) {
		logger.warn("Method visitTypeDeclaration for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TypeDeclarationStatement node) {
		logger.warn("Method visitTypeDeclarationStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TypeLiteral node) {
		logger.warn("Method visitTypeLiteral for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(TypeParameter node) {
		logger.warn("Method visitTypeParameter for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		logger.warn("Method visitVariableDeclarationExpression for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		logger.warn("Method visitVariableDeclarationFragment for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		logger.warn("Method visitVariableDeclarationStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(WhileStatement node) {
		logger.warn("Method visitWhileStatement for " + node + " not implemented!");
		return super.visit(node);
	}

	/** {@inheritDoc} */
	@Override
	public boolean visit(WildcardType node) {
		logger.warn("Method visitWildcardType for " + node + " not implemented!");
		return super.visit(node);
	}
}
