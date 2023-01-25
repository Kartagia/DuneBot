package com.kautiainen.antti.infinitybot;

/**
 * Implementation of the default tester allowing creating default exception
 * testers with predicate testing validity of the exception. 
 * @author Antti Kautiainen
 *
 */
public class DefaultExceptionTester implements DefaultExceptionPredicate {

	private Class<? extends Throwable> validClass = null; 
	
	private java.util.function.Predicate<? super Throwable> tester = null; 
	
	/**
	 * Create a new default exception tester with given required class.
	 * @param type The type of the accepted exceptions. Defaults to {@linkplain Throwable}.
	 */
	public DefaultExceptionTester(Class<? extends Throwable> type) {
		this.validClass = type; 
	}
	
	/**
	 * Create a new tester testing valid exceptions. 
	 * @param tester The tester of exceptions. 
	 */
	public DefaultExceptionTester(java.util.function.Predicate<? super Throwable> tester) {
		this.tester = tester; 
	}
	
	/**
	 * Create a new default exception tester with given required class, and tester of exception.
	 * @param type The type of the accepted exceptions.  
	 * @param tester The tester testing valid instances of the given type. If undefined, all
	 *  values are valid. 
	 * @throws IllegalArgumentException The given class was undefined. 
	 */
	@SuppressWarnings("unchecked")
	public<EXCEPTION extends Throwable> DefaultExceptionTester(
			Class<? extends Throwable> type, 
			java.util.function.Predicate<? super EXCEPTION> tester) 
	throws IllegalArgumentException {
		if (type == null) throw new IllegalArgumentException("Invalid type of exception"); 
		this.validClass = type; 
		this.tester = (Throwable exception) -> {
			try {
				Class<? extends EXCEPTION> castedType = (Class<? extends EXCEPTION>)type;
				return tester == null || tester.test(castedType.cast(exception));
			} catch(ClassCastException cce) {
				return false;
			}
		};
	}
	
	
	/**
	 * Create a new default exception tester with given required class, and tester of exception.
	 * @param type The type of the accepted exceptions.  
	 * @param tester The tester testing valid instances of the given type. If undefined, all
	 *  values are valid. 
	 * @throws IllegalArgumentException The given class was undefined. 
	 */
	@SuppressWarnings("unchecked")
	public<EXCEPTION extends Throwable> DefaultExceptionTester(
			EXCEPTION exception, 
			java.util.function.Predicate<? super EXCEPTION> tester) 
	throws IllegalArgumentException {
		this(exception==null?(Class<? extends EXCEPTION>)null:exception.getClass(), tester);
	}

	@Override
	public Class<? extends Throwable> getValidClass() {
		return validClass==null?DefaultExceptionPredicate.super.getValidClass():validClass;
	}

	@Override
	public boolean test(Throwable tested) {
		return DefaultExceptionPredicate.super.test(tested) &&
				(tester == null || tester.test(tested)); 
	}
	
	

}
