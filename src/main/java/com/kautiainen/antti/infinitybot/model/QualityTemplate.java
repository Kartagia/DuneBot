package com.kautiainen.antti.infinitybot.model;

import java.util.Optional;
import java.util.function.Function;

/**
 * The form of functional special representing template of qualities. 
 * @author Antti Kautiainen
 *
 */
public class QualityTemplate extends QualitySpecial {

	public QualityTemplate(String name) {
		this(name, true, QualityTemplate.EMPTY_VALUE_FUNC, null, null); 
	}
	
	public QualityTemplate(String name, boolean stacks) {
		this(name, stacks, QualityTemplate.EMPTY_VALUE_FUNC, null, null);
	}
	
	/**
	 * Creates a new quality template. 
	 * @param name The name of the quality template. 
	 * @param stacks Does the quality template stack. 
	 * @param valueFunc The value function of the numeric values the quality template generates. 
	 * @param defaultValue The default value of the quality template. 
	 * @param minValue The minimal value of the quality template. 
	 * @param maxValue THe maximal value of the quality template. 
	 * @throws IllegalArgumentException Any argument is invalid.  
	 */
	public QualityTemplate(String name, boolean stacks, Function<Integer, Optional<Integer>> valueFunc, 
			Integer defaultValue, Integer minValue, Integer maxValue) {
		super(name, 0, minValue, maxValue, valueFunc, stacks);
	}

	/**
	 * Creates a new quality template with given name, value multiplier, and stacking status.
	 * The default value will be set to 1. Undefined quality multiplier means the quality template
	 * will not have value, and other values create current value multiplied with given defined
	 * multiplier as numeric value. 
	 * @param name The name of the quality template. 
	 * @param valueMultiplier If this value is undefined, the template will not have numeric value.
	 *  Otherwise the numeric value will be the current value multiplied by the given multiplier. 
	 * @param stacks Does the quality template stack. 
	 * @throws IllegalArgumentException Any argument is invalid.  
	 */
	public QualityTemplate(String name, Integer valueMultiplier, boolean stacks) {
		this(name, stacks, (valueMultiplier!=null)?
				QualityTemplate.GetMultiplierValueFunction(valueMultiplier):
					QualityTemplate.EMPTY_VALUE_FUNC, 1, null, null);
	}

	/**
	 * Creates a new quality template. 
	 * @param name The name of the quality template. 
	 * @param stacks Does the quality template stack. 
	 * @param valueFunc The value function of the numeric values the quality template generates. 
	 * @param minValue The minimal value of the quality template. 
	 * @param maxValue THe maximal value of the quality template. 
	 * @throws IllegalArgumentException Any argument is invalid.  
	 */
	public QualityTemplate(String name, boolean stacks, Function<Integer, Optional<Integer>> valueFunc, 
			Integer minValue, Integer maxValue) {
		super(name, 0, minValue, maxValue, valueFunc, stacks);
		
	}

	
	/**
	 * Create a new template for qualities without numeric value with the value of the given 
	 * special as the default value of the created quality.  
	 * 
	 * @param added The special used to generate the quality template. 
	 * @throws IllegalArgumentException The given special is invalid. 
	 */
	public QualityTemplate(com.kautiainen.antti.infinitybot.model.Special added) 
	throws IllegalArgumentException {
		this(added.getName(), added.stacks(), FunctionalSpecial.EMPTY_VALUE_FUNC, null, null);
		if (added.getValue() != 0) {
			throw new IllegalArgumentException("Invalid template"); 
		}
	}

	/**
	 * Create a new quality template from given functional special. 
	 * @param added The special used to generate the quality template. 
	 * @throws IllegalArgumentException The given special is invalid. 
	 */
	public QualityTemplate(FunctionalSpecial added) 
	throws IllegalArgumentException {
		this(added.getName(), added.stacks(), added.getValueFunction(), null, null);
		if (added.getValue() != 0) {
			throw new IllegalArgumentException("Invalid template"); 
		}
	}
	
	

	@Override
	public Optional<Integer> getDefaultLevel() {
		Optional<Integer> result; 
		return (result = getDefaultMinimumLevel()).isPresent()?result:
					Optional.of(super.getDefaultLevel().orElse(0)); 
	}


	/**
	 * The default smallest allowed level. 
	 * @return The default of the smallest allowed level, if any exists. 
	 */
	public Optional<Integer> getDefaultMinimumLevel() {
		return Optional.of(0); 
	}

	/**
	 * The default largest allowed level. 
	 * @return The default of the tHe largest allowed level, if any exists. 
	 */
	public Optional<Integer> getDefaultMaximumLevel() {
		return Optional.empty(); 
	}
	

	@Override
	public com.kautiainen.antti.infinitybot.model.Special getStacked(int value) {
		return new Quality(this, value); 
	}
	
	@Override
	public boolean validLevel(Integer level) {
		return level == null || level == 0; 
	}
	
	@Override
	public Integer setValue(Integer level) throws UnsupportedOperationException {
		if (level == null || level == 0) {
			return super.setValue(0);
		} else {
			throw new IllegalArgumentException("Invalid level"); 
		}
	}
	
	/**
	 * Check the level of created quality. 
	 * @param level The tested level. 
	 * @return True,  if and only if the level is valid quality for this template. 
	 */
	public boolean validQualityLevel(Integer level) {
		if (level == null) return false; 
		Optional<Integer> boundary = this.getMinimumLevel();
		if (boundary.isPresent() && boundary.get().compareTo(level) > 0) {
			return false; 
		}
		boundary = this.getMaximumLevel();
		if (boundary.isPresent() && boundary.get().compareTo(level) < 0) {
			return false; 
		}
		return true; 
	}
	
	/**
	 * Create a new quality with given value. 
	 * @param value The value. 
	 * @return The created quality. 
	 * @throws IllegalArgumentException The given value is invalid. 
	 */
	public Quality createQuality(int value) throws IllegalArgumentException {
		// Constructing the value using get stacked. 
		if (validQualityLevel(value)) {
			return (Quality)getStacked(value); 				
		} else {
			throw new IllegalArgumentException(String.format("Invalid level of quality %d in [%s,%s]", value, this.getMinimumLevel(), this.getMaximumLevel())); 
		}
	}

}