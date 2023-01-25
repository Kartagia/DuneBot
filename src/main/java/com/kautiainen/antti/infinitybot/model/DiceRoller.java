package com.kautiainen.antti.infinitybot.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.kautiainen.antti.infinitybot.DiscordBot;
import com.kautiainen.antti.infinitybot.Logging;
import com.kautiainen.antti.infinitybot.DiscordBot.StackingSpecial;

/**
 * Dice roller represents random number generator.
 * 
 * @author Antti Kautiainen
 *
 */
public class DiceRoller extends Logging {
	
	/**
	 * Create new dice roller with given random number generator.
	 * 
	 * @param random The random number generator.
	 */
	public DiceRoller(Random random) {
		this.rnd = random;
	}

	/**
	 * Create a new roller with given seed.
	 * 
	 * @param seed The random generation seed.
	 */
	public DiceRoller(long seed) {
		this.rnd = new java.util.Random(seed);
	}

	/**
	 * Create a new roller with current time as seed.
	 */
	public DiceRoller() {
	}

	/**
	 * The random number generator.
	 */
	private java.util.Random rnd = new java.util.Random();

	/**
	 * Calculate the total value of the given specials list.
	 * 
	 * @param specials The list of specials.
	 * @return The total value of the specials.
	 */
	public int getSpecialsValue(java.util.List<? extends Special> specials) {
		return specials == null ? 0
				: specials.stream()
						.map((Special special) -> (special == null ? 0 : special.getNumberValue().orElse(0)))
						.collect(Collectors.summingInt((Integer value) -> (value)));
	}

	/**
	 * Combines totals of specials.
	 * 
	 * @param specials
	 * @return
	 */
	public java.util.List<Special> combineSpecials(List<Special> specials) {
		java.util.List<Special> result = new ArrayList<>();
		if (specials != null) {
			specials.stream().forEachOrdered((Special special) -> {
				addSpecial(result, special);
			});
		}
		return result;
	}

	/**
	 * Adds given special to the list of specials. 
	 * @param orderedList The ordered list into which the values are added. 
	 * @param added The added specials. 
	 */
	public void addSpecial(List<Special> orderedList, List<Special> added) {
		if (added != null && orderedList != null) {
			for (Special special: added) {
				addSpecial(orderedList, special); 
			}
		}
	}
	
	/**
	 * Adds given special to the list of specials.
	 * 
	 * @param orderedList The list of specials ordered by the name of the special. 
	 *  (The natural order of specials does implement suitable order as the name 
	 *  is the most significant key). 
	 * @param added The added special. 
	 */
	public void addSpecial(List<Special> orderedList, Special added) {
		if (added != null && orderedList != null) {
			int index = Collections.binarySearch(orderedList, added, 
					(Special a, Special b)->(a.getName().compareTo(b.getName())));
			debug(String.format("Adding %s to %s at index %d", added, orderedList, index)); 
			if (index >= 0) {
				// The value was found - checking if it is stacking.
				if (orderedList.get(index).stacks()) {
					// Replacing the found value with stacked value.
					debug(String.format("DiceRoller#addSpecial: Replacing value at index %d with %s", index, orderedList.get(index).getStacked(added.getValue()))); 
					orderedList.set(index, orderedList.get(index).getStacked(added.getValue()));
				} else if (added.stacks()) {
					// Replacing all elements with same stacking value with value stacked with
					// the given added.
					debug(String.format("DiceRoller#addSpecial: Replacing value at index %d with %s", index, orderedList.get(index).getStacked(added.getValue()))); 
					orderedList.set(index, added.getStacked(orderedList.get(index).getValue()));
				} else {
					// Ignoring duplicates of non-stacking elements.
				}
			} else {
				// There is no instance of value in the list.
				debug(String.format("Adding new entry %s", added)); 
				int oldSize = orderedList.size(); 
				orderedList.add(-1-index, added); 
				if (oldSize +1 != orderedList.size()) {
					throw new IllegalStateException("Could not add new special"); 
				} else if (orderedList.get(-1-index) != added) {
					throw new IllegalStateException("The added element does not exist where it should!"); 
				}
				debug(String.format("Added %s to %s at %d", added, orderedList, -1 -index)); 
			}
		}
	}

