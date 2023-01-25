package com.kautiainen.antti.infinitybot.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kautiainen.antti.infinitybot.Logging;

public class BasicSpecial extends Logging implements Special {

	public static final String REQUIRED_FIELD = "Required";

	public static final String OPTIONAL_FIELD = "Optional";

	private String name = null; 
	
	private int value = 0; 
	
	private boolean stacks = false; 
	
	private Optional<Integer> numericValue = Optional.empty(); 
	
	////////////////////////////////////////////////////////////////////
	//
	// Beans and Property support
	//
	////////////////////////////////////////////////////////////////////
	
	/**
	 * Is the current object under construction. 
	 */
	private boolean underConstruction = true;
	
	/**
	 * The property change support dealing with property change events. 
	 */
	private PropertyChangeSupport reportPropertyChange = new PropertyChangeSupport(this);

	/**
	 * The vetoable property change support dealing with validation of the property
	 * changes.
	 */
	private VetoableChangeSupport checkPropertyChange = new VetoableChangeSupport(this);
	
	
	/**
	 * Add a property change listener to the class. 
	 * @param listener The added listener. 
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.reportPropertyChange.addPropertyChangeListener(listener);
	}
	
	/**
	 * Remove a property change listener to the class. 
	 * @param listener The added listener. 
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.reportPropertyChange.removePropertyChangeListener(listener);
	}
	
	/**
	 * Add a vetoable change listener to the class. 
	 * @param listener The added listener. 
	 */
	public void addVetoableChangeListener(VetoableChangeListener listener) {
		this.checkPropertyChange.addVetoableChangeListener(listener);
	}
	
	/**
	 * Remove a vetoable change listener to the class. 
	 * @param listener The added listener. 
	 */
	public void removeVetoableChangeListener(VetoableChangeListener listener) {
		this.checkPropertyChange.removeVetoableChangeListener(listener);
	}
	
	
	/**
	 * Get the property value of the property with given getter. 
	 * @param <TYPE> The return type. 
	 * @param getter The getter returning the value of the property. 
	 * @return The value of the given property. 
	 */
	public<TYPE> TYPE getPropertyValue(Supplier<? extends TYPE> getter) {
		return getter == null?null:getter.get(); 
	}
	
	/**
	 * Get the property value. 
	 * @param <TYPE> The type of the wanted value. 
	 * @param propertyName The property name. 
	 * @return The property value of the given property. 
	 * @throws NoSuchElementException The given property does not exist. 
	 * @throws ClassCastException The given property is not of the wanted type. 
	 */
	@SuppressWarnings("unchecked")
	public<TYPE> TYPE getPropertyValue(String propertyName) throws NoSuchElementException, ClassCastException {
		return (TYPE)getPropertyValue(getPropertyGetter(propertyName));
	}
	
