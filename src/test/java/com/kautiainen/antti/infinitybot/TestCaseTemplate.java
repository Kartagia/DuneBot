package com.kautiainen.antti.infinitybot;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Test case template used to generate test cases.
 * 
 * @author Antti Kautiainen
 *
 */
public interface TestCaseTemplate {

	/**
	 * The value tester always returning true.
	 */
	public static final Predicate<Object> ACCEPTING_VALIDATOR = (Object obj) -> true;

	/**
	 * The value tester always returning false.
	 */
	public static final Predicate<Object> REJECTING_VALIDATOR = (Object obj) -> false;

	/**
	 * The descriptions of the parameters. 
	 * @return The list of parameter descriptions. 
	 */
	public java.util.List<String> getDescriptions();
	
	/**
	 * The list of known types of parameters. 
	 * 
	 * @return The list of known types.
	 */
	public java.util.List<Class<?>> getParameterTypes();

	/**
	 * The list of known types of exceptions. 
	 * 
	 * @return The list of known types exception types. 
	 */
	public java.util.List<Class<? extends Throwable>> getExceptionTypes();


	/**
	 * Get default exception of the test case template. 
	 * @return The default exception, if the test case throws exception. 
	 */
	public Optional<Class<? extends Throwable>> getDefaultException();
	
	/**
	 * The default tester of the exception. 
	 * @return The default tester of the exception.
	 */
	default Optional<DefaultExceptionPredicate> getDefaultExceptionTester() {
		Optional<Class<? extends Throwable>> type = this.getDefaultException();
		return type.isPresent()?Optional.of(
				new DefaultExceptionTester(type.orElse(null))):Optional.empty();
	}
	
	/**
	 * Set the default exception tester. 
	 * @param tester The default exception tester for the class. 
	 */
	public void setDefaultExceptionTester(DefaultExceptionPredicate tester);

	/**
	 * Set default exception. 
	 * @param defaultException The default exception, if the test case throws exception
	 *  by default. 
	 * @param defaultExceptionTester The default tester, if the exception matches the default
	 *  exception type. 
	 * @throws IllegalArgumentException Either argument is invalid. 
	 */
	default<EXCEPTION> void setDefaultException(Optional<Class<? extends Throwable>> defaultException, 
			Predicate<? super EXCEPTION> defaultExceptionTester) 
	throws IllegalArgumentException {
		try {
			setDefaultExceptionTester(new DefaultExceptionTester(defaultException.orElse(null), 
					defaultExceptionTester));
		} catch(NullPointerException npe) {
			throw new IllegalArgumentException("Optional must be defined!"); 
		}
	}

	/**
	 * Set default exception. 
	 * @param defaultException The default exception, if the test case throws exception
	 *  by default. 
	 */
	default void setDefaultException(Optional<Class<? extends Throwable>> defaultException) {
		setDefaultException(defaultException==null?Optional.empty():defaultException, null); 
	}
	
	/**
	 * Set default exception. 
	 * @param defaultException The default exception, if the test case throws exception
	 *  by default. 
	 */
	default void setDefaultException(Throwable exceptionType) {
		setDefaultException(Optional.ofNullable(exceptionType==null?null:exceptionType.getClass()));
	}

	
	/**
	 * The number of types the template recognizes.
	 * 
	 * @return The number of types (and test case parameters) the template
	 *         recognizes.
	 */
	default int getTypeCount() {
		List<Class<?>> types = getParameterTypes();
		return (types == null ? 0 : types.size());
	}

	/**
	 * Get the type of the given index.
	 * 
	 * @param index The index of types.
	 * @return The type of the given index, if any exists, or null if none exists.
	 */
	default Class<?> getType(int index) {
		if (index >= 0 || index < this.getTypeCount()) {
			return this.getParameterTypes().get(index);
		} else {
			return null;
		}
	}

	/**
	 * The list of known type validators.
	 * 
	 * @return The list of known types.
	 */
	public java.util.List<Predicate<Object>> getValueValidators();

	/**
	 * Get the validator of the given index.
	 * 
	 * @param index The index.
	 * @return The validator of the given index. If the index is invalid, the
	 *         validator rejects all values.
	 */
	default Predicate<Object> getValidator(int index) {
		if (index >= 0 && index < this.getTypeCount()) {
			Predicate<Object> result = getValueValidators().get(index);
			return result == null ? ACCEPTING_VALIDATOR : result;
		} else {
			return REJECTING_VALIDATOR;
		}
	}

	/**
	 * Setting value predicate to the predicate of the given type.
	 * 
	 * @param index  The index of the assigned tester.
	 * @param tester The new tester.
	 */
	default void setValueValidator(int index, Predicate<?> tester) throws IllegalArgumentException {
		setParameterValueValidator(index, getType(index), tester);
	}

	/**
	 * Sets the value of given index validator to the given predicate.
	 * 
	 * @param <TYPE>    The type of the value in the index.
	 * @param index     The index of the value.
	 * @param type      Type of the value at the given index. THis is needed to get
	 *                  the type for casting.
	 * @param predicate The new validator.
	 * @throws IllegalArgumentException The given validator was invalid.
	 */
	default <TYPE> void setParameterValueValidator(int index, Class<TYPE> type, Predicate<?> predicate)
			throws IllegalArgumentException {
		if (type == null)
			throw new IllegalArgumentException("Invalid index");

		List<Predicate<Object>> validators = getValueValidators();
		if (predicate == null)
			validators.set(index, null);
		else
			try {
				final Predicate<? super TYPE> typedPredicate = (Predicate<? super TYPE>) predicate;
				validators.set(index, (Object value) -> (type.isAssignableFrom(value.getClass())
						&& typedPredicate.test(type.cast(value))));
			} catch (ClassCastException | NullPointerException e) {
				throw new IllegalArgumentException("Invalid predicate!", e);
			} catch (IndexOutOfBoundsException ioobe) {
				throw new IllegalArgumentException("Invalid index!", ioobe);
			}
	}

