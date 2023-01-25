package com.kautiainen.antti.infinitybot.model;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

/**
 * Infinity special result.
 * 
 * @author Antti Kautiainen
 *
 */
public class Quality extends QualitySpecial {

	/**
	 * The quality template of this quality. 
	 * If template is defined, the minimal and maximal level is determined 
	 * by the intersection of the template valid level range, and current
	 * object valid level range. 
	 */
	private QualityTemplate template = null; 
	
	/**
	 * Create a new quality with
	 * 
	 * @param name  The name of the quality.
	 * @param value The value of the quality.
	 */
	public Quality(String name, int value) {
		this(name, value, null, FunctionalSpecial.EMPTY_VALUE_FUNC, false);
	}

	public Quality(String name, Integer value, Function<Integer, Optional<Integer>> valueFunc) {
		this(name, value, null, valueFunc, value != null && value != 0);
	}

	public Quality(String name, Integer value, Integer maxLevel, Function<Integer, Optional<Integer>> valueFunc,
			boolean stacks) {
		this(name, value, maxLevel, 0, valueFunc, stacks);
	}
	
	/**
	 * Create a new quality from the given template. 
	 * @param template the quality template. 
	 * @param value
	 * @param template
	 * @throws NullPointerException The given template is undefined. 
	 */
	public Quality(QualityTemplate template, Integer value) {
		this(template.getName(), value, template.getMaximumLevel().orElse(null), 
				template.getMinimumLevel().orElse(null), 
				template.getValueFunction(), template.stacks()); 
		this.template = template; 
	}

	public Quality(String name, Integer value, Integer maxLevel, Integer minLevel,
			Function<Integer, Optional<Integer>> valueFunc, boolean stacks) {
		super(name, value, minLevel, maxLevel, valueFunc, stacks);
	}

	/**
	 * Create a new quality with
	 * 
	 * @param name   The name of the quality.
	 * @param value  The value of the quality.
	 * @param stacks Does the values stack.
	 */
	public Quality(String name, int value, boolean stacks) {
		this(name, value, null, FunctionalSpecial.EMPTY_VALUE_FUNC, stacks);
	}

	public Quality(String name, Integer value, Function<Integer, Optional<Integer>> valueFunc, boolean stacks) {
		this(name, value, null, valueFunc, stacks);
	}

	public Quality(String name, Integer value, Integer maxLevel, Function<Integer, Optional<Integer>> valueFunc) {
		this(name, value, maxLevel, 0, valueFunc);
	}

	public Quality(String name, Integer value, Integer maxLevel, Integer minLevel,
			Function<Integer, Optional<Integer>> valueFunc) {
		super(name, value, minLevel, maxLevel, valueFunc, value != null && value != 0);
	}

	/**
	 * Get the template of the quality. 
	 * @return The template of the quality, if any exists. 
	 */
	public Optional<QualityTemplate> getTemplate() {
		return Optional.ofNullable(this.template); 
	}
	
	/**
	 * The maximal value of the quality. 
	 * @return The maximal value of quality, if any exists. 
	 */
	@Override
	public Optional<Integer> getMaximumLevel() {
		Optional<QualityTemplate> template = getTemplate(); 
		if (template.isPresent()) {
			Optional<Integer> result = template.get().getMaximumLevel(), tmp;
			tmp = super.getMinimumLevel(); 
			if (tmp.isPresent()) {
				result =  
						Optional.of(result.isPresent()?Math.min(tmp.get(), result.get()):tmp.get());
			}
			return result; 
		} else {
			return super.getMaximumLevel();
		}
	}

	/**
	 * The minimal value of the quality. 
	 * @return The minimal value of quality, if any exists. 
	 */
	@Override
	public Optional<Integer> getMinimumLevel() {
		Optional<QualityTemplate> template = getTemplate(); 
		if (template.isPresent()) {
			Optional<Integer> result = template.get().getMinimumLevel(), tmp; 
			tmp = super.getMaximumLevel(); 
			if (tmp.isPresent()) {
				result =  
						Optional.of(result.isPresent()?Math.max(tmp.get(), result.get()):tmp.get());
			}
			return result; 
		} else {
			return super.getMinimumLevel();
		}
	}

}