package com.kautiainen.antti.infinitybot;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.kautiainen.antti.infinitybot.model.Special;

public class SpecialRegistry extends TreeMap<String, Special> {

	/** Create a new empty special registry.
	 * 
	 */
	public SpecialRegistry() {
	}


	/**
	 * Create a new special registry with given initial values. 
	 * @param m The initial values.
	 * @throws IllegalArgumentException Any value of the map was invalid. 
	 */
	public SpecialRegistry(Map<String, Special> m) {
		super(m);
	}

	/**
	 * Create a new special registry with given initial values. 
	 * @param m The initial values.
	 * @throws IllegalArgumentException Any value of the map was invalid. 
	 */
	public SpecialRegistry(SortedMap<String, Special> m) {
		super(m);
	}

	/**
	 * The serialized version of the registry. 
	 */
	private static final long serialVersionUID = 9124208833245497346L;

	
	
	@Override
	public boolean containsValue(Object value) {
		return value != null && value instanceof Special && 
				containsValue((Special)value);
	}
	
	/**
	 * Do the mapping contain given value. 
	 * @param special The tested value. 
	 * @return True, if and only if the mapping contains given special value. 
	 */
	public boolean containsValue(Special special) {
		String name = special==null?null:special.getName(); 
		return containsKey(name) && special.equals(get(name)); 
	}


	/**
	 * Tests the given key value pair. 
	 * @param key The tested key. 
	 * @param value The tested value. 
	 * @return True, if and only if the given key and value are valid. 
	 */
	public boolean validValue(String key, Special value) {
		return (Special.validName(key) && (value!=null && key.equals(value.getName())));
	}
	
	@Override
	public Special put(String key, Special value) {
		if (validValue(key, value)) {
			return super.put(key, value);
		} else {
			throw new IllegalArgumentException("Invalid key!"); 
		}
	}

	@Override
	public boolean replace(String key, Special oldValue, Special newValue) {
		if (validValue(key, newValue) && validValue(key, oldValue)) {
			// Performing replacement
			return super.replace(key, oldValue, newValue);
		} else if (validValue(key, oldValue)) {
			// Old value cannot belong to the list. 
			return false; 
		} else {
			// The given new value is invalid. 
			throw new IllegalArgumentException("Invalid new value!"); 
		}
	}

	@Override
	public Special replace(String key, Special value) {
		if (validValue(key, value)) {
			return super.replace(key, value);
		} else {
			throw new IllegalArgumentException("Invalid value!");
		}
	}

	/**
	 * Adds special into the registry. 
	 * @param added The added special. 
	 * @return True, if and only if the special was added. 
	 */
	public boolean register(Special added) {
		if (added != null && validValue(added.getName(), added) && !containsKey(added.getName())) {
			this.put(added.getName(), added);
			return true; 
		} else {
			return false; 
		}
	}
	
	/**
	 * Remove the special of the given name from registry. 
	 * @param name The name of the removed special. 
	 * @return True, if and only if the special was removed. 
	 */
	public boolean unregister(String name) {
		if (this.containsKey(name)) {
			this.remove(name);
			return true; 
		} else {
			return false; 
		}
	}
	
	/**
	 * Removes special from the registry, if it exist in registry. 
	 * @param special The removed special. 
	 * @return True, if and only if the special was removed from registry. 
	 */
	public boolean unregister(Special special) {
		if (special != null && this.containsValue(special)) {
			this.remove(special.getName());
			return true; 
		} else {
			return false; 
		}
	}
}
