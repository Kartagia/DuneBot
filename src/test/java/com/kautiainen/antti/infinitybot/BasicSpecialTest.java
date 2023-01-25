package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import com.kautiainen.antti.infinitybot.model.BasicSpecial;

/**
 * Tester testing BasicSpecial functionality.
 * @author Antti Kautiaienn
 *
 */
class BasicSpecialTest extends Logging {

	private TestCase<?> testCase; 
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testSetFrom() {
		TestCaseTemplate template = new TestCase<Object>();
		template.addParameterType(String.class, null);
		template.addParameterType(Matcher.class, null);
		template.addParameterType(BasicSpecial.class, null);
		template.addParameterType(Class.class, null);
		template.addParameterType(Predicate.class, null);
		BasicSpecial instance = new BasicSpecial() {
		}; 
		String source; 
		for (List<? extends Object> testArgs: Arrays.asList(
				Arrays.asList(
						(source = "Test(3)"),
						instance.getFromStringPattern().matcher(source),
						new BasicSpecial("Test", true, 3, null), 
						null,
						null),
				Arrays.asList(
						(source = "Test(3=1)"),
						instance.getFromStringPattern().matcher(source), new BasicSpecial("Test", true, 3, Optional.of(1)),
				null, 
				null),
				Arrays.asList(
						source = "",
						instance.getFromStringPattern().matcher(source), null, 
				IllegalArgumentException.class, 
				(Predicate<Exception>)(Exception e)->{
					return  e != null && 
							IllegalArgumentException.class.isAssignableFrom(
									e.getClass()); 
				})
				)) {
			TestCase<?> tester = template.createTestCase(testArgs);
			int index = 0; 
			Optional<String> matched = tester.getTypedValue(index++);
			Optional<Matcher> testSource = tester.getTypedValue(index++);
			Optional<BasicSpecial> expResult = tester.getTypedValue(index++);
			Optional<Class<? extends Throwable>> expException = tester.getTypedValue(index++);
			Optional<Predicate<? super Throwable>> exceptionTester = tester.getTypedValue(index++); 
			debug("Testing match on %s", matched.orElse(null)); 
			if (expException.isPresent()) {
				Throwable result = assertThrows(expException.orElse(null), 
						()->{
							instance.setFrom(testSource.orElse(null)); 
						});
				if (exceptionTester.isPresent()) {
					assertTrue(exceptionTester.get().test(result),
							String.format("Invalid exception %s thrown!", result.getClass().toString()));
				}
			} else {
				try {
					instance.setFrom(testSource.get());
				} catch(Exception e) {
					throw new AssertionFailedError(String.format("Test source %s did not match", testSource), e);
				}
				assertEquals(instance, expResult.orElse(null)); 
			}
		}
	
	}

	@Test
	void testBasicSpecialString() {
		TestCaseTemplate template = new TestCase<Object>(); 
		template.addParameterType(String.class, null);
		template.addParameterType(BasicSpecial.class, null);
		template.addParameterType(Predicate.class, null);
		for (List<? extends Object> testArgs: Arrays.asList(
				Arrays.asList("", new BasicSpecial(""), 
						(Predicate<Exception>)(Exception e)->{
							return  e != null && 
									IllegalArgumentException.class.isAssignableFrom(
											e.getClass()); 
						}),
				Arrays.asList(null, null, 
						(Predicate<Exception>)(Exception e)->{
							return  e != null && 
									IllegalArgumentException.class.isAssignableFrom(
											e.getClass()); 
						}),
				Arrays.asList("Test", new BasicSpecial("Test", null, null, null), 
						null)
				)) {
			TestCase<?> tester = template.createTestCase(testArgs);
			int index = 0; 
			Optional<String> source = tester.getTypedValue(index++);
			Optional<BasicSpecial> expResult = tester.getTypedValue(index++);
			Optional<Predicate<? super Throwable>> expException = tester.getTypedValue(index++); 
			if (expException.isPresent()) {
				Throwable result = assertThrows(Throwable.class, 
						()->{
							BasicSpecial obj = new BasicSpecial(source.orElse(null));
							fail("Did not throw exception, but got " + obj.toString()); 
						});
				if ((result instanceof AssertionFailedError) && !expException.get().equals(result)) {
					throw (AssertionFailedError)result; 
				}
				assertTrue(expException.get().test(result), 
						String.format("Invalid exception %s thrown!", result.getClass().toString()));
			} else {
				BasicSpecial result = new BasicSpecial(source.orElse(null));
				assertEquals(result, expResult.orElse(null)); 
			}
		}
	}

