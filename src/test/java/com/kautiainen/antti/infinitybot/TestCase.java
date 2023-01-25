package com.kautiainen.antti.infinitybot;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.opentest4j.AssertionFailedError;

/**
 * Test case is class to build test cases.
 *  
 * @author Antti Kautiainen
 *
 *@param <TYPE> The default return type of the test case. 
 */
public class TestCase<TYPE> extends java.util.AbstractList<Object> implements TestCaseTemplate {

	/**
	 * Class representing return value which is either value or exception. 
	 * 
	 * @author Antti Kautiainen
	 *
	 * @param <TYPE> The return type of the value. 
	 * @param <EXCEPTION> The exception type of the thrown exception. 
	 */
	public static class ExceptionOrValue<TYPE, EXCEPTION extends Throwable> {

		/**
		 * Create new exception or value containing an exception and its type.
		 * 
		 * @param <TYPE> The type of the value.
		 * @param <EXCEPTION> The type of the exception.
		 * @param exceptionType The exception type.
		 * @param exception The exception of the created object.
		 * @return An exception or value containing given exception and its actual type type.
		 */
		public static<TYPE, EXCEPTION extends Throwable> ExceptionOrValue<TYPE, EXCEPTION> create(
				Class<? extends EXCEPTION> exceptionType, EXCEPTION exception) {
			return new ExceptionOrValue<TYPE, EXCEPTION>(exception); 
		} 
		
		/**
		 * Create a new exception or value containing an exception.
		 * 
		 * @param <TYPE> The type of the value.
		 * @param <EXCEPTION> The type of the exception.
		 * @param exceptionType The exception type.
		 * @param exception The exception of the created object.
		 * @return An exception or value containing given exception.
		 */
		public static<TYPE, EXCEPTION extends Throwable> ExceptionOrValue<TYPE, EXCEPTION> create(
				EXCEPTION exception) {
			return new ExceptionOrValue<TYPE, EXCEPTION>(exception); 
		} 
		
		/**
		 * Create a new exception or value containing a value of given optional.
		 * 
		 * @param <TYPE> The type of the value.
		 * @param <EXCEPTION> Type of the possibly contained exception.
		 * @param value The value wrapped into an optional.
		 * @return An exception or value containing given value.
		 */
		public static<TYPE, EXCEPTION extends Throwable> ExceptionOrValue<TYPE, EXCEPTION> create(
				Optional<Optional<? extends TYPE>> value) {
			return new ExceptionOrValue<TYPE, EXCEPTION>(value); 
		}

		/**
		 * Create a new exception or value containing a value.
		 * 
		 * @param <TYPE> The type of the value.
		 * @param <EXCEPTION> Type of the possibly contained exception.
		 * @param value The contained value.
		 * @return An exception or value containing given value.
		 */
		public static<TYPE, EXCEPTION extends Throwable> ExceptionOrValue<TYPE, EXCEPTION> create(TYPE value) {
			return new ExceptionOrValue<TYPE, EXCEPTION>(value); 
		}
	
		/**
		 * The optional containing the value allowing existing undefined value, and empty value.
		 */
		private Optional<Optional<TYPE>> value = Optional.empty();
		
		/**
		 * The contained exception.
		 */
		private EXCEPTION exception = null;
		
		/**
		 * Create an exception or value which have neither value nor exception. 
		 * This is suitable for procedure calls. 
		 */
		public ExceptionOrValue() {
			
		}
		
		/**
		 * Create an exception or value with given exception, and no value. 
		 * 
		 * @param exception The exception thrown. If this is undefined, no exception is
		 *  thrown. 
		 */
		public ExceptionOrValue(EXCEPTION exception) {
			this.exception = exception; 
		}
		
		/**
		 * Create an exception or value the value of given value.
		 * 
		 * @param value The optional containing the value, which may be undefined (<code>null</code>).
		 */
		public ExceptionOrValue(Optional<Optional<? extends TYPE>> value) {
			Optional<? extends TYPE> castValue = value.orElse(null); 
			if (castValue == null) {
				this.value = Optional.empty();
			} else {
				this.value = Optional.of(Optional.ofNullable((TYPE)(value.get().orElse(null))));
			}
		}
		
		/**
		 * Create an exception or value with given value, and no exception. 
		 * @param value
		 */
		public ExceptionOrValue(TYPE value) {
			this(Optional.of(Optional.ofNullable(value)));
		}
		
