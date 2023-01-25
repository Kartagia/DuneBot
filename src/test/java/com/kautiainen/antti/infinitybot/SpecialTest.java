package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kautiainen.antti.infinitybot.model.FunctionalSpecial;
import com.kautiainen.antti.infinitybot.model.Special;

class SpecialTest {

	public SpecialRegistry registry = new SpecialRegistry();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {

	}
	
	public static Special createStackingSpecial(String name) {
		return createSpecial(name, 1);
	}

	
	
	/**
	 * Get special with given name. 
	 * @param name The name of the special.
	 * @return If there is a registered special with the name, that
	 *  special is returned. Otherwise returns a new special with default
	 *  value.
	 * @throws NoSuchElementException The default value does not exists. 
	 */
	public Special getSpecial(String name) {
		if (registry.containsKey(name)) {
			return registry.get(name);
		} else {
			return createSpecial(name, null);
		}
	}

	/**
	 * Get special with given name and value. 
	 * IF the registry contains a special with given value, it is used as 
	 * template for created special. 
	 * @param name The name of the special. 
	 * @param value The value of the special. 
	 * @return 
	 */
	public Special getSpecial(String name, Integer value) {
		return getSpecial(registry.get(name), name, value); 
	}
	
	public Special getSpecial(Special template, String name, Integer value) {
		return createSpecial(registry, createSpecial(template, name, value));
	}
	
	/**
	 * Create a new special, which is registered to the given registry. 
	 */
	public Special createSpecial(SpecialRegistry regsitry, Special created) {
		if (registry != null) {
			registry.register(created); 
		}
		return created; 
	}
	
	/**
	 * Create a new special with given name and value.
	 * 
	 * If the registry has special with given name, it is used as template for the
	 * created special.
	 * 
	 * If the registry does not have special with given name, a new template is
	 * created with following:
	 * <ul>
	 * <li>If the value is defined and not zero, the created special stacks.</li>
	 * <li>IF the value is undefined, the default value of 1 is used.</li>
	 * <li>The created special will not have numeric value.</li>
	 * <li>The created special is registered</li>
	 * </ul>
	 * 
	 * @param name  The name of the created special.
	 * @param value The value of the special. Defaults to 1.
	 * @return The created special.
	 */
	public static Special createSpecial(String name, Integer value) {
		return createSpecial(null, name, value); 
	}
		
	/**
	 * Create a new special with given name and value.
	 * 
	 * If the registry has special with given name, it is used as template for the
	 * created special.
	 * 
	 * If the registry does not have special with given name, a new template is
	 * created with following:
	 * <ul>
	 * <li>If the value is defined and not zero, the created special stacks.</li>
	 * <li>IF the value is undefined, the default value of 1 is used.</li>
	 * <li>The created special will not have numeric value.</li>
	 * <li>The created special is registered</li>
	 * </ul>
	 * 
	 * @param template The name of the template. 
	 * @param name  The name of the created special.
	 * @param value The value of the special. Defaults to 1.
	 * @return The created special.
	 */
	public static Special createSpecial(Special template, String name, Integer value) {
		if (template != null) {
			Function<Integer, Optional<Integer>> valueFunc = 
					(template instanceof FunctionalSpecial)
					? ((FunctionalSpecial) template).getValueFunction()
					: null;
			return createSpecial(name, value, valueFunc, template.stacks());
		} else {
			return createSpecial(name, value == null ? 1 : value, FunctionalSpecial.CURRENT_VALUE_FUNC,
					value != null && value != 0);
		}
	}

	/**
	 * Create a new special with given name and value.
	 * 
	 * If the registry has special with given name, it is used as template for the
	 * created special.
	 * 
	 * If the registry does not have special with given name, a new template is
	 * created with following:
	 * <ul>
	 * <li>If the value is defined and not zero, the created special stacks.</li>
	 * <li>IF the value is undefined, the default value of 1 is used.</li>
	 * <li>The created special will have numeric value equal to the current
	 * value</li>
	 * <li>The created special is registered</li>
	 * </ul>
	 * 
	 * @param name  The name of the created special.
	 * @param value The value of the special. Defaults to 1.
	 * @return The created special.
	 */
	public Special getNumericSpecial(String name, Integer value) {
		return createNumericSpecial(registry, registry.get(name), name, value, 1);
	}
	
	public static Special createNumericSpecial(String name, Integer value) {
		return createNumericSpecial(null, name, value, 1); 
	}


