package com.kautiainen.antti.infinitybot;

/**
 * The interface of logging levels. 
 * @author Antti Kautiainen
 *
 */
public interface LoggingLevel {
	
	/**
	 * The level of the logging level. 
	 * @return The logging level. 
	 */
	public int getLevel();
	
	/**
	 * Compare this with another level. 
	 * @param other The other. 
	 * @return <0, if this is less than other, 0, if they are equals, 
	 *  and >0, if this is greater than other. 
	 * @throws NullPointerException The given other is undefined. 
	 */
	default int compareTo(Level other) {
		return Integer.compare(this.getLevel(), other.getLevel());
	}
	
	/**
	 * The comparator for leveled objects. It is nulls-last-order making
	 * undefined instances highest. This null levels works just like largest
	 * level. 
	 */
	public static final java.util.Comparator<Level> LEVEL_COMPARATOR = 
			(Level a, Level b) -> ((a!=null&&b!=null)?a.compareTo(b):(a==null?1:-1)); 
}