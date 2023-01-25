package com.kautiainen.antti.infinitybot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.kautiainen.antti.infinitybot.dice.ComboResult;
import com.kautiainen.antti.infinitybot.dice.Dice;
import com.kautiainen.antti.infinitybot.dice.DiceResult;
import com.kautiainen.antti.infinitybot.dice.NumericResult;

/**
 * Context of infinity games. 
 * @author Antti Kautiainen
 *
 */
public class InfinityContext {

	/**
	 * CombatDice implementation for Infinity. 
	 * @author Antti Kautainen. 
	 *
	 */
	public static class CombatDie implements Dice {
		
		/**
		 * The Effect face. 
		 */
		public static final DiceResult EFFECT_FACE = new ComboResult("S", 0); 

		
		/**
		 * The Two value face. 
		 */
		public static final DiceResult TWO_FACE = new NumericResult(2);
		
		/**
		 * The Zero face. 
		 */
		public static final DiceResult ONE_FACE = new NumericResult(1);
		
		/**
		 * The Zero face. 
		 */
		public static final DiceResult ZERO_FACE = new NumericResult(0);
		
	
		/**
		 * The list of dice results of the roll. 
		 */
		private java.util.List<DiceResult> results = 
				Arrays.asList(ONE_FACE, TWO_FACE, ZERO_FACE, ZERO_FACE, ZERO_FACE, EFFECT_FACE); 
		
		public CombatDie() {
			
		}
		
		public CombatDie(Random random) {
			this.setDefaultRandom(random); 
		}
		
		@Override
		public List<DiceResult> getAllResults() {
			return Collections.unmodifiableList(results);
		}

		/**
		 * The default random number generator. 
		 */
		private Random random = new Random(); 
		
		/**
		 * Get the current default random number generator. 
		 * If the generator is undefined, the dice has undefined value. 
		 * @return The current default random number generator. 
		 */
		protected Random getDefaultRandom() {
			return random; 
		}

		/**
		 * Set the current default random number generator.
		 * @param random The new random number generator.  
		 * @return The replaced default random number generator. 
		 */
		protected Random setDefaultRandom(Random random) {
			Random result= getDefaultRandom(); 
			this.random = random; 
			return result; 
		}

		
		@Override
		public DiceResult getRandomResult() {
			return getRandomResult(getDefaultRandom()); 
		}
		
		@Override
		public DiceResult getRandomResult(Random random) {
			if (random == null) {
				return null; 
			} else {
				List<DiceResult> sides = getAllResults(); 
				return sides.get(random.nextInt(sides.size())) ;
			}
		}
		
	}
	
	/**
	 * Constructs a new infinity context. 
	 */
	public InfinityContext() {
		
	}

}
