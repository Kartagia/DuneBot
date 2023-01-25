package com.kautiainen.antti.infinitybot;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class for logging objects.
 * 
 * @author Antti Kautiainen
 *
 */
public class Logging {

	/**
	 * The current logging level.
	 */
	private Level currentLoggingLevel = new Level(0);

	/**
	 * Set the current logging level.
	 * 
	 * @param newLevel The new logging level.
	 */
	public synchronized void setLoggingLevel(Level newLevel) {
		this.currentLoggingLevel = newLevel;
	}

	/**
	 * Get the current logging level.
	 * 
	 * @return The current logging level.
	 */
	public synchronized Level getLoggingLevel() {
		return this.currentLoggingLevel;
	}

	/**
	 * The consumer outputting the time stamped debug message.
	 */
	private final java.util.function.Consumer<String> debug;

	/**
	 * The consumer outputting the time stamped error message.
	 */
	private final java.util.function.Consumer<String> error;

	/**
	 * The consumer outputting the time stamped warning message.
	 */
	private final java.util.function.Consumer<String> warning;

	/**
	 * The consumer outputting the time stamped info message.
	 */
	private final java.util.function.Consumer<String> info;

	/**
	 * The formatter format for date strings of the default format.
	 */
	protected static final String DateFormaterString = "[%1$tF][%1$tH:%1$tT]";

	/**
	 * Escapes the given string
	 * 
	 * @param source The source.
	 * @return The escaped sequence.
	 */
	public static String escape(CharSequence source) {
		if (source == null)
			return null;
		final Map<Integer, String> encodingMap = new TreeMap<>();
		encodingMap.put(Integer.valueOf('\n'), "\\n");
		encodingMap.put(Integer.valueOf('\t'), "\\t");
		encodingMap.put(Integer.valueOf('\f'), "\\f");
		encodingMap.put(Integer.valueOf('\r'), "\\r");
		encodingMap.put(Integer.valueOf('\''), "\\\'");
		encodingMap.put(Integer.valueOf('`'), "\\`");
		encodingMap.put(Integer.valueOf('´'), "\\´");
		encodingMap.put(Integer.valueOf('"'), "\\\"");
		encodingMap.put(Integer.valueOf('\\'), "\\\\");
		encodingMap.put(Integer.parseInt("5d", 16), "\\\u005d");
		encodingMap.put(Integer.parseInt("5b", 16), "\\\u005b");
		encodingMap.put(Integer.parseInt("2c", 16), "\\\u002c");
		
		final StringBuilder result = new StringBuilder();
		source.codePoints().forEachOrdered((int i) -> {
			if (Character.isUnicodeIdentifierPart(i) 
					// Punctuation characters not requiring escapes. 
					|| (i >= '\u0020' && i <= '\u002f') || (i >= '\u003a' && i <= '\u0040')
					|| (i == '\u005b' || i >= '\u005d' && i <= '\u0060') ||
					(i >= '\u007b' && i <= '\u007e') ) {
				// Printing the character.
				result.appendCodePoint(i);
			} else {
				if (encodingMap.containsKey(i)) {
					result.append(encodingMap.get(i)); 
				} else {
					// Escaping the character with unicode escape.
					StringBuilder escapeBuilder = new StringBuilder(); 
					escapeBuilder.appendCodePoint(i); 
					for (int index = escapeBuilder.length()-1; index >= 0; index--) { 
						char charValue = escapeBuilder.charAt(index); 
						escapeBuilder.replace(index, index+1,
							String.format("%04x", (int)charValue)); 
					}
					escapeBuilder.insert(0, "\\x"); 
					result.append(escapeBuilder.toString());
				}
			}
		});
		return result.toString();
	}

	/**
	 * Create new default output consumer.
	 * 
	 * @param out The stream into which the result is sent.
	 * @param tag The tag of the log type.
	 * @return The consumer sending the log to the given stream.
	 */
	public static Consumer<String> createLogConsumer(java.io.PrintStream out, String tag) {
		return (String msg) -> out.printf(DateFormaterString + "[%2$5.5s]: %3$s\n", new Date(), "DEBUG", escape(msg));
	}

