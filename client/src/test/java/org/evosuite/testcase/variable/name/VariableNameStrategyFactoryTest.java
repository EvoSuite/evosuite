package org.evosuite.testcase.variable.name;

import org.evosuite.Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VariableNameStrategyFactoryTest {

    @Test
    void whenNullStrategy_thenIllegalArgumentExceptionIsExpected() {
        assertThrows(IllegalArgumentException.class, () -> VariableNameStrategyFactory.get(null));
    }

    @Test
    void whenTypeBasedStrategy_thenTypeBasedVariableNamingStrategyIsExpected() {
        VariableNameStrategy nameStrategy = VariableNameStrategyFactory.get(Properties.VariableNamingStrategy.TYPE_BASED);
        assertInstanceOf(TypeBasedVariableNameStrategy.class, nameStrategy);
    }

}