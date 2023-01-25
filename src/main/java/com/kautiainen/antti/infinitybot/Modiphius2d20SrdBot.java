package com.kautiainen.antti.infinitybot;

import java.text.CharacterIterator;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.reactivestreams.Publisher;

import com.kautiainen.antti.infinitybot.model.DiceRoller;
import com.kautiainen.antti.infinitybot.model.OrderedTerm;
import com.kautiainen.antti.infinitybot.model.RollResult;
import com.kautiainen.antti.infinitybot.model.Special;
import com.kautiainen.antti.infinitybot.model.Term;
import com.kautiainen.antti.infinitybot.model.TermValue;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * Modiphius2d20SrdBot is an abstract framework for implementing any Modiphius 2d20 system bot.
 * 
 * @author Antti Kautiainen
 *
 */
public abstract class Modiphius2d20SrdBot extends Logging {

	public static final String PROPERTY_BASE_NAME = "modiphius2d20bot";
		
	public static final String TERM_GROUP_NAME = "term";
	
	/**
	 * The term localization key prefix.
	 */
	public static final String TERM_MESSAGE_PREFIX = Modiphius2d20SrdBot.getTermPropertyKey(PROPERTY_BASE_NAME, null, TERM_GROUP_NAME);
	/**
	 * Get the target number term name.
	 */
	public static final String TARGET_NUMBER_TERM_NAME = "tn";
	
	/**
	 * The term name for critical range.
	 */
	public static final String CRITICAL_RANGE_TERM_NAME = "focus";
	
	/**
	 * The term name for skill.
	 */
	public static final String COMPLICATION_TERM_NAME = "complication";
	
	/**
	 * The term name for skill.
	 */
	public static final String SKILL_TERM_NAME = "skill";
	
	/**
	 * The term name for attribute.
	 */
	public static final String ATTRIBUTE_TERM_NAME = "drive";
	
	/**
	 * The term name for dice.
	 */
	public static final String DICE_TERM_NAME = "dice";
	
	/**
	 * The term name for difficulty.
	 */
	public static final String DIFFICULTY_TERM_NAME = "difficulty";

	/**
	 * Get the term property key. The separators are added when necessary.
	 * 
	 * @param propertyNamePrefix The property name of the term.
	 * @param termSubPrefix The term sub prefix. 
	 * @param term The term.
	 * @return The string containing the term.
	 */
	static String getTermPropertyKey(String propertyNamePrefix, String termSubPrefix, String term)
			throws NoSuchElementException {
		try {
			return getPropertyKey(propertyNamePrefix, termSubPrefix, term);
		} catch(NoSuchElementException iae) {
			throw new NoSuchElementException("Undefined term does not exist.");
		}
	}
	
	/**
	 * Get the property base name.
	 * 
	 * @return The name of the property base.
	 */
	protected String getPropertyBase() {
		return PROPERTY_BASE_NAME;
	}
	
	/**
	 * Get the name of the term group.
	 * 
	 * @return The name of the term group.
	 */
	protected String getTermGroupName() {
		return TERM_GROUP_NAME;
	}
	
	/**
	 * Get the term property key.
	 * 
	 * @param termName The term name.
	 * @return The term property key, if any exists.
	 * @throws NoSuchElementException The given term does not exist.
	 */
	public String getTermPropertyKey(String termName) 
	throws NoSuchElementException {
		return getPropertyKey(getPropertyBase(), getTermGroupName(), termName);
	}
	
	/**
	 * Get the property key.
	 * 
	 * @param groupName The property group name. Defaults to no property group.
	 * @param propertyName The property name.
	 * @return The property key, if any exists.
	 * @throws NoSuchElementException The given property does not exist.
	 */
	public String getPropertyKey(String groupName, String propertyName) 
	throws NoSuchElementException {
		return getPropertyKey(getPropertyBase(), groupName, propertyName);
	}
	
	/**
	 * Get property key. The separators are added when necessary.
	 * 
	 * @param propertyNamePrefix The property name prefix. Defaults to no property name prefix.
	 *  The prefix does not contain the ending separator.
	 * @param subPrefix The sub prefix. Defaults to no sub prefix.
	 *  The sub prefix does not contain the starting or ending prefix.
	 * @param propertyName The property name. 
	 * @return The property name key for the property with given prefix, sub prefix, and property name.
	 * @throws NoSuchElementException The given property name was undefined.
	 */
	static String getPropertyKey(String propertyNamePrefix, String subPrefix, String propertyName) {
		if (propertyName == null) throw new NoSuchElementException("Undefined property name");
		return String.format("%s%s%s%s", 
				(propertyNamePrefix != null?propertyNamePrefix:""),
				(propertyNamePrefix != null?".":""), 
				(subPrefix != null?subPrefix:""), 
				(subPrefix != null?".":""), 
				propertyName
				);
	}
	
	/**
	 * Configures the server from default configuration.
	 * 
	 * @throws ServiceConfigurationError The configuration was invalid.
	 */
	protected abstract void configure() throws ServiceConfigurationError;

	/**
	 * Configures the server from given command line arguments.
	 * 
	 * @param cmdLineArguments The defined list of defined command line arguments.
	 */
	protected abstract void configure(String[] cmdLineArguments);

	/**
	 * Initialize the known commands of the discord bot.
	 */
	protected abstract void initCommands();

