package com.kautiainen.antti.infinitybot.model;

import java.util.Comparator;
import java.util.Optional;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

/**
 * 
 * The ordered term does have a defined order. 
 * If the order allows undefined values, the {@link OrderedTerm(Comparator, String, String, Object, Optional, Optional)} should be used
 * as constructor, as it allows undefined minimum and maximum to exist.
 * 
 * @author Antti Kautiainen
 *
 * @param <TYPE> The content type of the term.
 */
public class OrderedTerm<TYPE> extends Term<TYPE> {
	
	/**
	 * NaturalOrderTerm creates an ordered term with natural order. As natural order does not have null values, 
	 * the undefined values are also interpreted as non-existing.
	 * 
	 * @author Antti Kautiainen
	 *
	 * @param <TYPE> The comparable value type of the term value.
	 */
	public static class NaturalOrderTerm<TYPE extends Comparable<? super TYPE>> extends OrderedTerm<TYPE> {
		
		/**
		 * Create a new naturally ordered term with an order, a name, a property name, a default value, and a minimum and a maximum.
		 * 
		 * @param name The name of the term.
		 * @param propertyName The property name of the term. Defaults to no property name.
		 * @param defaultValue The default value of the term. Defaults to no default value. 
		 * @param minimum The minimal value of the term. Defaults to no minimal value.
		 * @param maximum The maximal value of the term. Defaults to no maximal value.
		 * @throws IllegalArgumentException Any argument was invalid.
		 */
		public NaturalOrderTerm(@NonNull String name, @Nullable String propertyName,
				@Nullable TYPE defaultValue, @Nullable TYPE minimumValue, @Nullable TYPE maximumValue) throws IllegalArgumentException {
			super(Comparator.naturalOrder(), name, propertyName, defaultValue, minimumValue, maximumValue);
		}
		
		@Override
		public boolean hasMinimum() {
			return getActualMinimum().isPresent();
		}
		
		@Override
		public boolean hasMaximum() {
			return getActualMaximum().isPresent();
		}
	}

	private Comparator<? super TYPE> comparator_;
	private Optional<TYPE> maximum_;
	private Optional<TYPE> minimum_;
	private boolean hasDefault_;

	/**
	 * Create a new ordered term with an order, a name, a property name, a default value, and an existing minimum and an existing maximum.
	 * 
	 * @param comparator The comparator used to compare values.
	 * @param name The name of the term.
	 * @param propertyName The property name of the term. Defaults to no property name.
	 * @param defaultValue The default value of the term. Defaults to no default value.
	 * @param minimum The smallest valid value of the term.
	 * @param maximum The largest valid value of the term.
	 * @throws IllegalArgumentException Any parameter was invalid.
	 */
	public OrderedTerm(@NonNull Comparator<? super TYPE> comparator,
			@NonNull String name, @Nullable String propertyName, @Nullable TYPE defaultValue, @NonNull TYPE minimum, @NonNull TYPE maximum)
			throws IllegalArgumentException {
		this(comparator, name, propertyName, defaultValue, Optional.of(minimum), Optional.of(maximum));
	}
	/**
	 * Create a new ordered term with an order, a name, a property name, a default value, and a minimum and a maximum.
	 * 
	 * @param comparator The comparator used to compare the term values.
	 * @param name The name of the term.
	 * @param propertyName The property name of the term. Defaults to no property name.
	 * @param defaultValue The default value of the term. Defaults to no default value. 
	 * @param minimum The minimal value of the term. Defaults to no minimal value unless the comparator accepts an undefined values.
	 * @param maximum The maximal value of the term. Defaults to no maximal value unless the comparator accepts an undefined values.
	 * @throws IllegalArgumentException Any argument was invalid.
	 */
	public OrderedTerm(@NonNull Comparator<? super TYPE> comparator,
			@NonNull String name, @Nullable String propertyName, @Nullable TYPE defaultValue, @Nullable Optional<TYPE> minimum, @Nullable Optional<TYPE> maximum)
			throws IllegalArgumentException {
		this(comparator, name, propertyName, (defaultValue != null?null:Optional.ofNullable(defaultValue)), minimum, maximum);
	}
	
