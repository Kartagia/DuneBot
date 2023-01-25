package com.kautiainen.antti.infinitybot.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

public class StringToolsTest {
    
    public static java.util.List<String> getValidIdentifiers() {
        return java.util.Arrays.asList("auto", "auto.length", "auto.size", "auto-bahn", "_auto", "_auto3bahn_3");
    }

    public static java.util.List<String> getInvalidIdentifiers() {
        return java.util.Arrays.asList("1auto", ".auto.length", "", (String)null, "-test");
    }
    
    @Test
    void testValidIdentifier() {
        for (String value: getValidIdentifiers()) {
            assertTrue("Value " + String.valueOf(value) + " was not valid identifier", StringTools.validIdentifier(value));
        }
        for (String value: getInvalidIdentifiers()) {
            assertFalse("Value " + String.valueOf(value) + " was valid identifier", StringTools.validIdentifier(value));
        }
    }

    @Test
    void testValidStatement() {

    }
}
