package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kautiainen.antti.infinitybot.model.Special;

/**
 * Test testing regular expresions of the Specials.
 * 
 * @author kautsu
 *
 */
class SpecialRegexpTest {

	public final java.util.List<TestCase<?>> testCases = new java.util.ArrayList<>();

	public final java.util.List<TestCaseTemplate> testCaseTemplates = new java.util.ArrayList<>();

	protected Special instance = new Special() {

		@Override
		public String getName() {
			return "TestInstance";
		}

		@Override
		public int getValue() {
			return 1;
		}

		@Override
		public Special getStacked(int value) {
			return SpecialTest.createSpecial(this, this.getName(), getValue() + (stacks() ? value : 0));
		}

		@Override
		public Optional<Integer> getNumberValue() {
			return Special.super.getNumberValue();
		}

		@Override
		public boolean stacks() {
			return Special.super.stacks();
		}

	};

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {

		TestCaseTemplate template = new TestCase<Object>();
		template.addParameterType("The matched string", String.class, null);
		template.addParameterType("Does the string match", Boolean.class, null);
		template.addParameterType("The list of groups", java.util.List.class, null);
		template.addParameterType("Predicate testing the matcher", Predicate.class, null);
		template.setDefaultException(IllegalArgumentException.class);
		template.setDefaultExpectedResult(Boolean.class,
				(Predicate<Boolean>) (Boolean test) -> (test != null && test.booleanValue()));
		this.testCaseTemplates.add(template);
	}

	public java.util.Collection<TestCaseTemplate> getTemplates() {
		return Collections.unmodifiableCollection(this.testCaseTemplates);
	}

	public java.util.Collection<TestCase<?>> getTestCases() {
		return Collections.unmodifiableCollection(this.testCases);
	}

	public Predicate<Matcher> getMatcherTester(boolean matches) {
		return (Matcher matcher) -> (matcher != null && matcher.matches() == matches);
	}

	public List<String> getMatcherGroups(Matcher matcher) {
		List<String> result = new ArrayList<>();
		if (matcher != null && matcher.matches()) {
			for (int i = 0, len = matcher.groupCount(); i <= len; i++) {
				result.add(matcher.group(i));
			}
		}
		return result;
	}

	/**
	 * Create a matcher tester with given group values.
	 * 
	 * @param groupValues
	 * @return
	 */
	public Predicate<Matcher> getMatcherTester(List<? extends CharSequence> groupValues) {
		if (groupValues == null || groupValues.isEmpty()) {
			return (Matcher matcher) -> (matcher != null && !matcher.matches());
		} else {
			return (Matcher matcher) -> {
				return matcher != null && matcher.matches() && groupValues.equals(getMatcherGroups(matcher));
			};
		}
	}

	/**
	 * Create a new map from collection of keys and values.
	 * 
	 * @param <KEY>   The key type of the created map.
	 * @param <VALUE> The value type of the created map.
	 * @param keys    The keys.
	 * @param values  The values of each key.
	 * @throws IllegalArgumentException The given values or keys are invalid.
	 */
	public static <KEY, VALUE> Map<KEY, VALUE> createMap(Collection<? extends KEY> keys,
			Collection<? extends VALUE> values) throws IllegalArgumentException {
		if (keys == null)
			throw new IllegalArgumentException("Invalid keys", new NullPointerException());
		if (values == null)
			throw new IllegalArgumentException("Invalid values", new NullPointerException());
		java.util.Iterator<? extends VALUE> valueIter = values.iterator();
		java.util.Iterator<? extends KEY> keyIter = keys.iterator();
		Map<KEY, VALUE> result = new java.util.TreeMap<>();
		try {
			while (valueIter.hasNext() && keyIter.hasNext()) {
				result.put(keyIter.next(), valueIter.next());
			}
		} catch (NullPointerException | ClassCastException cce) {
			throw new IllegalArgumentException("Invalid keys", cce);
		}
		if (keyIter.hasNext()) {
			throw new IllegalArgumentException("Invalid values", new NullPointerException());
		}
		return result;
	}

	/**
	 * Create a new map from collection of keys and values.
	 * 
	 * @param <KEY>   The key type of the created map.
	 * @param <VALUE> The value type of the created map.
	 * @param keys    The keys of the created map.
	 * @param values  The values of each key. The values has to have at least as
	 *                many elements as key list.
	 * @return The map with all keys of the key list with value of corresponding
	 *         value. If the keys list contains duplicates, the value of the key is
	 *         the value of last key-value-pair.
	 * @throws IllegalArgumentException The given values or keys are invalid.
	 */
	public static <KEY, VALUE> Map<KEY, VALUE> createMap(List<? extends KEY> keys, List<? extends VALUE> values)
			throws IllegalArgumentException {
		if (keys == null)
			throw new IllegalArgumentException("Invalid keys", new NullPointerException());
		if (values == null)
			throw new IllegalArgumentException("Invalid values", new NullPointerException());
		if (keys.size() > values.size())
			throw new IllegalArgumentException("Invalid values",
					new IllegalArgumentException("Values have too few elements"));

		// TODO: add possiblity to create immutable
		Map<KEY, VALUE> result = new java.util.TreeMap<>();
		try {
			for (int i = 0, end = keys.size(); i < end; i++) {
				result.put(keys.get(i), values.get(i));
			}
		} catch (NullPointerException | ClassCastException cce) {
			throw new IllegalArgumentException("Invalid keys", cce);
		}
		return result;
	}

	/**
	 * Create matcher tester testing given test values.
	 * 
	 * @param groupTesters The mapping from group names to group testers. Every
	 *                     tester should be defined.
	 * @return The predicate testing matcher to have all groups of the group tester,
	 *         if the matcher matches.
	 */
	public Predicate<Matcher> getMatcherTester(Map<String, Predicate<CharSequence>> groupTesters) {
		return (Matcher matcher) -> {
			// Matcher matches every group of the given tested group. A missing group
			// will cause throwing of exception NoSuchElementException.
			return matcher != null && matcher.matches() && (groupTesters == null || (groupTesters.entrySet().stream()
					.allMatch((Map.Entry<String, Predicate<CharSequence>> entry) -> {
						return entry.getValue().test(matcher.group(entry.getKey()));
					})));
		};
	}

	@Test
	void testPatternToStringPatternBoolean() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testPatternToStringPattern() {

	}

	@Test
	void testValueFromStringPattern() {
		TestCase<Object> testCase;
		final Pattern pattern = this.instance.getFromStringPattern();
		Function<List<? extends Object>, Boolean> operation = (List<? extends Object> parameters) -> {
			int index = 0; 
			String source = (String)parameters.get(index++);
			Boolean matches = (Boolean)parameters.get(index++); 
			Predicate<? super Matcher> tester = (Predicate<? super Matcher>)parameters.get(index++); 
			Matcher matcher = pattern.matcher(source);
			return matcher.matches() == matches && tester.test(matcher); 
		};
		for (TestCaseTemplate template : getTemplates()) {
			testCase = template.createTestCase("ValueFromStrinPattern");
			testCase.setOperation(operation);
			assertTrue(testCase.excecute(), 
					String.format("Test case %s failed due %s", testCase.getTitle(), testCase.error()));
		}
	}

	@Test
	void testGetNameFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testGetValueFieldsFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testFromStringPatternPatternPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testGetFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testNameFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testValueFieldsFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testStackingFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testValueFieldFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	void testNumericValueFromStringPattern() {
		fail("Not yet implemented"); // TODO
	}

}