		/**
		 * Get the exception of the current exception or value.
		 * 
		 * @return The exception of the current value, if any exists.
		 */
		public Optional<EXCEPTION> getException() {
			return Optional.ofNullable(this.exception);
		}
		
		/**
		 * Get the safe value.
		 * @return The value, which is empty, if the exception or value has no value, and
		 *  contains the value wrapped into optional, if it exists. The value is acquired
		 *  with <code>getSafeValue().get().orElse(null)</code>.
		 */
		public Optional<Optional<TYPE>> getSafeValue() {
			return this.value;
		}
		
		/**
		 * Get the value, if it exists.
		 * 
		 * @return The value of the exception or value.
		 * @throws NoSuchElementException The value does not exist.
		 */
		public TYPE getValue() throws NoSuchElementException {
			return getSafeValue().get().orElse(null); 
		}
		
		/**
		 * Does the exception or value have an exception.
		 * 
		 * @return True, if and only if, the exception does exist.
		 */
		public boolean hasException() {
			return this.exception != null; 
		}

		/**
		 * Does the exception or value have a value.
		 * @return True, if and only if, the value does exist.
		 */
		public boolean hasValue() {
			return this.value.isPresent(); 
		}
	}

	/**
	 * Execute the test with given parameters. 
	 * @param expectedResult The expected result of the operation. 
	 * @param parameters The parameters of the operation. 
	 * @return True, if and only if the test passes. 
	 * @throws AssertionFailedError The operation failed. 
	 */
	public static<TYPE, EXCEPTION extends Throwable> ExceptionOrValue<TYPE, EXCEPTION> executeTest(
			Function<? super List<? extends Object>, ? extends TYPE> operation, 
			List<? extends Object> parameters
			) throws ClassCastException {
		try {
			return ExceptionOrValue.create((TYPE)operation.apply(parameters)); 
		} catch (Exception e) {
			// Testing the exception
			return (ExceptionOrValue<TYPE, EXCEPTION>)ExceptionOrValue.create(e);
		}
	}

	public static<TYPE, EXCEPTION extends Throwable> ExceptionOrValue<TYPE, EXCEPTION> executeTest(
			Function<List<? extends Object>, Optional<Optional<? extends TYPE>>> operation, 
			List<? extends Object> parameters,
			Class<? extends EXCEPTION> expectedException,
			Predicate<? super EXCEPTION> exceptionTester
			) throws AssertionFailedError {
		try {
			return (ExceptionOrValue<TYPE, EXCEPTION>)
					ExceptionOrValue.create(operation.apply(parameters).orElse(Optional.empty())); 
		} catch (Exception e) {
			// Testing the exception
			if (expectedException == null) {
				// Error: The exception should not e thrown. 
				throw new ClassCastException("Incompatible class of exception");
			} else if (expectedException.isAssignableFrom(e.getClass()) && 
					(exceptionTester == null || exceptionTester.test(expectedException.cast(e)))) {
				return (ExceptionOrValue<TYPE, EXCEPTION>)ExceptionOrValue.create(expectedException, e);
			} else {
				// We have an error
				throw new ClassCastException(
						String.format("Expected exception %s, but got %s", 
								expectedException.getClasses(), e.getClass())); 
			}
		}
	}

	/**
	 * The test case template.
	 */
	private TestCaseTemplate parent = null;

	/**
	 * The initial entries of the case.
	 */
	private java.util.List<Object> initial = new ArrayList<>();

	/**
	 * Entries of the case.
	 */
	private java.util.List<Object> entries = new ArrayList<>();

	/**
	 * Validators of the entry values.
	 */
	private java.util.List<Predicate<Object>> validators = new ArrayList<>();

	/**
	 * Types of values of values.
	 */
	private java.util.List<Class<?>> types = new ArrayList<>();

	private List<String> descriptions = new ArrayList<>();

	/**
	 * The mapping from throwable exceptions to the predicate testing the exception is valid.
	 */
	private java.util.Map<Class<? extends Throwable>, Predicate<? super Throwable>> validExceptions = 
			new java.util.HashMap<>();

	/**
	 * The default result. 
	 */
	private Optional<Optional<? extends TYPE>> defaultResult = Optional.empty();

	/**
	 * The default thrown exception. 
	 */
	private Optional<Class<? extends Throwable>> defaultException = Optional.empty();

	/**
	 * The current exception. If this value is undefined, the default exception is used. 
	 */
	private Optional<Throwable> exception = null;

