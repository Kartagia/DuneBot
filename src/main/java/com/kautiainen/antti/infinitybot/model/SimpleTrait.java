package com.kautiainen.antti.infinitybot.model;

import java.util.Optional;

/**
 * Simple trait.
 * 
 * @author Antti Kautiainen.
 *
 */
public class SimpleTrait implements Trait {

	/**
	 * The trait name.
	 */
	private String myName_; 
	
	/**
	 * The trait level.
	 */
	private Optional<Integer> myLevel_;

	/**
	 * The trait description.
	 */
	private Optional<String> myDescription_; 
	
	public SimpleTrait(String traitName) {
		this(traitName, 1); 
	}
	
	public SimpleTrait(String name, int level) 
	throws IllegalArgumentException {
		this(name, level, null);
	}
	
	public SimpleTrait(String name, Integer level, String description)
	throws IllegalArgumentException {
		if (validName(name) ) {
			myName_ = name; 				
		} else {
			throw new IllegalArgumentException("Invalid trait name");
		}
		if (validLevel(level)) {
			myLevel_ = level==null?getDefaultLevel():Optional.of(level);
		} else {
			throw new IllegalArgumentException("Invalid trait level");
		}
		myDescription_ = Optional.ofNullable(description); 
	}
	
	@Override
	public String getName() {
		return myName_;
	}

	@Override
	public Optional<Integer> getLevel() {
		return myLevel_; 
	}
	
	@Override
	public Optional<String> getDescription() {
		return myDescription_;
	}

	@Override
	public Trait getStacked(int value) {
		return new SimpleTrait(getName(), getLevel().orElse(0) + value, getDescription().orElse(null));
	}
	
}