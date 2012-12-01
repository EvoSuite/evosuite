package org.evosuite.rmi.service;

public enum ClientState {
	NOT_STARTED,
	STARTED,
	INITIALIZATION,
	SEARCH,
	MINIMIZATION,
	ASSERTION_GENERATION,
	DONE
}
