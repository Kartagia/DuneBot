package com.kautiainen.antti.infinitybot.dune;

import java.util.Optional;

/**
 * Simple implementation of an asset.
 * 
 * @author Antti Kautianen
 *
 */
public class SimpleAsset 
implements Asset {

	/**
	 * The message of an invalid asset name.
	 */
	public static final String INVALID_NAME_MESSAGE = "Invalid asset name";

	/**
	 * The message of an invalid value message.
	 */
	public static final String INVALID_VALUE_MESSAGE = "Invalid asset value"; 

	/**
	 * The name of the asset. This value is always defined.
	 */
	private final String name_;
	
	/**
	 * The value of the value of asset. This is always at least 1.
	 */
	private final Optional<Integer> value_;
	
	/**
	 * The description of the asset.
	 */
	private final Optional<String> description_;

	/**
	 * The quality of the asset. This may change.
	 */
	private Optional<Integer> quality_;
	
	
	/**
	 * Create a new simple asset.
	 * 
	 * @param name The name of the created asset.
	 * @param quality The quality of the created asset. Defaults to 0.
	 * @param description The description of the created asset.
	 * @throws IllegalArgumentException
	 */
	public SimpleAsset(String name, Integer quality, String description) 
	throws IllegalArgumentException {
		this(name, 1, quality, description);
	}
	
	/**
	 * Create a new simple asset with default quality and value. The created asset
	 * will not have description.
	 * 
	 * @param name The name of the quality.
	 * @throws IllegalArgumentException The name was invalid.
	 */
	public SimpleAsset(String name) 
	throws IllegalArgumentException {
		this(name, null, null); 
	}
	
	/**
	 * Create a new simple asset with a name, and quality. The created asset will
	 * have default value, and will not have description.
	 * 
	 * @param name The name of the created asset.
	 * @param quality The quality of the created asset.
	 * @throws IllegalArgumentException The name or quality was invalid.
	 */
	public SimpleAsset(String name, Integer quality) 
	throws IllegalArgumentException {
		this(name, quality, null);
	}
	
	/**
	 * Create a new simple asset with name and description. The created asset will
	 * have default value and quality.
	 * 
	 * @param name The name of the created asset.
	 * @param description The description of the asset.
	 * @throws IllegalArgumentException The name was invalid.
	 */
	public SimpleAsset(String name, String description) 
	throws IllegalArgumentException {
		this(name, null, description); 
	}
	
	
	/**
	 * Create a new simple asset.
	 * 
	 * @param name The name of the created asset.
	 * @param level The value of the quality. Defaults to the {@link #getDefaultQuality()}.
	 * @param quality The quality of the created asset. Defaults to {@link #getDefaultValue()}.
	 * @param description The description of the created asset. Defaults to no description.
	 * @throws IllegalArgumentException Any argument was invalid.
	 */
	public SimpleAsset(String name, Integer level, Integer quality, String description) {
		if (validName(name)) {
			name_ = name; 
		} else {
			throw new IllegalArgumentException(INVALID_NAME_MESSAGE);
		}
		if (level == null) {
			value_ = getDefaultLevel(); 
		} else if (validLevel(level)) {
			value_ = Optional.ofNullable(level);
		} else {
			throw new IllegalArgumentException(INVALID_VALUE_MESSAGE);
		}
		if (quality == null) {
			quality_ = getDefaultQuality();
		} else if (validQuality(quality)) {
			quality_ = Optional.ofNullable(quality);
		} else {
			throw new IllegalArgumentException(INVALID_QUALITY_MESSAGE);
		}
		
		description_ = Optional.ofNullable(description);
	}

	@Override
	public String getName() {
		return name_;
	}
	
	@Override
	public Optional<Integer> getLevel() {
		return value_;
	}
	
	@Override
	public Optional<Integer> getQuality() {
		return quality_;
	}
	
	@Override
	public Optional<String> getDescription() {
		return description_;
	}
	
	

}
