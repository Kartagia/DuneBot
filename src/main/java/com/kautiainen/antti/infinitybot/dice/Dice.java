package com.kautiainen.antti.infinitybot.dice;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The interface representing dice.
 * 
 * The dice is the dice results of their most recent roll, if any
 * exists. 
 *  
 * Dice are also valid dice results allowing easy construction 
 * of combined dice. 
 * 
 * @author Antti Kautiainen.
 *
 */
public interface Dice extends DiceResult {

	/**
	 * Get the list of all results.
	 * 
	 * @return The list of all possible results.
	 */
	public List<DiceResult> getAllResults();
		
	/**
	 * Get next random result of the dice.
	 * 
	 * @return The next random result of the die.
	 */
	default DiceResult getRandomResult() {
		return getRandomResult(new Random()); 
	}

	/**
	 * Get next random result of the dice using given random number generator.
	 * 
	 * @param random The random number generator.
	 * @return A randomly selected result.
	 */
	default DiceResult getRandomResult(Random random) {
		if (random == null) {
			return null;
		} else {
			List<DiceResult> sides = getAllResults();
			// Dealing first the case sides is undefined or empty. 
			if (sides == null || sides.isEmpty()) return null; 
			// The sides has values - getting randomly selected one. 
			return sides.get(random.nextInt(sides.size()));
		}		
	}
	
	/**
	 * The previous roll result. 
	 * @return The previous roll result, if any exists.
	 */
	default Optional<DiceResult> getLastResult() {
		return Optional.empty(); 
	}
	
	@Override
	default List<Object> getValues() {
		Optional<DiceResult> lastResult = this.getLastResult(); 
		return lastResult.isPresent()?lastResult.get().getValues():Collections.emptyList();
	}

	
	/**
	 * The value of the previous roll. If there is no previous roll, 
	 * calling this method would cause random roll. 
	 * @return The value of previous roll.
	 */
	default Integer getValue() {
		Optional<DiceResult> previous = this.getLastResult(); 
		DiceResult result; 
		if (previous.isPresent()) {
			// Getting the previous value. 
			result = previous.get(); 
		} else {
			// Rolling new random result. 
			result = getRandomResult();
		}
		return result == null?null:result.getValue();
	}
}