	/**
	 * The exception tester in case the exception matches the default exception. 
	 * If this value is undefined, the default exception tester is used. 
	 */
	private Optional<DefaultExceptionPredicate> exceptionTester = null;

	/**
	 * The default operation. 
	 */
	private Function<? super List<? extends Object>, Optional<Optional<? extends TYPE>>> operation = null;

	/**
	 * The result of the most recent execution. 
	 */
	private Optional<Optional<? extends TYPE>> result = null;
	
	/**
	 * The expected result of the test. 
	 */
	private Optional<Optional<? extends TYPE>> expectedResult = Optional.empty();

	/**
	 * The expected exception of the text. 
	 */
	private Optional<Class<? extends Throwable>> expectedException = Optional.empty();

	/**
	 * The default exception tester. 
	 */
	private Optional<DefaultExceptionPredicate> defaultExceptionTester = Optional.empty();

	
	/**
	 * The default result type. 
	 */
	private Class<? extends TYPE> defaultResultType;

	private Optional<Predicate<? super TYPE>> defaultResultTester;

	private Throwable error;

	private String title;

	/**
	 * Creates a new empty test case.
	 * 
	 */
	public TestCase() {
		super();
	}

	/**
	 * Create a new test case from given template.
	 * 
	 * @param template The template from which the case is created.
	 */
	public TestCase(TestCaseTemplate template, Class<? extends TYPE> defaultResultType) {
		if (template != null) {
			this.defaultException = template.getDefaultException();
			this.defaultExceptionTester = template.getDefaultExceptionTester();
			this.defaultResult = null;
			this.defaultResultTester = Optional.empty();
			this.defaultResultType = defaultResultType;
			this.types.addAll(template.getParameterTypes());
			this.validators.addAll(template.getValueValidators());
			this.initial.addAll(template.initialEntries());
			this.entries.addAll(template.initialEntries());
		}
	}
	
	/**
	 * Create a new test case from the given test case.
	 * 
	 * @param templateCase The test case whose copy is created.
	 */
	public TestCase(TestCase<TYPE> templateCase) {
		if (templateCase != null) {
			this.defaultException = templateCase.getDefaultException();
			this.defaultExceptionTester = templateCase.getDefaultExceptionTester();
			this.defaultResult = templateCase.getDefaultResult();
			this.defaultResultTester = templateCase.getDefaultResultTester();
			this.defaultResultType = templateCase.getDefaultResultType();
			this.types.addAll(templateCase.getParameterTypes());
			this.validators.addAll(templateCase.getValueValidators());
			this.initial.addAll(templateCase.initialEntries());
			this.entries.addAll(templateCase.initialEntries());
		}
	}
	
	/**
	 * Get the default result type.
	 * 
	 * @return The default result type of the test case.
	 */
	private Class<? extends TYPE> getDefaultResultType() {
		return this.defaultResultType;
	}

	/**
	 * Get the default result of the test case.
	 * 
	 * @return The default test result.
	 */
	public Optional<Optional<? extends TYPE>> getDefaultResult() {
		return this.defaultResult;
	}

	/**
	 * Add test argument.
	 * 
	 * @param value The added value.
	 * @throws IllegalArgumentException The given argument was invalid.
	 */
	public boolean add(Object value) throws IllegalArgumentException {
		if (size() == types.size()) {
			// Adding new value of untested. object.
			try {
			if (add(value, (Class<TYPE>)getDefaultClass(), (Predicate<? super TYPE>)getDefaultPredicate())) {
				return true; 
			} else {
				throw new IllegalArgumentException("Cannot add new arguments!"); 
			}
			} catch(ClassCastException cce) {
				throw new IllegalArgumentException("Invalid value", cce);
			}
		} else if (this.validValue(size(), value)) {
			return entries.add(value);
		} else {
			throw new IllegalArgumentException(
					String.format("The value %s was invalid", value)); 
		}
	}
	
	/**
	 * Add test argument.
	 * 
	 * @param value     The added value.
	 * @param type      The type of the value.
	 * @param predicate The predicate of the value validity.
	 * @return True, if and the value was added to the test case.
	 * @throws IllegalArgumentExcpetion The type, predicate, or value was invalid.
	 * @throws IllegalStateException    The operation is not possible due test case
	 *                                  state.
	 */
	public boolean add(Object value, Class<TYPE> type, Predicate<? super TYPE> predicate) {
		return add(value, null, type, predicate); 
	} 

