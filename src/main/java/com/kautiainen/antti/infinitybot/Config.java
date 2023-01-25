package com.kautiainen.antti.infinitybot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration loader.
 * 
 * @author Antti Kautiainen
 *
 */
public class Config extends Logging {

	/**
	 * The properties of the configuration.
	 */
	private java.util.Properties properties = new java.util.Properties();
	
	/**
	 * The configuration file name, if any exists. 
	 */
	private Optional<String> configFileName = Optional.empty();

	/**
	 * The search path of configuration files. 
	 */
	private String[] configFilePaths = new String[0];
	
	/**
	 * The current configuration file. 
	 */
	private Optional<File> configFile = Optional.empty(); 

	/**
	 * Does the configuration have unsaved changes. 
	 */
	private boolean unsavedChanges = false; 
	
	/**
	 * Create a new configuration without configuration file. 
	 */
	public Config() {
		
	}
	
	public Config(File configurationFile) throws IllegalArgumentException {
		this(configurationFile, true); 
	}
	
	/**
	 * Create a configuration from given file and possible automatic loading of the configuration.
	 * 
	 * @param configurationFile The configuration file containing the configuration.
	 * @param autoLoad Does the construction load the configuration. 
	 * @throws IllegalArgumentException The given configuration file was not suitable.
	 * @throws ServiceConfigurationError The configuration could not load non-existing file.
	 */
	public Config(File configurationFile, boolean autoLoad) {
		this.configFile = Optional.ofNullable(configurationFile);
		if (configFile.isPresent()) {
			if (configurationFile.exists() && !(configurationFile.isFile() && configurationFile.canRead())) {
				throw new IllegalArgumentException("Configuration file is not readable file"); 				
			}
		} else if (autoLoad) {
			throw new ServiceConfigurationError("Cannot load configuration from undefined file"); 
		}
		if (autoLoad) {
			loadConfig(); 			
		}
	}
	
	/**
	 * Create a new configuration with automatic loading of the configuration.
	 * 
	 * @param configurationFileName The file name of the configuration file. 
	 * @throws java.util.ServiceConfigurationError The configuration failed.
	 */
	public Config(String configurationFileName) throws java.util.ServiceConfigurationError {
		this(configurationFileName, true); 
	}
	
	/**
	 * Create a new configuration with possible autoload.
	 * 
	 * @param configurationFileName The name of the configuration file.
	 * @param autoLoad Does the operation load the configuration on construction or not.
	 */
	public Config(String configurationFileName, boolean autoLoad) {
		this(configurationFileName, autoLoad, System.getProperty("user.pwd"), System.getProperty("user.home"));		
	}

	/**
	 * Create a new configuration loaded from given list of paths.
	 * 
	 * Tries to find configuration file from each directory in the given paths.
	 * 
	 * @param configurationFileName The configuration file name. 
	 * @param configurationFilePaths The configuration file paths.
	 */
	public Config(String configurationFileName, String... configurationFilePaths) {
		this(configurationFileName, true, configurationFilePaths); 
	}

	/**
	 * Create a new configuration with given list of paths.
	 * 
	 * Tries to find configuration file from each directory in the given paths.
	 * 
	 * @param configurationFileName The configuration file name. 
	 * @param autoLoad Does the constructor automatically load the configuration. 
	 * @param configurationFilePaths The configuration file paths.
	 */
	public Config(String configurationFileName, boolean autoLoad,  String... configurationFilePaths) {
		super();
		this.configFileName = Optional.ofNullable(configurationFileName);
		this.configFilePaths = configurationFilePaths; 
		if (autoLoad) {
			loadConfig(); 
		}
	}
	
	/**
	 * Create a new configuration using given logger, configuration file names, and configuration
	 * file path.
	 * @param logger The logger used for logging. 
	 * @param configurationFileName The configuration file name. 
	 * @param autoLoad Does the operation automatically load the configuration. 
	 * @param configurationFilePaths Does the list of paths from which configuration file is sought. 
	 */
	public Config(Logging logger, String configurationFileName, boolean autoLoad, String... configurationFilePaths) 
	throws ServiceConfigurationError {
		super(logger); 
		this.configFileName = Optional.ofNullable(configurationFileName);
		if (autoLoad) {
			if (!loadConfig()) 
				throw new java.util.ServiceConfigurationError("Could not load the configuration"); 
		}
	}

