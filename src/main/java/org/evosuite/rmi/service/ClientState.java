package org.evosuite.rmi.service;

/**
 * FIXME: sync with ProgressMonitor, and finish set it in all client code
 * 
 * @author arcuri
 *
 */


public enum ClientState {
	NOT_STARTED,
	STARTED,
	INITIALIZATION,
	SEARCH,
	MINIMIZATION,
	ASSERTION_GENERATION,
	WRITING_STATISTICS,
	DONE
}
