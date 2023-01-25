package com.kautiainen.antti.infinitybot.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import reactor.util.annotation.NonNull;

public abstract class RPGCharacter {

	/**
	 * The term name was invalid.
	 */
	public static final String INVALID_TERM_NAME_MESSAGE = "Invalid term name";

	/**
	 * Test validity of the term name.
	 * 
	 * @param termName The tested term name.
	 * @return True, if and only if the term name is valid term name.
	 */
	public abstract boolean validTermName(String termName);

	/**
	 * ConstrainedTreeMap is a tree map with functional constraint validating the keys and values.
	 * 
	 * The constrained map ensures all keys passes the key constraint, all values pass the value constraint, 
	 * and all key-value-pairs pass the value of key constraint.
	 * 
	 * @author Antti Kautiainen
	 *
	 * @param <KEY> The key type.
	 * @param <VALUE> The value type.
	 */
	public static class ConstrainedTreeMap<KEY, VALUE> extends java.util.TreeMap<KEY, VALUE> {
	
		/**
		 * Get the serialization version.
		 */
		private static final long serialVersionUID = 1L;
	
		/**
		 * The message of an invalid key.
		 */
		public static final String INVALID_KEY_MESSAGE = "Invalid key";
	
		/**
		 * The message of an undefined key.
		 */
		public static final String UNDEFINED_KEY_MESSAGE = "Undefined key not accepted";
	
		/**
		 * The message of an invalid value.
		 */
		public static final String INVALID_VALUE_MESSAGE = "Invalid value";
	
		/**
		 * The message of an undefined key.
		 */
		public static final String UNDEFINED_VALUE_MESSAGE = "Undefined value not accepted";
	
		/**
		 * The always true predicate.
		 */
		public static final Predicate<Object> TRUE_PREDICATE = (Object tested)->true;
		
		/**
		 * The always true bi-predicate.
		 */
		public static final BiPredicate<Object, Object> TRUE_BIPREDICATE = (Object testedKey, Object testedValue) -> true;
		
		/**
		 * The predicate all keys has to pass, if it is present. If the value is empty, all keys are accepted. 
		 */
		private final Optional<Predicate<? super KEY>> keyConstraint;
		
		/**
		 * All values must pass this constraint, if it is present. If constraint is not
		 * present, it does not limit values at all.
		 */
		private final Optional<Predicate<? super VALUE>> valueConstraint; 
	
		/**
		 * All key-value-pairs must pass this constraint, if it is present. If constraint is not
		 * present, it does not limit key-value-pairs.
		 */
		private final Optional<BiPredicate<? super KEY, ? super VALUE>> valueOfKeyConstraint; 
	
		
		/**
		 * Create an empty constrained tree map without constraints.
		 */
		protected ConstrainedTreeMap() {
			super();
			keyConstraint = Optional.empty();
			valueConstraint = Optional.empty();
			valueOfKeyConstraint = Optional.empty();
		}
		
		/**
		 * Create a constrained tree map with a comparator, key constraint, value constraint, and value of
		 * key constraint.
		 * 
		 * @param keyComparator The comparator used to order the keys.
		 * @param keyConstraint The key constraint all keys must pass. Defaults to no constraint.
		 * @param valueConstraint The value constraint all values must pass. Defaults to no constraint.
		 * @param valueOfKeyConstraint The value of key constraint all key-value-pairs must pass. Defaults to
		 *  no constraint.
		 */
		public ConstrainedTreeMap(java.util.Comparator<? super KEY> keyComparator, 
				Predicate<? super KEY> keyConstraint, 
				Predicate<? super VALUE> valueConstraint, 
				BiPredicate<? super KEY, ? super VALUE> valueOfKeyConstraint) {
			super(keyComparator);
			this.keyConstraint = Optional.ofNullable(keyConstraint);
			this.valueConstraint = Optional.ofNullable(valueConstraint);
			this.valueOfKeyConstraint = Optional.ofNullable(valueOfKeyConstraint);
		}
		
