package com.kautiainen.antti.infinitybot.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kautiainen.antti.infinitybot.DiscordBot;
import com.kautiainen.antti.infinitybot.SpecialRegistry;

/**
 * Interface of Special values.
 * 
 * @author Antti Kautianen
 *
 */
public interface Special extends Comparable<Special> {

	
	/**
	 * Pattern patching not a quote or escape sequence
	 */
	public static final Pattern NOT_A_QUOTE_OR_ESCAPER = Pattern.compile("[^\\\\\\\"]");
	

	/**
	 * Pattern matching words with punctuation characters, or quoted strings with
	 * starting quote escaped. The pattern does not have capturing group.
	 */
	public static final Pattern WORD_PATTERN = Pattern
			.compile(
					"(?:\\p{Lu}[\\p{Ll}\\-`´&&"+
			NOT_A_QUOTE_OR_ESCAPER.toString() + "]*)+",
					Pattern.UNICODE_CHARACTER_CLASS);
	
	
	/**
	 * Create a special with a name, may stack, has value, and numeric value.
	 * 
	 * @param name The name of the special.
	 * @param stacks The stacking of the special.
	 * @param value The value of the special.
	 * @param numericValue The numeric value of the special. 
	 * @return The special constructed with given parameters.
	 * @throws IllegalArgumentException Any parameter was invalid.
	 */
	public static Special of(String name, boolean stacks, int value, 
			Optional<Integer> numericValue) 
	throws IllegalArgumentException {
		if (!validName(name))
			throw new IllegalArgumentException("Invalid name"); 
		if (numericValue == null) throw new IllegalArgumentException("Invalid numeric value");
		
		return new Special() {

			final String myName = name; 
			final boolean isStacking = stacks;
			final int myValue = value;
			final Optional<Integer> numValue = numericValue;
			
			@Override
			public Special createAnotherFromString(String stringRep) {
				return of(stringRep);
			}

			@Override
			public String getName() {
				return myName; 
			}

			@Override
			public int getValue() {
				return myValue; 
			}
			
			@Override
			public Optional<Integer> getNumberValue() {
				return this.numValue;
			}
			
			@Override
			public boolean stacks() {
				return this.isStacking;
			}

			@Override
			public Special getStacked(int value) {
				return of(this.getName(), this.stacks(), 
						this.getValue() + (this.stacks()?value:0), 
						this.getNumberValue()); 
			}
			
		};
	}

