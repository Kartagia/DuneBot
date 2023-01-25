package com.kautiainen.antti.infinitybot.model;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Functional Special implements Special using function to determine the numeric value 
 * of the special. 
 * @author Antti Kautiainen
 *
 */
public class FunctionalSpecial extends BasicSpecial {

	
	/**
	 * Numeric value function. 
	 * @author Antti Kautiainen
	 *
	 */
	public abstract static class NumericValueFunction implements Function<Integer, Optional<Integer>> {
		
		private Optional<Integer> value; 
		
		/**
		 * Create a new numeric value function with given value. 
		 * @param value The value of the function. 
		 */
		protected NumericValueFunction(Integer value) {
			this.value = Optional.ofNullable(value); 
		}

		
		/**
		 * The current value of the numeric value function. 
		 * @return The value of the numeric value function. 
		 */
		public Optional<Integer> getValue() {
			return this.value; 
		}

		@Override
		public String toString() {
			Optional<Integer> value = getValue();  
			if (value.isPresent()) {
				return String.format("%s(%d)/%s", this.getClass().getSimpleName(), 
						value.get(), this.hashCode());
			} else {
				return String.format("%s(-)/%x", this.getClass().getSimpleName(), this.hashCode());
			}
		}

		@Override
		public boolean equals(Object other) {
			return other != null && other instanceof NumericValueFunction &&
					equals((NumericValueFunction)other);
		}
		
		/**
		 * Determine the outer equality - the equality from perspective
		 * of this class alone. The true equality requires that 
		 * <code>outerEquals(other) && other.outerEquals(this)</code>. 
		 * @param other The other numeric value function. 
		 * @return True, if and only if the other appears to be equal 
		 *  from perspective of this. 
		 */
		public boolean outerEquals(NumericValueFunction other) {
			return other instanceof FixedValueFunction && outerEquals((FixedValueFunction)other); 
		}		
	}
	
	/**
	 * Function determining numeric value. 
	 * @author Antti Kautiainen
	 *
	 */
	public static class MultiplierValueFunction extends NumericValueFunction {
		
		/**
		 * Create a new numeric value function without numeric value. 
		 */
		public MultiplierValueFunction() {
			this(null); 
		}
		
		/**
		 * Create a numeric value function with given multiplier of the value. 
		 * @param multiplier The multiplier of the value to get the numeric value.
		 *  If this value is undefined, the result will always be empty. 
		 */
		public MultiplierValueFunction(Integer multiplier) {
			super(multiplier);
		}
		
		/**
		 * The multiplier of the current object. 
		 * @return The multiplier of the current object. 
		 */
		public Optional<Integer> getMultiplier() {
			return super.getValue(); 
		}
		
		@Override
		public String toString() {
			Optional<Integer> multiplier = this.getMultiplier(); 
			if (multiplier.isPresent()) {
				return String.format("%s(x%d)/%s", this.getClass().getSimpleName(), 
						multiplier.get(), this.hashCode());
			} else {
				return String.format("%s(-)/%x", this.getClass().getSimpleName(), this.hashCode());
			}
		}
		
		@Override
		public Optional<Integer> apply(Integer value) {
			Optional<Integer> multiplier = this.getMultiplier(); 
			if (multiplier.isPresent() && value != null) {
				return Optional.of(multiplier.get()*value);
			} else {
				return Optional.empty(); 
			}
		}
		

		@Override
		public boolean outerEquals(NumericValueFunction other)  {
			return other instanceof MultiplierValueFunction && 
					outerEquals((MultiplierValueFunction)other);
		}
		
		/**
		 * Determine the outer equality - the equality from perspective
		 * of this class alone. The true equality requires that 
		 * <code>outerEquals(other) && other.outerEquals(this)</code>. 
		 * @param other The other numeric value function. 
		 * @return True, if and only if the other appears to be equal 
		 *  from perspective of this. 
		 */
		public boolean outerEquals(MultiplierValueFunction other) {
			return super.outerEquals(other);
		}
	}