	/**
	 * Adds test argument type.
	 * 
	 * @param <TYPE>    The type of the argument.
	 * @param type      The type of the argument.
	 * @param predicate The validator of the argument
	 * @return Was the type added or not.
	 */
	default <TYPE> boolean addParameterType(Class<TYPE> type, Predicate<? super TYPE> predicate) {
		if (type == null)
			return false;
		List<Predicate<Object>> validators = getValueValidators();
		getParameterTypes().add(type);
		if (predicate == null) {
			validators.add(
					(Predicate<Object>) (Object obj) -> (obj == null || type.isAssignableFrom(obj.getClass())));
		} else {
			validators.add((Predicate<Object>) (Object obj) -> (obj == null
					|| type.isAssignableFrom(obj.getClass()) && (predicate.test(type.cast(obj)))));
		}
		return true;
	}

	/** Create a new test case based on this template.  
	 * 
	 * @return The created test case will have the current test case values as initial values. 
	 */
	default<TYPE> TestCase<TYPE> createTestCase() {
		return new TestCase<TYPE>(this, null); 
	}
	
	default<TYPE> TestCase<TYPE> createTestCase(String title) {
		TestCase<TYPE> result = createTestCase();
		result.setTitle(title); 
		return result; 
	}

	/**
	 * Create a new test case with initial entries followed by the given test values. 
	 * @param testValues The values added after initial entries. 
	 * @return The created new test case.
	 * @throws IllegalArgumentException Some of the test values were invalid. 
	 */
	default<TYPE> TestCase<TYPE> createTestCase(List<? extends Object> testValues) throws IllegalArgumentException {
		return createTestCase("", testValues, null, null, null); 
	}
	
	
	
	/**
	 * Create test case. 
	 * @param <TYPE> The result type of the test case. 
	 * @param title The title of the crated case. 
	 * @param defaultTestValues The default test parameters. Defaults to no default
	 *  parameters. 
	 * @param operation The operation performed by the test. Defaults to no default 
	 *  operation. 
	 * @param defaultResult The default result. Defaults to no result (empty value).  
	 * @param defaultException The default exception thrown. Defaults to no exception. 
	 * @return The created test case. 
	 * @throws IllegalArgumentException Any arguments were invalid. 
	 */
	default<TYPE> TestCase<TYPE> createTestCase(
			String title, 
			List<? extends Object> defaultTestValues, 
			Function<? super List<? extends Object>, ? extends TYPE> operation, 
			java.util.Optional<java.util.Optional<? extends TYPE>> defaultResult, 
			Throwable defaultException
			) throws IllegalArgumentException {
		TestCase<TYPE> result = createTestCase(title); 
		if (defaultTestValues != null) {
			result.addAll(defaultTestValues);
		}
		// The default result is either result or exception. 
		result.setDefaultResult(defaultResult, defaultException); 
	    
		
		return result;		
	}

	/**
	 * The list of initial entries of the test case.
	 * 
	 * @return The list of initial entries of the test case.
	 */
	public List<Object> initialEntries();

	/**
	 * Get the default predicate for default type. 
	 * @return The default predicate for default type. 
	 */
	default Predicate<Object> getDefaultPredicate() {
		return null; 
	}

	/**
	 * Add a new type with given description. 
	 * @param description The description. 
	 * @param type The type of the parameter. 
	 * @param valueTester The value tester testing the validity of the value. 
	 */
	default void addParameterType(String description, Class<?> type, Predicate<Object> valueTester) {
		addParameterType(type, valueTester); 
		List<String> descriptions = this.getDescriptions();
		if (descriptions != null) {
			descriptions.add(description); 
		}
	}

	/**
	 * Set the default exception. 
	 * @param exceptionType The exception type. 
	 */
	default void setDefaultException(Class<IllegalArgumentException> exceptionType) {
		setDefaultException(Optional.ofNullable(exceptionType)); 
	}

	/**
	 * Set the default result. 
	 * @param resultValue The default result value. 
	 */
	default<TYPE> void setDefaultExpectedResult(TYPE resultValue) {
		setDefaultExpectedResult(resultValue==null?Object.class:resultValue.getClass(), resultValue);
	}
	
	/**
	 * Set the default result. 
	 * @param <TYPE> The type of the result value. 
	 * @param type The type of the result. 
	 * @param resultValue The default result value. 
	 */
	default<TYPE> void setDefaultExpectedResult(Class<? extends TYPE> type, TYPE resultValue) {
		setDefaultExpectedResult(type, resultValue, null); 
	}

	/**
	 * Set the default result. 
	 * @param <TYPE> The type of the result value. 
	 * @param type The type of the result. 
	 * @param resultValue The default result value. 
	 * @param tester The predicate validating correct result. 
	 */
	public<TYPE> void setDefaultExpectedResult(Class<? extends TYPE> type, TYPE resultValue, Predicate<? super TYPE> tester);

}