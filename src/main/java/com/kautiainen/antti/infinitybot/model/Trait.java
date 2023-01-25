package com.kautiainen.antti.infinitybot.model;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Trait is a descriptive element.
 * 
 * @author Antti Kautiainen
 *
 */
public interface Trait {
	
	/**
	 * The default level of a trait.
	 */
	public static final Optional<Integer> DEFAULT_LEVEL = Optional.of(1);

	/**
	 * The description capture group name.
	 */
	public static final String DESCRIPTION_GROUP_NAME = "desc";
	
	/**
	 * The level capture group name.
	 */
	public static final String LEVEL_GROUP_NAME = "level";

	/**
	 * The name capture group name.
	 */
	public static final String NAME_GROUP_NAME = Special.NAME_GROUP_NAME;

	/**
	 * The message of an invalid asset name.
	 */
	public static final String INVALID_NAME_MESSAGE = getInvalidPropertyMessage("talent", "name");

	/**
	 * The message of an invalid level message.
	 */
	public static final String INVALID_LEVEL_MESSAGE = getInvalidPropertyMessage("talent", "level");
	
	/**
	 * Get the invalid level message for a class.
	 * @param className 
	 * @return
	 */
	static String getInvalidLevelMessage(String className) {
		return String.format("Invalid %s level"); 
	}

	/**
	 * Generate invalid property value message.
	 * 
	 * @param className The class name.
	 * @param propertyName The property name.
	 * @return The message indicating invalid property of given class name.
	 */
	static String getInvalidPropertyMessage(String className, String propertyName) {
		return String.format("Invalid %s%s%%", 
				((className == null || className.trim().isEmpty()) ? "" : className) , 
				((className == null || className.trim().isEmpty() || propertyName == null || propertyName.trim().isEmpty() ) ? "" : " "),
				(propertyName == null ? "" : propertyName));
	}
	
	
	/**
	 * The pattern matching to the asset value. The value of the asset is stored into the named group the {@link #LEVEL_GROUP_NAME}. 
	 */
	static final Pattern LEVEL_PARSE_PATTERN = Pattern.compile("\\((?<" + LEVEL_GROUP_NAME + ">\\d)\\)");

	Pattern NAME_WORD_PATTERN = Special.WORD_PATTERN;
	// Pattern.compile("\\p{Lu}(?:\\.|[\\p{Ll}]+)?", Pattern.UNICODE_CHARACTER_CLASS);
	
	/**
	 * The pattern matching to the name pattern. The name
	 *  of the asset is stored into the group {@link #NAME_GROUP_NAME}.
	 */
	Pattern NAME_PARSE_PATTERN = Pattern.compile("(?<"+ NAME_GROUP_NAME + ">"+ NAME_WORD_PATTERN +
			"(?:[-\\s]" + NAME_WORD_PATTERN + ")*" + ")", Pattern.UNICODE_CHARACTER_CLASS);
	/**
	 * The pattern matching to the description pattern. 
	 * The description is stored into the group {@link #DESCRIPTION_GROUP_NAME}. 
	 */
	Pattern DESCRIPTION_PARSE_PATTERN = Pattern.compile(":\\s*(?<" + DESCRIPTION_GROUP_NAME + ">" + "[^\\n]" + ")\\s*(?:$|\\n)");
	
	/**
	 * the pattern matching to the trait.
	 * THe name of the trait is stored into the group {@link #NAME_GROUP_NAME}, the value is stored into
	 * group {@link #LEVEL_GROUP_NAME}, and the possible description is stored into the group 
	 * {@link #DESCRIPTION_GROUP_NAME}. 
	 */
	Pattern TRAIT_PARSE_PATTERN = Pattern.compile(NAME_PARSE_PATTERN + "(?:" + LEVEL_PARSE_PATTERN + ")?" +
	"(?:" + DESCRIPTION_PARSE_PATTERN + ")?");
	
	
	/**
	 * Get the current level of the trait.
	 * 
	 * @return The level of the trait, if the trait has level.
	 */
	default Optional<Integer> getLevel() {
		return getDefaultLevel();
	}
	
	/**
	 * Get name of the trait.
	 * 
	 * @return The name of the trait. This is always defined value.
	 */
	public String getName(); 
	
	/**
	 * Get the description of the trait.
	 * 
	 * @return The description of the trait, if any exists-
	 */
	default Optional<String> getDescription() {
		return Optional.empty(); 
	}
	
	/**
	 * The pattern matching to the trait name.
	 * 
	 * @return the pattern matching to the name of the trait. 
	 * The pattern captures name into the group {@link #NAME_GROUP_NAME}
	 */
	default Pattern getNameParsePattern() {
		return Trait.NAME_PARSE_PATTERN;
	}
	
	/**
	 * The pattern matching to the trait level. 
	 * 
	 * @return The pattern matching to the trait level. THe
	 * pattern captures the trait level into
	 *  the group {@link #LEVEL_GROUP_NAME}
	 */
	default Pattern getLevelParsePattern() {
		return Pattern.compile("(?:" + Trait.LEVEL_PARSE_PATTERN + ")" + (hasMandatoryLevel() ? "" : "?"));  
	}
	
