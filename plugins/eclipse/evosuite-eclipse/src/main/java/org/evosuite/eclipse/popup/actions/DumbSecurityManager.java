/**
 * 
 */
package org.evosuite.eclipse.popup.actions;

import java.security.Permission;

/**
 * @author Gordon Fraser
 * 
 */
public class DumbSecurityManager extends SecurityManager {

	public void checkPermission() {
	}

	@Override
	public void checkPermission(Permission perm) {
	}

	@Override
	public void checkPermission(Permission perm, Object context) {
	}

}