	/**
	 * Create a new ordered term with an order, a name, a property name, a default value, and an existing minimum and an existing maximum.
	 * 
	 * @param comparator The comparator used to compare the term values.
	 * @param name The name of the term.
	 * @param propertyName The property name of the term. Defaults to no property name.
	 * @param defaultValue The default value of the term. Defaults to no default value. 
	 * @param minimum The minimal value of the term. Defaults to no minimal value.
	 * @param maximum The maximal value of the term. Defaults to no maximal value.
	 * @throws IllegalArgumentException Any argument was invalid.
	 */
	public OrderedTerm(@NonNull Comparator<? super TYPE> comparator,
			@NonNull String name, @Nullable String propertyName, @Nullable Optional<TYPE> defaultValue, @Nullable Optional<TYPE> minimum, @Nullable Optional<TYPE> maximum)
			throws IllegalArgumentException {
		super(name, propertyName, defaultValue);
		this.hasDefault_ = defaultValue != null;
		this.comparator_ = comparator;
		this.minimum_ = minimum;
		this.maximum_ = maximum;
	}
	
	/**
	 * True, if and only if the ordered term has set minimum value.
	 * 
	 * @return True, if and only if the minimum of the value is set.
	 */
	public boolean hasMinimum() {
		return this.minimum_ != null;
	}
	
	/**
	 * True, if and only if the ordered term has set maximum value.
	 * 
	 * @return True, if and only if the maximum of the value is set.
	 */
	public boolean hasMaximum() {
		return this.maximum_ != null;
	}
	
	/**
	 * Get the actual minimum value which may be null.
	 * 
	 * @return The optional containing the minimum value.
	 */
	protected @Nullable Optional<TYPE> getActualMinimum() {
		return minimum_;
	}
	
	/**
	 * Get the actual maximum value which may be null.
	 * 
	 * @return The optional containing the minimum value.
	 */
	protected @Nullable Optional<TYPE> getActualMaximum() {
		return maximum_;
	}
	
	/**
	 * Get the smallest valid value of the term.
	 * 
	 * @return The smallest valid value of the term. If the order allows undefined values, the undefined
	 *  value is mapped as empty value.
	 */
	public Optional<TYPE> getMinimumValue() {
		return hasMinimum()?minimum_:Optional.empty();
	}
	
	/**
	 * Get the largest valid value of the term.
	 * 
	 * @return The largest valid value of the term. If the order allows undefined values, the undefined
	 *  value is mapped as empty value.
	 */
	public Optional<TYPE> getMaximumValue() {
		return hasMaximum()?maximum_:Optional.empty();
	}
	
	@Override
	public boolean hasDefaultValue() {
		return this.hasDefault_;
	}
	
	@Override
	public boolean validValue(TYPE value) {
		if (value == null) {
			value = getDefaultValue().orElse(null);
		}
		return super.validValue(value) 
				&& (!hasMinimum() || comparator_.compare(getMinimumValue().orElse(null), value) <= 0) 
				&& (!hasMaximum() || comparator_.compare(value, getMaximumValue().orElse(null)) <= 0)
				;
	}
	
	/**
	 * Test validity of the value. The method allows nulls as valid values, and invalidates all undefined
	 * values, if the comparator does not accept them.
	 * 
	 * @param value The tested value as optional. Defaults to the the default value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validValue(@Nullable Optional<TYPE> value) {
		if (value == null) {
			if (hasDefaultValue()) {
				value = getDefaultValue();					
			} else {
				return false;
			}
		}
		try {
			return (!hasMinimum() || comparator_.compare(getMinimumValue().orElse(null), value.orElse(null)) <= 0) 
					&& (!hasMaximum() || comparator_.compare(value.orElse(null), getMaximumValue().orElse(null)) <= 0)
					;	
		} catch(ClassCastException cce) {
			return false; 
		}
	}
}