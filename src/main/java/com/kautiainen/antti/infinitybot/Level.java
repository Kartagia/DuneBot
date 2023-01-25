package com.kautiainen.antti.infinitybot;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * The logging level.
 * 
 * @author Antti Kautiainen
 *
 */
public class Level implements LoggingLevel {

	/**
	 * Get the default debugging level from system properties. 
	 * @return The default debugging level. 
	 */
	public static Level getDefaultDebuggingLevel() {
		String loggingLevel =  System.getProperty("logging.level");
		if (loggingLevel != null && knownLoggingLevels != null) {
			if (Pattern.matches("^\\d+$", loggingLevel)) {
				// we have proper value. 
				int level = Integer.parseInt(loggingLevel);
				return knownLoggingLevels.stream().filter(
						(Level result)->(result != null && result.getLevel() == level)).findFirst().orElse(all);
			}
		}
		return all; 
	}
	
	/**
	 * The next level available.
	 */
	private static int nextLoggingLevel = 0;

	/**
	 * Get the next logging level available.
	 * 
	 * @return The next available logging level.
	 */
	protected static synchronized int getNextLoggingLevel() {
		return nextLoggingLevel++;
	}
	
	/**
	 * The known logging levels. 
	 */
	private static SortedSet<Level> knownLoggingLevels; 


	/**
	 * The message level of all messages.
	 */
	public static final Level all;
	/**
	 * The message level of debugging messages.
	 */
	public static final Level debug;
	/**
	 * The message level of warning messages.
	 */
	public static final Level warning;
	/**
	 * The message level of error messages.
	 */
	public static final Level error;
	/**
	 * The message level of information messages.
	 */
	public static final Level info;

	/**
	 * The message level of disabled messaging.
	 */
	public static final Level disabled;

	static {
		all = new Level(getNextLoggingLevel());
		debug = new Level();
		warning = new Level();
		error = new Level();
		info = new Level();
		disabled = new Level(Integer.MAX_VALUE);
		knownLoggingLevels = new TreeSet<Level>(LoggingLevel.LEVEL_COMPARATOR);
		knownLoggingLevels.addAll(Arrays.asList(all, debug, warning, error, info, disabled)); 
	}

	/**
	 * The level of the current instance.
	 */
	final int level;

	/**
	 * Create a new logging level using next level.
	 */
	public Level() {
		this(null);
	}

	/**
	 * Create a new logging level with given level.
	 * 
	 * @param level The value of level. If the value is undefined, the constructor
	 *              generates level with next higher priority than previous
	 *              automatically generated priorities.
	 */
	public Level(Integer level) {
		this.level = (level == null ? getNextLoggingLevel() : level);
	}


	@Override
	public int getLevel() {
		return this.level;
	}

	/**
	 * The hash code of the logging level.
	 * 
	 * @return The hash code of the logging level.
	 */
	public int hashCode() {
		int level = getLevel();
		if (level == Integer.MAX_VALUE) {
			return 0;
		} else {
			return level;
		}
	}

}