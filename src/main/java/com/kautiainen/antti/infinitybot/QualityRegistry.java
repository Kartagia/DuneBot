package com.kautiainen.antti.infinitybot;

import java.util.Optional;
import java.util.function.Function;

import com.kautiainen.antti.infinitybot.model.FunctionalSpecial;
import com.kautiainen.antti.infinitybot.model.Quality;
import com.kautiainen.antti.infinitybot.model.QualitySpecial;

/**
 * Registry storing quality templates.
 * 
 * @author kautsu
 *
 */
public class QualityRegistry extends SpecialRegistry {

	/**
	 * Generated serial version of the quality registry.
	 */
	private static final long serialVersionUID = -8752461103535082525L;

	/**
	 * Create a new quality registry.
	 */
	public QualityRegistry() {
		super();
	}

	public void register(String groupName, QualitySpecial createQuality) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validValue(String key, com.kautiainen.antti.infinitybot.model.Special value) {
		// TODO Auto-generated method stub
		return super.validValue(key, value);
	}

	@Override
	public boolean unregister(String name) {
		// TODO Auto-generated method stub4
		return super.unregister(name);
	}

	@Override
	public boolean unregister(com.kautiainen.antti.infinitybot.model.Special special) {
		// TODO Auto-generated method stub
		return super.unregister(special);
	}

	/**
	 * Create a new quality for the current registry with given name, level, and
	 * flags.
	 * 
	 * @param name  The name of the create quality.
	 * @param level The level of the quality.
	 * @param flags The flags.
	 * @return
	 */
	public QualitySpecial createQuality(String name, String level, String flags) {
		Function<Integer, Optional<Integer>> valueFunction = FunctionalSpecial.EMPTY_VALUE_FUNC;
		Integer minLevel=0, maxLevel=null, initLevel=null;
		boolean stacks = false;

		if ("x".equalsIgnoreCase(level)) {
			// Generic level template.
			initLevel = null;
			stacks = true;
		} else {
			try {
				initLevel = Integer.parseInt(level);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid level");
			}
		}

		// Checking flags.
		if (flags != null && flags.length() > 0) {
			String key;
			if (flags.contains(key = " value=")) {
				// The value is given with fixed value.
				int index = flags.indexOf(key) + key.length();
				int end = flags.indexOf(" ", index);
				if (end < 0) {
					end = flags.length();
				}
				String value = flags.substring(index, end);
				try {
					if (value.endsWith("x") || value.endsWith("X")) {
						// We have multiplier.
						if (value.length() > 1) {
							valueFunction  = FunctionalSpecial.GetMultiplierValueFunction(
									Integer.parseInt(value.substring(0, value.length()-1)));
						} else {
							valueFunction = FunctionalSpecial.CURRENT_VALUE_FUNC;
						}
						stacks = true; 
					} else {
						// We have fixed value.
						initLevel = Integer.parseInt(value); 
					}
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException("Invalid flag value");
				}

			} else if (flags.contains(key = " value")) {
				// The value function existss.
				valueFunction = FunctionalSpecial.CURRENT_VALUE_FUNC;
			}
			if (flags.contains(" stacks")) {
				stacks = true; 
			}
		}

		return new Quality(name, initLevel, maxLevel, minLevel, valueFunction, stacks);
	}
}