	/**
	 * Create a special from matcher of a value.
	 * 
	 * @param matcher The matcher matching for a special.
	 * @return The special representation of the given matcher.
	 * @throws IllegalArgumentException The matcher was invalid, or any matched value was invalid.
	 */
	public static Special of(Matcher matcher) 
	throws IllegalArgumentException {
		if (matcher != null && matcher.matches()) {
			String name=null, stacking=null, value=null, numericValue=null;
			Integer valueValue = null, numericValueValue = null; 
			boolean stackingValue = false; 
			try {
				stacking = matcher.group(NAME_GROUP_NAME);
			} catch(IllegalArgumentException iae) {
				throw new IllegalArgumentException("Name group is mandatory!");
			}
			try {
				stacking = matcher.group(STACKING_GROUP_NAME);
			} catch(IllegalArgumentException iae) {
				stackingValue = false; 
			}
			stackingValue = (stacking != null && 
					"s".equalsIgnoreCase(stacking)); 
			try {
				value = matcher.group(VALUE_GROUP_NAME);
				if (value != null) 
					valueValue = Integer.parseInt(value); 
			} catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("The value was not a number!"); 
			} catch(IllegalArgumentException iae) {
				valueValue = null; 
			}
			try {
				numericValue = matcher.group(Special.NUMERIC_VALUE_GROUP_NAME);
				if (numericValue != null) 
					numericValueValue = Integer.parseInt(numericValue); 
				else
					numericValueValue = null; 
			} catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("The numeric value was not a number!"); 
			} catch(IllegalArgumentException iae) {
				numericValueValue = null; 				
			}
			return of(name, stackingValue, (valueValue==null?defaultValue():valueValue.intValue()), 
					Optional.ofNullable(numericValueValue)); 
		} else {
			return null; 
		}
	}

	/**
	 * The default value of the value. 
	 * @return The default value of the undefined value. 
	 */
	public static int defaultValue() {
		return 1;
	}

	public static Special of(String stringRep) {
		if (stringRep == null) return null; 
		Pattern pattern = fromStringPattern(); 
		if (pattern != null) {
			return of(pattern.matcher(stringRep)); 
		} else {
			return null; 
		}
	}
	
	default Special createAnotherFromString(String stringRep) {
		return of(stringRep); 
	}
	
	/**
	 * The default regular expression for matching value of the Special from string.
	 * 
	 * The pattern uses named groups <code>special</code>, <code>value</code>, and
	 * <code>numvalue</code> to extract subsections of the value pattern. These are
	 * also only capturing groups in the pattern.
	 */
	public static final Pattern VALUE_FROM_STRING_PATTERN = valueFromStringPattern(
			stackingFromStringPattern(),
			valueFieldFromStringPattern(), 
			numericValueFromStringPattern());

	/**
	 * The regular expression matching a valid name of the special.
	 * 
	 * The pattern uses named group <code>name</code> to store the name of the
	 * special. This is the only capturing groups in the pattern.
	 */
	public static final Pattern NAME_FROM_STRING_PATTERN = Pattern.compile("(?<name>" +
			WORD_PATTERN.toString() + 
			")", Pattern.UNICODE_CHARACTER_CLASS);

	/**
	 * Test validity of the special name.
	 * 
	 * @param name The name.
	 * @return True, if and only if the name is valid.
	 */
	static boolean validName(String name) {
		return name != null && !name.isEmpty() && DiscordBot.WORD_PATTERN.matcher(name).matches();
	}

	/**
	 * Get the string representation of the pattern. 
	 * 
	 * If the optional is set true, and the result is non-empty, 
	 * the result is wrapped into optional non-capturing group.   
	 * 
	 * @param pattern The pattern whose string representation is acquired. 
	 * @param boolean Is the pattern optional. 
	 * @return The string of the pattern with given optional added for
	 *  existing non-empty pattern string. 
	 */
	static String patternToString(Pattern pattern, boolean optional)  {
		String result = pattern==null?"":pattern.toString();
		if (optional && !result.isEmpty()) {
			// Wrapping result to group.
			return String.format("(?:%s)?", result);
		} else {
			return result; 
		}
	}
	
	/**
	 * Get the pattern string representation.
	 * 
	 * @param pattern The pattern.
	 * @return The regular expression string representation of the given pattern.
	 */
	static String patternToString(Pattern pattern) {
		return patternToString(pattern, false); 
	}
	
	/**
	 * Generate value from string capturing pattern from given three patterns. 
	 * 
	 * @param stackingFromStringPattern The pattern matching stacking. 
	 * @param valueFromStringPattern The pattern matching value of the special. 
	 * @param numericValueFromStringPattern The pattern validating numeric value
	 *  part of the string representation. 
	 * @return The pattern validating value part of the special. 
	 */
	static Pattern valueFromStringPattern(
			Pattern stackingFromStringPattern, 
			Pattern valueFromStringPattern,
			Pattern numericValueFromStringPattern) {
		return Pattern.compile(
				patternToString(
						Pattern.compile(
								"(?<" + Special.STACKING_GROUP_NAME + ">" + 
								patternToString(stackingFromStringPattern)
								+ ")"), true) + 
				"(?<" + Special.VALUE_GROUP_NAME + ">" + 
								patternToString(valueFromStringPattern) + ")" +
				"(?:=" + 
				"(?<" + Special.NUMERIC_VALUE_GROUP_NAME + ">" + 
				patternToString(numericValueFromStringPattern)
				+ ")" + ")?", Pattern.UNICODE_CHARACTER_CLASS);
	}

	/**
	 * The registry mapping storing registered special values.
	 */
	public static final SpecialRegistry REGISTRY = new SpecialRegistry();

	/**
	 * Get the registered special of given name.
	 * 
	 * @param name The name of the special.
	 * @return The registered special of the given name, if any exists.
	 */
	static Optional<Special> getRegistered(String name) {
		return Optional.ofNullable(REGISTRY.get(name));
	}

	/**
	 * Registers given special, if it is not already registered.
	 * 
	 * @param registered The registered special.
	 * @return True, if and only if the registration succeeded.
	 */
	static boolean registerSpecial(Special registered) {
		return REGISTRY.register(registered);
	}

	static boolean unregisterSpecial(String name) {
		return REGISTRY.unregister(name);
	}

	static boolean unregisterSpecial(Special registered) {
		return REGISTRY.unregister(registered);
	}

	/**
	 * Get the number value of the special, if it has any.
	 * 
	 * @return The number value of the special.
	 */
	default Optional<Integer> getNumberValue() {
		return Optional.empty();
	}

	/**
	 * Does the special value stack.
	 * 
	 * @return True, if and only if the special value does not stack.
	 */
	default boolean stacks() {
		return false;
	}

	/**
	 * Get the name of the special.
	 * 
	 * @return Always defined and trimmed name of the special.
	 */
	String getName();

	/**
	 * Get the value of the special.
	 * 
	 * @return The integer value of the special.
	 */
	int getValue();

	/**
	 * Comparator comparing integers with nulls ordered lowest values.
	 */
	public static final Comparator<Integer> NULLS_FIRST_INTEGER_COMPARATOR = Comparator
			.nullsFirst(Comparator.naturalOrder());

	/**
	 * Comparator comparing numeric value of the special.
	 */
	public static final Comparator<Optional<? extends Integer>> NUMERIC_VALUE_COMPARATOR = (
			Optional<? extends Integer> a, Optional<? extends Integer> b) -> (NULLS_FIRST_INTEGER_COMPARATOR
					.compare((Integer) a.orElse(null), (Integer) b.orElse(null)));

	/**
	 * Compare two special. values.
	 * 
	 * @param other The compared value.
	 * @return -1, if this is less than other, 0 if this is equal to other, and 1,
	 *         if this is greater than other.
	 */
	default int compareTo(Special other) {
		int result = this.getName().compareTo(other.getName());
		if (result == 0) {
			result = Integer.compare(this.getValue(), other.getValue());
		}
		if (result == 0) {
			result = NUMERIC_VALUE_COMPARATOR.compare(this.getNumberValue(), other.getNumberValue());
		}
		return result;
	}

	/**
	 * Returns the special with given valued same special stacked with it.
	 * 
	 * @param value The value of the added stacking value.
	 * @return The new stacked object with stacked value. If the special is not
	 *         stacking, a copy of special is returned.
	 */
	public Special getStacked(int value);

	/**
	 * String representation of the level of the special.
	 * 
	 * The default string representation of the level is in the format
	 * <code>&lt;stacks&gt;&lt;value&gt;&lt;numeric value&gt;</code> where
	 * <dl>
	 * <dt>&lt;stacks&gt;</dt>
	 * <dd>The string <code>s</code>, if the special stacks.</dd>
	 * <dt>&lt;value&gt;</dt>
	 * <dd>The value of the special.</dd>
	 * <dt>&lt;numeric value&gt;</dt>
	 * <dd>The string <code>=</code> followed by the numeric value, if the special
	 * does have numeric value.</dd>
	 * </dl>
	 * 
	 * @param special The special, whose level string rep is wanted.
	 * @return The string representation of the level.
	 */
	default String valueToString(Special special) {
		Optional<Integer> numVal = this.getNumberValue();
		return (this.getValue() == 1 && !(this.stacks() || numVal.isPresent())) ? ""
				: String.format("%s%d%s", this.stacks() ? "s" : "", this.getValue(),
						numVal.isPresent() ? String.format("=%d", numVal.get()) : "");
	}

	/**
	 * The string representation of the value of the special.
	 * 
	 * @return The value of the special.
	 */
	default String valueToString() {
		return valueToString(this);
	}

	/**
	 * The string representation of the given special.
	 * 
	 * @param special The formatted special.
	 * @return The string representation of the given special.
	 */
	default String toString(Special special) {
		String levelString = this.valueToString(this);
		return String.format("%s%s", this.getName(),
				levelString == null || levelString.isEmpty() ? "":"(" + levelString + ")");
	}

	/**
	 * The pattern for matching name of the special.
	 * 
	 * @return The pattern matching to the name of the pattern.
	 */
	default java.util.regex.Pattern getNameFromStringPattern() {
		return nameFromStringPattern();
	}

	/**
	 * The pattern for matching the value of the special.
	 * 
	 * @return The pattern matching the value of the pattern.
	 */
	default java.util.regex.Pattern getValueFieldsFromStringPattern() {
		return valueFieldsFromStringPattern();
	}
	
	/**
	 * Construct from string pattern from given name and value patterns.
	 * 
	 * @param namePattern  The name pattern.
	 * @param valuePattern The value pattern.
	 * @return The pattern matching specials with given name and value patters.
	 * @throws java.util.regex.PatternSyntaxException The given pattern is not valid
	 *                                                pattern.
	 */
	static java.util.regex.Pattern fromStringPattern(Pattern namePattern, Pattern valuePattern)
			throws java.util.regex.PatternSyntaxException {
		return Pattern.compile((namePattern == null ? "" : 
			namePattern.toString()) + "(?:\\("
				+ (valuePattern == null ? "" : valuePattern.toString()) + "\\))?", 
				Pattern.UNICODE_CHARACTER_CLASS);
	}

	/**
	 * The pattern of the from string parsing of the string representations.
	 * 
	 * @return The from string pattern performing from string representation
	 *         validation. If value is undefined, no conversion from string is
	 *         available.
	 */
	static java.util.regex.Pattern fromStringPattern() {
		try {
			return fromStringPattern(nameFromStringPattern(), valueFieldsFromStringPattern());
		} catch (java.util.regex.PatternSyntaxException pe) {
			return null;
		}
	}
	
	/**
	 * The pattern used to convert string representation to the value it represents.
	 * 
	 * @return The pattern matching valid string representation and returning the
	 *         stacks, name, level, and numeric value from the result.
	 */
	default java.util.regex.Pattern getFromStringPattern() {
		return Pattern.compile("^" + this.getNameFromStringPattern().toString() + "(?:\\("
				+ this.getValueFieldsFromStringPattern().toString() + "\\))?$");
	}

	/**
	 * The pattern for getting the name from string representation.
	 * 
	 * @return The pattern matching to the name of the special.
	 */
	static Pattern nameFromStringPattern() {
		return Special.NAME_FROM_STRING_PATTERN;
	}
	/**
	 * The pattern for getting value from string.
	 * 
	 * @return The pattern matching valid value string content. The pattern extracts
	 *         the
	 */
	static Pattern valueFieldsFromStringPattern() {
		return valueFromStringPattern(Special.stackingFromStringPattern(), 
				Special.valueFieldFromStringPattern(), 
				Special.numericValueFromStringPattern());
	}

	/**
	 * The capturing pattern matching to the value of stacking. 
	 * 
	 * The pattern reserves the named group <code>stacking</code>. 
	 * 
	 * @return The pattern matching to the group representing the 
	 *  special is stacking. 
	 */
	static Pattern stackingFromStringPattern() {
		return Pattern.compile("[sS]");
	}
	
	/**
	 * The capturing pattern matching to the value of actual value of the
	 * special. 
	 * 
	 * The pattern reserves the named group <code>value</code>. 
	 * 
	 * @return The pattern matching to the group representing the 
	 *  value of special. 
	 */
	static Pattern valueFieldFromStringPattern() {
		return Pattern.compile("[+-]?\\d+");
	}

	/**
	 * The capturing group name containing the name of the special in 
	 * from name patterns. 
	 */
	public static final String NAME_GROUP_NAME ="name"; 
	
	/**
	 * The capturing group name containing the stacking trait of the special in 
	 * from name patterns. 
	 */
	public static final String STACKING_GROUP_NAME = "stacking"; 
	
	/**
	 * The capturing group name containing the value of the special in 
	 * from name patterns. 
	 */
	public static final String VALUE_GROUP_NAME = "value"; 
	
	/**
	 * The capturing group name containing the numeric value of the special in 
	 * from name patterns. 
	 */
	public static final String NUMERIC_VALUE_GROUP_NAME = "numvalue"; 
	
	/**
	 * The capturing pattern matching to the value of stacking. 
	 * 
	 * The pattern reserves the named group {@link #NUMERIC_VALUE_GROUP_NAME} 
	 * 
	 * @return The pattern matching to the group representing the 
	 *  special is stacking. 
	 */
	static Pattern numericValueFromStringPattern() {
		return Pattern.compile("[+-]?\\d+");
	}
}