	/**
	 * Add test argument.
	 * 
	 * @param <TYPE>    Type of the argument.
	 * @param value     The added value.
	 * @param type      The type of the value.
	 * @param predicate The predicate of the value validity.
	 * @return True, if and the value was added to the test case.
	 * @throws IllegalArgumentExcpetion The type, predicate, or value was invalid.
	 * @throws IllegalStateException    The operation is not possible due test case
	 *                                  state.
	 */
	public boolean add(Object value, String description, Class<TYPE> type, Predicate<? super TYPE> predicate) {
		if (size() != types.size()) {
			throw new IllegalStateException("Cannnot add value with validation types to non-full test case");
		} else if (type != null && type.isAssignableFrom(value.getClass())) {
			if (addParameterType(type, predicate)) {
				this.getDescriptions().add(description);
				try {
					if (add(value)) {
						return true; 
					} else {
						throw new IllegalArgumentException("The value was invalid"); 
					}
				} catch(IllegalArgumentException iae) {
					// Removing the added type values
					this.getValueValidators().remove(this.getTypeCount()-1); 
					this.getParameterTypes().remove(this.getTypeCount()-1);
					this.getDescriptions().remove(this.getTypeCount()-1);
					throw iae; 
				}
			} else {
				throw new IllegalArgumentException("Could not register new type!"); 
			}
		} else if (type == null) {
			throw new IllegalArgumentException("Cannot add undefined type!"); 
		} else {
			throw new IllegalArgumentException("Value was not suitable for given type"); 
		}
	}

	/**
	 * Executes the operation and assigns the result. 
	 * @return Did the execution pass the test or not. 
	 */
	public boolean excecute() {
		return execute(this); 
	} 
	
	/**
	 * Executes the default operation with given parameters setting the result and exception
	 * according to the execution. 
	 * @param parameters The parameters for the operation. 
	 * @return Did the execution pass the test or not. 
	 */
	public boolean execute(
			Consumer<? super List<? extends Object>> operation, 
			List<? extends Object> parameters) { 
		final Function<List<? extends Object>, Optional<Optional<? extends TYPE>>> func = 
				(List<? extends Object> param)->{
					operation.accept(parameters);
					return Optional.empty(); 
				};
		return execute(func, parameters);
	}

	/**
	 * Executes the given operation with given parameters setting the result and exception
	 * according to the execution. 
	 * @param operation The executed operation. 
	 * @param parameters The parameters for the operation. 
	 * @return Did the execution pass the test or not. 
	 */
	public boolean execute(
			Function<List<? extends Object>, Optional<Optional<? extends TYPE>>> operation, 
			List<? extends Object> parameters) { 
		try {
			ExceptionOrValue<TYPE, ? extends Throwable> result = 
					(ExceptionOrValue<TYPE, ? extends Throwable>)executeTest(operation, parameters);
			if (this.getExpectedException().isPresent()) {
				this.setResult(Optional.empty(), getExpectedException().get().cast(result.getException()));
			} else {
				this.setResult(Optional.ofNullable(result.getSafeValue().orElse(null)), (Throwable)null);
			}
			return true; 
		} catch(ClassCastException cce) {
			this.setResult(Optional.empty(), cce);
			return false; 
		} catch(Throwable e) {
			this.setResult(Optional.empty(), e);
			return false; 
		}
		
	}
	
	/**
	 * Executes the default operation with given parameters setting the result and exception
	 * according to the execution. 
	 * @param parameters The parameters for the operation. 
	 * @return Did the execution pass the test or not. 
	 */
	public boolean execute(List<? extends Object> parameters) { 
		final Function<? super List<? extends Object>, Optional<Optional<? extends TYPE>>> operation = 
				this.getOperation(); 
		return execute( (List<? extends Object> list)->(
				operation==null?Optional.empty():operation.apply(list)), parameters);
	} 
	