	/**
	 * Loads the configuration from default location. 
	 * @return True, if and only if the configuration was loaded. 
	 */
	public boolean loadConfig() {
		if (this.configFile.isPresent()) {
			// Seeking configuration file. 
			return loadConfig(this.configFile.get());
		} else if (!this.getConfigFileName().isPresent()) {
			// The configuration file does not exist - the configuration is successfully loaded. 
			return true; 
		} else {
			return loadConfig(getConfigurationFilePaths());
		}
	}
	
	/**
	 * The path of the configuration files. 
	 * @return The configuration file paths.
	 */
	protected String[] getConfigurationFilePaths() {
		return this.configFilePaths; 
	}
	
	protected boolean hasUnsavedChanges() {
		return this.unsavedChanges;
	}
	
	protected void setUnsavedChanges(boolean hasUnsavedChanges) {
		this.unsavedChanges = hasUnsavedChanges; 
	}
	
	/**
	 * Get the XML configuration comment.
	 * 
	 * @return The XML configuration comment.
	 */
	protected String getXMLConfigComment() {
		return "InfinityBot Configuration - saved on " + java.time.LocalDateTime.now();
	}
	
	/**
	 * Saving changes to the current config file.
	 * 
	 * @return
	 */
	protected boolean saveConfig() {
		if (hasUnsavedChanges()) {
			// Saving to the default target.
			Optional<File> configFile = this.getConfigFile();
			if (configFile.isPresent()) {
				// The configuration may be saved.
				File file = configFile.get();
				if (file.isFile() && file.canWrite()) {
					try {
						this.properties.storeToXML(new java.io.FileOutputStream(file), getXMLConfigComment());
						debug(String.format("Configuration saved to \"%s\"", file.toString()));
						return true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						error(String.format("Storing to configuration file \"%s\" failed: %s", file.toString(),e.getMessage()));
						return false;
					}
				} else {
					// The configuration file cannot be saved.
					return false; 
				}
			}
			
			// The saving succeeded.
			setUnsavedChanges(false);
			return true;
		} else {
			// There is nothing to do - the save succeeds.
			return true; 
		}
	}

	/**
	 * Load configuration from given file. 
	 * @param file The file. 
	 * @return True, if and only if the given file is valid file. 
	 */
	protected boolean loadConfig(File file) {
		if (file != null && file.isFile() && file.canRead()) {
			if (Config.isXmlFileName(file.getAbsolutePath())) {
				debug("Loading from XML file {0}", file.getAbsolutePath());
				try {
					this.properties.loadFromXML(new FileInputStream(file));
					debug("Configuration loaded from XML file {0}", file.getName());
					setUnsavedChanges(false); // Resetting the unsaved changes, as changes is saved.
					return true; 
				} catch(Exception ioe) {
					return false;  
				}
			} else if (Config.isTextFileName(file.getAbsolutePath())) {
				debug("Loading from text file {0}", file.getAbsolutePath());
				try {
					this.properties.load(new FileInputStream(file));
					debug("Configuration loaded from text file {0}", file.getName());
					setUnsavedChanges(false); // Resetting the unsaved changes, as changes is saved.
					return true; 
				} catch(Exception ioe) {
					return false;  
				}				
			}
		} 
		return false; 
	}
	
