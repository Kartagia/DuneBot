package com.kautiainen.antti.infinitybot;

import java.util.function.Predicate;

/**
 * Predicate testing validity of the exceptions. 
 * @author Antti Kautiainen
 *
 */
public interface DefaultExceptionPredicate extends Predicate<Throwable> {

	/**
	 * The valid class for the exception. 
	 * @return The class for valid exception.
	 */
	default Class<? extends Throwable> getValidClass() {
		return Throwable.class; 
	}
	
	/**
	 * Test the validity of the class. 
	 * @param tested The tested. 
	 * @return True, if and only if the given class is of valid class. 
	 */
	default boolean validClass(Class<?> tested) {
		return tested != null && getValidClass().isAssignableFrom(tested);
	}
	
	/**
	 * Test the validity of the class. 
	 * @param tested The tested. 
	 * @return True, if and only if the given throwable is of valid class. 
	 */
	default boolean validClass(Throwable tested) {
		return tested == null || validClass(tested.getClass());
	}
	
	/**
	 * Test validity of the given exception.
	 * @return True; if and only if the given tested is valid tested. 
	 */
	default boolean test(Throwable tested) {
		return validClass(tested); 
	}
	
}
