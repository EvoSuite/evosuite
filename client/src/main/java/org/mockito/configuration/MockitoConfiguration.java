package org.mockito.configuration;

/**
 * Class needed to be on classpath to configure Mockito, which cannot be
 * configured programmatically :(
 *
 * This is essential during search, but should NOT be part of standalone runtime.
 *
 * During search, the same SUT class can be loaded by different classloader (eg, search,
 * assertion generation, junit checks), and would lead to class cast exceptions because
 * Mockito does cache class definitions. So, we have to disable such behavior
 *
 * Created by Andrea Arcuri on 21/08/15.
 */
public class MockitoConfiguration extends DefaultMockitoConfiguration {

    @Override
    public boolean enableClassCache(){
        return false;
    }
}
