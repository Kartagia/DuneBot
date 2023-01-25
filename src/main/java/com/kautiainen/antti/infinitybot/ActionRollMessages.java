package com.kautiainen.antti.infinitybot;

import java.util.ListResourceBundle;

import com.kautiainen.antti.infinitybot.DiscordBot.ActionRollCommand;

/**
 * The default message resource bundle for effect roll messages.
 * 
 * @author Antti Kautiainen
 *
 */
public class ActionRollMessages extends ListResourceBundle {
	protected Object[][] getContents() {
		return new Object[][] { { 
			ActionRollCommand.DIFFICULTY_TOO_LOW_MESSAGE, "nothing is that easy" },
				{ ActionRollCommand.DELIMITER_MESSAGE, ", and" },
				{ ActionRollCommand.COMPLICATION_TOO_HIGH_MESSAGE, "it cannot be safer than safe" },
				{ ActionRollCommand.COMPLICATION_TOO_LOW_MESSAGE, "it cannot be riskier than the stealing of the Sampo" },
				{ ActionRollCommand.FOCUS_TOO_HIGH_MESSAGE, "not even Väinämöinen has that high focus" },
				{ ActionRollCommand.FOCUS_TOO_LOW_MESSAGE, "not even Joukahainen has that low focus" },
				{ ActionRollCommand.TN_TOO_HIGH_MESSAGE, "you do not look like Väinämöinen to me" },
				{ ActionRollCommand.TN_TOO_LOW_MESSAGE, "not even Joukahainen is that unskilled" },
				{ ActionRollCommand.TOO_MANY_DICE_MESSAGE, "not even I can throw that many dice" },
				{ ActionRollCommand.TOO_FEW_DICE_MESSAGE, "not even I can throw dice that does not exists" },
				{ ActionRollCommand.DIFFICULTY_TOO_HARD_MESSAGE, "nothing is that hard" } };
	};
}