	/** Function representing fixed value function.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class FixedValueFunction extends NumericValueFunction implements Supplier<Optional<Integer>> {

		/**
		 * Create a new fixed value function which will always return empty value. 
		 */
		public FixedValueFunction() {
			this(null); 
		}
		
		/** Create a new fixed value function which always returns given value.
		 * 
		 * @param value The fixed value. 
		 */
		public FixedValueFunction(Integer value) {
			super(value); 
		}
		
		@Override
		public final Optional<Integer> apply(Integer value) {
			return this.get(); 
		}
		
		@Override
		public boolean outerEquals(NumericValueFunction other)  {
			return other instanceof FixedValueFunction && 
					outerEquals((FixedValueFunction)other);
		}
		
		/**
		 * Determine the outer equality - the equality from perspective
		 * of this class alone. The true equality requires that 
		 * <code>outerEquals(other) && other.outerEquals(this)</code>. 
		 * @param other The other numeric value function. 
		 * @return True, if and only if the other appears to be equal 
		 *  from perspective of this. 
		 */
		public boolean outerEquals(FixedValueFunction other) {
			return super.outerEquals(other); 
		}

		@Override
		public Optional<Integer> get() {
			return getValue(); 
		}
		
		
	}

	
	/**
	 * The function returning empty as numeric value. 
	 */
	public static final Function<Integer, Optional<Integer>> EMPTY_VALUE_FUNC = 
			new FixedValueFunction();

	/**
	 * The function returning current value as numeric value. 
	 */
	public static final Function<Integer, Optional<Integer>> CURRENT_VALUE_FUNC = GetMultiplierValueFunction(1); 
	
	/**
	 * The function calculating numeric value by multiplying current value with given multiplier. 
	 * @param multiplier The multiplier of the value. 
	 * @return The function returning the numeric value by multiplying defined value by multiplier.
	 *  Undefined values still return {@link Optional#empty()}.  
	 */
	public static NumericValueFunction GetMultiplierValueFunction(int multiplier) {
		return new MultiplierValueFunction(multiplier); 
	}
	
	/** 
	 * The function returning a given numeric value every time. 
	 * @param fixedValue The fixed value. 
	 * @return The fixed value function. 
	 */
	public static NumericValueFunction GetFixedValueFunction(int fixedValue) {
		return new FixedValueFunction(fixedValue); 
	}

	/**
	 * Create a new special with given name, value, numeric value function, and
	 * stacking status.
	 * 
	 * @param name          The name of the special.
	 * @param value         The value of the special.
	 * @param valueFunction The function determining the numeric value of special.
	 * @param stacks        Does the special stack.
	 * @throws IllegalArgumentException The value is invalid. 
	 */
	public FunctionalSpecial(String name, Integer value, Function<Integer, Optional<Integer>> valueFunction, boolean stacks) {
		super(name, stacks, value, Optional.empty());
		if (valueFunction != null)
			this.myValueFunc = valueFunction;
	}
	
	/**
	 * Create functional special for string representation. 
	 * @param stringRep The string representation.
	 */
	public FunctionalSpecial(String stringRep) {
		super(stringRep); 
	}
	
	/**
	 * Create a new special with given name, value, and numeric value multiplier.  
	 * The value will be stacking, if the value has defined non-zero value. 
	 * 
	 * @param name          The name of the special.
	 * @param value         The value of the special.
	 * @param valueFunction The function determining the numeric value of special.
	 * @param stacks        Does the special stack.
	 */
	public FunctionalSpecial(String name, Integer value, int valueMultiplier) {
		this(name, value, 
				GetMultiplierValueFunction(valueMultiplier), 
				(value == null || value != 0));
	}

	

	/**
	 * The value function converting the value to the numeric value.
	 */
	private Function<Integer, Optional<Integer>> myValueFunc = (Integer value) -> (Optional.empty());
	

	@Override
	public Optional<Integer> getNumberValue() {
		Function<Integer, Optional<Integer>> func = getValueFunction(); 
		return func==null?super.getNumberValue():func.apply(getValue());
	}
	
	@Override
	protected void setNumberValue(String numericValue) {
		if (numericValue == null) {
			// Setting the value to default. 
			super.setNumberValue((Optional<Integer>)null);
		} else {
			Matcher matcher = getNumericValueFromStringPattern().matcher(numericValue); 
			if (matcher.matches()) {
				// TODO: change this to set numeric value function from string. 
				// We do have proper matcher. 
				int fixedValue;
				if (numericValue.startsWith("=")) {
					// we do have fixed value function. 
					try {
						fixedValue = Integer.parseInt(numericValue.substring(1));
					} catch(NumberFormatException nfe) {
						throw new IllegalArgumentException("Invalid fixed numeric value!"); 
					}
					this.setValueFunction(GetFixedValueFunction(fixedValue));
				} else if (numericValue.startsWith("*") || numericValue.startsWith("x") || numericValue.startsWith("X")) {
					// we do have multiplicative function. 
					try {
						fixedValue = Integer.parseInt(numericValue.substring(1));
					} catch(NumberFormatException nfe) {
						throw new IllegalArgumentException("Invalid numeric value multiplier!"); 
					}
					this.setValueFunction(GetMultiplierValueFunction(fixedValue));
				} else {
					// We do have the default case of fixed value without function
					try {
						fixedValue = Integer.parseInt(numericValue);
					} catch(NumberFormatException nfe) {
						throw new IllegalArgumentException("Invalid numeric value!"); 
					}
					super.setNumberValue(Optional.of(fixedValue));
				}
			} else {
				// Fall back to the number format of the parent. 
				super.setNumberValue(numericValue);
				this.setValueFunction(null); 
			}
		}
	}

	public Pattern getNumericValueFromStringPattern() {
		return numericValueFromStringPattern(); 
	}
	
	/**
	 * Set the value function. 
	 * @param valueFunction The new value function. 
	 */
	protected void setValueFunction(NumericValueFunction valueFunction) {
		this.myValueFunc = valueFunction; 
	}

	/**
	 * The function generating score value from value function.
	 * 
	 * @return Always defined function which generates value function.
	 */
	public Function<Integer, Optional<Integer>> getValueFunction() {
		return this.myValueFunc;
	}

	
	
	/**
	 * Convert special to human readable string representation.
	 * @return The string representation of the special.  
	 */
	public String toString() {
		return String.format("%s(valf: %s)", super.toString(this), 
				this.getValueFunction()); 
	}
	
	
	/** 
	 * @see Special#numericValueFromStringPattern()
	 */
	public static Pattern numericValueFromStringPattern() {
		return Pattern.compile("[xX*]?[+-]?\\d+"); 
	}
	
	
	
	@Override
	public Special createAnotherFromString(String stringRep) {
		// TODO Auto-generated method stub
		Pattern pattern = this.getFromStringPattern();
		if (pattern == null || stringRep == null) return null; 
		Matcher matcher = pattern.matcher(stringRep);
		if (matcher.matches()) {
			return of(stringRep); 
		} else {
			return super.createAnotherFromString(stringRep);
		}
	}

	public static FunctionalSpecial of(String stringRep) throws IllegalArgumentException  {
		return new FunctionalSpecial(stringRep);
	}
	
	public static Pattern valueFieldsFromPattern() {
		return valueFieldsFromPattern(stackingFromStringPattern(), 
				valueFromStringPattern(), 
				numericValueFromStringPattern());
	}
	

	public static Pattern fromStringPattern() {
		return Special.fromStringPattern(nameFromStringPattern(), valueFieldsFromStringPattern());
	}


	/**
	 * The pattern matching to the value, stacking, and default value of the quality. 
	 * @return The pattern matching the value of the quality. 
	 * @see Special#valueFieldsFromStringPattern()
	 */
	public static java.util.regex.Pattern valueFieldsFromStringPattern() {
		return Special.valueFromStringPattern(stackingFromStringPattern(), 
				valueFromStringPattern(), numericValueFromStringPattern()); 
	}

}
