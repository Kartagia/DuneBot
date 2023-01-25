package com.kautiainen.antti.infinitybot;

import java.util.ListResourceBundle;

import com.kautiainen.antti.infinitybot.DiscordBot.ActionRollCommand;

/**
 * The Finnish message resource bundle for effect roll messages.
 * 
 * @author Antti Kautiainen
 *
 */
public class ActionRollMessages_fi extends ListResourceBundle {
	protected Object[][] getContents() {
		return new Object[][] { { ActionRollCommand.DIFFICULTY_TOO_LOW_MESSAGE, 
			"Ei se voi noin helppoa voi olla" },
				{ ActionRollCommand.DELIMITER_MESSAGE, ", ja" },
				{ ActionRollCommand.COMPLICATION_TOO_HIGH_MESSAGE, 
					"Mikään ei ole turvallisempaa kuin turvallinen" },
				{ ActionRollCommand.COMPLICATION_TOO_LOW_MESSAGE, "Ei se voi Sammon ryöstöä uhmapäisempää" },
				{ ActionRollCommand.FOCUS_TOO_HIGH_MESSAGE, "Ei edes Väinämöinen ole noin pätevä" },
				{ ActionRollCommand.FOCUS_TOO_LOW_MESSAGE, "Ei edes Joukahainen ole noin osaamaton" },
				{ ActionRollCommand.TN_TOO_HIGH_MESSAGE, "Et sie näytä Väinämöiseltä" },
				{ ActionRollCommand.TN_TOO_LOW_MESSAGE, "Edes Joukahainen ei ole noin huono haastamahan" },
				{ ActionRollCommand.DIFFICULTY_TOO_LOW_MESSAGE, "Ei se voi noin helppoa olla" },
				{ ActionRollCommand.TOO_MANY_DICE_MESSAGE, "En edes minä jättinä voi heittää noin montaa noppaa" },
				{ ActionRollCommand.TOO_FEW_DICE_MESSAGE, "En edes minä kykene heittämään noppia,joita ei ole" },
				{ ActionRollCommand.DIFFICULTY_TOO_HARD_MESSAGE, "Mikään ei ole noin vaikeaa" } };
	};
}