		/**
		 * Create a new constrained map with natural order.
		 * 
		 * @param map
		 * @param keyComparator The comparator used to order the keys.
		 * @param keyConstraint The key constraint all keys must pass. Defaults to no constraint.
		 * @param valueConstraint The value constraint all values must pass. Defaults to no constraint.
		 * @param valueOfKeyConstraint The value of key constraint all key-value-pairs must pass. Defaults to
		 *  no constraint.
		 * @throws IllegalArgumentException The key does not have natural order.
		 */
		protected ConstrainedTreeMap(java.util.Map<? extends KEY, ? extends VALUE> map,
				Predicate<? super KEY> keyConstraint, 
				Predicate<? super VALUE> valueConstraint, 
				BiPredicate<? super KEY, ? super VALUE> valueOfKeyConstraint)
						throws IllegalArgumentException {
			super(getNaturalOrderComparator().orElseThrow(()->new IllegalArgumentException("The key type does not have natural order")));
			this.keyConstraint = Optional.ofNullable(keyConstraint);
			this.valueConstraint = Optional.ofNullable(valueConstraint);
			this.valueOfKeyConstraint = Optional.ofNullable(valueOfKeyConstraint);
			putAll(map);
		}
	
		/**
		 * Get natural order of the given type.
		 * 
		 * @param <KEY> The type of the compared value.
		 * @return The natural order of given type if any exist.
		 */
		@SuppressWarnings("unchecked")
		public static<KEY> Optional<Comparator<? super KEY>> getNaturalOrderComparator() {
			try {
				return Optional.ofNullable((java.util.Comparator<? super KEY>)java.util.Comparator.naturalOrder());
			} catch(ClassCastException cce) {
				return Optional.empty();
			}
		}
		
		/**
		 * Create a new constrained tree map from sorted map.
		 * 
		 * @param <SUBKEY> The key type of the sorted map.
		 * @param map The sorted map.
		 * @param keyConstraint The key constraint. Defaults to no constraint, but the type constraint.
		 * @param valueConstraint The value constraint. Defaults to no constraint.
		 * @param valueOfKeyConstraint The value of key constraint.
		 */
		@SuppressWarnings("unchecked")
		protected<SUBKEY extends KEY> ConstrainedTreeMap(java.util.SortedMap<SUBKEY, ? extends VALUE> map,
				Predicate<? super KEY> keyConstraint, 
				Predicate<? super VALUE> valueConstraint, 
				BiPredicate<? super KEY, ? super VALUE> valueOfKeyConstraint) {
			this((java.util.Comparator<KEY>)(KEY first, KEY second) -> (map.comparator().compare((SUBKEY)first, (SUBKEY)second)), 
					((Predicate<KEY>)(KEY key) -> {
						try {
							SUBKEY subKey = (SUBKEY)key;
							return keyConstraint == null || keyConstraint.test(subKey);
						} catch(ClassCastException cce) {
							return false;
						}
					}), valueConstraint, valueOfKeyConstraint);
		}
		
		/**
		 * Create a new constrained tree map with a key comparator, a key constraint, a value constraint, a value of key constraint,
		 * and the entries of given map. 
		 * 
		 * @param keyComparator The comparator used to order the keys.
		 * @param map
		 * @param keyConstraint The key constraint all keys must pass. Defaults to no constraint.
		 * @param valueConstraint The value constraint all values must pass. Defaults to no constraint.
		 * @param valueOfKeyConstraint The value of key constraint all key-value-pairs must pass. Defaults to
		 *  no constraint.
		 * @throws NullPointerException ANy value of map was undefined, and the map does not allow undefined values.
		 * @throws IllegalArgumentException Any value of the map was invalid.
		 * @throws ClassCastException Any value of the map was of invalid type.
		 */
		protected ConstrainedTreeMap(java.util.Comparator<? super KEY> keyComparator, java.util.Map<? extends KEY, ? extends VALUE> map, 
				Predicate<? super KEY> keyConstraint, 
				Predicate<? super VALUE> valueConstraint, 
				BiPredicate<? super KEY, ? super VALUE> valueOfKeyConstraint)
		throws NullPointerException, IllegalArgumentException, ClassCastException {
			super(keyComparator);
			this.keyConstraint = Optional.ofNullable(keyConstraint);
			this.valueConstraint = Optional.ofNullable(valueConstraint);
			this.valueOfKeyConstraint = Optional.ofNullable(valueOfKeyConstraint);
			putAll(map);
		}
		
