package com.kautiainen.antti.infinitybot.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.kautiainen.antti.infinitybot.dune.DuneCharacter;

/**
 * Talent represents single talent of a character.
 * 
 * @author Antti Kautiainen
 *
 */
public class Talent implements Comparable<Talent> {

	/**
	 * Requirement determines the requirement. 
	 * 
	 * @author Antti Kautiainen
	 *
	 * @param <TYPE> The type of the requirement.
	 */
	public static class Requirement<TYPE> {
		
		/**
		 * The caption of the requirement.
		 */
		private String myTarget_; 
		
		/**
		 * The predicate testing the target.
		 */
		private Predicate<? super TYPE> myValue_;

		/**
		 * Create a new requirement of a target and predicate.
		 * 
		 * @param target The target of the requirement. Must be non-null.
		 * @param value the predicate of the value validity. Defaults to an existence
		 *  test.
		 * @throws IllegalArgumentException Either target or value was invalid.
		 */
		public Requirement(String target, Predicate<? super TYPE> value) 
		throws IllegalArgumentException {
			if (target == null) throw new IllegalArgumentException("target");
			this.myTarget_ = target;
			this.myValue_ = value == null?((Object tested)->(tested != null)):value;
		}
		
		/**
		 * Create a new requirement only requiring existence of the requirement.
		 * 
		 * @param target The target value.
		 */
		public Requirement(TYPE target) {
			this(target==null?null:target.toString(), null); 
		}
		
		/**
		 * The predicate testing the validity of the target.
		 * 
		 * @return Always defined predicate ensuring the predicate is correct.
		 */
		public Predicate<? super TYPE> getPredicate() {
			return myValue_;
		}
		
		/**
		 * Get the target name.
		 * 
		 * @return The string containing the target name.
		 */
		public String getTarget() {
			return myTarget_;
		}
		
		/**
		 * Test the requirement.
		 * 
		 * @param value The tested value.
		 * @return True, if and only if the given value fulfills the requirement.
		 */
		public boolean test(TYPE value) {
			return myValue_.test(value); 
		}
		
		@Override
		public String toString() {
			String target = getTarget();
			return target==null?"":target;
		}
		
	}
	
	/**
	 * Trait requirement requiring certain level of trait.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class TraitRequirement 
		extends Requirement<Trait> {
		
		private Integer lowerBoundary_ = null;
		
		private Integer upperBoundary_ = null;
		
		public TraitRequirement(String traitName) {
			super(traitName, null);
		}

		/**
		 * Create a new trait requirement with a trait name, and possible lower and upper boundary.
		 * @param traitName The trait name.
		 * @param lowerBoundary The lower boundary. Defaults to no boundary.
		 * @param upperBoundary The upper boundary. Defaults to no boundary.
		 * @throws IllegalArgumentException The trait name is invalid.
		 */
		public TraitRequirement(String traitName, Integer lowerBoundary, Integer upperBoundary)
		throws IllegalArgumentException {
			super(traitName, (Trait trait)->( (lowerBoundary == null || lowerBoundary.compareTo(trait.getLevel().orElse(Integer.MIN_VALUE)) <= 0) 
					&& (upperBoundary == null || upperBoundary.compareTo(trait.getLevel().orElse(Integer.MAX_VALUE)) >= 0)));
			this.lowerBoundary_ = lowerBoundary;
			this.upperBoundary_ = upperBoundary;
			if (lowerBoundary != null && upperBoundary != null && lowerBoundary.compareTo(upperBoundary) > 0) {
				throw new IllegalArgumentException("Lower boundary greater than upper boundary");
			}
		}
		
