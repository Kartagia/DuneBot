package com.kautiainen.antti.infinitybot;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LevelTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		
		
	}

	@Test 
	void testValues() {
		List<Level> expected = Arrays.asList(Level.all, Level.debug, Level.warning, Level.error, Level.info, Level.disabled); 
		System.out.printf("%s\n", expected);
	}
	
	@Test
	void testEquals() {
		
	}

	@Test
	void testToString() {
	}

}