		/**
		 * Create a new empty constrained tree map with a key comparator, a key constraint, and a value constraint.
		 * @param keyComparator The comparator used to order the keys.
		 * @param keyConstraint The key constraint all keys must pass. Defaults to no constraint.
		 * @param valueConstraint The value constraint all values must pass. Defaults to no constraint.
		 *  no constraint.
		 */
		public ConstrainedTreeMap(java.util.Comparator<? super KEY> keyComparator, 
				Predicate<? super KEY> keyConstraint, Predicate<? super VALUE> valueConstraint) {
			this(keyComparator, keyConstraint, valueConstraint, null);
		}
		
		/**
		 * Create a new empty constrained tree map with a key comparator, a key constraint, and a value of key constraint.
		 * 
		 * @param keyComparator The comparator used to order the keys.
		 * @param keyConstraint The key constraint all keys must pass. Defaults to no constraint.
		 * @param valueOfKeyConstraint The value of key constraint all key-value-pairs must pass. Defaults to
		 *  no constraint.
		 */
		public ConstrainedTreeMap(java.util.Comparator<? super KEY> keyComparator,
				Predicate<? super KEY> keyConstraint, BiPredicate<? super KEY, ? super VALUE> valueOfKeyConstraint) {
			this(keyComparator, keyConstraint, null, valueOfKeyConstraint);
		}
		
		
		/**
		 * Test validity of a key.
		 * 
		 * @param key The tested key.
		 * @return True, if and only if the given key is valid.
		 */
		public boolean validKey(KEY key) {
			return this.keyConstraint.orElse(TRUE_PREDICATE).test(key);
		}
		
		/**
		 * Test validity of a value.
		 * @param value The tested value.
		 * @return True, if and only if the given value is valid value.
		 */
		public boolean validValue(VALUE value) {
			return this.valueConstraint.orElse(TRUE_PREDICATE).test(value);
		}
		
		/**
		 * Test validity of a value of a key.
	
		 * @param key The key.
		 * @param value The tested value.
		 * @return True, if and only if both key and value is valid, and the valeu of key pair is valid.
		 */
		public boolean validValue(KEY key, VALUE value) {
			return this.validKey(key) && this.validValue(value) && this.valueOfKeyConstraint.orElse(TRUE_BIPREDICATE).test(key, value);			
		}
		
		
		@Override
		public VALUE put(KEY key, VALUE value) throws IllegalArgumentException, NullPointerException, UnsupportedOperationException, ClassCastException {
			if (validKey(key)) {
				if (validValue(key, value)) {
					return super.put(key,  value);
				} else if (value == null) {
					throw new NullPointerException(UNDEFINED_VALUE_MESSAGE);
				} else {
					throw new IllegalArgumentException(INVALID_VALUE_MESSAGE);
				}
			} else if (key == null) {
				throw new NullPointerException(UNDEFINED_KEY_MESSAGE);
			} else {
				throw new IllegalArgumentException(INVALID_KEY_MESSAGE);
			}
		}
	
	
		@Override
		public Set<java.util.Map.Entry<KEY, VALUE>> entrySet() {
			// Entry set has to be altered to prevent adding invalid values.
			return new java.util.AbstractSet<>() {
	
				@Override
				public Iterator<java.util.Map.Entry<KEY, VALUE>> iterator() {
					return new Iterator<java.util.Map.Entry<KEY, VALUE>>() {
						private Iterator<java.util.Map.Entry<KEY, VALUE>> iterator_ = entrySet().iterator();
						
						@Override
						public boolean hasNext() {
							return iterator_.hasNext();
						}
						
						@Override
						public java.util.Map.Entry<KEY, VALUE> next() throws java.util.NoSuchElementException {
							return new java.util.AbstractMap.SimpleEntry<KEY, VALUE>(iterator_.next()) {
	
								/**
								 * The serial version of the simple entry wrapper.
								 */
								private static final long serialVersionUID = 1L;
	
								@Override
								public VALUE setValue(VALUE value) {
									if (validValue(getKey(), value)) {
										return super.setValue(value);
									} else if (value == null) {
										throw new NullPointerException(UNDEFINED_VALUE_MESSAGE);
									} else {
										throw new IllegalArgumentException(INVALID_VALUE_MESSAGE);
									}
								}
								
								
							};
						}
						
						@Override
						public void remove() throws IllegalStateException {
							iterator_.remove();
						}
					};
				}
	
				@Override
				public int size() {
					// TODO Auto-generated method stub
					return 0;
				}
				
			};
		}
		
