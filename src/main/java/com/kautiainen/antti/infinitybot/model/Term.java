package com.kautiainen.antti.infinitybot.model;

import java.util.Optional;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

/**
 * Term is a basic unit.
 * 
 * @author Antti Kautiainen
 *
 * @param <TYPE> The content type of the term.
 */
public class Term<TYPE> {
	
	/**
	 * The name of the term.
	 */
	private final @NonNull String name_;
	
	/**
	 * The possible property name. if the value is empty, the term does not have property name.
	 */
	private final Optional<String> propertyName_;
	
	/**
	 * The default value. This value may be undefined to ensure that the term may handle the situation
	 * the undefined value is valid value.
	 */
	private Optional<TYPE> defaultValue_;
	
	/**
	 * Term is a combination of a property name, default value, and name.
	 * 
	 * @param name The name of the property.
	 * @param propertyName The property name. Defaults to no property name.
	 * @param defaultValue The default value of the term
	 */		
	public Term(@NonNull String name, @Nullable String propertyName, @Nullable Optional<TYPE> defaultValue)
			throws IllegalArgumentException {
		if (validName(name)) {
			this.name_ = name;
		} else {
			throw new IllegalArgumentException("Invalid name");
		}
		if (propertyName == null || validPropertyName(propertyName)) {
			this.propertyName_ = Optional.ofNullable(propertyName);
		} else {
			throw new IllegalArgumentException("Invalid property name");
		}
		this.defaultValue_ = defaultValue;			
	}
	
	/**
	 * Term is a combination of a property name, default value, and name.
	 * 
	 * @param name The name of the property.
	 * @param propertyName The property name. Defaults to no property name.
	 * @param defaultValue The default value of the term
	 */
	public Term(@NonNull String name, @Nullable String propertyName, TYPE defaultValue) {
		this(name, propertyName, Optional.ofNullable(defaultValue));
	}
	
	/**
	 * Test validity of the name.
	 * 
	 * @param name The tested name.
	 * @return True, if and only if the name is valid.
	 */
	public boolean validName(String name) {
		return name != null && Trait.NAME_PARSE_PATTERN.matcher(name).matches();
	}
	
	/**
	 * Test validity of the property name.
	 * 
	 * @param name The tested name.
	 * @return
	 */
	public boolean validPropertyName(String name) {
		return name == null || (StringTools.validIdentifier(name) && !name.endsWith("."));
	}
	
	/**
	 * Get the name of the term.
	 * 
	 * @return The name of the term.
	 */
	public @NonNull String getName() {
		return this.name_;
	}
	
	/**
	 * Get the property name of the term.
	 * 
	 * @return An empty value, if the term has no property value. Otherwise an
	 *  optional containing the property name.
	 */
	public Optional<String> getPropertyName() {
		return this.propertyName_;
	}
	
	/**
	 * Does the term have default value.
	 * 
	 * @return True, if and only if the term has default value.
	 */
	public boolean hasDefaultValue() {
		return this.defaultValue_ != null;
	}
	
	/**
	 * Get the default value of the term.
	 * 
	 * @return The default value of the term, if any exists. An empty value indicates
	 *  that the term has no default value.
	 */
	public Optional<TYPE> getDefaultValue() {
		return hasDefaultValue()?defaultValue_:Optional.empty();
	}
	
	/**
	 * Get the actual default value.
	 * 
	 * @return An undefined value, if the optional does not have default value. Otherwise 
	 *  the default value wrapped into optional.
	 */
	protected @Nullable Optional<TYPE> getActualDefaultValue() {
		return hasDefaultValue()?defaultValue_:null;
	}
	
	/**
	 * Test validity of the term value.
	 * 
	 * @param value The tested value.
	 * @return True, if and only if the given value is valid value for term.
	 */
	public boolean validValue(@Nullable TYPE value) {
		return hasDefaultValue() || value != null;
	}

	/**
	 * Test validity of the term value.
	 * 
	 * @param value The tested value.
	 * @return True, if and only if the given value is valid value for term.
	 */
	public boolean validValue(@Nullable Optional<TYPE> value) {
		return hasDefaultValue() || (value != null && validValue(value.orElse(null)));
	}
	
}