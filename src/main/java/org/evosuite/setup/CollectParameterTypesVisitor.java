/**
 * 
 */
package org.evosuite.setup;

import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class CollectParameterTypesVisitor extends SignatureVisitor {

	private final static Logger logger = LoggerFactory.getLogger(CollectParameterTypesVisitor.class);

	private final Set<Type> classes = new LinkedHashSet<Type>();

	public Set<Type> getClasses() {
		return classes;
	}

	/**
	 * @param api
	 */
	public CollectParameterTypesVisitor() {
		super(Opcodes.ASM4);
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		logger.info("  visitFormalTypeParameter(" + name + ")");
	}

	@Override
	public SignatureVisitor visitClassBound() {
		logger.info("  visitClassBound()");
		return this;
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		logger.info("  visitInterfaceBound()");
		return this;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		logger.info("  visitSuperclass()");
		return this;
	}

	@Override
	public SignatureVisitor visitInterface() {
		logger.info("  visitInterface()");
		return this;
	}

	@Override
	public SignatureVisitor visitParameterType() {
		logger.info("  visitParameterType()");
		return this;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.signature.SignatureVisitor#visitClassType(java.lang.String)
	 */
	@Override
	public void visitClassType(String name) {
		logger.info("  visitClassType(" + name + ")");
		classes.add(Type.getObjectType(name));
		super.visitClassType(name);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeVariable(java.lang.String)
	 */
	@Override
	public void visitTypeVariable(String name) {
		logger.info("  visitTypeVariable(" + name + ")");

		super.visitTypeVariable(name);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeArgument()
	 */
	@Override
	public void visitTypeArgument() {
		logger.info("  visitTypeArgument");
		super.visitTypeArgument();
	}
}