	/**
	 * Default logging using standard error stream as target of logging.
	 */
	public Logging() {
		this(createLogConsumer(System.out, "DEBUG"),
				(String msg) -> System.err.printf(DateFormaterString + "[%2$5.5s]: %3$s\n", new Date(), "ERROR", msg),
				(String msg) -> System.err.printf(DateFormaterString + "[%2$s]: %3$5.5s\n", new Date(), "WARNING", msg),
				(String msg) -> System.err.printf(DateFormaterString + "[%2$.5.s]: %3$s\n", new Date(), "INFO", msg)

		);
	}

	/**
	 * Create a new logging using given consumers to perform logging.
	 * 
	 * Undefined function indicates that kind of logging is disabled.
	 * 
	 * @param debug   The debug message consumer.
	 * @param error   The error message consumer.
	 * @param warning The warning message consumer.
	 */
	public Logging(Consumer<String> debug, Consumer<String> error, Consumer<String> warning) {
		this(debug, error, warning, null);
	}

	/**
	 * Create a new logging using given consumers to perform logging.
	 * 
	 * Undefined function indicates that kind of logging is disabled.
	 * 
	 * @param debug   The debug message consumer.
	 * @param error   The error message consumer.
	 * @param warning The warning message consumer.
	 */
	public Logging(Consumer<String> debug, Consumer<String> error, Consumer<String> warning, Consumer<String> info) {
		this.debug = debug;
		this.error = error;
		this.warning = warning;
		this.info = info;
	}

	/**
	 * Create a new logging with given logger name.
	 * 
	 * @param loggerName The logger name. Defaults to the current class name.
	 */
	public Logging(String loggerName) {
		this((loggerName == null ? (System.Logger) null : System.getLogger(loggerName)));
	}

	/**
	 * Create a logger using given logger.
	 * 
	 * @param logger The logger used for logging. If undefined, no logging is done.
	 */
	public Logging(java.util.logging.Logger logger) {
		this((String message) -> {
			if (logger != null) {
				logger.log(java.util.logging.Level.INFO, message);
			}
		}, (String message) -> {
			if (logger != null) {
				logger.log(java.util.logging.Level.SEVERE, message);
			}
		}, (String message) -> {
			if (logger != null) {
				logger.log(java.util.logging.Level.WARNING, message);
			}
		});
	}

	/**
	 * Create a logger using given logger.
	 * 
	 * @param logger The logger used for logging. If undefined, no logging is done.
	 */
	public Logging(reactor.util.Logger logger) {
		this((String message) -> {
			if (logger != null) {
				logger.debug(message);
			}
		}, (String message) -> {
			if (logger != null) {
				logger.error(message);
			}
		}, (String message) -> {
			if (logger != null) {
				logger.warn(message);
			}
		});
	}

	/**
	 * Create a logger using system logger.
	 * 
	 * @param logger The system logger logging the messages. if the logger is
	 *               undefined, the logger of the current class is used.
	 */
	public Logging(System.Logger logger) {
		final System.Logger myLogger = logger == null ? System.getLogger(getClass().getName()) : logger;

		this.debug = (String message) -> {
			myLogger.log(System.Logger.Level.DEBUG, message);
		};
		this.error = (String message) -> {
			myLogger.log(System.Logger.Level.ERROR, message);
		};
		this.warning = (String message) -> {
			myLogger.log(System.Logger.Level.WARNING, message);
		};
		this.info = (String message) -> {
			myLogger.log(System.Logger.Level.INFO, message);
		};
	}

	/**
	 * Creates a logging using given logger.
	 * 
	 * @param logger The logger to which the logging is delegated.
	 */
	public Logging(Logging logger) {
		this((logger == null ? null : (String message) -> logger.debug(message)),
				(logger == null ? null : (String message) -> logger.error(message)),
				(logger == null ? null : (String message) -> logger.warn(message)), 
				(logger == null ? null : (String message) -> logger.info(message)));
	}