	@Override
	public Object get(int index) {
		if (index >= 0 || index < size()) {
			return this.entries.get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the default class of new parameters. If this value is undefined, 
	 * no new parameters can be added. 
	 * @return The default class of a new parameter. 
	 */
	public Class<?> getDefaultClass() {
		return Object.class; 
	}
	
	
	/**
	 * Get default exception of the test case template. 
	 * @return The default exception, if the test case throws exception. 
	 */
	public Optional<Class<? extends Throwable>> getDefaultException() {
		return this.defaultException == null?Optional.empty():defaultException; 
	}

	/**
	 * The default tester of the exception. 
	 * @return The default tester of the exception.
	 */
	public Optional<DefaultExceptionPredicate> getDefaultExceptionTester() {
		return this.defaultExceptionTester ; 
	}
	
	
	/**
	 * The default operation. 
	 * @return The default operation. 
	 */
	public Function<? super List<? extends Object>, Optional<Optional<? extends TYPE>>> getDefaultOperation() {
		return null;
	}
	
	@Override
	public List<String> getDescriptions() {
		return this.descriptions; 
	} 
	
	/**
	 * The exception of the most recent execution. 
	 * @return The exception of the most recent execution. If there is none
	 *  the execution succeeded. 
	 */
	public Optional<? extends Throwable> getException() {
		return this.exception; 
	} 

	/**
	 * Get the exception tester testing the result exception. 
	 * @return The current exception tester. If the exception tester does not exist, 
	 *  the default value is used.  
	 */
	public Optional<DefaultExceptionPredicate> getExceptionTester() {
		return exceptionTester==null?this.getDefaultExceptionTester():exceptionTester; 
	}
	
	@Override
	public List<Class<? extends Throwable>> getExceptionTypes() {
		return new ArrayList<Class<? extends Throwable>>(validExceptions.keySet()); 
	}
	
	/**
	 * Get the expected exception. 
	 * @return The exception expected by the call. 
	 */
	public Optional<Class<? extends Throwable>> getExpectedException() {
		return this.expectedException==null?this.getDefaultException():expectedException;
	}
	
	/**
	 * Get the expected result. 
	 * @return The optional which is empty, if there is no expected return value. 
	 */
	private Optional<Optional<? extends TYPE>> getExpectedResult() {
		return expectedResult==null?this.defaultResult:expectedResult;
	} 
	
	/**
	 * Get the tester validating that the list contains only elements of specific type. 
	 * @param <CONTENT_TYPE> The content type of the list. 
	 * @param contentType The content type of the list. 
	 * @return The predicate testing that the all elements of the given list belongs to the given
	 *  class. 
	 */
	public<CONTENT_TYPE> Predicate<Object> getListValidator(Class<CONTENT_TYPE> contentType) {
		Predicate<CONTENT_TYPE> contentTester = (CONTENT_TYPE content) -> (contentType == null || contentType.isAssignableFrom(content.getClass()));
		return getListValidator((List<? extends CONTENT_TYPE> list)->(list != null), contentTester); 
	}

	/**
	 * Get the tester validating that the list passes both list tester, and every element
	 * of an existing list passes the content tester. 
	 * @param <CONTENT_TYPE> The content type of the list. 
	 * @param listTester The list tester testing validity of the list. If undefined, all values
	 *  passes the test. 
	 * @param contentTester The content tester used to test contents of a valid list. If undefined,
	 *  all values passes the test.  
	 * @return Predicate returning true, if and only if both list passes the list tester, 
	 *  and every element of an existing list passes the content tester. 
	 */
	public<CONTENT_TYPE> Predicate<Object> getListValidator(
			Predicate<? super List<? extends CONTENT_TYPE>> listTester, 
			Predicate<? super CONTENT_TYPE> contentTester) {
		Class<?> type = List.class; 
		
		return 				
				(Object list) -> {
					if (list instanceof java.util.List) {
			try {
				List<? extends CONTENT_TYPE> typedList = (List<? extends CONTENT_TYPE>)list;
				if (listTester != null && !listTester.test(typedList)) return false; 
				
				if (list != null && contentTester != null) {
					// TEsting content for existing list. 
					return type.isAssignableFrom(list.getClass()) && 
						typedList.stream().allMatch((CONTENT_TYPE content)-> (contentTester.test(content)));
				} else {
					// Undefined list is valid, if it passes the list tester. 
					return true; 
				}
			} catch(Exception e) {
				return false; 
			}
					}
			return false; 
		};
		
	} 
	
	/**
	 * The operation the test executes. 
	 * @return The function returning the result, if any exists.  
	 */
	public Function<? super List<? extends Object>, Optional<Optional<? extends TYPE>>> getOperation() {
		return this.operation == null?getDefaultOperation():operation; 
	}

	/**
	 * The list of known types.
	 * 
	 * @return The list of known types.
	 */
	public java.util.List<Class<?>> getParameterTypes() {
		return types;
	} 
	
	/**
	 * Get the expected result. 
	 * @return The optional which is empty, if there is no expected return value. 
	 * @throws NoSuchElementException There is no result. 
	 */
	private Optional<Optional<? extends TYPE>> getResult() throws NoSuchElementException {
		if (result == null) throw new NoSuchElementException("The result has not been set!"); 
		return result; 
	}

	/**
	 * Get the typed value.
	 * 
	 * @param <PARAM_TYPE> Type of the returned parameter value.
	 * @param index  The index of the queried value.
	 * @return The typed value, if such value exists.
	 * @throws ClassCastException The value could not be casted to the type.
	 */
	public <PARAM_TYPE> Optional<PARAM_TYPE> getTypedValue(int index) throws ClassCastException {
		Class<? extends PARAM_TYPE> type = (Class<? extends PARAM_TYPE>) getType(index);
		if (type != null) {
			return Optional.ofNullable(type.cast(get(index)));
		}
		return Optional.empty();
	} 
	
	/**
	 * The list of known type validators.
	 * 
	 * @return The list of known types.
	 */
	public java.util.List<Predicate<Object>> getValueValidators() {
		return validators;
	}

	@Override
	public List<Object> initialEntries() {
		return initial;
	}

	public boolean passed() {
		try {
			Optional<Class<? extends Throwable>> expectedException = this.getExpectedException();
			if (expectedException.isPresent()) {
				// The result should be exception. 
				Throwable exception = this.getException().orElse(null);
				Optional<DefaultExceptionPredicate> tester = this.getExceptionTester();
				return exception != null && (!tester.isPresent() || tester.get().test(exception));
			} else {
				// We have value. 
				Optional<Optional<? extends TYPE>> result = getResult(), 
						expResult = this.getExpectedResult(); 
				return expResult.equals(result); 
			}
		} catch(NoSuchElementException e) {
			return false; 
		}
	}
	
	@Override
	public Object set(int index, Object value) throws IllegalArgumentException, IndexOutOfBoundsException {
		if (validValue(index, value)) {
			return this.entries.set(index, value);
		} else {
			throw new IllegalArgumentException("The value was not valid");
		}
	}

	@Override
	public void setDefaultExceptionTester(DefaultExceptionPredicate tester) {
		this.defaultExceptionTester = Optional.ofNullable(tester);
	}

	
	public Optional<Predicate<? super TYPE>> getDefaultResultTester() {
		return this.defaultResultTester;
	}
	
	@Override
	public <RESULT> void setDefaultExpectedResult(Class<? extends RESULT> type, 
			RESULT resultValue,
			Predicate<? super RESULT> tester) 
	throws ClassCastException{
		Class<? extends TYPE> castType = (Class<? extends TYPE>)type; 
		this.defaultResultType = castType;
		this.defaultResult = Optional.of((Optional.ofNullable(castType.cast(type)))); 
		this.defaultResultTester = Optional.ofNullable(tester==null?null:(Predicate<TYPE>)(TYPE tested) -> {
			try {	
				return (tested == null || castType.isAssignableFrom(tested.getClass()))?
						tester.test((RESULT)castType.cast(tested)):false;
			} catch(ClassCastException cce) {
				return false; 
			}
		});
	}
	

	/**
	 * Set the default result. 
	 * @param defaultResult The default result. The undefined value indicates the default value
	 *  is unset. Empty value indicates that the default operation does not return value nor 
	 *  throw exception.  
	 * @param defaultException The default exception. The default exception thrown by the 
	 *  operation. Empty value indicates that the operation
	 *  will not throw an exception by default. 
	 * @param defaultExceptionPredicate The default tester of exception, if the exception is of
	 *  valid type. Defaults to undefined test passing everything. 
	 * @throws IllegalStateException The setting of default result is not allowed.
	 * @throws IllegalArgumentException The given default result or default exception is invalid.
	 * @param <EXCEPTION> The exception type of the default result.  
	 */
	public<EXCEPTION extends Throwable> void setDefaultResult(
			Optional<Optional<? extends TYPE>> defaultResult, 
			Class<? extends EXCEPTION> defaultException, 
			Predicate<? super EXCEPTION> defaultExceptionTester) {
		this.defaultResult = defaultResult; 
		this.defaultException = Optional.ofNullable(defaultException);
		this.defaultExceptionTester = 
				defaultExceptionTester==null?Optional.empty():
				Optional.of(new DefaultExceptionTester(defaultException, defaultExceptionTester));
	}
	
	
	/**
	 * Set the default result. 
	 * @param defaultResult The default result. The undefined value indicates the default value
	 *  is unset. Empty value indicates that the default operation does not return value nor 
	 *  throw exception.  
	 * @param defaultException The default exception. The default exception thrown by the 
	 *  operation. Empty value indicates that the operation
	 *  will not throw an exception by default. 
	 * @param defaultExceptionPredicate The default tester of exception, if the exception is of
	 *  valid type. Defaults to undefined test passing everything. 
	 * @throws IllegalStateException The setting of default result is not allowed.
	 * @throws IllegalArgumentException The given default result or default exception is invalid.
	 * @param <EXCEPTION> The exception type of the default result.  
	 */
	public<EXCEPTION extends Throwable> void setDefaultResult(Optional<Optional<? extends TYPE>> defaultResult, 
			Optional<Class<? extends EXCEPTION>> defaultException, 
			Predicate<? super EXCEPTION> defaultExceptionTester) {
		this.setDefaultResult(defaultResult, 
				(defaultException!=null && defaultException.isPresent()?
						defaultException.get():
						(Class<? extends EXCEPTION>)null), 
				defaultExceptionTester);
	}

	
	/**
	 * Set the default result. 
	 * @param defaultResult The default result. The undefined value indicates the default value
	 *  is unset. Empty value indicates that the default operation does not return value nor 
	 *  throw exception.  
	 * @param defaultException The default exception. The default exception thrown by the 
	 *  operation. Empty value indicates that the operation
	 *  will not throw an exception by default. 
	 * @throws IllegalStateException The setting of default result is not allowed.
	 * @throws IllegalArgumentException The given default resull or default exception is invalid. 
	 */
	public void setDefaultResult(Optional<Optional<? extends TYPE>> defaultResult, 
			Optional<Throwable> defaultException) {
		setDefaultResult(defaultResult, 
				Optional.ofNullable(defaultException.isPresent()?defaultException.get().getClass():null), 
				(Throwable exception)->(
					(!defaultException.isPresent()) || (defaultException.get().equals(exception))
				)
				); 
	}
	
	/**
	 * Set the default result. 
	 * @param defaultResult The default result. The undefined value indicates the default value
	 *  is unset. Empty value indicates that the default operation does not return value nor 
	 *  throw exception.  
	 * @param defaultException The default exception. The default exception thrown by the 
	 *  operation. Empty value indicates that the operation
	 *  will not throw an exception by default. 
	 * @throws IllegalStateException The setting of default result is not allowed.
	 * @throws IllegalArgumentException The given default resull or default exception is invalid. 
	 */
	public void setDefaultResult(Optional<Optional<? extends TYPE>> defaultResult, 
			Throwable defaultException) {
		this.setDefaultResult(defaultResult, Optional.ofNullable(defaultException));
	}
	

	/**
	 * Set the default result. 
	 * @param result The default result. The undefined value indicates the default value
	 *  is unset. Empty value indicates that the default operation does not return value nor 
	 *  throw exception.  
	 * @param exception The exception thrown by the execution. If undefined, no exception is thrown.
	 * @throws IllegalStateException The setting of default result is not allowed.
	 * @throws IllegalArgumentException The given result or exception is invalid. 
	 */
	public void setResult(Optional<Optional<? extends TYPE>> result, 
			Throwable exception) {
		if (result != null && exception != null && result.isPresent() && exception != null) {
			// The value would both return value and throw exception.
			throw new IllegalArgumentException("Test case cannot throw exception and return value at the same time");
		}
		this.exception = Optional.ofNullable(exception); 
		this.result = result; 
		if (this.passed()) {
			this.error = null; 
		} else if (result.isPresent() && !this.validResult(result)) {
			this.error = getInvalidResultError(); 
		} else {
			this.error = getInvalidExceptionError(); 
		}
	}
	
	
	private Throwable getInvalidExceptionError() {
		return new InvalidExceptionException(this.getException().orElse(null)); 
	}

	/**
	 * Test validity of the given result. 
	 * @param result The tested result. 
	 * @return True, if and only if the result is valid.  
	 */
	public boolean validResult(Optional<Optional<? extends TYPE>> result) {
		Optional<Optional<? extends TYPE>> expected = this.getExpectedResult();
		return expected.equals(result == null?Optional.empty():result); 
	}

	/**
	 * AssertionFailureException is a set of exceptions representing failed assertion. 
	 * 
	 * @author Antti Kautiainen
	 */
	public static class AssertionFailureException extends Exception {
		/**
		 * The serialization version of the assertion failure.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Create a new assertion failure exception with given message and without no cause.
		 * 
		 * @param message the message of the exception.
		 */
		public AssertionFailureException(String message) {
			this(message, null); 
		}
		
		/**
		 * Create a new assertion failure with a message and a cause.
		 * 
		 * @param message The message of the exception.
		 * @param cause The cause of the exception.
		 */
		public AssertionFailureException(String message, Throwable cause) {
			super(message==null?"":message, cause); 
		}
	
	}
	
	/**
	 * InvalidResultException represents exception caused by an invalid result value.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class InvalidResultException extends AssertionFailureException {
		
		/**
		 * The serialization version of the invalid result exception.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * The invalid result.
		 */
		private final Optional<Object> result; 
		
		public InvalidResultException(Optional<? extends Object> result) {
			this(result, "Invalid result"); 
		}
		
		public InvalidResultException(Object result) {
			this(Optional.ofNullable(result), "Invalid result"); 
		}
		
		public InvalidResultException(Optional<? extends Object> result, String message) {
			super(message); 
			this.result = result==null?null:Optional.ofNullable(result.orElse(null)); 
		}
		
		public Optional<Optional<Object>> getResult() {
			return Optional.ofNullable(result); 
		}
		
		public String toString() {
			return this.getMessage(); 
		}
	}
	
	/**
	 * InvalidExceptionException represents exception caused by an invalid exception thrown.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class InvalidExceptionException extends AssertionFailureException {
		/**
		 * The current serialization version.
		 */
		private static final long serialVersionUID = 1L;
		private final Throwable exception; 
	
		public InvalidExceptionException(Throwable exception) {
			this(exception, "Invalid exception"); 
		}
		
		public InvalidExceptionException(Throwable exception, String message) {
			super(message); 
			this.exception = exception; 
		}
		
		
		
		public Optional<? extends Throwable> getException() {
			return Optional.ofNullable(exception); 
		}
		
		public String toString() {
			String message = exception==null?null:exception.getMessage();
			if (message == null || message.isEmpty()) {
				return ""; 
			}
			return String.format("%s %s: %s", super.getMessage(), exception.getClass(), message);
		}
	}
	
	
	
