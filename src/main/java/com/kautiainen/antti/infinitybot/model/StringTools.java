package com.kautiainen.antti.infinitybot.model;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 * This class combines several string tools into same file.
 * 
 * @author Antti Kautiainen
 *
 */
public class StringTools {
	
	/**
	 * ChainedIntPredicate chains IntPredicates. 
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class ChainedIntPredicate implements java.util.function.IntPredicate {
		
		private final ChainedPredicate<Integer> implementation; 
		
		public ChainedIntPredicate(IntPredicate predicate, IntPredicate nextOnSuccess, IntPredicate nextOnFailure) {
			this(
					(Integer value)->(value != null && predicate != null && predicate.test(value)), 
					(Integer value) ->(value != null && nextOnSuccess != null && nextOnSuccess.test(value)), 
					(Integer value) ->(value != null && nextOnFailure != null && nextOnFailure.test(value))					
					);
		}
		
		public ChainedIntPredicate(IntPredicate predicate, IntPredicate nextPredicate) {
			this(
					(Integer value)->(value != null && predicate != null && predicate.test(value)), 
					(Integer value) ->(value != null && nextPredicate != null && nextPredicate.test(value))					
					);			
		}
		
		public ChainedIntPredicate(
				Predicate<? super Integer> predicate, 
				Predicate<? super Integer> nextOnSuccess, 
				Predicate<? super Integer> nextOnFailure 
				) {
			this.implementation = new ChainedPredicate<Integer>(predicate, nextOnSuccess, nextOnFailure);
		}
		
		public ChainedIntPredicate(
				Predicate<? super Integer> predicate, 
				Predicate<? super Integer> nextPredicate
				) {
			this(predicate, nextPredicate, nextPredicate);
		}
		
		@Override
		public boolean test(int value) {
			return implementation.test(value);
		}
	}

	/**
	 * Chained predicate continuing to switch the predicate according to the test result until
	 * the test predicate becomes undefined. At that point test always fails, and no update is 
	 * performed- 
	 * 
	 * @author Antti Kautiainen
	 *
	 * @param <TYPE> The type of the tested value.
	 */
	public static class ChainedPredicate<TYPE> implements java.util.function.Predicate<TYPE>  {
		
		/**
		 * The next predicate after successful test.If this value is undefined, the test chain is broken.
		 */
		private final Predicate<? super TYPE> nextSuccess_;
		
		/**
		 * The next predicate after failed test. If this value is undefined, the text chain is broken.
		 */
		private final Predicate<? super TYPE> nextFailure_;
		
		/**
		 * The current testing predicate. if this value is undefined, the test will always fail without updating
		 * the test predicate.
		 */
		private Predicate<? super TYPE> tester_;
		public ChainedPredicate(Predicate<? super TYPE> currentPredicate, 
				Predicate<? super TYPE> nextOnSuccess, 
				Predicate<? super TYPE> nextOnFailure) {
			this.tester_ = currentPredicate;
			this.nextSuccess_ = nextOnSuccess;
			this.nextFailure_ = nextOnFailure; 
		}
		
		/**
		 * Create a new test predicate with a current predicate, and same predicate following the current one. 
		 * 
		 * @param currentPredicate The current predicate.
		 * @param nextPredicate The next predicate used after current one unless the current one is undefined.
		 */
		public ChainedPredicate(Predicate<? super TYPE> currentPredicate, Predicate<? super TYPE> nextPredicate) {
			this(currentPredicate, nextPredicate, nextPredicate);
		}
				
		@Override
		public synchronized boolean test(TYPE tested) {
			if (this.tester_ == null) {
				return false;
			} else {
				boolean result = this.tester_.test(tested);
				// Moving the predicate.
				if (result)  {
					tester_ = this.nextSuccess_;
				} else {
					tester_ = this.nextFailure_;
				}
				return result;
			}
		}
		
	}
	
	/**
	 * Test if the given tested is a valid identifier.
	 * 
	 * @param tested The tested value.
	 * @return True, if and only if the given tested is a valid identifier.
	 */
	public static boolean validIdentifier(String tested) {
		if (tested == null || tested.isEmpty()) {
			return false;
		}
		
		return tested.codePoints().allMatch(new ChainedIntPredicate(
				(Integer codePoint)->(codePoint != null && Character.isUnicodeIdentifierStart(codePoint)), 
				(Integer codePoint)->(codePoint != null && Character.isUnicodeIdentifierPart(codePoint)),
				null
				));
	}
	
	/**
	 * Sentence word pattern.
	 */
	public static final Pattern SENTENCE_WORD_PATTERN = Pattern.compile("(?u:\\w+)");

	/**
	 * The sentence pattern consists several words ending to either end of string or comma.
	 */
	public static final Pattern SENTENCE_PATTERN = Pattern.compile("" + 
			SENTENCE_WORD_PATTERN + "(?u:\\p{Pu}\\s" + SENTENCE_WORD_PATTERN + ")*(?:$|[.!?])"
	); 
	
	public static boolean validStatement(String statement) {
		return statement != null && SENTENCE_PATTERN.matcher(statement).matches();
	}
}
