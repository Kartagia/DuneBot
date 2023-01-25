package com.kautiainen.antti.infinitybot.dune;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.kautiainen.antti.infinitybot.model.RPGCharacter;
import com.kautiainen.antti.infinitybot.model.StringTools;
import com.kautiainen.antti.infinitybot.model.Talent;
import com.kautiainen.antti.infinitybot.model.Term;
import com.kautiainen.antti.infinitybot.model.TermValueMap;
import com.kautiainen.antti.infinitybot.model.Trait;


/**
 * Player character stores information about player. 
 * 
 * @author Antti Kautaiinen
 *
 */
public class DuneCharacter extends RPGCharacter {
	
	/**
	 * The list of skill term names.
	 */
	public static final java.util.Set<String> SKILL_TERM_NAMES = 
			Collections.unmodifiableSet(new TreeSet<>(java.util.Arrays.asList("battle", "communicate", "discipline", "move", "understand"))); 

	/**
	 * The list of drive term names.
	 */
	public static final java.util.Set<String> ATTRIBUTE_TERM_NAMES = 
			Collections.unmodifiableSet(new TreeSet<>(java.util.Arrays.asList("duty", "faith", "justice", "power", "truth")));

	/**
	 * The skill values of the character. Values are never null.
	 */
	private java.util.TreeMap<String, Integer> mySkills_ = new ConstrainedTreeMap<String, Integer>(
			java.util.Comparator.naturalOrder(),
			(Predicate<String>)(String skillName) -> (DuneCharacter.this.validSkillName(skillName)), 
			(Predicate<Integer>)(Integer skillValue) -> (skillValue != null && DuneCharacter.this.validSkillValue(skillValue)), 
			(BiPredicate<String, Integer>)(String skillName, Integer skillValue) -> (skillValue != null &&
			DuneCharacter.this.validSkillValue(skillName, skillValue.intValue()))
			);
	
	/**
	 *  The drive values of the character. Values are never null.
	 */
	private java.util.TreeMap<String, Integer> myDrives_ = new ConstrainedTreeMap<String, Integer>(
			java.util.Comparator.naturalOrder(),
			(Predicate<String>)(String name) -> (DuneCharacter.this.validAttributeName(name)), 
			(Predicate<Integer>)(Integer value) -> (value != null && DuneCharacter.this.validAttributeValue(value)), 
			(BiPredicate<String, Integer>)(String name, Integer value) -> (value != null &&
			DuneCharacter.this.validAttributeValue(name, value.intValue()))
			);
	
	/**
	 * The drive statements of the character. Values are never null.
	 */
	private java.util.TreeMap<String, String> myDriveStatements_ = new ConstrainedTreeMap<String, String>(
			java.util.Comparator.naturalOrder(),
			(Predicate<String>)(String name) -> (DuneCharacter.this.validAttributeName(name)), 
			(Predicate<String>)(String value) -> (value != null && DuneCharacter.this.validDriveStatement(value)), 
			(BiPredicate<String, String>)(String name, String value) -> (value != null &&
			DuneCharacter.this.validDriveStatement(name, value))
			);
	
	public boolean validDriveStatement(String statement) {
		return StringTools.validStatement(statement);
	}
	
	public boolean validDriveStatement(String attributeName, String statement) {
		return validAttributeName(attributeName) && validDriveStatement(statement);
	}
	
	/**
	 * The talents of the character.
	 */
	private java.util.TreeSet<Talent> myTalents_ = new java.util.TreeSet<>();
	
	/**
	 * The traits of the character.
	 */
	private java.util.TreeSet<Trait> myTraits_ = new java.util.TreeSet<>();
	
	/**
	 * The assets of the character.
	 */
	private java.util.TreeSet<Asset> myAssets_ = new java.util.TreeSet<>();

	/**
	 * Get term names.
	 * 
	 * @param name The name of the term.
	 * @return An undefined value, if the given set of names is not restricted. Otherwise
	 *  the list of accepted set names.
	 */
	public static Set<String> getTermNames(String name) {
		if (name == null) return null;
		switch (name)  {
		case SKILLS_TERM_NAME: 
			return DuneCharacter.SKILL_TERM_NAMES;
		case ATTRIBUTES_TERM_NAME:
			return DuneCharacter.ATTRIBUTE_TERM_NAMES;
			default:
				return null; 
		}
	}

