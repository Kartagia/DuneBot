package com.kautiainen.antti.infinitybot.model;

import java.util.Objects;
import java.util.Optional;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

/**
 * Term value. Term value combines term with a value.
 * 
 * @author Antti Kautiainen
 *
 * @param <TYPE> The type of the term.
 */
public class TermValue<TYPE> {
	
	public static final String INVALID_VALUE_MESSAGE = "Invalid value";

	private final Term<TYPE> term_;
	
	private TYPE value_;
	
	/**
	 * Create a new term with given value. This should only be used, if the 
	 * undefined value is not accepted.
	 * 
	 * @param definition The term. 
	 * @param value The term value.
	 * @throws IllegalArgumentException The given value or term was invalid.
	 */
	public TermValue(@NonNull Term<TYPE> definition, @Nullable TYPE value) {
		if (validDefinition(definition)) {
			this.term_ = definition;
		} else {
			throw new IllegalArgumentException("Invalid definition");
		}
		setValue(value);
	}
	
	public TermValue(Term<TYPE> term) {
		this(term, term.getDefaultValue());
	}
	
	/**
	 * Create a new term value with optional value.
	 * 
	 * @param definition The attribute definition.
	 * @param value The initial value. Defaults to default value.
	 */
	public TermValue(Term<TYPE> definition, @Nullable Optional<TYPE> value) {
		if (validDefinition(definition)) {
			this.term_ = definition;
		} else {
			throw new IllegalArgumentException("Invalid definition");
		}
		setValue(value);
	}

	/**
	 * Test validity of the definition.
	 * 
	 * @param definition The tested definition.
	 * @return True, if and only if the definition is valid.
	 */
	public boolean validDefinition(Term<TYPE> definition) {
		return definition != null;
	}

	/**
	 * Get the term of the term value.
	 * 
	 * @return The term of the term value.
	 */
	public @NonNull Term<TYPE> getTerm() {
		return term_;
	}
	
	/**
	 * Get value of term value.
	 * 
	 * @return The current value of the term value.
	 */
	public @Nullable TYPE getValue() {
		return value_;
	}
	
	/**
	 * Test validity of the term value.
	 * 
	 * @param value The tested value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validValue(TYPE value) {
		return getTerm().validValue(value);
	}
	
	/**
	 * Test validity of the term value.
	 * 
	 * @param value The tested value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validValue(Optional<TYPE> value) {
		return getTerm().validValue(value);
	}
	
	/**
	 * Set value of a term value. If an undefined value is a valid value, the method
	 * @{link {@link #setValue(Optional)} should be used.
	 * 
	 * @param value The new value. Defaults to the default value of the term.
	 * @throws IllegalArgumentException The given value was invalid.
	 */
	public void setValue(TYPE value) throws IllegalArgumentException {
		if (validValue(value)) {
			if (value == null) {
				this.value_ = getTerm().getDefaultValue().orElse(null);
			} else {
				this.value_ = value;
			}
		} else {
			throw new IllegalArgumentException(INVALID_VALUE_MESSAGE);
		}

	}
	/**
	 * Set value of a term value. This version should be used, if the term has an undefined value as
	 * a valid value.
	 * 
	 * @param value The new value. Defaults to the default value of the term.
	 * @throws IllegalArgumentException The given value was invalid.
	 */
	public void setValue(@Nullable Optional<TYPE> value) throws IllegalArgumentException {
		if (validValue(value)) {
			if (value == null) {
				this.value_ = getTerm().getDefaultValue().orElse(null);
			} else {
				this.value_ = value.orElse(null);
			}
		} else {
			throw new IllegalArgumentException(INVALID_VALUE_MESSAGE);
		}

	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		
		if (obj instanceof TermValue<?> termValue) {
			try {
				return this.getTerm().equals(termValue.getTerm()) 
						&& Objects.equals(this.getValue(), termValue.getValue()); 
			} catch(Exception exception) {
				return false;
			}
		} 
		return false;	
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getTerm(), getValue());
	}

}