package com.kautiainen.antti.infinitybot.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The quality special is the quality special implementation combining
 * all stuff shared by both qualities and quality templates. 
 * 
 * The default implementation allows null values. If the default value is 
 * defined, the null value is replaced with default value {@link #getDefaultLevel()}, if 
 * it is present. 
 * 
 * @author Antti Kautiainen
 *
 */
public abstract class QualitySpecial extends FunctionalSpecial {

	/**
	 * The default value of the quality special. 
	 */
	private Integer defaultLevel = 1; 
	
	/**
	 * Create new quality special. 
	 * @param name The name of the quality special. 
	 * @param level The level of the quality special. 
	 * @param maxLevel The largest level of the quality special, if defined. Undefined
	 *  value indicates there is no maximal level.  
	 * @param minLevel The smallest level of the quality special, if defined. Undefined
	 *  value indicates there is no minimal level.
	 * @param valueFunc The value function determining the numerical value of the quality. 
	 * @param stacks Does the quality special stack or not. 
	 */
	public QualitySpecial(String name, Integer level, 
			Integer defaultLevel, Integer minLevel, Integer maxLevel, 
			Function<Integer, Optional<Integer>> valueFunc, boolean stacks) {
		super(name, level, valueFunc, stacks); 
		this.minLevel = minLevel;
		this.defaultLevel = defaultLevel; 
		this.maxLevel = maxLevel; 
		if (!validLevel(level)) {
			throw new IllegalArgumentException("Invalid level"); 
		}
	}

	/**
	 * Create new quality special. 
	 * @param name The name of the quality special. 
	 * @param level The level of the quality special. 
	 * @param maxLevel The largest level of the quality special, if defined. Undefined
	 *  value indicates there is no maximal level.  
	 * @param minLevel The smallest level of the quality special, if defined. Undefined
	 *  value indicates there is no minimal level.
	 * @param valueFunc The value function determining the numerical value of the quality. 
	 * @param stacks Does the quality special stack or not. 
	 */
	public QualitySpecial(String name, Integer level, Integer minLevel, Integer maxLevel, 
			Function<Integer, Optional<Integer>> valueFunc, boolean stacks) {
		super(name, level, valueFunc, stacks); 
		this.minLevel = minLevel; 
		this.maxLevel = maxLevel; 
		if (!validLevel(level)) {
			throw new IllegalArgumentException("Invalid level"); 
		}
	}
	
	/**
	 * Does the quality special allow undefined level. 
	 * @return Does the quality special allow undefined values. 
	 */
	public boolean allowsUndefinedLevel() {
		return true; 
	}
	
	/**
	 * The default level, if the undefined levels are allowed. 
	 * @return The default level, if undefined values are allowed. 
	 */
	public Optional<Integer> getDefaultLevel() {
		return (this.defaultLevel == null)?super.getDefaultValue():Optional.of(this.defaultLevel); 
	}
	
	@Override
	public final Optional<Integer> getDefaultValue() {
		return this.getDefaultLevel(); 
	}
	
	@Override
	public final int getValue() {
		return this.getLevel().orElse(this.getDefaultValue().orElseThrow(()->new IllegalStateException("Invalid value"))); 
	}
	
	/**
	 * The level of the quality. 
	 * @return The level of the quality, if any exits. 
	 */
	public Optional<Integer> getLevel() {
		return Optional.of(super.getValue()); 
	}
	
	/**
	 * Test validity of the level. 
	 * @param level The tested level. 
	 * @return True, if and only if the level is valid. 
	 */
	public boolean validLevel(Integer level) {
		if (level == null)  {
			// Null is valid if the undefined values are specified. 
			return this.allowsUndefinedLevel(); 
		} else {
			// Testing the value. 
			Optional<Integer> boundary = this.getMinimumLevel(); 
			if (boundary.isPresent() && level.compareTo(boundary.get()) < 0) {
				return false; 
			}
			boundary = this.getMaximumLevel(); 
			if (boundary.isPresent() && level.compareTo(boundary.get()) > 0) {
				return false; 
			}
			return true; 
		}
	}
	
	/**
	 * The maximum level of the quality.
	 */
	private Integer maxLevel = null;
	/**
	 * The minimum level of the quality.
	 */
	private Integer minLevel = null;

	/**
	 * The minimal value of the quality. 
	 * @return The minimal value of quality, if any exists. 
	 */
	public Optional<Integer> getMinimumLevel() {
		return Optional.ofNullable(this.minLevel); 
	}
	

	/**
	 * Set the maximal level of the quality. 
	 * @param maxLevel the maxLevel to set
	 */
	protected void setMaximumLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	/**
	 * @param minLevel the minLevel to set
	 */
	protected void setMinimumLevel(Integer minLevel) {
		this.minLevel = minLevel;
	}

	/**
	 * The maximal value of the quality. 
	 * @return The maximal value of quality, if any exists. 
	 */
	public Optional<Integer> getMaximumLevel() {
		return Optional.ofNullable(this.maxLevel); 
	}
	
	
	public QualitySpecial(String name, int value, Function<Integer, Optional<Integer>> valueFunction, boolean stacks) {
		super(name, value, valueFunction, stacks);
	}

	public QualitySpecial(String name, Integer value, int valueMultiplier) {
		super(name, value, valueMultiplier);
	}

	/**
	 * The list of quality names this quality opposes. Opposed qualities cannot
	 * exist on the same equipment.
	 */
	public Set<String> getOpposedQualityNames() {
		return new TreeSet<>();
	}

	/**
	 * The list of quality names this quality replaces.
	 * 
	 * @return The list of qualities this quality replaces.
	 */
	public Set<String> getReplacedQualityNames() {
		return new TreeSet<>();
	}

	
	@Override
	public String valueToString(Special special) {
		Function<Optional<? extends Integer>, String> toStringer = 
				(Optional<? extends Integer> value) -> (
						value.isPresent()?value.get().toString():""
						); 
		String valueString = super.valueToString(special); 
		if (valueString == null || valueString.isEmpty()) {
			// The value string does not exists.
			StringBuilder result = new StringBuilder(); 
			
			// Adding minimum. 
			result.append(toStringer.apply(this.getMinimumLevel()));

			// Adding delimiter between min and maximum, if minimum exits. 
			if (result.length() > 0) result.append(",");

			// Adding maximum. 
			result.append(toStringer.apply(this.getMaximumLevel()));
			
			// Adding delimiter before existing maximum, if minimum does not 
			// exist. 
			if (result.length() > 0 && result.charAt(0) != ',') 
				result.insert(0,",");
			
			// Adding decoration around result, if we have valid result. 
			if (result.length() > 0) {
				// Adding prepends and footers.
				result.insert(0, "in[");
				result.append("]");
			}
			return result.toString();
		} else {
			return String.format("%s in[%s,%s]", valueString, 
					toStringer.apply(getMinimumLevel()), 
					toStringer.apply(getMaximumLevel()));
		}
	}
	
	public static final Pattern FROM_STRING_PATTERN = fromStringPattern(); 
	
	public static final Pattern BOUNDARY_FROM_STRING_PATTERN = 
			Pattern.compile("(?:" + 
					"\\s*in\\[(?<minboundary>[+-]\\d+)?,(?<maxboundary>[+-]\\d+)?\\]" +
					")?"); 
	
	public static final Pattern VALUE_FROM_STRING_PATTERN = 
			valueFromStringPattern(); 
				
	
	public java.util.regex.Pattern getBoundariesFromStringPattern() {
		return BOUNDARY_FROM_STRING_PATTERN; 
	}

	public static Pattern valueFromStringPattern() {
		return Pattern.compile(FunctionalSpecial.valueFieldsFromStringPattern().toString() + 
				BOUNDARY_FROM_STRING_PATTERN); 		
	}
	
	public java.util.regex.Pattern getValueFieldsFromStringPattern() {
		return Pattern.compile(super.getValueFieldsFromStringPattern().toString() + 
				this.getBoundariesFromStringPattern()); 
	}
		
	/**
	 * 
	 * @return
	 */
	public static Pattern fromStringPattern() {
		return Special.fromStringPattern(nameFromStringPattern(), valueFromStringPattern());
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Quality) {
			Quality otherQuality = (Quality)obj; 
			return this.compareTo(otherQuality) == 0 
					&& this.getMinimumLevel().equals(otherQuality.getMaximumLevel()) && 
					this.getMaximumLevel().equals(otherQuality.getMaximumLevel());
		} else if (obj instanceof Special) {
			return this.compareTo((Special)obj) == 0; 
		} else {
			return false; 
		}
	}
	
	@Override
	public int compareTo(Special other) {
		if (other instanceof QualitySpecial) {
			return compareTo((QualitySpecial)other); 
		} else {
			return super.compareTo(other); 
		}
	}
	
	/**
	 * Compare given quality with 
	 * @param other The other quality. 
	 * @return negative number, if the current object is less than other, 0 if they are equals, and
	 *  positive number if this is larger than other.
	 * @throws NullPointerException The given other is undefined. 
	 */
	public int compareTo(QualitySpecial other) {
		int result = super.compareTo((Special)other);
		if (result == 0) {
			// Performing comparison according to the traits of quality. 
			Comparator<Integer> intCmp = Comparator.naturalOrder();
			final Comparator<Integer> minCmp = Comparator.nullsFirst(intCmp); 
			Comparator<Optional<Integer>> optionalCmp = (Optional<Integer> a, Optional<Integer> b) -> (minCmp.compare(a.orElse(null), b.orElse(null)));
			result = optionalCmp.compare(this.getMinimumLevel(), other.getMinimumLevel()); 
			if (result == 0) {
				final Comparator<Integer> maxCmp = Comparator.nullsLast(intCmp); 
				optionalCmp = (Optional<Integer> a, Optional<Integer> b) -> (maxCmp.compare(a.orElse(null), b.orElse(null)));
				result = optionalCmp.compare(this.getMaximumLevel(),  other.getMaximumLevel()); 
			}
		}
		return result; 
	}

}