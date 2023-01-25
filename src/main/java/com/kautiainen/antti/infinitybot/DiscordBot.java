package com.kautiainen.antti.infinitybot;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.reactivestreams.Publisher;

import com.kautiainen.antti.infinitybot.model.DiceRoller;
import com.kautiainen.antti.infinitybot.model.QualityTemplate;
import com.kautiainen.antti.infinitybot.model.RollResult;

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
import reactor.util.Logger;
import reactor.util.Loggers;

public class DiscordBot extends Logging implements Runnable {

	/**
	 * The mapping from category names to the specials of that category.
	 */
	protected Map<String, SpecialRegistry> specialsByCategory = new TreeMap<>();

	/**
	 * Get the default special templates. 
	 * @return THe recognized special templates. 
	 */
	protected static java.util.List<com.kautiainen.antti.infinitybot.model.Special> defaultSpecialTemplates() {
		return Arrays.asList(
				new QualityTemplate("Vicious", 1, true), 
				new QualityTemplate("Penetration", null, true),
				new QualityTemplate("Effect", null, true), 
				new QualityTemplate("Tariff", 1, true)
				); 
	}
	
	/**
	 * Get all specials attached to given category.
	 * 
	 * @param category The category of specials.
	 * @return The set of specials in given category, if any exists.
	 */
	public Optional<Set<com.kautiainen.antti.infinitybot.model.Special>> getSpecialsOfCategory(String category) {
		return Optional.ofNullable(
				new TreeSet<com.kautiainen.antti.infinitybot.model.Special>(specialsByCategory.get(category).values()));
	}

	protected SpecialRegistry createSpecialRegistry(String category) {
		return new SpecialRegistry();
	}

	public void addSpecialToCategory(String category, com.kautiainen.antti.infinitybot.model.Special Special) {
		if (!specialsByCategory.containsKey(category)) {
			this.specialsByCategory.put(category, createSpecialRegistry(category));
		}
		this.specialsByCategory.get(category).register(Special);
	}

	public void removeSpecialFromCategory(String category, com.kautiainen.antti.infinitybot.model.Special special) {
		if (specialsByCategory.containsKey(category)) {
			this.specialsByCategory.get(category).unregister(special);
		}
	}

	/**
	 * Initializes the special registry. 
	 * @param config The configuration of the discord bot. 
	 */
	public void initSpecialRegistry(Config config) {
		SpecialRegistry registry = this.getSpecialRegistry();
		for (com.kautiainen.antti.infinitybot.model.Special special : defaultSpecialTemplates()) {
			if (registry.register(special)) {
				try {
					debug("Registered special %s", special); 
				} catch( IllegalArgumentException ie) {
					error("Registering special %s failed due %s", special, ie);
				}
			} else {
				debug("Registration of special %s failed", special);
			}
		}
	}
	
	/**
	 * The configuration key for served guilds.
	 */
	protected static final String INFINITYBOT_GUILDS = "infinitybot.guilds";

	/**
	 * The configuration key for bot token.
	 */
	protected static final String INFINITYBOT_TOKEN = "infinitybot.token";

	/**
	 * The logger used to send messages.
	 */
	public static final Logger log = Loggers.getLogger(DiscordBot.class);

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

	public static final Pattern QUIT_PATTERN = Pattern.compile("^q(?:uit)?$", Pattern.CASE_INSENSITIVE);

	public static final Pattern LIST_GAMES = Pattern
			.compile("^((?:list\\s+)?(games))((?:\\s+" + WORD_PATTERN.toString() + ")*)$");

	/**
	 * The discord client used to communicate with the serve.r
	 */
	private GatewayDiscordClient connection;

	/**
	 * The guild identifiers of the Discord guild identifiers this bot serves.
	 */
	private java.util.Set<Long> guildIds = new java.util.TreeSet<>();