	/**
	 * Get the error of invalid result. 
	 * 
	 * @return The cuase of the invalid result error.
	 */
	private Throwable getInvalidResultError() {
		// TODO Auto-generated method stub
		return new InvalidResultException(this.getResult().orElse(null));
	}

	public int size() {
		return entries.size();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0, end = size(); i < end; i++) {
			if (result.length() > 0)
				result.append(", ");
			result.append(String.format("\"%s\":%s", String.valueOf(get(i)), getType(i).getName()));
		}
		result.insert(0, "[");
		result.append("]");
		return result.toString();
	}
	
	/**
	 * Test validity of the value of index.
	 * 
	 * @param index The index.
	 * @param value The tested value.
	 * @return True, if and only if the value is valid for given index.
	 */
	public boolean validType(int index, Object value) {
		Class<?> type = getType(index);
		return (value == null) || ((type != null) && type.isAssignableFrom(value.getClass()));
	}

	/**
	 * Test validity of the value of index.
	 * 
	 * @param index The index.
	 * @param value The tested value.
	 * @return True, if and only if the value is valid for given index.
	 */
	public boolean validValue(int index, Object value) {
		Predicate<Object> tester = this.getValidator(index);
		return validType(index, value) && (tester == null || tester.test(value));
	}

	public void setOperation(Function<? super List<? extends Object>, ? extends TYPE> operation) {
		Function<List<? extends Object>, Optional<Optional<? extends TYPE>>> func = 
				(List<? extends Object> list)->{
					if (operation == null) return Optional.empty(); 
					Optional<? extends TYPE> result = 
							Optional.ofNullable(operation.apply(list)); 
					return Optional.of(result);
				};
		this.operation = func; 
	}

	/**
	 * Get the error causing the failure of the parse. 
	 * @return The error causing the failure of the operation. 
	 */
	public Throwable error() {
		return this.error; 
	}

	/** The title of the test case. 
	 * 
	 * @return The title of the test case. 
	 */
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(CharSequence title) {
		this.title = title==null?"":title.toString(); 
	}
	
}