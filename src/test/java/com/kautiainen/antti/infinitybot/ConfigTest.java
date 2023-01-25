package com.kautiainen.antti.infinitybot;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import reactor.util.annotation.NonNull;

class ConfigTest {

	/**
	 * Collection of file types.
	 * 
	 * Each file type is unique within the type. 
	 *  
	 * @author Antti Kautiainen
	 *
	 */
	public static class FileTypes {
		/**
		 * The registered types. 
		 */
		private final Map<String, FileType> registeredTypes = 
				new TreeMap<>(); 
	
		public FileTypes() {
			
		}
		
		/**
		 * Test validity of the type. 
		 * @param name The tested name. 
		 * @return True, if and only if the given name is valid type name
		 *  for this collection. 
		 */
		public boolean validTypeName(String name) {
			return name != null && !name.isEmpty() && name.trim().equals(name);
		}

		/**
		 * Registers the type to the system wide registry of types. 
		 * @param type The registered type.
		 * @throws IllegalArgumentException The type was undefined, or otherwise
		 *  invalid. 
		 * @throws IllegalStateException There was already a definition for
		 *  the type. 
		 */
		public void registerType(FileType type) {
			if (type == null) {
				throw new IllegalArgumentException("Invalid type"); 
			} else if (registeredTypes.containsKey(type.getName())) {
				throw new IllegalStateException("Type already registered"); 
			} else {
				registeredTypes.put(type.getName(), type);
			}
		}
		
	 
		/**
		 * Get the type of given name. 
		 * @param typeName The type name. 
		 * @return The type associated with given name, if any exists. 
		 */
		public Optional<FileType> getType(String typeName) {
			return Optional.ofNullable(this.registeredTypes.get(typeName));
		}
	}
	
	/**
	 * File type descriptor. 
	 * @author Antti Kautiainen. 
	 *
	 */
	public interface FileType {

		/**
		 * The global types registry. 
		 */
		public static final FileTypes GLOBAL_TYPES = new FileTypes(); 
		
		/**
		 * Registers the type to the system wide registry of types. 
		 * @param type The registered type.
		 * @throws IllegalArgumentException The type was undefined, or otherwise
		 *  invalid. 
		 * @throws IllegalStateException There was already a definition for
		 *  the type. 
		 */
		public static void registerType(FileType type) {
			GLOBAL_TYPES.registerType(type); 
		}
		
		/**
		 * Tests validity of the type name. 
		 * @param name The tested name. 
		 * @return True, if and only if, the given name is valid type name. 
		 */
		public static boolean validTypeName(String name) {
			return GLOBAL_TYPES.validTypeName(name);
		}

		/**
		 * Get the name of the type. 
		 * 
		 * @return Always defined trimmed, and non-empty,  name of the type. 
		 */
		public String getName();

		/**
		 * Create a new file type with given name. 
		 * @param typeName The name of the type. 
		 * @return The type of given name. 
		 * @throws IllegalArgumentException The given name was invalid. 
		 */
		public static Optional<FileType> fromName(String typeName) throws IllegalArgumentException {
			return GLOBAL_TYPES.getType(typeName); 
		}
		
		
		default Optional<String> getDefaultEncoding() {
			return Optional.empty(); 
		}
		
		/**
		 * Get the list of known suffixes of the file type. 
		 * @return The list of suffixes, if the type limits suffixes. 
		 */
		default Optional<Set<String>> getSuffixes() {
			return Optional.empty();
		}

		default Predicate<String> getSuffixPredicate(String suffix) {
			return suffix==null?(String name)->true:
				(String name)->(name != null && name.endsWith(suffix));
		}
		
		/**
		 * The predicate testing valid file name for this file type. 
		 * @return True, if and only if, the filename passes the validity
		 *  tests of this type. 
		 */
		default Predicate<String> getFileNamePredicate() {
			Optional<? extends java.util.List<Predicate<String>>> validators = 
					this.getFileNameValidators();
			
			return (String fileName) -> (
					(validators.isPresent() && !validators.get().isEmpty())?
					validators.get().stream().anyMatch(
							(Predicate<String> tester) -> tester.test(fileName)):
						true); 
		}
		
		/**
		 * Get the predicates testing for valid file name. 
		 * @return The list of predicates testing a single suffix.  
		 */
		default Optional<java.util.List<Predicate<String>>> getFileNameValidators() {
			Optional<Set<String>> suffixes = this.getSuffixes();
			return suffixes.isPresent()
					?
					Optional.of(suffixes.get().stream().filter(
							(String suffix)->(suffix != null)
							).map(
							(String suffix)->(getSuffixPredicate(suffix)))
							.collect(Collectors.toList()))
					:Optional.empty(); 
		}
	}
	
