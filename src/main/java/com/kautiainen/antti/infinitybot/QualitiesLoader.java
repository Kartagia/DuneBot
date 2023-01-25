package com.kautiainen.antti.infinitybot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.kautiainen.antti.infinitybot.model.FunctionalSpecial;
import com.kautiainen.antti.infinitybot.model.Quality;
import com.kautiainen.antti.infinitybot.model.QualitySpecial;

/**
 * Quality loader loads the quality.
 * 
 * @author Antti Kautiainen
 *
 */
public class QualitiesLoader {

	/**
	 * Constructing a new quality registry.  
	 * @return The new quality registry into which the qualities are added. 
	 */
	public QualityRegistry createNewRegistry() {
		return new QualityRegistry();
	}

	/**
	 * Load qualities from the given stream. 
	 * @param stream The source stream. 
	 * @return The quality registry with qualities loaded from the given stream. 
	 * @throws IOException The loading failed due Input Error. 
	 */
	public QualityRegistry loadRegistry(java.io.InputStream stream) throws IOException {
		return loadRegistry(createNewRegistry(), stream);
	}

	/**
	 * Loads XML registry from given reader.
	 * 
	 * The registry is created as registry of templates.
	 * 
	 * @param registry The registry into which the result is attached. If undefined,
	 *                 an new registry is created.
	 * @param reader   The reader from which the entries are read.
	 * @return The registry containing the read qualities. If the defined registry
	 *         was given, it is returned.
	 * @throws IOException The loading failed due IO error.
	 */
	public QualityRegistry loadXMLRegistry(QualityRegistry registry, InputStream reader) throws IOException {
		if (registry == null)
			return null;

		// Loading the quality information from the file.
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			// Adding configuration for document builder factory.

			// Building the document builder
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(reader);
			Element root = doc.getDocumentElement();
			NodeList qualityGroups = root.getElementsByTagName("group"), qualities;
			String groupName;
			Element group, special;
			for (int i = 0, end = qualityGroups.getLength(); i < end; i++) {
				group = (Element) qualityGroups.item(i);
				if (group != null) {
					groupName = group.getAttribute("name");
					qualities = group.getElementsByTagName("quality");
					for (int qi = 0, childCount = qualities.getLength(); qi < childCount; qi++) {
						registry.register(groupName, createQuality(registry, qualities.item(qi)));
					}
				}
			}
		} catch (ParserConfigurationException pce) {
			// THis should never happen.
			throw new Error("!!!Quality Registry XML Parser configuration failed!!!");
		} catch (SAXException saxe) {
			// The file was corrupted.
			throw new java.io.StreamCorruptedException("Invalid xml document: " + saxe.getMessage());
		}

