package com.examples.with.different.packagename;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * Snippet from Lang project
 * (org.apache.commons.lang3.ClassUtilsTest)
 */
public class ClassWithPrivateInterfacesTest {

	private static interface IA {
    }
    private static interface IB {
    }
    private static interface IC extends ID, IE {
    }
    private static interface ID {
    }
    private static interface IE extends IF {
    }
    private static interface IF {
    }
    private static class CX implements IB, IA, IE {
    }
    private static class CY extends CX implements IB, IC {
    }

    @Test
	public void testGetAllInterfaces() {
		final List<Class<?>> list = ClassWithPrivateInterfaces.getAllInterfaces(CY.class);

		assertEquals(6, list.size());
        assertEquals(IB.class, list.get(0));
        assertEquals(IC.class, list.get(1));
        assertEquals(ID.class, list.get(2));
        assertEquals(IE.class, list.get(3));
        assertEquals(IF.class, list.get(4));
        assertEquals(IA.class, list.get(5));

        assertEquals(null, ClassWithPrivateInterfaces.getAllInterfaces(null));
	}
}