	/**
	 * Create a new player character with a name, guild identifier, and owner identifier.
	 * 
	 * @param name The name of the character.
	 * @param guildId The guild of the character. If none, the character is a template not bound to a guild.
	 * @param ownerId THe owner identifier. The owner has more information on character. If the owner is undefined,
	 *  no player has ownership of the character.
	 */
	public DuneCharacter(String name, Long guildId, Long ownerId) 
	throws IllegalArgumentException{
		super();
		if (!validName(name)) {
			throw new IllegalArgumentException("Invalid name"); 
		}
		if (validIdentifier(guildId)) {
			throw new IllegalArgumentException("Invalid guild identifier");
		}
		if (!validIdentifier(ownerId)) {
			throw new IllegalArgumentException("Invalid user identifier");			
		}
		this.name_ = name;
		this.guildId_ = guildId;
		this.ownerId_ = ownerId;
	}
	
	/**
	 * Create a new character with given name, guild, owner, skill definition, and
	 * attribute definition.
	 * @param name The name of the character.
	 * @param guildId The guild of the character.
	 * @param ownerId The owner of the character.
	 * @param skillDefinition The skill definitions of the character.
	 * @param attributeDefinition The attribute definition of the character.
	 * @throws IllegalArgumentException Any value was invalid.
	 */
	public DuneCharacter(String name, Long guildId, Long ownerId, 
			Term<Integer> skillDefinition, 
			Term<Integer> attributeDefinition) throws IllegalArgumentException {
		this(name, guildId, ownerId);
		this.initAttributes(attributeDefinition);
		this.initSkills(skillDefinition);
	}
	
	/**
	 * Create a new character with given name, guild, owner, skill definition, and
	 * attribute definition.
	 * @param name The name of the character.
	 * @param guildId The guild of the character.
	 * @param ownerId The owner of the character.
	 * @param skillDefinition The skill definitions of the character.
	 * @param attributeDefinition The attribute definition of the character.
	 * @throws IllegalArgumentException Any value was invalid.
	 */
	public DuneCharacter(String name, Term<Integer> skillDefinition, Term<Integer> attributeDefinition) {
		this(name, null, null, skillDefinition, attributeDefinition);
	}
	
	protected void initAttributes(Term<Integer> attributeDefinition) throws IllegalArgumentException {
		if (attributeDefinition == null) {
			attributeDefinition = getDefaultAttributeDefinition();
		}
		TermValueMap<Integer> attributes = new TermValueMap<>(attributeDefinition, this.getAttributeNames());
		for (String name: this.getAttributeNames()) {
			attributes.set(name, attributeDefinition.getDefaultValue());
		}
		this.initTermValueMap(attributeDefinition, attributes);
	}
	
	/**
	 * Initializes skills. 
	 * 
	 * @param skillDefinition The skill definition.
	 * @throws IllegalArgumentException The 
	 */
	protected void initSkills(Term<Integer> skillDefinition) throws IllegalArgumentException {
		if (skillDefinition == null) {
			skillDefinition = getDefaultSkillDefinition();
		}
		TermValueMap<Integer> skills = new TermValueMap<>(skillDefinition, this.getSkillNames());
		for (String name: this.getAttributeNames()) {
			skills.set(name,  skillDefinition.getDefaultValue());
		}
		this.initTermValueMap(skillDefinition, skills);
	}
	
	
	/**
	 * Test validity of the term name.
	 * 
	 * @param termName The tested term name.
	 * @return True, if and only if the term name is valid term name.
	 */
	@Override
	public boolean validTermName(String termName) {
		return termName != null;
	}

	/**
	 * Test validity of the skill name.
	 * 
	 * @param skillName The tested skill name.
	 * @return True, if and only if the given skill name is valid.
	 */
	public boolean validSkillName(String skillName) {
		return getSkillNames().contains(skillName);
	}

	/**
	 * Test validity of a discord identifier.
	 * 
	 * @param id The tested identifier.
	 * @return True, if and only if the given identifier is valid. 
	 */
	public boolean validIdentifier(Long id) {
		return id == null || id >= 0L;
	}