	/**
	 * Does the system allow debugging.
	 * 
	 * @return True, if and only if the debug messages are dispatched.
	 */
	public synchronized boolean allowDebug() {
		return this.debug != null && Level.LEVEL_COMPARATOR.compare(currentLoggingLevel, Level.debug) <= 0;
	}

	/**
	 * Does the system allow error messages.
	 * 
	 * @return True, if and only if the debug messages are dispatched.
	 */
	public synchronized boolean allowError() {
		return this.error != null && Level.LEVEL_COMPARATOR.compare(currentLoggingLevel, Level.error) <= 0;
	}

	/**
	 * Does the system allow warning messages.
	 * 
	 * @return True, if and only if the warning messages are dispatched.
	 */
	public synchronized boolean allowWarning() {
		return this.warning != null && Level.LEVEL_COMPARATOR.compare(currentLoggingLevel, Level.warning) <= 0;
	}

	/**
	 * Does the system allow info messages.
	 * 
	 * @return True, if and only if the warning messages are dispatched.
	 */
	public synchronized boolean allowInfo() {
		return this.info != null && Level.LEVEL_COMPARATOR.compare(currentLoggingLevel, Level.info) <= 0;
	}

	/**
	 * Logs a debug message with time stamp.
	 * 
	 * @param message The debug message.
	 * @return The logged message without time stamp.
	 */
	public synchronized String debug(String message) {
		if (allowDebug()) {
			if (debug != null && message != null)
				debug.accept(message);
		}
		return message;
	}

	/**
	 * Logs a debug message with time stamp.
	 * 
	 * @param message The message format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public synchronized String debug(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return debug(java.lang.String.format(format, params));
	}

	/**
	 * Logs a debug message with time stamp.
	 * 
	 * @param message The message format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String debugMessage(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return debug(MessageFormat.format(format, params));
	}

	/**
	 * Logs an error message with time stamp.
	 * 
	 * @param message The error message.
	 * @return The logged message without time stamp.
	 */
	public synchronized String error(String message) {
		if (allowError()) {
			if (debug != null && message != null)
				error.accept(message);
		}
		return message;
	}

	/**
	 * Logs an error message with time stamp.
	 * 
	 * @param message The format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String error(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return error(String.format(format, params));
	}

	/**
	 * Logs an error message with time stamp.
	 * 
	 * @param message The message format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String errorMessage(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return error(MessageFormat.format(format, params));
	}

	/**
	 * Logs a warning message with time stamp.
	 * 
	 * @param message THe warning message.
	 * @return The logged message without time stamp.
	 */
	public synchronized String warn(String message) {
		if (allowWarning()) {
			if (debug != null && message != null)
				warning.accept(message);
		}
		return message;

	}

	/**
	 * Logs a warning message with time stamp.
	 * 
	 * @param message The format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String warn(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return warn(String.format(format, params));
	}

	/**
	 * Logs a warning message with time stamp.
	 * 
	 * @param message The message format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String warnMessage(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return warn(MessageFormat.format(format, params));
	}

	/**
	 * Logs a information message with time stamp.
	 * 
	 * @param message THe information message.
	 * @return The logged message without time stamp.
	 */
	public synchronized String info(String message) {
		if (allowWarning()) {
			if (debug != null && message != null)
				info.accept(message);
		}
		return message;

	}

	/**
	 * Logs a information message with time stamp.
	 * 
	 * @param message The format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String info(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return info(String.format(format, params));
	}

	/**
	 * Logs a information message with time stamp.
	 * 
	 * @param message The message format.
	 * @param params  The parameters for the format.
	 * @return The logged message without time stamp.
	 */
	public String infoMessage(String format, Object... params) {
		if (format == null) {
			return null;
		} else
			return info(MessageFormat.format(format, params));
	}

	
}