	/**
	 * Set the property value. 
	 * @param <TYPE> The type of the assigned value. 
	 * @param propertyName The property name. 
	 * @param propertyValue The property value. 
	 * @throws NoSuchElementException The given property does not exist. 
	 * @throws ClassCastException The given value is of invalid type. 
	 * @throws IllegalArgumentException The given value is invalid. 
	 */
	@SuppressWarnings("unchecked")
	public<TYPE> void setPropertyValue(String propertyName, TYPE propertyValue) throws NoSuchElementException, ClassCastException, IllegalArgumentException {
		getPropertySetter(propertyName, (Class<TYPE>)propertyValue.getClass()).accept(propertyValue);
	}
	
	
	/**
	 * Get the string representation of the value. 
	 * @param propertyName The property name. 
	 * @return The string representation. 
	 * @throws NoSuchElementException The given property does not exist. 
	 */
	public String getProperty(String propertyName) throws NoSuchElementException {
		Object value = getPropertyValue(getPropertyGetter(propertyName));
		return value==null?null:value.toString(); 
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Property information support
	//
	// The following segment deals with methods performing property information
	// support.
	//
	// TODO: Replace these with <code>interface PropertyInfo<code> storing the 
	// property info generalization similar to the {@link java.beans.BeanInfo}, but
	// with support for Java2 generics. 
	//
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Get the setter from string. 
	 * @param propertyName The property name. 
	 * @return The setter setting the value form string. 
	 * @throws NoSuchElementException The given property does not exist. 
	 */
	public Consumer<String> getPropertySetter(String propertyName) throws NoSuchElementException {
		List<String> propertyNames = this.getPropertyNames();
		List<Consumer<String>> propertySetters = this.getPropertySetters();
		int index = propertyNames.indexOf(propertyName);
		if (index >= 0) {
			return propertySetters.get(index);
		} else {
			throw new NoSuchElementException("The given property name does not exist");
		}
	}
	
	/**
	 * Get the property setter of given property for the property type. 
	 * @param propertyName The property name. 
	 * @param type the type of the assigned value. 
	 * @return The consumer setting the value from given type. 
	 */
	@SuppressWarnings("unchecked")
	public<TYPE> Consumer<? super TYPE> getPropertySetter(String propertyName, Class<TYPE> type) throws NoSuchElementException, ClassCastException {
		
		// TODO: Create support for several types of property values. 
		if (String.class.isAssignableFrom(type)) {
			return (Consumer<? super TYPE>)getPropertySetter(propertyName);
		} else {
			throw new ClassCastException("The given value type is not supported!"); 
		}
	}

	/**
	 * Get the supplier giving the property value. 
	 * @param propertyName The property name. 
	 * @return The supplier returning the property value. 
	 * @throws NoSuchElementException The given propery does not exist. 
	 */
	public Supplier<?> getPropertyGetter(String propertyName) throws NoSuchElementException {
		List<String> propertyNames = this.getPropertyNames();
		List<Supplier<?>> getters = this.getPropertyGetters();
		int index = propertyNames.indexOf(propertyName);
		if (index >= 0) {
			return getters.get(index);
		} else {
			throw new NoSuchElementException(
					String.format("Unknown property name %s!", propertyName)); 
		}				
	}

	/**
	 * Get the list of known property names. 
	 * @return The list of known property names. 
	 */
	public List<String> getPropertyNames() {
		return Arrays.asList(getNamePropertyName(), getValuePropertyName(), getStackingPropertyName(), getNumberValuePropertyName());
	}

	/**
	 * The name property name. 
	 * @return The value property name. 
	 */
	public String getNamePropertyName() {
		return "name";
	}
	
	/**
	 * The stacking property name. 
	 * @return The value property name. 
	 */
	public String getStackingPropertyName() {
		return "stacking";
	}

	/**
	 * The value property name. 
	 * @return The value property name. 
	 */
	public String getValuePropertyName() {
		return "value";
	}

	/**
	 * The numeric value property name. 
	 * @return The name of the number value property name. 
	 */
	public String getNumberValuePropertyName() {
		return "numberValue"; 
	}
	

	
	/**
	 * Get the list of property setters. 
	 * @return An unmodifiable list of property setters. 
	 */
	public List<Consumer<String>> getPropertySetters() {
		return Arrays.asList(
				this::setName, this::setValue, this::setStacks, this::setNumberValue
				);		
	}

	/**
	 * Get the list of property setters. 
	 * @return An unmodifiable list of property setters. 
	 */
	public List<Supplier<?>> getPropertyGetters() {
		return Arrays.asList(
				this::getName, 
				this::getValue, 
				(Supplier<String>)()->(this.stacks()?"s":""), 
				this::getNumberValue
				);		
	}


	/**
	 * Test validity of the given property change. 
	 * @param <TYPE>
	 * @param propertyName The property name. 
	 * @param newValue The new value. 
	 * @return True, if and only if the none of the change was not vetoed. 
	 * @throws NoSuchElementException The given property does not exists. 
	 */
	public<TYPE> boolean validPropertyValue(String propertyName, TYPE newValue) {
		try {
			this.checkPropertyChange.fireVetoableChange(propertyName, getPropertyValue(propertyName), newValue);
			return true; 
		} catch(PropertyVetoException pve) {
			return false; 
		}
	}

	/**
	 * Test the property change.  
	 * 
	 * By default the value change is valid, if all vetoable change listers
	 * accept the change. 
	 * 
	 * @param propertyName The changed property name. 
	 * @param propertyValue The new property value. 
	 * @return True, if and only if the given property value is valid for given
	 *  property. 
	 * @throws NoSuchElementException The given property does not exists. 
	 */
	public boolean validPropertyChange(String propertyName, String propertyValue)  {
		try {
			this.checkPropertyChange.fireVetoableChange(propertyName, 
					getPropertyGetter(propertyName).get(),
					propertyValue);
			return true; 
		} catch(PropertyVetoException pve) {
			return false; 
		}
	}
	
	/**
	 * Set the value of given property. 
	 * @param propertyName The property name. 
	 * @param propertyValue The value of the property.
	 * @throws IllegalArgumentException the given property value was invalid.
	 * @throws NoSuchElementExcpetion THe given property does not exist.
	 * @throws ClassCastException the property does not support setting from string.
	 */
	public void setProperty(String propertyName, String propertyValue) 
			throws IllegalArgumentException, NoSuchElementException, ClassCastException {
		if (!validPropertyChange(propertyName, propertyValue)) {
			throw new IllegalArgumentException("Invalid property change");
		}
		// Setting the value. 
		Consumer<String> setter = this.getPropertySetter(propertyName);
		if (setter == null) throw new ClassCastException("Setting from string is not supported"); 
			
		// Storing the old value. 
		String oldValue = getProperty(propertyName); 

		// Setting the value
		setter.accept(propertyValue);
			
		// Reporting the property change. 
		this.reportPropertyChange.firePropertyChange(propertyName, oldValue, propertyValue);
	}
	
	/**
	 * Are we under construction. 
	 * @return True, if and only if the state is under construction. 
	 */
	protected boolean underConstruction() {
		return this.underConstruction;
	}
	
	/**
	 * Locks the build.  
	 */
	protected void build() throws IllegalStateException {
		if (underConstruction) {
			validateSelf(); 
			underConstruction = false; 
		} 
	}
	
	/**
	 * Checks validity of the values. 
	 * @throws IllegalStateException The internal state is invalid. 
	 */
	protected void validateSelf() {
		try {
			if (!validName(getName())) {
				new IllegalArgumentException("Invalid name");
			}
			if (!validValue(getValue())) {
				new IllegalStateException(new IllegalArgumentException("Invalid value"));
			}			
		} catch(IllegalArgumentException | NoSuchElementException ex) {
			throw new IllegalArgumentException((Throwable)ex); 
		}

	}
	
	/**
	 * Create blank basic special which does not hold all constraints. 
	 */
	protected BasicSpecial() {
		
	}

	/**
	 * Set from given matcher. 
	 * @param matcher The  matcher from from string. 
	 * @throws IllegalArgumentException The given value is invalid. 
	 * @throws NoSuchElementException The default value for missing property
	 *  does not exists. 
	 */
	public void setFrom(Matcher matcher) throws IllegalArgumentException {
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid matcher - does not have match"); 
		}
		String variableName = "name", type = REQUIRED_FIELD; 
		String value = null; 
		try {
			variableName = "name";
			type = REQUIRED_FIELD; 
			value = matcher.group(NAME_GROUP_NAME);
		} catch(IllegalArgumentException iae) {
			if (REQUIRED_FIELD.equals(type)) {
				throw new IllegalArgumentException(String.format("%s %s missing", type, variableName)); 
			} else {
				value = null;
			}
		}
		debug("Setting name to %s", value==null?"undefined":"\"" + value + "\"");
		this.setName(value);
		
		try {
			type = OPTIONAL_FIELD; 
			variableName = "stacking"; 
			value = matcher.group(STACKING_GROUP_NAME);
		} catch(IllegalArgumentException iae) {
			if (REQUIRED_FIELD.equals(type)) {
				throw new IllegalArgumentException(String.format("%s %s missing", type, variableName)); 
			} else {
				value = null;
			}
		}
		setStacks(value); 
		
		try {
			type = REQUIRED_FIELD; 
			variableName = "value"; 
			value = matcher.group(STACKING_GROUP_NAME);
		} catch(IllegalArgumentException iae) {
			if (REQUIRED_FIELD.equals(type)) {
				throw new IllegalArgumentException(String.format("%s %s missing", type, variableName)); 
			} else {
				value = null;
			}
		}
		this.setValue(value); 
		try {
			type = OPTIONAL_FIELD; 
			variableName = "numeric value"; 
			value = matcher.group(NUMERIC_VALUE_GROUP_NAME);
		} catch(IllegalArgumentException iae) {
			if (REQUIRED_FIELD.equals(type)) {
				throw new IllegalArgumentException(String.format("%s %s missing", type, variableName)); 
			} else {
				value = null;
			}
		}
		this.setNumberValue(value);

	}
		
