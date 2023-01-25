package com.kautiainen.antti.infinitybot.dune;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kautiainen.antti.infinitybot.model.Special;
import com.kautiainen.antti.infinitybot.model.Trait;

/**
 * Asset is a special trait with Quality instead of level.
 * 
 * @author Antti Kautainen
 *
 */
public interface Asset extends Trait {
	
	/**
	 * The invalid asset quality message.
	 */
	static final String INVALID_QUALITY_MESSAGE = Trait.getInvalidPropertyMessage("asset", "quality");
	
	/**
	 * The invalid asset level message.
	 */
	static final String INVALID_VALUE_MESSAGE = Trait.getInvalidPropertyMessage("asset", "level");
	
	/**
	 * The invalid asset name message.
	 */
	static final String INVALID_NAME_MESSAGE = Trait.getInvalidPropertyMessage("asset", "name"); 
	
	/**
	 * The name of the pattern group capturing the quality.
	 */
	static final String QUALITY_GROUP_NAME = "quality";
	
	/**
	 * The pattern matching to the asset value. The value of the asset is stored into the named group the {@link #QUALITY_GROUP_NAME}. 
	 */
	static final Pattern QUALITY_PARSE_PATTERN = Pattern.compile("\\(Q(?<" + QUALITY_GROUP_NAME + ">\\d)\\)");
	
	/** 
	 * The pattern matching an asset name. The name
	 *  of the asset is stored into the group {@link Special#NAME_GROUP_NAME} and 
	 *  the value is stored into group {@link Special#VALUE_GROUP_NAME}.
	 */
	static final Pattern ASSET_PARSE_PATTERN = Pattern.compile(Trait.NAME_PARSE_PATTERN.toString() + 
	QUALITY_PARSE_PATTERN + "(?:" + Trait.LEVEL_PARSE_PATTERN + ")?" + "(?:" + Trait.DESCRIPTION_PARSE_PATTERN + ")?");

	/**
	 * The default quality of an asset.
	 */
	static final Integer DEFAULT_QUALITY = 0;
	
	/**
	 * The default minimum quality.
	 */
	static final Integer DEFAULT_MINIMUM_QUALITY = 0;
	
	/**
	 * The default maximum quality.
	 */
	static final Integer DEFAULT_MAXIMUM_QUALITY = 4;
	
	/**
	 * Create a new asset from string representation.
	 * 
	 * @param stringRep The string representation.
	 * @return The created asset.
	 * @throws IllegalArgumentException The string representation was invalid.
	 */
	static Asset of(String stringRep) 
			throws IllegalArgumentException {
		Matcher matcher; 
		if (stringRep != null && (matcher = ASSET_PARSE_PATTERN.matcher(stringRep)).matches()) {
			String valueString = matcher.group(LEVEL_GROUP_NAME);
			Integer value = valueString == null ? null : Integer.parseInt(valueString);
			valueString = matcher.group(QUALITY_GROUP_NAME);
			int quality = valueString == null ? null : Integer.parseInt(valueString);
			return new SimpleAsset(matcher.group(NAME_GROUP_NAME), value, quality, matcher.group(DESCRIPTION_GROUP_NAME));
		} else {
			throw new IllegalArgumentException("Invalid string representation"); 
		}
	}

	/**
	 * The pattern parsing quality string representation.
	 * 
	 * @return The pattern parsing quality of an assent. The value of the quality
	 *  is stored into group {@link #QUALITY_GROUP_NAME}
	 */
	default Pattern getQualityParsePattern() {
		return Pattern.compile("(?:" + QUALITY_PARSE_PATTERN + ")" + (hasMandatoryQuality() ? "" : "?")); 
	}
	
	@Override
	default Pattern getParsePattern() {
		return Pattern.compile("" + getNameParsePattern() + 
				getQualityParsePattern() + 
				getLevelParsePattern() + 
				getDescriptionParsePattern()); 
	}
	
	@Override
	default boolean hasMandatoryLevel() {
		return false; 
	}
	
	@Override
	default Optional<Integer> getMinimumLevel() {
		return Optional.of(1);
	}
	
	
	/**
	 * Get the default quality.
	 * 
	 * @return The default quality of the asset.
	 */
	default Optional<Integer> getDefaultQuality() {
		return Optional.ofNullable(DEFAULT_QUALITY);
	}
	