	/**
	 * Create a new special with given name and value.
	 * 
	 * If the registry has special with given name, it is used as template for the
	 * created special.
	 * 
	 * If the registry does not have special with given name, a new template is
	 * created with following:
	 * <ul>
	 * <li>If the value is defined and not zero, the created special stacks.</li>
	 * <li>IF the value is undefined, the default value of 1 is used.</li>
	 * <li>The created special will have numeric value equal to the current
	 * value</li>
	 * <li>The created special is registered</li>
	 * </ul>
	 * 
	 * @param name  The name of the created special.
	 * @param value The value of the special. Defaults to 1.
	 * @return The created special.
	 */
	public static Special createNumericSpecial(String name, Integer value, int multiplier) {
		return createNumericSpecial(null, name, value, multiplier); 
	}
	
	public Special getNumericspecial(String name, Integer value, int multiplier) {
		return createNumericSpecial(registry, null, name, value, multiplier);
	}
	
	/**
	 * Create a new special with given name and value.
	 * 
	 * If the registry has special with given name, it is used as template for the
	 * created special.
	 * 
	 * If the registry does not have special with given name, a new template is
	 * created with following:
	 * <ul>
	 * <li>If the value is defined and not zero, the created special stacks.</li>
	 * <li>IF the value is undefined, the default value of 1 is used.</li>
	 * <li>The created special will have numeric value equal to the current
	 * value</li>
	 * <li>The created special is registered</li>
	 * </ul>
	 * 
	 * @param name  The name of the created special.
	 * @param value The value of the special. Defaults to 1.
	 * @return The created special.
	 */
	public static Special createNumericSpecial(Special template, String name, Integer value, int multiplier) {
		return createNumericSpecial(null, template, name, value, multiplier); 
	}
	
	public static Special createNumericSpecial(SpecialRegistry registry, Special template, String name, Integer value, int multiplier) {
		Function<Integer, Optional<Integer>> valueFunc = FunctionalSpecial.GetMultiplierValueFunction(multiplier);
		if (template != null) {
			return createSpecial(registry, name, value, valueFunc, template.stacks());
		} else {
			return createSpecial(registry, name, value == null ? 1 : value, valueFunc, value != null && value != 0);
		}
	}

	/**
	 * Create a new special with given name, value, value function, and stacking
	 * status. The created specials are registered to the registry of the current
	 * tester.
	 * 
	 * @param name      The name of the special.
	 * @param value     The value of the special.
	 * @param valueFunc The value function determining the numeric value of the
	 *                  special. If undefined, the numeric value is empty.
	 * @param stacking  Does the created special stack.
	 * @return The created special.
	 * @throws IllegalArgumentException Any argument was invalid.
	 */
	public static Special createSpecial(String name, Integer value, Function<Integer, Optional<Integer>> valueFunc,
			boolean stacking) {
		return createSpecial(null, name, value, valueFunc, stacking); 
	}
	/**
	 * Create a new special with given name, value, value function, and stacking
	 * status. The created specials are registered to the registry of the current
	 * tester.
	 * 
	 * @param name      The name of the special.
	 * @param value     The value of the special.
	 * @param valueFunc The value function determining the numeric value of the
	 *                  special. If undefined, the numeric value is empty.
	 * @param stacking  Does the created special stack.
	 * @return The created special.
	 * @throws IllegalArgumentException Any argument was invalid.
	 */
	public static Special createSpecial(SpecialRegistry registry, 
			String name, Integer value, Function<Integer, Optional<Integer>> valueFunc,
			boolean stacking) {
		return createSpecial(registry, (registry==null?null:registry.get(name)), name,value, valueFunc, stacking);
	}
	/**
	 * Create a new special with given name, value, value function, and stacking
	 * status. The created specials are registered to the registry of the current
	 * tester.
	 * 
	 * @param name      The name of the special.
	 * @param value     The value of the special.
	 * @param valueFunc The value function determining the numeric value of the
	 *                  special. If undefined, the numeric value is empty.
	 * @param stacking  Does the created special stack.
	 * @return The created special.
	 * @throws IllegalArgumentException Any argument was invalid.
	 */
	public static Special createSpecial(SpecialRegistry registry, 
			Special template, 
			String name, Integer value, Function<Integer, Optional<Integer>> valueFunc,
			boolean stacking) {
		if (!Special.validName(name))
			throw new IllegalArgumentException("Invalid name");

		Special result = new Special() {

			@Override
			public String getName() {
				return name; 
			}

			@Override
			public int getValue() {
				return value; 
			}

			@Override
			public Optional<Integer> getNumberValue() {
				return valueFunc.apply(getValue());
			}
			
			@Override
			public Special getStacked(int value) {
				return createSpecial(getName(), getValue() + (stacking?value:0), 
						valueFunc, stacking);
			}
			
			@Override
			public boolean stacks() {
				return stacking; 
			}
			
		};
		// Registering the resulting special - if the special is already registered,
		// this
		// does nothing.
		if (registry != null) {
			registry.register(result);
		}
		return result;
	}

