package org.evosuite.setup;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 6/14/14.
 */
public class InheritanceTreeGeneratorTest {

    @Test
    public void canFindJDKData(){
        InheritanceTree it = InheritanceTreeGenerator.readJDKData();
        Assert.assertNotNull(it);
    }

}
