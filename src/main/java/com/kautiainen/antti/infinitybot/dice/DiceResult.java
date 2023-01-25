package com.kautiainen.antti.infinitybot.dice;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Dice result - also the value of single face.
 * 
 * The default dice result represents fixed valued result, and thus
 * re-roll by {@link #reroll()} has no effect. 
 * 
 * The default caption has only single value returned by {@link #getValue()}, and
 * the list of values is generated from this list. 
 *  
 * @author Antti Kautiainen. 
 *
 */
public interface DiceResult {
	
	/**
	 * Generates new random result for all dice in the dice.
	 * 
	 */
	default void reroll() {
		// Nothing to do. 
		
	}

	
	/**
	 * Get the caption of the die result. 
	 * @return The caption of the dice result. 
	 */
	default String getCaption() {
		return getCaption(getValues(), "", (BiConsumer<StringBuilder, Object>)null); 
	}
	
	/**
	 * Get the value of the die result. 
	 * @return The value of die result used to determine
	 *  the total value of the roll. 
	 */
	public Integer getValue(); 
	
	/**
	 * Get all values of the die roll. 
	 * @return The list of all values of the die face. 
	 */
	default List<Object> getValues() {
		return Arrays.asList(getValue()); 
	}
	
	/**
	 * Generate caption of given values using given delimiter and accumulator. 
	 * @param values The converted value. 
	 * @param delimiter The delimiter used to join the values, if there is more than one. 
	 * @param accumulator The accumulator combining previous result with next 
	 *  value.  
	 * @return The caption generated for given values. 
	 */
	default String getCaption(List<Object> values, String delimiter, 
			BiFunction<String, Object, String> accumulator ) {
		return getCaption(values, delimiter, 
				accumulator==null?null:(StringBuilder result, Object value) -> {
					result.replace(0,  result.length(), 
							accumulator.apply(result.toString(), value));
				}); 
	}
	
	/**
	 * Generate caption of given values using given delimiter and accumulator. 
	 * @param values The converted value. 
	 * @param delimiter The delimiter used to join the values, if there is more than one. 
	 * @param accumulator The accumulator combining previous result with next 
	 *  value.  
	 * @return The caption generated for given values. 
	 */
	default String getCaption(List<Object> values, String delimiter, 
			BiConsumer<StringBuilder, Object> accumulator) {
		
		final String delim = delimiter == null?"":delimiter; 
		final BiConsumer<StringBuilder, Object> combiner = 
				(accumulator == null?
						(StringBuilder result, Object value) -> {
							String valueRep = String.valueOf(value); 
							if (value != null && valueRep != null && valueRep.length() > 0) {
								if (result.length() > 0) result.append(delim); 
								result.append(String.valueOf(value));
							}
						}
						:accumulator);
		if (values != null) {
			BiConsumer<StringBuilder, StringBuilder> join = 
					(StringBuilder first, StringBuilder second) -> {
						combiner.accept(first, second);
					};
			return ((StringBuilder)values.stream().collect(
					((Supplier<StringBuilder>)()->{ return new StringBuilder(); }), 
					combiner, join)).toString();
		} else {
			return "";
		}
	}
}