	/**
	 * Create a new basic special from string representation . 
	 * @param name The name of the special, or string representation. 
	 * @throws IllegalArgumentException The given name, or string representation,
	 *  was invalid. 
	 * @throws NoSuchElementException The construction from default value
	 *  is not supported, and either only name was given, or string representation
	 *  contained no value.  
	 */
	public BasicSpecial(String name) throws NoSuchElementException, IllegalArgumentException {
		this();
		Pattern pattern = this.getFromStringPattern();
		Matcher matcher; 
		if (pattern == null) {
			this.setName(name);
		} else if (name != null && (matcher = pattern.matcher(name)).matches()) {
			debug("Setting name from matcher %s", matcher);
			setFrom(matcher); 
		} else if (name != null) {
			debug("Setting name to %s", name);
			this.setName(name); 
		}
	}
	
	/**
	 * Create a new basic special. 
	 * @param name The name of the special. 
	 * @param stacking Does the created special stack. 
	 * @param value The value of the created special. 
	 * @param numericValue The numeric value of created special. 
	 * @throws IllegalArgumentException Any argument was invalid. 
	 * @throws NoSuchElementException The given value is undefined, and the 
	 *  default value does not exist. 
	 */
	public BasicSpecial(String name, Boolean stacking, Integer value, Optional<Integer> numericValue) 
	throws IllegalArgumentException {
		setName(name);
		setStacks(stacking);
		setValue(value);
		setNumberValue(numericValue);
		build(); 
	}