	/**
	 * The sub-property name of the token.
	 */
	protected static final String TOKEN_SUBPROPERTY_NAME = "token";
	/**
	 * The sub-property name of the guilds.
	 */
	protected static final String GUILDS_SUBPROPERTY_NAME = "guilds";
	/**
	 * The configuration key for served guilds.
	 */
	protected static final String GUILDS_PROPERTY_NAME = getPropertyKey(PROPERTY_BASE_NAME, null, GUILDS_SUBPROPERTY_NAME);
	/**
	 * The configuration key for the discordbot token.
	 */
	protected static final String TOKEN_PROPERTY_NAME = getPropertyKey(PROPERTY_BASE_NAME, null, TOKEN_SUBPROPERTY_NAME);
	/**
	 * The pattern matching to a sequence of strings not containing quote or escape.
	 */
	public static final Pattern NOT_A_QUOTE_OR_ESCAPER = Pattern.compile("[^\\\\\\\"]");
	/**
	 * Pattern matching words with punctuation characters, or quoted strings with
	 * starting quote escaped.
	 */
	public static final Pattern WORD_PATTERN = Pattern
				.compile(
						"([\\w\\p{Punct}&&[^\\{\\}\\[\\]\\(\\)]&&" + NOT_A_QUOTE_OR_ESCAPER
								+ "]+|\\\"(?:(?:\\\\\\\\)*\\\\\\\"|" + NOT_A_QUOTE_OR_ESCAPER + "+)*\\\")",
						Pattern.UNICODE_CHARACTER_CLASS);
	/**
	 * The discord client used to communicate with the serve.r
	 */
	private GatewayDiscordClient connection;
	/**
	 * The guild identifiers of the Discord guild identifiers this bot serves.
	 */
	private java.util.Set<Long> guildIds = new java.util.TreeSet<>();
	/**
	 * The random number generator. 
	 */
	private final Random random = new Random();
	/**
	 * Dice roller performing the dice rolling. 
	 */
	private final DiceRoller dice = new DiceRoller(random);
	/**
	 * The current message bundle.
	 */
	private ResourceBundle messages = ResourceBundle.getBundle("DuneBotMessages");
	/**
	 * The commands known to the bot.
	 */
	protected java.util.TreeMap<String, ApplicationCommandRequest> commands = new java.util.TreeMap<>();
	/**
	 * The commands of specific guild.
	 */
	@SuppressWarnings("unused")
	private java.util.TreeMap<Long, java.util.Set<String>> guildCommands = new java.util.TreeMap<>();

	/**
	 * The main program starting the discord bot.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		try {
			DuneBot bot = new DuneBot(args);
			// we do have a bot. Starting it.
			(new Thread(bot)).start();
			System.exit(0);
		} catch (java.util.ServiceConfigurationError sce) {
			// Server startup failed.
			System.err.println(MessageFormat.format("Server configuration failed: {0}", sce.getMessage()));
			System.exit(-1);
			;
		}
	}

	/**
	 * Create a new modiphius 2d20 bot with default output streams, and default config.
	 */
	public Modiphius2d20SrdBot() {
		super();
	}
	
	/**
	 * Create a new modiphius2d20bot with given consumers as streams.
	 * 
	 * @param debug The debug and standard output stream.
	 * @param error The error stream.
	 * @param warning The warning stream.
	 */
	public Modiphius2d20SrdBot(Consumer<String> debug, Consumer<String> error, Consumer<String> warning) {
		super(debug, error, warning);
	}

	/**
	 * Get the name of the dune action command.
	 * 
	 * @return The name of the Dune Action command.
	 */
	public String getActionCommandName() {
		return "action";
	}

