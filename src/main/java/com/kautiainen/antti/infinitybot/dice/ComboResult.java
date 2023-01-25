package com.kautiainen.antti.infinitybot.dice;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Combo result with separate defined numeric value, and string caption. 
 * 
 * @author Antti Kautiainen
 *
 */
public class ComboResult extends NumericResult {
	
	/** The caption of the combo result. 
	 * 
	 */
	private String caption; 
	
	/**
	 * Create a new combo dice result with given cpation and value. 
	 * @param caption The caption of the result. 
	 * @param value The value of the result. 
	 */
	public ComboResult(String caption, int value) {
		super(value); 
		this.caption = caption; 
	}
	

	@Override
	public String getCaption() {
		return caption; 
	}


	@Override
	public List<Object> getValues() {
		return Arrays.asList(getValue(), getCaption());
	}


	@Override
	public String getCaption(List<Object> values, String delimiter,
			BiFunction<String, Object, String> accumulator) {
		// TODO Auto-generated method stub
		return super.getCaption(values, delimiter, accumulator);
	}


	@Override
	public String getCaption(List<Object> values, String delimiter, BiConsumer<StringBuilder, Object> accumulator) {
		if (values == null || values.size() < 2) return null; 
		StringBuilder result = new StringBuilder();
		if (accumulator != null) {
			accumulator.accept(result, values.get(1));
		} else {
			Object value = values.get(1);
			String caption; 
			if (value != null && (caption = value.toString()) != null && caption.length() > 0) {
				result.append(caption); 
			}
		}
		return result.toString(); 
	}
	

	/**
	 * The string representation of the combo caption
	 * @return The string representation of the combination caption. 
	 */
	public String toString() {
		return this.getValues().toString();
	}
}