	/**
	 * Generates the roll result of the given base number, roll result, and specials
	 * per effect.
	 * 
	 * @param baseNumber The base number of the total.
	 * @param rollResult The roll result as objects. Values are either integers, or
	 *                   string "S".
	 * @param specials   The list of special effects incurring for every effect roll
	 *                   (S) on dice pool.
	 * @return THe roll result of the given roll.
	 */
	public RollResult getCDRollResult(
			int baseNumber, 
			java.util.List<Object> rollResult,
			java.util.List<Special> specials) {
		return getCDRollResult(baseNumber, rollResult, specials, getSpecialsValue(specials));
	}

	/**
	 * Generates the roll result of the given base number, roll result, and specials
	 * per effect.
	 * 
	 * @param baseNumber The base number of the total.
	 * @param rollResult The roll result as objects. Values are either integers, or
	 *                   string "S".
	 * @param specials   The list of special effects incurring for every effect roll
	 *                   (S) on dice pool.
	 * @return THe roll result of the given roll.
	 */
	public RollResult getCDRollResult(int baseNumber, java.util.List<Object> rollResult,
			java.util.List<Special> specials, int specialTotal) {
		int value = baseNumber;
		java.util.List<String> rolls = new java.util.ArrayList<>((rollResult == null ? 0 : rollResult.size()));
		java.util.List<Special> effectTotal = new java.util.ArrayList<>((specials == null ? 0 : specials.size()));
		for (Object roll : rollResult) {
			rolls.add(roll.toString());
			if (roll instanceof String) {
				if (specials != null)
					addSpecial(effectTotal, specials);
				value += specialTotal;
			} else {
				value += (Integer) roll;
			}
		}
		return new RollResult(value, rolls, effectTotal);
	}
	
	/**
	 * The effect result indicator.
	 */
	public static final String EFFECT_RESULT = "S";
	
	/**
	 * The old combat die used by the Infinity and the Mutant Chronicles.
	 */
	public static final java.util.List<Object> OLD_COMBAT_DIE = Arrays.asList(1,2,0,0,0,0,EFFECT_RESULT);

	
	/**
	 * The new combat die used by newer games and by the SRD.
	 */
	public static final java.util.List<Object> NEW_COMBAT_DIE = Arrays.asList(1,2,0,0,0,EFFECT_RESULT,EFFECT_RESULT);
	
	/**
	 * Get the CD roll result.
	 * 
	 * @param baseNumber   The base value of the roll.
	 * @param dicePool     The dice pool - how many CDs is rolled.
	 * @param specials     The sorted specials list.
	 * @param specialTotal The total numeric value of the specials.
	 * @return The roll result.
	 */
	protected RollResult rollCD(int baseNumber, int dicePool, java.util.List<Special> specials, int specialTotal) {
		return rollCD(OLD_COMBAT_DIE, baseNumber, dicePool, specials, specialTotal);
	}

	/**
	 * Perform a combat die roll with given combat die.
	 * 
	 * @param specialDie The combat die as an array.
	 * @param baseNumber The base number of the roll.
	 * @param dicePool The dice pool of the roll.
	 * @param specials The specials triggered by the special result of the combat die.
	 * @param specialTotal The value total of the given specials.
	 * @return The roll result of the combat dice roll.
	 */
	public RollResult rollCD(List<Object> specialDie, int baseNumber, int dicePool, java.util.List<Special> specials, int specialTotal)  {
		java.util.List<Object> rolls = new java.util.ArrayList<>(dicePool);
		List<Object> sides = specialDie==null?Arrays.asList(1,2,3,4,5,6):specialDie;
		for (int i = 0; i < dicePool; i++) {
			Object roll = sides.get(rnd.nextInt(sides.size()));
			rolls.add(roll);
		}
		return getCDRollResult(baseNumber, rolls, specials);
	}
	