	/**
	 * Get the skill value of the character.
	 * 
	 * @param skill The skill name.
	 * @return If the character has given skill, returns that skill value. Otherwise returns an empty value.
	 */
	public java.util.Optional<Integer> getSkillValue(String skill) {
		return Optional.ofNullable(this.mySkills_.get(skill));
	}
	
	/**
	 * Set skill value.
	 * 
	 * @param skill The set skill name.
	 * @param value The new value of the skill.
	 * @throws IllegalArgumentException Either the skill or value was invalid.
	 */
	public void setSkillValue(String skill, int value) throws IllegalArgumentException {
		if (validSkillValue(skill, value)) {
			this.mySkills_.put(skill,  value); 
		} else {
			throw new IllegalArgumentException("Invalid skill value"); 
		}
	}

	/**
	 * Get the list of valid skill names.
	 * 
	 * @return The list of valid skill names.
	 */
	public java.util.Set<String> getSkillNames() {
		return SKILL_TERM_NAMES; 
	}
	
	/**
	 * Get the maximum skill total.
	 * 
	 * @return Get the maximum skill total.
	 */
	public OptionalInt getSkillMaximumTotal() {
		return OptionalInt.of(28);
	}
	
	/**
	 * The default skill value.
	 * 
	 * @return The default skill value.
	 */
	public int getDefaultSkillValue() {
		return getSkillMinimumValue().orElse(0);
	}
	
	/**
	 * Get the smallest accepted skill value.
	 * 
	 * @return The smallest accepted skill value.
	 */
	public OptionalInt getSkillMinimumValue() {
		return OptionalInt.of(4);
	}
	
	
	/**
	 * Get the largest accepted skill value.
	 * 
	 * @return The largest accepted skill value.
	 */
	public OptionalInt getSkillMaximumValue() {
		return OptionalInt.of(8);
	}


	/**
	 * Test validity of a skill value.
	 * 
	 * @param value The tested value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validSkillValue(Integer value) {
		return value != null && value >= getSkillMinimumValue().orElse(Integer.MIN_VALUE) && 
				value <= getSkillMaximumValue().orElse(Integer.MAX_VALUE); 
	}
	
	/**
	 * Test validity of the given value of given skill.
	 * 
	 * @param skill The skill name.
	 * @param value The new skill value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validSkillValue(String skill, int value) {
		return validSkillName(skill) && 
				( getSkillMaximumTotal().orElse(Integer.MAX_VALUE) >= (getSkillTotal() + value - getSkillValue(skill).orElse(this.getDefaultSkillValue())) ); 
	}
	
	/**
	 * Get the total value of skills.
	 * 
	 * @return The total of all skills of the character.
	 */
	public int getSkillTotal() {
		return getSkillNames().stream().collect(Collectors.summingInt((String skillName) -> (getSkillValue(skillName).orElse(this.getDefaultSkillValue()))));
	}
	


	/**
	 * The default attribute value.
	 * 
	 * @return The default attribute value.
	 */
	public int getDefaultAttributeValue() {
		return getAttributeMinimumValue().orElse(0);
	}
	
	/**
	 * Get the smallest accepted attribute value.
	 * 
	 * @return The smallest accepted attribute value.
	 */
	public OptionalInt getAttributeMinimumValue() {
		return OptionalInt.of(4);
	}
	
	
	/**
	 * Get the largest accepted attribute value.
	 * 
	 * @return The largest accepted attribute value.
	 */
	public OptionalInt getAttributeMaximumValue() {
		return OptionalInt.of(8);
	}

	
	/**
	 * Set attribute value.
	 * 
	 * @param attribute The set attribute name.
	 * @param value The new value of the attribute.
	 * @throws IllegalArgumentException Either the attribute or value was invalid.
	 */
	public void setAttributeValue(String attribute, int value) throws IllegalArgumentException {
		if (validAttributeValue(attribute, value)) {
			this.myDrives_.put(attribute,  value); 
		} else {
			throw new IllegalArgumentException("Invalid drive value"); 
		}
	}