		return registry;
	}

	/**
	 * Create a new quality from node.
	 * 
	 * @param registry The registry for which the value is read.
	 * @param item     The read node.
	 * @return The created node.
	 */
	private QualitySpecial createQuality(QualityRegistry registry, Node item) {
		if (item == null) {
			return null;
		} else if (item.getNodeType() == Node.TEXT_NODE || item.getNodeType() == Node.CDATA_SECTION_NODE) {
			// Creating from string representation.
			return createQuality(registry, item.getTextContent());
		} else if (item.getNodeType() == Node.ELEMENT_NODE) {
			// Creating quality from given element.
			Element quality = (Element) item;
			Integer level = null;
			Integer maxLevel = null;
			Function<Integer, Optional<Integer>> valueFunc = FunctionalSpecial.EMPTY_VALUE_FUNC;

			String attrName, attrValue;
			// Checking the quality level.
			if (quality.hasAttribute(attrName = "level")) {
				attrValue = quality.getAttribute(attrName).trim();
				if (attrValue.contentEquals("x") || attrValue.equals("X")) {
					// The attribute will have unlimited stacking.
					level = 1;
				} else {
					try {
						level = Integer.parseInt(attrValue.trim());
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Non-numeric quality level!");
					}
				}
			}
			// Checking if the quality has value.
			if (quality.hasAttribute(attrName = "value")) {
				attrValue = quality.getAttribute(attrName).trim();
				if (attrValue.contentEquals("x") || attrValue.equals("X")) {
					valueFunc = FunctionalSpecial.CURRENT_VALUE_FUNC;
				} else {
					try {
						maxLevel = Integer.parseInt(attrValue);
					} catch (NumberFormatException nfe) {
						throw new IllegalArgumentException("Non-numeric maximum quality level!");
					}
				}
			}
			// The value has multiplier.
			if (quality.hasAttribute(attrName = "multiplier")) {
				attrValue = quality.getAttribute(attrName);
				try {
					final int multiplier = Integer.parseInt(attrValue);
					valueFunc = valueFunc.andThen((Optional<Integer> value) -> {
						if (value.isPresent()) {
							return Optional.of(multiplier * value.get());
						} else {
							return value;
						}
					});
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException("Invalid value multiplier" + quality.toString());
				}
			}

			return new Quality(quality.getAttribute("name"), level, maxLevel, valueFunc);
		} else {
			// Unknown node type.
			return null;
		}
	}
	
	
	/**
	 * Qualities is quality registry which takes into account the opposite qualities. 
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class Qualities extends QualityRegistry {
		
		/**
		 * Get the serialization version of the qualities.
		 */
		private static final long serialVersionUID = 1L;
		
		private Optional<QualityTemplates> allowedQualities = Optional.empty();
		
		public Qualities() {
			
		}
		
		public Qualities(QualityTemplates allowedQualities) {
			this.allowedQualities = Optional.ofNullable(allowedQualities); 
		}
		
		/**
		 * Check validity of the quality.
		 * 
		 * @param quality The validated quality.
		 * @return True, if and only if the given quality is valid.
		 */
		public boolean validQuality(QualitySpecial quality) {
			return allowedQualities.isEmpty() || allowedQualities.get().containsKey(quality.getName());
		}
		
		/**
		 * Add quality to the qualities. 
		 * @param quality The added quality. 
		 */
		public void register(QualitySpecial quality) {
			if (validQuality(quality)) {
				super.register(quality);
			} else {
				throw new IllegalArgumentException("Quality rejected");
			}
		}
		
	}

	/**
	 * Loads XML registry from given reader.
	 * 
	 * The registry is created as registry of templates.
	 * 
	 * @param registry The registry into which the result is attached. If undefined,
	 *                 an new registry is created.
	 * @param reader   The reader from which the entries are read.
	 * @return The registry containing the read qualities. If the defined registry
	 *         was given, it is returned.
	 * @throws IOException The loading failed due IO error.
	 */
	public QualityRegistry loadRegistry(QualityRegistry registry, InputStream reader) throws IOException {
		if (registry == null)
			return null;
		return loadRegistry(registry, reader == null ? null : new BufferedReader(new InputStreamReader(reader)));
	}

	/**
	 * Create a new quality from string representation.
	 * 
	 * @param item The string representation of the quality.
	 * @return The created quality.
	 */
	private QualitySpecial createQuality(QualityRegistry registry, String stringRep) {
		if (stringRep == null) {
			return null;
		}
		Pattern delimiterPattern = Pattern.compile("(?:$|,\\s+)");
		Pattern qualityPattern = 
				Pattern.compile(
				Quality.fromStringPattern().toString() + delimiterPattern.toString());
		Matcher match;
		if ((match = qualityPattern.matcher(stringRep)).matches()) {
			int index = 1;
			String name = match.group(index++);
			String level = match.group(index++);
			String flags = match.group(index++);
			return registry.createQuality(name, level, flags);
		}
		return null;
	}

	/**
	 * Read registry from the text file.
	 * 
	 * @param registry The registry into which the result is attached. If undefined,
	 *                 an new registry is created.
	 * @param reader   The reader from which the entries are read.
	 * @return The registry containing the read qualities. If the defined registry
	 *         was given, it is returned.
	 * @throws IOException The loading failed due IO error.
	 */
	public QualityRegistry loadRegistry(QualityRegistry registry, BufferedReader reader) throws IOException {
		if (registry == null) {
			registry = this.createNewRegistry();
		}

		// Loading the quality information from the file.
		Pattern categoryStart = Pattern.compile("^" + DiscordBot.WORD_PATTERN + "$");
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().isEmpty()) {
				// Skipping empty lines, but next line starts new category.
			} else if (categoryStart.matcher(line).matches()) {
				// We have category start.
			} else {
				// WE have line containing quality.
			}
		}

		return registry;
	}

	public QualityRegistry loadRegistry(BufferedReader reader) throws IOException {
		return loadRegistry(createNewRegistry(), reader);
	}

}