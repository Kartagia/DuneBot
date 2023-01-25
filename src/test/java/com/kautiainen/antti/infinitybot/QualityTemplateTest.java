package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kautiainen.antti.infinitybot.model.Quality;
import com.kautiainen.antti.infinitybot.model.QualityTemplate;

class QualityTemplateTest extends Logging {

	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		
		
	}

	@Test
	void testSetValue() {
		String name = "Tested"; 
		boolean stacks = true; 
		Function<Integer, Optional<Integer>> valueFunc = QualityTemplate.EMPTY_VALUE_FUNC;
		Integer min = null, max = null, old; 
		QualityTemplate tested;
		
		tested = new QualityTemplate(name, stacks, valueFunc, min, max); 
		try {
			old = tested.setValue(1); 
			fail("Invalid value was not violated");
		} catch(IllegalArgumentException iae) {
			
		}
		tested = new QualityTemplate(name, stacks, valueFunc, min, max); 
		try {
			old = tested.setValue(0); 
			old = tested.setValue((Integer)null);
		} catch(IllegalArgumentException iae) {
			fail("Invalid value when it should not be.");			
		}
		min=1; max = 3;
		tested = new QualityTemplate(name, stacks, valueFunc, min, max); 
		try {
			old = tested.setValue(1); 
			fail("Invalid value was not violated");
		} catch(IllegalArgumentException iae) {
			
		}
	}

	@Test
	void testQualityTemplateStringBooleanFunctionOfIntegerOptionalOfIntegerIntegerInteger() {
		TestCaseTemplate template = new TestCase<Object>(); 
		template.addParameterType(List.class, null);
		
		TestCase<?> success = template.createTestCase(Arrays.asList(
				"Test", 0, (Function<Integer, Optional<Integer>>)null, (Integer)null, (Integer)null));
		
	}

	@Test
	void testQualityTemplateSpecial() {
		fail("Not yet implemented");
	}

	@Test
	void testQualityTemplateFunctionalSpecial() {
		fail("Not yet implemented");
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetStackedInt() {
		// Testing construction of a new special from template. 
		TestCaseTemplate template = new TestCase<Object>(); 
		template.addParameterType(QualityTemplate.class, null);
		template.addParameterType(Integer.class, (Integer val)->val != null);
		template.addParameterType(Predicate.class, (@SuppressWarnings("rawtypes") Predicate pred) -> {
			try {
				@SuppressWarnings("unused")				
				Predicate<? super Quality> tester = (Predicate<? super Quality>)pred;
				return true; 
			} catch(ClassCastException cce) {
				return false;
			}
		});
		
		
		BiFunction<QualityTemplate, Integer, Predicate<Quality>> qualityTester = 
				(QualityTemplate source, Integer level) -> {
					if (source == null) {
						return (Quality quality)->(quality == null); 
					}
					return (Quality quality) -> {
						if (quality == null) {						
							debug("Undefined quality");
							
							return false; 
						} else if (!source.getName().equals(quality.getName())) {
							debug("Differetn names"); 
							
							return false; 
						} else if (quality.getValue() != (level==null?source.getDefaultLevel().orElse(1):level)) {
							debug("Different levels: Expected %d, got %d", level, quality.getValue()); 
							return false; 
						} else if (quality.stacks() != source.stacks()) {
							debug("Different stacking status"); 
							return false; 
						} else if (source.getValueFunction() != quality.getValueFunction()) {
							debug("Different valuet fnctions"); 
							return false; 
						} else  {
							return true; 
						}
					};
				};
		QualityTemplate source; 
		Quality result; 
		TestCase<?> test; 
		for (List<Object> args: Arrays.asList(
				Arrays.asList((source = new QualityTemplate("Tested")), 1, 
						qualityTester.apply(source, 1)), 
				Arrays.asList(source, 1, qualityTester.apply(source, 1)),
				Arrays.asList((source = new QualityTemplate("Stacking", true)), 1, 
						qualityTester.apply(source, 1)),
				Arrays.asList(source, 1, qualityTester.apply(source, 1)),
				Arrays.asList((source = new QualityTemplate("Stacking", true, 
						Quality.GetMultiplierValueFunction(1), 1, 10)), 1, 
						qualityTester.apply(source, 1)),
				Arrays.asList(source, 1, qualityTester.apply(source, 1))
				)) {
			test = template.createTestCase(args);
			int i=0; 
			source = (QualityTemplate)test.get(i++);
			Integer value = (Integer)test.get(i++); 
			Predicate<Quality> tester = (Predicate<Quality>)test.get(i++);
			result = source.createQuality(value);
			assertTrue(tester.test(result), 
					String.format("Testing failed: Expeted %s with value %d, got %s ",
							source, value, result));
		}
	}

	@Test
	void testValidQualityLevel() {
		fail("Not yet implemented");
	}

	@Test
	void testCreateQuality() {
		fail("Not yet implemented");
	}

}