		@SuppressWarnings("unchecked")
		public boolean equals(Object other) {
			if (other == null) return false;
			if (other == this) return true;
			if (other instanceof @SuppressWarnings("rawtypes") ConstrainedTreeMap map) {
				if (this.size() != map.size()) {
					return false;
				}
				return (this.entrySet().containsAll(map.entrySet()) && 
						map.entrySet().containsAll(entrySet())); 
			} else {
				return false;
			}
		}
	}

	/**
	 * The name of the term containing the skills.
	 */
	protected static final String SKILLS_TERM_NAME = "skills";
	
	/**
	 * The name of the term containing the attributes.
	 */
	protected static final String ATTRIBUTES_TERM_NAME = "attributes";
	/**
	 * The name of the character.
	 */
	protected String name_;
	/**
	 * The guild identifier of the character.
	 */
	protected Long guildId_;
	/**
	 * The owner identifier of the character.
	 */
	protected Long ownerId_;
	/**
	 * The map containing terms.
	 */
	private Map<String, Object> terms_;

	/**
	 * A term map mapping term names the structures storing term values.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class TermMap<TYPE> extends ConstrainedTreeMap<String, Object> {
		
		/**
		 * Default serial version.
		 */
		private static final long serialVersionUID = 1L;
		public TermMap() {
			super(Comparator.naturalOrder(), 
					null, null, null);
		}
		
		protected TermMap(
				Predicate<? super String> termNamePredicate, 
				Predicate<? super Object> termValueRepresentationPredicate, 
				BiPredicate<? super String, ? super Object> termNameAndValueRepresentationPredicate) {
			super(Comparator.naturalOrder(), 
					termNamePredicate,
					termValueRepresentationPredicate, 
					termNameAndValueRepresentationPredicate);
		}
		
		@SuppressWarnings({"unchecked", "unused"})
		@Override
		public boolean validValue(Object value) {
			if (value == null) return false;
			try {
				if (value instanceof TermValue) {
					TermValue<TYPE> casted = (TermValue<TYPE>)value;
				} else if (value instanceof TermValueMap) {
					TermValueMap<TYPE> casted = (TermValueMap<TYPE>)value;				
				}
				return true;
			} catch(ClassCastException cce) {
				return false;
			}
		}
		
