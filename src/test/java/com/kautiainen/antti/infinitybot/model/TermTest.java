package com.kautiainen.antti.infinitybot.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class TermTest {
    
    @SuppressWarnings("unchecked")
    protected static <TYPE> Term<TYPE> getTerm(Term<?> term) throws ClassCastException {
        return (Term<TYPE>)term;
    }

    protected static <TYPE> Optional<TYPE> getTermDefaultValue(Term<?> term) throws ClassCastException {
        Term<TYPE> result = getTerm(term);
        return result.getDefaultValue();
    }

    @SuppressWarnings("unchecked")
    protected static <TYPE> Optional<TYPE> getDefaultValue(Optional<?> value) throws ClassCastException {
        return (Optional<TYPE>)value;
    }

    public static final List<List<?>> getTestCases() {
        return Arrays.asList(
        (List<?>)Arrays.asList("Null default", (Term<String>)new Term<String>("Testi", TermTest.class.getName(), (Optional<String>)null), (String)null)
        ,(List<?>)Arrays.asList("Empty default", (Term<String>)new Term<String>("Testi", TermTest.class.getName(), Optional.empty()), Optional.empty())
    );
    }
    
    @Test
    void testGetActualDefaultValue() {

        Term<?> instance;
        Optional<?> expResult, result;

        // Test cases
        String testName;
        int index;
        for (List<?> testValues: getTestCases()) {
            index = 0;
            testName = testValues.get(index++).toString();
            System.out.println("Test " + testName + ":");
            instance = (Term<?>)testValues.get(index++);
            expResult = (Optional<?>)testValues.get(index++);
            result = instance.getActualDefaultValue();
            assertEquals(expResult, result);
        }

    }

    @Test
    void testGetDefaultValue() {

        Term<?> instance;
        Optional<?> expResult, result;

        String testName;
        int index;
        for (List<?> testValues: getTestCases()) {
            index = 0;
            testName = testValues.get(index++).toString();
            System.out.println("Test " + testName + ":");
            instance = (Term<?>)testValues.get(index++);
            expResult = (Optional<?>)testValues.get(index++);
            if (expResult == null) expResult = Optional.empty();
            result = instance.getDefaultValue();
            assertEquals(expResult, result);
        }
    }

    @Test
    void testGetName() {

    }

    @Test
    void testGetPropertyName() {

    }

    @Test
    void testHasDefaultValue() {

    }

    @Test
    void testValidName() {

        

    }

    @Test
    void testValidPropertyName() {
        Term<?> instance;
        Boolean expResult, result;
        String name;

        name = null;
        instance = new Term<String>("Testi", name, Optional.empty());
        expResult = true;
        result = instance.validPropertyName(name);


        name = "testi";
        instance = new Term<String>("Testi", name, Optional.empty());
        expResult = true;
        result = instance.validPropertyName(name);
    }

    @Test
    void testValidValue() {

    }

    @Test
    void testValidValue2() {

    }
}