	/**
	 * Roll a combat dice roll with given list of specials happening on effect.
	 * 
	 * @param baseNumber The base number of the roll.
	 * @param dicePool The number of dice rolled.
	 * @param specials The list of specials on effect roll.
	 * @return The result of the combat dice roll.
	 */
	public RollResult rollCD(int baseNumber, int dicePool, java.util.List<Special> specials) {
		return rollCD(baseNumber, dicePool, specials, getSpecialsValue(specials));
	}

	/**
	 * Roll a combat dice pool with given base value, dice pool, and specials.
	 * 
	 * @param baseNumber The base number.
	 * @param dicePool The dice pool.
	 * @param specials The list of specials happening on effect on a combat die.
	 * @return The roll result.
	 */
	public RollResult rollCD(int baseNumber, int dicePool, Special... specials) {
		if (specials == null || specials.length == 0) {
			// The specials total is zero.
			return rollCD(baseNumber, dicePool, Collections.emptyList(), 0);
		} else {
			// Getting through specials list given by the user, and skipping all undefined
			// values.
			java.util.List<Special> specialsList = new java.util.ArrayList<>(specials.length);
			for (int index = 0, end = specials.length; index < end; index++) {
				addSpecial(specialsList, specials[index]);
			}
			return rollCD(baseNumber, dicePool, specialsList);
		}
	}

	/**
	 * The complication is stacking special effect for roll determining the total
	 * number of complications the roll had.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class Complication extends StackingSpecial {
		/**
		 * The name of the complication special.
		 */
		public static final String COMPLICATION_NAME = "Complication";

		/**
		 * Create a new default complication with value of 1.
		 */
		public Complication() {
			this(1);
		}

		/**
		 * Create a complication with given complication value.
		 * 
		 * @param value The complication value.
		 */
		public Complication(int value) {
			super(COMPLICATION_NAME, value);
		}
		
		/**
		 * Replacing the to string version which returns empty string on 
		 * complication without value.
		 * 
		 * @return The string representation of the complication.
		 */
		public String toString() {
			return getValue() == 0?"":super.toString();
		}
	}

	/**
	 * Formats single d20 roll for standard roll.
	 * 
	 * @param roll         The die roll result.
	 * @param success      The success threshold.
	 * @param critical     The critical success threshold.
	 * @param complication The complication threshold.
	 * @return The string formatting the resulting value.
	 */
	public String formatRoll(int roll, boolean success, boolean critical, boolean complication) {
		String result = Integer.toString(roll);
		if (success) {
			if (critical) {
				// Bolding critical results.
				result = "**" + result + "**";
			}
		} else {
			// Putting failures with strike-through.
			result = "~~" + result + "~~";
		}
		if (complication) {
			// Underlining complications.
			result = "__" + result + "__";
		}
		return result.toString();
	}

	/**
	 * Performs action roll.
	 * 
	 * @param diceNumber        The dice number. Between 1 and 5.
	 * @param TN                The target number for each dice.
	 * @param criticalRange		The critical success range. Any roll less than this is an additional success.
	 * @param complicationRange The complication range. Results greater than this
	 *                          value cause complication.
	 * @return The roll result with value equal to the total number of successes,
	 *         and specials containing the number the effects.
	 */
	public RollResult rollAction(int diceNumber, int TN, int criticalRange, int complicationRange) {
		int result = 0, complications = 0;
		List<String> rolls = new ArrayList<>();
		int roll;
		for (int i = 0; i < diceNumber; i++) {
			roll = rnd.nextInt(20) + 1;
			if (roll <= TN) {
				result += (roll <= criticalRange ? 2 : 1);
			}
			if (roll >= complicationRange) {
				complications++;
			}
			// Adding the roll to the results
			rolls.add(formatRoll(roll, (roll <= TN), (roll <= criticalRange), (roll >= complicationRange)));
		}
		return new RollResult(result, rolls, new Complication(complications));
	}
}