package com.kautiainen.antti.infinitybot.dice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Simple die rolls a simple die, and stores the result of the roll.
 * 
 * 
 * @author Antti Kautiainen
 *
 */
public class SimpleDie extends BasicDie {

	/**
	 * Create sides of the simple die.
	 * 
	 * This is a standard die with values from 1 to sides.
	 * 
	 * @param sides The number of sides the die has. If sides is negative, an empty
	 *              dice is returned.
	 * @return The list of sides for a basic die with given sides.
	 */
	public static List<DiceResult> createSides(int sides) {
		return createSides(1, sides, 1);
	}

	/**
	 * Creates sides of the simple die.
	 * 
	 * @param start     The first value (inclusive).
	 * @param end       The last value (Inclusive).
	 * @param increment The increment. This value has to be at least 1.
	 * @return The list of the sides.
	 */
	public static List<DiceResult> createSides(int start, int end, int increment) throws IllegalArgumentException {
		ArrayList<DiceResult> result = new ArrayList<>();
		if (Math.signum(end - start) != Math.signum(increment)) {
			throw new IllegalArgumentException("Invalid increment!");
		} else if (start > end) {
			for (int i = start; i >= end; i += increment) {
				result.add(new NumericResult(i)); 
			}

		} else {
			for (int i = start; i <= end; i += increment) {
				result.add(new NumericResult(i)); 
			}
		}

		return result;
	}

	/**
	 * Create a new 6 sided die with default random number generator. 
	 */
	public SimpleDie() {
		this(new Random()); 
	}
	
	/**
	 * Create a new simple 6 sided die, which uses given random number generator. 
	 * @param random The rnadom number gnerator. 
	 */
	public SimpleDie(Random random) {
		this(random, 6);		
	}

	/**
	 * Create a new simple die with given random generator and number of sides. 
	 * 
	 * The die will have sides from 1 to sides, if sides is positive.
	 * If sides is negative or zero, the die will have no sides. 
	 * 
	 * @param random The random number generator used to generate random values. 
	 * @param sides The number of sides the die have. 
	 */
	public SimpleDie(Random random, int sides)  {
		this(random, createSides(sides)); 
	}
	
	/**
	 * Create a new simple basic die with given number of sides.
	 * 
	 * The die will have sides from 1 to sides, if sides is positive.
	 * If sides is negative or zero, the die will have no sides. 
	 *  
	 * @param sides The number of sides. 
	 */
	public SimpleDie(int sides) {
		this(new Random(), createSides(sides));
	}

	/** {@inheritDoc} */
	public SimpleDie(Collection<? extends DiceResult> sides) {
		super(new Random(), sides);
	}


	/**
	 * {@inheritDoc}
	 * @param random {@inheritDoc}
	 * @param sides {@inheritDoc}
	 */
	public SimpleDie(Random random, Collection<? extends DiceResult> sides) {
		super(random, sides);
	}
}
