package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kautiainen.antti.infinitybot.model.DiceRoller;
import com.kautiainen.antti.infinitybot.model.FunctionalSpecial;
import com.kautiainen.antti.infinitybot.model.Quality;
import com.kautiainen.antti.infinitybot.model.RollResult;
import com.kautiainen.antti.infinitybot.model.Special;

class DiceRollerTest {

	private List<TestCase<?>> testCases = new ArrayList<>();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	private static int cursor = 0;

	@SuppressWarnings("unused")
	private static final int BASE = cursor++;
	@SuppressWarnings("unused")
	private static final int ROLL = cursor++;
	private static final int SPECIALS = cursor++;
	private static final int SPECIALS_VALUE = cursor++;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() throws Exception {
		TestCaseTemplate caseTemplate = new TestCase<Object>();
		caseTemplate.addParameterType(Integer.class, null); // the base
		caseTemplate.addParameterType(List.class, (@SuppressWarnings("rawtypes") final List list) -> {
			return list.stream().allMatch((Object obj) -> (obj instanceof Integer || obj instanceof String));
		});
		caseTemplate.addParameterType(List.class, (@SuppressWarnings("rawtypes") List list) -> {
			return list != null && list.stream().allMatch((Object obj) -> (obj instanceof Special));
		});
		caseTemplate.addParameterType(Integer.class, null); // The value of specials.

		List<Special> specials = new ArrayList<>();
		specials.add(new DiscordBot.Vicious(1));
		specials.add(new DiscordBot.Special("Piercing", 1));
		specials.add(new DiscordBot.Special("Unhackable", null));

		for (List<? extends Object> list : Arrays.asList(Arrays.asList(2, Arrays.asList(1, 1, 2, 0, "S", "S"), specials, 1),
				Arrays.asList(2, Arrays.asList(1, 1, 2, 0, "S", "S"), null, 0),
				Arrays.asList(2, Arrays.asList(1, 1, 2, 0, "S", "S"),
						Arrays.asList(new DiscordBot.Special("Effect", 1)), 0),
				Arrays.asList(2, Arrays.asList(1, 1, 2, 0, "S", "S"),
						Arrays.asList(new FunctionalSpecial("Effect", 1, 3)), 3))) {
			TestCase<? extends Object> testCase = caseTemplate.createTestCase(list);
			testCases.add(testCase);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetSpecialsValue() throws ClassCastException {
		DiceRoller roller = new DiceRoller();
		for (TestCase<?> testCase : testCases) {
			Integer expResult = (Integer) testCase.getTypedValue(SPECIALS_VALUE).orElse(0);
			List<Special> source = (List<Special>) testCase.getTypedValue(SPECIALS).orElse(Collections.emptyList());
			int result = roller.getSpecialsValue(source);
			assertEquals(expResult, result);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	void testBinarySearch() {
		TestCase<?> template = new TestCase<Object>();
		template.addParameterType(List.class, (Object list) -> {
			try {
				@SuppressWarnings("unused")
				List<Integer> tested = (List<Integer>) list;
				return true;
			} catch (Exception e) {
				return false;
			}
		}); // The ordered list.
		template.addParameterType(List.class, (Object list) -> {
			try {
				@SuppressWarnings("unused")
				List<Integer> tested = (List<Integer>) list;
				return true;
			} catch (Exception e) {
				return false;
			}
		}); // The added elements.
		template.addParameterType(List.class, (Object list) -> {
			try {
				@SuppressWarnings("unused")
				List<Integer> tested = (List<Integer>) list;
				return true;
			} catch (Exception e) {
				return false;
			}
		}); // The expected results.
		java.util.List<TestCase<?>> testCases = new ArrayList<>();
		for (List<? extends Object> list : Arrays.asList(Arrays.asList(
				new ArrayList<Integer>(Arrays.asList(1, 3, 5, 7)), new ArrayList<Integer>(Arrays.asList(1, 1, 1, 2, 3)),
				new ArrayList<Integer>(Arrays.asList(0, 0, 0, -2, 1))))) {
			TestCase<?> testCase = template.createTestCase(new ArrayList<Object>(list));
			testCases.add(testCase);
		}

		// Testing.
		for (TestCase<?> testCase : testCases) {
			List<Integer> target = (List<Integer>) testCase.getTypedValue(0).orElse(Collections.emptyList());
			List<Integer> source = (List<Integer>) testCase.getTypedValue(1).orElse(Collections.emptyList());
			List<Integer> expResults = (List<Integer>) testCase.getTypedValue(2).orElse(Collections.emptyList());
			Integer result, expResult;
			for (int i = 0, len = Math.min(source.size(), expResults.size()); i < len; i++) {
				expResult = expResults.get(i);
				result = Collections.binarySearch(target, source.get(i));
				assertEquals(expResult, result);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	void testBinarySearchOfSpecial() {
		TestCase<?> template = new TestCase<Object>();
		Class<?> type = List.class, contentType = Special.class;
		Predicate<Object> validator = template.getListValidator(contentType);
		template.addParameterType(type, validator);
		template.addParameterType(type, validator);
		contentType = Integer.class;
		validator = template.getListValidator(contentType);
		template.addParameterType(type, validator); //

		java.util.List<TestCase<?>> testCases = new ArrayList<>();
		for (List<? extends Object> list : Arrays.asList(
				Arrays.asList(new ArrayList<Special>(Arrays.asList()),
				new ArrayList<Special>(Arrays.asList(new DiscordBot.Special("Nonhackable", null),
						new DiscordBot.Special("Effect", 1), new DiscordBot.Vicious(2))),
				new ArrayList<Integer>(Arrays.asList(-1, -1, -1))),
				Arrays.asList(
						new ArrayList<Special>(Arrays.asList(new DiscordBot.Special("Divisor", null))),
						new ArrayList<Special>(Arrays.asList(
								new DiscordBot.Special("Nonhackable", null),
								new DiscordBot.Special("Effect", 1), 
								new DiscordBot.Special("Divisor", 1), 
								new DiscordBot.Vicious(2), 
								new DiscordBot.Special("Alpha", null))),
						new ArrayList<Integer>(Arrays.asList(-2, -2, 0, -2, -1)))
				)) {
			TestCase<?> testCase = template.createTestCase(new ArrayList<Object>(list));
			testCases.add(testCase);
		}

		// Testing.
		for (TestCase<?> testCase : testCases) {
			List<Special> target = (List<Special>) testCase.getTypedValue(0).orElse(Collections.emptyList());
			List<Special> source = (List<Special>) testCase.getTypedValue(1).orElse(Collections.emptyList());
			List<Integer> expResults = (List<Integer>) testCase.getTypedValue(2).orElse(Collections.emptyList());
			Integer result, expResult;
			for (int i = 0, len = Math.min(source.size(), expResults.size()); i < len; i++) {
				expResult = expResults.get(i);
				result = Collections.binarySearch(target, source.get(i));
				assertEquals(expResult, result);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	void testCombineSpecials() {
		DiceRoller roller = new DiceRoller();
		TestCase<?> template = new TestCase<Object>() {

			@Override
			public Class<?> getDefaultClass() {
				return List.class;
			}

			@Override
			public Predicate<Object> getDefaultPredicate() {
				return (Object obj) -> {
					return (obj instanceof List)
							&& ((List<?>) obj).stream().allMatch((Object val) -> (val instanceof Special));
				};
			}
		};
		template.addParameterType(template.getDefaultClass(), template.getDefaultPredicate()); // the source.
		template.addParameterType(template.getDefaultClass(), template.getDefaultPredicate()); // the expected result.

		List<TestCase<?>> testCases = new ArrayList<>();
		testCases.add(template.createTestCase(Arrays.asList(
				Arrays.asList(new DiscordBot.Special("NonStack", null), new DiscordBot.Special("NonStack", null),
						new DiscordBot.Special("Nonhackable", 1, false),
						new DiscordBot.Special("Nonhackable", 1, false)),
				Arrays.asList(new DiscordBot.Special("NonStack", null),
						new DiscordBot.Special("Nonhackable", 1, false)))));

		testCases.add(template.createTestCase(Arrays.asList(
				Arrays.asList(new DiscordBot.Special("Stack", 1), new DiscordBot.Special("Stack", 2),
						new DiscordBot.Special("Nonhackable", 1, false),
						new DiscordBot.Special("Nonhackable", 1, false)),
				Arrays.asList(new DiscordBot.Special("Stack", 3), new DiscordBot.Special("Nonhackable", 1, false)))));

		for (TestCase<?> testCase : testCases) {
			List<Special> source = (List<Special>) testCase.getTypedValue(0).orElse(Collections.emptyList());
			List<Special> expResult = new ArrayList<>((List<Special>) testCase.getTypedValue(1).orElse(Collections.emptyList()));
			Collections.sort(expResult);
			List<Special> result = roller.combineSpecials(source);

			assertEquals(expResult, result);
		}
	}

	@Test
	void testAddSpecialListOfSpecialListOfSpecial() {
		fail("Not yet implemented");
	}

	@Test
	void testAddSpecialListOfSpecialSpecial() {
		DiceRoller roller = new DiceRoller(); 
		List<Special> result = new ArrayList<Special>(), expResult = 
				new ArrayList<Special>(); 
		List<Special> addedValues = Arrays.asList(
				new DiscordBot.Special("Nonhackable", null),
				new DiscordBot.Special("Effect", 1), 
				new DiscordBot.Special("Divisor", 1), 
				new DiscordBot.Vicious(2), 
				new DiscordBot.Special("Alpha", null), 
				new DiscordBot.Special("Alpha", null), 
				new DiscordBot.Vicious(2));
		for (Special added: addedValues) {
			int index = Collections.binarySearch(expResult, added, 
					(Special a, Special b)->(a.getName().compareTo(b.getName()))
					); 
			System.out.printf("Adding %s to %s\n", added, result); 
			if (index >= 0) {
				// A value is found - doing nothing. 
				expResult.set(index, result.get(index).getStacked(added.getValue()));
			} else {
				index = -1 - index;
				expResult.add(index, added);
			}
			roller.addSpecial(result, added);
			System.out.printf("Result %s\n", result); 
			assertEquals(expResult, result); 
		}
	}

	@Test
	void testGetCDRollResultIntListOfObjectListOfSpecial() {
		fail("Not yet implemented");
	}

	@Test
	void testGetCDRollResultIntListOfObjectListOfSpecialInt() {
		DiceRoller roller = new DiceRoller();
		java.util.List<Object> roll; 
		java.util.List<Special> specials, expSpecials; 
		String caption;
		RollResult result, expResult;
		int specialTotal, base, expResultValue, specialCount=0; 
		
	
		// Teste case 1: 	
		base = 0; 
		roll = new ArrayList<>(); 
		specialCount = 0; 
		expResultValue = 0; 
		specials = new ArrayList<Special>(
				Arrays.asList(
						new Quality("Stacking", 1, true), 
						new Quality("Non-Stacking", 2, false))); 
		specialTotal = 0; 
		caption =
				String.format("Test base %d with rolll %s and special total %d",
				base, roll, specialTotal); 
		expSpecials = new ArrayList<>(); 
		expResultValue += base + specialTotal*specialCount; 
		result = roller.getCDRollResult(base, roll, new ArrayList<>(specials), specialTotal); 
		expResult = new RollResult(expResultValue, roll, expSpecials);
		assertEquals(expResult, result, 
				String.format("Values are not equal\n\tExcpected: %s\n\tGot: %s\n", 
						expResult, result));
		
		base = 0; 
		roll.addAll(Arrays.asList(1, 2));
		caption =
				String.format("Test base %d with rolll %s and special total %d",
				base, roll, specialTotal); 
		expSpecials = new ArrayList<>(); 
		expResultValue = 3; 
		expResultValue += base + specialTotal*specialCount; 
		result = roller.getCDRollResult(base, roll, new ArrayList<>(specials), specialTotal); 
		expResult = new RollResult(expResultValue, roll, expSpecials);
		assertEquals(expResult, result, 
				String.format("Test %s\n\t%s\n", caption,  
				String.format("Values are not equal\n\tExcpected: %s\n\tGot: %s\n", 
						expResult, result)));

		roll.addAll(Arrays.asList("S", 0, 2));
		caption =
				String.format("Test base %d with rolll %s and special total %d",
				base, roll, specialTotal); 
		expResultValue += 2 - base - specialCount*specialTotal; 
		specialCount++;  
		expSpecials = specials = new ArrayList<Special>(
				Arrays.asList(
						new Quality("Non-Stacking", 2, false),
						new Quality("Stacking", 1, true)
						));
		Collections.sort(expSpecials);
		expResultValue += base + specialTotal*specialCount; 
		result = roller.getCDRollResult(base, roll, new ArrayList<>(specials), specialTotal); 
		expResult = new RollResult(expResultValue, roll, expSpecials);
		assertEquals(expResult, result, 
				String.format("Test %s\n\t%s\n", caption,  
				String.format("Values are not equal\n\tExcpected: %s\n\tGot: %s\n", 
						expResult, result)));

		// TEstign with special total value of 1. 
		expResultValue += 0 - base - specialCount*specialTotal; 
		specialTotal = 1; 
		expResultValue = 5; 
		result = roller.getCDRollResult(base, roll, new ArrayList<>(specials), specialTotal); 
		expResultValue += base + specialTotal*specialCount; 
		expResult = new RollResult(expResultValue, roll, expSpecials);
		assertEquals(expResult, result, 
				String.format("Test %s\n\t%s\n", caption,  
				String.format("Values are not equal\n\tExcpected: %s\n\tGot: %s\n", 
						expResult, result)));
		expResultValue -= specialCount*specialTotal; 
		specialTotal = 0; 
		

		roll.addAll(Arrays.asList("S"));
		expResultValue += 0 - base - specialCount*specialTotal; 
		specialCount++; 
		specials = new ArrayList<Special>(
				Arrays.asList(
						new Quality("Stacking", 2, true), 
						new Quality("Non-Stacking", 2, false)));
		expSpecials = new ArrayList<Special>(
				Arrays.asList(
						new Quality("Stacking", 4, true), 
						new Quality("Non-Stacking", 2, false)));
		
		Collections.sort(expSpecials);
		expResultValue += base + specialTotal*specialCount; 
		result = roller.getCDRollResult(base, roll, new ArrayList<>(specials), specialTotal); 
		expResult = new RollResult(expResultValue, roll, expSpecials);
		assertEquals(expResult, result, 
				String.format("Test %s\n\t%s\n", caption,  
				String.format("Values are not equal\n\tExcpected: %s\n\tGot: %s\n", 
						expResult, result)));

	}
		

	@Test
	void testFormatRoll() {
		fail("Not yet implemented");
	}

}