	/**
	 * Get the action command of the dune bot.
	 * 
	 * @return The Dune Action command.
	 */
	public ApplicationCommandRequest getActionCommand() {
		Term<Integer> term = null;
		String termName = null;
		return ApplicationCommandRequest.builder().name(getActionCommandName())
				.description("Rolls a basic skill test")
				.addOption(ApplicationCommandOptionData.builder().name(getDifficultyPameterName())
						.description(MessageFormat.format(
								"The difficulty of the action ({0,number} to {1,number}, default {2,number})", 
								getTermMinimum(termName = DIFFICULTY_TERM_NAME).get(), 
								getTermMaximum(termName).get(), 
								getTermDefault(termName).get()
								)
						)
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name(getDiceParameterName())
						.description(
								MessageFormat.format(
								"The number of dice rolled ({0,number} to {1,number}, default {2,number})",
								getTermMinimum(termName = DICE_TERM_NAME).get(), 
								getTermMaximum(termName).get(), 
								getTermDefault(termName).get()
								)
						)
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name(getDriveParameterName())
						.description(
								MessageFormat.format("The {0} of the action ({1,number} to {2,number}, defaults to {3,number})",
										getMessage(getTerm(termName = ATTRIBUTE_TERM_NAME).get().getPropertyName().orElse(termName)),
										getTermMinimum(termName).get(), 
										getTermMaximum(termName).get(), 
										getTermDefault(termName).get()
										))
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name(getSkillParameterName())
						.description(
								MessageFormat.format("The {0} of the action ({1,number} to {2,number}, defaults to {3,number})",
										getMessage(getTerm(termName = SKILL_TERM_NAME).get().getPropertyName().orElse(termName)),
										getTermMinimum(termName).get(), 
										getTermMaximum(termName).get(), 
										getTermDefault(termName).get()
										))
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name(getComplicationRangeParameterName())
						.description("The smallest number causing complication (16 to 21, default 20)")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name(getCriticalRangeParameterName())
						.description("Does the character have focus")
						.type(ApplicationCommandOption.Type.BOOLEAN.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name(getTargetNumberParameterName())
						.description("The TN of everydie of the action")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.build();
	}
	
	public Optional<Integer> getTermMaximum(@NonNull String termName) {
		Optional<Term<Integer>> term = getTerm(termName);
		if (term.isPresent() && term.get() instanceof OrderedTerm<Integer> orderedTerm) {
			return orderedTerm.getMaximumValue();
		} else {
			return Optional.empty();
		}
	}

	public Optional<Integer> getTermDefault(@NonNull String termName) {
		Optional<Term<Integer>> term = getTerm(termName);
		if (term.isPresent()) {
			return term.get().getDefaultValue();
		} else {
			return Optional.empty();
		}
	}

	public Optional<Integer> getTermMinimum(@NonNull String termName) {
		Optional<Term<Integer>> term = getTerm(termName);
		if (term.isPresent() && term.get() instanceof OrderedTerm<Integer> orderedTerm) {
			return orderedTerm.getMinimumValue();
		} else {
			return Optional.empty();
		}
	}

	public static final Term<Integer> DIFFICULTY = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), 
			DIFFICULTY_TERM_NAME, 
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, DIFFICULTY_TERM_NAME),
			1, 0, 4);
	
	public static final Term<Integer> TARGET_NUMBER = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), 
			TARGET_NUMBER_TERM_NAME,
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, TARGET_NUMBER_TERM_NAME),
			8, 1, 20);
	
	public static final Term<Integer> DICE = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), 
			DICE_TERM_NAME,
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, DICE_TERM_NAME),
			2, 1, 5);
			
	public static final Term<Integer> SKILL = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), 
			SKILL_TERM_NAME, 
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, SKILL_TERM_NAME),
			2, 1, 5);
	
	public static final Term<Integer> ATTRIBUTE = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), DIFFICULTY_TERM_NAME, 
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, ATTRIBUTE_TERM_NAME),
			2, 1, 5);
	
	public static final Term<Integer> CRITICAL_RANGE = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), CRITICAL_RANGE_TERM_NAME, 
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, CRITICAL_RANGE_TERM_NAME),
			0, 0, 4);
	
	public static final Term<Integer> CONSEQUENCE_RANGE = new OrderedTerm<Integer>(
			Comparator.naturalOrder(), COMPLICATION_TERM_NAME, 
			getTermPropertyKey(PROPERTY_BASE_NAME, TERM_GROUP_NAME, COMPLICATION_TERM_NAME),
			20, 16, 21);

	/**
	 * Initialization of the terms.
	 */
	protected void initTerms() {
		for (Term<Integer> term: Arrays.asList(DIFFICULTY, DICE, TARGET_NUMBER, SKILL, ATTRIBUTE, CRITICAL_RANGE, CONSEQUENCE_RANGE)) {
			initTermValue(term);
		}
	}
	
	/**
	 * Initialize an uninitialized term value with given term value.
	 * 
	 * @param addedValue The added new term and its value.
	 */
	protected void initTermValue(@NonNull TermValue<Integer> addedValue) {
		if (!terms_.containsKey(addedValue.getTerm().getName())) {
			// The term is new one.
			terms_.put(addedValue.getTerm().getName(), addedValue.getTerm());
		}
	}
	
	/**
	 * Initialize an uninitialized term value with given term value.
	 * 
	 * @param addedValue The added new term and its value.
	 */
	protected void initTermValue(@NonNull Term<Integer> addedValue) {
		if (!terms_.containsKey(addedValue.getName())) {
			// The term is new one.
			terms_.put(addedValue.getName(), addedValue);
		}
	}
	
	/**
	 * The mapping from term names to term values.
	 */
	private java.util.Map<String, Term<Integer>> terms_ = new java.util.TreeMap<>();
	
	/**
	 * Get the property name of the term.
	 * 
	 * @param termName The term name.
	 * @return The property name of the term.
	 */
	public Optional<String> getTermPropertyName(String termName) {
		Term<Integer> term = terms_.get(termName);
		if (term == null) return Optional.empty();
		return term.getPropertyName();
	}
	
	/**
	 * Get the default value of the given term.
	 * 
	 * @param termName The term name.
	 * @return If the term has default value, the default value as optional.
	 * Otherwise, an empty value.
	 */
	public Optional<Integer> getTermDefaultValue(String termName) {
		Term<Integer> term = getTerm(termName).orElse(null);
		if (term != null && term.hasDefaultValue()) {
			return term.getDefaultValue();
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * Get the maximum value of a term.
	 * 
	 * @param termName The term name.
	 * @return The optional containing the maximum value of the term if any exists.
	 */
	public Optional<Integer> getTermMaximumValue(String termName) {
		Optional<Term<Integer>> term = getTerm(termName);
		if (term.isPresent() && (term.get() instanceof OrderedTerm<Integer> orderedTerm)
					&& orderedTerm.hasMaximum()) {
			return orderedTerm.getMaximumValue();
		} else {
			return Optional.empty();
		}		
	}
	
	/**
	 * Get the minimum value of a term.
	 * 
	 * @param termName The term name.
	 * @return The optional containing the minimum value of the term if any exists.
	 */
	public Optional<Integer> getTermMinimumValue(String termName) {
		Optional<Term<Integer>> term = getTerm(termName);
		if (term.isPresent() && (term.get() instanceof OrderedTerm<Integer> orderedTerm)
					&& orderedTerm.hasMinimum()) {
			return orderedTerm.getMinimumValue();
		} else {
			return Optional.empty();
		}		
	}
	
	/**
	 * Get the term with a term name.
	 * 
	 * @param termName The term name.
	 * @return If the term does not exist, an empty value. Otherwise, the existing term wrapped into
	 *  optional.
	 */
	public Optional<Term<Integer>> getTerm(String termName) {
		if (terms_.containsKey(termName)) {
			return Optional.ofNullable(this.terms_.get(termName));
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * Get target number parameter name with current locale value.
	 * 
	 * @return The localized target number parameter name.
	 */
	protected String getTargetNumberParameterName() {
		return getMessage(getPropertyKey(null, TARGET_NUMBER_TERM_NAME));
	}

	/**
	 * Get critical range parameter name with current locale value.
	 * 
	 * @return The localized critical range parameter name.
	 */
	protected String getCriticalRangeParameterName() {
		return getMessage(getPropertyKey(null, CRITICAL_RANGE_TERM_NAME));
	}

	/**
	 * Get complication range parameter name with current locale value.
	 * 
	 * @return The localized complication range parameter name.
	 */
	protected String getComplicationRangeParameterName() {
		return getMessage(getPropertyKey(null, COMPLICATION_TERM_NAME));
	}

	/**
	 * Get skill parameter name with current locale value.
	 * 
	 * @return The localized skill parameter name.
	 */
	protected String getSkillParameterName() {
		return getMessage(getPropertyKey(null, SKILL_TERM_NAME));
	}

	/**
	 * Get drive parameter name with current locale value.
	 * 
	 * @return The localized drive parameter name.
	 */
	protected String getDriveParameterName() {
		return getMessage(getPropertyKey(null, ATTRIBUTE_TERM_NAME));
	}

	/**
	 * Get the difficulty parameter name with current locale value.
	 * 
	 * @return The localized difficulty parameter name.
	 */
	protected String getDifficultyPameterName() {
		return getMessage(getPropertyKey(null, DIFFICULTY_TERM_NAME));
	}

	/**
	 * Get dice parameter name with current locale value.
	 * 
	 * @return The localized dice parameter name.
	 */
	protected String getDiceParameterName() {
		return getMessage(getPropertyKey(null, DICE_TERM_NAME));
	}

	/**
	 * Configures the server from given configuration.
	 * 
	 * @param config The configuration. Defaults to the default configuration.
	 * @throws ServiceConfigurationError The configuration was invalid.
	 */
	protected void configure(Config config) throws ServiceConfigurationError {
		if (config == null) {
			// Triggering default configuration.
			configure();
			return;
		} else {
			Optional<String> property = config.getProperty(TOKEN_PROPERTY_NAME);
			debug("Using token {0}", property.orElse("!!NO TOKEN!!"));
			connection = DiscordClient.create(property.orElseThrow(
					() -> (new java.util.ServiceConfigurationError("Cannot start service without valid token"))))
					.login().block();
	
			if ((property = config.getProperty(GUILDS_PROPERTY_NAME)).isPresent()) {
				// WE have application id.
				if (Pattern.matches("^\\s*$", property.get())) {
					// Empty guild list - this allows all guilds.
					debug("Config: Empty guild list");
				} else if (Pattern.matches("^\\d+$", property.get())) {
					// We have one character sequence
					debug("Config: Single guild list");
					this.guildIds.add(Long.parseLong(property.get()));
					debug("Includge guild identifier {0}", property.get());
				} else if (Pattern.matches("^\\d+(?:\\s+\\d+)$", property.get())) {
					debug("Config: Multiple guild list");
					for (String guildId : property.get().split("\\s+")) {
						try {
							this.guildIds.add(Long.parseLong(guildId));
							debug("Include guild identifier %s", guildId);
						} catch (NumberFormatException nfe) {
							error("Invalid guild identifier %s", guildId);
						}
					}
				} else {
					throw new ServiceConfigurationError("Invalid guild identifier");
				}
			} else {
				debug("Config: Global bot without guild limitations");
			}
		}
	}

	/**
	 * Add given handlers to the handlers of the bot.
	 * 
	 * @param adapters The adapters registered as handlers of the bot.
	 */
	protected void addHandlers(ReactiveEventAdapter... adapters) {
		if (adapters != null) {
			for (ReactiveEventAdapter adapter : adapters) {
				connection.on(adapter).blockLast();
			}
		}
	}

	/**
	 * Add default handlers to the discord bot server.
	 * 
	 */
	protected void addHandlers() {
		addHandlers(new ReactiveEventAdapter() {
	
			public Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
				Optional<Snowflake> gid = event.getInteraction().getGuildId();
				if (gid.isPresent()) {
					Set<String> names = Modiphius2d20SrdBot.this.getKnownCommandNames();
					debug("Checking command {0}", event.getCommandName());
					if (names != null && names.contains(event.getCommandName())) {
						debug("Executing command %s on guild %s", event.getCommandName(), gid.get().asString());
						return executeCommand(event, getCommand(event.getCommandName()).orElse(null));
					} else {
						debug("Ignoring unknown command %s not in %s", event.getCommandName(),
								String.join(", ", names));
						return event.reply(
								String.format("I am sorry, I did not recognize command %s", event.getCommandName()));
					}
				}
	
				return Mono.empty();
			}
		});
	}

	public Modiphius2d20SrdBot(Consumer<String> debug, Consumer<String> error, Consumer<String> warning,
			Consumer<String> info) {
		super(debug, error, warning, info);
	}

	public Modiphius2d20SrdBot(String loggerName) {
		super(loggerName);
	}

	public Modiphius2d20SrdBot(Logger logger) {
		super(logger);
	}

	public Modiphius2d20SrdBot(reactor.util.Logger logger) {
		super(logger);
	}

	public Modiphius2d20SrdBot(java.lang.System.Logger logger) {
		super(logger);
	}

	/**
	 * Get message format. 
	 * @param messageName The message name. 
	 * @return The message format as string. 
	 */
	public String getMessageFormat(String messageName) {
		try {
			return messages.getString(messageName);
		} catch (MissingResourceException mre) {
			error("Could not load resource %s due missing resource error %s", messageName, mre);
		}
		return messageName; 
	}

	/**
	 * Get the given message.
	 * 
	 * @param messageName The message name.
	 * @param values      The parameter values given to the message.
	 * @return The given message with message parameters.
	 */
	public String getMessage(String messageName, Object... values) {
		if (values != null) {
			return MessageFormat.format(getMessageFormat(messageName), values);
		} else {
			return getMessageFormat(messageName);
		}
	}

	/**
	 * Perform action roll.
	 * 
	 * @param acid The acid event interaction with parameters.
	 * @return The string of the action result.
	 */
	protected String executeAction(DiceRoller roller, ApplicationCommandInteraction acid) {
		return (this.new ActionRollCommand(roller)).execute(acid);
	}

	/**
	 * Get the default title message.
	 * 
	 * @param difficulty The difficulty of the test.
	 * @param dice The number of dice.
	 * @param critRange The critical success range.
	 * @param tn The target number for each die.
	 * @param complicationRange The complication range.
	 * @return The default message for title.
	 */
	public String getDefaultTitleMessage(long difficulty, long dice, long critRange, long tn, long complicationRange) {
		// TODO Auto-generated method stub
		return "Test: D" + difficulty + " with " + dice + "d20, critical range " + critRange + ", and complication range " + complicationRange + 
				" against target number " + tn; 
	}

	/**
	 * Executes command triggered by the given event.
	 * 
	 * @param event      The event causing the command.
	 * @param definition The definition of the command.
	 * @return The resulting reply message of the command execution.
	 */
	protected Publisher<?> executeCommand(ChatInputInteractionEvent event, ApplicationCommandRequest definition) {
			// TODO: add storage for command names of registered commands linked to their
			// execution.
			try {
				if (definition == null) {
					return event.reply(
							String.format(getMessage("dunebot.messages.unknown_command_error_format"),
	//						String.format("I am sorry, but I have forgotten how to do %s",
							event.getCommandName())
							);
				} else if (definition.name() == this.getActionCommandName()) {
					// TODO: remove hard coded action name. 
					// Rolling normal roll
					return event.reply(executeAction(dice, event.getInteraction().getCommandInteraction().get()));
				} else {
					return Mono.empty();
				}
			} catch (Exception e) {
				error("Command execution failed due event %s with message %s", e.getClass(), e.getMessage());
				e.printStackTrace(System.err);
				return event.reply(
						String.format(
								getMessage("dunebot.messages.unknown_command_error_format"),
	//					"Something weird happened. ",
				e.getMessage()));
			}
		}

	/**
	 * The set of the known command names.
	 * 
	 * @return Always defined set of all known command names.
	 */
	public java.util.Set<String> getKnownCommandNames() {
		return java.util.Collections.unmodifiableSet(commands.keySet());
	}

	/**
	 * Get known command with given name.
	 * 
	 * @param cmdName The command name.
	 * @return The command request with given name.
	 */
	public Optional<ApplicationCommandRequest> getCommand(String cmdName) {
		return Optional.ofNullable((cmdName == null ? null : commands.get(cmdName)));
	}

	/**
	 * Set the application command with given name.
	 * 
	 * @param cmdName The command name.
	 * @param command The command with given name.
	 * @return The replaced command, if any exists.
	 * @throws IllegalArgumentException Either the command name or command was
	 *                                  invalid.
	 */
	protected Optional<ApplicationCommandRequest> setCommand(String cmdName, ApplicationCommandRequest command) throws IllegalArgumentException {
		if (command == null) {
			return removeCommand(cmdName);
		} else {
			return Optional.ofNullable(this.commands.put(cmdName, command));
		}
	}

	/**
	 * Remove the given command.
	 * 
	 * @param cmdName The removed command name.
	 * @return The previous value of the command, if any exists.
	 */
	public Optional<ApplicationCommandRequest> removeCommand(String cmdName) {
		return Optional.ofNullable(cmdName == null ? null : this.commands.remove(cmdName));
	}

	/**
	 * Registers all known commands to all known guilds.
	 */
	public void registerCommands() {
		registerCommands(this.getKnownCommandNames());
	}

	/**
	 * Are the request and command equals. 
	 * @param command The command on server.  
	 * @param request The request defining the command. 
	 * @return True, if and only if the values are equals. 
	 */
	public boolean equalCommand(ApplicationCommandData command, Optional<ApplicationCommandRequest> request) {
		if (request.isPresent() && command != null) {
			ApplicationCommandRequest req = request.get();
			if (req.type().equals(command.type()) && 
					req.description().equals(command.description()) &&
					req.name().equals(command.name())) {
				if (!req.options().isAbsent() && !req.options().isAbsent()) {
					for (ApplicationCommandOptionData option: req.options().get()) {
						Optional<ApplicationCommandOptionData> commandOption = 
								req.options().get().stream().filter((ApplicationCommandOptionData data)->(
										data != null && data.name().equals(option.name()))).findFirst();
						if (!Objects.equals(option, commandOption.orElse(null))) {	
							return false;
						} 
					}
					
					// The option did pass the test.
					return true; 
				}
			}
			return false;
		} else {
			// Empty is not equal with non-empty.
			return command == null && !request.isPresent(); 
		}
	}

	/**
	 * Register given commands to the server. The commands are either updated or
	 * registered on this command.
	 * 
	 * @param commands The list of registered commands.
	 * @return The list of added commands.
	 */
	public Set<String> registerCommands(java.util.Collection<String> commands) {
		debug("Registering commands" + (commands == null ? "" : commands.toString()));
		java.util.TreeSet<String> result = new java.util.TreeSet<>();
		if (commands != null) {
			// The added commands.
			for (Long guildId : this.guildIds) {
				long appId = connection.getRestClient().getApplicationId().block();
				debug("Registering commands to server %s with appId %s", guildId, appId);
				java.util.Map<String, ApplicationCommandData> discordCommands = connection.getRestClient()
						.getApplicationService().getGuildApplicationCommands(appId, guildId)
						.collectMap(ApplicationCommandData::name).block();
				debug("We got the commands listing from server!");
				for (String cmdName : commands) {
					// Pulling the current command.
					Optional<ApplicationCommandRequest> addedCommand = getCommand(cmdName);
					if (addedCommand.isPresent()) {
						ApplicationCommandData cmdOnServer = discordCommands.get(cmdName);
						if (cmdOnServer == null) {
							// We have new command.
							debug("Adding new command %s to server %s", cmdName, guildId);
							connection.getRestClient().getApplicationService()
									.createGuildApplicationCommand(appId, guildId, addedCommand.get()).subscribe();
							debug(MessageFormat.format("Command {0} succsssfully added", cmdName));
							result.add(MessageFormat.format("{0}@{1}", cmdName, guildId));
						} else if (equalCommand(cmdOnServer, addedCommand)) {
							debug("Nothing to do - the command %1$s#%#%3$s on server %2$s", 
									cmdName, guildId, cmdOnServer.id()); 
						} else {
							// We have command to alter.
							debug("Altering existing command %1$s#%3$s on server %2$s%nReplacing: %4$s%nwith %5$s", cmdName, guildId,
									cmdOnServer.id(), cmdOnServer.toString(), addedCommand.get());
							long commandId = Long.parseLong(cmdOnServer.id());
							connection.getRestClient().getApplicationService()
									.modifyGuildApplicationCommand(appId, guildId, commandId, addedCommand.get())
									.subscribe();
						}
					} else {
						error("Application command %s did not exist!", cmdName);
						removeCommand(cmdName);
						error("Missing command %s removed!", cmdName);
					}
				}
			}
		}
		return result; // Returning the added commands.
	}

	/**
	 * Connect to the discord.
	 */
	protected void connect() {
	
	}

	/**
	 * Start bot thread.
	 */
	public void run() {
		connect();
		registerCommands();
	
	}

	/**
	 * Unregister application commands.
	 * 
	 * @param guildId The guild identifier. if undefined, tries to unregister global
	 *                command.
	 * @param removed The removed commands list.
	 */
	public void unregisterCommands(Long guildId, String... removed) {
		long appId = connection.getRestClient().getApplicationId().block();
		java.util.Map<String, ApplicationCommandData> discordCommands = connection.getRestClient()
				.getApplicationService().getGuildApplicationCommands(appId, guildId)
				.collectMap(ApplicationCommandData::name).block();
	
		for (String cmdName : removed) {
			if (discordCommands.containsKey(cmdName)) {
				long commandId = Long.parseLong(discordCommands.get(cmdName).id());
				if (guildId != null) {
					connection.getRestClient().getApplicationService()
							.deleteGuildApplicationCommand(appId, guildId, commandId).subscribe();
					info("Unregitering guild command %s#%x@%x", cmdName, commandId, guildId);
				} else {
					connection.getRestClient().getApplicationService().deleteGlobalApplicationCommand(appId, commandId)
							.subscribe();
					info("Unregitering global command %s#%x", cmdName, commandId);
				}
			}
		}
	}

	public Modiphius2d20SrdBot(Logging logger) {
		super(logger);
	}


	/**
	 * The command performing Effect Roll command.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public class ActionRollCommand {
		
		/**
		 * The group name of the messages property group.
		 */
		public static final String MESSAGES_PROPERTYGROUP_NAME = "messages";

		/**
		 * The property name of the too low value.
		 */
		public static final String VALUE_TOO_LOW_PROPERTY_NAME = "too_low";

		/**
		 * The property name of the too high value.
		 */
		public static final String VALUE_TOO_HIGH_PROPERTY_NAME = "too_high";

		/**
		 * The property name of the delimiter.
		 */
		private static final String DELIMITER_PROPERTY_NAME = "delimiter";

		/**
		 * The property name of the title property.
		 */
		public static final String TITLE_FORMAT_PROPERTY_NAME = "title_format";

		/**
		 * The property name for error format property.
		 */
		public static final String ERROR_FORMAT_PROPERTY_NAME = "error_format";

		/**
		 * The message base of the action command properties base prefix.
		 */
		public static final String MESSAGE_PROPERTY_BASE = "action_roll";

		/**
		 * The prefix for the messages. 
		 */
		public static final String MESSAGE_PREFIX = getPropertyKey(MESSAGE_PROPERTY_BASE, null, MESSAGES_PROPERTYGROUP_NAME);
		
		
		/**
		 * The title format for response title. 
		 */
		public static final String TITLE_FORMAT_MESSAGE = getPropertyKey(MESSAGE_PREFIX, null, TITLE_FORMAT_PROPERTY_NAME);

		/**
		 * The delimiter message name.
		 */
		public static final String DELIMITER_MESSAGE = getPropertyKey(MESSAGE_PREFIX, null, DELIMITER_PROPERTY_NAME);

		
		/**
		 * The message name for too high complication message.
		 */
		public static final String COMPLICATION_TOO_HIGH_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, COMPLICATION_TERM_NAME, VALUE_TOO_HIGH_PROPERTY_NAME);

		/**
		 * The message name for too low complication message.
		 */
		public static final String COMPLICATION_TOO_LOW_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, COMPLICATION_TERM_NAME, VALUE_TOO_LOW_PROPERTY_NAME);
		/**
		 * The message name for too high target number message.
		 */
		public static final String TN_TOO_HIGH_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, TARGET_NUMBER_TERM_NAME, VALUE_TOO_HIGH_PROPERTY_NAME);
		/**
		 * The message name for too low target number message.
		 */
		public static final String TN_TOO_LOW_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, TARGET_NUMBER_TERM_NAME, VALUE_TOO_LOW_PROPERTY_NAME);
		/**
		 * The message name for too low difficulty message.
		 */
		public static final String DIFFICULTY_TOO_LOW_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, DIFFICULTY_TERM_NAME, VALUE_TOO_LOW_PROPERTY_NAME);
		/**
		 * The message name for too high dice message.
		 */
		public static final String TOO_MANY_DICE_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, DICE_TERM_NAME, VALUE_TOO_HIGH_PROPERTY_NAME);
		/**
		 * The message name for too low dice message.
		 */
		public static final String TOO_FEW_DICE_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, DICE_TERM_NAME, VALUE_TOO_LOW_PROPERTY_NAME);
		/**
		 * The message name for too high difficulty message.
		 */
		public static final String DIFFICULTY_TOO_HARD_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, DIFFICULTY_TERM_NAME, VALUE_TOO_HIGH_PROPERTY_NAME);

		/**
		 * The message name for too high skill message.
		 */
		public static final String SKILL_TOO_HIGH_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, SKILL_TERM_NAME, VALUE_TOO_HIGH_PROPERTY_NAME);
		
		/**
		 * The message name for too low focus message.
		 */
		public static final String SKILL_TOO_LOW_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, SKILL_TERM_NAME, VALUE_TOO_LOW_PROPERTY_NAME);

		/**
		 * The message name for too high skill message.
		 */
		public static final String MOTIVATION_TOO_HIGH_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, ATTRIBUTE_TERM_NAME, VALUE_TOO_HIGH_PROPERTY_NAME);
		
		/**
		 * The message name for too low focus message.
		 */
		public static final String MOTIVATION_TOO_LOW_MESSAGE = 
				getPropertyKey(MESSAGE_PREFIX, ATTRIBUTE_TERM_NAME, VALUE_TOO_LOW_PROPERTY_NAME);


		/**
		 * The format name for command error. 
		 */
		private static final String ACTION_COMMAND_ERROR_FORMAT = 
				getPropertyKey(MESSAGE_PREFIX, null, ERROR_FORMAT_PROPERTY_NAME);

		/**
		 * The dice roller used to perform the dice rolling.
		 */
		private DiceRoller roller;

		
		/**
		 * Create a new action dice with given dice roller.
		 * 
		 * @param roller The dice roller used to roll dice.
		 */
		public ActionRollCommand(DiceRoller roller) {
			this.roller = roller;
		}


		/**
		 * Executes the command as specified by the acid command request.
		 * 
		 * @param acid The application command interaction of Discord.
		 * @return The application command result.
		 */
		public String execute(ApplicationCommandInteraction acid) {
			long difficulty = acid.getOption(getDifficultyPameterName()).flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(1L);
			long dice = acid.getOption(getDiceParameterName()).flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(2L);
			long motivation = acid.getOption(getDriveParameterName()).flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(4L);
			long skill = acid.getOption(getSkillParameterName()).flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(4L);
			long tn = acid.getOption(getTargetNumberParameterName()).flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(skill + motivation);
			long critRange = acid.getOption(getCriticalRangeParameterName()).flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(false)?skill:1;
			long complicationRange = acid.getOption(getComplicationRangeParameterName())
					.flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(20L);
			boolean hasErrors = false;
			
			String actionFormat = getMessage(getTitleMessage());

			String titleMessage = getDefaultTitleMessage(difficulty, dice, critRange, tn, complicationRange); 
			try {
				titleMessage = String.format(actionFormat, difficulty, dice, critRange, tn, complicationRange); 
			} catch(Exception e) {
				error(String.format("Format %s failed with parameters \"%s\", \"%s\", \"%s\"", actionFormat, difficulty, dice, critRange, tn, complicationRange));
			}
			debug(titleMessage);
			StringBuilder msg = new StringBuilder("");
			if (difficulty < 0) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getDifficultyTooLowMessageKey()));
			} else if (difficulty > 5) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getDifficultyTooHighMessageKey()));
			}
			if (dice < 0) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getDiceTooSmallMessageKey()));
			} else if (dice > 5) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getDiceTooLargeMessageKey()));
			}
			if (tn < 1) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getTargetNumberTooLowMessageKey()));
			} else if (tn > 20) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getTargetNumberTooHighMessageKey()));
			}
			if (skill < 4) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getSkillTooLowMessageKey()));
			} else if (skill > 8) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getSkillTooHighMessageKey()));
			}
			if (motivation < 4) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getAttributeTooLowMessageKey()));
			} else if (motivation > 8) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getAttributeTooHighMessageKey()));
			}
			if (complicationRange < 16) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getComplicationRangeTooSmallMessageKey()));
			} else if (complicationRange > 21) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(getDelimiterMessageKey()));
				}
				msg.append(getMessage(getComplicationRangeTooLargeMessageKey()));
			}
			if (hasErrors) {
				// Outputting error rather than performing action.
				String errorMessage = String.format(getMessage(getActionCommandErrorFormatKey()), titleMessage, msg.toString()); 
				error(errorMessage);
				return errorMessage;
			} else {
				// Tossing the dice.
				RollResult result = roller.rollAction((int) dice, (int) tn, (int) critRange, (int) complicationRange);
				int complications = 0;
				java.util.List<com.kautiainen.antti.infinitybot.model.Special> specials = result.getSpecials();
				for (com.kautiainen.antti.infinitybot.model.Special  special: specials) {
					if (special.getName().equals(getComplicationSpecialName())) {
						if (special.getValue() != 0) {
							complications += special.getValue();
						}
					}
				}
			
				String roll = result.getRollFormat();
				int value = result.getValue();
				if (value < difficulty) {
					String format = getMessage("action_roll.messages.failure_format");
					debug(String.format("Result pattern: \"%s\"%n%s, %d, %d, %s", format, "\"" + titleMessage+ "\"", 
							value,complications, roll));
					try {
						return String.format(format, 
								titleMessage, value, complications, roll);
					} catch (Exception e) {
						debug("Format: " + format + " failed with "  + titleMessage + ", " + value + ", " + complications + ", " + roll);
						return titleMessage + "\nFailed with " + value + " successes and " + complications + " complications\n" + roll; 
					}
				} else {
					String format = getMessage("action_roll.messages.success_format");
					value -= difficulty;
					try {
						debug(String.format("Result pattern: \"%s\"%n%s, %d, %d, %s", format, "\"" + titleMessage + "\"",  value, complications, roll));
					} catch(IllegalArgumentException iae) {
						debug("Debug message formatting failed: "+ format + " with " + titleMessage + ", " + value + ", " + complications + ", " + roll);
					}
					try {
						return String.format(format, 
								titleMessage, value, complications, roll);
					} catch (Exception e) {
						debug("Format: " + format + " failed with "  + titleMessage + ", " + value + ", " + complications + ", " + roll);
						return titleMessage + "\nSuccess with " + value + " momentum and " + complications + " complications\n" + roll; 
					}
				}
			}

		}


		/**
		 * Get the name of the complication special.
		 * 
		 * @return The complication special name.
		 */
		public String getComplicationSpecialName() {
			return DiceRoller.Complication.COMPLICATION_NAME;
		}


		/**
		 * Get the action command error format property key.
		 * 
		 * @return The key of the property containing the action command error format.
		 */
		public String getActionCommandErrorFormatKey() {
			return ACTION_COMMAND_ERROR_FORMAT;
		}


		/**
		 * Get the action command complication range too large message key.
		 * 
		 * @return Get the action command complication range too large message key.
		 */
		public String getComplicationRangeTooLargeMessageKey() {
			return COMPLICATION_TOO_HIGH_MESSAGE;
		}


		/**
		 * Get the action command complication range too small message key.
		 * 
		 * @return The complication range too small message key.
		 */
		public String getComplicationRangeTooSmallMessageKey() {
			return COMPLICATION_TOO_LOW_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getAttributeTooHighMessageKey() {
			return MOTIVATION_TOO_HIGH_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getAttributeTooLowMessageKey() {
			return MOTIVATION_TOO_LOW_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getSkillTooHighMessageKey() {
			return SKILL_TOO_HIGH_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getSkillTooLowMessageKey() {
			return SKILL_TOO_LOW_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getTargetNumberTooHighMessageKey() {
			return TN_TOO_HIGH_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getTargetNumberTooLowMessageKey() {
			return TN_TOO_LOW_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getDiceTooLargeMessageKey() {
			return TOO_MANY_DICE_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getDiceTooSmallMessageKey() {
			return TOO_FEW_DICE_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getDifficultyTooHighMessageKey() {
			return DIFFICULTY_TOO_HARD_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getDifficultyTooLowMessageKey() {
			return DIFFICULTY_TOO_LOW_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getDelimiterMessageKey() {
			return DELIMITER_MESSAGE;
		}


		/**
		 * @return
		 */
		public String getTitleMessage() {
			return TITLE_FORMAT_MESSAGE;
		}
	}

	/**
	 * ActionRollResultFormat formats action result format.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public class ActionRollResultFormat extends java.text.Format {


		/**
		 * The serialization version of the format.
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			if (obj instanceof RollResult) {
				RollResult roll = (RollResult)obj;
				if (roll != null) {
					debug(String.format("Format: %s: Roll format %s", getMessage("action_roll.messages.roll_result_format"), 
									roll.getRollFormat()));
					toAppendTo.append(
							String.format(getMessage("action_roll.messages.roll_result_format"), 
									roll.getRollFormat()));
				}
				return toAppendTo;
			} else {
				throw new IllegalArgumentException("Cannot format given object"); 
			}
		}

		/**
		 * Parse dice value.
		 * 
		 * @param source The source string.
		 * @param pos The parse position.
		 * @return Two element object array containing the dice value and flags in that order.
		 */
		public Object parseDiceValue(String source, ParsePosition pos) {
			int index = pos.getIndex();
			int maxLen = source.length();
			if (index >= maxLen) {
				return null; 
			} else {
				java.util.List<java.util.List<String>> flagGroups = Arrays.asList(new ArrayList<>(Collections.singleton("__")), (java.util.List<String>)Arrays.asList("**", "~~")); 
				int groupIndex = 0; 
				int groupCount = flagGroups.size();
				java.util.List<String> group = groupIndex < groupCount?flagGroups.get(groupIndex):Collections.emptyList();
				int indexInGroup = 0;
				int groupSize = group.size();
				TreeSet<String> flags = new TreeSet<>();
				Integer result = 0; 

				char current = source.charAt(index);
				LinkedList<Character> tailChars = new LinkedList<>(); 
				int flagIndex = 0; 
				String currentFlag = (indexInGroup < groupSize)?group.get(indexInGroup):null;
				char[] groupChars = (currentFlag != null)?currentFlag.toCharArray():null; 
				while (groupIndex < groupCount && (index+flagIndex) < maxLen) {
					if (groupChars != null) {
						// Testing the start of the index.
						if (flagIndex < groupChars.length && current == groupChars[flagIndex]) {
							// Continuing the matching of the result.
							flagIndex++; 
							current = (index+flagIndex < maxLen)?source.charAt(index+flagIndex):CharacterIterator.DONE;
							if (flagIndex == groupChars.length) {
								// The flag was completed. Updating the current cursor position and moving to the next 
								// group.
								java.util.List<Character> groupAsList = (java.util.List<Character>)java.util.List.of(current); 
								tailChars.addAll(groupAsList);
								flags.add(group.get(indexInGroup));
								
								// Moving the index to the end of the flag.
								index += flagIndex;
								
								// Forcing move to the next group.
								groupChars = null;
								indexInGroup = groupSize;
								flagIndex = 0;
								
								// Altering the current character
								current = (index+flagIndex < maxLen)?source.charAt(index+flagIndex):CharacterIterator.DONE;
							}
						} else {
							// Matching failed. Moving to next group member.
							if (flagIndex > 0) {
								// Rewinding the cursor to the start index as the current flag did not match.
								flagIndex = 0;
								current = (index+flagIndex < maxLen)?source.charAt(index+flagIndex):CharacterIterator.DONE;
							} 
							groupChars = null;
							indexInGroup++;
						}
					} else {
						// The group chars is not defined - moving to the next member of the group.
						if (indexInGroup < groupSize) {
							// The group exists - initializing next round to go through that list.
							currentFlag = group.get(indexInGroup);
							groupChars = (currentFlag != null)?currentFlag.toCharArray():null; 
							flagIndex = 0;
						} else {
							// The group ended - moving to next group.
							groupIndex++;
							group = groupIndex < groupCount?flagGroups.get(groupIndex):Collections.emptyList();
							indexInGroup = 0; 
							groupSize = (group == null?0:group.size());
						}
					}
				}
				
				if (current == '*' || current == '~') {
					// Critical or success.
					if (index+1 == maxLen) {
						pos.setErrorIndex(index);
						return null;							
					} else if (source.charAt(index+1) != current) {
						// The flag is not known to us.
						
					} else {
						// Successful double flag.
						flags.add("" + current + current);
						index+=2;
						tailChars.addFirst(current);
						tailChars.addFirst(current);							
					}
				} else {
					// Just a number
				}
				
				
				// Dealing with numbers.
				int numberStart = index; 
				if (index < maxLen && "+-".indexOf(source.charAt(index)) >= 0) {
					index++;
				}
				while (index < maxLen && Character.isDigit(source.charAt(index))) {
					index++;
				}
				try {
					result = Integer.parseInt(source.substring(numberStart, index));
				} catch(NumberFormatException nfe) {
					// This should never happen.
					result = null; 
				}
				
				// Dealing with tail chars.
				while (!tailChars.isEmpty()) {
					if (index == maxLen || source.charAt(index) != tailChars.getFirst()) {
						pos.setErrorIndex(index);
						return null;
					} else {
						index++;
					}
				}
				pos.setIndex(index);
				return result; 
			}
		}
		
		@Override
		public Object parseObject(String source, ParsePosition pos) {
			if (pos.getErrorIndex() >= 0) return null;
			int maxLen = source.length();
			int index = pos.getIndex();
			java.util.List<? extends Object> dice = new ArrayList<>();
			int value = 0; 
			java.util.List<? extends Special> specials = new ArrayList<>();
			
		
			// Getting dice:
			while (index < maxLen && Character.isWhitespace(source.charAt(index))) {
				index++;
			}
			if (index < maxLen && source.charAt(index) == '[') {
				// We do have dice.
				index++;
				if (index < maxLen) {
					char current = source.charAt(index); 
					while (index < maxLen && current != ']') {
						
						if (current == '*' || current == '~' || current == '_') {
							// WE do have emphasizing.
							
						}
						
						index++;
						if (index == maxLen) {
							pos.setErrorIndex(index);
							return null; 
						}
					}
				}
				if (index == maxLen) {
					pos.setErrorIndex(index);
					return null;
				}
				index++; 
			} 
			
			pos.setIndex(index);
			return new RollResult(value, dice, specials);
		}
		
	}
}