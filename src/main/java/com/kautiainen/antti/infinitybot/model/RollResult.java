package com.kautiainen.antti.infinitybot.model;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


/**
 * The roll result of the infinity.
 * 
 * @author Antti Kautiainen.
 *
 */
public class RollResult {

	/**
	 * Convert specials array into list.
	 * 
	 * @param specials The specials list.
	 * @return The array wrapper for given list.
	 */
	public static List<Special> getSpecialsList(Special[] specials) {
		return new java.util.AbstractList<Special>() {

			@Override
			public Special get(int index) {
				if (specials == null)
					throw new IndexOutOfBoundsException();
				return specials[index];
			}

			@Override
			public Special set(int index, Special value) {
				Special result = get(index);
				specials[index] = value;
				return result;
			}

			@Override
			public int size() {
				return specials == null ? 0 : specials.length;
			}

		};
	}

	/**
	 * Create a new roll result from given value, roll, and special effects.
	 * 
	 * @param value    The resulting value of the roll.
	 * @param diceRoll The die result list.
	 * @param specials The special result list.
	 */
	public RollResult(int value, List<? extends Object> diceRoll, Special... specials) {
		this(value, diceRoll, getSpecialsList(specials));
	}

	/**
	 * Create a new roll result from given value, roll, and special effects.
	 * 
	 * @param value    The resulting value of the roll.
	 * @param diceRoll The die result list.
	 * @param specials The special result list.
	 */
	public RollResult(int value, List<? extends Object> diceRoll, List<? extends Special> specials) {
		this.setValue(value);
		this.setRoll(diceRoll);
		this.setSpecial(specials);
	}
	
	/**
	 * Create a new die roll by calculating the value of the roll. 
	 * @param dieRoll The die roll. 
	 * @param specials The scecials. 
	 */
	public RollResult(List<? extends Object> dieRoll, List<? extends Special> specials) {
		this(getDefaultValueFunction(), dieRoll, specials);
	}
	
	public RollResult(BiFunction<Object, Integer, Integer> valueFunc, List<? extends Object> dieRoll, List<? extends Special> specials) {
		this(calculateValue(valueFunc, dieRoll, specials), dieRoll, specials);
	}

	/**
	 * Calculates the value of a roll. 
	 * @param valueFunc The value function. 
	 * @param dieRoll The die roll. 
	 * @param specials The special affecting the roll. 
	 * @return The numeric value of the roll. 
	 */
	public static Integer calculateValue(BiFunction<Object, Integer, Integer> valueFunc,
			List<? extends Object> dieRoll, List<? extends Special> specials) {
		int specialCost = specials.stream()
				.collect(Collectors.summingInt(
						(Special spec)->{
							return (int)(spec==null?0:spec.getNumberValue().orElse(0));
						}
						));
		return dieRoll.stream()
				.collect(Collectors.summingInt(
						(Object roll)->{
							Integer result = valueFunc.apply(roll, specialCost);
							return (result == null?0:result.intValue()); 
						}
						));
	}

	/**
	 * Get the default value function taking the die roll, and total numeric
	 * value of specials as keys. 
	 * @return The function calculating the value of a die. 
	 */
	public static BiFunction<Object, Integer, Integer> getDefaultValueFunction() {
		return (Object value, Integer specialValue) -> {
			if (value == null) return null;
			if (value instanceof Integer) {
				return (Integer)value; 
			} else if (value instanceof String && "S".equals(value)) {
				return specialValue; 
			} else {
				return (Integer)0; 
			}
		};
	}
	
	/**
	 * Get the resulting value of the roll.
	 * 
	 * @return The value of the roll.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Set the value of the roll.
	 * 
	 * @param value The new value of the roll.
	 */
	protected void setValue(int value) {
		this.value = value;
	}

	/**
	 * Get the die result list.
	 * 
	 * @return An unmodifiable list of die results.
	 */
	public java.util.List<String> getRoll() {
		return Collections.unmodifiableList(roll);
	}

	/**
	 * Set the dice results list of the roll.
	 * 
	 * @param roll The new dice result list of the roll.
	 */
	protected void setRoll(List<? extends Object> roll) {
		this.roll = roll==null?null:roll.stream().map((Object obj)->obj.toString()).collect(Collectors.toList());
	}

	/**
	 * Get the special results of the roll.
	 * 
	 * @return An unmodifiable list of die results.
	 */
	public java.util.List<Special> getSpecials() {
		return Collections.unmodifiableList(special);
	}

	/**
	 * Set the special results list of the roll.
	 * 
	 * @param special The new special result list of the roll.
	 */
	protected void setSpecial(java.util.List<? extends Special> special) {
		this.special.clear();
		this.special.addAll(special);
		if (!this.getSpecials().equals(special)) {
			throw new Error("The given special was not properly set");
		}
	}

	private int value = 0;
	private java.util.List<String> roll = new java.util.ArrayList<>();
	private java.util.List<Special> special = new java.util.ArrayList<>();

	/**
	 * The message format pattern used to generate the string result.
	 * 
	 * @return The pattern getting value, string representation of the dice rolls,
	 *         the number of special values, and the string representation of the
	 *         special values.
	 */
	public String getResultFormat() {
		return "Result: {0} " + getSpecialsFormat() + "\nRoll: " + getRollFormat();
	}

	/**
	 * Escape pattern.
	 * 
	 * @param pattern The pattern.
	 * @return The pattern with all unsuitable values replaced with suitable escape.
	 */
	public String escapePattern(String pattern) {
		return pattern.replaceAll("([\\'\\\"])", "$1$1");
	}

	public String getSpecialsFormat() {
		List<com.kautiainen.antti.infinitybot.model.Special> specials = this.getSpecials();
		return  (specials==null||specials.isEmpty())?"":" with " + 
				escapePattern(String.join(", ",
				this.getSpecials().stream().map((Special s) -> s.toString()).collect(Collectors.toList())));
	}

	/**
	 * Get the roll formatted.
	 * 
	 * @return
	 */
	public String getRollFormat() {
		return "[" + escapePattern(String.join(", ", this.getRoll())) + "]";
	}
	
	/**
	 * Convert specials to the string.
	 * 
	 * @return The string representation of the specials.
	 */
	public String specialsToString() {
		// Generating the special value list.
		int specialSize = special == null ? 0 : special.size();
		StringBuilder specialCaption = new StringBuilder();
		Special specialValue;
		for (int i = 0, end = specialSize - 1; i < end; i++) {
			if ((specialValue = special.get(i)) != null) {
				if (!(specialCaption.length() == 0)) {
					specialCaption.append(", ");
				}
				specialCaption.append(specialValue.toString());
			}
		}
		if (specialSize > 0) {
			if ((specialValue = special.get(specialSize - 1)) != null) {
				if (!(specialCaption.length() == 0)) {
					specialCaption.append(", and ");
				}
				specialCaption.append(specialValue.toString());
			}
		}
		return specialCaption.toString();
	}

	@Override
	public String toString() {
		return MessageFormat.format(getResultFormat(), this.getValue());
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && (other == this || other instanceof RollResult && equals((RollResult)other) );
	}
	
	/**
	 * Test equality with other roll result.
	 * 
	 * @param other The other roll result.
	 * @return True, if and only if the other is equal to this.
	 */
	public boolean equals(RollResult other) {
		if (other == null) return false; 
		return this.getValue() == other.getValue() &&
				this.getRoll().equals(other.getRoll()) &&
				this.getSpecials().equals(other.getSpecials());
	}
}