	/**
	 * The basic file type. 
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class BasicFileType implements FileType {
		
		private String name; 
		
		private java.util.Set<String> extensions = null; 
		
		private Optional<String> defaultEncoding = Optional.empty();
		
		public BasicFileType(String name) throws IllegalArgumentException, IllegalStateException {
			if (!FileType.validTypeName(name)) {
				throw new IllegalArgumentException("Invalid type name"); 
			}
			this.name = name; 
			FileType.registerType(this);
		}
		
		public BasicFileType(String name, String defaultEncoding, 
				java.util.List<String> extensions) {
			this(name); 
			this.defaultEncoding = Optional.ofNullable(defaultEncoding);
			this.extensions = extensions==null?null:new java.util.TreeSet<>(extensions);
		}
		
		@Override
		public String getName() {
			return this.name; 
		}

		@Override
		public Optional<String> getDefaultEncoding() {
			// TODO Auto-generated method stub
			return this.defaultEncoding;
		}

		@Override
		public Optional<Set<String>> getSuffixes() {
			// TODO Auto-generated method stub
			return Optional.ofNullable(Collections.unmodifiableSet(this.extensions));
		}
		
		
	}
	
	/**
	 * Class representing typed file which contains extra information of the file type. 
	 */
	public static class TypedFile extends File {
		
		/**
		 * The serialization version of the typed file.
		 */
		private static final long serialVersionUID = 1527112578721220343L;
		
		/**
		 * The file type of the file.
		 */
		private @NonNull FileType type; 
		
		/**
		 * Create a new typed file.
		 * 
		 * @param typeName the name of the type.
		 * @param file The file. 
		 */
		public TypedFile(@NonNull String typeName, @NonNull File file) {
			super(file==null?null:file.getAbsolutePath());
			this.setName(typeName); 
		}
		
		/**
		 * Create a new typed file.
		 * 
		 * @param typeName the name of the type.
		 * @param parent The parent file.
		 * @param child the child file name.
		 */
		public TypedFile(String typeName, File parent, String child) {
			super(parent,child);
			this.setName(typeName); 
		}
		
		/**
		 * Setting the name of the type. 
		 * @param name The name of the type. 
		 */
		protected void setName(String name) throws IllegalArgumentException {
			if (name == null || name.isEmpty() || !name.trim().equals(name)) 
				throw new IllegalArgumentException("Invalid type name"); 
			type = FileType.fromName(name).orElse(null);
			if (type == null) {
				// Creating new file type. 
				type = new BasicFileType(name);
			}
		}
		
		/**
		 * Get the name of the file type.
		 */
		public String getName() {
			return type.getName();
		}
	}
	
	@Test
	void testLoadConfig() {
		String fileName = ".infinitybot/config.xml"; 
		Config test = new Config(fileName, false);
		boolean result;
		
		try {
			result = test.loadConfig();
			System.out.printf("Configuration loaded from %s: %s\n", 
					(Config.isXmlFileName(test.getConfigFileName().orElse(null))?"XML":"Text"),
					test.getConfigFile().orElse(null));
			for (String key: Arrays.asList(DiscordBot.INFINITYBOT_TOKEN)) {
				if (!test.getProperty(DiscordBot.INFINITYBOT_TOKEN).isPresent()) {
					fail(MessageFormat.format("Missing existing key {0}", key)); 
				}
			}
		} catch(java.util.ServiceConfigurationError sce) {
			sce.printStackTrace();
			fail("Failed due " + sce.toString()); 
		}
	}

	@Test
	void testGetConfigFile() {
		String fileName = "Test File"; 
		Config test;
		fileName = "Test File";
		test = new Config(fileName, false);
		assertTrue(test.getConfigFileName().isPresent()); 
		assertEquals(fileName, test.getConfigFileName().get());
		
	}
	
	@Test
	void testIsXmlFile() {
		String fileName; 
		for (String tested: Arrays.asList(".infinitybot/config.xml", "config.xml", "infinitybot/config.xml", "index.xml", ".xml")) {
			assertTrue(Pattern.matches("^.*?\\.xml$", tested), "The Pattern is broken!");
			assertTrue(Config.isXmlFileName(tested), MessageFormat.format("Did not recognize {0} as xml!", tested));
		}
		for (String tested: Arrays.asList("config.txt", "infinitybot/config.txt", "index.txt", "foobar.xml/index.txt")) {
			assertFalse(Pattern.matches("^.*?\\.xml$", tested), "The Pattern is broken!");
			assertFalse(Config.isXmlFileName(tested), MessageFormat.format("Did falsely recognize {0} as xml!", tested));
		}
	
	}

	@Test
	void testGetXmlFile() {
		String fileName; 
		Config test;
		fileName = "Test File";
		test = new Config(fileName, false);
		assertFalse(test.getXmlFile().isPresent()); 
		assertNotEquals(fileName, test.getXmlFile().orElse(null), "non-xml file passed as xml file");
		
		fileName = ".infinitybot/config.xml"; 
		test = new Config(fileName, false);
		assertTrue(test.getXmlFile().isPresent()); 
		assertEquals(fileName, test.getXmlFile().orElse(null), "XML File did not pass as xml file");
	}

	@Test
	void testGetTextFile() {
		String fileName; 
		Config test;
		fileName = "Test File";
		test = new Config(fileName, false);
		assertTrue(test.getTextFile().isPresent()); 
		assertEquals(fileName, test.getTextFile().orElse(null));

		fileName = "infinitybot/test.xml"; 
		test = new Config(fileName, false);
		assertFalse(test.getTextFile().isPresent()); 
		assertNotEquals(fileName, test.getTextFile().orElse(null));
	}

}
