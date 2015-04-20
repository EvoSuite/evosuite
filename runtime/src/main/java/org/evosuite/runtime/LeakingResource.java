package org.evosuite.runtime;

/**
 * Even in the case of virtual/mock objects, there might be resources that
 * need to be deallocated after the search if the SUT does not do it
 * 
 * @author arcuri
 *
 */
public interface LeakingResource {

	/**
	 * Release this resource
	 * 
	 * @throws Exception
	 */
	public void release() throws Exception;
	
}