	/**
	 * Test validity of the value. 
	 * @param value The tested value. 
	 * @return True, if an only if the given value is valid value. 
	 */
	public boolean validValue(Integer value) {
		return validPropertyValue(this.getValuePropertyName(), value) &&  (value != null || allowsUndefinedValue());  
	}

	/**
	 * Test validity of the numeric value. 
	 * @param numericValue The numeric value. 
	 * @return True, if and only if the numeric value is valid. 
	 */
	public boolean validNumberValue(Optional<Integer> numericValue) {
		return validPropertyValue(this.getNumberValuePropertyName(), numericValue) && numericValue != null; 
	}

	/**
	 * Test validity of the name. 
	 * @param value The tested  name. 
	 * @return True, if an only if the given value is valid name. 
	 */
	public boolean validName(String name) {
		return validPropertyValue(this.getNamePropertyName(), name) && name != null && !name.isEmpty() &&
				getNameFromStringPattern().matcher(name).matches(); 
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name of the stacking. 
	 * @param name The name of the property. 
	 * @throws IllegalArgumentException The value is invalid. 
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected String setName(String name) throws IllegalArgumentException {
		// TODO: add checking if the operation is permitted at the call time.
		String propertyName = "name";
		String assigned = name == null?getDefaultName().orElseThrow(
				()->(new NoSuchElementException(
						String.format("Default %s does not exist", propertyName)))
				):name;
		if (!validName(assigned)) {
			throw new IllegalArgumentException(
					String.format("Invalid %s", propertyName));
		} else {
			String result = this.getName(); 
			this.name = assigned; 
			this.reportPropertyChange.firePropertyChange(propertyName, result, name);
			return result; 
		}
	}

	/**
	 * Get the default name. 
	 * @return The default name, if any exists. 
	 */
	protected Optional<String> getDefaultName() {
		return Optional.empty();
	}

	@Override
	public int getValue() {
		return this.value;
	}
	
	/**
	 * Set value from numeric representation. 
	 * @param value The new value of the value.
	 * @throws IllegalArgumentException The value is invalid. 
	 * @throws NoSuchElementException The setting the value to undefined, and 
	 *  the default value does not exist
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected Integer setValue(Integer newValue) throws IllegalArgumentException, NoSuchElementException {
		// TODO: add checking if the operation is permitted at the call time.
		String propertyName = "";
		Integer assigned = newValue == null?getDefaultValue().orElseThrow(
				()->(new NoSuchElementException(
						String.format("Default %s%svalue does not exist", propertyName, 
								(propertyName != null && !propertyName.isEmpty())?" ":"")))
				):newValue;
		if (!validValue(assigned)) {
			throw new IllegalArgumentException(
					String.format("Invalid %s%svalue", propertyName, (propertyName != null && !propertyName.isEmpty())?" ":""));
		} else {
			Integer result = this.getValue();
			this.value = assigned; 
			this.reportPropertyChange.firePropertyChange(propertyName, result, newValue);
			return result; 
		}
	}
	
	/**
	 * Set stacks from string representation. 
	 * @param stringRep The string representation of the value.
	 * @throws IllegalArgumentException The value is invalid. 
	 * @throws NoSuchElementException The setting the value to undefined, and 
	 *  the default value does not exist
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected void setValue(String value) throws IllegalArgumentException, NoSuchElementException {
		if (value == null) {
			this.setValue((Integer)null);;
		} else {
			try {
				this.setValue(Integer.parseInt(value));
			} catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid value - not a number"); 
			}
		}
	}
	
	/**
	 * Set stacks. 
	 * @param newValue Do the string representation stack. 
	 * @throws NoSuchElementException The setting the value to undefined, and 
	 *  the default value does not exist
	 * @throws IllegalArgumentException The value is invalid.
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected Boolean setStacks(Boolean newValue) throws IllegalArgumentException, 
	IllegalStateException {
		// TODO: add checking if the operation is permitted at the call time.
		String propertyName = "stacking ";
		Boolean assigned = newValue == null?getDefaultStackingValue().orElseThrow(
						()->(new NoSuchElementException(
								String.format("Default %s%svalue does not exist", propertyName, 
										(propertyName != null && !propertyName.isEmpty())?" ":"")))
						):newValue;
		if (!validStackingValue(assigned)) {
			throw new IllegalArgumentException(
							String.format("Invalid %svalue", propertyName, 
									(propertyName != null && !propertyName.isEmpty())?" ":""));
		} else {
			Boolean result = this.stacks(); 
			this.stacks = assigned; 
			this.reportPropertyChange.firePropertyChange(propertyName, result, newValue);
			return result; 
		}
	}
	
	/**
	 * Test the validity of the given stacking value. 
	 * @param newValue The tested value. 
	 * @return True, if and only if the given value is valid value. 
	 */
	private boolean validStackingValue(Boolean newValue) {
		return true;
	}

	/**
	 * Get the default value of stacking property value. 
	 * @return The default value, if any exists. If none exits, returns 
	 * {@link Optional#empty()}
	 */
	protected Optional<Boolean> getDefaultStackingValue() {
		return Optional.of(false);
	}

	/**
	 * Set stacks from numeric representation. 
	 * @param stringRep The string representation of the value.
	 * @throws IllegalArgumentException The value is invalid. 
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected void setStacks(String stringRep) throws IllegalArgumentException {
		this.setStacks("s".equalsIgnoreCase(stringRep)); 
	}
	
	/**
	 * Set the numeric value of the special. 
	 * @param newValue The numeric value. 
	 * @throws NoSuchElementException The setting the value to undefined, and 
	 *  the default value does not exist
	 * @throws IllegalArgumentException The numeric value is invalid. 
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected Optional<Integer> setNumberValue(Optional<Integer> newValue) {
		// TODO: add checking if the operation is permitted at the call time.
		String propertyName = "numeric value";
		Optional<Integer> assigned = newValue == null?getDefaultNumberValue().orElseThrow(
				()->(new NoSuchElementException(
						String.format("Default %s%svalue does not exist", propertyName, 
								(propertyName != null && !propertyName.isEmpty())?" ":"")))
				):newValue;
		if (!validNumberValue(assigned)) {
			throw new IllegalArgumentException(
					String.format("Invalid %svalue", propertyName, 
							(propertyName != null && !propertyName.isEmpty())?" ":""));
		} else {
			Optional<Integer> result = this.getNumberValue();
			this.numericValue = assigned;
			this.reportPropertyChange.firePropertyChange(propertyName, result, newValue);
			return result; 
		}
	}
	
	/**
	 * Get the default value of number value. 
	 * @return The default value, if any exists. If none exits, returns 
	 * {@link Optional#empty()}
	 */
	protected Optional<Optional<Integer>> getDefaultNumberValue() {
		return Optional.of(Optional.empty());
	}

	/**
	 * Set numeric value from numeric representation. 
	 * @param stringRep The string representation of the value.
	 * @throws IllegalArgumentException The value is invalid.
	 * @throws IllegalStateException the state of the object prevents setting
	 *  the value. 
	 */
	protected void setNumberValue(String numericValue) {
		if (numericValue == null) {
			this.setNumberValue((Optional<Integer>)null);
		} else {
			try {
				this.setNumberValue(Optional.of(Integer.parseInt(numericValue)));
			} catch(NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid numeric value"); 
			}
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Special) && equals((Special)obj);
	}
	
	/**
	 * Comparison of the equality of specials. 
	 * @param special The special compared with this. 
	 * @return True, if and only if the given special is equal to the current object.
	 */
	public boolean equals(Special special) {
		return special != null && this.compareTo(special) == 0; 
	}

	
	@Override
	public boolean stacks() {
		return this.stacks; 
	}
	
	@Override
	public Optional<Integer> getNumberValue() {
		return this.numericValue; 
	}

	/**
	 * Does the quality special allow undefined level. 
	 * @return Does the quality special allow undefined values. 
	 */
	public boolean allowsUndefinedValue() {
		return this.getDefaultValue().isPresent(); 
	}
	
	/**
	 * The default value of the class. 
	 * @return The default value of the class. 
	 */
	public static Optional<Integer> defaultValue() {
		return Optional.of(1); 
	}
	
	/**
	 * The default level, if the undefined levels are allowed. 
	 * @return The default level, if undefined values are allowed. 
	 */
	public Optional<Integer> getDefaultValue() {
		return defaultValue(); 
	}


	@Override
	public Special getStacked(int value) {
		return new BasicSpecial(this.getName(), 
				this.stacks(), this.getValue() + 
				(this.stacks()?value:0), 
				this.getNumberValue());
	}

	/**
	 * Convert special to human readable string representation.
	 * @return The string representation of the special.  
	 */
	public String toString() {
		return Special.super.toString(this);
	}

	/**
	 * The pattern matching name of the quality. 
	 * @return The pattern matching to the name of the special.. 
	 * @see Special#nameFromStringPattern()
	 */
	public static Pattern nameFromStringPattern() {
		return Special.nameFromStringPattern();
	}


	/**
	 * The string determining the stacking value from string representation. 
	 * @return The pattern validating stacking value from string representation.
	 */
	public static Pattern stackingFromStringPattern() {
		return Special.stackingFromStringPattern(); 
	}

	/**
	 * The string determining the value from string representation. 
	 * @return The pattern validating value from string representation.
	 */
	public static Pattern valueFromStringPattern() {
		return Special.valueFieldFromStringPattern();
	}
	
	/**
	 * The numeric value from string pattern. 
	 * @return The numeric value from string pattern. 
	 */
	public static Pattern numericValueFromStringPattern() {
		return Pattern.compile("[+-]?\\d+"); 
	}

	/**
	 * The pattern determining the value fields pattern from given sub-patterns. 
	 * @param stackingFromStringPattern The sub-pattern matching stacking status. 
	 * @param valueFromStringPattern The sub-pattern matching the value of pattern. 
	 * @param numericValueFromStringPattern The sub pattern matching the numeric value
	 *  of the pattern. 
	 * @return The pattern matching value fiends of the string representation.
	 * @see Special#valueFromStringPattern(Pattern, Pattern, Pattern)
	 */
	public static Pattern valueFieldsFromPattern(Pattern stackingFromStringPattern, Pattern valueFromStringPattern,
			Pattern numericValueFromStringPattern) {
		return Special.valueFromStringPattern(stackingFromStringPattern, valueFromStringPattern, numericValueFromStringPattern);
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