	protected java.util.List<String> normalSpecials = new ArrayList<>();

	protected java.util.List<String> stackingSpecials = new ArrayList<>();

	protected java.util.List<String> nonStackingSpecials = new ArrayList<>();

	protected java.util.List<String> numericSpecials = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		String key;
		Integer value, multiplier;
		Special created;
		for (List<? extends Object> fields : Arrays.asList(Arrays.asList("Effect", 1, null),
				Arrays.asList("Vicious", 1, 1), Arrays.asList("Penetration", 1, 3),
				Arrays.asList("Unhackable", null, null))) {
			key = fields.get(0).toString();
			value = (Integer) fields.get(1);
			multiplier = (Integer) fields.get(2);
			created = (multiplier == null ? getSpecial(key, value) : getNumericSpecial(key, value, multiplier));
			if (created.getNumberValue().isPresent()) {
				numericSpecials.add(key);
			} else {
				normalSpecials.add(key);
			}
			if (created.stacks()) {
				stackingSpecials.add(key);
			} else {
				nonStackingSpecials.add(key); 
			}

		}
	}

	public Special getNumericSpecial(String key, Integer value, Integer multiplier) {
		Special result = createNumericSpecial(registry.get(key), key, value, multiplier); 
		registry.register(result);  
		return result; 
	}

	@Test
	void testGetNumberValue() {
		for (String key : this.normalSpecials) {
			assertFalse(registry.get(key).getNumberValue().isPresent(),
					String.format("The key %s should not have numeric value", key));
		}
		for (String key : this.numericSpecials) {
			assertTrue(registry.get(key).getNumberValue().isPresent(),
					String.format("The key %s should have numeric value", key));
		}
	}

	@Test
	void testStacks() {
		com.kautiainen.antti.infinitybot.model.Special special;
		for (String key : this.nonStackingSpecials) {
			special = registry.get(key);
			if (special != null) {
				assertFalse(special.stacks(), String.format("The key %s should not stack", key));
			}
		}
		for (String key : this.stackingSpecials) {
			special = registry.get(key);
			if (special != null) {
				assertTrue(special.stacks(), String.format("The key %s should stack", key));
			}
		}

	}

	@Test
	void testCompareToSpecial() {
		Special special = new DiscordBot.Special("NoStack", null), other;
		assertEquals(0, special.compareTo(special), "Special was not equal to itself: " + special);
		other = new DiscordBot.Special("Viscious", 1);
		assertTrue(special.compareTo(other)<0); 
		special = other.getStacked(1);
		assertTrue(special.compareTo(other)>0); 
	}

	@Test
	void testGetStacked() {
		com.kautiainen.antti.infinitybot.model.Special special;
		int addition = 3; 
		for (String key : this.normalSpecials) {
			special = registry.get(key);
			if (special != null) {
				assertEquals(special.getName(), special.getStacked(addition).getName(), "Name differs"); 
				assertEquals(special.stacks(), special.getStacked(addition).stacks(), "Stacking differs"); 
				assertEquals(special.getValue(), special.getStacked(addition).getValue(), "Value differs"); 
				if (special instanceof FunctionalSpecial) {
					assertEquals(((FunctionalSpecial)special).getValueFunction(), 
							((FunctionalSpecial)special.getStacked(addition)).getValueFunction(), 
							"Function differs");
				}
			}
		}
		for (String key : this.stackingSpecials) {
			special = registry.get(key);
			if (special != null) {
				assertEquals(special.getName(), special.getStacked(addition).getName(), "Name differs"); 
				assertEquals(special.stacks(), special.getStacked(addition).stacks(), "Stacking differs"); 
				assertEquals(special.getValue()+addition, special.getStacked(addition).getValue(), "Value differs"); 
				if (special instanceof FunctionalSpecial) {
					assertEquals(((FunctionalSpecial)special).getValueFunction(), 
							((FunctionalSpecial)special.getStacked(addition)).getValueFunction(), 
							"Value differs from expected");
				}
			}
		}
	}
	

}
