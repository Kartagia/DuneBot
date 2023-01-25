package com.kautiainen.antti.infinitybot.dice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Basic die is a a generic die storing the last roll. 
 * 
 * Basic Die represents a single die, whose value is stored. 
 * 
 * @author Antti Kautiainen. 
 *
 */
public class BasicDie implements Dice{

	/**
	 * The sides of the dice. 
	 */
	private List<DiceResult> sides; 
	
	/**
	 * The random number generator used. 
	 */
	private Random random; 

	/**
	 * The most recent roll result. 
	 */
	private Optional<DiceResult> lastResult = Optional.empty(); 

	
	/**
	 * Constructs a basic die with given sides. 
	 * 
	 * The list of sides is generated from the iteration of the given 
	 * sides collection. 
	 * 
	 * @param sides The sides of the created die. 
	 */
	public BasicDie(java.util.Collection<? extends DiceResult> sides) {
		this(new Random(), sides); 
	}

	/**
	 * Constructs a basic die with given randomizer and sides.
	 * 
	 * The list of sides is generated from the iteration of the given 
	 * sides collection. 
	 * 
	 * @param random The randomizer used to generate random numbers.  
	 * @param sides The sides of the created die. 
	 */
	public BasicDie(Random random, java.util.Collection<? extends DiceResult> sides) {
		this.random = random; 
		this.sides = (sides==null?Collections.emptyList():new ArrayList<>(sides)); 
	}

	

	@Override
	public Optional<DiceResult> getLastResult() {
		return this.lastResult; 
	}
	
	@Override
	public List<DiceResult> getAllResults() {
		return Collections.unmodifiableList(sides);
	}

	@Override
	final public DiceResult getRandomResult() {
		return getRandomResult(random); 
	}

	@Override
	public DiceResult getRandomResult(Random random) {
		this.lastResult = Optional.ofNullable(Dice.super.getRandomResult(random));
		
		return lastResult.orElse(null); 
	}

	@Override
	public void reroll() {
		this.lastResult = Optional.ofNullable(getRandomResult()); 
	}

	
}