		@SuppressWarnings("unchecked")
		protected TermValueMap<TYPE> getTermValueMap(String termName) throws NoSuchElementException {
			Object valueRep = get(termName);
			try {
				if (valueRep != null && (valueRep instanceof @SuppressWarnings("rawtypes") TermValueMap valueMap)) {
					return (TermValueMap<TYPE>)valueMap;
				}
			} catch(ClassCastException cce) {
				throw new NoSuchElementException();
			}
			throw new NoSuchElementException();
			
		}
		@SuppressWarnings("unchecked")
		protected TermValue<TYPE> getTermValue(String termName) throws NoSuchElementException {
			Object valueRep = get(termName);
			try {
				if (valueRep != null && (valueRep instanceof @SuppressWarnings("rawtypes") TermValue valueMap)) {
					return (TermValue<TYPE>)valueMap;
				}
			} catch(ClassCastException cce) {
				throw new NoSuchElementException();
			}
			throw new NoSuchElementException();
			
		}
	}
	
	/**
	 * Create a new character.
	 */
	public RPGCharacter() {
		super();
	}

	@SuppressWarnings("unchecked")
	protected <TYPE> Map<String, TermValueMap<TYPE>> getMultiValuedTerms() {
		return terms_.entrySet().stream().filter( (Map.Entry<String, ?> entry) -> {
			try {
				@SuppressWarnings("unused")
				TermValueMap<TYPE> termValues = (TermValueMap<TYPE>)entry.getValue();
				return true;
			} catch(ClassCastException cce) {
				return false;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, (Map.Entry<String, ?> entry) -> {
			return (TermValueMap<TYPE>)entry.getValue();
		}));
	}
	
	@SuppressWarnings("unchecked")
	protected <TYPE> Map<String, TermValue<TYPE>> getSingleValuedTerms() {
		return terms_.entrySet().stream().filter( (Map.Entry<String, ?> entry) -> {
			try {
				@SuppressWarnings("unused")
				TermValue<TYPE> termValues = (TermValue<TYPE>)entry.getValue();
				return true;
			} catch(ClassCastException cce) {
				return false;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, (Map.Entry<String, ?> entry) -> {
			return (TermValue<TYPE>)entry.getValue();
		}));
	}
	

	/**
	 * Initializes term values with given names and values.
	 * 
	 * @param <TYPE> The type of the term value.
	 * @param definition The definition of the set term.
	 * @param names The names of the terms.
	 * @param values The values of the term. The name of the value is acquired with same iteration count from names.
	 * @throws IllegalArgumentException The definition was already defined, the names and values did not contain same values, 
	 *  or any value or name was invalid.
	 */
	protected <TYPE> void initTermValueMap(@NonNull Term<TYPE> definition, @NonNull java.util.Collection<String> names, @NonNull java.util.Collection<TYPE> values) throws IllegalArgumentException {
		if (terms_.containsKey(definition.getName())) {
			// The term is already defined.
			throw new IllegalArgumentException("Term already initialized");
		} else if (names.size() != values.size()){
			// Invalid names and values list.
			throw new IllegalArgumentException("Names and values does not have same number of elements");
		} else {
			Iterator<String> nameIterator = names.iterator();
			Iterator<TYPE> valueIterator = values.iterator();
			TermValueMap<TYPE> valueMap = new TermValueMap<TYPE>(definition, new java.util.TreeSet<>(names));
			String name;
			TYPE value;
			while (nameIterator.hasNext() && valueIterator.hasNext()) {
				name = nameIterator.next();
				value = valueIterator.next();
				valueMap.set(name, value);
			}
			terms_.put(definition.getName(), valueMap);
		}
		
	}

	/**
	 * Initialize term from mapping of term values.
	 * 
	 * @param <TYPE> The type of the term value.
	 * @param definition The definition of the term.
	 * @param valueMap The mapping from term name to term value.
	 * @throws IllegalArgumentException The definition was already initialized, or the mapping contained invalid or incompatible term value.
	 */
	protected <TYPE> void initTermValueMap(@NonNull Term<TYPE> definition, @NonNull java.util.Map<String, TermValue<TYPE>> values) throws IllegalArgumentException {
		if (terms_.containsKey(definition.getName())) {
			throw new IllegalArgumentException("Term already initialized");
		} else {
			try {
			terms_.put(definition.getName(), values.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, 
					(Map.Entry<String, TermValue<TYPE>> entry)->{
						if (definition.equals(entry.getValue().getTerm())) { 
							return entry.getValue(); }
						else
							throw new IllegalArgumentException("Incompatbile term of a value");
					})));
			} catch(NullPointerException npe) {
				throw new IllegalArgumentException("Invalid values", npe);
			}
		}
		
	}

	/**
	 * Initialize term from mapping of term values.
	 * 
	 * @param <TYPE> The type of the term value.
	 * @param definition The definition of the term.
	 * @param valueMap The mapping from term name to term value.
	 * @throws IllegalArgumentException The definition was already initialized, or the mapping contained invalid value.
	 */
	protected <TYPE> void initTerm(@NonNull Term<TYPE> definition, @NonNull TermValueMap<TYPE> valueMap) throws IllegalArgumentException {
		if (terms_.containsKey(definition.getName())) {
			throw new IllegalArgumentException("Term already initialized");
		} else if (definition.equals(valueMap.getDefinition())) {
			terms_.put(definition.getName(), valueMap);
		} else {
			throw new IllegalArgumentException("Term value map uses different definition");
		}
	}

	/**
	 * Set term value to the given value.
	 * 
	 * @param <TYPE> The type of the term value.
	 * @param termName The term name.
	 * @param value The assigned value.
	 * @return The replaced value of the term.
	 * @throws IllegalArgumentException
	 */
	protected <TYPE> Object setTerm(@NonNull String termName, TermValue<TYPE> value) throws IllegalArgumentException {
		if (validTermName(termName)) {
			return terms_.put(termName,  value);
		} else {
			throw new IllegalArgumentException(INVALID_TERM_NAME_MESSAGE);
		}
	}

	/**
	 * Set the term value to the given value.
	 * 
	 * @param <TYPE> The type of the term value.
	 * @param termName The term name.
	 * @param valueMap The value mapping for a compound term value.
	 * @return The replaced value of the term.
	 * @throws IllegalArgumentException Any given argument was invalid.
	 */
	protected <TYPE> Object setTerm(@NonNull String termName, TermValueMap<TYPE> valueMap) throws IllegalArgumentException {
		if (validTermName(termName)) {
			return terms_.put(termName, valueMap);
		} else {
			throw new IllegalArgumentException(INVALID_TERM_NAME_MESSAGE);
		}
	}

	/**
	 * Get singular term value.
	 * 
	 * @param <TYPE> The type of the wanted value.
	 * @param termName The term name.
	 * @return The singular value of the term, if any exists.
	 * @throws ClassCastException The type of the term was not suitable. Either the value type was invalid, or the value did not contain
	 *  singular value.
	 * @throws NoSuchElementException The given term does not exist.
	 */
	@SuppressWarnings("unchecked")
	protected <TYPE> Optional<TermValue<TYPE>> getTermValue(@NonNull String termName) throws ClassCastException, NoSuchElementException {
		if (validTermName(termName)) {
			Object value = terms_.get(termName);
			if (value instanceof TermValue) {
				try {
					return Optional.ofNullable((TermValue<TYPE>)value);
				} catch(ClassCastException cce) {
					throw new ClassCastException("Incompatible type of the value");
				}
			} else {
				throw new ClassCastException("The value is not a singular value");
			}
		} else {
			throw new NoSuchElementException("Unknown term name"); 
		}
	}

	/**
	 * The default attribute definition.
	 * 
	 * @return The default attribute definition.
	 */
	public Term<Integer> getDefaultAttributeDefinition() {
		return null;
	}

	/**
	 * The default skill definition.
	 * 
	 * @return The default skill definition.
	 */
	public Term<Integer> getDefaultSkillDefinition() {
		return null;
	}

	/**
	 * Get the name of the character.
	 * 
	 * @return The name of the character.
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Get the guild identifier of the character.
	 * 
	 * @return The guild identifier of the character. If the character is not bound to 
	 *  a guild, this value is empty.
	 */
	public Optional<Long> getGuildId() {
		return Optional.ofNullable(this.guildId_);
	}

	/**
	 * Get the owner identifier of the character.
	 * 
	 * @return The owner identifier of the character, if the character has owner.
	 *  Otherwise, an empty value is returned.
	 */
	public Optional<Long> getOwnerId() {
		return Optional.ofNullable(this.ownerId_);
	}

	/**
	 * Test validity of a character name.
	 * 
	 * @param name The tested name.
	 * @return True, if and only if the name is valid.
	 */
	public boolean validName(String name) {
		return !(name != null && (!name.isEmpty() || (!name.equals(name.trim()))));
	}
	
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj instanceof RPGCharacter character) {
			if (!(Objects.equals(getName(), character.getName()) && 
					Objects.equals(getGuildId().orElse(null), character.getGuildId().orElse(null)) &&
					Objects.equals(getOwnerId().orElse(null), character.getOwnerId().orElse(null)))) {
				return false;
			}
			if (!Objects.deepEquals(terms_, character.terms_)) {
				return false;
			}
		}
		return true;
	}

}