	/**
	 * Get the list of valid drive names.
	 * 
	 * @return The list of valid drive names.
	 */
	public java.util.Set<String> getAttributeNames() {
		return SKILL_TERM_NAMES; 
	}
	
	/**
	 * Test validity of an attribute name. 
	 * 
	 * @param attributeName Tested attribute name.
	 * @return True, if and only if the given name is a valid attribute name.
	 */
	public boolean validAttributeName(String attributeName) {
		return getAttributeNames().contains(attributeName);
	}
	
	/**
	 * Get the maximum attribute total, if any exists.
	 * 
	 * @return The maximum attribute total.
	 */
	public OptionalInt getAttributeMaximumTotal() {
		return OptionalInt.of(4+5+6+7+8);
	}
	
	/**
	 * Test validity of the given value of given drive.
	 * 
	 * @param drive The drive name.
	 * @param value The new drive value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validAttributeValue(int value) {
		return value >= getAttributeMinimumValue().orElse(Integer.MIN_VALUE) && 
				value <= getAttributeMaximumValue().orElse(Integer.MAX_VALUE);
	}

	/**
	 * Test validity of the given value of given drive.
	 * 
	 * @param attribute The attribute name.
	 * @param value The new attribute value.
	 * @return True, if and only if the given value is valid.
	 */
	public boolean validAttributeValue(String attribute, int value) {
		return validAttributeName(attribute) && validAttributeValue(value) && 
				(getAttributeMaximumTotal().orElse(Integer.MAX_VALUE) >= 
				(getAttributeTotal() - getAttributeValue(attribute).orElse(getDefaultAttributeValue())));
	}
	
	/**
	 * Get the attribute value total.
	 * 
	 * @return The current attribute value total.
	 */
	public int getAttributeTotal() {
		return getAttributeNames().stream().collect(
				Collectors.summingInt((String name) -> (
						getAttributeValue(name).orElse(getDefaultAttributeValue()))
						)
				);
	}
	

	/**
	 * Get the attribute value of the character.
	 * 
	 * @param attribute The attribute name.
	 * @return If the character has given attribute, returns that attribute value. Otherwise returns an empty value.
	 */
	public java.util.Optional<Integer> getAttributeValue(String attribute) {
		return Optional.ofNullable(this.myDrives_.get(attribute));
	}
	
	/**
	 * Set the drive statement.
	 * 
	 * @param drive The drive of the statement.
	 * @param driveStatement  The drive statement.
	 * @throws IllegalArgumentException Either the drive statement, or the drive value was invalid
	 *  for a drive statement.
	 */
	public void setDriveStatement(String drive, String driveStatement)
	throws IllegalArgumentException {
		java.util.Optional<Integer> value = getAttributeValue(drive);
		if (!value.isPresent()) {
			throw new IllegalArgumentException("The drive does not exist");
		} else if (value.get() < getDriveStatementDriveMinimum()) {
			throw new IllegalArgumentException("The drive too low for statmeent");
		}
		if (validName(driveStatement)) {
			this.myDriveStatements_.put(drive, driveStatement);
		} else {
			throw new IllegalArgumentException("Invalid drive statement"); 
		}
	}

	/**
	 * Get the minimum value of a drive for statement.
	 * 
	 * @return The minimal value of a drive value for a statement.
	 */
	public int getDriveStatementDriveMinimum() {
		return 6;
	}
	
	/**
	 * Get the drive statement of the character.
	 * 
	 * @param drive The drive name.
	 * @return If the character has given drive, returns that drive statement. Otherwise returns an empty value.
	 */
	public java.util.Optional<String> getDriveStatement(String drive) {
		return Optional.ofNullable(this.myDriveStatements_.get(drive)); 
	}
	
	
	/**
	 * Get the talents of the character.
	 * 
	 * @return The set of talents the character has.
	 */
	public java.util.Set<Talent> getTalents() {
		return myTalents_;
	}
	
	/**
	 * Get the traits of the character.
	 * 
	 * @return The list of traits of the character.
	 */
	public java.util.Set<Trait> getTraits() {
		return myTraits_;
	}
	
	/**
	 * Get the assets of the character.
	 * 
	 * @return The list of the character assets.
	 */
	public java.util.Set<Asset> getAssets() {
		return myAssets_;
	}
}
