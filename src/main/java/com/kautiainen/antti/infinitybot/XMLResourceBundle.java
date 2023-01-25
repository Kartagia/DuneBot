package com.kautiainen.antti.infinitybot;

import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

/**
 * XML Resource bundle handling resource bundles stored as XML Properties file. 
 * @author Antti Kautiainen
 *
 */
public class XMLResourceBundle extends ResourceBundle {
	
	private java.util.Properties properties = new java.util.Properties(); 
	
	public XMLResourceBundle(String baseBundleName) {
		// Reading the properties from given name. 
	}
	
	public XMLResourceBundle(String baseBundleName, Locale locale) {
		// Readign hte properites from given locale. 
		
	}

	@Override
	protected Object handleGetObject(String key) {
		return properties.get(key); 
	}
	
	/**
	 * Setting property. 
	 * @param key The property name. 
	 * @param value The value of property. If undefined, revert to default. 
	 * @return The previous value of the property. 
	 * @throws IllegalArgumentException The value or key was invalid. 
	 */
	public String setProperty(String key, String value) throws IllegalArgumentException {
		String result = properties.getProperty(key);
		properties.setProperty(key, value);
		return result; 
	}

	@Override
	public Enumeration<String> getKeys() {
		return new Enumeration<String>() {
			private final Enumeration<Object> iter = properties.keys();

			@Override
			public boolean hasMoreElements() {
				return iter.hasMoreElements(); 
			}

			@Override
			public String nextElement() throws NoSuchElementException {
				return (String)iter.nextElement(); 
			}
		};
	}
}