	@Test
	void testBasicSpecialStringBooleanIntegerOptionalOfInteger() {
		fail("Not yet implemented");
	}

	@Test
	void testValidValue() {
		fail("Not yet implemented");
	}

	@Test
	void testValidNumberValue() {
		boolean expResult = true, result; 
		Optional<Integer> empty = Optional.empty();
		BasicSpecial special = new BasicSpecial() {
			
		};
		for (Optional<Integer> name: Arrays.asList(empty, Optional.of(0), Optional.of(-5), Optional.of(25))) {
			result = special.validNumberValue(name); 
			assertEquals(expResult, result, String.format("Expected %s, but got %s", expResult, result)); 
		}
		expResult = false;
		for (Optional<Integer> name: Arrays.asList((Optional<Integer>)null)) {
			result = special.validNumberValue(name); 
			assertEquals(expResult, result, String.format("Expected %s, but got %s", expResult, result)); 
		}
	}

	@Test
	void testValidName() {
		boolean expResult = true, result; 
		BasicSpecial special = new BasicSpecial() {
			
		};
		for (String name: Arrays.asList("Vicious", "Piercing","Nonhackable")) {
			result = special.validName(name); 
			assertEquals(expResult, result, String.format("Expected %s, but got %s", expResult, result)); 
		}
		expResult = false;
		for (String name: Arrays.asList(null, "", "Vicious Bitch", "Piercing Whale "," Nonhackable")) {
			result = special.validName(name); 
			assertEquals(expResult, result, String.format("Expected %s, but got %s", expResult, result)); 
		}
	}

	@Test
	void testGetNameFromStringPattern() {
		boolean expResult = true, result; 
		BasicSpecial special = new BasicSpecial() {}; 
		debug("%nMatching successes%n", (Object[])null); 
		for (String name: Arrays.asList("Vicious", "Piercing","Nonhackable")) {
			debug("Matching %s", name); 
			Matcher matcher =special.getNameFromStringPattern().matcher(name); 
			result = matcher.matches();  
			assertEquals(expResult, result, String.format("Expected %s, but got %s", expResult, result)); 
		}
		debug("%nMatching failures%n", (Object[])null); 
		expResult = false;
		for (String name: Arrays.asList(null, "Vicious Bitch", "Piercing Whale "," Nonhackable")) {
			debug("Matching %s", name); 
			Class<? extends Throwable> exception = (name==null?NullPointerException.class:IllegalArgumentException.class);
			final boolean myExpResult = expResult; 
			assertThrows(exception, 
					()->{
						Matcher matcher =special.getNameFromStringPattern().matcher(name); 
						boolean matches = matcher.matches();  
						assertEquals(myExpResult, matches, String.format("Expected %s, but got %s", myExpResult, matches)); 						
					}); 
		}
		
	}
	
	@Test
	void testGetName() {
		fail("Not yet implemented");
	}

	@Test
	void testSetName() {
		fail("Not yet implemented");
	}

	@Test
	void testGetValue() {
		fail("Not yet implemented");
	}

	@Test
	void testSetValueInteger() {
		fail("Not yet implemented");
	}

	@Test
	void testSetValueString() {
		fail("Not yet implemented");
	}

	@Test
	void testSetStacksBoolean() {
		fail("Not yet implemented");
	}

	@Test
	void testSetStacksString() {
		fail("Not yet implemented");
	}

	@Test
	void testSetNumberValueOptionalOfInteger() {
		fail("Not yet implemented");
	}

	@Test
	void testSetNumberValueString() {
		fail("Not yet implemented");
	}

	@Test
	void testStacks() {
		fail("Not yet implemented");
	}

	@Test
	void testGetNumberValue() {
		fail("Not yet implemented");
	}

	@Test
	void testToString() {
		fail("Not yet implemented");
	}

}
