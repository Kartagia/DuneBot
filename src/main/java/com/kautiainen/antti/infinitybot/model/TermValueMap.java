package com.kautiainen.antti.infinitybot.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.kautiainen.antti.infinitybot.model.RPGCharacter.ConstrainedTreeMap;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

/**
 * The TermValueMap creates mapping from term names to the term values. 
 * The term value map has a definition all of its term values share.
 * 
 * @author Antti Kautiainen
 *
 * @param <TYPE> The value type of the term.
 */
public class TermValueMap<TYPE> extends ConstrainedTreeMap<String, TermValue<TYPE>> {
	/**
	 * The serialized version of the term value map.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The definition of the term shared by all term values.
	 */
	private final Term<TYPE> definition_;
	
	/**
	 * Create a new term value map with a definition, contained name predicate, contained value predicate,
	 * and a contained name-value pair predicate.
	 * <p>The value and value of key predicates exists in addition to the value restrictions by the definition
	 * imposed by the term value.</p>
	 * 
	 * @param definition The definition of all contained terms.
	 * @param namePredicate The name validating predicate for contained term names. Defaults to predicate accepting
	 *  all names.
	 * @param valuePredicate The value validating predicate for contained term values. Defaults to predicate accepting
	 *  all values.
	 * @param valueOfKeyPredicate The name-value pair validating predicate. Defaults to predicate accepting all 
	 *  name-value pairs.
	 */
	public TermValueMap(
			@NonNull Term<TYPE> definition,
			@NonNull Predicate<String> namePredicate, 
			@Nullable Predicate<? super TermValue<TYPE>> valuePredicate, 
			@Nullable BiPredicate<? super String, ? super TermValue<TYPE>> valueOfKeyPredicate) {
		super(Comparator.naturalOrder(), namePredicate, valuePredicate, valueOfKeyPredicate);
		this.definition_ = definition;
	}
	
	public TermValueMap(@NonNull Term<TYPE> definition, @Nullable java.util.Set<String> validNames) {
		this(definition, 
				(Predicate<String>)(String name) -> {
					return validNames == null || validNames.contains(name);
				}, 
				(TermValue<TYPE> value) -> (value != null && definition.equals(value.getTerm())), 
				null);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the definition of the term all contained terms share.
	 * 
	 * @return The term all contained values share.
	 */
	protected @NonNull Term<TYPE> getDefinition() {
		return definition_;
	}
	
	/**
	 * Set the value of given term instance to the given. 
	 * If the term instance exists, the existing value is altered. Otherwise
	 * a new value is added.
	 * 
	 * @param name The name of the set instance.
	 * @param value The value of the instance.
	 * @throws IllegalArgumentException Either name or value was invalid.
	 */
	public void set(String name, TYPE value) throws IllegalArgumentException {
		if (validKey(name)) {
			TermValue<TYPE> currentValue = get(name);
			if (currentValue != null) {
				// Setting current value.
				currentValue.setValue(value);
			} else {
				// Adding new value.
				TermValue<TYPE> addedValue = new TermValue<TYPE>(getDefinition(), value);
				put(name, addedValue);
			}
		} else {
			throw new IllegalArgumentException(INVALID_KEY_MESSAGE);
		}
	}

	/**
	 * Set value of a contained term.
	 * 
	 * @param name The name of the set term.
	 * @param value The value of the term.
	 * @throws IllegalArgumentException The given name, value, or name value pair is invalid.
	 */
	public void set(String name, Optional<TYPE> value) throws IllegalArgumentException {
		if (this.validKey(name)) {
			if (containsKey(name)) {
				// The key is contained - it is valid.
				get(name).setValue(value);
			} else {
				put(name, new TermValue<>(getDefinition(), value));
			}
		} else {
			throw new IllegalArgumentException(INVALID_KEY_MESSAGE);
		}
	}
	
	@Override
	public boolean validValue(@NonNull TermValue<TYPE> value) {
		return value != null && getDefinition().equals(value.getTerm()) && super.validValue(value);
	}

}