	/**
	 * Does the trait have mandatory value.
	 * 
	 * @return True, if and only if the value is mandatory.
	 */
	default boolean hasMandatoryLevel() {
		return false;
	}
	
	/**
	 * The pattern matching to the description of the trait.
	 * 
	 * @return The pattern matching to the description of the pattern.
	 *  The pattern captures the description is stored into the group
	 *  {@link #DESCRIPTION_GROUP_NAME}.
	 */
	default Pattern getDescriptionParsePattern() {
		return Pattern.compile("(?:" + Trait.DESCRIPTION_PARSE_PATTERN + ")" + (hasMandatoryDescription() ? "" : "?"));  
	}
	
	/**
	 * Does the trait have mandatory description.
	 * 
	 * @return True, if and only if the description is mandatory.
	 */
	default boolean hasMandatoryDescription() {
		return false; 
	}
		
	/**
	 * Test validity of a name.
	 * 
	 * @param name The tested name.
	 * @return True, if and only if the name is valid name.
	 */
	default boolean validName(String name) {
		return name != null && getNameParsePattern().matcher(name).matches(); 
	}

	
	/**
	 * The parse pattern matching to the trait string representation.
	 * 
	 * @return The pattern matching to the string representation, and capturing
	 *  the trait name into the group {@link Special#NAME_GROUP_NAME}, value into
	 *  the group {@link Special#VALUE_GROUP_NAME}, and description into the group
	 *  {@link #DESCRIPTION_GROUP_NAME}.
	 */
	default Pattern getParsePattern() {
		return Pattern.compile("" + 
				getNameParsePattern() + 
				getLevelParsePattern() +  
				getDescriptionParsePattern()); 
	}

	/**
	 * Test validity of a level.
	 * 
	 * @param level The tested level. Defaults to the default value.
	 * @return True, if and only if the given level is valid.
	 */
	default boolean validLevel(Integer level) {
		if (level == null) {
			level = getDefaultLevel().orElse(null);
		}
		
		if (level == null) return false;
		Optional<Integer> boundary = getMinimumLevel();
		if (boundary.isPresent() && boundary.get() > level) {
			return false;
		}
		boundary = getMaximumLevel();
		if (boundary.isPresent() && boundary.get() < level) {
			return false;
		}
		return true;
	}
	
	/**
	 * Get the default level.
	 * 
	 * @return The default level of the trait.
	 */
	default Optional<Integer> getDefaultLevel() {
		return DEFAULT_LEVEL;
	}
	
	/**
	 * Get the smallest allowed level.
	 * @return The smallest allowed level. An empty value, if no such value exists.
	 */
	default Optional<Integer> getMinimumLevel() {
		return Optional.of(1);
	}
	
	/**
	 * Get the largest allowed level.
	 * @return The largest allowed level. An empty value, if no such value exists.
	 */
	default Optional<Integer> getMaximumLevel() {
		return Optional.empty();
	}
	
	/**
	 * Get stacked trait gained by adding or removing the traits.
	 * 
	 * @param levelModifier The number of levels the level is modified. If the modifier is positive, 
	 *  the level is added. If it is negative, the level is reduced. 
	 * @return An undefined value, if trait is negated by the stacking. Otherwise the trait result of the
	 *  stacking. The resulting defined trait cannot have invalid level.
	 */
	default Trait getStacked(int levelModifier) {
		Optional<Integer> level = getLevel();
		int newLevel = level.isPresent()?level.get()+levelModifier:levelModifier;
		if (validLevel(newLevel)) {
			return new SimpleTrait(getName(), newLevel, getDescription().orElse(null));
		} else if (newLevel > getMaximumLevel().orElse(Integer.MAX_VALUE)) {
			// Getting the maximum trait.
			return new SimpleTrait(getName(), getMaximumLevel().get(), getDescription().orElse(null));
		} else {
			// The trait is voided.
			return null;
		}
	}

	/** Create another instance of the current object type.
	 * 
	 * @param stringRep The string representation.
	 * @return The instance of the given string representation.
	 * @throws IllegalArgumentException The string representation was invalid.
	 */
	default Trait createAnotherFromString(String stringRep) throws IllegalArgumentException {
		if (stringRep==null) return null; 
		Matcher matcher = getParsePattern().matcher(stringRep);
		
		if (matcher.matches()) {
			// Getting the values.
			String name = matcher.group(NAME_GROUP_NAME); 
			String valueRep = matcher.group(LEVEL_GROUP_NAME);
			Integer level = (valueRep==null?null:Integer.parseInt(valueRep));
			String desc = matcher.group(DESCRIPTION_GROUP_NAME);
			return new SimpleTrait(name, level, desc);
		} else {
			throw new IllegalArgumentException("Invalid asset"); 
		}
	}
}
