package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.kautiainen.antti.infinitybot.model.RollResult;
import com.kautiainen.antti.infinitybot.model.Special;

class DiscordBotTest {

	@Test
	void testGetSpecial() {
		DiscordBot.Special vicious = new DiscordBot.Vicious(2);
		
	}

	@Test
	void testRollResult() {
		java.util.List<Object> roll = new ArrayList<Object>();
		roll.addAll(Arrays.asList(0, 1, 1, 2));
		roll.addAll(Arrays.asList("S", "S"));
		java.util.List<Special> specials = new ArrayList<>();
		specials.addAll(Arrays.asList(new DiscordBot.StackingSpecial("Effect", 2), new DiscordBot.Vicious(1)));

		RollResult result = new RollResult(5, roll, specials);
		System.out.println(result.toString());
		
		int expResult = 4+1+1; 
		assertEquals(expResult, result.getValue());
		
	}

	/**
	 * The class representing string lists. 
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class StringList extends ArrayList<String> {
		/**
		 * The generated serialization version.
		 */
		private static final long serialVersionUID = -1267004442987787669L;

		/**
		 * Create a new empty string list.
		 */
		public StringList() {
			super();
		}

		/**
		 * Create a new string list containing given strings.
		 * @param values
		 */
		public StringList(final Collection<String> values) {
			super(values);
		}
	}

	@Test
	void testWordPattern() {

		Pattern pattern = Pattern.compile("^" + DiscordBot.WORD_PATTERN + "$", Pattern.UNICODE_CHARACTER_CLASS);

		for (String tested : Arrays.asList("Vicious", "Auto-Bahn", "A", "\"This should work \\\"\"")) {
			assertTrue(pattern.matcher(tested).matches(), String.format("Value %s did not match", tested));
		}

		for (String tested : Arrays.asList("", " Auto", "Auto ", "A To", "\\\\\"")) {
			assertFalse(pattern.matcher(tested).matches());
		}
	}

	@Test
	void testLevelPattern() {
		Pattern pattern = Pattern.compile("^" + "(?:\\(([-+\\s]?\\d*)\\))?" + "$", Pattern.UNICODE_CHARACTER_CLASS);

		for (String tested : Arrays.asList("", "()", "(1)", "( 0)", "(+2)", "(-3)", "(002349250930523)")) {
			assertTrue(pattern.matcher(tested).matches(), String.format("Value %s did not match", tested));
		}

		for (String tested : Arrays.asList("(", "25", " Auto", "Auto ", "A To", "\\\\\"")) {
			assertFalse(pattern.matcher(tested).matches(), String.format("Value %s matched", tested));
		}

	}

	@Test
	void testSpecialPattern1() {
		Pattern pattern = Pattern.compile("^" + DiscordBot.WORD_PATTERN + "(?:\\(([-+\\s]?\\d*)\\))?" + "$",
				Pattern.UNICODE_CHARACTER_CLASS);
		for (String tested : Arrays.asList("Vicious", "Vicious()", "Vicious(1)", "Vicious( 0)", "Vicious(+2)",
				"Vicious(-3)", "Vicious(002349250930523)")) {
			assertTrue(pattern.matcher(tested).matches(), String.format("Value %s did not match", tested));
		}

		for (String tested : Arrays.asList("(", "", " Auto", "Auto ", "A To", "\\\\\"")) {
			assertFalse(pattern.matcher(tested).matches(), String.format("Value %s matched", tested));
		}

	}

	@Test
	void testSpecialPattern() {
		Pattern pattern = Pattern.compile("^" + DiscordBot.WORD_PATTERN + "(?:\\(([-+\\s]?\\d*)\\))?" + "$",
				Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher;
		TestCaseTemplate template = new TestCase<Object>();
		template.addParameterType(String.class, null); // The tested string.
		template.addParameterType(Boolean.class, null); // Does the pattern match.
		template.addParameterType(Integer.class, (Integer val) -> (val == null || val >= 0)); // The number of found patterns.
		template.addParameterType(List.class, (@SuppressWarnings("rawtypes") List list) -> {
			try {
				@SuppressWarnings({ "unchecked", "unused" })
				List<StringList> correctList = (List<StringList>) list;
				return true;
			} catch (ClassCastException cce) {
				return false;
			}
		}); // The list of pattern matches.
		List<TestCase<?>> testCases = new ArrayList<>();

		List<List<String>> testCaseResults = Arrays.asList(Arrays.asList("Vicious", "Vicious", (String) null),
				Arrays.asList("Vicious(1)", "Vicious", "1"), Arrays.asList("Vicious(2)", "Vicious", "2"),
				Arrays.asList("Blended-Ended(3)", "Blended-Ended", "3"));
		// Building test cases of full matching values.
		for (List<String> matchResults : testCaseResults) {
			TestCase<?> testCase = template.createTestCase();
			testCase.add(matchResults.get(0)); // TEsted pattern.
			testCase.add(true); // Does the pattern match.
			testCase.add(1); // The number of found groups.
			testCase.add(Arrays.asList(new StringList(matchResults)));
			testCases.add(testCase);
		}

		// Adding test cases of no match, but multiple finds.

		// Adding test cases of non-matching values without single find.
		for (String tested : Arrays.asList("", "   ")) {
			TestCase<?> testCase = template.createTestCase();
			testCase.add(tested); // TEsted pattern.
			testCase.add(false); // Does the pattern match.
			testCase.add(0); // The number of found groups.
			testCase.add(null);
			testCases.add(testCase);
		}

		// PErformign testing.
		List<List<String>> foundGroups = new ArrayList<>();
		for (TestCase<?> testCase : testCases) {
			boolean isMatch = false;
			int index = 0;
			Optional<String> value = testCase.getTypedValue(index++);
			Optional<Boolean> pass = testCase.getTypedValue(index++);
			Optional<Integer> matches = testCase.getTypedValue(index++);
			Optional<List<StringList>> expectedMatches = testCase.getTypedValue(index++);
			List<StringList> expectedFoundGroups = expectedMatches.orElse(Collections.emptyList());
			foundGroups.clear();
			if ((matcher = pattern.matcher(value.get())).matches()) {
				// We had perfect hit.
				List<String> groupList = new ArrayList<>();
				for (int i = 0, end = matcher.groupCount(); i <= end; i++) {
					groupList.add(matcher.group(i));
				}
				foundGroups.add(groupList);
				isMatch = true;
			} else {
				// Getting all hits.
				while (matcher.find()) {
					List<String> groupList = new ArrayList<>();
					for (int i = 0, end = matcher.groupCount(); i <= end; i++) {
						groupList.add(matcher.group(i));
					}
					foundGroups.add(groupList);
				}
			}

			// Performing tests.
			int matchCount = foundGroups.size();
			assertEquals(pass.get(), isMatch, String.format("\"%s\"Did not match ", value.get()));
			assertEquals(matches.get(), Integer.valueOf(matchCount),
					String.format("The match count differs for %s", value));
			if (matches.orElse(0) == matchCount) {
				// Testing the match results.
				for (int i = 0; i < matchCount; i++) {
					List<String> expected = expectedFoundGroups.get(i);
					List<String> found = foundGroups.get(i);
					assertEquals(expected, found,
							String.format("Found %2$s intead of %1$s", expected.toString(), found.toString()));
				}
			}

		}
	}
}
