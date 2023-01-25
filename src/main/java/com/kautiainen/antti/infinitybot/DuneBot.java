package com.kautiainen.antti.infinitybot;

import java.text.MessageFormat;
import java.util.ServiceConfigurationError;

import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.util.Logger;
	import reactor.util.Loggers;

/**
 * Bot variant for Dune. 
 * 
 * @author Antti Kautiainen
 *
 */
public class DuneBot extends Modiphius2d20SrdBot implements Runnable {

	
	/**
	 * The property prefix of the bot.
	 */
	protected static final String PROPERTY_PREFIX = "dunebot."; 

	/**
	 * Get the target number parameter name key.
	 */
	public static final String TARGET_NUMBER_PARAMETER_KEY = TERM_MESSAGE_PREFIX + TARGET_NUMBER_TERM_NAME;

	/**
	 * Get the critical range parameter name key.
	 */
	public static final String CRITICAL_RANGE_PARAMETER_KEY = TERM_MESSAGE_PREFIX + CRITICAL_RANGE_TERM_NAME;

	/**
	 * Get the complication range parameter name key.
	 */
	public static final String COMPLICATION_PARAMETER_KEY = TERM_MESSAGE_PREFIX + COMPLICATION_TERM_NAME;

	/**
	 * Get the skill parameter name key.
	 */
	public static final String SKILL_PARAMETER_KEY = TERM_MESSAGE_PREFIX + SKILL_TERM_NAME;

	/**
	 * Get the drive parameter name key.
	 */
	public static final String DRIVE_PARAMETER_KEY = TERM_MESSAGE_PREFIX + ATTRIBUTE_TERM_NAME;

	/**
	 * Get the dice parameter name key.
	 */
	public static final String DICE_PARAMETER_KEY = TERM_MESSAGE_PREFIX + DICE_TERM_NAME;

	/**
	 * Get the difficulty parameter name key.
	 */
	public static final String DIFFICULTY_PARAMETER_KEY = TERM_MESSAGE_PREFIX + DIFFICULTY_TERM_NAME;

	public static final String DUNE_ACTION_ROLL_COMMAND_NAME = "dunetest";

	/**
	 * The logger used to send messages.
	 */
	public static final Logger log = Loggers.getLogger(DiscordBot.class);

	
	
	/**
	 * Initialize the known commands of the discord bot.
	 */
	@Override
	protected void initCommands() {
		ApplicationCommandRequest action = getActionCommand();
		this.addCommand(action);
	}

	
	/**
	 * Create a discord bot with default commands.
	 */
	protected DuneBot() {
		super();
		// super(DiscordBot.class.getName());
		initCommands();

	}

	@Override
	protected String getPropertyBase() {
		return DuneBot.PROPERTY_BASE_NAME;
	}


	@Override
	public String getActionCommandName() {
		return DuneBot.DUNE_ACTION_ROLL_COMMAND_NAME;
	}


	/**
	 * Adds command to the known commands of the system.
	 * 
	 * @param command The added Discord command
	 */
	public void addCommand(ApplicationCommandRequest command) {
		if (command == null) {
			debug("Cannot register undefined command");
		} else {
			this.commands.put(command.name(), command);
			debugMessage(MessageFormat.format("Command {0} registered", command.name()));
		}
	}

	/**
	 * Configures the server from given command line arguments.
	 * 
	 * @param cmdLineArguments The defined list of defined command line arguments.
	 */
	@Override
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
	@Override
	protected void configure() throws ServiceConfigurationError {
		configure(new com.kautiainen.antti.infinitybot.Config(".infinitybot/config.xml"));
	}

	/**
	 * Creates a new discord bot.
	 * 
	 * @param args The command line arguments.
	 * @throws java.util.ServiceConfigurationError The configuration failed.
	 */
	public DuneBot(String[] args) throws java.util.ServiceConfigurationError {
		this();

		configure(args);

		registerCommands();

		addHandlers();

	}

	/**
	 * The category of the template specials. 
	 */
	public static final String TEMPLATES = "template";

}