	/**
	 * Tries to seek out the configuration from the given configuration path. 
	 * @param configurationFilePaths The list of defined paths from which the configuration file is sought. 
	 * @return True if, and only if the configuration file was found and contained valid configuration. 
	 */
	protected boolean loadConfig(String[] configurationFilePaths) {
		Optional<String> configFileName = this.getConfigFileName();
		if (configFileName.isPresent()) {
			boolean result = true; 
			for (String pwd : configurationFilePaths) {
				result = false; // We have at least one configuration path - the default result turns false. 
				if (pwd == null)
					continue; // Skipping null paths.
				debugMessage("Seeking config at {0}", pwd);
				File dir = new File(pwd), config = null;
				if (dir.isDirectory() && dir.canExecute() && (config = new File(dir, configFileName.get())).canRead()
						&& config.isFile()) {
					// we found configuration file, and it is readable.
					if (loadConfig(config)) {
						// Storing the configuration file. 
						this.configFile = Optional.of(config); 
						return true; 
					} else {
						debug("Loading configuration from file {0} failed", config.getName()); 
					}
				} else if (dir.isDirectory() && dir.canExecute()) {
					// The variable configuration is suitable.
					debug("Could not read config file at directory \"%s\"", pwd);
				} else if (!dir.canExecute()) {
					debug("Configuration directory \"%s\" not accessible", pwd);
				} else {
					debug("Configuration directory \"%s\" is not a directory", pwd);
				}
			}
			return result; 
		} else {
			error("Configuration file not given"); 
		}
		return false;
	}

	/**
	 * The configuration file name, if any exists. 
	 * @return The file name of the configuration file. 
	 */
	public Optional<String> getConfigFileName() {
		Optional<File> configFile = this.getConfigFile();
		if (configFile.isPresent()) {
			// The configuration file exists - using its name. 
			return Optional.of(configFile.get().getName());
		} else {
			// Using the default configuration file. 
			return this.configFileName;
		}
	}
	
	/**
	 * The configuration file from which the configuration has been loaded or into which 
	 * the configuration has been saved. 
	 * @return The configuration file, if any exists. 
	 */
	protected Optional<File> getConfigFile() {
		return this.configFile;
	}

	/**
	 * The pattern determining an file name is an xml file name. 
	 */
	protected static final java.util.regex.Pattern XML_FILE_PATTERN = 
			java.util.regex.Pattern.compile("\\.xml$");
	

	/**
	 * Test whether the given filename is xml file. 
	 * @param fileName The file name. 
	 * @return True, if and only if the given name is xml file name. 
	 */
	public static boolean isXmlFileName(CharSequence fileName) {
		if (fileName == null) return false;
		
		return XML_FILE_PATTERN.matcher(fileName).find();
	}
	
	public static boolean isTextFileName(CharSequence fileName) {
		if (fileName == null) return false;
		Matcher matcher = XML_FILE_PATTERN.matcher(fileName);
		if (matcher.find()) {
			return false; 
		} else {
			return true; 
		}
	}
	
	/**
	 * Get the XML configuration file name. 
	 * 
	 * @return THe XML configuration file name.
	 */
	public Optional<String> getXmlFile() {
		Optional<String> result = this.getConfigFileName();
		if (result.isPresent() && isXmlFileName(result.orElse(null))) {
			return result; 
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Get the text configuration file name. 
	 * 
	 * @return THe text configuration file name.
	 */
	public Optional<String> getTextFile() {
		Optional<String> result = this.getConfigFileName();
		if (isTextFileName(result.orElse(null))) {
			return result; 
		} else {
			return Optional.empty(); 
		}
	}
	
	/**
	 * Get the value of given property.
	 * 
	 * @param propertyKey The property key.
	 * @return The value of the property, if any exists.
	 */
	public Optional<String> getProperty(String propertyKey) {
		return (propertyKey == null ? Optional.empty() : Optional.ofNullable(this.properties.getProperty(propertyKey)));
	}

	/**
	 * Set the value of given property.
	 * 
	 * @param propertyKey The property key.
	 * @param value       The new value of the property.
	 * @return The value of the property, if any exists.
	 */
	public Optional<String> setProperty(String propertyKey, String value) {
		Optional<String> oldVal = getProperty(propertyKey);
		this.properties.setProperty(propertyKey, value);
		this.unsavedChanges = !(value == null?oldVal.isPresent():oldVal.get().equals(value));
		return oldVal;
	}
}