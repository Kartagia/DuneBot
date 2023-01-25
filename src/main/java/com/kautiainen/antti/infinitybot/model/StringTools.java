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
	 * Test if hte given code point may start an XML identifier or name.
	 * @param codePoint The tested code point.
	 * @return True, if and only if the given code point may start XML identifier.
	 */
	public static boolean isIdentifierStart(int codePoint) {
		return Character.isUnicodeIdentifierStart(codePoint) || '_' == codePoint 
		|| codePoint == '\u003a' 
		|| (((int)'\u00C0') <= codePoint && codePoint <= ((int)'\u00D6'))
		|| (((int)'\u00d8') <= codePoint && codePoint <= ((int)'\u00f6'))
		|| (((int)'\u00f8') <= codePoint && codePoint <= ((int)'\u02ff'))
		|| (((int)'\u00f8') <= codePoint && codePoint <= ((int)'\u02ff'))
		|| (((int)'\u0370') <= codePoint && codePoint <= ((int)'\u037d'))
		|| (((int)'\u037f') <= codePoint && codePoint <= ((int)'\u1fff'))
		|| (((int)'\u200c') <= codePoint && codePoint <= ((int)'\u200d'))
		|| (((int)'\u2070') <= codePoint && codePoint <= ((int)'\u21bf'))
		|| (((int)'\u2c00') <= codePoint && codePoint <= ((int)'\u2fef'))
		|| (((int)'\u3001') <= codePoint && codePoint <= ((int)'\ud7ff'))
		|| (((int)'\uf900') <= codePoint && codePoint <= ((int)'\ufdcf'))
		|| (((int)'\ufdf0') <= codePoint && codePoint <= ((int)'\ufffd'))
		|| ((1<<16) <= codePoint && codePoint <= (15<<16-1))
		;
	}

	/**
	 * Test if hte given code point may belong to an XML identifier or name, but may not start one.
	 * @param codePoint The tested code point.
	 * @return True, if and only if the given code point may be part of the XML identifier, but not
	 * the first codepoint. 
	 */
	public static boolean isIdentifierPart(int codePoint) {
		return isIdentifierStart(codePoint) || Character.isDigit(codePoint) 
		|| codePoint == '\u002e' || codePoint == '-' || codePoint == '\u00b7'
		|| (((int)'\u0300') <= codePoint && codePoint <= ((int)'\u036f'))
		|| (((int)'\u203f') <= codePoint && codePoint <= ((int)'\u2040'))
		;
	}

	/**
	 * Test if the given code point may start an XML NC name.
	 * @param codePoint The tested code point.
	 * @return True, if and only if the given code point may start an XML NC name.
	 */
	public static boolean isQualifiedNameStart(int codePoint) {
		return isIdentifierStart(codePoint);
	}
	/**
	 * Test if the given code point may belong to an XML NC name, but may not start one.
	 * @param codePoint The tested code point.
	 * @return True, if and only if the given code point may belong to the XML NC name, but
	 *  may not start it.
	 */
	 public static boolean isQualifiedNamePart(int codePoint) {
		return codePoint != ':' && isIdentifierPart(codePoint);
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
		int index = 0;
		for (int cp: tested.codePoints().toArray()) {
			if (index == 0) {
				if (!isIdentifierStart(cp)) {
					return false;
				}
			} else {
				if (!isIdentifierPart(cp)) {
					return false;
				}
			}
			index++;
		}
		return true;
	}
	
	/**
	 * Sentence word pattern.
	 */
	public static final Pattern SENTENCE_WORD_PATTERN = Pattern.compile("(?u:\\w+)");

	/**
	 * The sentence pattern consists several words ending to either end of string or comma.
	 */
	public static final Pattern SENTENCE_PATTERN = Pattern.compile("" + 
			SENTENCE_WORD_PATTERN + "(?u:\\p{Punct}\\s" + SENTENCE_WORD_PATTERN + ")*(?:$|[.!?])",
			Pattern.UNICODE_CHARACTER_CLASS
	); 
	
	public static boolean validStatement(String statement) {
		return statement != null && SENTENCE_PATTERN.matcher(statement).matches();
	}
}
