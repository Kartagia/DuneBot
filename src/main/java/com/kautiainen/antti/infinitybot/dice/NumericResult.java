package com.kautiainen.antti.infinitybot.dice;

/**
 * Numeric result is dice result with single defined numeric value. 
 * 
 * @author Antti Kautiainen
 *
 */
public class NumericResult implements DiceResult {
	
	/**
	 * The value of the numeric result. 
	 */
	private Integer myValue; 
	
	/**
	 * Create new dice result with given value. 
	 * @param value The numeric value of the dice result.
	 * @throws IllegalArgumentExcpetion The given value was undefined.  
	 */
	public NumericResult(Integer value)  {
		if (value == null) throw new IllegalArgumentException("Undefined value"); 
		this.myValue = value; 
	}
	
	@Override
	public Integer getValue() {
		return myValue; 
	}
	
	@Override
	public String toString() {
		return getCaption(); 
	}
}