	/**
	 * Get the smallest accepted quality.
	 * 
	 * @return The smallest accepted quality.
	 */
	default Optional<Integer> getMinimumQuality() {
		return Optional.ofNullable(DEFAULT_MINIMUM_QUALITY);
	}
	
	/**
	 * Get the largest accepted quality
	 * 
	 * @return The largest accepted quality.
	 */
	default Optional<Integer> getMaximumQuality() {
		return Optional.ofNullable(DEFAULT_MAXIMUM_QUALITY);
	}

	
	/**
	 * Does the asset have mandatory quality.
	 * 
	 * @return True, if and only if the quality is mandatory for asset.
	 */
	default boolean hasMandatoryQuality() {
		return true;
	}
	
	/**
	 * Test validity of the quality.
	 * 
	 * @param quality The tested quality.
	 * @return True, if and only if the given quality is valid quality.
	 */
	default boolean validQuality(Integer quality) {
		if (quality == null) quality = getDefaultQuality().orElse(null);
		return quality != null && quality >= getMinimumQuality().orElse(Integer.MIN_VALUE) && 
				quality <= getMaximumQuality().orElse(Integer.MAX_VALUE);
	}
	
	/**
	 * Get the quality of the asset.
	 * 
	 * @return The quality of the asset is one less than its value.
	 */
	default Optional<Integer> getQuality() {
		return getDefaultQuality();
	}

	
	@Override
	default Asset getStacked(int value) {
		return getStacked(value, 0);
	}


	/**
	 * Create stacked asset with given value and quality modifier.
	 * @param valueModifier The modifier of the quality value.
	 * @param qualityModifier The modifier to the quality.
	 * @return The asset created by adding given value modifier to the value and quality modifier to the quality, 
	 *  but keeping both values within valid bounds.
	 */
	default Asset getStacked(int valueModifier, int qualityModifier) {
		int newValue = Integer.max(getMinimumLevel().orElse(Integer.MAX_VALUE), 
				Integer.min(getMaximumLevel().orElse(Integer.MIN_VALUE), getLevel().orElse(null) + valueModifier));
		int newQuality = Integer.max(
				getMinimumQuality().orElse(Integer.MAX_VALUE), 
				Integer.min(getMaximumQuality().orElse(Integer.MIN_VALUE), 
						Optional.ofNullable(getQuality().orElse(0)).orElse(getDefaultQuality().orElse(0)) + qualityModifier));
			
		// The area is totally fine.
		return new SimpleAsset(getName(), newValue, newQuality, getDescription().orElse(null));			
	}

	
	@Override
	default Asset createAnotherFromString(String stringRep) throws IllegalArgumentException {
		if (stringRep==null) return null; 
		Matcher matcher = getParsePattern().matcher(stringRep);
		
		if (matcher.matches()) {
			// Getting the values.
			String name = matcher.group(NAME_GROUP_NAME); 
			String valueRep = matcher.group(LEVEL_GROUP_NAME);
			Integer value = (valueRep==null?null:Integer.parseInt(valueRep));
			valueRep = matcher.group(QUALITY_GROUP_NAME);
			Integer quality = (valueRep==null?null:Integer.parseInt(valueRep));
			String desc = matcher.group(DESCRIPTION_GROUP_NAME);
			return new SimpleAsset(name, value, quality, desc);
		} else {
			throw new IllegalArgumentException("Invalid asset"); 
		}
	}

	
	/**
	 * Get the effectiveness of an asset.
	 * 
	 * @return The effectiveness of an asset for a player character.
	 */
	default int getEffectiveness() {
		Optional<Integer> quality = getQuality(); 
		return quality.isPresent()?quality.get() +2:0;
	}
	
	
	/**
	 * Get the string representation of an asset.
	 * 
	 * @param asset The converted asset.
	 * @return A defined string representation, if the asset is defined. An undefined value, if the asset is undefined.
	 */
	static String toString(Asset asset) {
		if (asset == null) return null;
		Optional<Integer> level = asset.getLevel();
		Optional<Integer> quality = asset.getQuality();
		Optional<String> description = asset.getDescription();
		return asset.getName() +
				(quality.isPresent() ? "(Q" + quality.get() + ")":"") + 
				(level.isPresent() ? "(" + level.get() + ")" : "") + 
				(description.isPresent()?": " + description.get() : ""); 
	}
	
}