		@Override
		public String toString() {
			if (upperBoundary_ != null && lowerBoundary_ != null) {
				// Both are bounded.
				return super.toString() + " " + lowerBoundary_ + "-" + upperBoundary_;
			} else if (upperBoundary_ != null) {
				// Upper boundary exist.
				return super.toString() + " " + upperBoundary_ + "+";
			} else if (lowerBoundary_ != null) {
				// Lower boundary exists.
				return super.toString() + " " + lowerBoundary_ + "-";
			} else {
				// No boundaries defaults to existence of the basic requiremetn.
				return super.toString();
			}
		}
	}
	
	public static class CharacterRequirement extends Requirement<DuneCharacter> {

		
		public CharacterRequirement(Predicate<? super DuneCharacter> predicate) {
			super(null, predicate);
		}
		
		

	}
	
	/**
	 * TalentRequirement requires certain talent.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class TalentRequirement extends Requirement<Talent> {
		
		/**
		 * Testing a talent to be at least the given benchmark.
		 * 
		 * @param benchmark The benchmark the talent is tested with. If undefined, 
		 *  any defined talent beats it.
		 * @param tested The tested talent.
		 * @return
		 */
		public static boolean atLeast(Talent benchmark, Talent tested) {
			if (benchmark == null) {
				return tested != null; 
			} else if (tested == null || benchmark.getName() != tested.getName()) {
				return false; 
			} 
				
			// The names are same. Testing drive name.
			if (benchmark.getDriveName().isPresent()) {
				if (benchmark.getDriveName().get() != tested.getDriveName().orElse(null)) return false;
			} 
			
			
			// The drive names are same. Testing drive skill name.
			if (benchmark.getSkillName().isPresent()) {
				if (benchmark.getSkillName().get() != tested.getSkillName().orElse(null)) return false;
			}
			
			// The test passed.
			return true; 
		}
		
		public TalentRequirement(String talentName) {
			super(talentName, null);
		}
	} // class TalentRequirement
	
	/**
	 * The name of the talent.
	 */
	private String myName_;
	
	/**
	 * The drive of the talent.
	 */
	private Optional<String> myDrive_ = Optional.empty();
	
	/**
	 * The skill of the talent.
	 */
	private Optional<String> mySkill_ = Optional.empty();

	/**
	 * The requirements of the talent.
	 */
	private Set<Requirement<?>> myRequirements_ = new java.util.TreeSet<>();
	
	/**
	 * Create a new talent without requisites.
	 * 
	 * @param name The name of the created talent. 
	 * @throws IllegalArgumentException The given name was invalid.
	 */
	public Talent(String name) 
	throws IllegalArgumentException {
		if (validName(name)) {
			myName_ = name;
		} else {
			throw new IllegalArgumentException("Invalid talent name");
		}
	}
	
	/**
	 * Create new talent with given requisites.
	 * 
	 * @param name The name of the talent.
	 * @param requisites The requisites of the created talent.
	 */
	public Talent(String name, Set<? extends Requirement<?>> requisites) {
		this(name);
		this.myRequirements_.addAll(requisites);
	}
	
	/**
	 * Check validity of a name.
	 * 
	 * @param name The tested name.
	 * @return True, if and only if the given name is valid.
	 */
	public boolean validName(String name) {
		return name != null && name.equals(name.trim()) && !name.isEmpty();
	}
	
	/**
	 * The drive name attached to the talent.
	 * 
	 * @return The drive name of the talent.
	 */
	public Optional<String> getDriveName() {
		return this.myDrive_;
	}
	
	/**
	 * The skill name attached to the talent.
	 * 
	 * @return The skill name of the talent.
	 */
	public Optional<String> getSkillName() {
		return this.mySkill_;
	}
	
	
	/**
	 * Get the requirements of the talent.
	 * 
	 * @return The set of requirements.
	 */
	public Set<Requirement<?>> getRequirements() {
		return Collections.unmodifiableSet(this.myRequirements_);
	}
	
	/**
	 * Get the name of the talent.
	 * 
	 * @return The name of the talent.
	 */
	public String getName() {
		return myName_; 
	}
	
	@Override
	public String toString() {
		Optional<String> drive = getDriveName();
		Optional<String> skill = getSkillName(); 
		Set<String> requirements = getRequirements().stream().map((Requirement<?> req)->(req.toString())).collect(Collectors.toSet()); 
		return getName() + (drive.isPresent()?" " + drive.get():"") + (skill.isPresent()?" " + skill.get():"") + 
				(requirements.isEmpty()?"":"(" + String.join(", ", requirements)+ ")");
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Talent)) return false; 
		else if (other == this) return true;
		return compareTo((Talent)other) == 0; 
	}
	
	@Override
	public int compareTo(Talent other) {
		return toString().compareTo(other.toString());
	}
	
	
	/**
	 * Does the given character fulfill the requisites.
	 * 
	 * @param character The character.
	 * @return True, if and only if the character fulfills the requisites.
	 */
	public boolean testRequisites(DuneCharacter character) {
		for (Requirement<?> requirement: this.getRequirements()) {
			if (requirement instanceof TalentRequirement) {
				TalentRequirement talentRequirement = (TalentRequirement)requirement;
				if (!character.getTalents().stream().anyMatch((Talent talent)->(talentRequirement.test(talent)))) {
					return false; 
				}
			} else if (requirement instanceof TraitRequirement) {
				TraitRequirement traitRequirement = (TraitRequirement)requirement;
				if (!character.getTraits().stream().anyMatch((Trait trait)->(traitRequirement.test(trait)))) {
					return false; 
				}				
			} else if (requirement instanceof CharacterRequirement) {
				// The last chance is the character requirement.
				CharacterRequirement characterRequirement = (CharacterRequirement)requirement; 
				if (!characterRequirement.test(character)) {
					return false;
				}
			}
		}
		return true; 
	}
	
}