	/**
	 * Initialize the known commands of the discord bot.
	 */
	protected void initCommands() {
		ApplicationCommandRequest action = ApplicationCommandRequest.builder().name("infinitytest")
				.description("Rolls a basic skill test")
				.addOption(ApplicationCommandOptionData.builder().name("difficulty")
						.description("The difficulty of the action (0 to 5)")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name("dice")
						.description("The number of dice rolled (0 to 5, default 2)")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name("tn")
						.description("The TN of everydie of the action")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name("focus")
						.description("The focus of the action (0 to 5, default 0)")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name("complication")
						.description("The smallest number causing complication (16 to 21, default 20)")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.build();
		this.addCommand(action);

		ApplicationCommandRequest effect = ApplicationCommandRequest.builder().name("effect")
				.description("Rolls an effect check with combat dice")
				.addOption(ApplicationCommandOptionData.builder().name("base")
						.description("The base number of the result.")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(ApplicationCommandOptionData.builder().name("dice").description("The number of dice rolled")
						.type(ApplicationCommandOption.Type.INTEGER.getValue()).required(false).build())
				.addOption(
						ApplicationCommandOptionData.builder().name("traits").description("The list of special traits")
								.type(ApplicationCommandOption.Type.STRING.getValue()).required(false).build())
				.build();
		this.addCommand(effect);
	}

	/**
	 * Create a discord bot with default commands.
	 */
	protected DiscordBot() {
		super();
		// super(DiscordBot.class.getName());
		initCommands();

	}

	/**
	 * Adds command to the known commands of the system.
	 * 
	 * @param command The added Discord command
	 */
	private void addCommand(ApplicationCommandRequest command) {
		if (command == null) {
			debug("Cannot register undefined command");
		} else {
			this.commands.put(command.name(), command);
			debugMessage("Command {0} registered", command.name());
		}
	}

	/**
	 * Configures the server from given command line arguments.
	 * 
	 * @param cmdLineArguments The defined list of defined command line arguments.
	 */
	protected void configure(String[] cmdLineArguments) {
		String configFile = ".infinitybot/config.xml";

		// Handling command line parameters.

		// Performing configuration.
		configure(new com.kautiainen.antti.infinitybot.Config(configFile));
	}

	/**
	 * Configures the server from default configuration.
	 * 
	 * @throws ServiceConfigurationError The configuration was invalid.
	 */
	protected void configure() throws ServiceConfigurationError {
		configure(new com.kautiainen.antti.infinitybot.Config(".infinitybot/config.xml"));
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
			Optional<String> property = config.getProperty(INFINITYBOT_TOKEN);
			debug("Using token {0}", property.orElse("!!NO TOKEN!!"));
			connection = DiscordClient.create(property.orElseThrow(
					() -> (new java.util.ServiceConfigurationError("Cannot start service without valid token"))))
					.login().block();

			if ((property = config.getProperty(INFINITYBOT_GUILDS)).isPresent()) {
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
					Set<String> names = DiscordBot.this.getKnownCommandNames();
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

	/**
	 * Creates a new discord bot.
	 * 
	 * @param args The command line arguments.
	 * @throws java.util.ServiceConfigurationError The configuration failed.
	 */
	public DiscordBot(String[] args) throws java.util.ServiceConfigurationError {
		this();

		configure(args);

		registerCommands();

		addHandlers();

	}

	/**
	 * The random number generator. 
	 */
	private final Random random = new Random();

	/**
	 * Dice roller performing the dice rolling. 
	 */
	private final DiceRoller dice = new DiceRoller(random);


	/**
	 * The category of the template specials. 
	 */
	public static final String TEMPLATES = "template";
	
	/**
	 * The mapping from known special names to the template specials returning a new
	 * instance of a special when stacked.
	 */
	protected SpecialRegistry knownSpecials = this.createSpecialRegistry(TEMPLATES);

	/**
	 * Get the special registry. 
	 * @return The special registry. 
	 */
	protected SpecialRegistry getSpecialRegistry() {
		return knownSpecials; 
	}
	
	/**
	 * Gets the special of the given string representation.
	 * 
	 * @param stringRepresentation The string rep of the special.
	 * @return The special of the given name, if it is valid special for current
	 *         bot.
	 */
	public Optional<? extends com.kautiainen.antti.infinitybot.model.Special> getSpecial(String stringRepresentation) {

		SpecialRegistry knownSpecials = this.getSpecialRegistry(); 
		// Stacking weapon traits.
		if (stringRepresentation != null) {
			Optional<Special> basicInfo = DiscordBot.Special.of(stringRepresentation);
			if (basicInfo.isPresent()) {
				if (knownSpecials.containsKey(basicInfo.get().getName())) {
					// WE do have known special.
					return Optional.of(
							knownSpecials.get(basicInfo.get().getName()).getStacked(basicInfo.get().getValue()));
				} else {
					// Creating the given special.
					return basicInfo;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * The command performing Effect Roll command.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public class ActionRollCommand {

		/**
		 * The prefix for the messages. 
		 */
		public static final String MESSAGE_PREFIX = "action_roll.messages.";
		
		
		/**
		 * The title format for response title. 
		 */
		public static final String TITLE_FORMAT_MESSAGE = MESSAGE_PREFIX + "title_format";

		/**
		 * The delimiter message name.
		 */
		public static final String DELIMITER_MESSAGE = MESSAGE_PREFIX + "delimiter";

		/**
		 * The message name for too high complication message.
		 */
		public static final String COMPLICATION_TOO_HIGH_MESSAGE = MESSAGE_PREFIX + "complication.too_high";

		/**
		 * The message name for too low complication message.
		 */
		public static final String COMPLICATION_TOO_LOW_MESSAGE = MESSAGE_PREFIX + "complication.too_low";
		/**
		 * The message name for too high focus message.
		 */
		public static final String FOCUS_TOO_HIGH_MESSAGE = MESSAGE_PREFIX + "focus.too_high";

		/**
		 * The message name for too low focus message.
		 */
		public static final String FOCUS_TOO_LOW_MESSAGE = MESSAGE_PREFIX + "focus.too_low";
		/**
		 * The message name for too high target number message.
		 */
		public static final String TN_TOO_HIGH_MESSAGE = MESSAGE_PREFIX + "tn.too_high";
		/**
		 * The message name for too low target number message.
		 */
		public static final String TN_TOO_LOW_MESSAGE = MESSAGE_PREFIX + "tn.too_low";
		/**
		 * The message name for too low difficulty message.
		 */
		public static final String DIFFICULTY_TOO_LOW_MESSAGE = MESSAGE_PREFIX + "difficulty.too_low";
		/**
		 * The message name for too high dice message.
		 */
		public static final String TOO_MANY_DICE_MESSAGE = MESSAGE_PREFIX + "dice.too_high";
		/**
		 * The message name for too low dice message.
		 */
		public static final String TOO_FEW_DICE_MESSAGE = MESSAGE_PREFIX + "dice.too_low";
		/**
		 * The message name for too high difficulty message.
		 */
		public static final String DIFFICULTY_TOO_HARD_MESSAGE = MESSAGE_PREFIX + "difficulty.too_high";
		private DiceRoller roller;

		public ActionRollCommand(DiceRoller roller) {
			this.roller = roller;
		}


		/**
		 * The current message bundle.
		 */
		private ResourceBundle messages = ResourceBundle.getBundle("ActionRollMessages");

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
		 * Executes the command as specified by the acid command request.
		 * 
		 * @param acid The application command interaction of Discord.
		 * @return The application command result.
		 */
		public String execute(ApplicationCommandInteraction acid) {
			long difficulty = acid.getOption("difficulty").flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(1L);
			long dice = acid.getOption("dice").flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(2L);
			long tn = acid.getOption("tn").flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(1L);
			long critRange = acid.getOption("focus").flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(0L);
			long complicationRange = acid.getOption("complication")
					.flatMap(ApplicationCommandInteractionOption::getValue)
					.map(ApplicationCommandInteractionOptionValue::asLong).orElse(20L);
			boolean hasErrors = false;
			
			String actionFormat = getMessage(TITLE_FORMAT_MESSAGE); 

			String titleMessage = String.format(actionFormat, difficulty, dice, critRange, tn, complicationRange); 
			debug(titleMessage);
			StringBuilder msg = new StringBuilder("I cannot do this as ");
			if (difficulty < 0) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(DIFFICULTY_TOO_LOW_MESSAGE));
			} else if (difficulty > 5) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(DIFFICULTY_TOO_HARD_MESSAGE));
			}
			if (dice < 0) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(TOO_FEW_DICE_MESSAGE));
			} else if (dice > 5) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(TOO_MANY_DICE_MESSAGE));
			}
			if (tn < 1) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(TN_TOO_LOW_MESSAGE));
			} else if (tn > 20) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(TN_TOO_HIGH_MESSAGE));
			}
			if (critRange < 0) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(FOCUS_TOO_LOW_MESSAGE));
			} else if (critRange > 5) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(FOCUS_TOO_HIGH_MESSAGE));
			}
			if (complicationRange < 16) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(COMPLICATION_TOO_LOW_MESSAGE));
			} else if (complicationRange > 21) {
				hasErrors = true; 
				if (msg.length() > 0) {
					msg.append(getMessage(DELIMITER_MESSAGE));
				}
				msg.append(getMessage(COMPLICATION_TOO_HIGH_MESSAGE));
			}
			if (hasErrors) {
				// Outputting error rather than performing action.
				return msg.toString();
			} else {
				// Tossing the dice.
				RollResult result = roller.rollAction((int) dice, (int) tn, (int) critRange, (int) complicationRange);
				int complications = 0;
				java.util.List<com.kautiainen.antti.infinitybot.model.Special> specials = result.getSpecials();
				for (com.kautiainen.antti.infinitybot.model.Special  special: specials) {
					if (special.getName().equalsIgnoreCase("complication")) {
						complications += special.getValue();
					}
				}
			
				if (result.getValue() < difficulty) {
					return String.format(getMessage("action_roll.messages.failure_format"), 
							titleMessage, result.getValue(), complications, result.toString());
				} else {
					return String.format(getMessage("action_roll.messages.success_format"), 
							titleMessage, result.getValue() - difficulty, complications, result.toString()); 
				}
				
				// return result.toString(); 
			}

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
	 * Perform effect roll.
	 * 
	 * @param acid The acid event interaction with parameters.
	 * @return The string of the action result.
	 */
	protected String executeEffect(DiceRoller roller, ApplicationCommandInteraction acid) {
		long base = acid.getOption("base").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asLong).orElse(0L);
		long dice = acid.getOption("dice").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asLong).orElse(0L);
		String traits = acid.getOption("traits").flatMap(ApplicationCommandInteractionOption::getValue)
				.map(ApplicationCommandInteractionOptionValue::asString).orElse("");
		java.util.List<com.kautiainen.antti.infinitybot.model.Special> traitList = new ArrayList<>();
		if (traits == null || traits.isEmpty()) {
			// Adding basic stacking trait which does not affect the total
			traitList.add(new StackingSpecial("Effect", 1));
		} else {
			// Calculating trait list.
			Optional<? extends com.kautiainen.antti.infinitybot.model.Special> newTrait;
			for (String trait : traits.split("\\s+")) {
				if ((newTrait = getSpecial(trait)).isPresent()) {
					traitList.add(newTrait.get());
				} else {
					debug("Unknown trait %s", trait);
					traitList.add(new Special(trait, null));
				}
			}
		}
		RollResult result = roller.rollCD(
				Math.max(base > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) base, Integer.MIN_VALUE),
				Math.max(dice > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) dice, 0), traitList);
		return result.toString();
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
				return event
						.reply(String.format("I am sorry, but I have forgotten how to do %s", event.getCommandName()));
			} else if (definition.name() == "action" || definition.name() == "infinitytest") {
				// Rolling normal roll
				return event.reply(executeAction(dice, event.getInteraction().getCommandInteraction().get()));
			} else if (definition.name() == "effect" || definition.name() == "cd") {
				// Rolling CD
				return event.reply(executeEffect(dice, event.getInteraction().getCommandInteraction().get()));
			} else {
				return Mono.empty();
			}
		} catch (Exception e) {
			error("Command execution failed due event %s with message %s", e.getClass(), e.getMessage());
			e.printStackTrace(System.err);
			return event.reply("Something weird happened. " + e.getMessage());
		}
	}

	/**
	 * The commands known to the bot.
	 */
	private java.util.TreeMap<String, ApplicationCommandRequest> commands = new java.util.TreeMap<>();

	/**
	 * The commands of specific guild.
	 */
	@SuppressWarnings("unused")
	private java.util.TreeMap<Long, java.util.Set<String>> guildCommands = new java.util.TreeMap<>();

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
	protected Optional<ApplicationCommandRequest> setCommand(String cmdName, ApplicationCommandRequest command)
			throws IllegalArgumentException {
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
			return req.type().equals(command.type()) && 
					req.description().equals(command.description()) &&
					req.options().equals(command.options()) &&
					req.name().equals(command.name());
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
							debug("Altering existing command %1$s#%3$s on server %2$s", cmdName, guildId,
									cmdOnServer.id());
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
	 * Special result of the Infinity.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class Special implements com.kautiainen.antti.infinitybot.model.Special {
		private String name;
		private int value = 1;
		private boolean stacking = false;

		/**
		 * Creates a new special value.
		 * 
		 * @param name     The name of the special value.
		 * @param value    THe value of the special value.
		 * @param stacking Does the value stack, or is the largest number used.
		 */
		public Special(String name, int value, boolean stacking) {
			if (name == null || name.isEmpty() || !name.trim().equals(name)) {
				throw new IllegalArgumentException(
						"Invalid special efect name " + (name == null ? "does not exist" : "\"" + name + "\""));
			}
			this.name = name;
			this.value = value;
			this.stacking = stacking;
		}

		/**
		 * Creates a new special value.
		 * 
		 * The special value is stacking, if it has defined value differing from 0.
		 * 
		 * @param name  The name of the special value.
		 * @param value THe value of the special value.
		 */
		public Special(String name, Integer value) {
			this(name, value == null ? 1 : value, value != null && value != 0);
		}

		/**
		 * Create a special from its string representation.
		 * 
		 * @param stringRep The string representation.
		 * @return THe created Special, if any exists.
		 */
		public static Optional<Special> of(String stringRep) {
			Pattern pattern = Pattern.compile("^" + WORD_PATTERN.toString() + 
					"(?:\\(([+\\-\\s]\\d*)\\))?$");
			Matcher match = pattern.matcher(stringRep);
			if (match.matches()) {
				String name = match.group(1), value = match.group(2);
				return Optional.of(new Special(name, value == null ? null : Integer.parseInt(value)));
			} else {
				return Optional.empty();
			}
		}

		/**
		 * Does the special value stack.
		 * 
		 * @return True, if and only if the special value does not stack.
		 */
		public boolean stacks() {
			return this.stacking;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public int getValue() {
			return this.value;
		}

		@Override
		public Special getStacked(int value) {
			return new Special(this.getName(), this.getValue() + (this.stacks() ? value : 0), this.stacks());
		}

		/**
		 * Compare this special with integer.
		 * 
		 * Specials are always greater than any number by default.
		 * 
		 * @param number The integer number with whom the special is compared with.
		 * @return -1, if this special is greater than number, 0, if this number is
		 *         equal to the number, and 1, if this is greater than the number.
		 */
		public int compareTo(Integer number) {
			return 1;
		}

		@Override
		public boolean equals(Object other) {
			return other != null && other instanceof Special && compareTo((Special) other) == 0;
		}

		/**
		 * The format pattern.
		 * 
		 * @return The format pattern of special values.
		 */
		public String getFormatPattern() {
			return "%s{%d)";
		}

		@Override
		public String toString() {
			return toString(this);
		}

	}

	/**
	 * Special values which are stacking.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class StackingSpecial extends Special {
		public StackingSpecial(String name, int value) {
			super(name, value, true);
		}
	}

	/**
	 * The Vicious specials does have numeric value.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class Vicious extends StackingSpecial {
		/**
		 * Create a new vicious special.
		 * 
		 * @param value The value of the vicious.
		 */
		public Vicious(int value) {
			super("Vicious", value);
		}

		@Override
		public Optional<Integer> getNumberValue() {
			return Optional.of(this.getValue());
		}
	}

	protected void connect() {

	}

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

	/**
	 * 
	 * The command line allowing controlling the bot.
	 * 
	 * @author Antti Kautainen
	 *
	 */
	public class CLI extends Thread {

		/**
		 * The input stream used to read input commands.
		 */
		private java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

		/**
		 * The output stream used to write messages.
		 */
		private java.io.PrintWriter out = new java.io.PrintWriter(System.out);

		/**
		 * The error stream used to write error messages.
		 */
		private java.io.PrintWriter err = new java.io.PrintWriter(System.err);

		public CLI() {

		}

		/**
		 * Do we have interactive CLI.
		 * 
		 * @return True, if and only if the interface is interactive with prompts.
		 */
		public boolean interactive() {
			return true;
		}

		/**
		 * Get the prompt of the interactive ui.
		 * 
		 * @return
		 */
		public String prompt() {
			return "DiscordBot> ";
		}

		/**
		 * The main program running.
		 */
		public void run() {

			String line;
			boolean goOn = true;
			try {
				if (interactive()) {
					out.println("Starting console");
					out.print(prompt());
				}
				while (goOn && (line = in.readLine()) != null) {
					line = line.trim();
					if (QUIT_PATTERN.matcher(line).matches()) {
						out.println("Quitting...");
						goOn = false;
					} else if (LIST_GAMES.matcher(line).matches()) {
						out.println("\n=======================\nListing games");
						out.println("========================\n");
					} else if (!line.isEmpty()) {
						out.println("Unknown command");
					}
				}
			} catch (IOException e) {
				err.println("Execution terminated due exception");
				e.printStackTrace();
			}
			out.println("Closing server");
			System.exit(-1);
		}
	}

	/**
	 * The main program starting the discord bot.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		try {
			DiscordBot bot = new DiscordBot(args);
			// we do have a bot. Starting it.
			CLI cli = bot.new CLI();
			(new Thread(cli)).start();

			(new Thread(bot)).start();
			System.exit(0);
		} catch (java.util.ServiceConfigurationError sce) {
			// Server startup failed.
			System.err.println(MessageFormat.format("Server configuration failed: {0}", sce.getMessage()));
			System.exit(-1);
			;
		}
	}

}